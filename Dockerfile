# Etapa 1: Construir la aplicación con Maven
FROM maven:3.8.6-openjdk-11 AS build
# Copiar archivos de Maven primero (para cache eficiente)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar código fuente
COPY src ./src

# Construir la aplicación
RUN mvn clean package -DskipTests

# Etapa 2: Imagen de ejecución más pequeña
FROM openjdk:11-jdk-slim
WORKDIR /app

# Copiar archivos construidos desde la etapa 1
COPY --from=build /app/target/wicket-app.war .
COPY --from=build /app/target/dependency/webapp-runner.jar .

# Puerto que usará Render
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "webapp-runner.jar", "wicket-app.war", "--port", "8080"]