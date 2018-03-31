import java.util.*;
import java.io.*;
import java.awt.Point;
import java.awt.Rectangle;

public class GraphCluster {

    /************ Properties *************/

    private String identifier = "<None>";                           // ID of the cluster.
    private long lastUpdated = 0;                                   // Time at which the cluster was last updated (Epoch in millis).
    private int lifespan = 500;                                     // Maximum lifetime value.
    private int lifetime = 0;                                       // Current lifetime value..
    private ArrayList<Sensor> sensors = new ArrayList<Sensor>();    // Sensors triggered.
    private Point origin;                                           // Approximated origin. Null initialized.                               

    /************ Constructors *************/

    /* Initializes an empty GraphCluster */
    public GraphCluster() {
    }

    /************ Methods *************/

    // Returns True if the cluster has expired.
    public Boolean isExpired() {
        return (++lifetime >= lifespan);
    }

    // Computes the estimated origin of the signal.
    private Point computeOrigin() {
        return new Point(0,0);
    }

    /************ Accessors: Getters *************/

    // Return identifer.
    public String getIdentifer() {
        return identifier;
    }

    // Returns the lastUpdated value.
    public long getLastUpdated() {
        return this.lastUpdated;
    }

    // Returns the remaining lifetime of the sensor as a normalized fraction.
    public double getLifetime() {
        return (1.0 - ((float)lifetime / (float)lifespan));
    }

    // Returns the sensor location array.
    public ArrayList<Sensor> getSensors() {
        return this.sensors;
    }

    // Return approximated origin.
    public Point getOrigin() {
        if (origin == null) {
            origin = computeOrigin();
        }
        return origin;
    }

    /************ Accessors: Setters *************/

    // Sets Identifier.
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    // Sets the lastUpdated value.
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Sets the sensors location array.
    public void setSensors (ArrayList<Sensor> sensors) {
        this.sensors = sensors;
    }
}