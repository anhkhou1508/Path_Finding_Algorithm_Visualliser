import javax.swing.*;
import java.awt.*;

/**
 * Represents a cell in the grid
 * Displays different states visually
 */
public class Cell extends JPanel {
    // Cell types
    public enum CellType {
        EMPTY,
        WALL,
        START,
        END,
        VISITED,
        CONSIDERING,
        PATH
    }
    
    // Cell properties
    public final int row;
    public final int col;
    private CellType type;
    
    // For pathfinding algorithms
    public Cell previous;
    public int gScore = 0;
    public int fScore = 0;
    public int distance = Integer.MAX_VALUE; // Added for Dijkstra's algorithm
    
    // For Q-value visualization
    private double qValue = 0;
    private boolean showQValue = false;
    
    // For dynamic danger zone visualization
    private boolean isDangerZone = false;
    private Color dangerColor = null;
    
    // For obstacle trail visualization
    private boolean isTrail = false;
    private Color trailColor = null;
    
    // Colors
    public static final Color EMPTY_COLOR = Color.WHITE;
    public static final Color WALL_COLOR = Color.BLACK;
    public static final Color START_COLOR = Color.GREEN;
    public static final Color END_COLOR = Color.RED;
    public static final Color VISITED_COLOR = new Color(175, 238, 238);  // Pale Turquoise
    public static final Color CONSIDERING_COLOR = new Color(135, 206, 250);  // Light Sky Blue
    public static final Color PATH_COLOR = new Color(255, 255, 0);  // Yellow
    
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.type = CellType.EMPTY;
        
        setBackground(EMPTY_COLOR);
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        setPreferredSize(new Dimension(30, 30));
    }
    
    /**
     * Sets the type of this cell
     */
    public void setType(CellType type) {
        this.type = type;
        updateAppearance();
    }
    
    /**
     * Gets the type of this cell
     */
    public CellType getType() {
        return type;
    }
    
    /**
     * Sets the Q-value for visualization
     */
    public void setQValue(double value) {
        this.qValue = value;
        this.showQValue = true;
        repaint();
    }
    
    /**
     * Clears Q-value visualization
     */
    public void clearQValue() {
        this.showQValue = false;
        repaint();
    }
    
    /**
     * Sets or clears the danger zone visualization
     */
    public void setDangerZone(boolean isDanger, Color color) {
        this.isDangerZone = isDanger;
        this.dangerColor = color;
        repaint();
    }
    
    /**
     * Sets or clears the trail visualization for obstacle movement
     */
    public void setTrail(boolean isTrail, Color color) {
        this.isTrail = isTrail;
        this.trailColor = color;
        repaint();
    }
    
    /**
     * Updates the cell's appearance based on its type
     */
    private void updateAppearance() {
        switch (type) {
            case EMPTY:
                setBackground(EMPTY_COLOR);
                break;
            case WALL:
                setBackground(WALL_COLOR);
                break;
            case START:
                setBackground(START_COLOR);
                break;
            case END:
                setBackground(END_COLOR);
                break;
            case VISITED:
                setBackground(VISITED_COLOR);
                break;
            case CONSIDERING:
                setBackground(CONSIDERING_COLOR);
                break;
            case PATH:
                setBackground(PATH_COLOR);
                break;
        }
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Draw Q-value heat map if needed
        if (showQValue && type != CellType.WALL) {
            // Use red to blue gradient based on Q-value
            float blend = (float) qValue;
            Color heatColor = new Color(
                (int) (RLPathAgent.HEAT_MAP_HIGH.getRed() * blend + RLPathAgent.HEAT_MAP_LOW.getRed() * (1-blend)),
                (int) (RLPathAgent.HEAT_MAP_HIGH.getGreen() * blend + RLPathAgent.HEAT_MAP_LOW.getGreen() * (1-blend)),
                (int) (RLPathAgent.HEAT_MAP_HIGH.getBlue() * blend + RLPathAgent.HEAT_MAP_LOW.getBlue() * (1-blend)),
                180  // semi-transparent
            );
            
            g2d.setColor(heatColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Optional: show numerical Q-value
            /*
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.format("%.2f", qValue), 5, getHeight() - 5);
            */
        }
        
        // Draw the danger zone overlay if enabled
        if (isDangerZone && dangerColor != null && type != CellType.WALL) {
            g2d.setColor(dangerColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Add a small warning indicator
            g2d.setColor(Color.WHITE);
            g2d.drawString("!", getWidth()/2-3, getHeight()/2+5);
        }
        
        // Draw the trail overlay if enabled
        if (isTrail && trailColor != null && type != CellType.WALL) {
            g2d.setColor(trailColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Optional: add small dots to indicate trail
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.fillOval(getWidth()/2-3, getHeight()/2-3, 6, 6);
        }
    }
    
    @Override
    public String toString() {
        return "Cell[" + row + "," + col + "]";
    }
} 