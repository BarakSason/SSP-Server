#! /bin/bash

echo "FROM openjdk:11-jdk
EXPOSE 8080
COPY target/SSP_Server.jar SSP_SERVER.jar" > Dockerfile

args=("$@")  
ELEMENTS=${#args[@]} 
 
for (( i=0;i<$ELEMENTS/2;i++)); do 
	echo "RUN mkdir -p ${args[${i}]}" >> Dockerfile
	container_paths+="${args[${i}]};"
done

echo "ENTRYPOINT [\"java\", \"-jar\", \"SSP_SERVER.jar\",  \"$container_paths\"]" >> Dockerfile

mounts=""
for (( i=$ELEMENTS/2;i<$ELEMENTS;i++)); do 
	mounts+="-v ${args[${i-$ELEMENTS/2}]}:${args[${i}]} "
done

echo docker build -t ssp-server . > build.sh
chmod 700 build.sh
echo "docker run --name ssp-server -p 8080:8080 $mounts -d ssp-server:latest" > run.sh
chmod 700 run.sh