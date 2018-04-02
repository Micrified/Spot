# Spot

Spot is a signal processing network composed of several components.
1. A Java Editor Application. This is located in java/editor/. It allows for simulation of a sensor network.
2. A Go Server. This is located in server/. It performs signal filtering and aggregation. 
3. A RabbitMQ message exchange. This software is not provided and must be downloaded by all interested parties.
4. A Java Client Application. This is located in java/client/. It provides visualizations of a sensor network's activity.

## Installation

This installation manual is written with the following assumptions in mind.
1. The Go server is to be run on localhost.
2. The Java Editor is run on the same machine as the Go server.
3. The RabbitMQ editor is run on some machine residing on the local network to which both the server and editor have access.
4. The Java Client is run on some machine on the local network.

### RabbitMQ
The RabbitMQ message exchange is to be initialized and accessed at it's default address: `localhost:15672`.
Here, make the following modifications.
1. Add a new user with username and password "test".
2. Ensure this user has full permissions set.
3. Execute `sh makeMQConfig.sh <LAN-IP-Address> 5672` to install the RabbitMQ configuation file in `/usr/local/etc/rabbitmq/`. Note that this is specific to macOS as of this time.
4. Execute `sh makeMQEnvConfig.sh <LAN-IP-Address>` to install the RabbitMQ env file in the same location as the configuration file.

### Other Files
5. Edit `EXCHANGE_NET_ADDR` in `server/server.go` such that it contains your local IP address (the address now used by the RabbitMQ exchange).

## Running the Programs.
Perform all steps in the following order.
1. Launch RabbitMQ with `rabbitmq-server`.
2. Compile the server in `server/` with `go build` and execute with `./server`. 
3. Compile the Editor in `java/editor/` with `javac *.java` and run it with `java GraphEdit`. 
4. Compile the client in `java/client/` with `sh compile.sh` and run with `sh run.sh <LAN-IP-Address` where `LAN-IP-Address` is the address of the machine which is hosting the RabbitMQ message exchange. 

Visit the server's webpage at `localhost:8080`.

## Dependencies & Troubleshooting
This project requires that Go and RabbitMQ be installed.

### Go is not installed.
Install Go following the official installation instructions on the Golang website. Remember to
1. `export PATH=$PATH:/usr/local/go/bin` if that's where you installed Go. 
2. `export GOPATH=$HOME/path-to-project`. The project should be located in a `/src` subdirectory at the end of `GOPATH`. 

### The Client/Server fails to connect to Exchange
Sometimes an exchange is setup with a setting which is not shared amongst other users of the exchange. Typically this is seen with an exchange having `Durable` set by one machine and not set on another. To solve this, log into the RabbitMQ web client and delete the `events` exchange. Restart everything and it should work.

### RabbitMQ is not installed.
The RabbitMQ packages used in this project are bundled with it. Normally you'd install these with `go get github.com/streadway/amqp`. You'll still need to install the server software yourself though. If you're running macOS, this can be easily accomplished with Brew. I assume most Linux systems can likewise install this with `apt-get`. 

