package main

import (
	"fmt"
	"net"
	"log"
	"encoding/json"
	"queue"
	"server/heap"
	"data"
	"time"
	"config"
)

// Server connection information.
const (
	CONN_HOST 			= "localhost"
	CONN_PORT 			= "8024"
	CONN_TYPE 			= "tcp"
)

var Time_Threshold int
var Sensor_Threshold int

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

	// Update the heap.
	processHeap(h)
}

// Processes the heap.
func processHeap (h *heap.Heap) {
	for !h.IsEmpty() {
		g := h.Pop().(data.Gram)
		if data.IsInteresting(g) {
			x, y := g.Origin()
			i, j, err := data.ZoneIndex(x, y, area, zone)
			if err != nil {
				log.Fatal("Can't place gram: ", err.Error())
			}
			q := &queues[i][j]
			q.Enqueue(g)
			processQueue(q, i, j)
		}
	}
}

// Returns true if a given gram is out of date.
func expired (g data.Gram) bool {
	cutoff := time.Now().Add(-time.Second * time.Duration(Time_Threshold))
	return cutoff.After(g.When)
}

// Processes a data queue. 
func processQueue (q *queue.Queue, i, j int) {

	// Flush old data.
	for !q.IsEmpty() {
		g, _ := q.Peek()
		if !expired(g.(data.Gram)) {
			break
		}
		q.Dequeue()
	}

	// Generate new event.
	if q.Len() >= Sensor_Threshold {
		fmt.Println("There is an important event in zone (", i, ",", j,")")
	} else {
		fmt.Println("Nothing important in zone (", i, ",", j, ")")
	}
}

// Globals
var area data.Area
var zone data.Zone
var queues [][]queue.Queue

func main () {

	// Attempt to read configuration file.
	f, err := config.OpenFileStream("config.json")
	if err != nil {
		log.Fatal("Couldn't find config: ", err.Error())
	}
	c, err := config.ReadConfig(f)
	if err != nil {
		log.Fatal("Bad config file: ", err.Error())
	}
	Time_Threshold = c.Time_Threshold
	Sensor_Threshold = c.Sensor_Threshold
	area = c.Area()
	zone = c.Zone()
	
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
	fmt.Println("Ready -> Listening on a (", rows, "x", columns, ") zoned grid.")
	
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