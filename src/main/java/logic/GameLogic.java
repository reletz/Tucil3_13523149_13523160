package logic;

import java.util.List;

/**
 * Kelas GameLogic buat mengatur operasi logika permainan.
 * Menghandle penempatan dan pergerakan Block di papan.
 */
public class GameLogic {
    
    /**
     * Taruh Block di papan pada posisi tertentu.
     * 
     * @param board Papan permainan
     * @param piece Block yang akan ditaruh
     * @param topLeftX Koordinat X ujung kiri atas piece
     * @param topLeftY Koordinat Y ujung kiri atas piece
     * @return true jika berhasil menaruh Block, false jika gagal (posisi sudah terisi)
     */
    public static boolean placePiece(Board board, Piece piece, int topLeftX, int topLeftY) {
        char[][] grid = board.getGrid();
        boolean[][] shape = piece.getShape();
        
        // Validasi posisi
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j]) {
                    int y = topLeftY + i;
                    int x = topLeftX + j;
                    
                    // Validasi batas papan
                    if (y < 0 || y >= grid.length || x < 0 || x >= grid[0].length) {
                        return false;
                    }
                    
                    // Validasi cell kosong
                    if (grid[y][x] != ' ') {
                        return false;
                    }
                }
            }
        }
        
        // Taruh Block ke papan
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j]) {
                    grid[topLeftY + i][topLeftX + j] = piece.getLabel();
                }
            }
        }
        
        return true;
    }
    
    /**
     * Geser Block ke arah maju (kanan jika horizontal, bawah jika vertikal).
     * 
     * @param gameState Keadaan permainan saat ini
     * @param piece Block yang akan digeser
     * @return true jika berhasil digeser, false jika tidak
     */
    public static boolean moveForward(GameState gameState, Piece piece) {
        Board board = gameState.getBoard();
        char[][] grid = board.getGrid();
        
        // Cari posisi Block
        int[] pos = findPiecePosition(board, piece.getLabel());
        if (pos == null) return false;
        
        int pieceX = pos[0];
        int pieceY = pos[1];
        boolean[][] shape = piece.getShape();
        boolean isHorizontal = piece.isHorizontal();
        
        if (isHorizontal) {
            // Cek apakah bisa geser ke kanan
            int rightmost = pieceX + shape[0].length - 1;
            if (rightmost + 1 >= board.getGrid()[0].length) {
                return false; // Tidak bisa keluar batas papan
            }
            
            // Cek apakah kolom sebelah kanan kosong
            for (int i = 0; i < shape.length; i++) {
                if (shape[i][shape[i].length-1]) { // Hanya cek bagian paling kanan yang ada isinya
                    if (grid[pieceY + i][rightmost + 1] != ' ' && 
                        !(piece instanceof PrimaryPiece && 
                          rightmost + 1 == board.getOutCoordX() && 
                          pieceY + i == board.getOutCoordY())) {
                        return false; // Ada Block lain di sana
                    }
                }
            }
            
            // Geser Block ke kanan
            for (int i = 0; i < shape.length; i++) {
                for (int j = shape[0].length - 1; j >= 0; j--) {
                    if (shape[i][j]) {
                        // Hapus posisi paling kiri yang terisi
                        if (j == 0) grid[pieceY + i][pieceX] = ' ';
                        // Pindahkan ke kanan
                        grid[pieceY + i][pieceX + j + 1] = piece.getLabel();
                    }
                }
            }
        } else {
            // Cek apakah bisa geser ke bawah
            int bottommost = pieceY + shape.length - 1;
            if (bottommost + 1 >= board.getGrid().length) {
                return false; // Tidak bisa keluar batas papan
            }
            
            // Cek apakah baris di bawah kosong
            for (int j = 0; j < shape[0].length; j++) {
                if (shape[shape.length-1][j]) { // Hanya cek bagian paling bawah yang ada isinya
                    if (grid[bottommost + 1][pieceX + j] != ' ' &&
                        !(piece instanceof PrimaryPiece && 
                          pieceX + j == board.getOutCoordX() && 
                          bottommost + 1 == board.getOutCoordY())) {
                        return false; // Ada Block lain di sana
                    }
                }
            }
            
            // Geser Block ke bawah
            for (int j = 0; j < shape[0].length; j++) {
                for (int i = shape.length - 1; i >= 0; i--) {
                    if (shape[i][j]) {
                        // Hapus posisi paling atas yang terisi
                        if (i == 0) grid[pieceY][pieceX + j] = ' ';
                        // Pindahkan ke bawah
                        grid[pieceY + i + 1][pieceX + j] = piece.getLabel();
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Geser Block ke arah mundur (kiri jika horizontal, atas jika vertikal).
     * 
     * @param gameState Keadaan permainan saat ini
     * @param piece Block yang akan digeser
     * @return true jika berhasil digeser, false jika tidak
     */
    public static boolean moveBackward(GameState gameState, Piece piece) {
        Board board = gameState.getBoard();
        char[][] grid = board.getGrid();
        
        // Cari posisi Block
        int[] pos = findPiecePosition(board, piece.getLabel());
        if (pos == null) return false;
        
        int pieceX = pos[0];
        int pieceY = pos[1];
        boolean[][] shape = piece.getShape();
        boolean isHorizontal = piece.isHorizontal();
        
        if (isHorizontal) {
            // Cek apakah bisa geser ke kiri
            if (pieceX - 1 < 0) {
                return false; // Tidak bisa keluar batas papan
            }
            
            // Cek apakah kolom sebelah kiri kosong
            for (int i = 0; i < shape.length; i++) {
                if (shape[i][0]) { // Hanya cek bagian paling kiri yang ada isinya
                    if (grid[pieceY + i][pieceX - 1] != ' ') {
                        return false; // Ada Block lain di sana
                    }
                }
            }
            
            // Geser Block ke kiri
            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[0].length; j++) {
                    if (shape[i][j]) {
                        // Hapus posisi paling kanan yang terisi
                        if (j == shape[0].length - 1) grid[pieceY + i][pieceX + j] = ' ';
                        // Pindahkan ke kiri
                        grid[pieceY + i][pieceX + j - 1] = piece.getLabel();
                    }
                }
            }
        } else {
            // Cek apakah bisa geser ke atas
            if (pieceY - 1 < 0) {
                return false; // Tidak bisa keluar batas papan
            }
            
            // Cek apakah baris di atas kosong
            for (int j = 0; j < shape[0].length; j++) {
                if (shape[0][j]) { // Hanya cek bagian paling atas yang ada isinya
                    if (grid[pieceY - 1][pieceX + j] != ' ') {
                        return false; // Ada Block lain di sana
                    }
                }
            }
            
            // Geser Block ke atas
            for (int j = 0; j < shape[0].length; j++) {
                for (int i = 0; i < shape.length; i++) {
                    if (shape[i][j]) {
                        // Hapus posisi paling bawah yang terisi
                        if (i == shape.length - 1) grid[pieceY + i][pieceX + j] = ' ';
                        // Pindahkan ke atas
                        grid[pieceY + i - 1][pieceX + j] = piece.getLabel();
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Cari posisi Block di papan berdasarkan labelnya.
     * 
     * @param board Papan permainan
     * @param label Label Block yang dicari
     * @return Array berisi koordinat [x, y] dari ujung kiri atas Block, null jika tidak ditemukan
     */
    public static int[] findPiecePosition(Board board, char label) {
        char[][] grid = board.getGrid();
        
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == label) {
                    return new int[]{j, i}; // [x, y]
                }
            }
        }
        
        return null; // Block tidak ditemukan
    }
}