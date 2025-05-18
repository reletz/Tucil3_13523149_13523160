package logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Kelas GameState ini buat nampung keadaan permainan.
 * Menyimpan papan dan semua Block yang ada dalam permainan.
 */
public class GameState {
    private final Board board;
    private final Map<Character, PieceState> pieces;
    private final PieceState primaryPieceState;

    /**
     * Class representing a piece's state including its position
     */
    public static class PieceState {
        private final Piece piece;
        private final int x;
        private final int y;

        /**
         * Konstruktor untuk PieceState.
         * 
         * @param piece Block yang direpresentasikan
         * @param x Koordinat X dari ujung kiri atas Block
         * @param y Koordinat Y dari ujung kiri atas Block
         */
        public PieceState(Piece piece, int x, int y) {
            this.piece = piece;
            this.x = x;
            this.y = y;
        }

        /**
         * Mengambil Block yang direpresentasikan.
         * 
         * @return Block
         */
        public Piece getPiece() {
            return piece;
        }

        /**
         * Mengambil koordinat X dari ujung kiri atas Block.
         * 
         * @return Koordinat X
         */
        public int getX() {
            return x;
        }

        /**
         * Mengambil koordinat Y dari ujung kiri atas Block.
         * 
         * @return Koordinat Y
         */
        public int getY() {
            return y;
        }

        /**
         * Memeriksa apakah Block ini menempati sel tertentu.
         * 
         * @param checkX Koordinat X yang diperiksa
         * @param checkY Koordinat Y yang diperiksa
         * @return true jika Block menempati sel tersebut, false jika tidak
         */
        public boolean occupies(int checkX, int checkY) {
            if (piece.isHorizontal()) {
                return checkY == y && checkX >= x && checkX < x + piece.getSize();
            } else {
                return checkX == x && checkY >= y && checkY < y + piece.getSize();
            }
        }

        /**
         * Membandingkan PieceState ini dengan objek lain.
         * 
         * @param o Objek yang dibandingkan
         * @return true jika sama, false jika tidak
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PieceState that = (PieceState) o;
            return x == that.x && 
                   y == that.y && 
                   Objects.equals(piece, that.piece);
        }

        /**
         * Menghasilkan hash code untuk PieceState ini.
         * 
         * @return Hash code
         */
        @Override
        public int hashCode() {
            return Objects.hash(piece, x, y);
        }
    }
    
    /**
     * Bikin GameState baru dengan papan, daftar Block, dan Block utama
     * 
     * @param board Papan permainan
     * @param pieces Map dari label Block ke PieceState-nya
     * @param primaryPieceState PieceState dari Block utama
     */
    public GameState(Board board, Map<Character, PieceState> pieces, PieceState primaryPieceState) {
        this.board = board;
        this.pieces = pieces;
        this.primaryPieceState = primaryPieceState;
    }

    /**
     * Convenience constructor yang menerima List<Piece> dan mengubahnya jadi Map
     * 
     * @param board Papan permainan
     * @param pieces Daftar Block biasa
     * @param positionsX Daftar koordinat X untuk setiap Block
     * @param positionsY Daftar koordinat Y untuk setiap Block
     * @param primaryPiece Block utama
     * @param primaryX Koordinat X Block utama
     * @param primaryY Koordinat Y Block utama
     */
    public GameState(Board board, List<Piece> pieces, List<Integer> positionsX, 
                    List<Integer> positionsY, PrimaryPiece primaryPiece, 
                    int primaryX, int primaryY) {
        this.board = board;
        
        // Convert lists to map with positions
        this.pieces = new HashMap<>();
        for (int i = 0; i < pieces.size(); i++) {
            Piece piece = pieces.get(i);
            this.pieces.put(piece.getLabel(), new PieceState(piece, positionsX.get(i), positionsY.get(i)));
        }
        
        this.primaryPieceState = new PieceState(primaryPiece, primaryX, primaryY);
    }
    
    /**
     * Mengambil papan permainan.
     * 
     * @return Papan permainan
     */
    public Board getBoard() {
        return board;
    }
    
    /**
     * Mengambil Map dari label Block ke PieceState-nya.
     * 
     * @return Map dari label Block ke PieceState-nya
     */
    public Map<Character, PieceState> getPieces() {
        return pieces;
    }
    
    /**
     * Mengambil PieceState berdasarkan label Block.
     * 
     * @param label Label Block yang dicari
     * @return PieceState dari Block tersebut, null jika tidak ditemukan
     */
    public PieceState getPieceState(char label) {
        return pieces.get(label);
    }
    
    /**
     * Mengambil PieceState dari Block utama.
     * 
     * @return PieceState dari Block utama
     */
    public PieceState getPrimaryPieceState() {
        return primaryPieceState;
    }
    
