# syntax=docker/dockerfile:1.6

########################
#   Build stage
########################
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1) сначала POM — слои зависимостей кэшируются
COPY pom.xml .

# 2) прогреть зависимости в локальный кэш BuildKit (сохраняется между сборками)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests dependency:go-offline

# 3) теперь исходники (ломают кэш только при изменении кода)
COPY src ./src

# 4) сборка, снова используем кэш ~/.m2
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests -T 1C package

########################
#   Runtime stage
########################
FROM eclipse-temurin:21-jre
WORKDIR /app
# Копируем единственный boot-jar (без -plain)
COPY --from=build /app/target/*-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
