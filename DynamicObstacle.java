import java.awt.Color;
import java.util.Random;

/**
 * Represents a moving obstacle on the grid
 * Can follow different movement patterns
 */
public class DynamicObstacle {
    // Movement patterns
    public enum MovementPattern {
        LINEAR,      // Move in a straight line until hitting a wall, then bounce
        RANDOM,      // Move randomly
        PATROL,      // Move back and forth along a path
        CHASE        // Try to move towards the agent
    }
    
    // Movement directions: up, right, down, left
    private static final int[] DR = {-1, 0, 1, 0};
    private static final int[] DC = {0, 1, 0, -1};
    
    // Obstacle properties
    private int row;
    private int col;
    private int direction; // 0: up, 1: right, 2: down, 3: left
    private MovementPattern pattern;
    private int speed; // Steps to take per move (1 is normal speed)
    private int moveCounter = 0;
    private Cell[][] grid;
    private int gridSize;
    private Random random;
    
    // For patrol pattern
    private int patrolStart;
    private int patrolEnd;
    private boolean patrolHorizontal;
    
    // Track previous positions for trail visualization
    private int[][] previousPositions;
    private int trailLength = 5;
    
    // Colors
    public static final Color OBSTACLE_COLOR = new Color(139, 0, 139); // Dark magenta
    public static final Color PREDICTED_PATH_COLOR = new Color(255, 105, 180, 128); // Semi-transparent hot pink
    public static final Color TRAIL_COLOR = new Color(218, 112, 214, 100); // Semi-transparent orchid

    public DynamicObstacle(int row, int col, MovementPattern pattern, Cell[][] grid) {
        this.row = row;
        this.col = col;
        this.pattern = pattern;
        this.grid = grid;
        this.gridSize = grid.length;
        this.speed = 1;
        this.random = new Random();
        
        // Initialize direction randomly
        this.direction = random.nextInt(4);
        
        // For patrol, default to horizontal patrol spanning 5 cells
        this.patrolHorizontal = true;
        this.patrolStart = Math.max(0, col - 2);
        this.patrolEnd = Math.min(gridSize - 1, col + 2);
        
        // Initialize trail tracking
        previousPositions = new int[trailLength][2];
        for (int i = 0; i < trailLength; i++) {
            previousPositions[i][0] = row;
            previousPositions[i][1] = col;
        }
    }
    
    /**
     * Sets up a patrol path for the obstacle
     */
    public void setPatrolPath(int start, int end, boolean horizontal) {
        if (start >= 0 && end < gridSize && start <= end) {
            this.patrolHorizontal = horizontal;
            this.patrolStart = start;
            this.patrolEnd = end;
            // Reset direction based on patrol orientation
            if (horizontal) {
                direction = 1; // Right
            } else {
                direction = 2; // Down
            }
        }
    }
    
