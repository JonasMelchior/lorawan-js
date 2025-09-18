FROM maven:latest AS build
USER root
RUN apt-get update && apt-get install -y make protobuf-compiler git && rm -rf /var/lib/apt/lists/*
WORKDIR /chirpstack
RUN git clone --branch v4.14.1 --depth 1 https://github.com/chirpstack/chirpstack.git . && cd api/java && make install
WORKDIR /app
COPY . .
RUN mvn clean package -Dspring.profiles.active=dev -Dspring.datasource.url=jdbc:postgresql://172.17.0.1:5432/lorawan_js -Pproduction

# Stage 2: Runtime base image - leverage caching
FROM eclipse-temurin:17-alpine AS runtime-base
WORKDIR /app
FROM runtime-base as join-server
RUN mkdir /bc-fips
RUN wget -P /bc-fips/  https://repo1.maven.org/maven2/org/bouncycastle/bc-fips/1.0.2.4/bc-fips-1.0.2.4.jar
COPY --from=build /app/join-server/target/join-server-0.1.0.jar /app/join-server-0.1.0.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=js", "-Dloader.path=/bc-fips/bc-fips-1.0.2.4.jar", "-jar", "/app/join-server-0.1.0.jar"]

FROM runtime-base as join-server-ui
RUN mkdir /bc-fips
RUN wget -P /bc-fips/  https://repo1.maven.org/maven2/org/bouncycastle/bc-fips/1.0.2.4/bc-fips-1.0.2.4.jar
COPY --from=build /app/join-server-ui/target/join-server-ui-0.1.1.jar /app/join-server-ui-0.1.1.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=js", "-Dloader.path=/bc-fips/bc-fips-1.0.2.4.jar", "-jar", "/app/join-server-ui-0.1.1.jar"]
