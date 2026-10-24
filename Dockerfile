FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /build
COPY . .
RUN ./gradlew --no-daemon shadowJar

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /build/build/libs/krile-*-all.jar bot.jar
ENTRYPOINT ["java", "-Dbot.config=config/config.json", "-Dlog4j.configurationFile=config/log4j2.xml", "-jar", "bot.jar"]
