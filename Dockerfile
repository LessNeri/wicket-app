FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

ADD https://repo1.maven.org/maven2/com/github/jsimone/webapp-runner/9.0.27.0/webapp-runner-9.0.27.0.jar webapp-runner.jar

COPY --from=build /app/target/wicket-app.war app.war

EXPOSE 8080

CMD ["java", "-jar", "webapp-runner.jar", "--port", "8080", "app.war"]
