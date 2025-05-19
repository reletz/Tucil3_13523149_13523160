package solver.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import logic.GameState;
import logic.GameState.PieceState;
import logic.Node;
import logic.Piece;

public class UCSolver extends Solver {
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
      List<Node> successors = generateSuccessors(current);
      
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
  
  /**
   * Generates all possible successor nodes by moving each piece in valid directions
   */
  private List<Node> generateSuccessors(Node node) {
    List<Node> successors = new ArrayList<>();
    GameState state = node.getState();
    
    // Get all pieces including the primary piece
    Map<Character, PieceState> allPieces = new HashMap<>(state.getPieces());
    PieceState primaryState = state.getPrimaryPieceState();
    allPieces.put(primaryState.getPiece().getLabel(), primaryState);
    
    // For each piece, try moving in all valid directions
    for (PieceState pieceState : allPieces.values()) {
      Piece piece = pieceState.getPiece();
      char label = piece.getLabel();
      int currentX = pieceState.getX();
      int currentY = pieceState.getY();
      
      // Try moves based on piece orientation
      if (piece.isHorizontal()) {
        // Try moving left
        tryHorizontalMoves(node, state, piece, label, currentX, currentY, -1, successors);
        
        // Try moving right
        tryHorizontalMoves(node, state, piece, label, currentX, currentY, 1, successors);
      } else {
        // Try moving up
        tryVerticalMoves(node, state, piece, label, currentX, currentY, -1, successors);
        
        // Try moving down
        tryVerticalMoves(node, state, piece, label, currentX, currentY, 1, successors);
      }
    }
    
    return successors;
  }
  
  @Override
  public String getAlgorithmName() {
    return "Uniform Cost Search";
  }
}