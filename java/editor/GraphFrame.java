import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.File;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
public class GraphFrame extends JFrame implements MenuInterface, ButtonInterface, WindowListener, ControllerInterface {
    
    /* ******** Properties ******** */
    
    private GraphMenuBar menuBar;
    private GraphController graphController;
    private GraphButtonPanel buttonPanel;
    private String fileName;
    private GraphViewer graphViewer;

    /* ******** Constructor ******** */
    public GraphFrame(String title, String fileName, GraphModel model) {
        super(title);
        
        /* Set Close Operation */
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        /* Save FileName */
        this.fileName = fileName;
        
        /* Create Menu */
        this.menuBar = new GraphMenuBar(this);
        
        /* Create Graph Controller */
        this.graphController = new GraphController(this, model);
        
        /* Create Buttons */
        this.buttonPanel = new GraphButtonPanel(this);
        
        /* Create ContentPane */
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(graphController.getGraphPanel(), BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        
        /* Assemble Components */
        this.setJMenuBar(menuBar);
        this.setContentPane(contentPane);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setSize(1200, 900);
    }
    
    /* ******** Menu Interface ******** */
    
    /* Interface Method for saving */
    public void didSave() {
        System.out.printf("User did click save!\n");
        if (this.fileName == null){
            this.displayWarning("No file to save to!\n");
            return;
        }
        this.graphController.getModel().saveAs(this.fileName);
    }
    
    /* Interface Method for saving-as */
    public void didSaveAs() {
        System.out.printf("User did click save-as!\n");
        String newFileName = getModelSaveFile();
        if (newFileName == null){
            return;
        }
        this.fileName = newFileName;
        System.out.printf("File = " + newFileName);
        this.graphController.getModel().saveAs(newFileName);
    }
    
    /* Interface Method for loading */
    public void didLoad() {
        System.out.printf("User did click load!\n");
        String newFileName = getModelFile();
        if (newFileName == null){
            return;
        }
        try {
            this.fileName = newFileName;
            this.graphController.setModel(new GraphModel(newFileName));
            this.graphController.refreshGraph();
        } catch (Exception e){
            this.displayWarning("Couldn't read the file!");
        }
    }
    
    /* Interface Method for an undo action */
    public void didUndo() {
        System.out.printf("User did click on undo!\n");
        this.graphController.refreshGraph();
    }
    
    /* Interface Method for a redo action */
    public void didRedo() {
        System.out.printf("User did click on redo!\n");
        this.graphController.refreshGraph();
    }
    
    /* Interface Method for a duplicate action */
    public void didDuplicate() {
        System.out.printf("User did click on Duplicate!\n");
    }
    
    /* Interface Method for a help action */
    public void didRequestHelp() {
        System.out.printf("User did click on help!\n");
        this.displayHelp();
    }
    
    /* ******** Button Interface ******** */
    
    /* Interface Method for toggle of add-Vertex */
    public void didToggleAddVertex() {
        System.out.printf("User toggled Add-Vertex!\n");
        this.graphController.addNewVertex(null);
        this.graphController.refreshGraph();
    }
    
    /* Interface Method for toggle of remove-Vertex */
    public void didToggleRemoveVertex() {
        System.out.printf("User toggled Remove-Vertex!\n");
        GraphVertex vertex = this.graphController.getSelected();
        if (vertex != null){
            this.graphController.getModel().removeVertex(vertex);
            this.graphController.refreshGraph();
        }
    }

    /* ******** Controller Interface ********* */

    public void setSelectionRemovable(boolean flag) {
        this.buttonPanel.setRemovable(flag);
    }

    
    /* ******** User Prompts ******** */
    
    /* Displays a Warning Prompt to the user with the message argument */
    public void displayWarning(String message){
        JOptionPane.showMessageDialog(this, message, "Warning!", JOptionPane.ERROR_MESSAGE);
    }
    
    /* Displays a File-Chooser to choose a File from, then returns the name as a String */
    public String getModelFile(){
        /* Get current directory */
        File currentDirectory = new File(System.getProperty("user.dir"));
        
        /* Create FileChooser & Filter */
        JFileChooser fileChooser = new JFileChooser(currentDirectory);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text file", "txt");
        fileChooser.addChoosableFileFilter(filter);
        
        int r = fileChooser.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION){
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile.getName();
        }
        return null;
    }
    
    /* Displays a File-Chooser to choose a File to save to, returns the name as a String */
    public String getModelSaveFile(){
        /* Get current directory */
        File currentDirectory = new File(System.getProperty("user.dir"));
        
        /* Create FileChooser */
        JFileChooser fileChooser = new JFileChooser(currentDirectory);
        
        int r = fileChooser.showSaveDialog(this);
        if (r == JFileChooser.APPROVE_OPTION){
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile.getName();
        }
        return null;
    }
    
    /* Displays a popup for help */
    public void displayHelp(){
        JLabel lineA = new JLabel("Click to select vertices, hold to drag");
        JLabel lineB = new JLabel("Hold Shift to link or unlink vertices");
        JPanel contentPanel = new JPanel(new GridLayout(0,1));
        contentPanel.add(lineA);
        contentPanel.add(lineB);
        int r = JOptionPane.showConfirmDialog(this, contentPanel, "Instructions", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    }
    
    /* ******** WindowListener Interface ******** */
    
    /* Invoked when the a window is made visible */
    @Override
    public void windowOpened(WindowEvent e) {
    }
    /* Invoked when a user attempts to close the window */
    @Override
    public void windowClosing(WindowEvent e) {
        System.out.printf("The Viewer is closing!\n");
        GraphPanel panel = this.graphViewer.getPanel();
        this.graphController.unregisterPanel(panel);
        this.graphViewer = null;
    }
    /* Invoked when the window has been closed */
    @Override
    public void windowClosed(WindowEvent e) {
    }
    /* Invoked when the window is changed from a normal to minimized state */
    @Override
    public void windowIconified(WindowEvent e) {
    }
    /* Invoked when the window is changed from a minimized to normal state */
    @Override
    public void windowDeiconified(WindowEvent e) {
    }
    /* Invoked when the window is set to be the active window */
    @Override
    public void windowActivated(WindowEvent e) {
    }
    /* Invoked when the window is no longer the active window */
    @Override
    public void windowDeactivated(WindowEvent e) {
    }
    
}

