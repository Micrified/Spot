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

public class GraphModel implements Persistent {

    /* ******** Properties ******** */
    private ArrayList<GraphVertex> vertices = new ArrayList<GraphVertex>();
    private ArrayList<GraphEffect> effects = new ArrayList<GraphEffect>(); 
    private int port = 8024;
    private String address = "localhost";

    /* ******** Constructors ********* */
    
    /* Default constructor */
    public GraphModel() {}
    
    /* Initializes a GraphModel instance off a file */
    public GraphModel(String fileName) throws Exception {
        /* Load contents from file */
        ObjectTable objectTable = new ObjectTable();
        try {
            objectTable.restoreFromFile(fileName);
            GraphModel retreived = (GraphModel)objectTable.find(0);
            this.setVertices(retreived.getVertices());
        } catch (Exception e){
            throw new Exception("GraphModel cannot read this file!");
        }
    }

    /* ******** Accessors ********* */
    
    /* Setters */
    public void setVertices(ArrayList<GraphVertex> vertices){
        this.vertices = vertices;
    }

    public void setEffects(ArrayList<GraphEffect> effects) {
        this.effects = effects;
    }
    
    /* Getters */
    public ArrayList<GraphVertex> getVertices(){
        return this.vertices;
    }

    public ArrayList<GraphEffect> getEffects() {
        return this.effects;
    }
  

    /* ******** Interface ********* */
    
    /* Configure the GraphModel using a file */
    public int loadFromFile(String fileName){
        System.out.println("Loading from file");
        try {
            ObjectTable objectTable = new ObjectTable();
            objectTable.restoreFromFile(fileName);
            GraphModel retreived = (GraphModel)objectTable.find(0);
            this.setVertices(retreived.getVertices());
            System.out.println("Setting ID number");
            return 1;
        } catch (Exception e){
            System.out.printf("Could not load file!\n");
            return 0;
        }
    }
    
    /* Print existing Vertices & Edges */
    public void printReport(){
        System.out.printf("\nGraph-Vertices:\n");
        for (GraphVertex v : this.vertices){
            System.out.println("Name: " + v.getName() + " Origin: " + v.getOrigin().getX() + " " + v.getOrigin().getY() + " Radius: " + v.getRadius());
        }
    }
    
    /* Save the current graph to a specified file */
    public void saveAs(String fileName){
        ObjectTable objectTable = new ObjectTable();
        objectTable.registerObject(this);
        objectTable.saveToFile(fileName);
    }

    /* Add a Vertex instance to the graph */
    public void addVertex(GraphVertex vertex){
        if (!this.vertices.contains(vertex)){
            this.vertices.add(vertex);
        }
    }

    /* Add a Effect instance to the graph */
    public void addEffect(GraphEffect effect) {
        if (!this.effects.contains(effect)) {
            this.effects.add(effect);
        }
    }

    /* Removes a Vertex from the graph */
    public void removeVertex (GraphVertex vertex) {
        if (this.vertices.contains(vertex)) {
            this.vertices.remove(vertex);
        }
    }

    /* Removes an Effect from the graph */
    public void removeEffect (GraphEffect effect) {
        if (this.effects.contains(effect)) {
            this.effects.remove(effect);
        }
    }

    /* Removes all expired effects */
    public void removeExpiredEffects() {
        ArrayList<GraphEffect> survivors = new ArrayList<GraphEffect>();
        for (GraphEffect e : this.effects) {
            if (e.expired() == false) {
                survivors.add(e);
            }
        }
        this.effects = survivors;
    }

    /* Updates the triggered states of all vertices. Sends all signals to server. */
    public void updateVertexStates() {
        for (GraphVertex v : this.vertices) {
            Boolean triggered = false;
            long time;

            // If triggered by any effect, it is triggered.
            for (GraphEffect e : this.effects) {
                if (v.isTriggered() == false && v.isTriggeredByEffect(e)) {
                    triggered = true;
                    time = System.currentTimeMillis();

                    String json = v.toJSON(time);
                    ServerConnector connector = new ServerConnector(port, address);
                    if (connector.send(json) == false) {
                        System.out.println("Error: Couldn't send triggered sensor data!");
                    }
                    break;
                }
            }
  
            v.setIsTriggered(triggered);
        }
    }
    
    /* ******** Persistence (Interface Methods) ********* */
    
    /* Save the object to the outputStream */
    public void save(PersistentOutputStream os) {
        
        /* Write number of Vertices & Edges */
        os.writeInt("vertexCount=",this.vertices.size());
        os.writeInt(",effectCount=",this.effects.size());
        
        /* Write Vertices */
        os.writePersistentArrayList(",vertices=", this.vertices);
        
        /* Write Edges */
        os.writePersistentArrayList(",effects=", this.effects);
    }
    
    /* Load the object from the inputStream */
    public void load(PersistentInputStream is) throws IOException {
        System.out.println("Loading...");
        /* Read number of Vertices & Edges */
        int vertexCount = is.readInt("vertexCount=");
        
        int edgeCount = is.readInt(",effectCount=");
        
        /* Read Vertices */
        this.vertices = is.readPersistentArrayList(",vertices=");
        
        /* Read Edges */
        this.effects = is.readPersistentArrayList(",effects=");
    }
    
    /* Register all other objects in the Object-Table */
    public void registerConnectionsInTable(ObjectTable ot) {
        Enumeration otherVertices = Collections.enumeration(this.vertices);
        Enumeration otherEffects = Collections.enumeration(this.effects);
        
        /* Register vertices */
        while (otherVertices.hasMoreElements()){
            GraphVertex v = (GraphVertex)otherVertices.nextElement();
            ot.registerObject(v);
        }
        
        /* Register edges */
        while (otherEffects.hasMoreElements()){
            GraphEffect e = (GraphEffect)otherEffects.nextElement();
            ot.registerObject(e);
        }
    }

}
