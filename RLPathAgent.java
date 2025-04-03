import java.util.*;
import java.awt.Color;

/**
 * Reinforcement Learning Agent for Path Finding
 * Implements Q-Learning algorithm for finding optimal paths
 */
public class RLPathAgent {
    // Q-learning parameters
    private double learningRate = 0.1;
    private double discountFactor = 0.9;
    private double explorationRate = 0.3;
    private int numEpisodes = 500;
    
    // Action space: up, right, down, left
    private static final int[] DR = {-1, 0, 1, 0};
    private static final int[] DC = {0, 1, 0, -1};
    private static final int NUM_ACTIONS = 4;
    
    // Colors for visualization
    public static final Color RL_AGENT_COLOR = new Color(128, 0, 128); // Purple
    public static final Color RL_PATH_COLOR = new Color(255, 165, 0);  // Orange
    public static final Color HEAT_MAP_HIGH = new Color(255, 0, 0, 180);
    public static final Color HEAT_MAP_LOW = new Color(0, 0, 255, 180);
    
    // Q-table: maps states to action values
    private Map<State, double[]> qTable;
    private Cell[][] grid;
    private int gridSize;
    
    // Training metrics
    private List<Integer> episodeSteps;
    private List<Double> episodeRewards;
    private List<Boolean> episodeSuccess;
    
    // Current training state
    private Cell currentCell;
    private Cell startCell;
    private Cell endCell;
    private boolean isTraining = false;
    private int currentEpisode = 0;
    private int currentStep = 0;
    
    public RLPathAgent(Cell[][] grid) {
        this.grid = grid;
        this.gridSize = grid.length;
        this.qTable = new HashMap<>();
        this.episodeSteps = new ArrayList<>();
        this.episodeRewards = new ArrayList<>();
        this.episodeSuccess = new ArrayList<>();
    }
    
