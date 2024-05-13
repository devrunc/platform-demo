FROM openjdk:8-jdk-alpine
MAINTAINER hao cheng
WORKDIR /root
ADD target/platform-demo-1.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]