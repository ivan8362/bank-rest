#FROM ubuntu:latest
#LABEL authors="ivan"
#ENTRYPOINT ["top", "-b"]

FROM amazoncorretto:17

WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
