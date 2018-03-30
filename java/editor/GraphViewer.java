import java.util.*;
import java.io.*;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GraphViewer extends JFrame {
    
    /* ******** Properties ******** */
    private GraphPanel graphPanel;
    
    /* ******** Constructor ******** */
    public GraphViewer(String title, GraphPanel panel){
        super(title);
        
        /* Set graphPanel */
        this.graphPanel = (panel == null ? new GraphPanel(null) : panel);
        
        /* Assign panel as content pane */
        this.setContentPane(panel);
        
        /* Prepare Window */
        this.pack();
        this.setLocationRelativeTo(null);
        this.setSize(600, 400);
    }
    
    /* ******** Setters ******** */
    public void setPanel(GraphPanel panel){
        this.graphPanel = panel;
    }
    
    /* ******** Getters ******** */
    public GraphPanel getPanel(){
        return this.graphPanel;
    }
    
}
