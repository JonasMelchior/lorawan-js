FROM maven:latest AS build
WORKDIR /app
COPY . .
RUN mvn clean package -Dspring.profiles.active=dev -Pproduction

# Stage 2: Runtime base image - leverage caching
FROM eclipse-temurin:17-alpine AS runtime-base
WORKDIR /app

FROM runtime-base as join-server
RUN mkdir /bc-fips
RUN wget -P /bc-fips/  https://repo1.maven.org/maven2/org/bouncycastle/bc-fips/1.0.2.4/bc-fips-1.0.2.4.jar
COPY --from=build /app/join-server/target/join-server-1.0-SNAPSHOT.jar /app/join-server-1.0-SNAPSHOT.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=js", "-Dloader.path=/bc-fips/bc-fips-1.0.2.4.jar", "-jar", "/app/join-server-1.0-SNAPSHOT.jar"]

FROM runtime-base as join-server-ui
RUN mkdir /bc-fips
RUN wget -P /bc-fips/  https://repo1.maven.org/maven2/org/bouncycastle/bc-fips/1.0.2.4/bc-fips-1.0.2.4.jar
COPY --from=build /app/join-server-ui/target/join-server-ui-1.0-SNAPSHOT.jar /app/join-server-ui-1.0-SNAPSHOT.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=js", "-Dloader.path=/bc-fips/bc-fips-1.0.2.4.jar", "-jar", "/app/join-server-ui-1.0-SNAPSHOT.jar"]
