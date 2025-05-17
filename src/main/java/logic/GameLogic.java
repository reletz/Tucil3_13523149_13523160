package logic;

import java.util.HashMap;
import java.util.Map;

/**
 * Kelas GameLogic buat mengatur operasi logika permainan.
 * Menghandle penempatan dan pergerakan Block di papan.
 */
public class GameLogic {
  
  /**
   * Membuat salinan dari GameState.
   * 
   * @param gameState GameState yang akan disalin
   * @return Salinan GameState baru
   */
  public static GameState copyGameState(GameState gameState) {
    Board boardCopy = gameState.getBoard(); // Board is immutable
    
    // Copy pieces
    Map<Character, GameState.PieceState> piecesCopy = new HashMap<>();
    for (Map.Entry<Character, GameState.PieceState> entry : gameState.getPieces().entrySet()) {
      GameState.PieceState ps = entry.getValue();
      piecesCopy.put(entry.getKey(), 
              new GameState.PieceState(ps.getPiece().copy(), ps.getX(), ps.getY()));
    }
    
    // Copy primary piece
    GameState.PieceState primaryPS = gameState.getPrimaryPieceState();
    GameState.PieceState primaryPSCopy = new GameState.PieceState(
            ((PrimaryPiece)primaryPS.getPiece()).copy(), 
            primaryPS.getX(), primaryPS.getY());
    
    return new GameState(boardCopy, piecesCopy, primaryPSCopy);
  }
  
  /**
   * Menggerakkan Block ke arah tertentu.
   * 
   * @param gameState GameState saat ini
   * @param pieceLabel Label Block yang akan digerakkan
   * @param direction Arah gerakan (0: kanan, 1: bawah, 2: kiri, 3: atas)
   * @return GameState baru jika gerakan valid, null jika tidak valid
   */
  public static GameState movePiece(GameState gameState, char pieceLabel, int direction) {
    GameState.PieceState pieceState;
    boolean isPrimary = false;
    
    // Check if it's the primary piece
    if (gameState.getPrimaryPieceState().getPiece().getLabel() == pieceLabel) {
      pieceState = gameState.getPrimaryPieceState();
      isPrimary = true;
    } else {
      pieceState = gameState.getPieces().get(pieceLabel);
      if (pieceState == null) {
        return null; // Piece not found
      }
    }
    
    Piece piece = pieceState.getPiece();
    int currentX = pieceState.getX();
    int currentY = pieceState.getY();
    int newX = currentX;
    int newY = currentY;
    
    // Calculate new position based on direction
    switch (direction) {
      case 0 -> { // Right
        if (!piece.isHorizontal()) return null; // Can only move horizontally
        newX = currentX + 1;
      }
      case 1 -> { // Down
        if (piece.isHorizontal()) return null; // Can only move vertically
        newY = currentY + 1;
      }
      case 2 -> { // Left
        if (!piece.isHorizontal()) return null; // Can only move horizontally
        newX = currentX - 1;
      }
      case 3 -> { // Up
        if (piece.isHorizontal()) return null; // Can only move vertically
        newY = currentY - 1;
      }
      default -> { return null; } // Invalid direction
    }
    
    // Check bounds
    Board board = gameState.getBoard();
    int boardWidth = board.getGrid()[0].length;
    int boardHeight = board.getGrid().length;
    
    // Check if new position is valid (within bounds and no collision)
    if (newX < 0 || newY < 0) {
      return null; // Out of bounds (top/left)
    }
    
    // Check if piece would go out of bounds (right/bottom)
    if (piece.isHorizontal()) {
      if (newX + piece.getSize() > boardWidth) {
        return null; // Horizontal piece would exceed right boundary
      }
    } else {
      if (newY + piece.getSize() > boardHeight) {
        return null; // Vertical piece would exceed bottom boundary
      }
    }
    
    // Check for collisions with other pieces
    // Special case for primary piece reaching exit
    boolean allowExit = isPrimary && 
      ((piece.isHorizontal() && 
        newX + piece.getSize() - 1 == board.getOutCoordX() && 
        newY == board.getOutCoordY()) ||
        (!piece.isHorizontal() && 
        newX == board.getOutCoordX() && 
        newY + piece.getSize() - 1 == board.getOutCoordY()));
    
    if (!allowExit && wouldCollide(gameState, pieceLabel, newX, newY)) {
      return null; // Would collide with another piece
    }
    
    // Create a new game state with the updated position
    return createNewStateWithMovedPiece(gameState, pieceLabel, newX, newY);
  }
  
  /**
   * Geser Block ke kanan (hanya untuk Block horizontal).
   * 
   * @param gameState Keadaan permainan saat ini
   * @param pieceLabel Label Block yang akan digeser
   * @return GameState baru jika berhasil digeser, null jika tidak
   */
  public static GameState moveRight(GameState gameState, char pieceLabel) {
    return movePiece(gameState, pieceLabel, 0);
  }
  
