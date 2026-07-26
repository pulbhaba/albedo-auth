FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY auth-api/build/libs/auth-api-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
