FROM openjdk:21

WORKDIR /src
ARG file
COPY ${file} ./accounts.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "accounts.jar"]
