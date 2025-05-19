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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
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
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.Timer;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import logic.*;
import solver.algorithm.*;
import solver.heuristic.*;
import util.*;
import gui.*;

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
    private Solver currentSolver;
    private JButton saveButton;
    
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
            "A* Search",
            "Branch and Bound"
        });
        algorithmCombo.setSelectedIndex(0); // Default to UCS

        // Prepare two different ComboBox models - one for UCS, one for informed search
        String[] informedHeuristics = new String[] {
            "Manhattan Distance",
            "Blocking Pieces",
            "Distance To Exit",
            "Piece Density",
            "Combined"
        };

        // Add listener to handle algorithm changes
        algorithmCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isUCS = algorithmCombo.getSelectedItem().toString().contains("UCS");
                
                if (isUCS) {
                    // For UCS: Disable heuristic selection entirely
                    heuristicCombo.setEnabled(false);
                    
                    // Create a custom disabled model showing "N/A"
                    DefaultComboBoxModel<String> ucsModel = new DefaultComboBoxModel<>(new String[] {"N/A"});
                    heuristicCombo.setModel(ucsModel);
                    heuristicCombo.setSelectedIndex(0);
                } else {
                    // For informed search: Enable and set proper model
                    heuristicCombo.setEnabled(true);
                    
                    // Store current selection if it exists
                    String currentSelection = null;
                    if (heuristicCombo.getItemCount() > 0 && !heuristicCombo.getSelectedItem().equals("N/A")) {
                        currentSelection = (String)heuristicCombo.getSelectedItem();
                    }
                    
                    // Set informed search model
                    DefaultComboBoxModel<String> informedModel = new DefaultComboBoxModel<>(informedHeuristics);
                    heuristicCombo.setModel(informedModel);
                    
                    // Try to restore previous selection, or default to first option
                    if (currentSelection != null && Arrays.asList(informedHeuristics).contains(currentSelection)) {
                        heuristicCombo.setSelectedItem(currentSelection);
                    } else {
                        heuristicCombo.setSelectedIndex(0); // Default to first heuristic
                    }
                }
            }
        });
        algoPanel.add(algorithmCombo);

        // Heuristic selection
        algoPanel.add(new JLabel("Heuristic:"));
        heuristicCombo = new JComboBox<>(new String[] {"N/A"});
        heuristicCombo.setEnabled(false); // Initially disabled for UCS
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

        // Save solution button
        saveButton = new JButton("Save to TXT");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSolutionToFile();
            }
        });
        saveButton.setEnabled(false); // Initially disabled
        
        navigationPanel.add(restartButton);
        navigationPanel.add(prevStepButton);
        navigationPanel.add(playPauseButton);
        navigationPanel.add(nextStepButton);
        navigationPanel.add(stepLabel);
        navigationPanel.add(saveButton);
        
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
        
        String algorithm = algorithmCombo.getSelectedItem().toString();
        String heuristicStr = heuristicCombo.getSelectedItem().toString();
        
        Solver solver;
        Heuristic heuristic = null;
        
        // Create appropriate heuristic if needed
        if (!heuristicStr.equals("None")) {
            switch (heuristicStr) {
                case "Manhattan Distance":
                    heuristic = new ManhattanDistanceHeuristic();
                    break;
                case "Blocking Pieces":
                    heuristic = new BlockingPiecesHeuristic();
                    break;
                case "Combined":
                    heuristic = new CombinedHeuristic();
                    break;
                case "Distance To Exit":
                    heuristic = new DistanceToExitHeuristic();
                    break;
                case "Piece Density":
                    heuristic = new PieceDensityHeuristic();
                    break;
            }
        }
        
        // Create appropriate solver
        if (algorithm.contains("UCS")) {
            solver = new UCSolver();
        } else if (algorithm.contains("GBFS")) {
            // Placeholder until implemented
            solver = new BestFSolver(heuristic);
        } else if (algorithm.contains("A* Search")) {
            solver = new AStarSolver(heuristic);
        } else {
            solver = new BranchAndBoundSolver(heuristic);
        }
        
        if (heuristic != null) {
            statusLabel.setText("Solving puzzle with " + algorithm + "; Heuristic: " + heuristic.getName());
        } else {
            statusLabel.setText("Solving puzzle with " + algorithm);
        }

        // Disable UI during solving
        solveButton.setEnabled(false);

        this.currentSolver = solver;
        
        // Run solver in background thread to prevent UI freeze
        new SwingWorker<Node, Void>() {
            @Override
            protected Node doInBackground() throws Exception {
                return solver.solve(initialGameState);
            }
            
            @Override
            protected void done() {
                try {
                    // Get result from background task
                    Node solution = get();
                    
                    // Visualize the solution
                    visualizeSolverResults(solver);
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(RushHourSolverApp.this, 
                        "Error solving puzzle: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Failed to solve puzzle: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // Re-enable UI
                    solveButton.setEnabled(true);
                }
            }
        }.execute();
    }

    /**
     * Visualize results directly from the solver
     * @param solver The solver with solution results
     */
    private void visualizeSolverResults(Solver solver) {
        // Check if we have a solution
        List<Node> path = solver.getSolutionPath();
        boolean hasSolution = path != null && !path.isEmpty();
        
        // Update statistics display even if no solution found
        algorithmLabel.setText("Algorithm: " + solver.getAlgorithmName());
        
        // Heuristic info
        if (solver instanceof UCSolver) {
            heuristicLabel.setText("Heuristic: None");
        } else if (solver instanceof InformedSolver) {
            heuristicLabel.setText("Heuristic: " + ((InformedSolver)solver).getHeuristic().getName());
        } else {
            heuristicLabel.setText("Heuristic: Unknown");
        }
        
        // Always show nodes explored and time statistics
        nodesVisitedLabel.setText("Nodes visited: " + solver.getNodesExplored());
        maxFrontierLabel.setText("Max frontier size: " + solver.getMaxQueueSize());
        searchTimeLabel.setText("Search time: " + solver.getExecutionTimeMs() + "ms");
        
        // Solution length only shown if solution exists
        solutionLengthLabel.setText("Solution length: " + (hasSolution ? (path.size() - 1) + " moves" : "No solution"));
        
        if (!hasSolution) {
            // Show message dialog
            JOptionPane.showMessageDialog(this,
                "No solution found! Explored " + solver.getNodesExplored() + " nodes.",
                "No Solution", JOptionPane.INFORMATION_MESSAGE);
            
            // Set status and disable controls
            statusLabel.setText("No solution found. Explored " + solver.getNodesExplored() + " nodes.");
            prevStepButton.setEnabled(false);
            nextStepButton.setEnabled(false);
            playPauseButton.setEnabled(false);
            restartButton.setEnabled(false);
            saveButton.setEnabled(false);
            saveButton.setEnabled(true);
            
            // Show at least the initial state
            solutionSteps = new ArrayList<>();
            GameState initialState = initialGameState;
            if (initialState != null) {
                GameLogic.updateBoardGrid(initialState);
                char[][] grid = initialState.getBoard().getGrid();
                List<String> boardLines = new ArrayList<>();
                for (char[] row : grid) {
                    boardLines.add(new String(row));
                }
                solutionSteps.add(boardLines);
                currentStepIndex = 0;
                updateVisualization();
            }
            
            return;
        }
        
        try {
            // Initialize solution steps list
            solutionSteps = new ArrayList<>();
            
            // Convert each node in path to board representation
            for (Node node : path) {
                GameState state = node.getState();
                
                // Update game state for board rendering
                GameLogic.updateBoardGrid(state);
                
                // Get text representation of board
                char[][] grid = state.getBoard().getGrid();
                List<String> boardLines = new ArrayList<>();
                for (char[] row : grid) {
                    boardLines.add(new String(row));
                }
                
                // Add to solution steps
                solutionSteps.add(boardLines);
            }
            
            // Show first step
            currentStepIndex = 0;
            updateVisualization();
            
            // Enable navigation buttons - only if we have steps
            prevStepButton.setEnabled(false);
            nextStepButton.setEnabled(solutionSteps.size() > 1);
            playPauseButton.setEnabled(solutionSteps.size() > 1);
            restartButton.setEnabled(false);
            saveButton.setEnabled(true);
            
            statusLabel.setText("Solution found: " + solutionSteps.size() + " steps");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error visualizing solution: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Failed to visualize solution: " + e.getMessage());
            e.printStackTrace();
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

    /**
     * Saves the complete solution to a text file
     */
    private void saveSolutionToFile() {
        // Let the user save even when there's no solution - just to save the statistics
        if (currentSolver == null) {
            JOptionPane.showMessageDialog(this,
                "No solver results available to save.", 
                "Save Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Additional check to make sure we have at least the initial state
        if (solutionSteps == null || solutionSteps.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No board state available to save.",
                "Save Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Solution Report");
        fileChooser.setSelectedFile(new File("rush_hour_solution.txt"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            // Add .txt extension if missing
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            
            try (FileWriter writer = new FileWriter(file)) {
                // Write the solution report
                writer.write(generateSolutionReport());
                
                JOptionPane.showMessageDialog(this,
                    "Solution saved to: " + file.getAbsolutePath(),
                    "Save Successful", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error saving solution: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates the complete solution report text
     */
    private String generateSolutionReport() {
        StringBuilder report = new StringBuilder();
        
        // Header
        report.append("Rush Hour Puzzle Solution\n");
        report.append("=========================\n\n");
        
        // Board information
        if (initialGameState != null) {
            Board board = initialGameState.getBoard();
            report.append("Board size: ").append(board.getGrid().length).append("x")
                .append(board.getGrid()[0].length).append("\n");
            
            // Exit information
            int exitX = board.getOutCoordX();
            int exitY = board.getOutCoordY();
            int exitSide = board.getExitSide();
            
            report.append("Exit position: (").append(exitX).append(",").append(exitY).append(")\n");
            report.append("Exit side: ");
            switch (exitSide) {
                case 0: report.append("Top\n"); break;
                case 1: report.append("Right\n"); break;
                case 2: report.append("Bottom\n"); break;
                case 3: report.append("Left\n"); break;
                default: report.append("Unknown\n");
            }
            report.append("\n");
        }
        
        // Algorithm info
        report.append("Algorithm: ").append(
            currentSolver.getAlgorithmName()
        ).append("\n");
        
        // Heuristic info
        if (currentSolver instanceof UCSolver) {
            report.append("Heuristic: None\n");
        } else if (currentSolver instanceof InformedSolver) {
            report.append("Heuristic: ")
                .append(((InformedSolver)currentSolver).getHeuristic().getName())
                .append("\n");
        }
        
        // Statistics
        List<Node> path = currentSolver.getSolutionPath();
        boolean hasSolution = path != null && !path.isEmpty();
        
        report.append("Solution: ").append(hasSolution ? "Found" : "Not found").append("\n");
        if (hasSolution) {
            report.append("Solution length: ").append(path.size() - 1).append(" moves\n");
        }
        report.append("Nodes visited: ").append(currentSolver.getNodesExplored()).append("\n");
        report.append("Maximum frontier size: ").append(currentSolver.getMaxQueueSize()).append("\n");
        report.append("Execution time: ").append(currentSolver.getExecutionTimeMs()).append(" ms\n\n");
        
        // Solution steps (only if we have a solution)
        if (hasSolution) {
            report.append("Solution Steps\n");
            report.append("=============\n\n");
            
            // Get move descriptions and board states
            for (int i = 0; i < path.size(); i++) {
                Node node = path.get(i);
                report.append("Step ").append(i).append(": ");
                
                // Add move description
                if (i > 0 && node.getMoveMade() != null) {
                    report.append(node.getMoveMade());
                } else if (i == 0) {
                    report.append("Initial state");
                } else {
                    report.append("Move to next state");
                }
                report.append("\n\n");
                
                // Add board representation
                if (i < solutionSteps.size()) {
                    List<String> boardLines = solutionSteps.get(i);
                    for (String line : boardLines) {
                        report.append(line).append("\n");
                    }
                    report.append("\n");
                }
            }
        } else {
            report.append("No solution found.\n");
            if (!solutionSteps.isEmpty()) {
                report.append("Initial state:\n\n");
                List<String> boardLines = solutionSteps.get(0);
                for (String line : boardLines) {
                    report.append(line).append("\n");
                }
            }
        }
        
        return report.toString();
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