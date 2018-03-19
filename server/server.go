package main

import (
	"fmt"
	"net"
	"log"
	"encoding/json"
	"server/queue"
	"server/heap"
	"data"
)

// Server connection information.
const (
	CONN_HOST = "localhost"
	CONN_PORT = "8024"
	CONN_TYPE = "tcp"
)

// Handles incoming requests.
func handleRequest (conn net.Conn, h *heap.Heap) {

	// Make a buffer to hold incoming data.
	buf := make([]byte, 1024)

	// Read incoming connection into the buffer.
	read_len, err := conn.Read(buf)
	if err != nil {
		log.Fatal("Error reading: ", err.Error())
	}

	// Deserialize the datagram and print it.
	var gram data.Gram
	err = json.Unmarshal(buf[:read_len], &gram)

	if err != nil {
		log.Fatal("Error deserializing: ", err.Error())
	}

	// Push datagram on heap.
	h.Push(gram)

	// Print the current heap.
	h.Print()

	// Close the connection when you're done with it.
	conn.Close()
}


// Globals
var area data.Area
var zone data.Zone
var queues [][]queue.Queue


func main () {

	// Initialize Area and Zone.
	area = data.Area{0.0, 0.0, 3.0, 3.0}	// (X,Y), Width x Height
	zone = data.Zone{1.0, 1.0}				// Width x Height

	// Compute Grid Dimensions.
	rows, columns, err := data.ZoneDimensions(area, zone)

	if err != nil {
		log.Fatal("Bad grid initialization: ", err.Error())
	}

	// Initialize Queues.
	queues = make([][]queue.Queue, rows)

	for i := 0; i < rows; i++ {
		queues[i] = make([]queue.Queue, columns)
	}

	// Display Initialization Message.
	fmt.Println("Ready -> Listening on a ", rows, "x", columns, " zoned grid.")
	
	// Initialize the heap.
	h := &heap.Heap{}
	h.Init()

	// Listen for incoming connections.
	l, err := net.Listen(CONN_TYPE, CONN_HOST + ":" + CONN_PORT)

	if err != nil {
		log.Fatal("Error listening: ", err.Error())
	}

	// 2. Close listener when application closes.
	defer l.Close();

	fmt.Println("Listening on " + CONN_HOST + ":" + CONN_PORT);

	for {
		// Listen for incoming connection.
		conn, err := l.Accept()
		if err != nil {
			log.Fatal("Error accepting: ", err.Error())
		}

		// Handle connections in a new goroutine.
		go handleRequest(conn, h)
	}
}