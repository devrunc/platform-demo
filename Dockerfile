FROM openjdk:8-jdk-alpine
MAINTAINER hao cheng
WORKDIR /app
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
EXPOSE 17879
ADD target/platform-demo-1.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]