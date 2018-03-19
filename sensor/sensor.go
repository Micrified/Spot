package main

import (
	"fmt"
	"net"
	"time"
	"log"
	"encoding/json"
	"strings"
	"math/rand"
	"data"
)

// Server connection information.
const (
	CONN_HOST = "localhost"
	CONN_PORT = "8024"
	CONN_TYPE = "tcp"
)

// Sends a datagram to the server.
func sendDataGram (gram data.Gram) {

	// Create a new datagram, and serialise it.
	json, err := json.Marshal(gram)

	// Catch serialization error.
	if err != nil {
		log.Fatal("Couldn't serialize DataGram!")
	}

	// Construct the server address, and open a connection.
	addr := strings.Join([]string{CONN_HOST, CONN_PORT}, ":")
	fmt.Println("Will connect on: ", addr, " and send ", json)
	conn, err := net.Dial(CONN_TYPE, addr)

	// Handle exceptions or function completion with conn.Close()
	defer conn.Close()

	// Check connection for error.
	if err != nil {
		log.Fatal("Couldn't connect to server!")
	}

	// Write the datagram to the server.
	conn.Write([]byte(json))
}

func main () {
	sendDataGram(data.Generate())
	fmt.Print(rand.Intn(100))
}