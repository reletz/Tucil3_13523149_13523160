package solver.algorithm;

import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import logic.GameLogic;
import logic.GameState;
import logic.Node;
import solver.heuristic.BlockingPiecesHeuristic;
import solver.heuristic.Heuristic;

public class AStarSolver extends InformedSolver {
  
  public AStarSolver(Heuristic heuristic) {
    super(heuristic);
  }

  public AStarSolver(){
    this(new BlockingPiecesHeuristic());
  }

  @Override
  /**
   * Solves the Rush Hour puzzle using A* search algorithm.
   * This algorithm finds the optimal solution by combining:
   * - g(n): actual cost from start to current node
   * - h(n): estimated cost from current node to goal
   * - f(n) = g(n) + h(n): estimated total cost through this node
   *
   * @param initialState The starting game state
   * @return The goal node containing the solution path, or null if no solution found
   */
  public Node solve(GameState initialState) {
    long startTime = System.currentTimeMillis();

    // Initialize priority queue with starting node
    PriorityQueue<Node> openSet = new PriorityQueue<>();
    Set<GameState> closedSet = new HashSet<>();

    // Create start node with heuristic evaluation
    Node startNode = new Node(initialState);
    int startHeuristic = heuristic.calculate(initialState);
    startNode.setHeuristicValue(startHeuristic);
    openSet.add(startNode);

    int nodesExplored = 0;
    int maxQueueSize = 1;

    while (!openSet.isEmpty()) {
      // Get node with lowest f value
      Node current = openSet.poll();
      nodesExplored++;

      // Goal test
      if (current.isGoalState()) {
        this.solutionPath = buildPath(current);
        this.nodesExplored = nodesExplored;
        this.executionTimeMs = System.currentTimeMillis() - startTime;
        this.maxQueueSize = maxQueueSize;
        return current;
      }

      // Skip if already visited
      if (closedSet.contains(current.getState())) {
        continue;
      }

      // Add to visited set
      closedSet.add(current.getState());

      // Generate successors
      List<Node> successors = GameLogic.generateSuccessors(current, heuristic);

      // Process each successor
      for (Node successor : successors) {
        // Skip if already visited
        if (!closedSet.contains(successor.getState())) {
          // Note: The heuristic has already been applied by generateSuccessors
          // The Node compareTo method will use totalCost for priority queue ordering
          openSet.add(successor);
        }
      }

      // Track maximum queue size for statistics
      maxQueueSize = Math.max(maxQueueSize, openSet.size());
    }

    // No solution found
    this.nodesExplored = nodesExplored;
    this.executionTimeMs = System.currentTimeMillis() - startTime;
    this.maxQueueSize = maxQueueSize;
    return null;
  }

  @Override
  public String getAlgorithmName() {
    return "A* Search";
  }
}