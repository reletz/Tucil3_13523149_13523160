package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logic.Board;
import logic.GameState;
import logic.Piece;
import logic.PrimaryPiece;

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
    
    try {
      // Print file content for debugging
      System.out.println("File content:");
      List<String> allLines = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        allLines.add(line);
        System.out.println("[" + line + "]");
      }
      reader.close();
      
      if (allLines.size() < 3) {
        throw new IOException("File konfigurasi tidak lengkap");
      }
      
      // Baca dimensi papan
      String[] dimensions = allLines.get(0).trim().split("\\s+");
      int rows = Integer.parseInt(dimensions[0]);
      int cols = Integer.parseInt(dimensions[1]);
      
      // Print dimensions for debugging
      System.out.println("Board dimensions: " + rows + "x" + cols);
      
      // Baca jumlah Block biasa
      int nonPrimaryPieceCount = Integer.parseInt(allLines.get(1).trim());
      
      // Pastikan ada cukup baris untuk papan
      // Check if we have enough lines for the board (2 header lines + rows)
      if (allLines.size() - 2 < rows) {
        throw new IOException("Konfigurasi papan tidak lengkap, expected " + rows + 
                              " rows but found " + (allLines.size() - 2));
      }
      
      // Baca konfigurasi papan
      char[][] grid = new char[rows][cols];
      
      // Initialize grid with empty spaces
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
          grid[i][j] = '.';
        }
      }
      
      // Fill grid from file content
      for (int i = 0; i < rows; i++) {
        line = allLines.get(i + 2); // Skip first two lines (dimensions and piece count)
        System.out.println("Processing line " + (i + 3) + ": [" + line + "]");
        
        for (int j = 0; j < Math.min(line.length(), cols); j++) {
          grid[i][j] = line.charAt(j);
        }
      }
      
      // Print grid for verification
      System.out.println("\nParsed grid:");
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
          System.out.print(grid[i][j]);
        }
        System.out.println();
      }
      
        // Cari koordinat pintu keluar
        int exitX = -1;
        int exitY = -1;

        // Scan original input lines for K
        for (int i = 0; i < rows; i++) {
        String inputLine = allLines.get(i + 2); // Skip first two header lines
        int kIndex = inputLine.indexOf('K');
        if (kIndex != -1) {
            // Found K in this line
            exitX = kIndex;
            exitY = i;
            
            // Check if K is outside the grid (as it should be)
            if (kIndex >= cols) {
            System.out.println("Exit ('K') found outside grid at position (" + exitX + "," + exitY + ")");
            } else {
            System.out.println("Warning: Exit ('K') found inside grid at position (" + exitX + "," + exitY + ")");
            // Still use it, for compatibility with input files that place K inside the grid
            }
            break;
        }
        }
        
      // Buat papan permainan
      Board board = new Board(rows, cols, exitX, exitY);
      board.setGrid(grid); // Set grid to the board
      
      // Identifikasi dan buat semua Block
      Map<Character, List<int[]>> pieceCoordinates = new HashMap<>();
      
      // Kumpulkan koordinat untuk setiap label Block
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
          char cell = grid[i][j];
          if (cell != '.' && cell != 'K') {
            pieceCoordinates.computeIfAbsent(cell, k -> new ArrayList<>())
                        .add(new int[]{j, i});
          }
        }
      }
      
      // Debug output
      System.out.println("\nDetected pieces:");
      for (Map.Entry<Character, List<int[]>> entry : pieceCoordinates.entrySet()) {
        System.out.println("Piece " + entry.getKey() + ": " + entry.getValue().size() + " cells");
      }
      
      // Prepare for creating GameState with new structure
      List<Piece> pieces = new ArrayList<>();
      List<Integer> positionsX = new ArrayList<>();
      List<Integer> positionsY = new ArrayList<>();
      PrimaryPiece primaryPiece = null;
      int primaryX = -1;
      int primaryY = -1;
  
      // Process each piece
      for (Map.Entry<Character, List<int[]>> entry : pieceCoordinates.entrySet()) {
        char label = entry.getKey();
        List<int[]> coords = entry.getValue();
        
        // Determine orientation and size
        boolean isHorizontal;
        int size = coords.size(); // Size is the number of cells
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        
        // Find top-left corner
        for (int[] coord : coords) {
          minX = Math.min(minX, coord[0]);
          minY = Math.min(minY, coord[1]);
        }
        
        // Check if horizontal (all Y values are the same)
        boolean sameY = true;
        int baseY = coords.get(0)[1];
        
        for (int[] coord : coords) {
          if (coord[1] != baseY) {
            sameY = false;
            break;
          }
        }
        
        isHorizontal = sameY;
        
        // Debug output for each piece
        System.out.println("Processing piece " + label + ": size=" + size + 
                          ", isHorizontal=" + isHorizontal + 
                          ", position=(" + minX + "," + minY + ")");
        
        // Create piece
        if (label == 'P') {
          primaryPiece = new PrimaryPiece(label, size, isHorizontal);
          primaryX = minX;
          primaryY = minY;
        } else {
          Piece piece = new Piece(label, size, isHorizontal);
          pieces.add(piece);
          positionsX.add(minX);
          positionsY.add(minY);
        }
      }
      
      // Check if primary piece was found
      if (primaryPiece == null) {
        throw new IOException("Primary piece (P) not found in the configuration");
      }
      
      // Create and return the GameState with the new structure
      return new GameState(board, pieces, positionsX, positionsY, primaryPiece, primaryX, primaryY);
    } catch (Exception e) {
      System.out.println("Exception details: " + e);
      e.printStackTrace();
      throw new IOException("Error parsing configuration: " + e.getMessage(), e);
    }
  }
}