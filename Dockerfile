FROM gradle:9.0-jdk21 AS build
WORKDIR /app
LABEL authors="alexk"
COPY settings.gradle build.gradle ./
COPY src src
RUN gradle --no-daemon bootJar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/binance-price-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]