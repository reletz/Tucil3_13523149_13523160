package gui;

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
import gui.PieceColorManager;
import gui.BoardDrawingUtil;

/**
 * Kelas PlayPuzzle untuk aplikasi Rush Hour Solver dengan antarmuka grafis.
 */
public class PlayPuzzle extends JFrame {
    private GameState gameState;
    private JPanel boardPanel;
    private JComboBox<Character> pieceSelector;
    private JButton forwardButton;
    private JButton backwardButton;
    private JLabel statusLabel;

    public PlayPuzzle() {
        super("Rush Hour Game");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initializeGUI();
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
        
        // First make sure the grid is updated based on current piece positions
        GameLogic.updateBoardGrid(gameState);
        
        // Use the shared drawing utility
        BoardDrawingUtil.drawBoard(g, grid, grid.length, grid[0].length, 
                            board.getOutCoordX(), board.getOutCoordY(), 
                            board.getExitSide(), boardPanel);
    }

    /**
     * Load a game state into the GUI
     * 
     * @param state The GameState to load
     */
    public void loadGameState(GameState state) {
        this.gameState = state;
        
        // Update piece selector
        updatePieceSelector();
        
        // Repaint the board
        boardPanel.repaint();
        
        // Update status
        statusLabel.setText("Game loaded. Select a piece to move.");
        
        // Enable control buttons
        forwardButton.setEnabled(true);
        backwardButton.setEnabled(true);
    }

    /**
     * Update the piece selector dropdown with available pieces
     */
    private void updatePieceSelector() {
        if (gameState == null) return;
        
        pieceSelector.removeAllItems();
        
        // Add primary piece
        char primaryLabel = gameState.getPrimaryPieceState().getPiece().getLabel();
        pieceSelector.addItem(primaryLabel);
        
        // Add other pieces
        for (char label : gameState.getPieces().keySet()) {
            pieceSelector.addItem(label);
        }
        
        // Select primary piece by default
        pieceSelector.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PlayPuzzle gui = new PlayPuzzle();
            gui.setVisible(true);
        });
    }
}