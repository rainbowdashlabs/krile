FROM gradle:jdk19-alpine as build

COPY . .
ENV docker=true
RUN gradle build

FROM eclipse-temurin:21-alpine as runtime

WORKDIR /app

COPY --from=build /home/gradle/build/libs/krile-*-all.jar bot.jar

ENTRYPOINT ["java", "-Dbot.config=config/config.json", "-Dlog4j.configurationFile=config/log4j2.xml", "-jar" , "bot.jar"]
