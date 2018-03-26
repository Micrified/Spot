package main 

import (
    "fmt"
    "net"
    "log"
    "encoding/json"
    "queue"
    "data"
)

/* 
 ******************************************************************************
 *                                  Constants
 ******************************************************************************
*/

const (
    DEFAULT_NET_HOST    = "localhost"
    DEFAULT_NET_PORT    = "8024"
    DEFAULT_NET_TYPE    = "tcp"
    DEFAULT_BUF_SIZE    = 1024
)

/* 
 ******************************************************************************
 *                                   Globals
 ******************************************************************************
*/

// Data Gram Lifetime. 
var Threshold_Time int

// Minimum amount of sensors required to trigger an event.
var Threshold_Sensor int

// Radius of association.
var Threshold_Radius float64

/* 
 ******************************************************************************
 *                             Utility Routines
 ******************************************************************************
*/



/* 
 ******************************************************************************
 *                                Goroutines
 ******************************************************************************
*/

// Handler for incoming connections to the server. 
func requestHandler (conn net.Conn, in chan data.Gram) {
    fmt.Println("RequestHandler :: Launched for new connection!")
    // Initialize data buffer.
    buf := make([]byte, DEFAULT_BUF_SIZE)

    // Copy bytes to buffer.
    count, err := conn.Read(buf)
    if err != nil {
        log.Fatal("Connection Error: ", err.Error())
    }

    // Close connection.
    conn.Close()

    // Deserialize JSON to data.Gram.
    var g data.Gram
    if err = json.Unmarshal(buf[:count], &g); err != nil {
        log.Fatal("Deserialization Error: ", err.Error())
    }

    // If datagram is important, send to cluster queue.
    if data.IsInteresting(g) {
        fmt.Println("• Gram is interesting.")
        in <- g
    } else {
        fmt.Println("• Gram discarded.")
    }
}

// Handler for incoming data grams.
func queueHandler (in chan data.Gram, out chan data.Cluster) {
    var q queue.Queue
    fmt.Println("QueueHandler :: Active and listening!")
    for {

        // Accept data gram.
        g := <- in

        fmt.Println("QueueHandler :: Accepted new Gram!")


        
        // Remove expired events.
        fmt.Println("• Removing Expired Clusters")
        var survivors []interface{}
        for _, c := range q {
            k := c.(data.Cluster)
            if !k.Expired(Threshold_Time) {
                k.Update(Threshold_Time)
                survivors = append(survivors, k)
            }
        }
        q = survivors
        fmt.Printf("• %d clusters survived!\n", len(q))

        // Try to find suitable cluster for gram.
        fmt.Println("• Searching for suitable cluster...")
        matched := false
        for i, c := range q {
            k := c.(data.Cluster)
            if k.Suits(g, Threshold_Radius) {
                k.Insert(g)
                matched = true
                q[i] = k
                break
            }
        }
        if !matched {
            fmt.Println("• No matches -> Installing new cluster!")
            var c data.Cluster
            c.Insert(g)
            fmt.Println("• Cluster after insertion: ", c)
            q.Enqueue(c)
        }

        // Create events for important clusters.
        for _, c := range q {
            k := c.(data.Cluster)
            if len(k.Members) >= Threshold_Sensor {
                out <- k
            }
        }
    }
}

// Handler for incoming events (important/flagged clusters)
func eventHandler (in chan data.Cluster) {
    fmt.Println("EventHandler :: Active and listening!")
    for {

        // Accept important cluster.
        c := <- in

        fmt.Println("EventHandler: Important Event = ", c)

        // (Later) Try to synchronize with other servers.

        // Take something from queue. 

        // (Later) Resychronize again. 
    }
}

/* 
 ******************************************************************************
 *                                  Main
 ******************************************************************************
*/

func main () {

    // Set server settings [hardcoded].
    Threshold_Time      = 60        // Seconds
    Threshold_Sensor    = 3         // Count
    Threshold_Radius    = 1.0       // Kilometers.

    // Display Initialization Message:
    fmt.Printf("Initialized.\n- Time Threshold: %d\n- Server Threshold: %d\n- Trigger Radius: %f\n", Threshold_Time, Threshold_Sensor, Threshold_Radius)

    // Listen for incoming connections.
    socket, err := net.Listen(DEFAULT_NET_TYPE, DEFAULT_NET_HOST + ":" + DEFAULT_NET_PORT)
    if err != nil {
        log.Fatal("Error starting socket: ", err.Error())
    }
    // Initialize Inter-Routine Channels.
    gramChannel := make(chan data.Gram)
    clusterChannel := make(chan data.Cluster)

    // Launch queue handler.
    go queueHandler(gramChannel, clusterChannel)

    // Launch event handler.
    go eventHandler(clusterChannel)

    // Close listener when application closes.
    defer socket.Close()

    // Wait for connections. 
    for {

        // Accept connection.
        conn, err := socket.Accept()
        if err != nil {
            log.Fatal("Socket Error: ", err.Error())
        }

        // Launch request handler.
        go requestHandler(conn, gramChannel)
    }
}