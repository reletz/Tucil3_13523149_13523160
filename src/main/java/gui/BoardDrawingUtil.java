package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 * Shared drawing utilities for board visualization
 */
public class BoardDrawingUtil {
    
    /**
     * Draw a game board with consistent styling
     * 
     * @param g Graphics context to draw on
     * @param grid Board grid data
     * @param rows Number of rows
     * @param cols Number of columns
     * @param exitX Exit X coordinate
     * @param exitY Exit Y coordinate
     * @param exitSide Exit side (0=top, 1=right, 2=bottom, 3=left)
     * @param targetComponent Component to use for sizing
     */
    public static void drawBoard(Graphics g, char[][] grid, int rows, int cols,
                             int exitX, int exitY, int exitSide, Component targetComponent) {
        // Add padding of 1 cell on each side for exit visibility
        int displayRows = rows + 2;  // +2 for top and bottom padding
        int displayCols = cols + 2;  // +2 for left and right padding
        
        // Calculate cell size based on padded dimensions
        int cellSize = Math.min(targetComponent.getWidth() / displayCols, 
                           targetComponent.getHeight() / displayRows);
        
        // Drawing offset to center the padded board
        int offsetX = (targetComponent.getWidth() - (displayCols * cellSize)) / 2;
        int offsetY = (targetComponent.getHeight() - (displayRows * cellSize)) / 2;
        
        // Draw background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, targetComponent.getWidth(), targetComponent.getHeight());
        
        // Draw actual board area with darker lines
        g.setColor(Color.BLACK);
        for (int i = 1; i <= rows + 1; i++) {
            g.drawLine(offsetX + cellSize, offsetY + i * cellSize, 
                     offsetX + (cols + 1) * cellSize, offsetY + i * cellSize);
        }
        for (int j = 1; j <= cols + 1; j++) {
            g.drawLine(offsetX + j * cellSize, offsetY + cellSize, 
                     offsetX + j * cellSize, offsetY + (rows + 1) * cellSize);
        }
        
        // Draw pieces on the board (with offset for padding)
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char cell = grid[i][j];
                if (cell != '.' && cell != ' ') {
                    // Get color using the PieceColorManager
                    Color pieceColor = gui.PieceColorManager.getColorForPiece(cell);
                    
                    // Draw the piece
                    g.setColor(pieceColor);
                    g.fillRect(offsetX + (j+1) * cellSize + 1, 
                              offsetY + (i+1) * cellSize + 1, 
                              cellSize - 2, cellSize - 2);
                    
                    // Add label
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, cellSize / 2));
                    g.drawString(String.valueOf(cell), 
                                offsetX + (j+1) * cellSize + cellSize / 3, 
                                offsetY + (i+1) * cellSize + 2 * cellSize / 3);
                }
            }
        }
        
        // Draw exit with special color in the padding area
        int exitDrawX, exitDrawY;
        switch (exitSide) {
            case 0: // Top
                exitDrawX = offsetX + (exitX+1) * cellSize;
                exitDrawY = offsetY;
                break;
            case 1: // Right
                exitDrawX = offsetX + (cols+1) * cellSize;
                exitDrawY = offsetY + (exitY+1) * cellSize;
                break;
            case 2: // Bottom
                exitDrawX = offsetX + (exitX+1) * cellSize;
                exitDrawY = offsetY + (rows+1) * cellSize;
                break;
            case 3: // Left
                exitDrawX = offsetX;
                exitDrawY = offsetY + (exitY+1) * cellSize;
                break;
            default:
                // Fallback for invalid exit side
                exitDrawX = offsetX + (exitX+1) * cellSize;
                exitDrawY = offsetY + (exitY+1) * cellSize;
        }
        
        // Draw exit
        g.setColor(new Color(255, 215, 0, 128)); // Gold with transparency
        g.fillRect(exitDrawX, exitDrawY, cellSize, cellSize);
        g.setColor(Color.BLACK);
        
        // Use smaller font for EXIT text
        int exitFontSize = Math.max(cellSize / 4, 9); // Minimum size of 9 for readability
        g.setFont(new Font("Arial", Font.BOLD, exitFontSize));
        
        // Center text in the cell
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth("EXIT");
        int textHeight = fm.getHeight();
        g.drawString("EXIT", 
                    exitDrawX + (cellSize - textWidth) / 2, 
                    exitDrawY + (cellSize + textHeight) / 2 - fm.getDescent());
    }
}