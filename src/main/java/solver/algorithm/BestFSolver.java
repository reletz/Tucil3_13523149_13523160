package solver.algorithm;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import logic.GameLogic;
import logic.GameState;
import logic.Node;
import solver.heuristic.BlockingPiecesHeuristic;
import solver.heuristic.Heuristic;

/**
 * Implementation of the Greedy Best-First Search algorithm.
 * This algorithm always chooses to expand the node with the lowest heuristic value,
 * ignoring the cost of reaching that node.
 */
public class BestFSolver extends InformedSolver {
  
  /**
   * Constructor with specified heuristic.
   * 
   * @param heuristic The heuristic to use for search
   */
  public BestFSolver(Heuristic heuristic) {
      super(heuristic);
  }
  
  /**
   * Default constructor using BlockingPiecesHeuristic.
   */
  public BestFSolver() {
      this(new BlockingPiecesHeuristic());
  }
  
  @Override
  /**
   * Solves the Rush Hour puzzle using Greedy Best-First Search.
   * This algorithm prioritizes nodes solely by their heuristic value.
   *
   * @param initialState The starting game state
   * @return The goal node containing the solution path, or null if no solution found
   */
  public Node solve(GameState initialState) {
    long startTime = System.currentTimeMillis();
    
    // Custom comparator that orders nodes by heuristic value only
    Comparator<Node> heuristicComparator = (n1, n2) -> 
        Integer.compare(n1.getHeuristicValue(), n2.getHeuristicValue());
    
    // Initialize priority queue with the custom heuristic comparator
    PriorityQueue<Node> frontier = new PriorityQueue<>(heuristicComparator);
    Set<GameState> visited = new HashSet<>();
    
    // Create and add starting node with heuristic evaluation
    Node startNode = new Node(initialState);
    int startHeuristic = heuristic.calculate(initialState);
    startNode.setHeuristicValue(startHeuristic);
    frontier.add(startNode);
    
    int nodesExplored = 0;
    int maxQueueSize = 1;
    
    while (!frontier.isEmpty()) {
      // Get node with lowest heuristic value
      Node current = frontier.poll();
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
      if (visited.contains(current.getState())) {
          continue;
      }
      
      // Mark as visited
      visited.add(current.getState());
      
      // Generate successors
      List<Node> successors = GameLogic.generateSuccessors(current);
      
      // Process each successor
      for (Node successor : successors) {
          if (!visited.contains(successor.getState())) {
              // Calculate heuristic for the new state
              int h = heuristic.calculate(successor.getState());
              successor.setHeuristicValue(h);
              
              // In Best-First Search, we use only the heuristic for ordering
              frontier.add(successor);
          }
      }
      
      // Track maximum queue size for statistics
      maxQueueSize = Math.max(maxQueueSize, frontier.size());
    }
    
    // No solution found
    this.nodesExplored = nodesExplored;
    this.executionTimeMs = System.currentTimeMillis() - startTime;
    this.maxQueueSize = maxQueueSize;
    return null;
  }
    
  @Override
  public String getAlgorithmName() {
    return "Greedy Best-First Search";
  }
}