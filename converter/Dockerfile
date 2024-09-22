FROM openjdk:21

WORKDIR /src
ARG file
COPY ${file} ./app-sum.jar

ENTRYPOINT ["java", "-jar", "app-sum.jar"]
