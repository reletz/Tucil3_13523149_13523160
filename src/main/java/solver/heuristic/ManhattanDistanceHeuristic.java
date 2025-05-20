package solver.heuristic;

import java.util.HashMap;
import java.util.Map;

import logic.Board;
import logic.GameState;
import logic.GameState.PieceState;
import logic.Piece;

/**
 * Heuristic that estimates cost based on the sum of the Manhattan distances
 * each blocking piece needs to move to clear the primary piece's path.
 */
public class ManhattanDistanceHeuristic implements Heuristic {
    
    @Override
    public int calculate(GameState state) {
        Board board = state.getBoard();
        PieceState primaryPiece = state.getPrimaryPieceState();
        char[][] grid = board.getGrid();
        
        // Get board exit information
        int exitX = board.getOutCoordX();
        int exitY = board.getOutCoordY();
        int exitSide = board.getExitSide();
        
        // Get primary piece position
        int primaryX = primaryPiece.getX();
        int primaryY = primaryPiece.getY();
        int pieceSize = primaryPiece.getPiece().getSize();
        
        // Map to track pieces in the path and how far they need to move
        Map<Character, Integer> blockingPieces = new HashMap<>();
        
        // Determine path to exit and identify blocking pieces
        switch (exitSide) {
            case 0: // Top exit - path is from piece to top
                if (!primaryPiece.getPiece().isHorizontal()) {
                    // First check X alignment
                    if (primaryX != exitX) {
                        // Add cost to move primary piece to align with exit
                        return Math.abs(primaryX - exitX) + 1;
                    }
                    
                    // Check for blocking pieces from primary piece to top edge
                    for (int y = primaryY - 1; y >= 0; y--) {
                        char cell = grid[y][primaryX];
                        if (cell != '.' && cell != primaryPiece.getPiece().getLabel()) {
                            // Find the piece and calculate how far it needs to move
                            PieceState blockingPiece = findPieceState(state, cell);
                            if (blockingPiece != null) {
                                // Calculate minimum move distance to clear path
                                int moveDistance = getMinClearPathDistance(board, blockingPiece, primaryX, y);
                                blockingPieces.put(cell, moveDistance);
                            }
                        }
                    }
                } else {
                    return Integer.MAX_VALUE; // Horizontal piece can't exit from top
                }
                break;
                
            case 1: // Right exit
                if (primaryPiece.getPiece().isHorizontal()) {
                    // First check Y alignment
                    if (primaryY != exitY) {
                        // Add cost to move primary piece to align with exit
                        return Math.abs(primaryY - exitY) + 1;
                    }
                    
                    // Check for blocking pieces from primary piece to right edge
                    for (int x = primaryX + pieceSize; x < grid[0].length; x++) {
                        char cell = grid[primaryY][x];
                        if (cell != '.' && cell != primaryPiece.getPiece().getLabel()) {
                            // Find the piece and calculate how far it needs to move
                            PieceState blockingPiece = findPieceState(state, cell);
                            if (blockingPiece != null) {
                                // Calculate minimum move distance to clear path
                                int moveDistance = getMinClearPathDistance(board, blockingPiece, x, primaryY);
                                blockingPieces.put(cell, moveDistance);
                            }
                        }
                    }
                } else {
                    return Integer.MAX_VALUE; // Vertical piece can't exit from right
                }
                break;
                
            case 2: // Bottom exit
                if (!primaryPiece.getPiece().isHorizontal()) {
                    // First check X alignment
                    if (primaryX != exitX) {
                        // Add cost to move primary piece to align with exit
                        return Math.abs(primaryX - exitX) + 1;
                    }
                    
                    // Check for blocking pieces from primary piece to bottom edge
                    for (int y = primaryY + pieceSize; y < grid.length; y++) {
                        char cell = grid[y][primaryX];
                        if (cell != '.' && cell != primaryPiece.getPiece().getLabel()) {
                            // Find the piece and calculate how far it needs to move
                            PieceState blockingPiece = findPieceState(state, cell);
                            if (blockingPiece != null) {
                                // Calculate minimum move distance to clear path
                                int moveDistance = getMinClearPathDistance(board, blockingPiece, primaryX, y);
                                blockingPieces.put(cell, moveDistance);
                            }
                        }
                    }
                } else {
                    return Integer.MAX_VALUE; // Horizontal piece can't exit from bottom
                }
                break;
                
            case 3: // Left exit
                if (primaryPiece.getPiece().isHorizontal()) {
                    // First check Y alignment
                    if (primaryY != exitY) {
                        // Add cost to move primary piece to align with exit
                        return Math.abs(primaryY - exitY) + 1;
                    }
                    
                    // Check for blocking pieces from primary piece to left edge
                    for (int x = primaryX - 1; x >= 0; x--) {
                        char cell = grid[primaryY][x];
                        if (cell != '.' && cell != primaryPiece.getPiece().getLabel()) {
                            // Find the piece and calculate how far it needs to move
                            PieceState blockingPiece = findPieceState(state, cell);
                            if (blockingPiece != null) {
                                // Calculate minimum move distance to clear path
                                int moveDistance = getMinClearPathDistance(board, blockingPiece, x, primaryY);
                                blockingPieces.put(cell, moveDistance);
                            }
                        }
                    }
                } else {
                    return Integer.MAX_VALUE; // Vertical piece can't exit from left
                }
                break;
                
            default:
                return Integer.MAX_VALUE; // Invalid exit side
        }
        
        // Sum up all the move distances
        int totalDistance = 0;
        for (int distance : blockingPieces.values()) {
            totalDistance += distance;
        }
        
        // Add 1 for the primary piece's move to exit
        return totalDistance + 1;
    }
    
    /**
     * Finds the PieceState for a given piece label
     */
    private PieceState findPieceState(GameState state, char label) {
        if (state.getPrimaryPieceState().getPiece().getLabel() == label) {
            return state.getPrimaryPieceState();
        }
        
        return state.getPieces().get(label);
    }
    
    /**
     * Calculates the minimum distance a blocking piece needs to move to clear the path
     */
    private int getMinClearPathDistance(Board board, PieceState blockingPiece, int blockingX, int blockingY) {
        Piece piece = blockingPiece.getPiece();
        int size = piece.getSize();
        
        // For horizontal pieces, we need to move up or down to clear the path
        if (piece.isHorizontal()) {
            // Calculate distance to move up or down
            int distanceUp = blockingPiece.getY() + 1; // Distance to top edge
            int distanceDown = board.getGrid().length - (blockingPiece.getY() + size); // Distance to bottom edge
            
            // Return the shorter distance
            return Math.min(distanceUp, distanceDown);
        } 
        // For vertical pieces, we need to move left or right to clear the path
        else {
            // Calculate distance to move left or right
            int distanceLeft = blockingPiece.getX() + 1; // Distance to left edge
            int distanceRight = board.getGrid()[0].length - (blockingPiece.getX() + size); // Distance to right edge
            
            // Return the shorter distance
            return Math.min(distanceLeft, distanceRight);
        }
    }
    
    @Override
    public String getName() {
        return "Manhattan Distance";
    }
}