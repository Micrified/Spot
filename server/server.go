package main 

import (
    "fmt"
    "net"
    "log"
    "encoding/json"
    "queue"
    "data"
    "web"
    "github.com/streadway/amqp"
    "time"
)

/* 
 ******************************************************************************
 *                                  Constants
 ******************************************************************************
*/

// Server Information.
const (
    DEFAULT_NET_HOST    = "localhost"       // Server IP Address.
    DEFAULT_NET_PORT    = "8024"            // Server Port.
    DEFAULT_NET_TYPE    = "tcp"             // Server Communication Protocol.
    DEFAULT_BUF_SIZE    = 1024              // Server Data Buffer Size.
)

// Exchange Information
const (
    EXCHANGE_NET_NAME   = "test"            // Message-Exchange Name.
    EXCHANGE_NET_PSWD   = "test"            // Message-Exchange Password.
    EXCHANGE_NET_ADDR   = "192.168.2.2"     // Message-Exchange IP-Address.
    EXCHANGE_NET_PORT   = "5672"            // Message-Exchange Port.
)

// Threshold Information
const (
    THRESHOLD_EXPIRE    = 3000              // Cluster Lifetime (milliseconds).
    THRESHOLD_SENSOR    = 3                 // Minimum Sensors for Event.
    THRESHOLD_RADIUS    = 200.0             // Minimum Association Radius (meters).
)

// Server Initialization Banner.
const init_msg = "======================== SERVER ========================\n" +
                 "Address:\t\t%s\n" +
                 "Port:\t\t\t%s\n" + 
                 "======================= EXCHANGE =======================\n" +
                 "Login Name:\t\t%s\n" +
                 "Password:\t\t%s\n" +
                 "Address:\t\t%s\n" +
                 "Port:\t\t\t%s\n" +
                 "========================================================\n" +
                 "Time Threshold:\t\t\t%d milliseconds\n" +
                 "Sensor Threshold:\t\t%d sensors\n" +
                 "Clustering Radius:\t\t%.3f meters\n\n"

/* 
 ******************************************************************************
 *                             Utility Routines
 ******************************************************************************
*/

// Constructs a dial/login message for the RabbitMQ message exchange.
func getMQAddress(name, password, address, port string) string {
    return "amqp://" + name + ":" + password + "@" + address + ":" + port
}

// Checks if err is nil. If non-nil -> Program terminates.
func failOnError (err error, msg string) {
    if err != nil {
        log.Fatal("[!] Fatal Error: ", msg, "(", err.Error(), ")\n")
    }
}

// Checks if error is nil. If non-nil, message shown. Returns true if non-nil.
func failWithMsg (err error, h string, pass string, fail string) bool {
    var suffix string
    if err != nil {
        suffix = fail
    } else {
        suffix = pass
    }
    fmt.Printf("%s: %s", h, suffix); fmt.Println("(", err.Error(), ")")
    return err != nil
}

// Purges expired clusters from the given slice.
func pruneClusters (cs []interface{}) []interface{} {
    var survivors []interface{}

    for _, c := range cs {
        k := c.(data.Cluster)
        if k.Expired(THRESHOLD_EXPIRE) {
            continue
        }
        k.Update(THRESHOLD_EXPIRE)
        survivors = append(survivors, k)
    }

    return survivors
}

// Returns true and cluster index if fitting cluster found for given gram.
func fitsCluster (g data.Gram, cs []interface{}) (bool, int) {
    for i, c := range cs {
        k := c.(data.Cluster)
        if k.Suits(g, THRESHOLD_RADIUS) {
            return true, i
        }
    }
    return false, 0
}

/* 
 ******************************************************************************
 *                                Goroutines
 ******************************************************************************
*/

// Handler for incoming connections to the server. 
func requestHandler (conn net.Conn, in chan data.Gram) {
    fmt.Println("• RequestHandler :: -> Incoming!")

    // Initialize data buffer.
    buf := make([]byte, DEFAULT_BUF_SIZE)

    // Copy bytes to buffer.
    count, err := conn.Read(buf)
    failOnError(err, "Bad Connection!")

    // Close connection.
    conn.Close()

    // Deserialize JSON to data.Gram.
    fmt.Printf("• RequestHandler :: ~ %d bytes received.\n", count)

    var g data.Gram
    if err = json.Unmarshal(buf[:count], &g); err != nil {
        fmt.Printf("• RequestHandler :: Bad Message. Ignoring!")
        return
    }

    // If datagram is important, send to cluster queue.
    if data.IsInteresting(g) {
        fmt.Println("• RequestHandler :: Sent Gram to -> [Queue]!")
        in <- g
    } else {
        fmt.Println("• RequestHandler :: Sent Gram to -> [Trash]!")
    }
}

