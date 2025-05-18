package gui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for piece color management.
 * Ensures consistent piece coloring across all application views.
 */
public class PieceColorManager {
    private static final Map<Character, Color> pieceColors = new HashMap<>();
    
    static {
        // Initialize with standard colors
        pieceColors.put('P', Color.RED); // Primary piece is red
        
        // Pre-generate some fixed colors for common pieces
        pieceColors.put('A', new Color(30, 144, 255));  // Dodger Blue
        pieceColors.put('B', new Color(50, 205, 50));   // Lime Green
        pieceColors.put('C', new Color(255, 165, 0));   // Orange
        pieceColors.put('D', new Color(138, 43, 226));  // Blue Violet
        pieceColors.put('E', new Color(255, 69, 0));    // Red-Orange
        pieceColors.put('F', new Color(0, 139, 139));   // Teal
        pieceColors.put('G', new Color(160, 82, 45));   // Sienna
        pieceColors.put('H', new Color(255, 20, 147));  // Deep Pink
        pieceColors.put('I', new Color(46, 139, 87));   // Sea Green
        pieceColors.put('J', new Color(218, 165, 32));  // Golden Rod
        pieceColors.put('L', new Color(75, 0, 130));    // Indigo
        pieceColors.put('M', new Color(154, 205, 50));  // Yellow Green
    }
    
    /**
     * Gets the color for a given piece label, generating a new color if needed.
     * 
     * @param pieceLabel The character label of the piece
     * @return The color for the piece
     */
    public static Color getColorForPiece(char pieceLabel) {
        if (!pieceColors.containsKey(pieceLabel)) {
            // Generate a consistent random color for any new piece
            // Using hash code ensures the same piece always gets the same color
            int hash = Character.toString(pieceLabel).hashCode();
            float hue = (hash % 360) / 360.0f;
            pieceColors.put(pieceLabel, Color.getHSBColor(hue, 0.8f, 0.8f));
        }
        
        return pieceColors.get(pieceLabel);
    }
    
    /**
     * Gets the entire color map (for testing or special cases)
     */
    public static Map<Character, Color> getColorMap() {
        return new HashMap<>(pieceColors); // Return a copy to prevent modification
    }
}