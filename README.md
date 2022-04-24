# SSP-Server
Simple Storage Platform Server

# Overview
This project was created in order to practice development using Java Spring boot, MongoDB and containerization.
*** This project is a POC-level only code, not intended for any real-world use *** 

This server enables the aggregation of storage from multiple disks.
This server runs in a container with persistent storage mounted to it.
The server provides a simple set of REST APIs enabling the creating of files and dirs on the storage, as well as
the downloading files, and removing files and dirs.
The server connects to a containerized MongoDB in order to manage metadata of the stored objects.

# Usage
The root folder of the project contains a configuration script (configure.sh) which when executed creates a
Dockerfile, a build script (build.sh) and an execution script (run.sh).
Parameters MUST be provided to the configuration script in the following way:
The script MUST accept an even number of parameters, of which the 1st half are paths to be created within the
server container (files and dirs will be created at those locations) and the 2nd half of the arguments
are paths of mounted disks on the host machine.
Example execution:
./configure.sh </path/within/the/container> </path/to/a/mounted/disk/on/the/host/machine>
Once the configuration sciprt has been executed, no further configuration is needed 0 proceed to execute the 
build script and the run script in that order.
Once the server is running, use the client platform in order to communicate (SSP-Client) with the server.