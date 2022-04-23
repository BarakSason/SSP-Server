#! /bin/bash

echo "FROM openjdk:11-jdk
EXPOSE 8080
COPY target/SSP_Server.jar SSP_SERVER.jar" > Dockerfile

paths=""
for var in "$@"
do
    echo "RUN mkdir -p $var" >> Dockerfile
	paths+="$var;"
done

echo "ENTRYPOINT [\"java\", \"-jar\", \"SSP_SERVER.jar\",  \"$paths\"]" >> Dockerfile
