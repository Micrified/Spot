import java.util.ArrayList;
import java.util.Collections;
import java.lang.Long;
import java.lang.Number;
import java.awt.Point;

public class ClusterParser {

    private char[] characters;  // Character buffer.
    private String justParsed;  // Stores last substring parsed.

    /*
     *************************************************************************
     *                             Constructors                              *
     *************************************************************************
    */

    public ClusterParser () {
    }

    /*
     *************************************************************************
     *                            Public Methods                             *
     *************************************************************************
    */

    // Initializes GraphCluster from a given JSON string. Returns NULL on failure.
    public GraphCluster toGraphCluster(String data) {
        GraphCluster cluster = new GraphCluster();

        // Reset global stores.
        characters = data.toCharArray();
        justParsed = "";

        // Accept: {"Id":<long>
        if (!acceptString("{\"Id\":") || !acceptInteger()) {
            System.out.println("ClusterParser :: toGraphCluster :: Expected {\"Id\":<long>");
            return null;
        }

        // ASSIGN: Cluster Identifier.
        cluster.setIdentifier(justParsed);

        // Accept: ,"Updated":<long>
        if (!acceptString(",\"Updated\":") || !acceptInteger()) {
            System.out.println("ClusterParser :: toGraphCluster :: Expected ,\"Updated\":<long>");
            return null;
        }

        // ASSIGN: Cluster lastUpdated value.
        cluster.setLastUpdated(Long.parseLong(justParsed));

        // Accept ,"Members":[
        if (!acceptString(",\"Members\":[")) {
            System.out.println("ClusterParser :: toGraphCluster :: Expected ,\"Members\":[");
            return null;
        }

        // Accept all members: Events must contain at least ONE gram.
        do {
            Sensor s = toSensor();
            if (s == null) {
                System.out.println("ClusterParser :: toGraphCluster :: toSensor() failed!");
                return null;
            }
            cluster.getSensors().add(s);
        } while (acceptString(","));

        // Accept ]}
        if (!acceptString("]}")) {
            System.out.println("ClusterParser :: toGraphCluster :: Expected ]}\n");
            return null;
        }
        
        return cluster;
    }

    // Initializes Sensor instance from current JSON string in characters.
    private Sensor toSensor() {
        Sensor sensor = new Sensor();

        // Accept {"Id":<long>
        if (!acceptString("{\"Id\":") || !acceptInteger()) {
            System.out.println("ClusterParser :: toSensor :: Expected {\"Id\":<long>");
            return null;
        }

        // ASSIGN: Sensor identifer.
        sensor.setIdentifier(justParsed);

        // Accept ,"When":
        if (!acceptString(",\"When\":") || !acceptInteger()) {
            System.out.println("ClusterParser :: toSensor :: Expected ,\"When\":<long>"); 
            return null;
        }

        // ASSIGN: Sensor time.
        sensor.setTime(Long.parseLong(justParsed));

        // Accept ,"Location":[<long>
        if (!acceptString(",\"Location\":[") || !acceptDouble()) {
            System.out.println("ClusterParser :: toSensor :: Expected ,\"Location\":[<double>");
            return null;    
        }

        double x = Double.parseDouble(justParsed);

        // Accept ,<long>
        if (!acceptString(",") || !acceptDouble()) {
            System.out.println("ClusterParser :: toSensor :: Expected ,<double>\n");
            return null;
        }

        double y = Double.parseDouble(justParsed);

        // ASSIGN: Sensor location. (redundancy is noted -_-)
        sensor.setLocation(new Point((int)x, (int)y));

        // Accept ]
        if (!acceptString("]")) {
            System.out.println("ClusterParser :: toSensor :: Expected ]\n");
            return null;
        }

        // Discard Signal.
        dropUntil('}');

        // Accept }.
        if (!acceptString("}")) {
            System.out.println("ClusterParser :: toSensor :: Expected }\n");
            return null;
        }

        return sensor;
    }

    /*
     *************************************************************************
     *                           Utility Routines                            *
     *************************************************************************
    */

    // Returns a substring (begin, end)
    private String getSubstring(int begin, int end) {
        String s = "";
        for (Character c : characters) {
            s += c.toString();
        }
        return s.substring(begin, end);
    }

    // Drops a given amount of characters from the buffer.
    private void drop (int n) {
        String remaining = "";
        for (int i = n; i < characters.length; i++) {
            remaining += characters[i];
        }
        characters = remaining.toCharArray();
    }

    // Drops characters until specified one appears or EOF.
    private void dropUntil(char c) {
        int i;

        // Determine number of characters to drop.
        for (i = 0; i < characters.length; i++) {
            if (characters[i] == c) {
                break;
            }
        }

        // Drop characters.
        drop(i);
    }

    /*
     *************************************************************************
     *                           Parsing Routines                            *
     *************************************************************************
    */

    // Returns true if a double is in the character buffer.
    private Boolean acceptDouble() {
        StringBuilder accepted = new StringBuilder();
        int i = 0;

        // Accept a positive or negative sign - if any.
        if (characters[i] == '+' || characters[i] == '-') {
            accepted.append(characters[i++]);
        }

        // Accept Leading Digits (Require at least 1).
        if (!Character.isDigit(characters[i])) {
            System.out.println("acceptDouble: Requires leading digit!\n");
            return false;
        }
        while (Character.isDigit(characters[i])) {
            accepted.append(characters[i++]);
        }

        // If not accepting decimal point, then return here.
        if (characters[i] != '.') {
            justParsed = accepted.toString();
            drop(i);
            return true;
        } else {
            accepted.append(characters[i++]);
        }

        // Accept Trailing Digits (Require at least 1).
        if (!Character.isDigit(characters[i])) {
            System.out.println("acceptDouble: Requires trailing digit!\n");
            return false;
        }
        while (Character.isDigit(characters[i])) {
            accepted.append(characters[i++]);
        }

        // Do not accept E or any of that nonsense.
        justParsed = accepted.toString();
        drop(i);
        return true;
    }

    // Returns true if a POSITIVE long is in the character buffer. 
    private Boolean acceptInteger () {
        StringBuilder accepted = new StringBuilder();
        int i = 0;

        // While accepting digits, append to string.
        while (Character.isDigit(characters[i])) {
            accepted.append(characters[i]);
            i++;
        }

        // Assign to justParsed the parsed string.
        justParsed = accepted.toString();

        // Remove string from buffer.
        drop(i);

        // Return True if any digit was accepted.
        return (i > 0);
    }

    // Returns true if string in character buffer.
    private Boolean acceptString (String s) {
        char[] cs = s.toCharArray();
        int i;

        // Return false if any character within 's' isn't a substring of 'characters'.
        for (i = 0; i < cs.length; i++) {
            if (i >= characters.length || cs[i] != characters[i]) {
                return false;
            }
        }

        // Assign to justParsed the parsed string.
        justParsed = s;

        // Remove string from buffer.
        drop(i);

        // Return True.
        return true;
    }
}

/*
String example = {"Id":1522446822832256003,
                  "Updated":1522446823114,
                  "Members":[{"Id":1,
                              "When":1522446822828,
                              "Location":[561,451],
                              "Signal":[0,0,36.5,19.7,5.8,0.7,0]},
                             {"Id":2,
                              "When":1522446823041,
                              "Location":[540,412],
                              "Signal":[0,0,33.5,18.1,5.4,0.7,0]},
                             {"Id":3,
                              "When":1522446823114,
                              "Location":[590,398],
                              "Signal":[0,0,32.5,17.5,5.2,0.6,0]}
                            ]
                 }
*/

// {"Id":1522446822832256003,"Updated":1522446823114}