import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Enhanced version of PathFindingVisualizer with Machine Learning features:
 * 1. Reinforcement Learning Agent for path finding
 * 2. Real-time path adaptation with dynamic obstacles
 */
public class PathFindingVisualizerML extends JFrame {
    private static final int GRID_SIZE = 20;
    private static final int CELL_SIZE = 30;
    
    // Custom colors
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color TITLE_COLOR = new Color(50, 50, 50);
    private static final Color PANEL_BACKGROUND = new Color(250, 250, 250);
    private static final Color BUTTON_BACKGROUND = new Color(70, 130, 180); // Steel Blue
    
    private Cell[][] grid;
    private Cell startCell;
    private Cell endCell;
    private JPanel gridPanel;
    
    // ML Components
    private RLPathAgent rlAgent;
    private AdaptivePathFinder adaptivePathFinder;
    
    // UI components
    private JTabbedPane tabbedPane;
    private JPanel rlPanel;
    private JPanel dynamicObstaclesPanel;
    
    // RL controls
    private JButton trainRLButton;
    private JButton visualizeRLButton;
    private JLabel rlStatusLabel;
    private JProgressBar trainingProgressBar;
    private javax.swing.Timer rlTrainingTimer;
    private boolean isRLTraining = false;
    
    // Dynamic obstacles controls
    private JButton addObstacleButton;
    private JButton startAdaptiveButton;
    private JButton stopAdaptiveButton;
    private JComboBox<String> obstacleTypeSelector;
    private JCheckBox showPredictionsCheckbox;
    private JSlider speedSlider;
    private javax.swing.Timer adaptivePathTimer;
    private boolean isAdaptiveModeRunning = false;
    
