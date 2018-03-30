
public interface MenuInterface {

    /* Interface Method for saving */
    public void didSave();
    
    /* Interface Method for saving-as */
    public void didSaveAs();
    
    /* Interface Method for loading */
    public void didLoad();
    
    /* Interface Method for an undo action */
    public void didUndo();
    
    /* Interface Method for a redo action */
    public void didRedo();
    
    /* Interface Method for a duplicate action */
    public void didDuplicate();
    
    /* Interface Method for a help action */
    public void didRequestHelp();

}