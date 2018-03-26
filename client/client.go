package main

import (
	"fmt"
	"log"
	"data"
	"encoding/json"
	"github.com/streadway/amqp"
)

func main () {

	// Open connection to exchange.
	conn, err := amqp.Dial("amqp://guest:guest@localhost:5672")
	if err != nil {
		log.Fatal("Error: Failed to connect to exchange: ", err.Error())
	}

	// Open output channel.
	ch, err := conn.Channel()
	if err != nil {
		log.Fatal("Error: Failed to open channel: ", err.Error())
	}

	// Defer clean up.
	defer func() {
		ch.Close()
		conn.Close()
	}()

    // Declare a fanout exchange. Anonymous channels.
    err = ch.ExchangeDeclare(
        "events",               // Exchange Name.
        "fanout",               // Exchange Type.
        true,                   // Durable (resistance to crashing)
        false,                  // Auto-deleted.
        false,                  // Internal.
        false,                  // No-wait.
        nil,                    // Arguments.
    )
    if err != nil {
        log.Fatal("Error: Failed to declare an exchange: ", err.Error())
	}
	
	// Configure subscriber queue.
	q, err := ch.QueueDeclare(
		"",						// Random Queue (necessary).
		false,					// Durable (persistence).
		false,					// Delete when unused.
		true,					// Exclusive (Delete queue on connection closure).
		false,					// No-wait.
		nil,					// Arguments.
	)
	if err != nil {
		log.Fatal("Error: Queue declaration failed: ", err.Error())
	}

	// Bind Queue.
	err = ch.QueueBind(
		q.Name,					// Name (determined by exchange).
		"",						// Routing Key.
		"events",				// Exchange Name.
		false,					
		nil)
	if err != nil {
		log.Fatal("Error: Failed to bind queue: ", err.Error())
	}

	// Receive messages from queue. 
	msgs, err := ch.Consume(
		q.Name,					// Queue Name.
		"",						// Consumer.
		true,					// Auto-ack(nowledgement).
		false,					// Exclusive.
		false,					// No-local.
		false,					// No-wait.
		nil,					// Args.
	)

	// Create channel for receiving/exchanging booleans (purpose unknown).
	forever := make(chan bool)

	// Initialize message buffer.

	// Dispatch goroutine to read out messages.
	go func() {
		for d := range msgs {

			var c data.Cluster

			if err = json.Unmarshal([]byte(d.Body), &c); err != nil {
				log.Fatal("Error: Cluster Deserialization: ", err.Error())
			}

			fmt.Println("[!] Event: ", c)
		}
	}()

	log.Println("Client :: Waiting for events ...")
	<- forever
}