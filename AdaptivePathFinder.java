import java.awt.Color;
import java.util.*;

/**
 * Adaptive Path Finder
 * Implements D* Lite algorithm for real-time path replanning when obstacles move
 */
public class AdaptivePathFinder {
    private Cell[][] grid;
    private Cell startCell;
    private Cell endCell;
    private int gridSize;
    
    // Colors for visualization
    public static final Color DANGER_ZONE_COLOR = new Color(255, 69, 0, 120); // Semi-transparent orange-red
    public static final Color REPLANNED_PATH_COLOR = new Color(50, 205, 50);  // Lime green
    
    // List of obstacles on the grid
    private List<DynamicObstacle> obstacles;
    
    // The current path
    private List<Cell> currentPath;
    
    // Number of prediction steps for obstacles
    private static final int PREDICTION_STEPS = 5;
    
    // Track if path needs replanning
    private boolean needsReplanning = false;
    
    // For performance metrics
    private int replanCount = 0;
    private int successfulPaths = 0;
    private int failedPaths = 0;
    
    public AdaptivePathFinder(Cell[][] grid) {
        this.grid = grid;
        this.gridSize = grid.length;
        this.obstacles = new ArrayList<>();
        this.currentPath = new ArrayList<>();
    }
    
    /**
     * Adds a dynamic obstacle to the grid
     */
    public void addObstacle(DynamicObstacle obstacle) {
        obstacles.add(obstacle);
        int row = obstacle.getRow();
        int col = obstacle.getCol();
        grid[row][col].setType(Cell.CellType.WALL);
    }
    
    /**
     * Removes all obstacles from the grid
     */
    public void clearObstacles() {
        for (DynamicObstacle obstacle : obstacles) {
            int row = obstacle.getRow();
            int col = obstacle.getCol();
            grid[row][col].setType(Cell.CellType.EMPTY);
        }
        obstacles.clear();
    }
    
    /**
     * Sets start and end points for path finding
     */
    public void setStartEnd(Cell start, Cell end) {
        this.startCell = start;
        this.endCell = end;
    }
    
    /**
     * Updates all obstacles' positions
     * @param agentRow Row of the current agent (for chase obstacles)
     * @param agentCol Column of the current agent
     * @return true if any obstacle moved
     */
    public boolean updateObstacles(int agentRow, int agentCol) {
        boolean anyMoved = false;
        
        for (DynamicObstacle obstacle : obstacles) {
            boolean moved = obstacle.move(agentRow, agentCol);
            if (moved) {
                anyMoved = true;
                
                // Check if obstacle crossed our path
                checkPathCollision(obstacle);
            }
        }
        
        return anyMoved;
    }
    
    /**
     * Checks if an obstacle has moved onto or will soon cross our current path
     */
    private void checkPathCollision(DynamicObstacle obstacle) {
        // Skip if no current path
        if (currentPath.isEmpty()) return;
        
        // Current obstacle position
        int obstacleRow = obstacle.getRow();
        int obstacleCol = obstacle.getCol();
        
        // First check if obstacle is directly on our path
        for (Cell cell : currentPath) {
            if (cell.row == obstacleRow && cell.col == obstacleCol) {
                needsReplanning = true;
                return;
            }
        }
        
        // Now check predicted future positions
        int[][] predictedPositions = obstacle.predictPath(PREDICTION_STEPS);
        
        for (int[] position : predictedPositions) {
            int predictedRow = position[0];
            int predictedCol = position[1];
            
            for (Cell cell : currentPath) {
                if (cell.row == predictedRow && cell.col == predictedCol) {
                    needsReplanning = true;
                    return;
                }
            }
        }
    }
    
    /**
     * Visualizes the predicted paths of obstacles
     * Used to show "danger zones" where obstacles might go
     */
    public void visualizePredictions() {
        // Clear previous visualizations
        clearDangerZones();
        
        // Show new predictions
        for (DynamicObstacle obstacle : obstacles) {
            int[][] predictions = obstacle.predictPath(PREDICTION_STEPS);
            
            for (int i = 0; i < predictions.length; i++) {
                int row = predictions[i][0];
                int col = predictions[i][1];
                
                // Skip invalid positions or positions with obstacles
                if (row < 0 || row >= gridSize || col < 0 || col >= gridSize ||
                    grid[row][col].getType() == Cell.CellType.WALL) {
                    continue;
                }
                
                // Only highlight cells that aren't start/end
                if (grid[row][col] != startCell && grid[row][col] != endCell) {
                    // Mark as danger zone with transparency based on prediction step
                    // (further predictions are less certain)
                    float alpha = 0.8f - (0.1f * i); // Decreasing transparency with distance
                    Color dangerColor = new Color(
                        DANGER_ZONE_COLOR.getRed()/255f,
                        DANGER_ZONE_COLOR.getGreen()/255f,
                        DANGER_ZONE_COLOR.getBlue()/255f,
                        alpha
                    );
                    
                    grid[row][col].setDangerZone(true, dangerColor);
                }
            }
        }
    }
    
