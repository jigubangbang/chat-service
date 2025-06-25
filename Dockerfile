# 빌드 단계 (builder)
FROM openjdk:17-jdk-slim AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Maven Wrapper 파일 및 설정 복사
COPY mvnw .
COPY .mvn .mvn

# pom.xml 복사
COPY pom.xml .

# Maven 의존성 미리 다운로드 (변경이 잦지 않으므로 캐싱 효과)
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드 (테스트 건너뛰기)
RUN ./mvnw package -DskipTests

# 빌드된 JAR 파일 찾기 및 app.jar로 이름 변경
ARG JAR_FILE_NAME=target/*.jar
RUN cp ${JAR_FILE_NAME} app.jar || { echo "JAR file not found"; exit 1;}

# 실행 단계 (runtime)
FROM openjdk:17-slim

# spring 사용자 생성 및 사용자로 전환
RUN useradd --system --uid 1000 spring
USER spring

# 임시 디렉토리 볼륨 설정 (Spring Boot 애플리케이션에 유용)
VOLUME /tmp

# Chat Service의 기본 포트 노출 (실제 사용하는 포트로 변경 필요)
EXPOSE 8083

# 빌드 단계에서 생성된 app.jar 복사
COPY --from=builder /app/app.jar /app/app.jar

# 애플리케이션 실행 명령어
ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]

# Spring 프로파일 설정 (선택 사항, 필요에 따라 변경)
ENV SPRING_PROFILES_ACTIVE=production
