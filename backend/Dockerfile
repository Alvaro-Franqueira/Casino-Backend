FROM maven:3.9.4-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
# stage 2: run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# port exposed (adjust if needed)
EXPOSE 8080
# Command to run
ENTRYPOINT ["java", "-jar", "app.jar"]
