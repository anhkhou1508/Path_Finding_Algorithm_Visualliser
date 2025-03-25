import javax.swing.*;
import java.awt.*;

public class Cell extends JPanel {
    // Define static color constants for consistency
    public static final Color EMPTY_COLOR = Color.WHITE;
    public static final Color WALL_COLOR = new Color(45, 45, 45);
    public static final Color START_COLOR = new Color(50, 200, 50);
    public static final Color END_COLOR = new Color(200, 50, 50);
    public static final Color VISITED_COLOR = new Color(64, 206, 227);
    public static final Color CONSIDERING_COLOR = new Color(175, 238, 238);
    public static final Color PATH_COLOR = new Color(255, 215, 0);
    
    public enum CellType {
        EMPTY, WALL, START, END, VISITED, CONSIDERING, PATH
    }
    
    public int row, col;
    public int distance = Integer.MAX_VALUE; // For Dijkstra
    public int gScore = Integer.MAX_VALUE;   // For A*
    public int fScore = Integer.MAX_VALUE;   // For A*
    public Cell previous = null;
    
    private CellType type = CellType.EMPTY;
    
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        setPreferredSize(new Dimension(30, 30));
        setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
        setType(CellType.EMPTY);
    }
    
    public CellType getType() {
        return type;
    }
    
    public void setType(CellType type) {
        this.type = type;
        updateAppearance();
    }
    
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
        
        // Add smooth anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Add visual details based on cell type
        if (type == CellType.START) {
            drawStartSymbol(g2d);
        } else if (type == CellType.END) {
            drawEndSymbol(g2d);
        } else if (type == CellType.PATH) {
            drawPathSymbol(g2d);
        }
    }
    
    private void drawStartSymbol(Graphics2D g2d) {
        int size = Math.min(getWidth(), getHeight()) / 2;
        int x = getWidth() / 2 - size / 2;
        int y = getHeight() / 2 - size / 2;
        
        g2d.setColor(new Color(0, 100, 0));
        g2d.fillOval(x, y, size, size);
        
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        
        // Draw an 'S'
        int sx = getWidth() / 2;
        int sy = getHeight() / 2;
        g2d.drawLine(sx-3, sy-3, sx+3, sy-3);
        g2d.drawLine(sx-3, sy-3, sx-3, sy);
        g2d.drawLine(sx-3, sy, sx+3, sy);
        g2d.drawLine(sx+3, sy, sx+3, sy+3);
        g2d.drawLine(sx+3, sy+3, sx-3, sy+3);
    }
    
    private void drawEndSymbol(Graphics2D g2d) {
        int size = Math.min(getWidth(), getHeight()) / 2;
        int x = getWidth() / 2 - size / 2;
        int y = getHeight() / 2 - size / 2;
        
        g2d.setColor(new Color(139, 0, 0));
        g2d.fillOval(x, y, size, size);
        
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        int ex = getWidth() / 2;
        int ey = getHeight() / 2;
        
        // Draw a flag
        g2d.drawLine(ex-3, ey-5, ex-3, ey+5);
        g2d.drawLine(ex-3, ey-5, ex+4, ey-2);
        g2d.drawLine(ex-3, ey-1, ex+4, ey+2);
    }
    
    private void drawPathSymbol(Graphics2D g2d) {
        int size = 4;
        int x = getWidth() / 2 - size / 2;
        int y = getHeight() / 2 - size / 2;
        
        g2d.setColor(new Color(218, 165, 32));
        g2d.fillOval(x, y, size, size);
    }
    
    @Override
    public String toString() {
        return "Cell(" + row + ", " + col + ")";
    }
} 