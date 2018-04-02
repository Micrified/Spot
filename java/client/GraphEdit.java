import java.util.*;
import java.io.*;
import java.awt.Rectangle;

public class GraphEdit {

    /* Main assumes that the given file-name is located in args[0] */
    public static void main(String[] args) {
        GraphModel m = null;
        String exchangeAddress = null;

        /* Read in Message-Exchange IP from program arguments */
        if (args.length != 1) {
            System.out.println("Usage: sh run.sh <exchange-IP-address>");
            return;
        } else {
            exchangeAddress = args[0];
            System.out.println("GraphEdit: Initialized with Exchange-IP: " + exchangeAddress);
        }

        
        /* Show Frame */
        GraphFrame frame = new GraphFrame("Sensor Simulator", exchangeAddress, m);
        frame.setVisible(true);
    }

}
