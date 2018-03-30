
/* Allows the GraphPanel to request information from GraphController */
public interface GraphicsInterface {

    /* Interface Method for requesting the model */
    public GraphModel getModel();
    
    /* Interface Method for requesting the selected vertex */
    public GraphVertex getSelected();
    
}