    /**
     * Updates the obstacle's position based on its movement pattern
     * @param agentRow Current row of the agent (for chase pattern)
     * @param agentCol Current col of the agent (for chase pattern)
     * @return true if the obstacle moved, false otherwise
     */
    public boolean move(int agentRow, int agentCol) {
        moveCounter++;
        
        // Only move when counter matches speed (allows for slower-moving obstacles)
        if (moveCounter < speed) {
            return false;
        }
        
        moveCounter = 0;
        
        // Update trail - shift previous positions
        for (int i = trailLength - 1; i > 0; i--) {
            previousPositions[i][0] = previousPositions[i-1][0];
            previousPositions[i][1] = previousPositions[i-1][1];
        }
        previousPositions[0][0] = row;
        previousPositions[0][1] = col;
        
        // Clear previous trail visualization
        clearTrail();
        
        int newRow = row;
        int newCol = col;
        
        // Determine next position based on movement pattern
        switch (pattern) {
            case LINEAR:
                newRow = row + DR[direction];
                newCol = col + DC[direction];
                
                // If will hit a wall or another obstacle, bounce in the opposite direction
                if (!isValidMove(newRow, newCol)) {
                    direction = (direction + 2) % 4; // Reverse direction
                    newRow = row + DR[direction];
                    newCol = col + DC[direction];
                }
                break;
                
            case RANDOM:
                // Occasionally change direction
                if (random.nextDouble() < 0.2) {
                    direction = random.nextInt(4);
                }
                
                newRow = row + DR[direction];
                newCol = col + DC[direction];
                
                // If can't move that way, try a different random direction
                if (!isValidMove(newRow, newCol)) {
                    int attempts = 0;
                    while (!isValidMove(newRow, newCol) && attempts < 4) {
                        direction = random.nextInt(4);
                        newRow = row + DR[direction];
                        newCol = col + DC[direction];
                        attempts++;
                    }
                    
                    // If all directions blocked, stay put
                    if (!isValidMove(newRow, newCol)) {
                        newRow = row;
                        newCol = col;
                    }
                }
                break;
                
            case PATROL:
                if (patrolHorizontal) {
                    // Moving right
                    if (direction == 1) {
                        newCol = col + 1;
                        if (newCol > patrolEnd || !isValidMove(newRow, newCol)) {
                            direction = 3; // Switch to left
                            newCol = col - 1;
                        }
                    } 
                    // Moving left
                    else {
                        newCol = col - 1;
                        if (newCol < patrolStart || !isValidMove(newRow, newCol)) {
                            direction = 1; // Switch to right
                            newCol = col + 1;
                        }
                    }
                } else {
                    // Moving down
                    if (direction == 2) {
                        newRow = row + 1;
                        if (newRow > patrolEnd || !isValidMove(newRow, newCol)) {
                            direction = 0; // Switch to up
                            newRow = row - 1;
                        }
                    } 
                    // Moving up
                    else {
                        newRow = row - 1;
                        if (newRow < patrolStart || !isValidMove(newRow, newCol)) {
                            direction = 2; // Switch to down
                            newRow = row + 1;
                        }
                    }
                }
                break;
                
            case CHASE:
                // Simple chase: try to move in the direction of the agent
                int verticalDiff = agentRow - row;
                int horizontalDiff = agentCol - col;
                
                // Decide whether to move vertically or horizontally
                if (Math.abs(verticalDiff) > Math.abs(horizontalDiff)) {
                    // Move vertically
                    if (verticalDiff > 0) {
                        direction = 2; // Down
                    } else {
                        direction = 0; // Up
                    }
                } else {
                    // Move horizontally
                    if (horizontalDiff > 0) {
                        direction = 1; // Right
                    } else {
                        direction = 3; // Left
                    }
                }
                
                newRow = row + DR[direction];
                newCol = col + DC[direction];
                
                // If can't move in preferred direction, try others
                if (!isValidMove(newRow, newCol)) {
                    // Try other directions in order of preference
                    for (int i = 0; i < 4; i++) {
                        if (i != direction) {
                            int testRow = row + DR[i];
                            int testCol = col + DC[i];
                            if (isValidMove(testRow, testCol)) {
                                newRow = testRow;
                                newCol = testCol;
                                direction = i;
                                break;
                            }
                        }
                    }
                    
                    // If all directions blocked, stay put
                    if (!isValidMove(newRow, newCol)) {
                        newRow = row;
                        newCol = col;
                    }
                }
                break;
        }
        
        // If we actually moved, update position and mark it
        if (newRow != row || newCol != col) {
            // Clear old position
            if (grid[row][col].getType() == Cell.CellType.WALL) {
                grid[row][col].setType(Cell.CellType.EMPTY);
            }
            
            // Update position
            row = newRow;
            col = newCol;
            
            // Mark new position
            grid[row][col].setType(Cell.CellType.WALL);
            
            // Visualize trail
            visualizeTrail();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if a move is valid (within bounds and not a wall)
     */
    private boolean isValidMove(int newRow, int newCol) {
        if (newRow < 0 || newRow >= gridSize || newCol < 0 || newCol >= gridSize) {
            return false;
        }
        
        // Don't move into walls, start, or end positions
        Cell.CellType cellType = grid[newRow][newCol].getType();
        return cellType == Cell.CellType.EMPTY || 
               cellType == Cell.CellType.VISITED || 
               cellType == Cell.CellType.CONSIDERING || 
               cellType == Cell.CellType.PATH;
    }
    
    /**
     * Visualizes the recent movement trail of the obstacle
     */
    private void visualizeTrail() {
        for (int i = 1; i < trailLength; i++) {
            int trailRow = previousPositions[i][0];
            int trailCol = previousPositions[i][1];
            
            // Don't draw trail on the current position or walls
            if ((trailRow != row || trailCol != col) && 
                trailRow >= 0 && trailRow < gridSize && 
                trailCol >= 0 && trailCol < gridSize &&
                grid[trailRow][trailCol].getType() != Cell.CellType.WALL) {
                
                // Fading transparency based on position in trail
                float alpha = 0.7f - (0.1f * i);
                Color trailColor = new Color(
                    TRAIL_COLOR.getRed()/255f,
                    TRAIL_COLOR.getGreen()/255f,
                    TRAIL_COLOR.getBlue()/255f,
                    alpha
                );
                
                // Mark cell with trail color
                grid[trailRow][trailCol].setTrail(true, trailColor);
            }
        }
    }
    
    /**
     * Clears the trail visualization
     */
    private void clearTrail() {
        for (int i = 0; i < trailLength; i++) {
            int trailRow = previousPositions[i][0];
            int trailCol = previousPositions[i][1];
            
            if (trailRow >= 0 && trailRow < gridSize && 
                trailCol >= 0 && trailCol < gridSize) {
                grid[trailRow][trailCol].setTrail(false, null);
            }
        }
    }
    
    /**
     * Predicts the next N positions of the obstacle
     * @param steps Number of steps to predict
     * @return Array of predicted [row, col] positions
     */
    public int[][] predictPath(int steps) {
        int[][] predictions = new int[steps][2];
        int predictedRow = row;
        int predictedCol = col;
        int predictedDir = direction;
        
        // Clone relevant state for prediction
        int predMoveCounter = moveCounter;
        
        for (int i = 0; i < steps; i++) {
            // Simple linear prediction for most patterns
            if (pattern != MovementPattern.RANDOM && pattern != MovementPattern.CHASE) {
                predMoveCounter++;
                
                if (predMoveCounter >= speed) {
                    predMoveCounter = 0;
                    
                    if (pattern == MovementPattern.LINEAR) {
                        // Check if would hit wall and bounce
                        int nextRow = predictedRow + DR[predictedDir];
                        int nextCol = predictedCol + DC[predictedDir];
                        
                        if (nextRow < 0 || nextRow >= gridSize || 
                            nextCol < 0 || nextCol >= gridSize ||
                            grid[nextRow][nextCol].getType() == Cell.CellType.WALL) {
                            
                            predictedDir = (predictedDir + 2) % 4; // Reverse direction
                        }
                        
                        predictedRow += DR[predictedDir];
                        predictedCol += DC[predictedDir];
                    } 
                    else if (pattern == MovementPattern.PATROL) {
                        if (patrolHorizontal) {
                            if (predictedDir == 1) { // Right
                                predictedCol++;
                                if (predictedCol > patrolEnd) {
                                    predictedDir = 3; // Switch to left
                                    predictedCol = patrolEnd - 1;
                                }
                            } else { // Left
                                predictedCol--;
                                if (predictedCol < patrolStart) {
                                    predictedDir = 1; // Switch to right
                                    predictedCol = patrolStart + 1;
                                }
                            }
                        } else {
                            if (predictedDir == 2) { // Down
                                predictedRow++;
                                if (predictedRow > patrolEnd) {
                                    predictedDir = 0; // Switch to up
                                    predictedRow = patrolEnd - 1;
                                }
                            } else { // Up
                                predictedRow--;
                                if (predictedRow < patrolStart) {
                                    predictedDir = 2; // Switch to down
                                    predictedRow = patrolStart + 1;
                                }
                            }
                        }
                    }
                }
            } else {
                // For random and chase patterns, simplistic prediction - continue in current direction
                // with uncertainty increasing with steps
                if (i < 3) { // First few steps more predictable
                    predictedRow += DR[predictedDir];
                    predictedCol += DC[predictedDir];
                    
                    // Ensure prediction stays in bounds
                    predictedRow = Math.max(0, Math.min(gridSize - 1, predictedRow));
                    predictedCol = Math.max(0, Math.min(gridSize - 1, predictedCol));
                } else {
                    // After a few steps, prediction becomes uncertain
                    // Just indicate general area rather than exact position
                }
            }
            
            predictions[i][0] = predictedRow;
            predictions[i][1] = predictedCol;
        }
        
        return predictions;
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
    
    public void setSpeed(int speed) {
        this.speed = Math.max(1, speed);
    }
    
    public int getSpeed() {
        return speed;
    }
    
    public void setTrailLength(int length) {
        this.trailLength = Math.max(1, Math.min(10, length));
        // Resize trail array if needed
        if (previousPositions.length != trailLength) {
            int[][] newTrail = new int[trailLength][2];
            for (int i = 0; i < Math.min(trailLength, previousPositions.length); i++) {
                newTrail[i][0] = previousPositions[i][0];
                newTrail[i][1] = previousPositions[i][1];
            }
            // Fill any new positions
            for (int i = previousPositions.length; i < trailLength; i++) {
                newTrail[i][0] = row;
                newTrail[i][1] = col;
            }
            previousPositions = newTrail;
        }
    }
    
    public MovementPattern getPattern() {
        return pattern;
    }
    
    public void setPattern(MovementPattern pattern) {
        this.pattern = pattern;
    }
} 