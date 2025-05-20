package solver.heuristic;

import logic.Board;
import logic.GameState;
import logic.GameState.PieceState;

/**
 * Heuristic that estimates cost based on how many moves the primary piece
 * would need to reach the exit if no other pieces were in the way.
 */
public class DistanceToExitHeuristic implements Heuristic {
    
  @Override
  public int calculate(GameState state) {
    Board board = state.getBoard();
    PieceState primaryPiece = state.getPrimaryPieceState();
    
    // Get board exit information
    int exitX = board.getOutCoordX();
    int exitY = board.getOutCoordY();
    int exitSide = board.getExitSide();
    
    // Get primary piece position
    int primaryX = primaryPiece.getX();
    int primaryY = primaryPiece.getY();
    int pieceSize = primaryPiece.getPiece().getSize();
    
    // Calculate distance based on exit side
    switch (exitSide) {
      case 0: // Top exit
          // For vertical piece, calculate distance to align with exit
        if (!primaryPiece.getPiece().isHorizontal()) {
          return Math.abs(primaryX - exitX) + primaryY; // Distance to align + distance to top
        } else {
          return Integer.MAX_VALUE; // Horizontal piece can't exit from top
        }
          
      case 1: // Right exit
        // For horizontal piece, calculate distance to right edge
        if (primaryPiece.getPiece().isHorizontal()) {
          return board.getGrid()[0].length - (primaryX + pieceSize); // Distance to right edge
        } else {
          return Integer.MAX_VALUE; // Vertical piece can't exit from right
        }
          
      case 2: // Bottom exit
        // For vertical piece, calculate distance to align with exit
        if (!primaryPiece.getPiece().isHorizontal()) {
          return Math.abs(primaryX - exitX) + (board.getGrid().length - (primaryY + pieceSize));
        } else {
          return Integer.MAX_VALUE; // Horizontal piece can't exit from bottom
        }
          
      case 3: // Left exit
        // For horizontal piece, calculate distance to left edge
        if (primaryPiece.getPiece().isHorizontal()) {
          return primaryX; // Distance to left edge
      } else {
          return Integer.MAX_VALUE; // Vertical piece can't exit from left
        }
          
      default:
          return Integer.MAX_VALUE; // Invalid exit side
    }
  }
  
  @Override
  public String getName() {
      return "Distance to Exit";
  }
}