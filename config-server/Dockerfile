FROM amazoncorretto:21-alpine-jdk
RUN apk add --no-cache curl
COPY config-server/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
EXPOSE 8888