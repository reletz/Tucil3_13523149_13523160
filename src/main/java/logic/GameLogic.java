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
        for (Map.Entry<Character, GameState.PieceState> entry :
            gameState.getPieces().entrySet()) {
            GameState.PieceState ps = entry.getValue();
            piecesCopy.put(entry.getKey(),
                new GameState.PieceState(
                    ps.getPiece().copy(), ps.getX(), ps.getY()));
        }

        // Copy primary piece
        GameState.PieceState primaryPS = gameState.getPrimaryPieceState();
        GameState.PieceState primaryPSCopy = new GameState.PieceState(
            ((PrimaryPiece) primaryPS.getPiece()).copy(), primaryPS.getX(),
            primaryPS.getY());

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
    public static GameState movePiece(
        GameState gameState, char pieceLabel, int direction) {
        GameState.PieceState pieceState;
        boolean isPrimary = false;

        // Check if it's the primary piece
        if (gameState.getPrimaryPieceState().getPiece().getLabel()
            == pieceLabel) {
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
    
    // Get board information
    Board board = gameState.getBoard();
    int boardWidth = board.getGrid()[0].length;
    int boardHeight = board.getGrid().length;
    int exitSide = board.getExitSide();
    int exitX = board.getOutCoordX();
    int exitY = board.getOutCoordY();
    
    // Check if the primary piece can exit through any side
    boolean canExit = false;
    
    if (isPrimary) {
        // For primary piece, check if it's about to exit through the designated exit
        switch (exitSide) {
            case 0: // Top exit
          // Vertical piece moving up, with its top aligned with the exit
          // position
          canExit = !piece.isHorizontal() && direction == 3 && newX == exitX
              && currentY == 0;
          break;

      case 1: // Right exit
          // Horizontal piece moving right, with its right end aligned with the
          // exit position
          canExit = piece.isHorizontal() && direction == 0
              && currentX + piece.getSize() == boardWidth && currentY == exitY;
          break;

      case 2: // Bottom exit
          // Vertical piece moving down, with its bottom aligned with the exit
          // position
          canExit = !piece.isHorizontal() && direction == 1 && newX == exitX
              && currentY + piece.getSize() == boardHeight;
          break;

      case 3: // Left exit
          // Horizontal piece moving left, with its left end aligned with the
          // exit position
          canExit = piece.isHorizontal() && direction == 2 && currentX == 0
              && currentY == exitY;
          break;
        }
    }

    // Check bounds for non-exit moves
    if (!canExit) {
        // Regular bound checking
        if (newX < 0 || newY < 0
            || (piece.isHorizontal() && newX + piece.getSize() > boardWidth)
            || (!piece.isHorizontal()
                && newY + piece.getSize() > boardHeight)) {
            return null; // Out of bounds
        }

        // Check for collisions with other pieces
        if (wouldCollide(gameState, pieceLabel, newX, newY)) {
            return null; // Collision detected
        }
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
    private static boolean wouldCollide(
        GameState gameState, char pieceLabel, int newX, int newY) {
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
    private static GameState createNewStateWithMovedPiece(
        GameState gameState, char pieceLabel, int newX, int newY) {
        // Create copies of all components to avoid modifying the original state
        Board boardCopy = gameState.getBoard(); // Board is immutable

        // Copy all pieces
        Map<Character, GameState.PieceState> piecesCopy = new HashMap<>();
        for (Map.Entry<Character, GameState.PieceState> entry :
            gameState.getPieces().entrySet()) {
            GameState.PieceState ps = entry.getValue();
            if (ps.getPiece().getLabel() == pieceLabel) {
                // Update this piece's position
                piecesCopy.put(entry.getKey(),
                    new GameState.PieceState(ps.getPiece().copy(), newX, newY));
            } else {
                // Copy unchanged
                piecesCopy.put(entry.getKey(),
                    new GameState.PieceState(
                        ps.getPiece().copy(), ps.getX(), ps.getY()));
            }
        }

        // Handle primary piece separately
        GameState.PieceState primaryPS = gameState.getPrimaryPieceState();
        GameState.PieceState primaryPSCopy;

        if (primaryPS.getPiece().getLabel() == pieceLabel) {
            // Update primary piece position - correctly handle the PrimaryPiece
            // casting
            Piece originalPiece = primaryPS.getPiece();
            // Check if it's actually a PrimaryPiece before casting
            if (originalPiece instanceof PrimaryPiece) {
                primaryPSCopy = new GameState.PieceState(
                    ((PrimaryPiece) originalPiece).copy(), newX, newY);
            } else {
                // Fallback if it's not a PrimaryPiece for some reason
                primaryPSCopy =
                    new GameState.PieceState(originalPiece.copy(), newX, newY);
            }
        } else {
            // Copy unchanged - correctly handle the PrimaryPiece casting
            Piece originalPiece = primaryPS.getPiece();
            // Check if it's actually a PrimaryPiece before casting
            if (originalPiece instanceof PrimaryPiece) {
                primaryPSCopy =
                    new GameState.PieceState(((PrimaryPiece) originalPiece).copy(),
                        primaryPS.getX(), primaryPS.getY());
            } else {
                // Fallback if it's not a PrimaryPiece for some reason
                primaryPSCopy = new GameState.PieceState(
                    originalPiece.copy(), primaryPS.getX(), primaryPS.getY());
            }
        }

        return new GameState(boardCopy, piecesCopy, primaryPSCopy);
    }

    private static void placePieceOnGrid(
        char[][] grid, GameState.PieceState pieceState) {
        Piece piece = pieceState.getPiece();
        int x = pieceState.getX();
        int y = pieceState.getY();
        int boardWidth = grid[0].length;
        int boardHeight = grid.length;

        for (int i = 0; i < piece.getSize(); i++) {
            // Only draw the piece if it's within the board boundaries
            if (piece.isHorizontal()) {
                int currentX = x + i;
                // Skip cells that are outside the board
                if (currentX >= 0 && currentX < boardWidth && y >= 0 && y < boardHeight) {
                    grid[y][currentX] = piece.getLabel();
                }
            } else {
                int currentY = y + i;
                // Skip cells that are outside the board
                if (x >= 0 && x < boardWidth && currentY >= 0 && currentY < boardHeight) {
                    grid[currentY][x] = piece.getLabel();
                }
            }
        }
    }

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
}