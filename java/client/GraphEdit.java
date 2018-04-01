import java.util.*;
import java.io.*;
import java.awt.Rectangle;

public class GraphEdit {

    /* Main assumes that the given file-name is located in args[0] */
    public static void main(String[] args) {
        GraphModel m = null;
        String fileName = null;
        
        /* Show Frame */
        GraphFrame frame = new GraphFrame("Sensor Simulator", fileName, m);
        frame.setVisible(true);
    }

}
