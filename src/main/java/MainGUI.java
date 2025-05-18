import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import logic.Board;
import logic.GameLogic;
import logic.GameState;
import logic.Piece;
import logic.PrimaryPiece;
import util.ConfigParser;

/**
 * Kelas MainGUI untuk aplikasi Rush Hour Solver dengan antarmuka grafis.
 */
public class MainGUI extends JFrame {
    private GameState gameState;
    private JPanel boardPanel;
    private JComboBox<Character> pieceSelector;
    private JButton forwardButton;
    private JButton backwardButton;
    private JLabel statusLabel;
    private Map<Character, Color> pieceColors;

    public MainGUI() {
        super("Rush Hour Solver");
        initializeGUI();
        initializePieceColors();
    }

    private void initializeGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLayout(new BorderLayout());

        // Panel atas untuk kontrol
        JPanel controlPanel = new JPanel();
        JButton loadButton = new JButton("Load Config");
        pieceSelector = new JComboBox<>();
        pieceSelector.setEnabled(false);
        forwardButton = new JButton("Move Forward");
        forwardButton.setEnabled(false);
        backwardButton = new JButton("Move Backward");
        backwardButton.setEnabled(false);

        controlPanel.add(loadButton);
        controlPanel.add(new JLabel("Select Piece:"));
        controlPanel.add(pieceSelector);
        controlPanel.add(forwardButton);
        controlPanel.add(backwardButton);

        add(controlPanel, BorderLayout.NORTH);

