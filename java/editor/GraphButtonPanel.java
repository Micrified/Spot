import javax.swing.JPanel;
import java.awt.FlowLayout;
import javax.swing.JButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GraphButtonPanel extends JPanel {
    
    /* ******** Properties ******** */
    private GraphFrame delegate;
    
    private JButton addVertexButton;
    private JButton removeVertexButton;

    /* ******** Constructor ******** */
    public GraphButtonPanel(GraphFrame delegate) {
        
        /* Set Delegate */
        this.delegate = delegate;
        
        /* Configure Layout */
        setLayout(new FlowLayout());
        
        /* Configure Buttons */
        this.addVertexButton = new JButton("New Sensor");
        this.removeVertexButton = new JButton("Remove Sensor");
        
        /* Set Non-Focusable (Important for the Graph) */
        this.addVertexButton.setFocusable(false);
        this.removeVertexButton.setFocusable(false);
        
        /* Add Action Listeners for Buttons */
        addVertexButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GraphButtonPanel.this.delegate.didToggleAddVertex();
            }
        });
        
        removeVertexButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GraphButtonPanel.this.delegate.didToggleRemoveVertex();
            }
        });
        
        /* Add Buttons to Panel */
        this.add(addVertexButton);
        this.add(removeVertexButton);
        
        /* Set to Default State */
        this.setRemovable(false);
    }
    
    /* ******** Setters ******** */
    public void setDelegate(GraphFrame delegate){
        this.delegate = delegate;
    }
    
    /* ******** Getters ******** */
    public GraphFrame getDelegate(){
        return this.delegate;
    }
    
    /* ******** Interface ******** */

    /* Enables the removal of a vertex */
    public void setRemovable (boolean flag) {
        this.removeVertexButton.setEnabled(flag);
    }

}
