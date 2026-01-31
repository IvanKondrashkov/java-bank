FROM amazoncorretto:21-alpine-jdk
COPY notifications-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
EXPOSE 8085