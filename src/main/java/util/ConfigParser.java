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
      
      // Collect all content lines (skip header lines)
      List<String> contentLines = new ArrayList<>();
      for (int i = 2; i < allLines.size(); i++) {
        contentLines.add(allLines.get(i));
      }
      
      // Initialize grid with empty spaces
      char[][] grid = new char[rows][cols];
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
          grid[i][j] = '.';
        }
      }
      
      // First scan to find the exit position and side
      int exitX = -1;
      int exitY = -1;
      int exitSide = -1; // 0=top, 1=right, 2=bottom, 3=left
      boolean hasLeftExit = false;
      boolean hasTopExit = false;
      boolean hasBottomExit = false;
      boolean hasRightExit = false;
      
      // STEP 1: Find exits outside the grid
      
      // Check for TOP exit (separate line before the board)
      if (!contentLines.isEmpty() && isExitLine(contentLines.get(0))) {
        hasTopExit = true;
        String topLine = contentLines.get(0);
        exitX = topLine.indexOf('K');
        exitY = -1;
        exitSide = 0; // Top
        System.out.println("TOP EXIT found at column " + exitX);
        contentLines.remove(0); // Remove this line so we don't process it as part of the board
      }
      
      // Check for BOTTOM exit (separate line after the board)
      if (contentLines.size() > rows && isExitLine(contentLines.get(rows))) {
        hasBottomExit = true;
        String bottomLine = contentLines.get(rows);
        exitX = bottomLine.indexOf('K');
        exitY = rows;
        exitSide = 2; // Bottom
        System.out.println("BOTTOM EXIT found at column " + exitX);
        // We'll skip this line when processing the board
      }
      
      // STEP 2: Process the actual board content
      List<String> boardLines = new ArrayList<>();
      
      // Take only the rows we need for the board
      for (int i = 0; i < Math.min(rows, contentLines.size()); i++) {
        boardLines.add(contentLines.get(i));
      }
      
      // STEP 3: Find exits in the board content
      
      // Check for LEFT EXIT in any row
      for (int i = 0; i < boardLines.size(); i++) {
        String rowLine = boardLines.get(i);
        if (rowLine.trim().startsWith("K")) {
          hasLeftExit = true;
          exitX = -1;
          exitY = i;
          exitSide = 3; // Left
          System.out.println("LEFT EXIT found at row " + i);
          
          // Update boardLine to remove K
          boardLines.set(i, rowLine.replaceFirst("K", " "));
          break;
        }
      }
      
      // Check for RIGHT EXIT in any row
      if (exitSide == -1) {
        for (int i = 0; i < boardLines.size(); i++) {
          String rowLine = boardLines.get(i);
          int kIndex = rowLine.indexOf('K');
          
          if (kIndex != -1 && (kIndex == rowLine.length() - 1 || kIndex >= cols)) {
            hasRightExit = true;
            exitX = cols;
            exitY = i;
            exitSide = 1; // Right
            System.out.println("RIGHT EXIT found at row " + i);
            
            // Update boardLine to remove K
            boardLines.set(i, rowLine.substring(0, kIndex));
            break;
          }
        }
      }
      
      // Check if we found an exit
      if (exitSide == -1) {
        throw new IOException("Exit ('K') not found in configuration");
      }
      
      // STEP 4: Fill the grid from board lines, handling left exit for all rows
      for (int i = 0; i < Math.min(rows, boardLines.size()); i++) {
        String rowContent = boardLines.get(i);
        
        // If has left exit, skip first character of EVERY row
        if (hasLeftExit) {
          if (rowContent.length() > 0) {
            rowContent = rowContent.substring(Math.min(1, rowContent.length()));
          }
        }
        
        // Fill grid from the processed content, skiping spaces and K
        for (int j = 0; j < Math.min(rowContent.length(), cols); j++) {
          if (j < rowContent.length()) {
            char c = rowContent.charAt(j);
            if (c != 'K' && c != ' ') {
              grid[i][j] = c;
            } else if (c == ' ') {
              grid[i][j] = '.'; // Replace spaces with dots
            }
          }
        }
      }
      
      // Print final grid for verification
      System.out.println("\nFinal grid (without K):");
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
          System.out.print(grid[i][j]);
        }
        System.out.println();
      }
      
      // Create board with exit information
      Board board = new Board(rows, cols, exitX, exitY, exitSide);
      board.setGrid(grid);
      
      // Debug exit information
      System.out.println("Exit position: (" + exitX + "," + exitY + "), exit side: " + exitSide);
      
      // Identifikasi dan buat semua Block
      Map<Character, List<int[]>> pieceCoordinates = new HashMap<>();
      
      // Kumpulkan koordinat untuk setiap label Block
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
          char cell = grid[i][j];
          if (cell != '.' && cell != ' ' && cell != 'K') {
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
          
          // Check if primary piece orientation is compatible with exit side
          boolean validOrientation = 
              (isHorizontal && (exitSide == 1 || exitSide == 3)) || // Horizontal piece with left/right exit
              (!isHorizontal && (exitSide == 0 || exitSide == 2));  // Vertical piece with top/bottom exit
          
          if (!validOrientation) {
            System.out.println("Warning: Primary piece orientation (" + 
                              (isHorizontal ? "horizontal" : "vertical") + 
                              ") may not be compatible with exit side (" + exitSide + ")");
          }
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
      
      // Create and return the GameState with the original structure
      return new GameState(board, pieces, positionsX, positionsY, primaryPiece, primaryX, primaryY);
    } catch (Exception e) {
      System.out.println("Exception details: " + e);
      e.printStackTrace();
      throw new IOException("Error parsing configuration: " + e.getMessage(), e);
    }
  }
  
  /**
   * Checks if a line contains only 'K' and spaces (exit line)
   */
  private static boolean isExitLine(String line) {
    if (line == null || line.isEmpty()) return false;
    
    line = line.trim();
    if (line.equals("K")) return true; // Simple case - just "K"
    
    boolean hasK = false;
    for (char c : line.toCharArray()) {
      if (c == 'K') hasK = true;
      else if (c != ' ') return false; // Contains character other than K or space
    }
    
    return hasK; // True if it has K and only spaces otherwise
  }
}