import java.util.*;
import java.io.*;
import java.awt.Rectangle;

public class GraphEdit {

    /* Main assumes that the given file-name is located in args[0] */
    public static void main(String[] args) {
        GraphModel m = null;
        String fileName = null;
        
        if (args.length != 0){
            fileName = args[0];
            System.out.println("Argument: " + fileName + " has been supplied.\n");
            try {
                m = new GraphModel(fileName);
            } catch (Exception e) {
                System.out.println("File " + fileName + " is corrupted!\n");
                m = null;
                fileName = null;
            }
        }
        
        /* Show Frame */
        GraphFrame frame = new GraphFrame("Sensor Simulator", fileName, m);
        frame.setVisible(true);
    }

}
