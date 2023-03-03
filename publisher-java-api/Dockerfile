FROM openjdk:17 as build
WORKDIR /app
COPY . ./
#RUN chmod +x ./mvnw
#RUN ./mvnw clean package -DskipTests -Dtests.skip=true -Dskip.unit.tests=true

FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

ENV LANG en_CA.UTF-8
ENV LANGUAGE en_CA.UTF-8
ENV LC_ALL en_CA.UTF-8
ENV JAVA_OPS -Xms512m -Xmx512m

COPY --from=build /app/target/publisher*.jar /app/service.jar


EXPOSE 3001

HEALTHCHECK --interval=35s --timeout=4s CMD wget --spider -S http://127.0.0.1:3001/health || exit 1

# Non-privileged user
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/service.jar"]
