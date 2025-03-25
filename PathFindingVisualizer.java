import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class PathFindingVisualizer extends JFrame {
    private static final int GRID_SIZE = 20;
    private static final int CELL_SIZE = 30;
    private static final int DELAY = 50; // milliseconds between visualization steps
    
    // Custom colors
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color TITLE_COLOR = new Color(50, 50, 50);
    private static final Color PANEL_BACKGROUND = new Color(250, 250, 250);
    private static final Color BUTTON_BACKGROUND = new Color(70, 130, 180); // Steel Blue
    private static final Color BUTTON_TEXT = Color.BLACK; // Changed to black for better visibility
    private static final Color ERROR_TEXT = new Color(200, 0, 0);
    
    private Cell[][] grid;
    private Cell startCell;
    private Cell endCell;
    private JPanel gridPanel;
    private JComboBox<String> algorithmSelector;
    private JButton startButton;
    private JButton resetButton;
    private JButton clearPathButton;
    private JLabel statusLabel;
    
    private boolean isRunning = false;
    private javax.swing.Timer timer;
    
    public PathFindingVisualizer() {
        setTitle("Path Finding Algorithm Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        createTitlePanel();
        initializeGrid();
        createControlPanel();
        createLegendPanel();
        
        // Add padding around the content
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_COLOR);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("Path Finding Algorithm Visualizer");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(TITLE_COLOR);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JLabel subtitleLabel = new JLabel("Visualize and compare different path finding algorithms");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setHorizontalAlignment(JLabel.CENTER);
        subtitleLabel.setForeground(new Color(100, 100, 100));
        
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.CENTER);
        
        add(titlePanel, BorderLayout.NORTH);
    }
    
    private void initializeGrid() {
        grid = new Cell[GRID_SIZE][GRID_SIZE];
        gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 1, 1));
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        gridPanel.setBackground(Color.DARK_GRAY); // Grid lines color
        
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                grid[row][col] = new Cell(row, col);
                gridPanel.add(grid[row][col]);
                
                // Add mouse listeners to cells for interaction
                Cell cell = grid[row][col];
                cell.addMouseListener(new CellClickListener(cell));
            }
        }
        
        // Default start and end positions
        startCell = grid[2][2];
        startCell.setType(Cell.CellType.START);
        
        endCell = grid[GRID_SIZE - 3][GRID_SIZE - 3];
        endCell.setType(Cell.CellType.END);
        
        // Add grid to a container with padding
        JPanel gridContainer = new JPanel(new BorderLayout());
        gridContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        gridContainer.add(gridPanel, BorderLayout.CENTER);
        
        add(gridContainer, BorderLayout.CENTER);
    }
    
    private void createControlPanel() {
        JPanel controlPanelContainer = new JPanel(new BorderLayout());
        controlPanelContainer.setBackground(BACKGROUND_COLOR);
        
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(PANEL_BACKGROUND);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 10),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(10, 10, 10, 10)
            )
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Algorithm selection dropdown
        JLabel algoLabel = new JLabel("Select Algorithm:");
        algoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        algorithmSelector = new JComboBox<>(new String[]{
            "Dijkstra's Algorithm", 
            "A* Algorithm", 
            "Greedy Best-First Search", 
            "Breadth-First Search"
        });
        algorithmSelector.setBackground(Color.WHITE);
        algorithmSelector.setFocusable(false);
        
        // Buttons with custom styling
        startButton = createStyledButton("Start");
        startButton.addActionListener(e -> startAlgorithm());
        
        resetButton = createStyledButton("Reset Grid");
        resetButton.addActionListener(e -> resetGrid());
        
        clearPathButton = createStyledButton("Clear Path");
        clearPathButton.addActionListener(e -> clearPath());
        
        statusLabel = new JLabel("Select an algorithm and press Start");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        // Add components using GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        controlPanel.add(algoLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        controlPanel.add(algorithmSelector, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.25;
        controlPanel.add(startButton, gbc);
        
        gbc.gridx = 1;
        controlPanel.add(clearPathButton, gbc);
        
        gbc.gridx = 2;
        controlPanel.add(resetButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        controlPanel.add(statusLabel, gbc);
        
        controlPanelContainer.add(controlPanel, BorderLayout.CENTER);
        add(controlPanelContainer, BorderLayout.SOUTH);
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(BUTTON_BACKGROUND);
        button.setForeground(BUTTON_TEXT);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_BACKGROUND.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BUTTON_BACKGROUND);
            }
        });
        
        return button;
    }
    
    private void createLegendPanel() {
        JPanel legendPanel = new JPanel(new GridLayout(0, 1, 5, 10));
        legendPanel.setBackground(PANEL_BACKGROUND);
        legendPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 15, 5, 5),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createTitledBorder(
                    BorderFactory.createEmptyBorder(10, 10, 10, 10),
                    "Legend",
                    TitledBorder.CENTER,
                    TitledBorder.TOP,
                    new Font("Arial", Font.BOLD, 14)
                )
            )
        ));
        
        legendPanel.add(createLegendItem("Start", Cell.START_COLOR));
        legendPanel.add(createLegendItem("End", Cell.END_COLOR));
        legendPanel.add(createLegendItem("Wall", Cell.WALL_COLOR));
        legendPanel.add(createLegendItem("Empty", Cell.EMPTY_COLOR));
        legendPanel.add(createLegendItem("Visited", Cell.VISITED_COLOR));
        legendPanel.add(createLegendItem("Considering", Cell.CONSIDERING_COLOR));
        legendPanel.add(createLegendItem("Path", Cell.PATH_COLOR));
        
        // Add instructions
        JPanel instructionsPanel = new JPanel(new BorderLayout());
        instructionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        instructionsPanel.setBackground(PANEL_BACKGROUND);
        
        JLabel instructionsTitle = new JLabel("Instructions:", JLabel.LEFT);
        instructionsTitle.setFont(new Font("Arial", Font.BOLD, 14));
        
        JTextArea instructionsText = new JTextArea(
            "• Left-click to place/remove walls\n" +
            "• Right-click to remove walls\n" +
            "• Click start/end to move them"
        );
        instructionsText.setEditable(false);
        instructionsText.setBackground(PANEL_BACKGROUND);
        instructionsText.setFont(new Font("Arial", Font.PLAIN, 12));
        instructionsText.setLineWrap(true);
        instructionsText.setWrapStyleWord(true);
        
        instructionsPanel.add(instructionsTitle, BorderLayout.NORTH);
        instructionsPanel.add(instructionsText, BorderLayout.CENTER);
        
        legendPanel.add(instructionsPanel);
        
        add(legendPanel, BorderLayout.EAST);
    }
    
    private JPanel createLegendItem(String text, Color color) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(PANEL_BACKGROUND);
        
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(25, 25));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        
        panel.add(colorBox, BorderLayout.WEST);
        panel.add(label, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void startAlgorithm() {
        if (isRunning) return;
        
        clearPath();
        isRunning = true;
        startButton.setEnabled(false);
        algorithmSelector.setEnabled(false);
        statusLabel.setText("Running algorithm...");
        statusLabel.setForeground(Color.BLACK);
        
        String selectedAlgorithm = (String) algorithmSelector.getSelectedItem();
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private boolean pathFound = false;
            
            @Override
            protected Void doInBackground() {
                switch (selectedAlgorithm) {
                    case "Dijkstra's Algorithm":
                        pathFound = runDijkstra();
                        break;
                    case "A* Algorithm":
                        pathFound = runAStar();
                        break;
                    case "Greedy Best-First Search":
                        pathFound = runGreedyBestFirst();
                        break;
                    case "Breadth-First Search":
                        pathFound = runBreadthFirst();
                        break;
                }
                return null;
            }
            
            @Override
            protected void done() {
                isRunning = false;
                startButton.setEnabled(true);
                algorithmSelector.setEnabled(true);
                
                if (pathFound) {
                    statusLabel.setText("Algorithm completed - Path found!");
                    statusLabel.setForeground(Color.BLACK);
                } else {
                    statusLabel.setText("No path found to destination!");
                    statusLabel.setForeground(ERROR_TEXT);
                    JOptionPane.showMessageDialog(
                        PathFindingVisualizer.this,
                        "No path could be found to the destination!",
                        "Path Not Found",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        };
        
        worker.execute();
    }
    
    private boolean runDijkstra() {
        PriorityQueue<Cell> openSet = new PriorityQueue<>(Comparator.comparingInt(c -> c.distance));
        Set<Cell> closedSet = new HashSet<>();
        
        // Initialize all cells
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                grid[row][col].distance = Integer.MAX_VALUE;
                grid[row][col].previous = null;
            }
        }
        
        startCell.distance = 0;
        openSet.add(startCell);
        
        while (!openSet.isEmpty()) {
            Cell current = openSet.poll();
            
            if (current == endCell) {
                reconstructPath();
                return true;
            }
            
            closedSet.add(current);
            
            if (current != startCell && current != endCell) {
                current.setType(Cell.CellType.VISITED);
                pause();
            }
            
            for (Cell neighbor : getNeighbors(current)) {
                if (closedSet.contains(neighbor) || neighbor.getType() == Cell.CellType.WALL) {
                    continue;
                }
                
                int tempDistance = current.distance + 1;
                
                if (tempDistance < neighbor.distance) {
                    neighbor.distance = tempDistance;
                    neighbor.previous = current;
                    
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                        if (neighbor != endCell) {
                            neighbor.setType(Cell.CellType.CONSIDERING);
                            pause();
                        }
                    } else {
                        // Need to update position in priority queue
                        openSet.remove(neighbor);
                        openSet.add(neighbor);
                    }
                }
            }
        }
        
        return false;
    }
    
    private boolean runAStar() {
        PriorityQueue<Cell> openSet = new PriorityQueue<>(Comparator.comparingInt(c -> c.fScore));
        Set<Cell> openSetContents = new HashSet<>();
        Set<Cell> closedSet = new HashSet<>();
        
        // Initialize all cells
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Cell cell = grid[row][col];
                cell.gScore = Integer.MAX_VALUE;
                cell.fScore = Integer.MAX_VALUE;
                cell.previous = null;
            }
        }
        
        startCell.gScore = 0;
        startCell.fScore = heuristic(startCell, endCell);
        openSet.add(startCell);
        openSetContents.add(startCell);
        
        while (!openSet.isEmpty()) {
            Cell current = openSet.poll();
            openSetContents.remove(current);
            
            if (current == endCell) {
                reconstructPath();
                return true;
            }
            
            closedSet.add(current);
            
            if (current != startCell && current != endCell) {
                current.setType(Cell.CellType.VISITED);
                pause();
            }
            
            for (Cell neighbor : getNeighbors(current)) {
                if (closedSet.contains(neighbor) || neighbor.getType() == Cell.CellType.WALL) {
                    continue;
                }
                
                int tentativeGScore = current.gScore + 1;
                
                if (tentativeGScore < neighbor.gScore) {
                    neighbor.previous = current;
                    neighbor.gScore = tentativeGScore;
                    neighbor.fScore = neighbor.gScore + heuristic(neighbor, endCell);
                    
                    if (!openSetContents.contains(neighbor)) {
                        openSet.add(neighbor);
                        openSetContents.add(neighbor);
                        
                        if (neighbor != endCell) {
                            neighbor.setType(Cell.CellType.CONSIDERING);
                            pause();
                        }
                    } else {
                        // Need to update position in priority queue
                        openSet.remove(neighbor);
                        openSet.add(neighbor);
                    }
                }
            }
        }
        
        return false;
    }
    
    private boolean runGreedyBestFirst() {
        PriorityQueue<Cell> openSet = new PriorityQueue<>(
            Comparator.comparingInt(c -> heuristic(c, endCell)));
        Set<Cell> openSetContents = new HashSet<>();
        Set<Cell> closedSet = new HashSet<>();
        
        // Initialize
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                grid[row][col].previous = null;
            }
        }
        
        openSet.add(startCell);
        openSetContents.add(startCell);
        
        while (!openSet.isEmpty()) {
            Cell current = openSet.poll();
            openSetContents.remove(current);
            
            if (current == endCell) {
                reconstructPath();
                return true;
            }
            
            closedSet.add(current);
            
            if (current != startCell && current != endCell) {
                current.setType(Cell.CellType.VISITED);
                pause();
            }
            
            for (Cell neighbor : getNeighbors(current)) {
                if (closedSet.contains(neighbor) || 
                    openSetContents.contains(neighbor) || 
                    neighbor.getType() == Cell.CellType.WALL) {
                    continue;
                }
                
                neighbor.previous = current;
                
                if (neighbor == endCell) {
                    reconstructPath();
                    return true;
                }
                
                openSet.add(neighbor);
                openSetContents.add(neighbor);
                
                if (neighbor != endCell) {
                    neighbor.setType(Cell.CellType.CONSIDERING);
                    pause();
                }
            }
        }
        
        return false;
    }
    
    private boolean runBreadthFirst() {
        Queue<Cell> queue = new LinkedList<>();
        Set<Cell> visited = new HashSet<>();
        
        // Initialize
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                grid[row][col].previous = null;
            }
        }
        
        queue.add(startCell);
        visited.add(startCell);
        
        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            
            if (current == endCell) {
                reconstructPath();
                return true;
            }
            
            if (current != startCell && current != endCell) {
                current.setType(Cell.CellType.VISITED);
                pause();
            }
            
            for (Cell neighbor : getNeighbors(current)) {
                if (!visited.contains(neighbor) && neighbor.getType() != Cell.CellType.WALL) {
                    neighbor.previous = current;
                    queue.add(neighbor);
                    visited.add(neighbor);
                    
                    if (neighbor != endCell) {
                        neighbor.setType(Cell.CellType.CONSIDERING);
                        pause();
                    }
                }
            }
        }
        
        return false;
    }
    
    private int heuristic(Cell a, Cell b) {
        // Manhattan distance
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }
    
    private java.util.List<Cell> getNeighbors(Cell cell) {
        java.util.List<Cell> neighbors = new ArrayList<>();
        int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}  // Up, Down, Left, Right
        };
        
        for (int[] dir : directions) {
            int newRow = cell.row + dir[0];
            int newCol = cell.col + dir[1];
            
            if (newRow >= 0 && newRow < GRID_SIZE && newCol >= 0 && newCol < GRID_SIZE) {
                neighbors.add(grid[newRow][newCol]);
            }
        }
        
        return neighbors;
    }
    
    private void reconstructPath() {
        Cell current = endCell;
        while (current != null && current != startCell) {
            if (current != endCell) {
                current.setType(Cell.CellType.PATH);
                pause();
            }
            current = current.previous;
        }
    }
    
    private void pause() {
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void resetGrid() {
        clearPath();
        
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (grid[row][col].getType() == Cell.CellType.WALL) {
                    grid[row][col].setType(Cell.CellType.EMPTY);
                }
            }
        }
        
        statusLabel.setText("Grid reset. Select an algorithm and press Start");
        statusLabel.setForeground(Color.BLACK);
    }
    
    private void clearPath() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (grid[row][col].getType() != Cell.CellType.START && 
                    grid[row][col].getType() != Cell.CellType.END && 
                    grid[row][col].getType() != Cell.CellType.WALL) {
                    grid[row][col].setType(Cell.CellType.EMPTY);
                }
            }
        }
        
        statusLabel.setText("Path cleared. Select an algorithm and press Start");
        statusLabel.setForeground(Color.BLACK);
    }
    
    private class CellClickListener extends MouseAdapter {
        private Cell cell;
        
        public CellClickListener(Cell cell) {
            this.cell = cell;
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            if (isRunning) return;
            
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (cell == startCell) {
                    startCell.setType(Cell.CellType.EMPTY);
                    startCell = null;
                } else if (cell == endCell) {
                    endCell.setType(Cell.CellType.EMPTY);
                    endCell = null;
                } else if (startCell == null) {
                    startCell = cell;
                    cell.setType(Cell.CellType.START);
                } else if (endCell == null) {
                    endCell = cell;
                    cell.setType(Cell.CellType.END);
                } else {
                    // Toggle wall
                    if (cell.getType() == Cell.CellType.EMPTY) {
                        cell.setType(Cell.CellType.WALL);
                    } else if (cell.getType() == Cell.CellType.WALL) {
                        cell.setType(Cell.CellType.EMPTY);
                    }
                }
            } else if (SwingUtilities.isRightMouseButton(e)) {
                // Remove wall with right click
                if (cell.getType() == Cell.CellType.WALL) {
                    cell.setType(Cell.CellType.EMPTY);
                }
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PathFindingVisualizer());
    }
} 