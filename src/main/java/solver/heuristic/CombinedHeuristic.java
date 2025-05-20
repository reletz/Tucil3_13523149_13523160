package solver.heuristic;

import logic.GameState;

/**
 * A combined heuristic that uses a weighted sum of multiple heuristics.
 * This provides a more balanced estimate that accounts for different
 * aspects of the puzzle state.
 */
public class CombinedHeuristic implements Heuristic {
    
    private final DistanceToExitHeuristic distanceHeuristic;
    private final BlockingPiecesHeuristic blockingHeuristic;
    private final ManhattanDistanceHeuristic manhattanHeuristic;
    private final PieceDensityHeuristic densityHeuristic;
    
    private final double distanceWeight;
    private final double blockingWeight;
    private final double manhattanWeight;
    private final double densityWeight;
    
    /**
     * Constructor with customizable weights for each component
     */
    public CombinedHeuristic(double distanceWeight, double blockingWeight, 
                             double manhattanWeight, double densityWeight) {
        this.distanceHeuristic = new DistanceToExitHeuristic();
        this.blockingHeuristic = new BlockingPiecesHeuristic();
        this.manhattanHeuristic = new ManhattanDistanceHeuristic();
        this.densityHeuristic = new PieceDensityHeuristic();
        
        this.distanceWeight = distanceWeight;
        this.blockingWeight = blockingWeight;
        this.manhattanWeight = manhattanWeight;
        this.densityWeight = densityWeight;
    }
    
    /**
     * Constructor with default equal weights
     */
    public CombinedHeuristic() {
        this(1.0, 1.0, 1.0, 0.5); // Default weights
    }
    
    @Override
    public int calculate(GameState state) {
        // Get values from individual heuristics
        int distanceValue = distanceHeuristic.calculate(state);
        int blockingValue = blockingHeuristic.calculate(state);
        int manhattanValue = manhattanHeuristic.calculate(state);
        int densityValue = densityHeuristic.calculate(state);
        
        // Handle MAX_VALUE cases (impossible situations)
        if (distanceValue == Integer.MAX_VALUE) {
            distanceValue = 1000; // Use a large but finite value
        }
        if (blockingValue == Integer.MAX_VALUE) {
            blockingValue = 1000;
        }
        if (manhattanValue == Integer.MAX_VALUE) {
            manhattanValue = 1000;
        }
        if (densityValue == Integer.MAX_VALUE) {
            densityValue = 1000;
        }
        
        // Calculate weighted sum
        double weightedSum = distanceValue * distanceWeight +
                            blockingValue * blockingWeight +
                            manhattanValue * manhattanWeight +
                            densityValue * densityWeight;
        
        // Normalize weights to prevent extremely large values
        double totalWeight = distanceWeight + blockingWeight + manhattanWeight + densityWeight;
        weightedSum = weightedSum / totalWeight;
        
        return (int) Math.round(weightedSum);
    }
    
    @Override
    public String getName() {
        return "Combined Heuristic";
    }
}