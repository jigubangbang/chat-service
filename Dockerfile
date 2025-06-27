# 빌드 단계 (builder)
# 빌드 환경을 위한 OpenJDK 17 Slim 이미지를 사용합니다.
FROM openjdk:17-jdk-slim AS builder

# 작업 디렉토리를 /app으로 설정합니다.
WORKDIR /app

# Maven Wrapper 스크립트(mvnw)와 관련 설정(.mvn 폴더)을 복사합니다.
# 이 파일들은 Maven이 설치되지 않은 환경에서도 프로젝트를 빌드할 수 있게 해줍니다.
# 이 파일들은 빌드 컨텍스트(Jenkins 워크스페이스 루트)에 있어야 합니다.
COPY mvnw .
COPY .mvn .mvn

# pom.xml 복사
# pom.xml만 먼저 복사하여 의존성 다운로드 단계를 캐싱할 수 있도록 합니다.
COPY pom.xml .

# mvnw 스크립트에 실행 권한을 부여합니다.
# 이 단계가 성공해야 다음 Maven 명령을 실행할 수 있습니다.
RUN chmod +x mvnw

# Maven 의존성을 미리 다운로드합니다.
# 이 단계는 pom.xml이 변경되지 않는 한 Docker 캐시를 활용하여 빌드 속도를 높입니다.
RUN ./mvnw dependency:go-offline -B

# 소스 코드 복사
# 의존성 다운로드 이후에 소스 코드를 복사하여 소스 변경 시에도 의존성 캐시를 재활용합니다.
COPY src ./src

# 애플리케이션 빌드 (테스트 건너뛰기)
# 빌드된 JAR 파일은 일반적으로 target/ 디렉토리에 생성됩니다.
RUN ./mvnw package -DskipTests

# 빌드된 JAR 파일을 찾아서 'app.jar'로 이름을 변경하여 /app에 저장합니다.
# Spring Boot의 repackage 목표는 JAR 파일 이름을 변경하거나 .original 파일을 생성할 수 있으므로,
# target/*.jar를 사용하여 최종 빌드된 JAR 파일을 찾습니다.
ARG JAR_FILE_NAME=target/*.jar
RUN cp ${JAR_FILE_NAME} app.jar || { echo "JAR 파일이 target 디렉토리에서 발견되지 않았습니다. 빌드 로그를 확인하세요."; exit 1;}


# 실행 단계 (runtime)
# 애플리케이션 실행을 위한 더 작고 가벼운 OpenJDK 17 Slim 이미지를 사용합니다.
FROM openjdk:17-slim

# 'spring' 사용자를 시스템 사용자로 생성합니다. 이 단계는 ROOT 권한으로 실행되어야 합니다.
RUN useradd --system --uid 1000 spring

# entrypoint.sh 스크립트를 컨테이너 내의 /usr/local/bin 경로로 복사합니다.
# 이 단계는 ROOT 권한으로 실행되어야 합니다.
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
# 복사된 entrypoint.sh 스크립트에 실행 권한을 부여합니다.
# 이 단계는 ROOT 권한으로 실행되어야 합니다.
RUN chmod +x /usr/local/bin/entrypoint.sh

# 이제부터 'spring' 사용자로 전환합니다.
# 이후의 모든 명령은 'spring' 사용자 권한으로 실행됩니다.
USER spring

# 임시 디렉토리 볼륨을 설정합니다. (Spring Boot 애플리케이션에 유용)
VOLUME /tmp

# Chat Service의 기본 포트 8083을 노출합니다. (실제 사용하는 포트로 변경 필요)
EXPOSE 8083

# 빌드 단계에서 생성된 app.jar 복사
COPY --from=builder /app/app.jar /app/app.jar

# 애플리케이션 실행 명령어 (진입점)를 정의합니다.
# Docker 컨테이너가 시작될 때 이 명령어가 실행됩니다.
ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]

# Spring 프로파일을 'production'으로 설정하는 환경 변수입니다. (선택 사항, 필요에 따라 변경)
ENV SPRING_PROFILES_ACTIVE=production
