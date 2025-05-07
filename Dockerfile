# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ---- Run Stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/santhrupthi-backend-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/keystore.p12 keystore.p12
EXPOSE 443
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=443"] 