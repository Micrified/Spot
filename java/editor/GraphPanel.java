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

    /* Draw's an Effect on the canvas */
    private void drawEffect(Graphics2D g, GraphEffect effect) {
        Point origin = effect.getOrigin();
        int radius = effect.getRadius();
        int diameter = radius * 2;
        Color borderColor;

        /* Save Configuration */
        Stroke lastStroke = g.getStroke();
        Color lastColor = g.getColor();

        /* Set Border and Fill Color */
        borderColor = Color.MAGENTA;

        /* Draw the Oval Border */
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(BORDER_WIDTH));
        g.drawOval((int)origin.getX() - radius, (int)origin.getY() - radius, diameter, diameter);

        /* Restore Configuration */
        g.setStroke(lastStroke);
        g.setColor(lastColor);
    }
    
    /* Draws a Vertex on the canvas */
    private void drawVertex(Graphics2D g, GraphVertex vertex){
        Point origin = vertex.getOrigin();
        int diameter = vertex.getRadius() * 2;
        Color borderColor;
        Color fillColor;
        
        /* Save Configuration */
        Stroke lastStroke = g.getStroke();
        Color lastColor = g.getColor();
        
        /* Determine Border Color */
        if (vertex == this.delegate.getSelected()){
            borderColor = Color.RED;
        } else {
            borderColor = new Color(0.94f, 0.94f, 0.94f);
        }

        /* Determine Fill Color */
        if (vertex.isTriggered()) {
            fillColor = new Color(0.9f, 0.1f, 0.1f);
        } else {
            fillColor = new Color(1.0f, 0.96f, 0.89f);
        }
        
        /* Draw the Rectangle Border */
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(BORDER_WIDTH));
        g.drawOval((int)origin.getX(), (int)origin.getY(), diameter, diameter);
        
        /* Fill the Rectangle */
        g.setColor(fillColor);
        g.fillOval((int)origin.getX(), (int)origin.getY(), diameter, diameter);
        
        /* Create the Font */
        Font font = new Font("Arial", Font.PLAIN, 16);
        FontMetrics metrics = g.getFontMetrics(font);
        
        /* Paint the Font */
        int x = (int)(diameter - metrics.stringWidth(vertex.getName())) / 2;
        int y = (int)((diameter - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setColor(Color.BLACK);
        g.setFont(font);
        g.drawString(vertex.getName(), (int)(origin.getX() + x), (int)(origin.getY() + y));
        
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

        /* Remove Expired Effects */
        model.removeExpiredEffects();

        /* Update Vertices */
        model.updateVertexStates();

        /* Draw Background First */
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, this);
        }

        /* Draw Effects Second */
        for (GraphEffect e : model.getEffects()) {
            this.drawEffect((Graphics2D)g, e);
        }
        
        /* Draw Boxes Third */
        for (GraphVertex v: model.getVertices()){
            this.drawVertex((Graphics2D)g, v);
        }
    }
    
}