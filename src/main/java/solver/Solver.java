package solver;

import java.util.ArrayList;
import java.util.List;

import logic.GameState;
import logic.Node;

/**
 * Abstract base class for Rush Hour puzzle solvers.
 * Provides common functionality for different search algorithms.
 */
public abstract class Solver {
    protected List<Node> solutionPath;
    protected int nodesExplored;
    protected int maxQueueSize;
    protected long executionTimeMs;
    
    /**
     * Constructor.
     */
    public Solver() {
        solutionPath = new ArrayList<>();
        nodesExplored = 0;
        maxQueueSize = 0;
        executionTimeMs = 0;
    }
    
    /**
     * Solves the Rush Hour puzzle from the given initial state.
     * 
     * @param initialState The starting game state
     * @return The solution node (containing path to goal) or null if no solution
     */
    public abstract Node solve(GameState initialState);
    
    /**
     * Gets the solution path from initial state to goal.
     * 
     * @return List of nodes representing the solution path
     */
    public List<Node> getSolutionPath() {
        return solutionPath;
    }
    
    /**
     * Gets the number of nodes explored during search.
     * 
     * @return Number of nodes
     */
    public int getNodesExplored() {
        return nodesExplored;
    }
    
    /**
     * Gets the maximum size reached by the frontier queue.
     * 
     * @return Maximum queue size
     */
    public int getMaxQueueSize() {
        return maxQueueSize;
    }
    
    /**
     * Gets the execution time in milliseconds.
     * 
     * @return Execution time
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    /**
     * Gets the name of the search algorithm.
     * 
     * @return The algorithm name
     */
    public abstract String getAlgorithmName();
    
    /**
     * Builds the solution path by traversing up from the goal node.
     * 
     * @param goalNode The goal node
     */
    protected void buildSolutionPath(Node goalNode) {
        solutionPath.clear();
        Node current = goalNode;
        
        // Collect nodes from goal to start
        List<Node> reversePath = new ArrayList<>();
        while (current != null) {
            reversePath.add(current);
            current = current.getParent();
        }
        
        // Reverse to get path from start to goal
        for (int i = reversePath.size() - 1; i >= 0; i--) {
            solutionPath.add(reversePath.get(i));
        }
    }
    
    /**
     * Prints statistics about the search.
     */
    public void printStatistics() {
        System.out.println("\n======== SOLUSI DITEMUKAN ========");
        System.out.println("Algoritma: " + getAlgorithmName());
        System.out.println("Jumlah langkah: " + (solutionPath.size() - 1));
        System.out.println("Jumlah simpul yang dibuat: " + nodesExplored);
        System.out.println("Ukuran frontier maksimum: " + maxQueueSize);
        System.out.println("Waktu eksekusi: " + executionTimeMs + " ms");
    }
}