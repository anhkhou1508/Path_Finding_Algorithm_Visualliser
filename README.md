# Path Finding Algorithm Visualizer

A Java application that visualizes various path finding algorithms on a grid, allowing you to compare their behaviors and performance.

## Algorithms Implemented

1. **Dijkstra's Algorithm** - Guarantees the shortest path by exploring nodes in order of their distance from the start.
2. **A* Algorithm** - Uses a heuristic function to prioritize paths that seem to lead closer to the goal, often faster than Dijkstra's.
3. **Greedy Best-First Search** - Always explores the node that appears closest to the goal based on heuristic alone.
4. **Breadth-First Search** - Explores all neighbors at the present depth before moving to nodes at the next depth level.

## Features

- Interactive grid where you can place/remove the start point, end point, and walls
- Visual representation of the algorithm's progress:
  - Green: Start point
  - Red: End point
  - Black: Wall (obstacle)
  - Cyan: Visited node
  - Light Gray: Node being considered
  - Yellow: Final path

## How to Use

1. Run the application
2. Set up your grid:
   - The default grid has a start (green) and end (red) point
   - Left-click on an empty cell to place a wall
   - Left-click on a wall to remove it
   - Right-click on a wall to remove it
   - Left-click on start/end to remove them, then click on empty cells to place them again
3. Select an algorithm from the dropdown menu
4. Click "Start" to visualize the algorithm
5. Use "Clear Path" to remove the visualization but keep the walls
6. Use "Reset Grid" to clear everything and start over

## Building and Running

Compile the Java files:
```
javac PathFindingVisualizer.java Cell.java
```

Run the application:
```
java PathFindingVisualizer
```

## Requirements

- Java JDK 8 or higher
- Swing (included in JDK) 