// Handler for incoming data grams.
func queueHandler (in chan data.Gram, out chan data.Cluster) {
    var q queue.Queue
    fmt.Println("• QueueHandler :: Standing By!")
    for {
        // Accept data gram.
        g := <- in

        // Prune expired clusters.
        q = pruneClusters(q)
        fmt.Printf("• QueueHandler :: Pruned Clusters [%d survived]\n", len(q))

        // Try to find suitable cluster for gram.
        match, i := fitsCluster(g, q)
        if match {
            k := q[i].(data.Cluster)
            k.Insert(g)
            q[i] = k
            fmt.Printf("• QueueHandler :: Gram %d -> Cluster %d\n", g.Id, k.Id)
        } else {
            var c data.Cluster
            c.Id = time.Now().UnixNano()    // Cluster Id is nanosecond time stamp.
            c.Insert(g)
            q.Enqueue(c)
            fmt.Printf("• QueueHandler :: Gram %d -> New Cluster %d\n", g.Id, c.Id)
        }

        // Send important clusters.
        for _, c := range q {
            k := c.(data.Cluster)
            if len(k.Members) >= THRESHOLD_SENSOR {
                out <- k
            }
        }
    }
}

// Handler for incoming events (important/flagged clusters)
func eventHandler (in chan data.Cluster) {

    // Open connection to message exchange.
    var conn *amqp.Connection
    var err error

    for {
        conn, err = amqp.Dial(getMQAddress(EXCHANGE_NET_NAME, EXCHANGE_NET_PSWD, 
            EXCHANGE_NET_ADDR, EXCHANGE_NET_PORT))
        if err != nil {
            fmt.Println("• EventHandler :: Failed to connect! (", err.Error(), ")")
            fmt.Println("• EventHandler :: Trying again in five seconds ...")
            time.Sleep(5)
        } else {
            fmt.Println("• EventHandler :: (huffs heavily) Standing By!")
            break
        }
    }

    // Open input channel.
    ch, err := conn.Channel()
    failOnError(err, "Failed to start channel with connection object!")

    // Deferred clean-up.
    defer func() {
        ch.Close()
        conn.Close()
    }()

    // Declare a fanout exchange. Anonymous channels.
    err = ch.ExchangeDeclare(
        "events",               // Exchange Name.
        "fanout",               // Exchange Type.
        false,                   // Durable (resistance to crashing)
        false,                  // Auto-deleted.
        false,                  // Internal.
        false,                  // No-wait.
        nil,                    // Arguments.
    )
    failOnError(err, "Exchange declaration failed!")

    for {

        // Accept important cluster.
        c := <- in

        // Append to web-server queue.
        web.AddEvent(c)

        // Export to exchange.
        bytes, err := c.Bytes()
        failOnError(err, "Failed to serialize cluster!")

        err = ch.Publish(
            "events",           // Exchange Name.
            "",                 // Routing Key.
            false,              // Mandatory.
            false,              // Immediate.
            amqp.Publishing{
                ContentType: "text/plain",
                Body: bytes,
            })
        failOnError(err, "Failed to publish message!")
        fmt.Println("• EventHandler :: Message → Exchange")
    }
}

/* 
 ******************************************************************************
 *                                  Main
 ******************************************************************************
*/

func main () {

    // Display Initialization Message:
    fmt.Printf(init_msg, DEFAULT_NET_HOST, DEFAULT_NET_PORT, 
        EXCHANGE_NET_NAME, EXCHANGE_NET_PSWD, 
        EXCHANGE_NET_ADDR, EXCHANGE_NET_PORT, THRESHOLD_EXPIRE, 
        THRESHOLD_SENSOR, THRESHOLD_RADIUS);

    // Listen for incoming connections.
    socket, err := net.Listen(DEFAULT_NET_TYPE, DEFAULT_NET_HOST + ":" + DEFAULT_NET_PORT)
    failOnError(err, "Bad socket!")

    // Initialize Inter-Routine Channels.
    gramChannel := make(chan data.Gram)
    clusterChannel := make(chan data.Cluster)


    fmt.Println("Main: All wings report in ...")

    // Launch queue handler.
    go queueHandler(gramChannel, clusterChannel)

    // Launch event handler.
    go eventHandler(clusterChannel)

    // Launch web handler.
    go web.WebHandler()

    // Close listener when application closes.
    defer func() {
        socket.Close()
    }()

    // Wait for connections. 
    for {

        // Accept connection.
        conn, err := socket.Accept()
        failOnError(err, "Bad Connection!")

        // Launch request handler.
        go requestHandler(conn, gramChannel)
    }
}