FROM maven:3.5-jdk-8-alpine   
MAINTAINER David Esner <esnerda@gmail.com>

ENV APP_VERSION 1.1.0
# install git
RUN apk add --no-cache git

# set switch that enables correct JVM memory allocation in containers
ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
ENV MAVEN_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

WORKDIR /home
RUN git clone --branch update/api-v-11 https://github.com/davidesner/keboola-bingads-ex.git ./  

RUN mvn compile

ENTRYPOINT mvn -q -e exec:java -Dexec.args=/data  