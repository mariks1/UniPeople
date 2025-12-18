FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY . .

RUN mvn -pl api-gateway -am package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/api-gateway/target/*.jar app.jar

EXPOSE 18080
ENTRYPOINT ["java","-jar","/app/app.jar"]
