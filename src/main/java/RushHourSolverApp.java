package rushhour;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.Timer;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import logic.Board;
import logic.GameLogic;
import logic.GameState;
import logic.Piece;
import logic.PrimaryPiece;
import util.ConfigParser;
import gui.PieceColorManager;
import gui.BoardDrawingUtil;
import gui.PlayPuzzle;

/**
 * Main application for the Rush Hour Solver with visualization capabilities.
 */
public class RushHourSolverApp extends JFrame {
    // Main components
    private JPanel mainPanel;
    private JPanel solverPanel;
    private JPanel visualizerPanel;
    private JTextField configPathField;
    private JComboBox<String> algorithmCombo;
    private JComboBox<String> heuristicCombo;
    private JButton loadConfigButton;
    private JButton solveButton;
    private JButton restartButton;
    private JButton playButton;
    private JButton prevStepButton;
    private JButton nextStepButton;
    private JLabel stepLabel;
    private JLabel statusLabel;
    private JPanel statsPanel;
    private JLabel algorithmLabel;
    private JLabel heuristicLabel;
    private JLabel solutionLengthLabel;
    private JLabel nodesVisitedLabel;
    private JLabel maxFrontierLabel;
    private JLabel searchTimeLabel;
    private SolutionBoardPanel boardVisualizer;
    private Timer animationTimer;
    private JButton playPauseButton;
    private JSlider speedSlider;
    private boolean isPlaying = false;
    
    // Game state and solution data
    private GameState initialGameState;
    private File currentConfigFile;
    private List<List<String>> solutionSteps;
    private int currentStepIndex = 0;
    
    public RushHourSolverApp() {
        super("Rush Hour Solver");
        
        // Initialize UI components
        initializeUI();
        initializeTimer();

        
        // Set up the main window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create solver panel (top)
        solverPanel = createSolverPanel();
        
        // Create stats panel (between solver and visualizer)
        statsPanel = createStatsPanel();
        
        // Create visualizer panel (center)
        visualizerPanel = createVisualizerPanel();
        
        // Create a panel to hold solver and stats
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(solverPanel, BorderLayout.NORTH);
        topPanel.add(statsPanel, BorderLayout.CENTER);
        
        // Add panels to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(visualizerPanel, BorderLayout.CENTER);
        
        // Create status bar (bottom)
        statusLabel = new JLabel("Load a configuration file to begin");
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        setContentPane(mainPanel);
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Search Statistics"));
        
        algorithmLabel = new JLabel("Algorithm: -");
        heuristicLabel = new JLabel("Heuristic: -");
        solutionLengthLabel = new JLabel("Solution length: -");
        nodesVisitedLabel = new JLabel("Nodes visited: -");
        maxFrontierLabel = new JLabel("Max frontier size: -");
        searchTimeLabel = new JLabel("Search time: -");
        
        panel.add(algorithmLabel);
        panel.add(nodesVisitedLabel);
        panel.add(heuristicLabel);
        panel.add(maxFrontierLabel);
        panel.add(solutionLengthLabel);
        panel.add(searchTimeLabel);
        
        return panel;
    }

