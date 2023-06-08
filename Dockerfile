FROM openjdk:17-oracle

ENV JAR_FILE=tasker-1.0.jar
ENV CONFIG_FILE=config_prod.yml

WORKDIR /tasker

COPY ./target/$JAR_FILE /tasker/$JAR_FILE
COPY ./$CONFIG_FILE /tasker/$CONFIG_FILE

EXPOSE 8080

ENTRYPOINT "java" "-jar" $JAR_FILE "server" $CONFIG_FILE
