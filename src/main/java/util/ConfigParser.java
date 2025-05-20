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
      // System.out.println("File content:");
      List<String> allLines = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        allLines.add(line);
        // System.out.println("[" + line + "]");
      }
      reader.close();
      
      if (allLines.size() < 3) {
        throw new IOException("Incomplete config file");
      }
      
      // Baca dimensi papan
      String[] dimensions = allLines.get(0).trim().split("\\s+");
      int rows = Integer.parseInt(dimensions[0]);
      int cols = Integer.parseInt(dimensions[1]);
      
      if (rows <= 1 || cols <= 1){
        throw new IOException("Board dimension must be greater than 2!");
      }
      
      // Baca jumlah Block biasa
      int nonPrimaryPieceCount = Integer.parseInt(allLines.get(1).trim());

      if (nonPrimaryPieceCount < 0){
        throw new IOException("The nonPrimary block cannot be negative!");
      }
      
      // Collect all content lines (skip header lines)
      List<String> contentLines = new ArrayList<>();
      for (int i = 2; i < allLines.size(); i++) {
        contentLines.add(allLines.get(i).toUpperCase());
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
        contentLines.remove(0); // Remove this line so we don't process it as part of the board
      }
      
      // Check for BOTTOM exit (separate line after the board)
      if (contentLines.size() > rows && isExitLine(contentLines.get(rows))) {
        hasBottomExit = true;
        String bottomLine = contentLines.get(rows);
        exitX = bottomLine.indexOf('K');
        exitY = rows;
        exitSide = 2; // Bottom
      }
      
      // STEP 2: Process the actual board content
      List<String> boardLines = new ArrayList<>();
      
      // Take only the rows we need for the board
      for (int i = 0; i < Math.min(rows, contentLines.size()); i++) {
        boardLines.add(contentLines.get(i));
      }

      // Validation: K must not be in Board (harus di luar grid)
      boolean leftExitHasSpace = false;

      // Pertama cek apakah baris lain memiliki spasi awalan (untuk left exit)
      for (int i = 0; i < boardLines.size(); i++) {
        String rowLine = boardLines.get(i);
        
        // Jika baris dimulai dengan spasi, catat
        if (!rowLine.startsWith(" ")) {
          leftExitHasSpace = false;
          break;
        } else leftExitHasSpace = true;
      }

      int kCount = 0;
      for (int i = 0; i < boardLines.size(); i++) {
        String rowLine = boardLines.get(i);
        int kIndex = rowLine.indexOf('K');
        
        if (kIndex != -1) {
          kCount++;
          
          // K hanya valid di posisi:
          // - Kolom pertama (indeks 0) HANYA jika akan diproses sebagai left exit DAN baris lain memiliki spasi awalan
          // - Kolom terakhir (>= cols atau == rowLine.length()-1) HANYA jika akan diproses sebagai right exit
          boolean isValidLeftExitPosition = (kIndex == 0 && leftExitHasSpace);
          boolean isValidRightExitPosition = (kIndex >= cols || kIndex == rowLine.length()-1);
          
          // K hanya valid di tepi, tidak boleh di dalam papan
          if (!isValidLeftExitPosition && !isValidRightExitPosition) {
            throw new IOException("Invalid exit position: Exit marker 'K' must be outside the board. " + 
                                "Found at row " + (i+1) + ", column " + (kIndex+1) + ".");
          }
          
          // Jika K di posisi left exit, pastikan ini adalah awal baris (tidak ada karakter lain di kiri)
          if (kIndex == 0 && !leftExitHasSpace) {
            throw new IOException("Invalid left exit configuration: For left exit, other rows must have " +  "leading spaces to position the exit outside the board.");
          }
        }
      }
      
      if (kCount > 1) {
        throw new IOException("Invalid configuration: Multiple exit markers ('K') found. Only one exit is allowed.");
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
      // System.out.println("\nFinal grid (without K):");
      // for (int i = 0; i < rows; i++) {
      //   for (int j = 0; j < cols; j++) {
      //     System.out.print(grid[i][j]);
      //   }
      //   System.out.println();
      // }
      
      // Create board with exit information
      Board board = new Board(rows, cols, exitX, exitY, exitSide);
      board.setGrid(grid);
      
      // Debug exit information
      // System.out.println("Exit position: (" + exitX + "," + exitY + "), exit side: " + exitSide);
      
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
      // System.out.println("\nDetected pieces:");
      // for (Map.Entry<Character, List<int[]>> entry : pieceCoordinates.entrySet()) {
      //   System.out.println("Piece " + entry.getKey() + ": " + entry.getValue().size() + " cells");
      // }
      
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

        // Semua koordinat piece harus terhubung
        boolean isConnected = validateConnectedPiece(coords);
        if (!isConnected) {
          throw new IOException("Found disconnected piece parts with same label '" + label + "'. Each piece must be contiguous.");
        }
        
        // Determine orientation and size
        boolean isHorizontal;
        int size = coords.size(); // Size is the number of cells
        if (size < 2) {
          throw new IOException("Piece length has to be more than 1 block length!");
        }
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
            throw new IOException("Primary piece orientation (" + 
                              (isHorizontal ? "horizontal" : "vertical") + 
                              ") is not compatible with exit side (" + exitSide + ")");
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
      throw new IOException("Error parsing configuration: " + e.getMessage(), e);
    }
  }
  
  /**
   * Checks if a line contains only 'K' and spaces (exit line)
   */
  private static boolean isExitLine(String line) {
    if (line == null || line.isEmpty()) return false;
    
    line = line.trim().toUpperCase();
    if (line.equals("K")) return true; // Simple case - just "K"
    
    boolean hasK = false;
    for (char c : line.toCharArray()) {
      if (c == 'K') hasK = true;
      else if (c != ' ') return false; // Contains character other than K or space
    }
    
    return hasK; // True if it has K and only spaces otherwise
  }

    /**
   * Validates that all coordinates for a piece are connected
   */
  private static boolean validateConnectedPiece(List<int[]> coords) {
    if (coords.size() <= 1) return true;
    
    // Cek apakah semua koordinat dalam satu garis (horizontal atau vertikal)
    boolean isHorizontal = true;
    boolean isVertical = true;
    int baseY = coords.get(0)[1];
    int baseX = coords.get(0)[0];
    
    for (int[] coord : coords) {
      if (coord[1] != baseY) {
        isHorizontal = false;
      }
      if (coord[0] != baseX) {
        isVertical = false;
      }
    }
    
    // Jika tidak semua dalam satu garis, berarti tidak terhubung
    if (!isHorizontal && !isVertical) {
      return false;
    }
    
    // Jika horizontal, semua koordinat X harus berurutan tanpa celah
    if (isHorizontal) {
      // Sort by X coordinate
      coords.sort((a, b) -> Integer.compare(a[0], b[0]));
      for (int i = 1; i < coords.size(); i++) {
        if (coords.get(i)[0] != coords.get(i-1)[0] + 1) {
          return false; // Ada celah
        }
      }
      return true;
    }
    
    // Jika vertical, semua koordinat Y harus berurutan tanpa celah
    if (isVertical) {
      // Sort by Y coordinate
      coords.sort((a, b) -> Integer.compare(a[1], b[1]));
      for (int i = 1; i < coords.size(); i++) {
        if (coords.get(i)[1] != coords.get(i-1)[1] + 1) {
          return false; // Ada celah
        }
      }
      return true;
    }
    
    return false;
  }
}