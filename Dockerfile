# Etapa 1: Construir con Maven + Eclipse Temurin 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar pom.xml primero (para cache eficiente)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar código fuente
COPY src ./src

# Construir aplicación
RUN mvn clean package -DskipTests

# Etapa 2: Runtime con Eclipse Temurin 17 JRE Alpine (más pequeño)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar archivos construidos
COPY --from=build /app/target/wicket-app.war .
COPY --from=build /app/target/dependency/webapp-runner.jar .

# Puerto
EXPOSE 8080

# Comando de ejecución
CMD ["java", "-jar", "webapp-runner.jar", "wicket-app.war", "--port", "8080"]