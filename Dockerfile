FROM eclipse-temurin:20-alpine as build

COPY . .
RUN ./gradlew clean build

FROM eclipse-temurin:20-alpine as runtime

WORKDIR /app

COPY --from=build /build/libs/krile-*-all.jar bot.jar

ENTRYPOINT ["java", "-Dbot.config=config/config.json", "-Dlog4j.configurationFile=config/log4j2.xml", "-jar" , "bot.jar"]
