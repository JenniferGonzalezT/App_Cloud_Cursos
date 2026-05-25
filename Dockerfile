# Etapa 1: compilar el proyecto con Maven
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# descarga dependencias primero para aprovechar cache de Docker
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: imagen final solo con el JAR
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Carpeta donde se montara el Wallet de Oracle Cloud
VOLUME /app/wallet

COPY --from=build /app/target/formativa-cloud-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
