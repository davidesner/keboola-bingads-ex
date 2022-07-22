FROM maven:3.5.2-jdk-8-slim   
MAINTAINER David Esner <esnerda@gmail.com>

ENV APP_VERSION 1.1.0


# set switch that enables correct JVM memory allocation in containers
COPY . /code/
ENV MAVEN_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Xmx512m"
ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Xmx512m"

WORKDIR /code/

# https://github.com/keboola/docker-bundle/issues/198
RUN chmod a+rw ./ -R
RUN mkdir -p /tmp/maven/.m2 && chmod a+rw /tmp/maven -R

# https://github.com/carlossg/docker-maven#running-as-non-root
ENV MAVEN_CONFIG=/tmp/maven/.m2

RUN mvn compile -Dmaven.repo.local=/tmp/maven/.m2

ENTRYPOINT mvn -q -e exec:java -Dexec.args=/data -Dmaven.repo.local=/tmp/maven/.m2 -Dlog4j.configuration=/code/src/main/resources/log4j2.properties -Dorg.apache.cxf.Logger=org.apache.cxf.common.logging.Log4jLogger
