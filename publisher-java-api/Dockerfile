FROM ghcr.io/graalvm/graalvm-ce:22.3.0 AS build
WORKDIR /workspace/app


COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x mvnw

RUN ./mvnw -Pnative-image package

FROM alpine:3.14
WORKDIR /app
COPY --from=build /workspace/app/target/ca.bc.gov.nrs.publisherjavaapi.publisherapiapplication /app/application
EXPOSE 3000

HEALTHCHECK --interval=300s --timeout=4s CMD wget --spider -S http://127.0.0.1:3000/actuator/health || exit 1
USER app
ENTRYPOINT ["/app/application"]
