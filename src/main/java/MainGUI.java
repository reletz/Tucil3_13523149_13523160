import logic.Board;
import logic.GameLogic;
import logic.GameState;
import logic.Piece;
import logic.PrimaryPiece;
import util.ConfigParser;
import util.GameManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        
        // Tambahkan primary piece
        pieceSelector.addItem(gameState.getPrimaryPiece().getLabel());
        
        // Tambahkan piece lainnya
        for (Piece piece : gameState.getPieces()) {
            pieceSelector.addItem(piece.getLabel());
        }
    }

    private void movePiece(boolean isForward) {
        if (gameState == null || pieceSelector.getSelectedItem() == null) {
            return;
        }
        
        char selectedLabel = (Character) pieceSelector.getSelectedItem();
        Piece selectedPiece = findPieceByLabel(selectedLabel);
        
        if (selectedPiece == null) {
            statusLabel.setText("Error: Piece not found");
            return;
        }
        
        boolean success;
        if (isForward) {
            success = GameLogic.moveForward(gameState, selectedPiece);
            statusLabel.setText(success ? 
                "Piece " + selectedLabel + " moved forward" : 
                "Cannot move piece " + selectedLabel + " forward");
        } else {
            success = GameLogic.moveBackward(gameState, selectedPiece);
            statusLabel.setText(success ? 
                "Piece " + selectedLabel + " moved backward" : 
                "Cannot move piece " + selectedLabel + " backward");
        }
        
        boardPanel.repaint();
        
        // Cek apakah game sudah selesai (primary piece sampai exit)
        checkGameCompletion();
    }
    
    private void checkGameCompletion() {
        if (gameState == null) return;
        
        Board board = gameState.getBoard();
        PrimaryPiece primaryPiece = gameState.getPrimaryPiece();
        char[][] grid = board.getGrid();
        
        int[] exitCoord = {board.getOutCoordX(), board.getOutCoordY()};
        
        // Cek jika primary piece berada di pintu keluar
        if (grid[exitCoord[1]][exitCoord[0]] == primaryPiece.getLabel()) {
            JOptionPane.showMessageDialog(this,
                "Congratulations! You solved the puzzle!",
                "Puzzle Solved", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private Piece findPieceByLabel(char label) {
        if (gameState == null) return null;
        
        // Cek primary piece
        PrimaryPiece primaryPiece = gameState.getPrimaryPiece();
        if (primaryPiece.getLabel() == label) {
            return primaryPiece;
        }
        
        // Cek piece lain
        for (Piece piece : gameState.getPieces()) {
            if (piece.getLabel() == label) {
                return piece;
            }
        }
        
        return null;
    }

    private void drawBoard(Graphics g) {
        if (gameState == null) return;
        
        Board board = gameState.getBoard();
        char[][] grid = board.getGrid();
        int rows = grid.length;
        int cols = grid[0].length;
        
        int cellSize = Math.min(boardPanel.getWidth() / cols, boardPanel.getHeight() / rows);
        
        // Gambar grid
        g.setColor(Color.BLACK);
        for (int i = 0; i <= rows; i++) {
            g.drawLine(0, i * cellSize, cols * cellSize, i * cellSize);
        }
        for (int j = 0; j <= cols; j++) {
            g.drawLine(j * cellSize, 0, j * cellSize, rows * cellSize);
        }
        
        // Gambar pieces
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char cell = grid[i][j];
                if (cell != ' ') {
                    // Set warna berdasarkan jenis piece
                    g.setColor(pieceColors.getOrDefault(cell, Color.GRAY));
                    g.fillRect(j * cellSize + 1, i * cellSize + 1, cellSize - 1, cellSize - 1);
                    
                    // Label piece
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, cellSize / 2));
                    g.drawString(String.valueOf(cell), 
                                j * cellSize + cellSize / 3, 
                                i * cellSize + 2 * cellSize / 3);
                }
            }
        }
        
        // Gambar pintu keluar dengan warna khusus
        int exitX = board.getOutCoordX();
        int exitY = board.getOutCoordY();
        g.setColor(new Color(255, 215, 0, 128)); // Gold dengan transparansi
        g.fillRect(exitX * cellSize, exitY * cellSize, cellSize, cellSize);
        g.setColor(Color.BLACK);
        g.drawString("EXIT", exitX * cellSize + 5, exitY * cellSize + cellSize - 5);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI();
            gui.setVisible(true);
        });
    }
}