    /**
     * Initialize a training session
     */
    public void startTraining(Cell startCell, Cell endCell) {
        this.startCell = startCell;
        this.endCell = endCell;
        this.isTraining = true;
        this.currentEpisode = 0;
        this.episodeSteps.clear();
        this.episodeRewards.clear();
        this.episodeSuccess.clear();
        
        // Initialize Q-table with small random values
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                if (grid[r][c].getType() != Cell.CellType.WALL) {
                    State state = new State(r, c);
                    qTable.put(state, new double[NUM_ACTIONS]);
                    for (int a = 0; a < NUM_ACTIONS; a++) {
                        qTable.get(state)[a] = Math.random() * 0.1;
                    }
                }
            }
        }
    }
    
    /**
     * Run a single training step
     * @return true if training is complete, false otherwise
     */
    public boolean trainStep() {
        if (!isTraining || currentEpisode >= numEpisodes) {
            isTraining = false;
            return true;
        }
        
        if (currentStep == 0) {
            // Reset for new episode
            resetEpisode();
        }
        
        // Current state
        State state = new State(currentCell.row, currentCell.col);
        
        // Choose action using epsilon-greedy policy
        int action = chooseAction(state);
        
        // Take action and observe new state
        int newRow = currentCell.row + DR[action];
        int newCol = currentCell.col + DC[action];
        
        double reward = -0.1; // Small negative reward for each step
        boolean done = false;
        
        // Check if new state is valid
        if (newRow < 0 || newRow >= gridSize || newCol < 0 || newCol >= gridSize 
                || grid[newRow][newCol].getType() == Cell.CellType.WALL) {
            reward = -1.0; // Penalty for hitting walls or boundaries
            // Stay in the same state
            newRow = currentCell.row;
            newCol = currentCell.col;
        } else {
            // Update current cell
            currentCell = grid[newRow][newCol];
            
            // Special rewards
            if (currentCell == endCell) {
                reward = 10.0; // Big reward for reaching the goal
                done = true;
                episodeSuccess.add(true);
            }
            
            // Visualize agent's current position if not at goal
            if (!done) {
                if (currentCell != startCell && currentCell != endCell) {
                    currentCell.setType(Cell.CellType.VISITED);
                }
            }
        }
        
        // Get new state
        State newState = new State(newRow, newCol);
        
        // Update Q-value using Q-learning formula
        double[] qValues = qTable.get(state);
        double[] newQValues = qTable.getOrDefault(newState, new double[NUM_ACTIONS]);
        
        double maxNewQ = Arrays.stream(newQValues).max().orElse(0);
        qValues[action] = qValues[action] + learningRate * (reward + discountFactor * maxNewQ - qValues[action]);
        
        // Update episode stats
        currentStep++;
        
        // Check if episode is done
        if (done || currentStep > gridSize * gridSize * 2) {
            // Episode exceeded maximum steps
            if (!done) {
                episodeSuccess.add(false);
            }
            
            // Record episode metrics
            episodeSteps.add(currentStep);
            
            // Prepare for next episode
            currentEpisode++;
            currentStep = 0;
            
            // Gradually reduce exploration rate
            explorationRate = Math.max(0.1, explorationRate * 0.99);
            
            // Reset grid for next episode
            return false;
        }
        
        return false;
    }
    
    /**
     * Reset environment for a new episode
     */
    private void resetEpisode() {
        currentCell = startCell;
        currentStep = 0;
    }
    
    /**
     * Choose an action using epsilon-greedy policy
     */
    private int chooseAction(State state) {
        // Exploration
        if (Math.random() < explorationRate) {
            return (int) (Math.random() * NUM_ACTIONS);
        }
        
        // Exploitation - choose best action from Q-table
        double[] qValues = qTable.getOrDefault(state, new double[NUM_ACTIONS]);
        int bestAction = 0;
        double bestValue = qValues[0];
        
        for (int i = 1; i < NUM_ACTIONS; i++) {
            if (qValues[i] > bestValue) {
                bestValue = qValues[i];
                bestAction = i;
            }
        }
        
        return bestAction;
    }
    
    /**
     * Find the best path from start to end using the learned policy
     * @return List of cells representing the path
     */
    public List<Cell> findPath() {
        List<Cell> path = new ArrayList<>();
        Cell current = startCell;
        path.add(current);
        
        // Maximum path length to prevent infinite loops
        int maxSteps = gridSize * gridSize;
        int steps = 0;
        
        while (current != endCell && steps < maxSteps) {
            State state = new State(current.row, current.col);
            double[] qValues = qTable.getOrDefault(state, new double[NUM_ACTIONS]);
            
            // Find best action
            int bestAction = 0;
            double bestValue = qValues[0];
            
            for (int i = 1; i < NUM_ACTIONS; i++) {
                if (qValues[i] > bestValue) {
                    bestValue = qValues[i];
                    bestAction = i;
                }
            }
            
            // Take best action
            int newRow = current.row + DR[bestAction];
            int newCol = current.col + DC[bestAction];
            
            // Check if new position is valid
            if (newRow < 0 || newRow >= gridSize || newCol < 0 || newCol >= gridSize 
                    || grid[newRow][newCol].getType() == Cell.CellType.WALL) {
                // If invalid, end path
                break;
            }
            
            current = grid[newRow][newCol];
            path.add(current);
            steps++;
        }
        
        return path;
    }
    
    /**
     * Visualizes the heat map of Q-values on the grid
     * Shows which areas the agent prefers to go through
     */
    public void visualizeHeatMap() {
        // Find max Q-value for normalization
        double maxQ = 0;
        for (double[] values : qTable.values()) {
            for (double v : values) {
                maxQ = Math.max(maxQ, v);
            }
        }
        
        // Skip if no meaningful Q-values learned
        if (maxQ <= 0.1) return;
        
        // Visualize the highest value for each cell
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                Cell cell = grid[r][c];
                if (cell.getType() != Cell.CellType.WALL && 
                    cell.getType() != Cell.CellType.START && 
                    cell.getType() != Cell.CellType.END) {
                    
                    State state = new State(r, c);
                    double[] qValues = qTable.getOrDefault(state, new double[NUM_ACTIONS]);
                    double maxValue = Arrays.stream(qValues).max().orElse(0);
                    
                    // Normalize and color-code
                    double normalizedValue = maxValue / maxQ;
                    if (normalizedValue > 0.1) { // Only show significant values
                        cell.setQValue(normalizedValue);
                    }
                }
            }
        }
    }
    
    public boolean isTraining() {
        return isTraining;
    }
    
    public double getSuccessRate() {
        if (episodeSuccess.isEmpty()) return 0;
        return episodeSuccess.stream().filter(Boolean::booleanValue).count() / (double) episodeSuccess.size();
    }
    
    public double getAverageSteps() {
        if (episodeSteps.isEmpty()) return 0;
        return episodeSteps.stream().mapToInt(Integer::intValue).average().orElse(0);
    }
    
    public int getCurrentEpisode() {
        return currentEpisode;
    }
    
    public int getNumEpisodes() {
        return numEpisodes;
    }
    
    public void setNumEpisodes(int numEpisodes) {
        this.numEpisodes = numEpisodes;
    }
    
    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }
    
    public void setDiscountFactor(double discountFactor) {
        this.discountFactor = discountFactor;
    }
    
    public void setExplorationRate(double explorationRate) {
        this.explorationRate = explorationRate;
    }
    
    /**
     * Represents a state in the environment
     */
    private static class State {
        private final int row;
        private final int col;
        
        public State(int row, int col) {
            this.row = row;
            this.col = col;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return row == state.row && col == state.col;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }
} 