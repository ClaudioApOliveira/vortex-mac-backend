# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /build

RUN apk add --no-cache bash

COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:25-jre-alpine AS runner
WORKDIR /work

RUN apk add --no-cache curl \
    && addgroup -S quarkus \
    && adduser -S quarkus -G quarkus

COPY --from=build --chown=quarkus:quarkus /build/target/quarkus-app/lib/ /work/lib/
COPY --from=build --chown=quarkus:quarkus /build/target/quarkus-app/*.jar /work/
COPY --from=build --chown=quarkus:quarkus /build/target/quarkus-app/app/ /work/app/
COPY --from=build --chown=quarkus:quarkus /build/target/quarkus-app/quarkus/ /work/quarkus/

USER quarkus

ENV QUARKUS_PROFILE=prod
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -fsS http://127.0.0.1:8080/api/localidades/estados/SP > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/work/quarkus-run.jar"]
