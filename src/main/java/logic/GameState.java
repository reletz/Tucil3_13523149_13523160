package logic;

import java.util.List;

/**
 * Kelas GameState ini buat nampung keadaan permainan.
 * Menyimpan papan dan semua Block yang ada dalam permainan.
 */
public class GameState {
    private final Board board;
    private final List<Piece> pieces;
    private final PrimaryPiece primaryPiece;
    
    /**
     * Bikin GameState baru dengan papan dan Block yang dikasih.
     * 
     * @param board Papan permainan
     * @param pieces Daftar Block biasa
     * @param primaryPiece Block utama
     */
    public GameState(Board board, List<Piece> pieces, PrimaryPiece primaryPiece) {
        this.board = board;
        this.pieces = pieces;
        this.primaryPiece = primaryPiece;
    }
    
    /**
     * Ngambil papan permainan.
     * 
     * @return Papan permainan
     */
    public Board getBoard() {
        return board;
    }
    
    /**
     * Ngambil daftar Block biasa.
     * 
     * @return Daftar Block biasa
     */
    public List<Piece> getPieces() {
        return pieces;
    }
    
    /**
     * Ngambil Block utama.
     * 
     * @return Block utama
     */
    public PrimaryPiece getPrimaryPiece() {
        return primaryPiece;
    }
}