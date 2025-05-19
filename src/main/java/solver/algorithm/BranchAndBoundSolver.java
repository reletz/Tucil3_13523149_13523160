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

/**
 * Implementation of the Branch and Bound algorithm for Rush Hour puzzle solving.
 * This algorithm maintains a global upper bound on solution cost and prunes branches
 * that exceed this bound, using a lower bound estimate to guide the search.
 */
public class BranchAndBoundSolver extends InformedSolver {
    private int upperBound;
    
    public BranchAndBoundSolver() {
      super(new BlockingPiecesHeuristic());
    }
    
    @Override
    public Node solve(GameState initialState) {
        long startTime = System.currentTimeMillis();
        
        // Initialize with a custom comparator ordering nodes by lower bound
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.getCost() + n.getHeuristicValue()));
        Set<GameState> visited = new HashSet<>();
        upperBound = Integer.MAX_VALUE;
        Node bestSolution = null;
        int nodesExplored = 0;
        int maxQueueSize = 1;
        
        // Create initial node and add to queue
        Node rootNode = new Node(initialState);
        int rootHeuristic = heuristic.calculate(initialState);
        rootNode.setHeuristicValue(rootHeuristic);
        queue.add(rootNode);
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            nodesExplored++;
            
            // Check if we've reached the goal
            if (current.isGoalState()) {
                if (current.getCost() < upperBound) {
                    upperBound = current.getCost();
                    bestSolution = current;
                }
                // Don't break - continue searching to ensure optimality
                continue;
            }
            
            // Skip if this branch can't improve on best solution
            int lowerBound = current.getCost() + current.getHeuristicValue();
            if (lowerBound >= upperBound) {
                continue;
            }
            
            // Skip if we've seen this state before
            if (visited.contains(current.getState())) {
                continue;
            }
            
            visited.add(current.getState());
            
            // Generate all possible next moves
            List<Node> successors = GameLogic.generateSuccessors(current);
            
            // Add valid successors to queue
            for (Node successor : successors) {
                // Only process if not already visited
                if (!visited.contains(successor.getState())) {
                    // Calculate heuristic for lower bound
                    int h = heuristic.calculate(successor.getState());
                    successor.setHeuristicValue(h);
                    
                    // Calculate lower bound (current cost + heuristic)
                    int successorLowerBound = successor.getCost() + h;
                    
                    // Only add if it could lead to a better solution
                    if (successorLowerBound < upperBound) {
                        queue.add(successor);
                    }
                }
            }
            
            maxQueueSize = Math.max(maxQueueSize, queue.size());
        }
        
        // Record metrics and return best solution
        if (bestSolution != null) {
          this.solutionPath = buildPath(bestSolution);
          this.nodesExplored = nodesExplored;
          this.executionTimeMs = System.currentTimeMillis() - startTime;
          this.maxQueueSize = maxQueueSize;
        }
        
        return bestSolution;
    }
    
    @Override
    public String getAlgorithmName() {
        return "Branch and Bound";
    }
}