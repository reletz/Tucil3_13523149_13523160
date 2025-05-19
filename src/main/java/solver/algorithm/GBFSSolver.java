package solver.algorithm;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import logic.GameLogic;
import logic.GameState;
import logic.Node;

public class GBFSSolver extends Solver {
  @Override
  /**
   * Generate a solution to a 
   */
  public Node solve(GameState initialState){
    long startTime = System.currentTimeMillis();

    PriorityQueue<Node> searchQueue = new PriorityQueue<>();
    Set<GameState> visited = new HashSet<>();

    Node startNode = new Node(initialState);
    searchQueue.add(startNode);

    int nodesExplored = 0;
    int maxQueueSize = 1;
    while (!searchQueue.isEmpty()){
      // Pop
      Node current = searchQueue.poll();
      nodesExplored++;

      if (current.isGoalState()) {
        this.solutionPath = buildPath(current);
        this.nodesExplored = nodesExplored;
        this.executionTimeMs = System.currentTimeMillis() - startTime;
        this.maxQueueSize = maxQueueSize;
        return current;
      }
      
      if (visited.contains(current.getState())){
        continue;
      }

      visited.add(current.getState());
      
      // Generate all possible moves from this state
      List<Node> successors = GameLogic.generateSuccessors(current);
      
      // Add all valid successors to the queue
      for (Node successor : successors) {
        if (!visited.contains(successor.getState())) {
          searchQueue.add(successor);
        }
      }
      
      // Update max queue size
      maxQueueSize = Math.max(maxQueueSize, searchQueue.size());
    }
    
    // No solution found
    this.nodesExplored = nodesExplored;
    this.executionTimeMs = System.currentTimeMillis() - startTime;
    this.maxQueueSize = maxQueueSize;
    return null;
  }

  private List<Node> buildPath(Node goalNode) {
    LinkedList<Node> path = new LinkedList<>();
    Node current = goalNode;
    
    while (current != null) {
      path.addFirst(current);
      current = current.getParent();
    }
    
    return path;
  }
  
  @Override
  public String getAlgorithmName() {
    return "Uniform Cost Search";
  }
}