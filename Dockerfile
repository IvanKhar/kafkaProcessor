#FROM openjdk:11.0.2-jre-slim
FROM openjdk:11-jdk-alpine
MAINTAINER Ivan Kharkevich <ivanhar1994@gmail.com>

ENV JAVA_OPTS="-Xms50m -Xmx128m"

ADD target/kafkaProcessor.jar /kafkaProcessor.jar

ENTRYPOINT java ${JAVA_OPTS} -jar /kafkaProcessor.jar