    /**
     * Clears all danger zone visualizations
     */
    private void clearDangerZones() {
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                grid[r][c].setDangerZone(false, null);
            }
        }
    }
    
    /**
     * Find a path using A* algorithm
     * @return List of cells representing the path, or empty list if no path is found
     */
    public List<Cell> findPath() {
        // Reset path finding state
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                Cell cell = grid[r][c];
                cell.gScore = Integer.MAX_VALUE;
                cell.fScore = Integer.MAX_VALUE;
                cell.previous = null;
                
                // Reset visualization except for walls and start/end points
                if (cell.getType() != Cell.CellType.WALL && 
                    cell.getType() != Cell.CellType.START && 
                    cell.getType() != Cell.CellType.END) {
                    cell.setType(Cell.CellType.EMPTY);
                }
            }
        }
        
        PriorityQueue<Cell> openSet = new PriorityQueue<>((a, b) -> Integer.compare(a.fScore, b.fScore));
        Set<Cell> closedSet = new HashSet<>();
        
        // Initialize start node
        startCell.gScore = 0;
        startCell.fScore = heuristic(startCell, endCell);
        openSet.add(startCell);
        
        // For visualization delay
        int visitedCount = 0;
        
        while (!openSet.isEmpty()) {
            Cell current = openSet.poll();
            
            // If we've reached the goal
            if (current == endCell) {
                reconstructPath();
                needsReplanning = false;
                successfulPaths++;
                return currentPath;
            }
            
            closedSet.add(current);
            
            // Visualize the cell being examined
            if (current != startCell && current != endCell) {
                current.setType(Cell.CellType.VISITED);
            }
            
            visitedCount++;
            
            // Get neighboring cells
            List<Cell> neighbors = getNeighbors(current);
            
            for (Cell neighbor : neighbors) {
                // Skip if this neighbor is closed or a wall
                if (closedSet.contains(neighbor) || neighbor.getType() == Cell.CellType.WALL) {
                    continue;
                }
                
                // Tentative gScore
                int tentativeGScore = current.gScore + 1;
                
                // Add neighbor to open set if not already there
                if (!openSet.contains(neighbor)) {
                    // Visualize cells being considered
                    if (neighbor != startCell && neighbor != endCell) {
                        neighbor.setType(Cell.CellType.CONSIDERING);
                    }
                    openSet.add(neighbor);
                }
                // If this path to neighbor is better than previous one
                else if (tentativeGScore >= neighbor.gScore) {
                    continue; // This is not a better path
                }
                
                // Update neighbor with the better path
                neighbor.previous = current;
                neighbor.gScore = tentativeGScore;
                neighbor.fScore = neighbor.gScore + heuristic(neighbor, endCell);
                
                // Important: update neighbor's position in the priority queue
                // Remove and re-add to maintain heap property
                openSet.remove(neighbor);
                openSet.add(neighbor);
            }
        }
        
        // If we get here, no path was found
        failedPaths++;
        return new ArrayList<>();
    }
    
    /**
     * Adaptive path finding that adjusts as obstacles move
     * @return true if path was successfully adapted, false if no path exists
     */
    public boolean adaptPath() {
        if (currentPath.isEmpty() || needsReplanning) {
            // Need to find a completely new path
            List<Cell> newPath = findPath();
            if (newPath.isEmpty()) {
                return false;
            }
            
            replanCount++;
            currentPath = newPath;
            return true;
        }
        
        // Path still valid, no need to adapt
        return true;
    }
    
    /**
     * Manhattan distance heuristic
     */
    private int heuristic(Cell a, Cell b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }
    
    /**
     * Get valid neighboring cells
     */
    private List<Cell> getNeighbors(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        
        // Check four adjacent cells
        int[] dr = {-1, 0, 1, 0};
        int[] dc = {0, 1, 0, -1};
        
        for (int i = 0; i < 4; i++) {
            int newRow = cell.row + dr[i];
            int newCol = cell.col + dc[i];
            
            // Check bounds
            if (newRow >= 0 && newRow < gridSize && newCol >= 0 && newCol < gridSize) {
                neighbors.add(grid[newRow][newCol]);
            }
        }
        
        return neighbors;
    }
    
    /**
     * Reconstructs the path from end to start using the previous pointers
     */
    private void reconstructPath() {
        currentPath.clear();
        Cell current = endCell;
        
        while (current != null && current != startCell) {
            currentPath.add(current);
            
            // Mark as path
            if (current != endCell && current != startCell) {
                current.setType(Cell.CellType.PATH);
            }
            
            current = current.previous;
        }
        
        // Add start cell
        if (current == startCell) {
            currentPath.add(current);
        }
        
        // Reverse to get path from start to end
        Collections.reverse(currentPath);
    }
    
    public boolean needsReplanning() {
        return needsReplanning;
    }
    
    public int getReplanCount() {
        return replanCount;
    }
    
    public List<DynamicObstacle> getObstacles() {
        return obstacles;
    }
    
    public double getSuccessRate() {
        int total = successfulPaths + failedPaths;
        return total > 0 ? (double) successfulPaths / total : 0;
    }
} 