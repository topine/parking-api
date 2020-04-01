FROM maven:3.6.3-jdk-11 AS MAVEN_BUILD
COPY . /tmp/
WORKDIR /tmp/
RUN mvn package
 
FROM openjdk:11-jre-slim
COPY --from=MAVEN_BUILD /tmp/target/parkingAp*.jar /app.jar
CMD ["java", "-jar", "/app.jar", "--spring.config.location=/tmp/application.properties"]
 
