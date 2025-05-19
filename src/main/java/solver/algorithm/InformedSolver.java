package solver.algorithm;

import solver.heuristic.Heuristic;

/**
 * Abstract base class for informed search algorithms that use heuristics.
 * This extends the general Solver with heuristic-specific functionality.
 */
public abstract class InformedSolver extends Solver {
    
  protected Heuristic heuristic;
  
  /**
   * Constructor with specified heuristic.
   * 
   * @param heuristic The heuristic to use for search
   */
  public InformedSolver(Heuristic heuristic) {
      super();
      this.heuristic = heuristic;
  }
  
  /**
   * Gets the heuristic used by this solver.
   * 
   * @return The heuristic
   */
  public Heuristic getHeuristic() {
      return heuristic;
  }
  
  /**
   * Sets a new heuristic for this solver.
   * 
   * @param heuristic The new heuristic to use
   */
  public void setHeuristic(Heuristic heuristic) {
    this.heuristic = heuristic;
  }
  
  @Override
  public void printStatistics() {
    super.printStatistics();
    System.out.println("Heuristik: " + heuristic.getName());
  }
}