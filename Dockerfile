FROM openjdk:11-jdk
EXPOSE 8080
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
RUN mkdir -p home/barak/uploads-1
RUN mkdir -p home/barak/uploads-2
ENTRYPOINT ["java", "-jar", "app.jar"]