        // Panel untuk board
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
            }
        };
        boardPanel.setPreferredSize(new Dimension(400, 400));
        boardPanel.setBackground(Color.WHITE);
        add(new JScrollPane(boardPanel), BorderLayout.CENTER);

        // Label status di bagian bawah
        statusLabel = new JLabel("Load a configuration file to start");
        add(statusLabel, BorderLayout.SOUTH);

        // Event handlers
        loadButton.addActionListener(e -> loadConfig());
        forwardButton.addActionListener(e -> movePiece(true));
        backwardButton.addActionListener(e -> movePiece(false));

        setLocationRelativeTo(null);
    }

    private void initializePieceColors() {
        pieceColors = new HashMap<>();
        pieceColors.put('P', Color.RED); // Primary piece selalu merah
        
        // Warna lainnya untuk piece reguler
        Color[] colors = {
            Color.BLUE, Color.GREEN, Color.ORANGE, Color.PINK, 
            Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.DARK_GRAY,
            new Color(165, 42, 42), // Brown
            new Color(50, 205, 50), // Lime Green
            new Color(138, 43, 226), // Purple
            new Color(0, 191, 255) // Deep Sky Blue
        };
        
        // Array untuk huruf A-Z kecuali P
        char[] letters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
                          'M', 'N', 'O', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        
        for (int i = 0; i < letters.length; i++) {
            pieceColors.put(letters[i], colors[i % colors.length]);
        }
    }

    private void loadConfig() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih File Konfigurasi");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                gameState = ConfigParser.parseConfig(selectedFile.getAbsolutePath());
                updatePieceSelector();
                forwardButton.setEnabled(true);
                backwardButton.setEnabled(true);
                pieceSelector.setEnabled(true);
                boardPanel.repaint();
                
                statusLabel.setText("Config loaded: " + selectedFile.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading config: " + ex.getMessage(), 
                    "Load Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Failed to load configuration");
            }
        }
    }

    private void updatePieceSelector() {
        pieceSelector.removeAllItems();
        
        // Add primary piece
        pieceSelector.addItem(gameState.getPrimaryPieceState().getPiece().getLabel());
        
        // Add other pieces
        for (GameState.PieceState pieceState : gameState.getPieces().values()) {
            pieceSelector.addItem(pieceState.getPiece().getLabel());
        }
    }

    /**
     * Moves the selected piece forward or backward.
     * 
     * @param isForward true to move forward, false to move backward
     */
    private void movePiece(boolean isForward) {
        if (gameState == null || pieceSelector.getSelectedItem() == null) {
            return;
        }
        
        char selectedLabel = (Character) pieceSelector.getSelectedItem();
        
        // Get the current piece's orientation to determine direction
        boolean isHorizontal = false;
        Piece selectedPiece = findPieceByLabel(selectedLabel);
        if (selectedPiece != null) {
            isHorizontal = selectedPiece.isHorizontal();
        } else {
            statusLabel.setText("Error: Piece not found");
            return;
        }
        
        // Determine direction based on orientation and forward/backward
        int direction;
        if (isHorizontal) {
            direction = isForward ? 0 : 2; // Right (0) or Left (2)
        } else {
            direction = isForward ? 1 : 3; // Down (1) or Up (3)
        }
        
        // Attempt to move the piece
        GameState newState = GameLogic.movePiece(gameState, selectedLabel, direction);
        
        if (newState != null) {
            // Movement was successful
            gameState = newState;
            String directionText = isForward ? "forward" : "backward";
            statusLabel.setText("Piece " + selectedLabel + " moved " + directionText);
        } else {
            // Movement was not valid
            String directionText = isForward ? "forward" : "backward";
            statusLabel.setText("Cannot move piece " + selectedLabel + " " + directionText);
        }
        
        boardPanel.repaint();
        
        // Check if game is complete
        checkGameCompletion();
    }
    
    private void checkGameCompletion() {
        if (gameState == null) return;
        
        // Check if the primary piece is at the exit
        if (gameState.isPrimaryPieceAtExit()) {
            JOptionPane.showMessageDialog(this,
                "Congratulations! You solved the puzzle!",
                "Puzzle Solved", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private Piece findPieceByLabel(char label) {
        if (gameState == null) return null;
        
        // Check primary piece
        if (gameState.getPrimaryPieceState().getPiece().getLabel() == label) {
            return gameState.getPrimaryPieceState().getPiece();
        }
        
        // Check other pieces
        GameState.PieceState pieceState = gameState.getPieces().get(label);
        return pieceState != null ? pieceState.getPiece() : null;
    }

    private void drawBoard(Graphics g) {
        if (gameState == null) return;
        
        Board board = gameState.getBoard();
        char[][] grid = board.getGrid();
        int rows = grid.length;
        int cols = grid[0].length;
        
        // Add padding of 1 cell on each side for exit visibility
        int displayRows = rows + 2;  // +2 for top and bottom padding
        int displayCols = cols + 2;  // +2 for left and right padding
        
        // Calculate cell size based on padded dimensions
        int cellSize = Math.min(boardPanel.getWidth() / displayCols, 
                            boardPanel.getHeight() / displayRows);
        
        // Drawing offset to center the padded board
        int offsetX = (boardPanel.getWidth() - (displayCols * cellSize)) / 2;
        int offsetY = (boardPanel.getHeight() - (displayRows * cellSize)) / 2;
        
        // First make sure the grid is updated based on current piece positions
        GameLogic.updateBoardGrid(gameState);
        
        // Draw background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, boardPanel.getWidth(), boardPanel.getHeight());
        
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
                if (cell != ' ') {
                    // Set color based on piece type
                    g.setColor(pieceColors.getOrDefault(cell, Color.GRAY));
                    g.fillRect(offsetX + (j+1) * cellSize + 1, offsetY + (i+1) * cellSize + 1, 
                            cellSize - 1, cellSize - 1);
                    
                    // Label piece
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, cellSize / 2));
                    g.drawString(String.valueOf(cell), 
                                offsetX + (j+1) * cellSize + cellSize / 3, 
                                offsetY + (i+1) * cellSize + 2 * cellSize / 3);
                }
            }
        }
        
        // Draw exit with special color in the padding area
        int exitX = board.getOutCoordX();
        int exitY = board.getOutCoordY();
        int exitSide = board.getExitSide();
        
        // Calculate actual drawing position based on exit side
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI();
            gui.setVisible(true);
        });
    }
}