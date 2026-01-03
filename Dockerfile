FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/discord-bot-0.0.1.jar app.jar
CMD ["java", "-jar", "app.jar"]