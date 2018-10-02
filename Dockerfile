FROM maven:3.5.2-jdk-8-slim   
MAINTAINER David Esner <esnerda@gmail.com>

ENV APP_VERSION 1.1.0


# set switch that enables correct JVM memory allocation in containers
COPY . /code/
ENV MAVEN_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Xmx512m"
ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Xmx512m"

WORKDIR /code/
RUN mvn compile

ENTRYPOINT mvn -q -e exec:java -Dexec.args=/data  
