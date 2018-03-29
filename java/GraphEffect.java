import java.util.*;
import java.io.*;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.Math;

public class GraphEffect implements Persistent {
    
    /********** Properties **********/
    private Point origin;
    private int radius;             // Maximum wave effective range.
    private int velocity = 1;       // Rate of wave spread.
    private int state = 0;          // Current state of spread.

    /********** Constructors **********/
    public GraphEffect() {
        origin = new Point(0,0);
        radius = 10;
    }

    public GraphEffect(Point origin, int radius) {
        this.origin = origin;
        this.radius = radius;
    }

    /********** Setters **********/

    void setOrigin (Point origin) {
        this.origin = origin;
    }

    void setRadius (int radius) {
        this.radius = radius;
    }

    /********** Interface **********/

    /* Returns True if a given point is within it's influence. Radius is subtracted */
    public Boolean contains (Point p, int radius) {
        double dx = this.origin.getX() - p.getX();
        double dy = this.origin.getY() - p.getY();
        Boolean withinFront = (state * state) > ((dx * dx) + (dy * dy) - radius);
        Boolean beforeBack = (state - 1) * (state - 1) < ((dx * dx) + (dy * dy) - radius);
        return withinFront && beforeBack;
    }

    /* Returns True if the Effect has expired */
    public Boolean expired () {
        return (this.state > this.radius);
    }

    /* Positions the Circle relative to its midpoint at point p */
    public void centerAroundPoint (Point p) {
        double x = p.getX() - radius;
        double y = p.getY() - radius;
        this.origin = new Point((int)x,(int)y);
    }

    /* Returns the origin point of the Circle */
    public Point getOrigin() {
        return this.origin;
    }

    /* Returns the radius of the Circle: Automatically updates at this point. */
    public int getRadius() {
        this.state += velocity;
        return Math.min(radius, this.state);
    }

    /* Returns the coordinates of the circle's midpoint */
    public Point getMidpoint() {
        double x = origin.getX() + radius;
        double y = origin.getY() + radius;
        return new Point((int)x, (int)y);
    }

    /* ******** Persistence (Interface Methods) ********* */

    /* Save the object to the outputStream */
    public void save(PersistentOutputStream os) {
        os.writeDouble("x=", origin.x);
        os.writeDouble(",y=", origin.y);
        os.writeDouble(",r=", radius);
    }

    /* Load the object from the inputStream */
    public void load(PersistentInputStream is) throws IOException {
        double x = is.readDouble("x=");
        double y = is.readDouble(",y=");
        double r = is.readDouble(",r=");
        this.origin = new Point((int)x, (int)y);
    }

    /* Register all other objects in the Object-Table */
    public void registerConnectionsInTable(ObjectTable ot) {
        /* Ignored */
        return;
    }

}