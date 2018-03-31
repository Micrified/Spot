import java.util.ArrayList;
import java.awt.Point;
import java.util.*;
import java.io.*;

public class Sensor {

    private String  identifier;     // Identifier for the sensor.
    private long    time;           // Time at which sensor was triggered (Milliseconds since Epoch).
    private Point   location;       // Location (x,y) of the sensor (virtual).

    /*
     *************************************************************************
     *                             Constructors                              *
     *************************************************************************
    */
    
    public Sensor () {
        identifier = "<none>";
        time = 0;
        location = new Point(0,0);
    }

    /*
     *************************************************************************
     *                              Accessors                                * 
     *************************************************************************
    */

    // Gets the sensor identifier.
    public String getIdentifier() {
        return identifier;
    }

    // Gets the sensor time.
    public long getTime() {
        return time;
    }

    // Gets the sensor location.
    public Point getLocation() {
        return location;
    }

    // Sets the sensor identifer.
    public void setIdentifier (String identifier) {
        this.identifier = identifier;
    }

    // Sets the sensor time.
    public void setTime (long time) {
        this.time = time;
    }

    // Sets the sensor location.
    public void setLocation (Point location) {
        this.location = location;
    }
}