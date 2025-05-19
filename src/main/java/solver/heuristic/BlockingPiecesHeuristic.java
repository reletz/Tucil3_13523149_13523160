package solver.heuristic;

import logic.Board;
import logic.GameState;
import logic.GameState.PieceState;

/**
 * Heuristic that counts the number of pieces directly in the path
 * of the primary piece to the exit.
 */
public class BlockingPiecesHeuristic implements Heuristic {
  
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
    
    int blockingPieces = 0;
    
    // Count blocking pieces based on exit side
    switch (exitSide) {
        case 0: // Top exit - check path from piece to top
            if (!primaryPiece.getPiece().isHorizontal()) {
                // First align X coordinate if needed
                if (primaryX != exitX) {
                    return Integer.MAX_VALUE; // Can't reach exit without moving
                }
                
                // Count pieces from primary piece to top edge
                for (int y = primaryY - 1; y >= 0; y--) {
                    if (grid[y][primaryX] != '.' && grid[y][primaryX] != primaryPiece.getPiece().getLabel()) {
                        blockingPieces++;
                    }
                }
            } else {
                return Integer.MAX_VALUE; // Horizontal piece can't exit from top
            }
            break;
            
        case 1: // Right exit - check path from piece to right edge
            if (primaryPiece.getPiece().isHorizontal()) {
                // First align Y coordinate if needed
                if (primaryY != exitY) {
                    return Integer.MAX_VALUE; // Can't reach exit without moving
                }
                
                // Count pieces from primary piece to right edge
                for (int x = primaryX + pieceSize; x < grid[0].length; x++) {
                    if (grid[primaryY][x] != '.' && grid[primaryY][x] != primaryPiece.getPiece().getLabel()) {
                        blockingPieces++;
                    }
                }
            } else {
                return Integer.MAX_VALUE; // Vertical piece can't exit from right
            }
            break;
            
        case 2: // Bottom exit - check path from piece to bottom edge
            if (!primaryPiece.getPiece().isHorizontal()) {
                // First align X coordinate if needed
                if (primaryX != exitX) {
                    return Integer.MAX_VALUE; // Can't reach exit without moving
                }
                
                // Count pieces from primary piece to bottom edge
                for (int y = primaryY + pieceSize; y < grid.length; y++) {
                    if (grid[y][primaryX] != '.' && grid[y][primaryX] != primaryPiece.getPiece().getLabel()) {
                        blockingPieces++;
                    }
                }
            } else {
                return Integer.MAX_VALUE; // Horizontal piece can't exit from bottom
            }
            break;
            
        case 3: // Left exit - check path from piece to left edge
            if (primaryPiece.getPiece().isHorizontal()) {
                // First align Y coordinate if needed
                if (primaryY != exitY) {
                    return Integer.MAX_VALUE; // Can't reach exit without moving
                }
                
                // Count pieces from primary piece to left edge
                for (int x = primaryX - 1; x >= 0; x--) {
                    if (grid[primaryY][x] != '.' && grid[primaryY][x] != primaryPiece.getPiece().getLabel()) {
                        blockingPieces++;
                    }
                }
            } else {
                return Integer.MAX_VALUE; // Vertical piece can't exit from left
            }
            break;
            
        default:
            return Integer.MAX_VALUE; // Invalid exit side
    }
    
    return blockingPieces;
  }
  
  @Override
  public String getName() {
      return "Blocking Pieces";
  }
}