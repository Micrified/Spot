import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.io.IOException;

public class GraphPanel extends JPanel {
    
    /* ******** Properties ******** */
    private GraphController delegate;
    private static int LINE_WIDTH = 3;
    private static int BORDER_WIDTH = 2;
    private BufferedImage backgroundImage;
    private String backgroundImageURL = "background.png";

    /* ******** Constructors ******** */
    public GraphPanel(GraphController delegate) {
        this.delegate = delegate;
        getBufferedImage(backgroundImageURL);
    }

    public GraphPanel(GraphController delegate, String backgroundImageURL) {
        this.delegate = delegate;
        this.backgroundImageURL = backgroundImageURL;
        getBufferedImage(backgroundImageURL);
    }
    
    /* ******** Setters ******** */
    
    public void setDelegate(GraphController delegate){
        this.delegate = delegate;
    }
    
    /* ******** Getters ******** */
    
    public GraphController getDelegate(){
        return this.delegate;
    }
    
    /* ******** Graph Interface ******** */
    
    /* Interface Method for re-drawing the graph */
    public void refresh() {
        repaint();
    }

    /* ******** Resource Loading ******* */

    /* Attempt to load buffered image from resource URL */
    private void getBufferedImage (String fromResourceURL) {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(fromResourceURL));
            this.backgroundImage = bufferedImage;
            return;
        } catch (IOException e) {
            System.out.println("Error: Failed to load background image: " + fromResourceURL);
        }
        this.backgroundImage = null;
    }
    
    /* ******** Line Painter ******** */

    /* Draws text on the canvas */
    private void drawText (Graphics2D g, String text, Color color, Point origin, int diameter) {

        /* Save Configuration */
        Stroke lastStroke = g.getStroke();
        Color lastColor = g.getColor();

        /* Create the Font */
        Font font = new Font("Arial", Font.PLAIN, 16);
        FontMetrics metrics = g.getFontMetrics(font);
        
        /* Paint the Font */
        int x = (int)(diameter - metrics.stringWidth(text)) / 2;
        int y = (int)((diameter - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setColor(color);
        g.setFont(font);
        g.drawString(text, (int)(origin.getX() + x), (int)(origin.getY() + y));

        /* Restore Configuration */
        g.setStroke(lastStroke);
        g.setColor(lastColor);
    }

    /* Draws a Cluster on the canvas */
    private void drawCluster(Graphics2D g, GraphCluster cluster) {
        ArrayList<Sensor> sensors = cluster.getSensors();
        Point estimate = cluster.getOrigin();
        double lifetime = cluster.getLifetime();

        // Determine color.
        Color color = new Color(Math.min(Math.max((int)(255 * lifetime), 0), 255), 0, 0);

        // Draw sensors.
        for (int i = 0; i < sensors.size(); i++) {
            Sensor s = sensors.get(i);
            drawSensor(g, color, s.getIdentifier(), s.getLocation());
        }

        // Draw estimated origin (graphics, text, color, point, diameter).
        drawText(g, "x", Color.CYAN, estimate, 10);
    }
    
    /* Draws a Vertex on the canvas */
    private void drawSensor(Graphics2D g, Color color, String name, Point p){
        int diameter = 10;

        /* Save Configuration */
        Stroke lastStroke = g.getStroke();
        Color lastColor = g.getColor();
        
        /* Draw the Rectangle Border */
        g.setColor(color);
        g.setStroke(new BasicStroke(BORDER_WIDTH));
        g.drawOval((int)p.getX(), (int)p.getY(), diameter, diameter);
        
        /* Fill the Rectangle */
        g.setColor(color);
        g.fillOval((int)p.getX(), (int)p.getY(), diameter, diameter);

        /* Paint the Font */
        drawText(g, name, Color.WHITE, p, diameter);
        
        /* Restore Configuration */
        g.setStroke(lastStroke);
        g.setColor(lastColor);
    }
    
    /* PaintComponent */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        /* Clear Screen */
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        GraphModel model = this.delegate.getModel();
        
        /* Do nothing if there isn't a model */
        if (model == null){
            return;
        }

        /* Draw Background First */
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, this);
        }

        /* Draw clusters */
        for (GraphCluster c : model.getClusters()) {
            this.drawCluster((Graphics2D)g, c);
        }
    }
}