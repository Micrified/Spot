import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Rectangle;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GraphController implements MouseMotionListener, MouseListener, GraphicsInterface, KeyListener, ActionListener {
    
    /* ******** Properties ******** */
    private GraphFrame delegate;
    private GraphModel model;
    private GraphPanel graphPanel;
    private ArrayList<GraphPanel> subscribers;
    private GraphVertex selected;
    private Timer timer;

    /* ******** Constructors ******** */
    public GraphController(GraphFrame delegate, GraphModel model) {
        
        /* Set Delegate (Necessary for disabling/enabling buttons) */
        this.delegate = delegate;
        
        /* Initialize Model */
        this.model = (model == null ? new GraphModel() : model);
        
        /* Initialize graphPanel */
        this.graphPanel = new GraphPanel(this);
        
        /* Initialize subscribers ArrayList */
        this.subscribers = new ArrayList<GraphPanel>();
        this.subscribers.add(this.graphPanel);
        
        /* Set as mouse-listener for Graph-Panel */
        this.graphPanel.addMouseListener(this);
        this.graphPanel.addMouseMotionListener(this);
        
        /* Set as key-listener for Graph-Panel */
        this.graphPanel.addKeyListener(this);
        this.graphPanel.setFocusable(true);

        /* Initialize refresh (30fps ~ 33ms) */
        this.setTimer(33);
    }

    /* ******** Methods ******** */

    /* Intializes a timer to periodically refresh the graph. Delay in milliseconds. */
    private void setTimer (int delay) {
        timer = new Timer(delay, this);
        timer.setInitialDelay(0);
        timer.start();
    }

    /* Action-Handler for timer event */
    public void actionPerformed(ActionEvent event) {
        refreshGraph();
    }
    
    /* ******** Setters ******** */
    
    public void setModel(GraphModel model){
        this.model = model;
    }
    
    public void setSelected(GraphVertex vertex){
        if (this.delegate != null){
            boolean flag = ((vertex == null) ? false : true);
            this.delegate.setSelectionRemovable(flag);
        }
        this.selected = vertex;
    }
    
    /* ******** Getters ******** */

    /* Also part of the required GraphicsInterface Interface */
    public GraphModel getModel(){
        return this.model;
    }
    
    /* Also part of the required GraphicsInterface Interface */
    public GraphVertex getSelected(){
        return this.selected;
    }
    
    /* Returns the Core GraphPanel associated with the controller */
    public GraphPanel getGraphPanel(){
        return this.graphPanel;
    }
    
    /* Returns a list of GraphPanels receiving updates on the model */
    public ArrayList<GraphPanel> getSubscribers(){
        return this.subscribers;
    }
    
    /* ******** Interface ******** */
    
    /* Orders the GraphPanel instance to re-draw */
    public void refreshGraph(){
        for (GraphPanel panel : this.subscribers){
            panel.refresh();
        }
    }

    /* Adds a new Effect to the graph */
    public void addNewEffect(Point origin) {
        int radius = 100;
        GraphEffect effect = new GraphEffect(origin, radius);
        this.model.addEffect(effect);
    }
    
    /* Adds a new Vertex to the graph */
    public void addNewVertex(GraphVertex vertex){
        int radius = 10;

        if (vertex == null){
            int identityNumber = 1 + this.model.getVertices().size();
            int midX = (int)(this.graphPanel.getWidth()/2);
            int midY = (int)(this.graphPanel.getHeight()/2);
            vertex = new GraphVertex(("" + identityNumber), new Point(midX - radius, midY - radius), radius);
        }
        this.model.addVertex(vertex);
    }
    
    /* Registers a new Panel to receive information */
    public void registerPanel(GraphPanel panel){
        if (panel == null){
            return;
        }
        if (this.subscribers.contains(panel)){
            return;
        }
        
        /* Set its delegate, sign it up to receive updates */
        panel.setDelegate(this);
        this.subscribers.add(panel);
    }
    
    /* De-registers a panel from receiving information */
    public void unregisterPanel(GraphPanel panel){
        if (panel == null){
            return;
        }
        if(!this.subscribers.contains(panel)){
            return;
        }
        /* Remove as delegate, remove from receiving updates */
        panel.setDelegate(null);
        this.subscribers.remove(panel);
    }
    
    /* Returns the vertex at the given point. If none, returns null */
    private GraphVertex vertexAtPoint(Point point){
        for (GraphVertex v : model.getVertices()){
            if (v.contains(point)) {
                return v;
            }
        }
        return null;
    }
    
    /* ******** MouseListener Interface ******** */
    
    /* Invoked when the mouse has been clicked (Pressed + Released) */
    @Override
    public void mouseClicked(MouseEvent event){

        // If a double-click -> Fire effect.
        if (event.getClickCount() == 2 && event.isConsumed() == false) {
            System.out.println("Double Click!");
            event.consume();
            this.addNewEffect(event.getPoint());
            return;
        }
        System.out.printf("Click!\n");

        // Otherwise selection/deselection.
        GraphVertex atPoint = vertexAtPoint(event.getPoint());
        
        if (atPoint == null){
            System.out.printf("Miss!\n");
            /* If something selected, unselect it */
            if (selected != null){
                System.out.printf("Miss! [Deselection]\n");
                this.setSelected(null);
            }
            
        } else {
            this.setSelected(atPoint);
        }
    }

    /* Invoked when the mouse enters a component */
    @Override
    public void mouseEntered(MouseEvent event){
        
    }
    /* Invoked when the mouse exits a component */
    @Override
    public void mouseExited(MouseEvent event){
        
    }
    /* Invoked when the mouse has been pressed on a component */
    @Override
    public void mousePressed(MouseEvent event){
        
    }
    /* Invoked when the mouse has been released on a component */
    @Override
    public void mouseReleased(MouseEvent event){
        
    }
    
    /* ******** MouseMotionListener Interface ******** */
    
    /* Invoked when a mouse is pressed on a component then dragged */
    @Override
    public void mouseDragged(MouseEvent event){
        GraphVertex atPoint = vertexAtPoint(event.getPoint());
        
        /* If dragging nothing, return */
        if (atPoint == null){
            this.setSelected(null);
            return;
        }
        
        /* If dragging something, and it is selected, move it */
        if (atPoint == selected){
            selected.centerAroundPoint(event.getPoint());
        }
    }
    
    /* Invoked when the mouse cursor has been moved onto a component */
    @Override
    public void mouseMoved(MouseEvent event){

        return;
    }
    
    /* ******** KeyListener Interface ******** */

    /* Invoked when a key has been pressed */
    @Override
    public void keyPressed(KeyEvent e){
        if (e.getKeyCode() == KeyEvent.VK_SHIFT){
            //this.shiftPressed = true;
        }
    }
    /* Invoked when a key has been released */
    @Override
    public void keyReleased(KeyEvent e){
        if (e.getKeyCode() == KeyEvent.VK_SHIFT){
            //this.shiftPressed = false;
        }
    }
    /* Invoked when a key has been typed */
    @Override
    public void keyTyped(KeyEvent e){
    }
    

}