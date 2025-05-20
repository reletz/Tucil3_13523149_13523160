package solver.heuristic;

import logic.GameState;

/**
 * Interface for heuristic functions used by search algorithms.
 * Heuristic functions estimate the cost from a state to the goal state.
 */
public interface Heuristic {
    /**
     * Calculates the heuristic value for a given game state.
     * 
     * @param state The game state to evaluate
     * @return The estimated distance/cost to goal (lower is better)
     */
    int calculate(GameState state);
    
    /**
     * Gets the name of this heuristic for reporting purposes.
     * 
     * @return The heuristic name
     */
    String getName();
}