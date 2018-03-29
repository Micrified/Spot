import java.io.*;
import java.net.*;
import java.nio.charset.Charset;

public class ServerConnector {

    /* Port on the server to connect to */
    private int port = 8024;

    /* Address to connect to */
    private InetAddress address;

    /********* Constructor *********/
    public ServerConnector (int port, String address) {
        this.port = port;
        this.address = getInetAddressFromString(address);
    }

    /********* Methods *********/

    private InetAddress getInetAddressFromString (String address) {
        try {
            return InetAddress.getByName(address);
        } catch (IOException e) {
            System.out.println("[ServerConnector]: Exception resolving address." + e.toString());
            return null;
        }
    }

    /* Connects to a TCP server and sends a signal gram */
    public Boolean send (String gram) {

        System.out.printf("Sending: \"%s\"\n", gram);

        // Verify required properties are set.
        if (address == null || gram == null) {
            System.out.println("Error: Can't connect with null fields!");
            return false;
        }

        // Send Gram to server.
        try {
            byte[] bytes = gram.getBytes(Charset.forName("UTF-8"));
            String len = bytes.length + "";
            byte[] head = len.getBytes(Charset.forName("UTF-8"));

            Socket socket = new Socket(address, port);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.write(bytes);
            socket.close();
            return true;
        } catch (Exception e) {
            System.out.println("[ServerConnector] Exception when sending Gram!\n" + e.toString());
            return false;
        }
    }
}