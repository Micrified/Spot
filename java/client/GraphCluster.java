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

    // Returns a description of the cluster.
    public String getDescription() {
        String h = String.format("Cluster (ID = \"%s\")\n", this.identifier);
        String l = String.format("* Last Updated:\t\t%d\n", this.lastUpdated);
        String s = String.format("* Sensors Triggered:\t%d [", this.sensors.size());
        for (int i = 0; i < this.sensors.size(); i++) {
            Sensor sensor = this.sensors.get(i);
            s += String.format("%s%s", sensor.getIdentifier(), (i < (this.sensors.size() - 1) ? "," : "]"));
        }
        return h + l + s;
    }

    // Returns True if the cluster has expired.
    public Boolean isExpired() {
        return (++lifetime >= lifespan);
    }

    // Computes the estimated origin of the signal.s
    private Point computeOrigin() {
        long minTime = Long.MAX_VALUE;
        int x = 0, y = 0;
        float sum = 0;

        // Calculate min time
        for(Sensor s : sensors) {
            minTime = Math.min(minTime, s.getTime());
        }

        for(Sensor s : sensors) {
            float t = Math.max(0, 2 - (float)(s.getTime() - minTime) / 1000);
            x += s.getLocation().x * t;
            y += s.getLocation().y * t;
            sum += t;
        }

        x = (int)((float)x / sum);
        y = (int)((float)y / sum);

        System.out.printf("Origin = (%d,%d)\n", x, y);

        return new Point(x,y);
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