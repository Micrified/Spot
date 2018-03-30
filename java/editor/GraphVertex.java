import java.util.*;
import java.io.*;
import java.awt.Point;
import java.awt.Rectangle;

public class GraphVertex implements Persistent {

    /* ******** Properties ******** */
    private String name;
    private Point origin;
    private int radius;
    private Boolean triggered = false;

    /* ******** Constructors ********* */
    
    /* Default constructor */
    public GraphVertex() {
        name = "Blank";
        origin = new Point(0,0);
        radius = 10;
    }

    /* Initializes a GraphVertex instance with name and rectangle */
    public GraphVertex(String name, Point origin, int radius){
        this.name = name;
        this.origin = origin;
        this.radius = radius;
    }

    /* ******** Methods ********* */

    public Boolean contains (Point p) {
        Point m = getMidpoint();
        int dx = m.x - p.x;
        int dy = m.y - p.y;
        return ((radius * radius) > (dx * dx) + (dy * dy));
    }

    public Boolean isTriggeredByEffect (GraphEffect effect) {
        return effect.contains(this.getMidpoint(), this.radius);
    }

    /* ******** Accessors ********* */
    
    /* Setters */
    void setName (String name) {
        this.name = name;
    }

    void setOrigin (Point origin) {
        this.origin = origin;
    }

    void setRadius (int radius) {
        this.radius = radius;
    }

    void setIsTriggered (Boolean triggered) {
        this.triggered = triggered;
    }

    /* Getters */
    String getName(){
        return this.name;
    }

    Point getOrigin() {
        return this.origin;
    }

    int getRadius() {
        return this.radius;
    }

    Boolean isTriggered() {
        return this.triggered;
    }
    
    /* ******** Interface ********* */
    
    /* Positions the Circle relative to its midpoint at point p */
    public void centerAroundPoint (Point p) {
        double x = p.getX() - radius;
        double y = p.getY() - radius;
        this.origin = new Point((int)x,(int)y);
    }
    
    /* Returns the origin point of the Circle */
    public Point getPosition() {
        return this.origin;
    }
    
    /* Returns the coordinates of the circle's midpoint */
    public Point getMidpoint() {
        double x = origin.getX() + radius;
        double y = origin.getY() + radius;
        return new Point((int)x, (int)y);
    }

    /* Returns given string in quotes */
    private String Q(String s) {
        return "\"" + s + "\"";
    }

    /* Returns the JSON representation of the Vertex */
    public String toJSON(long time, float amplitude) {
        float[] signal = new float[]{0.0f, 0.0f, 50.0f, 27.0f, 8.0f, 1.0f, 0.0f};
        String signalString = "";

        // Apply amplitude to signal template.
        for (int i = 0; i < signal.length; i++) {
            signalString += String.format("%.1f", signal[i] * amplitude);
            if (i < signal.length - 1) {
                signalString += ",";
            }
        }

        // Prepare JSON datagram.
        String json = Q("Id") + ":" + name + "," +
                      Q("When") + ":" + time + "," + 
                      Q("Location") + ":[" + origin.getX() + "," + origin.getY() + "]," +
                      Q("Signal") + ":[" + signalString + "]";
        
        // Return JSON data.
        return "{" + json + "}";
    }
    
    /* ******** Persistence (Interface Methods) ********* */
    
    /* Save the object to the outputStream */
    public void save(PersistentOutputStream os) {
        os.writeDouble("x=", origin.x);
        os.writeDouble(",y=", origin.y);
        os.writeDouble(",r=", radius);
        os.writeString(",name=", name);
    }

    /* Load the object from the inputStream */
    public void load(PersistentInputStream is) throws IOException {
        double x = is.readDouble("x=");
        double y = is.readDouble(",y=");
        double r = is.readDouble(",r=");
        String name = is.readString(",name=");
        this.origin = new Point((int)x, (int)y);
        this.name = name;
    }

    /* Register all other objects in the Object-Table */
    public void registerConnectionsInTable(ObjectTable ot) {
        /* Ignored */
        return;
    }

}
