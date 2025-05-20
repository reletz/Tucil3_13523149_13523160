package solver.heuristic;

import logic.Board;
import logic.GameState;
import logic.GameState.PieceState;

/**
 * Heuristic that measures how crowded the area between the primary piece 
 * and the exit is, with the assumption that more crowded areas will be
 * harder to solve.
 */
public class PieceDensityHeuristic implements Heuristic {
    
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
        
        int blockedCells = 0;
        int totalCells = 0;
        
        // Calculate density based on exit side
        switch (exitSide) {
            case 0: // Top exit - examine area above the primary piece
                if (!primaryPiece.getPiece().isHorizontal()) {
                    // Examine a corridor from the piece to the top edge
                    int corridorWidth = 3; // Width of corridor to examine (can be adjusted)
                    int startX = Math.max(0, primaryX - corridorWidth/2);
                    int endX = Math.min(grid[0].length - 1, primaryX + corridorWidth/2);
                    
                    for (int x = startX; x <= endX; x++) {
                        for (int y = 0; y < primaryY; y++) {
                            totalCells++;
                            if (grid[y][x] != '.') {
                                blockedCells++;
                            }
                        }
                    }
                } else {
                    return Integer.MAX_VALUE; // Horizontal piece can't exit from top
                }
                break;
                
            case 1: // Right exit - examine area to the right of the primary piece
                if (primaryPiece.getPiece().isHorizontal()) {
                    // Examine a corridor from the piece to the right edge
                    int corridorHeight = 3; // Height of corridor to examine
                    int startY = Math.max(0, primaryY - corridorHeight/2);
                    int endY = Math.min(grid.length - 1, primaryY + corridorHeight/2);
                    
                    for (int y = startY; y <= endY; y++) {
                        for (int x = primaryX + pieceSize; x < grid[0].length; x++) {
                            totalCells++;
                            if (grid[y][x] != '.') {
                                blockedCells++;
                            }
                        }
                    }
                } else {
                    return Integer.MAX_VALUE; // Vertical piece can't exit from right
                }
                break;
                
            case 2: // Bottom exit - examine area below the primary piece
                if (!primaryPiece.getPiece().isHorizontal()) {
                    // Examine a corridor from the piece to the bottom edge
                    int corridorWidth = 3; // Width of corridor to examine
                    int startX = Math.max(0, primaryX - corridorWidth/2);
                    int endX = Math.min(grid[0].length - 1, primaryX + corridorWidth/2);
                    
                    for (int x = startX; x <= endX; x++) {
                        for (int y = primaryY + pieceSize; y < grid.length; y++) {
                            totalCells++;
                            if (grid[y][x] != '.') {
                                blockedCells++;
                            }
                        }
                    }
                } else {
                    return Integer.MAX_VALUE; // Horizontal piece can't exit from bottom
                }
                break;
                
            case 3: // Left exit - examine area to the left of the primary piece
                if (primaryPiece.getPiece().isHorizontal()) {
                    // Examine a corridor from the piece to the left edge
                    int corridorHeight = 3; // Height of corridor to examine
                    int startY = Math.max(0, primaryY - corridorHeight/2);
                    int endY = Math.min(grid.length - 1, primaryY + corridorHeight/2);
                    
                    for (int y = startY; y <= endY; y++) {
                        for (int x = 0; x < primaryX; x++) {
                            totalCells++;
                            if (grid[y][x] != '.') {
                                blockedCells++;
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
        
        // Avoid division by zero
        if (totalCells == 0) {
            return 0;
        }
        
        // Calculate density as percentage of blocked cells
        double density = (double)blockedCells / totalCells;
        
        // Scale to reasonable range for heuristic (0-100)
        return (int)(density * 100);
    }
    
    @Override
    public String getName() {
        return "Piece Density";
    }
}