    public PathFindingVisualizerML() {
        setTitle("Path Finding Algorithm Visualizer - ML Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        createTitlePanel();
        initializeGrid();
        initializeMLComponents();
        createTabbedInterface();
        
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_COLOR);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("Path Finding Algorithm Visualizer - ML Edition");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(TITLE_COLOR);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JLabel subtitleLabel = new JLabel("Reinforcement Learning & Dynamic Obstacles");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setHorizontalAlignment(JLabel.CENTER);
        
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
                
                // Add mouse listeners for wall placement
                final int r = row;
                final int c = col;
                grid[row][col].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (grid[r][c] != startCell && grid[r][c] != endCell) {
                            // Toggle wall
                            if (grid[r][c].getType() == Cell.CellType.EMPTY) {
                                grid[r][c].setType(Cell.CellType.WALL);
                            } else if (grid[r][c].getType() == Cell.CellType.WALL) {
                                grid[r][c].setType(Cell.CellType.EMPTY);
                            }
                        }
                    }
                });
            }
        }
        
        // Default start and end positions
        startCell = grid[2][2];
        startCell.setType(Cell.CellType.START);
        
        endCell = grid[GRID_SIZE - 3][GRID_SIZE - 3];
        endCell.setType(Cell.CellType.END);
        
        JPanel gridContainer = new JPanel(new BorderLayout());
        gridContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        gridContainer.add(gridPanel, BorderLayout.CENTER);
        
        add(gridContainer, BorderLayout.CENTER);
    }
    
    private void initializeMLComponents() {
        // Initialize RL agent
        rlAgent = new RLPathAgent(grid);
        
        // Initialize adaptive pathfinder
        adaptivePathFinder = new AdaptivePathFinder(grid);
        adaptivePathFinder.setStartEnd(startCell, endCell);
        
        // Create timer for RL training
        rlTrainingTimer = new javax.swing.Timer(50, e -> {
            if (isRLTraining) {
                boolean done = rlAgent.trainStep();
                trainingProgressBar.setValue(rlAgent.getCurrentEpisode());
                
                rlStatusLabel.setText(String.format(
                    "Training: Episode %d/%d (%.1f%% complete)",
                    rlAgent.getCurrentEpisode(),
                    rlAgent.getNumEpisodes(),
                    (rlAgent.getCurrentEpisode() / (float)rlAgent.getNumEpisodes()) * 100
                ));
                
                if (done) {
                    stopTraining();
                }
            }
        });
        
        // Create timer for adaptive pathfinding - slower default speed (300ms)
        adaptivePathTimer = new javax.swing.Timer(300, e -> {
            if (isAdaptiveModeRunning) {
                boolean obstaclesMoved = adaptivePathFinder.updateObstacles(startCell.row, startCell.col);
                
                if (showPredictionsCheckbox.isSelected()) {
                    adaptivePathFinder.visualizePredictions();
                }
                
                if (obstaclesMoved || adaptivePathFinder.needsReplanning()) {
                    adaptivePathFinder.adaptPath();
                }
            }
        });
    }
    
    private void createTabbedInterface() {
        tabbedPane = new JTabbedPane();
        
        // Create the RL panel
        rlPanel = createRLPanel();
        
        // Create the dynamic obstacles panel
        dynamicObstaclesPanel = createDynamicObstaclesPanel();
        
        // Add tabs
        tabbedPane.addTab("Reinforcement Learning", rlPanel);
        tabbedPane.addTab("Dynamic Obstacles", dynamicObstaclesPanel);
        
        add(tabbedPane, BorderLayout.SOUTH);
    }
    
    private JPanel createRLPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Training controls
        trainRLButton = new JButton("Train RL Agent");
        trainRLButton.addActionListener(e -> startTraining());
        
        visualizeRLButton = new JButton("Show Learned Path");
        visualizeRLButton.setEnabled(false);
        visualizeRLButton.addActionListener(e -> visualizePath());
        
        trainingProgressBar = new JProgressBar(0, 500);
        trainingProgressBar.setStringPainted(true);
        
        rlStatusLabel = new JLabel("Click 'Train RL Agent' to start training");
        rlStatusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        
        // Add components
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        panel.add(trainRLButton, gbc);
        
        gbc.gridx = 1;
        panel.add(visualizeRLButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(trainingProgressBar, gbc);
        
        gbc.gridy = 2;
        panel.add(rlStatusLabel, gbc);
        
        return panel;
    }
    
    private JPanel createDynamicObstaclesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Obstacle controls
        obstacleTypeSelector = new JComboBox<>(new String[] {
            "Linear", "Random", "Patrol", "Chase"
        });
        
        addObstacleButton = new JButton("Add Obstacle");
        addObstacleButton.addActionListener(e -> addObstacle());
        
        showPredictionsCheckbox = new JCheckBox("Show Predictions");
        showPredictionsCheckbox.setSelected(true);
        
        // Speed slider
        JLabel speedLabel = new JLabel("Animation Speed:");
        speedSlider = new JSlider(JSlider.HORIZONTAL, 100, 1000, 300);
        speedSlider.setInverted(true); // Lower value = faster
        speedSlider.setMajorTickSpacing(300);
        speedSlider.setMinorTickSpacing(100);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        labels.put(100, new JLabel("Fast"));
        labels.put(500, new JLabel("Medium"));
        labels.put(1000, new JLabel("Slow"));
        speedSlider.setLabelTable(labels);
        
        speedSlider.addChangeListener(e -> {
            if (!speedSlider.getValueIsAdjusting()) {
                adaptivePathTimer.setDelay(speedSlider.getValue());
                // Restart timer if running to apply new delay
                if (isAdaptiveModeRunning) {
                    adaptivePathTimer.restart();
                }
            }
        });
        
        startAdaptiveButton = new JButton("Start Adaptive Mode");
        startAdaptiveButton.addActionListener(e -> startAdaptiveMode());
        
        stopAdaptiveButton = new JButton("Stop");
        stopAdaptiveButton.setEnabled(false);
        stopAdaptiveButton.addActionListener(e -> stopAdaptiveMode());
        
        // Add components
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.6;
        panel.add(obstacleTypeSelector, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        panel.add(addObstacleButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(showPredictionsCheckbox, gbc);
        
        // Add speed controls
        gbc.gridy = 2;
        panel.add(speedLabel, gbc);
        
        gbc.gridy = 3;
        panel.add(speedSlider, gbc);
        
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        panel.add(startAdaptiveButton, gbc);
        
        gbc.gridx = 1;
        panel.add(stopAdaptiveButton, gbc);
        
        return panel;
    }
    
    // RL methods
    private void startTraining() {
        if (isRLTraining) return;
        
        // Clear grid visualization while keeping walls
        clearGrid(false);
        
        try {
            // Set up the agent
            rlAgent.setNumEpisodes(500); // Default to 500 episodes
            rlAgent.startTraining(startCell, endCell);
            
            // Update UI
            trainingProgressBar.setMaximum(rlAgent.getNumEpisodes());
            trainingProgressBar.setValue(0);
            trainRLButton.setEnabled(false);
            visualizeRLButton.setEnabled(false);
            isRLTraining = true;
            
            // Start training
            rlTrainingTimer.start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error starting training: " + e.getMessage(),
                "Training Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            stopTraining(); // Cleanup in case of error
        }
    }
    
    private void stopTraining() {
        rlTrainingTimer.stop();
        isRLTraining = false;
        trainRLButton.setEnabled(true);
        visualizeRLButton.setEnabled(true);
        rlStatusLabel.setText("Training complete! Click 'Show Learned Path' to visualize.");
        
        // Clean up visualization
        clearGrid(false);
    }
    
    private void visualizePath() {
        // Clear previous visualization
        clearGrid(false);
        
        // Show the heat map of Q-values
        rlAgent.visualizeHeatMap();
        
        // Visualize the learned path
        java.util.List<Cell> path = rlAgent.findPath();
        
        if (path.size() > 0) {
            for (int i = 1; i < path.size() - 1; i++) {  // Skip start and end
                Cell cell = path.get(i);
                cell.setType(Cell.CellType.PATH);
            }
            
            rlStatusLabel.setText("Path found! Length: " + path.size());
        } else {
            rlStatusLabel.setText("No path found. Try training more.");
        }
    }
    
    // Dynamic obstacle methods
    private void addObstacle() {
        if (isAdaptiveModeRunning) {
            stopAdaptiveMode();
        }
        
        // Find a free spot for the obstacle
        Random random = new Random();
        int row, col;
        boolean valid = false;
        
        // Try up to 100 times to find a valid position
        for (int attempts = 0; attempts < 100; attempts++) {
            row = random.nextInt(GRID_SIZE);
            col = random.nextInt(GRID_SIZE);
            
            if (grid[row][col].getType() == Cell.CellType.EMPTY) {
                // Create the obstacle
                String typeStr = (String) obstacleTypeSelector.getSelectedItem();
                DynamicObstacle.MovementPattern pattern;
                
                switch (typeStr) {
                    case "Linear":
                        pattern = DynamicObstacle.MovementPattern.LINEAR;
                        break;
                    case "Random":
                        pattern = DynamicObstacle.MovementPattern.RANDOM;
                        break;
                    case "Patrol":
                        pattern = DynamicObstacle.MovementPattern.PATROL;
                        break;
                    case "Chase":
                        pattern = DynamicObstacle.MovementPattern.CHASE;
                        break;
                    default:
                        pattern = DynamicObstacle.MovementPattern.LINEAR;
                }
                
                DynamicObstacle obstacle = new DynamicObstacle(row, col, pattern, grid);
                adaptivePathFinder.addObstacle(obstacle);
                
                // Mark on grid
                grid[row][col].setType(Cell.CellType.WALL);
                valid = true;
                break;
            }
        }
        
        if (!valid) {
            JOptionPane.showMessageDialog(this, 
                "Could not find a valid position for the obstacle.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void startAdaptiveMode() {
        if (isAdaptiveModeRunning) return;
        
        // Clear path visualization
        clearGrid(false);
        
        // Update pathfinder with current start/end
        adaptivePathFinder.setStartEnd(startCell, endCell);
        
        // Check if we have obstacles
        if (adaptivePathFinder.getObstacles().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please add some obstacles first.",
                "No Obstacles", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Find initial path
        boolean pathFound = adaptivePathFinder.adaptPath();
        
        if (!pathFound) {
            JOptionPane.showMessageDialog(this, 
                "Could not find an initial path. Try removing some obstacles.",
                "No Path Found", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Start adaptive mode
        isAdaptiveModeRunning = true;
        startAdaptiveButton.setEnabled(false);
        stopAdaptiveButton.setEnabled(true);
        addObstacleButton.setEnabled(false);
        
        adaptivePathTimer.start();
    }
    
    private void stopAdaptiveMode() {
        if (!isAdaptiveModeRunning) return;
        
        isAdaptiveModeRunning = false;
        adaptivePathTimer.stop();
        
        // Clear danger zone visualization
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                grid[r][c].setDangerZone(false, null);
            }
        }
        
        startAdaptiveButton.setEnabled(true);
        stopAdaptiveButton.setEnabled(false);
        addObstacleButton.setEnabled(true);
    }
    
    // Utility methods
    private void clearGrid(boolean includeWalls) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Cell cell = grid[row][col];
                
                if (cell != startCell && cell != endCell) {
                    if (includeWalls || cell.getType() != Cell.CellType.WALL) {
                        cell.setType(Cell.CellType.EMPTY);
                    }
                    
                    // Clear any Q-value visualization
                    cell.clearQValue();
                    
                    // Clear any danger zone visualization
                    cell.setDangerZone(false, null);
                }
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PathFindingVisualizerML());
    }
} 