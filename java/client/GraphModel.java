import java.util.ArrayList;
import java.util.Collections;
import java.awt.Rectangle;
import java.util.*;
import java.io.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.event.UndoableEditEvent;
import java.lang.Math;
import com.rabbitmq.client.*;
import java.io.IOException;

public class GraphModel {

    /* ******** Properties ******** */
    private ClusterParser parser = new ClusterParser();
    private ArrayList<GraphCluster> clusters = new ArrayList<GraphCluster>();

    /* ******** RabbitMQ Properties ********* */
    private static final String EXCHANGE_NAME = "events";
    private static final String EXCHANGE_HOST = "192.168.2.2";
    private static final String EXCHANGE_USER = "test";
    private static final String EXCHANGE_PSWD = "test";

    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private String queueName;

    private Consumer consumer;

    /* ******** Constructors ********* */
    
    /* Default constructor */
    public GraphModel() {
        try {
            connectToExchange();
        } catch (Exception e) {
            System.out.println("GraphModel: Failed to connect!\n");
        }
    }

    /* Attempts to connect to message exchange */
    public void connectToExchange() throws Exception {
        factory = new ConnectionFactory();
        factory.setHost(EXCHANGE_HOST);
        factory.setUsername(EXCHANGE_USER);
        factory.setPassword(EXCHANGE_PSWD);
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println(" [*] Connected to exchange!");

        consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println("Received: " + msg);

                // Create Cluster.
                GraphCluster cluster = parser.toGraphCluster(msg);
                if (cluster != null) {
                    GraphModel.this.addCluster(cluster);
                } else {
                    System.out.println("GraphModel :: Failed to add incoming cluster!");
                }
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }

    /* ******** Accessors ********* */

    /* Setters */
    public void setClusters(ArrayList<GraphCluster> clusters) {
        this.clusters = clusters;
    }
    
    /* Getters */
    public ArrayList<GraphCluster> getClusters(){
        return this.clusters;
    }

    /* ******** Interface ********* */

    /* Add a Cluster instance to the graph */
    public void addCluster(GraphCluster cluster) {
        if (!this.clusters.contains(cluster)) {
            this.clusters.add(cluster);
        }
    }

    /* Removes all expired clusters */
    public void removeExpiredClusters() {
        ArrayList<GraphCluster> survivors = new ArrayList<GraphCluster>();
        for (GraphCluster c : this.clusters) {
            if (c.isExpired() == false) {
                survivors.add(c);
            }
        }
        this.clusters = survivors;
    }
}