    /**
     * Cek apakah ada Block yang menempati sel tertentu
     * 
     * @param x Koordinat X yang diperiksa
     * @param y Koordinat Y yang diperiksa
     * @return true jika ada Block yang menempati sel tersebut, false jika tidak
     */
    public boolean isCellOccupied(int x, int y) {
        // Check if primary piece occupies this cell
        if (primaryPieceState.occupies(x, y)) {
            return true;
        }
        
        // Check if any other piece occupies this cell
        for (PieceState pieceState : pieces.values()) {
            if (pieceState.occupies(x, y)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Method for detecting collisions when attempting to move a piece
     * 
     * @param pieceLabel Label Block yang akan dipindahkan
     * @param newX Koordinat X baru
     * @param newY Koordinat Y baru
     * @return true jika akan terjadi tabrakan, false jika tidak
     */
    public boolean wouldCollide(char pieceLabel, int newX, int newY) {
        // Get the piece that's moving
        PieceState pieceToMove = null;
        if (primaryPieceState.getPiece().getLabel() == pieceLabel) {
            pieceToMove = primaryPieceState;
        } else {
            pieceToMove = pieces.get(pieceLabel);
        }
        
        if (pieceToMove == null) return true; // Piece not found
        
        Piece piece = pieceToMove.getPiece();
        
        // Check each cell the piece would occupy in the new position
        for (int i = 0; i < piece.getSize(); i++) {
            int checkX = piece.isHorizontal() ? newX + i : newX;
            int checkY = piece.isHorizontal() ? newY : newY + i;
            
            // Skip cells that the piece already occupies in its current position
            if (pieceToMove.occupies(checkX, checkY)) {
                continue;
            }
            
            // Check if any other piece occupies this cell
            if (isCellOccupied(checkX, checkY)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Memeriksa apakah Block utama sudah mencapai pintu keluar.
     * Critically important to determine when the puzzle is solved!
     * 
     * @return true jika Block utama sudah mencapai pintu keluar, false jika belum
     */
    public boolean isPrimaryPieceAtExit() {
        int exitX = board.getOutCoordX();
        int exitY = board.getOutCoordY();
        int exitSide = board.getExitSide(); // Get exit side: 0=top, 1=right, 2=bottom, 3=left
        
        // Use Piece instead of PrimaryPiece to avoid ClassCastException
        Piece primaryPiece = primaryPieceState.getPiece();
        int primaryX = primaryPieceState.getX();
        int primaryY = primaryPieceState.getY();
        
        // Debug output when checking goal state
        System.out.println("Checking goal: Primary at (" + primaryX + "," + primaryY + 
                        "), size=" + primaryPiece.getSize() + 
                        ", Exit at (" + exitX + "," + exitY + 
                        "), Exit side=" + exitSide);
        
        // Check based on exit side
        switch (exitSide) {
            case 0: // Top exit
                // Primary piece must be vertical and its top edge at y=0
                if (!primaryPiece.isHorizontal()) {
                    boolean isGoal = (primaryX == exitX) && (primaryY <= 0);
                    System.out.println("Top exit check: primary at (" + primaryX + "," + primaryY + 
                                "), goal=" + isGoal);
                    return isGoal;
                }
                return false;
                
            case 1: // Right exit (standard)
                // Primary piece must be horizontal and its right edge reaching the exit
                if (primaryPiece.isHorizontal()) {
                    int rightEdgeX = primaryX + primaryPiece.getSize() - 1;
                    boolean isGoal = (rightEdgeX >= board.getGrid()[0].length - 1) && (primaryY == exitY);
                    System.out.println("Right exit check: right edge at " + rightEdgeX + ", goal=" + isGoal);
                    return isGoal;
                }
                return false;
                
            case 2: // Bottom exit
                // Primary piece must be vertical and its bottom edge reaching the exit
                if (!primaryPiece.isHorizontal()) {
                    int bottomEdgeY = primaryY + primaryPiece.getSize() - 1;
                    boolean isGoal = (primaryX == exitX) && (bottomEdgeY >= board.getGrid().length - 1);
                    System.out.println("Bottom exit check: bottom edge at " + bottomEdgeY + ", goal=" + isGoal);
                    return isGoal;
                }
                return false;
                
            case 3: // Left exit
                // Primary piece must be horizontal and its left edge at x=0 or negative
                if (primaryPiece.isHorizontal()) {
                    boolean isGoal = (primaryX <= 0) && (primaryY == exitY);
                    System.out.println("Left exit check: left edge at " + primaryX + ", goal=" + isGoal);
                    return isGoal;
                }
                return false;
                
            default:
                System.out.println("Invalid exit side: " + exitSide);
                return false;
        }
    }

    /**
     * Membandingkan GameState ini dengan objek lain.
     * 
     * @param o Objek yang dibandingkan
     * @return true jika sama, false jika tidak
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameState that = (GameState) o;
        return Objects.equals(primaryPieceState, that.primaryPieceState) && 
               Objects.equals(pieces, that.pieces);
    }

    /**
     * Menghasilkan hash code untuk GameState ini.
     * 
     * @return Hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(primaryPieceState, pieces);
    }
}