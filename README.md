# Spot
Data Processing Network. Work in progress, purpose classified.

## Local Network Setup.

There are a couple of key files that must be modified when setting up this system on a local network. There are also a few assumptions made about how it is run.
1. The Go server is run on localhost.
2. The Editor (java app) is run on the same machine as the Go server.
3. The RabbitMQ is run on any machine on the same local network as the Editor and Client java app.
4. The Client (java app) is run on another machine on the same local network.

In order to setup the software system assuming your machine has LAN IP 192.168.2.2, you must perform the following steps:

1. Ensure RabbitMQ (Reachable at localhost:15672 has a user with full permissions). Username = Password = "test".
2. [macOS] Run "makeMQConfig.sh" with your IP (192.168.2.2 here) and the RabbitMQ port (5672). I.E: `sh makeMQConfig.sh 192.168.2.2 5672`. This should setup the config file in /usr/local/etc/rabbitmq/ to run RabbitMQ on your LAN ip and not 127.0.0.1 or whatever localhost is. 
3. [macOS] Run "makeMQEnvConfig.sh" with your IP (192.168.2.2) as an argument.
4. Edit `EXCHANGE_HOST` in `java/client/GraphModel.java` such that it contains your local IP.
5. Edit `EXCHANGE_NET_ADDR` in `server/server.go` such that it contains your local IP.

## Running the Network. 
1. Compile the Editor in `java/editor/` with `javac *.java` and run it with `java GraphEdit`. 
2. Compile the server in `server/` with `go build` and execute with `./server`.
3. Launch RabbitMQ with `rabbitmq-server`. 
4. Compile the client in `java/client/` with `sh compile.sh` and run with `sh run.sh`. 

### Dependencies
The RabbitMQ packages used in this project are bundled with it. Normally you'd install these with `go get github.com/streadway/amqp`. You'll still need to install the server software yourself though. If you're running macOS, this can be easily accomplished with Brew. I assume most Linux systems can likewise install this with `apt-get`. 

