FROM amazoncorretto:21-alpine-jdk
COPY transfer-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
EXPOSE 8083