  /**
   * Geser Block ke bawah (hanya untuk Block vertikal).
   * 
   * @param gameState Keadaan permainan saat ini
   * @param pieceLabel Label Block yang akan digeser
   * @return GameState baru jika berhasil digeser, null jika tidak
   */
  public static GameState moveDown(GameState gameState, char pieceLabel) {
    return movePiece(gameState, pieceLabel, 1);
  }
  
  /**
   * Geser Block ke kiri (hanya untuk Block horizontal).
   * 
   * @param gameState Keadaan permainan saat ini
   * @param pieceLabel Label Block yang akan digeser
   * @return GameState baru jika berhasil digeser, null jika tidak
   */
  public static GameState moveLeft(GameState gameState, char pieceLabel) {
    return movePiece(gameState, pieceLabel, 2);
  }
  
  /**
   * Geser Block ke atas (hanya untuk Block vertikal).
   * 
   * @param gameState Keadaan permainan saat ini
   * @param pieceLabel Label Block yang akan digeser
   * @return GameState baru jika berhasil digeser, null jika tidak
   */
  public static GameState moveUp(GameState gameState, char pieceLabel) {
    return movePiece(gameState, pieceLabel, 3);
  }
  
  /**
   * Check for collisions between a piece at a new position and other pieces.
   * 
   * @param gameState Current game state
   * @param pieceLabel Label of the piece to check
   * @param newX New X coordinate
   * @param newY New Y coordinate
   * @return true if collision would occur, false otherwise
   */
  private static boolean wouldCollide(GameState gameState, char pieceLabel, int newX, int newY) {
    // Use the existing collision detection from GameState
    return gameState.wouldCollide(pieceLabel, newX, newY);
  }
  
  /**
   * Create a new GameState with an updated piece position.
   * 
   * @param gameState Current game state
   * @param pieceLabel Label of the piece to move
   * @param newX New X coordinate
   * @param newY New Y coordinate
   * @return New GameState with the piece moved
   */
  private static GameState createNewStateWithMovedPiece(GameState gameState, char pieceLabel, int newX, int newY) {
    // Create copies of all components to avoid modifying the original state
    Board boardCopy = gameState.getBoard(); // Board is immutable
    
    // Copy all pieces
    Map<Character, GameState.PieceState> piecesCopy = new HashMap<>();
    for (Map.Entry<Character, GameState.PieceState> entry : gameState.getPieces().entrySet()) {
      GameState.PieceState ps = entry.getValue();
      if (ps.getPiece().getLabel() == pieceLabel) {
        // Update this piece's position
        piecesCopy.put(entry.getKey(), 
                new GameState.PieceState(ps.getPiece().copy(), newX, newY));
      } else {
        // Copy unchanged
        piecesCopy.put(entry.getKey(), 
                new GameState.PieceState(ps.getPiece().copy(), ps.getX(), ps.getY()));
      }
    }
    
    // Handle primary piece separately
    GameState.PieceState primaryPS = gameState.getPrimaryPieceState();
    GameState.PieceState primaryPSCopy;
    
    if (primaryPS.getPiece().getLabel() == pieceLabel) {
      // Update primary piece position
      primaryPSCopy = new GameState.PieceState(
              ((PrimaryPiece)primaryPS.getPiece()).copy(), newX, newY);
    } else {
      // Copy unchanged
      primaryPSCopy = new GameState.PieceState(
              ((PrimaryPiece)primaryPS.getPiece()).copy(), 
              primaryPS.getX(), primaryPS.getY());
    }
    
    return new GameState(boardCopy, piecesCopy, primaryPSCopy);
  }
  
  /**
   * Update the board grid to reflect the current pieces' positions.
   * This is useful for visualization or when converting between representation models.
   * 
   * @param gameState The game state to use for updating the grid
   */
  public static void updateBoardGrid(GameState gameState) {
    Board board = gameState.getBoard();
    char[][] grid = board.getGrid();
    
    // Clear the grid
    for (char[] row : grid) {
      for (int j = 0; j < row.length; j++) {
        row[j] = ' ';
      }
    }
    
    // Place primary piece
    placePieceOnGrid(grid, gameState.getPrimaryPieceState());
    
    // Place all other pieces
    for (GameState.PieceState ps : gameState.getPieces().values()) {
      placePieceOnGrid(grid, ps);
    }
  }
  
  /**
   * Helper method to place a piece on the grid.
   * 
   * @param grid The grid to update
   * @param pieceState The piece state to place
   */
  private static void placePieceOnGrid(char[][] grid, GameState.PieceState pieceState) {
    Piece piece = pieceState.getPiece();
    int x = pieceState.getX();
    int y = pieceState.getY();
    
    for (int i = 0; i < piece.getSize(); i++) {
      if (piece.isHorizontal()) {
        if (x + i < grid[0].length && y < grid.length) {
          grid[y][x + i] = piece.getLabel();
        }
      } else {
        if (x < grid[0].length && y + i < grid.length) {
          grid[y + i][x] = piece.getLabel();
        }
      }
    }
  }
}