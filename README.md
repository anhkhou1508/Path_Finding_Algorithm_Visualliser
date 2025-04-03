# Path Finding Algorithm Visualizer with Machine Learning

A Java application that visualizes pathfinding algorithms on a grid, enhanced with machine learning and adaptive techniques. This project demonstrates both traditional algorithms and advanced AI approaches to pathfinding problems.

## Features Overview

### Traditional Algorithms
- **Dijkstra's Algorithm** - Guarantees the shortest path by exploring nodes in order of their distance from the start.
- **A* Algorithm** - Uses a heuristic function to prioritize paths that seem to lead closer to the goal, often faster than Dijkstra's.
- **Greedy Best-First Search** - Always explores the node that appears closest to the goal based on heuristic alone.
- **Breadth-First Search** - Explores all neighbors at the present depth before moving to nodes at the next depth level.

### Machine Learning and AI Features
- **Reinforcement Learning Agent (Q-learning)** - Learns optimal paths through trial and error without prior knowledge of the environment.
- **Dynamic Obstacle Management** - Handles moving obstacles with different movement patterns:
  - Linear - Moves in straight lines, bouncing off walls
  - Random - Moves randomly across the grid
  - Patrol - Moves back and forth along a defined path
  - Chase - Intelligently moves toward the agent's position

- **Adaptive Pathfinding** - Real-time path adaptation that responds to changing environments:
  - Obstacle movement prediction
  - Path replanning when obstacles interfere
  - Danger zone visualization
  - Heatmap visualization of learned paths

## Technical Skills Demonstrated

### Algorithms & Data Structures
- Implementation of multiple graph-based pathfinding algorithms
- Priority queues and efficient data structures
- Dynamic programming concepts

### Artificial Intelligence
- **Reinforcement Learning** - Q-learning algorithm with exploration vs. exploitation balancing
- **Predictive Modeling** - Predicting future positions of dynamic obstacles
- **Adaptive Real-time Planning** - Replanning paths based on environment changes

### Software Design
- Object-oriented design with clear separation of concerns
- Event-driven programming using Swing
- Visualization techniques for complex data (heat maps, danger zones)
- Real-time simulation with multi-threaded processing

### UI/UX Implementation
- Interactive grid-based interface
- Real-time visual feedback of algorithm execution
- Intuitive controls for environment manipulation

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

### Traditional Pathfinding
1. Run `java PathFindingVisualizer`
2. Set up your grid:
   - The default grid has a start (green) and end (red) point
   - Left-click on an empty cell to place a wall
   - Left-click on a wall to remove it
   - Right-click on a wall to remove it
   - Left-click on start/end to remove them, then click on empty cells to place them again
3. Select an algorithm from the dropdown menu
4. Click "Start" to visualize the algorithm

### Machine Learning Edition
1. Run `java PathFindingVisualizerML`
2. Set up the grid with walls as in the traditional version
3. Use the tabbed interface at the bottom:

   **Reinforcement Learning Tab:**
   - Click "Train RL Agent" to start the learning process
   - Watch as the agent explores the environment and learns
   - After training completes, click "Show Learned Path" to visualize:
     - The heat map showing the agent's learned preferences
     - The final path discovered by the agent

   **Dynamic Obstacles Tab:**
   - Select an obstacle type (Linear, Random, Patrol, Chase)
   - Click "Add Obstacle" to place obstacles on the grid
   - Adjust the animation speed with the slider
   - Check "Show Predictions" to visualize danger zones
   - Click "Start Adaptive Mode" to watch real-time path adaptation
   - Observe how the pathfinder adapts when obstacles move

## Project Structure

- **PathFindingVisualizer.java** - Main class for traditional algorithms
- **PathFindingVisualizerML.java** - Main class for ML-enhanced version
- **RLPathAgent.java** - Reinforcement learning implementation
- **AdaptivePathFinder.java** - Real-time adaptive pathfinding
- **DynamicObstacle.java** - Moving obstacle implementation
- **Cell.java** - Grid cell representation with visualization capabilities

## Building and Running

Compile all Java files:
```
javac *.java
```

Run the traditional version:
```
java PathFindingVisualizer
```

Run the ML-enhanced version:
```
java PathFindingVisualizerML
```

## Requirements

- Java JDK 8 or higher
- Swing (included in JDK)

## Educational Value

This project serves as a comprehensive demonstration of both classical algorithms and modern AI approaches to pathfinding problems. It visualizes the differences between deterministic algorithms and learning-based approaches, showing how AI can adapt to dynamic environments that traditional algorithms struggle with.

The reinforcement learning implementation demonstrates how agents can learn optimal policies through exploration and exploitation, while the adaptive pathfinding shows how real-time planning can respond to unpredictable changes in the environment.
