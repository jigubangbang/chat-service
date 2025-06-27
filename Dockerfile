# 빌드 단계 (builder)
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app
COPY chat-service/mvnw .
COPY chat-service/.mvn .mvn
COPY chat-service/pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY chat-service/src ./src
RUN ./mvnw package -DskipTests
ARG JAR_FILE_NAME=target/*.jar
RUN cp ${JAR_FILE_NAME} app.jar || { echo "JAR 파일이 target 디렉토리에서 발견되지 않았습니다. 빌드 로그를 확인하세요."; exit 1;}


# 실행 단계 (runtime)
FROM openjdk:17-slim
RUN useradd --system --uid 1000 spring
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh
USER spring
VOLUME /tmp
EXPOSE 8083
COPY --from=builder /app/app.jar /app/app.jar
ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]
ENV SPRING_PROFILES_ACTIVE=production