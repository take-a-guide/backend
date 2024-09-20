FROM eclipse-temurin:21-jdk-alpine

VOLUME /tmp

ENV JAR_FILE=/target/take-a-guide-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
