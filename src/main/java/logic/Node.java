package logic;

import java.util.Objects;

/**
 * Represents a node in the search space for Rush Hour puzzle solving algorithms.
 * Each node contains a game state and additional information for pathfinding.
 */
public class Node implements Comparable<Node> {
  private final GameState state;         // Current game state
  private Node parent;                   // Parent node
  private int cost;                      // Cost to reach this node (g value)
  private int heuristicValue;            // Heuristic value (h value)
  private int totalCost;                 // f = g + h for A*
  private String moveMade;               // Description of the move that led to this state

  /**
   * Creates a new node with all properties.
   * 
   * @param state The game state for this node
   * @param parent The parent node (null if this is the root)
   * @param cost The cost to reach this node
   * @param heuristicValue The heuristic value for this node
   * @param moveMade Description of the move that created this state
   */
  public Node(GameState state, Node parent, int cost, int heuristicValue, String moveMade) {
    this.state = state;
    this.parent = parent;
    this.cost = cost;
    this.heuristicValue = heuristicValue;
    this.totalCost = cost + heuristicValue;
    this.moveMade = moveMade;
  }

  /**
   * Creates a new node for the initial state.
   * 
   * @param state The initial game state
   */
  public Node(GameState state) {
    this(state, null, 0, 0, "Initial state");
  }

  /**
   * Gets the game state for this node.
   * 
   * @return The game state
   */
  public GameState getState() {
    return state;
  }

  /**
   * Gets the parent node.
   * 
   * @return The parent node
   */
  public Node getParent() {
    return parent;
  }

  /**
   * Gets the cost to reach this node.
   * 
   * @return The path cost
   */
  public int getCost() {
    return cost;
  }

  /**
   * Gets the heuristic value for this node.
   * 
   * @return The heuristic value
   */
  public int getHeuristicValue() {
    return heuristicValue;
  }

  /**
   * Gets the total cost (g + h) for this node.
   * 
   * @return The total cost
   */
  public int getTotalCost() {
    return totalCost;
  }

  /**
   * Gets the description of the move that led to this state.
   * 
   * @return The move description
   */
  public String getMoveMade() {
    return moveMade;
  }

  /**
   * Updates the heuristic value and recalculates total cost.
   * 
   * @param heuristicValue The new heuristic value
   */
  public void setHeuristicValue(int heuristicValue) {
    this.heuristicValue = heuristicValue;
    this.totalCost = this.cost + heuristicValue;
  }

  /**
   * Checks if the current state is a goal state (primary piece at exit).
   * 
   * @return true if this is a goal state, false otherwise
   */
  public boolean isGoalState() {
    return state.isPrimarPieceAtExit();
  }

  /**
   * Compares nodes based on total cost (for priority queue).
   * 
   * @param other The other node to compare with
   * @return Negative if this has lower cost, positive if higher
   */
  @Override
  public int compareTo(Node other) {
    return Integer.compare(this.totalCost, other.totalCost);
  }

  /**
   * Checks if two nodes represent the same state (for visited set).
   * 
   * @param obj The object to compare with
   * @return True if the states are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    
    // Two nodes are equal if they represent the same board configuration
    // This is important for checking visited states
    Node other = (Node) obj;
    
    // GameState.equals() compares piece configurations
    return Objects.equals(this.state, other.state);
  }

  /**
   * Generates a hash code based on the game state.
   * 
   * @return The hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(state);
  }
}