    // Initialize the timer
    private void initializeTimer() {
        // Initialize animation timer
        animationTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentStepIndex < solutionSteps.size() - 1) {
                    currentStepIndex++;
                    updateVisualization();
                    
                    // If we've reached the end, stop the animation
                    if (currentStepIndex >= solutionSteps.size() - 1) {
                        stopAnimation();
                    }
                }
            }
        });
    }
    
    private JPanel createSolverPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Puzzle Solver Controls"));
        
        // Configuration file selection
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePanel.add(new JLabel("Configuration File:"));
        
        configPathField = new JTextField(30);
        filePanel.add(configPathField);
        
        loadConfigButton = new JButton("Browse...");
        loadConfigButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseConfigFile();
            }
        });
        filePanel.add(loadConfigButton);
        
        // Algorithm selection
        JPanel algoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        algoPanel.add(new JLabel("Algorithm:"));
        
        algorithmCombo = new JComboBox<>(new String[] {
            "Uniform Cost Search (UCS)",
            "Greedy Best-First Search (GBFS)",
            "A* Search"
        });
        algorithmCombo.setSelectedIndex(2); // Default to A*
        algoPanel.add(algorithmCombo);
        
        // Heuristic selection (for informed search)
        algoPanel.add(new JLabel("Heuristic:"));
        heuristicCombo = new JComboBox<>(new String[] {
            "Manhattan Distance",
            "Blocking Pieces",
            "Combined"
        });
        heuristicCombo.setSelectedIndex(2); // Default to Combined
        algoPanel.add(heuristicCombo);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        solveButton = new JButton("Solve Puzzle");
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solvePuzzle();
            }
        });
        solveButton.setEnabled(false);
        buttonPanel.add(solveButton);
        
        playButton = new JButton("Play This Puzzle");
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openGameWindow();
            }
        });
        playButton.setEnabled(false);
        buttonPanel.add(playButton);
        
        // Add panels to main solver panel
        panel.add(filePanel);
        panel.add(algoPanel);
        panel.add(buttonPanel);
        
        return panel;
    }
    
    private JPanel createVisualizerPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Solution Visualization"));
        
        // Create board visualizer
        boardVisualizer = new SolutionBoardPanel();
        
        // Create step navigation controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        // First row - navigation buttons
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // Add restart button
        JButton restartButton = new JButton("⏮ Restart");
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartVisualization();
            }
        });
        restartButton.setEnabled(false);
        
        prevStepButton = new JButton("◀ Previous Step");
        prevStepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPreviousStep();
            }
        });
        prevStepButton.setEnabled(false);
        
        stepLabel = new JLabel("Step 0 of 0");
        
        nextStepButton = new JButton("Next Step ▶");
        nextStepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showNextStep();
            }
        });
        nextStepButton.setEnabled(false);
        
        // Play/Pause button
        playPauseButton = new JButton("▶ Play");
        playPauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePlayPause();
            }
        });
        playPauseButton.setEnabled(false);
        
        navigationPanel.add(restartButton);
        navigationPanel.add(prevStepButton);
        navigationPanel.add(playPauseButton);
        navigationPanel.add(nextStepButton);
        navigationPanel.add(stepLabel);
        
        // Second row - speed control
        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        speedPanel.add(new JLabel("Speed:"));
        
        speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateAnimationSpeed();
            }
        });
        speedPanel.add(speedSlider);
        
        // Combine both rows
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.add(navigationPanel);
        controlPanel.add(speedPanel);
        
        // Add components to panel
        panel.add(boardVisualizer, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        // Initialize animation timer (but don't start it yet)
        animationTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentStepIndex < solutionSteps.size() - 1) {
                    showNextStep();
                } else {
                    // Stop when we reach the end
                    stopAnimation();
                }
            }
        });

        this.restartButton = restartButton;
        
        return panel;
    }

    /**
     * Restarts the visualization by returning to the initial state
     */
    private void restartVisualization() {
        // Stop animation if it's playing
        if (isPlaying) {
            stopAnimation();
        }
        
        // Reset to initial state
        currentStepIndex = 0;
        updateVisualization();
    }

    /**
     * Toggles between play and pause states
     */
    private void togglePlayPause() {
        if (isPlaying) {
            stopAnimation();
        } else {
            startAnimation();
        }
    }

    /**
     * Starts automatic playback of solution steps
     */
    private void startAnimation() {
        if (solutionSteps == null || solutionSteps.isEmpty() || 
            currentStepIndex >= solutionSteps.size() - 1) {
            return;
        }
        
        isPlaying = true;
        playPauseButton.setText("⏸ Pause");
        prevStepButton.setEnabled(false);
        nextStepButton.setEnabled(false);
        
        // Set timer delay based on speed slider
        updateAnimationSpeed();
        
        // Start the timer
        animationTimer.start();
    }

    /**
     * Stops automatic playback
     */
    private void stopAnimation() {
        if (!isPlaying) return;
        
        isPlaying = false;
        animationTimer.stop();
        playPauseButton.setText("▶ Play");
        
        // Re-enable navigation buttons as appropriate
        prevStepButton.setEnabled(currentStepIndex > 0);
        nextStepButton.setEnabled(currentStepIndex < solutionSteps.size() - 1);
    }

    /**
     * Updates animation speed based on slider value
     */
    private void updateAnimationSpeed() {
        // Convert slider value (1-10) to milliseconds (1000ms to 100ms)
        int delay = 1050 - (speedSlider.getValue() * 100);
        animationTimer.setDelay(delay);
    }
    
    private void browseConfigFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Puzzle Configuration");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            configPathField.setText(selectedFile.getAbsolutePath());
            currentConfigFile = selectedFile;
            
            try {
                initialGameState = ConfigParser.parseConfig(selectedFile.getAbsolutePath());
                
                // Show the initial board state
                boardVisualizer.setGameState(initialGameState);
                
                // Enable solve and play buttons
                solveButton.setEnabled(true);
                playButton.setEnabled(true);
                
                // Reset solution data
                solutionSteps = null;
                currentStepIndex = 0;
                stepLabel.setText("Step 0 of 0");
                prevStepButton.setEnabled(false);
                nextStepButton.setEnabled(false);
                
                statusLabel.setText("Configuration loaded successfully");
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading configuration: " + e.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
                
                statusLabel.setText("Failed to load configuration");
            }
        }
    }
    
    private void solvePuzzle() {
        if (initialGameState == null) {
            JOptionPane.showMessageDialog(this, 
                "Please load a configuration file first",
                "No Configuration", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get selected algorithm and heuristic
        String algorithm = (String) algorithmCombo.getSelectedItem();
        String heuristic = (String) heuristicCombo.getSelectedItem();
        
        // For now, just use a dummy solution file
        // In a real implementation, this would call your actual algorithm
        loadDummySolution();
        
        statusLabel.setText("Using algorithm: " + algorithm + 
                          (algorithm.contains("A*") || algorithm.contains("GBFS") ? 
                           " with heuristic: " + heuristic : ""));
    }
    
    private void loadDummySolution() {
        try {
            // Initialize solution steps list
            solutionSteps = new ArrayList<>();
            
            // Add initial state as first step
            boardVisualizer.setGameState(initialGameState);
            List<String> initialBoard = boardVisualizer.getCurrentBoardAsText();
            solutionSteps.add(initialBoard);

            // Read from resource in the searchResult directory
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("searchResult/searchRes.txt");
            
            if (inputStream == null) {
                // Try alternate locations if not found
                inputStream = getClass().getClassLoader().getResourceAsStream("searchRes.txt");
                
                if (inputStream == null) {
                    // Create a fallback dummy solution if resource isn't found
                    JOptionPane.showMessageDialog(this,
                        "Could not find searchRes.txt resource. Creating a dummy solution instead.",
                        "Resource Not Found", JOptionPane.WARNING_MESSAGE);
                    
                    // Create dummy solution with just initial state
                    currentStepIndex = 0;
                    updateVisualization();
                    return;
                }
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            boolean readingBoard = false;
            List<String> boardLines = null;

            // Tracking stats
            String algorithm = "-";
            String heuristic = "-";
            String solutionLength = "-";
            String nodesVisited = "-";
            String maxFrontier = "-";
            String searchTime = "-";
            
            while ((line = reader.readLine()) != null) {
                // Parse statistics
                if (line.startsWith("Algorithm:")) {
                    algorithm = line.substring("Algorithm:".length()).trim();
                } else if (line.startsWith("Heuristic:")) {
                    heuristic = line.substring("Heuristic:".length()).trim();
                } else if (line.startsWith("Solution length:")) {
                    solutionLength = line.substring("Solution length:".length()).trim();
                } else if (line.startsWith("Nodes visited:")) {
                    nodesVisited = line.substring("Nodes visited:".length()).trim();
                } else if (line.startsWith("Maximum frontier size:")) {
                    maxFrontier = line.substring("Maximum frontier size:".length()).trim();
                } else if (line.startsWith("Search time:")) {
                    searchTime = line.substring("Search time:".length()).trim();
                }
                // Parse board steps
                else if (line.startsWith("Step ")) {
                    // New step found - save previous board if exists
                    if (boardLines != null && !boardLines.isEmpty()) {
                        solutionSteps.add(new ArrayList<>(boardLines));
                    }
                    
                    // Start new board
                    readingBoard = true;
                    boardLines = new ArrayList<>();
                } 
                else if (readingBoard && line.matches("^[A-Za-z\\.]+$")) {
                    // This looks like a board line - collect it
                    boardLines.add(line);
                }
                else if (line.trim().isEmpty()) {
                    // Empty line - end of board
                    readingBoard = false;
                }
            }
            
            // Add the last board if needed
            if (boardLines != null && !boardLines.isEmpty()) {
                solutionSteps.add(new ArrayList<>(boardLines));
            }
            
            reader.close();
            
            // Update stats display
            algorithmLabel.setText("Algorithm: " + algorithm);
            heuristicLabel.setText("Heuristic: " + heuristic);
            solutionLengthLabel.setText("Solution length: " + solutionLength);
            nodesVisitedLabel.setText("Nodes visited: " + nodesVisited);
            maxFrontierLabel.setText("Max frontier size: " + maxFrontier);
            searchTimeLabel.setText("Search time: " + searchTime);
            
            // Show first step
            currentStepIndex = 0;
            updateVisualization();
            
            // Enable navigation buttons
            prevStepButton.setEnabled(false);
            nextStepButton.setEnabled(solutionSteps.size() > 1);
            playPauseButton.setEnabled(solutionSteps.size() > 1);
            restartButton.setEnabled(false);
            
            statusLabel.setText("Solution loaded: " + solutionSteps.size() + " steps");
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading solution: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Failed to load solution: " + e.getMessage());
        }
    }
    
    private void showPreviousStep() {
        // Stop animation if it's playing
        if (isPlaying) {
            stopAnimation();
        }
        
        if (solutionSteps != null && currentStepIndex > 0) {
            currentStepIndex--;
            updateVisualization();
        }
    }
    
    private void showNextStep() {
        // Stop animation if it's playing
        if (isPlaying) {
            stopAnimation();
        }
        
        if (solutionSteps != null && currentStepIndex < solutionSteps.size() - 1) {
            currentStepIndex++;
            updateVisualization();
        }
    }
    
    private void updateVisualization() {
        // Update the board visualizer with the current step
        if (solutionSteps != null && !solutionSteps.isEmpty() && 
            currentStepIndex < solutionSteps.size()) {
            
            List<String> currentBoard = solutionSteps.get(currentStepIndex);
            boardVisualizer.setBoardFromText(currentBoard);
            
            // Update step label
            stepLabel.setText("Step " + currentStepIndex + " of " + (solutionSteps.size() - 1));
            
            // Update navigation buttons
            restartButton.setEnabled(currentStepIndex > 0);  // Only enable if not at start
            prevStepButton.setEnabled(currentStepIndex > 0);
            nextStepButton.setEnabled(currentStepIndex < solutionSteps.size() - 1);
            playPauseButton.setEnabled(currentStepIndex < solutionSteps.size() - 1);
        }
    }
    
    private void openGameWindow() {
        if (initialGameState == null) {
            JOptionPane.showMessageDialog(this, 
                "Please load a configuration file first",
                "No Configuration", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create a new game window
        final PlayPuzzle gameWindow = new PlayPuzzle();
        
        // Add a custom window listener that manually handles closing
        gameWindow.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                gameWindow.setVisible(false);
                gameWindow.dispose();
            }
        });
        
        // Tell the window to do nothing on close - our listener handles it
        gameWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        gameWindow.loadGameState(initialGameState);
        gameWindow.setVisible(true);
    }
    
    /**
     * Panel for displaying the solution board state
     */
    private class SolutionBoardPanel extends JPanel {
        // The game state to show
        private GameState gameState;
        // Text representation of the board
        private List<String> boardLines;
        // Constants for drawing
        private static final int CELL_PADDING = 2;
        
        public SolutionBoardPanel() {
            setPreferredSize(new Dimension(500, 500));
            setBackground(Color.WHITE);
        }
        
        /**
         * Set the board to display from a GameState
         */
        public void setGameState(GameState state) {
            this.gameState = state;
            this.boardLines = null;
            repaint();
        }
        
        /**
         * Set the board to display from text representation
         */
        public void setBoardFromText(List<String> boardText) {
            this.boardLines = boardText;
            this.gameState = null;
            repaint();
        }
        
        /**
         * Get the current displayed board as text
         */
        public List<String> getCurrentBoardAsText() {
            if (boardLines != null) {
                return boardLines;
            } else if (gameState != null) {
                // Convert the game state's grid to text
                Board board = gameState.getBoard();
                char[][] grid = board.getGrid();
                List<String> textLines = new ArrayList<>();
                
                for (int i = 0; i < grid.length; i++) {
                    StringBuilder line = new StringBuilder();
                    for (int j = 0; j < grid[i].length; j++) {
                        line.append(grid[i][j]);
                    }
                    textLines.add(line.toString());
                }
                return textLines;
            }
            return new ArrayList<>();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // If we have a game state, render from that
            if (gameState != null) {
                drawFromGameState(g);
                return;
            }
            
            // If we have board lines, render from that
            if (boardLines != null && !boardLines.isEmpty()) {
                drawFromBoardLines(g);
                return;
            }
            
            // Nothing to draw
            g.setColor(Color.GRAY);
            g.setFont(new Font("Arial", Font.ITALIC, 16));
            g.drawString("No board to display", getWidth() / 2 - 80, getHeight() / 2);
        }
        
        private void drawFromGameState(Graphics g) {
            GameLogic.updateBoardGrid(gameState);
            
            Board board = gameState.getBoard();
            char[][] grid = board.getGrid();
            
            // Use the shared drawing utility
            BoardDrawingUtil.drawBoard(g, grid, grid.length, grid[0].length, 
                                board.getOutCoordX(), board.getOutCoordY(), 
                                board.getExitSide(), this);
        }

        private void drawFromBoardLines(Graphics g) {
            int rows = boardLines.size();
            int cols = 0;
            
            for (String line : boardLines) {
                cols = Math.max(cols, line.length());
            }
            
            // Convert board lines to grid
            char[][] grid = new char[rows][cols];
            for (int i = 0; i < rows; i++) {
                String line = boardLines.get(i);
                for (int j = 0; j < cols; j++) {
                    if (j < line.length()) {
                        grid[i][j] = line.charAt(j);
                    } else {
                        grid[i][j] = '.'; // Fill with empty spaces
                    }
                }
            }
            
            // Use exit info from initial game state
            int exitX = initialGameState.getBoard().getOutCoordX();
            int exitY = initialGameState.getBoard().getOutCoordY();
            int exitSide = initialGameState.getBoard().getExitSide();
            
            // Use the shared drawing utility
            BoardDrawingUtil.drawBoard(g, grid, rows, cols, exitX, exitY, exitSide, this);
        }
    }
    
    // Add a TextField class if needed
    private static class JTextField extends javax.swing.JTextField {
        public JTextField(int columns) {
            super(columns);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                RushHourSolverApp mainApp = new RushHourSolverApp();
                mainApp.setVisible(true); 
            }
        });
    }
}