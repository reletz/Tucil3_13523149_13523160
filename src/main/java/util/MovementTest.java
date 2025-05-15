package util;

import logic.Board;
import logic.GameLogic;
import logic.GameState;
import logic.Piece;
import logic.PrimaryPiece;

import java.util.List;

/**
 * Kelas untuk menguji fungsi pergerakan
 */
public class MovementTest {
    public static void main(String[] args) {
        // Buat board dari file konfigurasi
        GameState gameState = createTestBoard();
        if (gameState == null) {
            System.out.println("Gagal membuat papan uji.");
            return;
        }
        
        System.out.println("==== TEST PERGERAKAN BLOCK ====");
        System.out.println("Konfigurasi awal:");
        printBoardState(gameState);
        
        // Uji pergerakan valid
        testValidMovements(gameState);
        
        // Buat ulang board untuk test invalid
        gameState = createTestBoard();
        
        // Uji pergerakan invalid
        testInvalidMovements(gameState);
    }
    
    private static GameState createTestBoard() {
        try {
            // Baca dari file konfigurasi yang sudah ada
            String configPath = GameManager.getResourcePath("config/testConfig.txt");
            return ConfigParser.parseConfig(configPath);
        } catch (Exception e) {
            System.out.println("Error membuat papan test: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static void printBoardState(GameState gameState) {
        Board board = gameState.getBoard();
        System.out.println("Pintu keluar di: (" + board.getOutCoordX() + ", " + board.getOutCoordY() + ")");
        System.out.println("Konfigurasi papan:");
        board.printBoard();
        System.out.println();
    }
    
    private static void testValidMovements(GameState gameState) {
        System.out.println("==== UJI PERGERAKAN VALID ====");
        
        // 1. Gerakkan Block F ke bawah (vertical)
        Piece pieceF = findPieceByLabel(gameState, 'F');
        testMove(gameState, pieceF, "F ke bawah (forward)", true);
        
        // 2. Gerakkan Block B ke kanan (horizontal)
        Piece pieceB = findPieceByLabel(gameState, 'B');
        testMove(gameState, pieceB, "B ke kanan (forward)", true);
        
        // 3. Gerakkan Block P (Primary) ke kanan
        PrimaryPiece primaryPiece = gameState.getPrimaryPiece();
        testMove(gameState, primaryPiece, "P (Primary) ke kanan (forward)", true);
        
        // 4. Gerakkan Block I ke kiri (backward)
        Piece pieceI = findPieceByLabel(gameState, 'I');
        testMove(gameState, pieceI, "I ke kiri (backward)", false);
        
        // 5. Gerakkan Block H ke atas (backward)
        Piece pieceH = findPieceByLabel(gameState, 'H');
        testMove(gameState, pieceH, "H ke atas (backward)", false);
    }

    private static void testInvalidMovements(GameState gameState) {
        System.out.println("==== UJI PERGERAKAN INVALID ====");
        
        // 1. Coba gerakkan Block A ke kanan (terhalang block B)
        Piece pieceA = findPieceByLabel(gameState, 'A');
        testMove(gameState, pieceA, "A ke kanan (forward) - terhalang B", true);
        
        // 2. Coba gerakkan Block L ke kanan (terhalang block J)
        Piece pieceL = findPieceByLabel(gameState, 'L');
        testMove(gameState, pieceL, "L ke kanan (forward) - terhalang J", true);
        
        // 3. Coba gerakkan Block C ke atas (terhalang block B)
        Piece pieceC = findPieceByLabel(gameState, 'C');
        testMove(gameState, pieceC, "C ke atas (backward) - terhalang B", false);
        
        // 4. Coba gerakkan Block G ke atas (keluar batas papan)
        Piece pieceG = findPieceByLabel(gameState, 'G');
        testMove(gameState, pieceG, "G ke atas (backward) - keluar batas", false);
        
        // 5. Coba gerakkan Block M ke kanan (keluar batas papan)
        Piece pieceM = findPieceByLabel(gameState, 'M');
        testMove(gameState, pieceM, "M ke kanan (forward) - keluar batas", true);
    }
    
    private static void testMove(GameState gameState, Piece piece, String description, boolean isForward) {
        System.out.println("Mencoba gerakan: " + description);
        
        boolean result;
        if (isForward) {
            result = GameLogic.moveForward(gameState, piece);
        } else {
            result = GameLogic.moveBackward(gameState, piece);
        }
        
        System.out.println("Hasil: " + (result ? "BERHASIL" : "GAGAL"));
        printBoardState(gameState);
    }
    
    private static Piece findPieceByLabel(GameState gameState, char label) {
        // Cek primary piece dulu
        PrimaryPiece primaryPiece = gameState.getPrimaryPiece();
        if (primaryPiece.getLabel() == label) {
            return primaryPiece;
        }
        
        // Cari di pieces lain
        List<Piece> pieces = gameState.getPieces();
        for (Piece piece : pieces) {
            if (piece.getLabel() == label) {
                return piece;
            }
        }
        
        return null;
    }
}