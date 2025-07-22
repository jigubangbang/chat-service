pipeline {
    agent any

    // Jenkins Job 실행 시 수동으로 전체 재배포를 강제할 수 있는 파라미터
    parameters {
        booleanParam(defaultValue: true, description: 'Force full deployment of chat-service (ignores changes).', name: 'FORCE_FULL_DEPLOY')
    }

    environment {
        // NAS 환경에 맞는 Docker Registry와 Kubeconfig 경로 설정
        DOCKER_REGISTRY = 'localhost:5000' 
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        KUBECONFIG = '/var/lib/jenkins/.kube/config' 
        NAMESPACE = 'bit-2503' // Kubernetes deployment.yaml의 네임스페이스와 일치
    }

    // GitHub 웹훅 트리거 (deployNAS 브랜치에 푸시 시)
    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                // chat-service 단일 레포지토리 사용
                git branch: 'deployNAS', credentialsId: 'github-token-333', url: 'https://github.com/jigubangbang/chat-service.git'
            }
        }

        stage('Cleanup Failed Resources') {
            steps {
                script {
                    echo "=== 실패한 리소스 정리 ==="
                    sh """
                        export KUBECONFIG=${env.KUBECONFIG}
                        
                        echo "Cleaning up failed ReplicaSets..."
                        kubectl get rs -n ${env.NAMESPACE} --no-headers | awk '\$2 == 0 && \$3 == 0 && \$4 == 0 {print \$1}' | xargs -r kubectl delete rs -n ${env.NAMESPACE}
                        
                        echo "Cleaning up failed Pods..."
                        kubectl get pods -n ${env.NAMESPACE} --field-selector=status.phase=Failed -o name | xargs -r kubectl delete -n ${env.NAMESPACE}
                        kubectl get pods -n ${env.NAMESPACE} --field-selector=status.phase=Error -o name | xargs -r kubectl delete -n ${env.NAMESPACE}
                        
                        echo "Cleaning up old Docker images for current project (chat-service)..."
                        docker rmi \$(docker images ${env.DOCKER_REGISTRY}/chat-service --format "{{.Repository}}:{{.Tag}}" | grep -v ":${env.IMAGE_TAG}\\|:latest" | head -5) 2>/dev/null || true
                        
                        echo "Current resource usage:"
                        kubectl get resourcequota -n ${env.NAMESPACE} || echo "No resource quota found"
                        kubectl get pods -n ${env.NAMESPACE}
                    """
                }
            }
        }

        stage('Determine Changes') {
            steps {
                script {
                    // 모노레포가 아니므로 변경 감지 로직은 단순화.
                    // chat-service repo 자체의 변경은 항상 빌드/배포로 간주.
                    echo "Build Number: ${env.BUILD_NUMBER}"
                    echo "Git Commit: ${env.GIT_COMMIT}"
                    echo "Generated IMAGE_TAG: ${env.IMAGE_TAG}"

                    // FORCE_FULL_DEPLOY 파라미터 또는 첫 빌드 시 항상 배포
                    if (params.FORCE_FULL_DEPLOY || env.BUILD_NUMBER == '1') {
                        env.SHOULD_DEPLOY_CHAT_SERVICE = 'true'
                        echo "Force full deployment or first build - deploying chat-service."
                    } else {
                        // 변경 사항 감지 (단일 레포이므로 단순히 변경이 있으면 배포)
                        def rawChanges = sh(returnStdout: true, script: 'git diff --name-only HEAD HEAD^1 || true').trim()
                        if (!rawChanges.isEmpty()) {
                            env.SHOULD_DEPLOY_CHAT_SERVICE = 'true'
                            echo "Detected changes in chat-service repository. Deploying."
                        } else {
                            env.SHOULD_DEPLOY_CHAT_SERVICE = 'false'
                            echo "No changes detected in chat-service. Skipping deployment."
                            currentBuild.result = 'SUCCESS'
                            return // 변경 사항이 없으면 파이프라인 종료
                        }
                    }
                }
            }
        }

        stage('Deploy Chat Service') {
            when {
                expression { return env.SHOULD_DEPLOY_CHAT_SERVICE == 'true' }
            }
            steps {
                script {
                    // Config Server와 Eureka Server 대기 (NAS K8s 클러스터 내에서)
                    echo "Waiting for Config Server and Eureka Server to be ready in ${env.NAMESPACE}..."
                    retry(5) { // 재시도 횟수 증가
                        sh "KUBECONFIG=${env.KUBECONFIG} kubectl wait --for=condition=available deployment/config-server-deployment -n ${env.NAMESPACE} --timeout=300s || exit 1"
                    }
                    echo "Config Server is ready."

                    retry(5) { // 재시도 횟수 증가
                        sh "KUBECONFIG=${env.KUBECONFIG} kubectl wait --for=condition=available deployment/eureka-server-deployment -n ${env.NAMESPACE} --timeout=300s || exit 1"
                    }
                    echo "Eureka Server is ready. Proceeding with Chat Service."

                    def serviceName = 'chat-service'
                    def imageUrl = "${env.DOCKER_REGISTRY}/${serviceName}:${env.IMAGE_TAG}"

                    echo "--- Building Docker image: ${imageUrl} ---"
                    sh """
                        docker build -t ${imageUrl} .
                        docker push ${imageUrl}
                    """
                    echo "--- Docker image pushed: ${imageUrl} ---"

                    // --- 기존 배포 강제 삭제 (새로운 이미지의 클린한 롤아웃을 보장하기 위해) ---
                    echo "--- Existing deployment cleanup (to ensure clean rollout) ---"
                    sh "KUBECONFIG=${env.KUBECONFIG} kubectl delete deployment chat-service-deployment -n ${env.NAMESPACE} --ignore-not-found || true"
                    sh "KUBECONFIG=${env.KUBECONFIG} kubectl wait --for=delete deployment/chat-service-deployment -n ${env.NAMESPACE} --timeout=300s --for=delete || true"
                    echo "--- Existing deployment deleted (if it existed) ---"

                    // Kubernetes 배포 (NAS 환경)
                    echo "--- Applying Kubernetes deployments for ${serviceName} ---"
                    sh """
                        export KUBECONFIG=${env.KUBECONFIG}
                        
                        sed -i "s|__ECR_IMAGE_FULL_PATH__|${imageUrl}|g" k8s/deployment.yaml
                        
                        kubectl apply -f k8s/deployment.yaml -n ${env.NAMESPACE}
                        kubectl apply -f k8s/service.yaml -n ${env.NAMESPACE}
                    """
                    echo "--- Kubernetes deployments applied for ${serviceName} ---"

                    // --- Kubernetes Deployment Debugging (Chat Service 파드 관련) ---
                    echo "--- Kubernetes Deployment Debugging (Chat Service Pods) ---"
                    echo "Pods in namespace ${env.NAMESPACE} after apply:"
                    sh "KUBECONFIG=${env.KUBECONFIG} kubectl get pods -n ${env.NAMESPACE} -l app=${serviceName} || true"
                    echo "Deployment events:"
                    sh "KUBECONFIG=${env.KUBECONFIG} kubectl describe deployment/${serviceName}-deployment -n ${env.NAMESPACE} || true"

                    echo "Main container logs:"
                    sh "KUBECONFIG=${env.KUBECONFIG} kubectl get pods -n ${env.NAMESPACE} -l app=${serviceName} -o custom-columns=NAME:.metadata.name --no-headers | xargs -r -I {} sh -c 'echo \"--- Main container {} logs: ---\"; KUBECONFIG=${env.KUBECONFIG} kubectl logs {} -n ${env.NAMESPACE} -c ${serviceName}-container || true; echo \"\";' || true"

                    echo "Init container logs (wait-for-config-server):"
                    sh "KUBECONFIG=${env.KUBECONFIG} kubectl get pods -n ${env.NAMESPACE} -l app=${serviceName} -o custom-columns=NAME:.metadata.name --no-headers | xargs -r -I {} sh -c 'echo \"--- Init container (config-server) {} logs: ---\"; KUBECONFIG=${env.KUBECONFIG} kubectl logs {} -n ${env.NAMESPACE} -c wait-for-config-server || true; echo \"\";' || true"

                    echo "Init container logs (wait-for-eureka):"
                    sh "KUBECONFIG=${env.KUBECONFIG} kubectl get pods -n ${env.NAMESPACE} -l app=${serviceName} -o custom-columns=NAME:.metadata.name --no-headers | xargs -r -I {} sh -c 'echo \"--- Init container (eureka) {} logs: ---\"; KUBECONFIG=${env.KUBECONFIG} kubectl logs {} -n ${env.NAMESPACE} -c wait-for-eureka || true; echo \"\";' || true"

                    echo "--- End Kubernetes Deployment Debugging (Chat Service Pods) ---"

                    // 롤아웃 상태 대기
                    sh """
                        KUBECONFIG=${env.KUBECONFIG} kubectl rollout status deployment/${serviceName}-deployment -n ${env.NAMESPACE} --timeout=600s || exit 1
                    """
                    echo "${serviceName} 배포 완료."
                }
            }
        }
        
        stage('Verify Deployment') { // 확인용 함수 추가
            steps {
                script {
                    echo "=== 배포 확인 ==="
                    sh """
                        export KUBECONFIG=${env.KUBECONFIG}
                        echo "Pods in namespace ${env.NAMESPACE}:"
                        kubectl get pods -n ${env.NAMESPACE}
                        echo "Services in namespace ${env.NAMESPACE}:"
                        kubectl get services -n ${env.NAMESPACE}
                    """
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        failure {
            echo "❌ CI/CD Pipeline failed for chat-service."
        }
        success {
            echo "✅ CI/CD Pipeline finished successfully for chat-service."
        }
    }
}