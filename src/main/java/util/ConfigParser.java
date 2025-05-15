package util;

import logic.Board;
import logic.GameState;
import logic.GameLogic;
import logic.Piece;
import logic.PrimaryPiece;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kelas ConfigParser buat baca file konfigurasi.
 * Instantiate Board dan Pieces dari file txt.
 */
public class ConfigParser {
    /**
     * Baca file konfigurasi permainan dan instantiate Board serta Pieces.
     * 
     * @param filePath Lokasi file konfigurasi
     * @return Objek GameState yang berisi Board dan Piece
     * @throws IOException Kalo ada error pas baca file
     */
    public static GameState parseConfig(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        
        // Baca dimensi papan
        String[] dimensions = reader.readLine().trim().split("\\s+");
        int rows = Integer.parseInt(dimensions[0]);
        int cols = Integer.parseInt(dimensions[1]);
        
        // Baca jumlah Block biasa
        int nonPrimaryPieceCount = Integer.parseInt(reader.readLine().trim());
        
        // Baca konfigurasi papan
        // Save dulu setiap line papan ke List of boardLines
        List<String> boardLines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                boardLines.add(line);
            }
        }
        reader.close();
        
        // Cari koordinat pintu keluar
        int kX = -1, kY = -1;
        for (int i = 0; i < boardLines.size(); i++) {
            String rowString = boardLines.get(i);
            int kIndex = rowString.indexOf('K');
            if (kIndex != -1) {
                kX = kIndex;
                kY = i;
                break;
            }
        }
        
        // Buat papan permainan
        Board board = new Board(rows, cols, kX, kY);
        
        // Identifikasi dan buat semua Block
        Map<Character, List<int[]>> pieceCoordinates = new HashMap<>();
        
        // Kumpulkan koordinat untuk setiap label Block
        for (int i = 0; i < boardLines.size(); i++) {
            String rowString = boardLines.get(i);
            for (int j = 0; j < rowString.length(); j++) {
                char cell = rowString.charAt(j);
                if (cell != '.' && cell != 'K') {
                    pieceCoordinates.computeIfAbsent(cell, k -> new ArrayList<>())
                                    .add(new int[]{j, i});
                }
            }
        }
        
        // Buat Block dari koordinat yang dikumpulkan
        List<Piece> pieces = new ArrayList<>();
        PrimaryPiece primaryPiece = null;

        for (Map.Entry<Character, List<int[]>> entry : pieceCoordinates.entrySet()) {
            char label = entry.getKey();
            List<int[]> coords = entry.getValue();
            
            // Tentukan bentuk dan orientasi Block
            boolean[][] shape = determineShape(coords);
            
            // Cari posisi top-left
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            for (int[] coord : coords) {
                minX = Math.min(minX, coord[0]);
                minY = Math.min(minY, coord[1]);
            }
            
            // Buat Block
            if (label == 'P') {
                primaryPiece = new PrimaryPiece(label, shape, true);
                GameLogic.placePiece(board, primaryPiece, minX, minY);
            } else {
                Piece piece = new Piece(label, shape);
                pieces.add(piece);
                GameLogic.placePiece(board, piece, minX, minY);
            }
        }
        
        return new GameState(board, pieces, primaryPiece);
    }
    
    /**
     * Tentukan bentuk Block berdasarkan koordinatnya
     */
    private static boolean[][] determineShape(List<int[]> coords) {
        // Cari dimensi Block
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        
        for (int[] coord : coords) {
            minX = Math.min(minX, coord[0]);
            minY = Math.min(minY, coord[1]);
            maxX = Math.max(maxX, coord[0]);
            maxY = Math.max(maxY, coord[1]);
        }
        
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        
        // Buat matriks bentuk
        boolean[][] shape = new boolean[height][width];
        
        // Tandai sel yang terisi
        for (int[] coord : coords) {
            int relX = coord[0] - minX;
            int relY = coord[1] - minY;
            shape[relY][relX] = true;
        }
        
        return shape;
    }
    
    /**
     * Taruh Block di papan pada koordinat yang ditentukan
     */
    private static void placePieceOnBoard(Board board, Piece piece, List<int[]> coords) {
        char[][] grid = board.getGrid();
        for (int[] coord : coords) {
            grid[coord[1]][coord[0]] = piece.getLabel();
        }
    }
}