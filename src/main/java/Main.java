import java.util.List;

import logic.Board;
import logic.GameManager;
import logic.GameState;
import logic.Node;
import solver.algorithm.Solver;
import solver.algorithm.UCSolver;

/**
 * Kelas Main untuk aplikasi Rush Hour Solver.
 * Entry point utama untuk program.
 */
public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("==== RUSH HOUR SOLVER ====");
            
            // Load konfigurasi
            GameState gameState = GameManager.loadGameState();
            if (gameState == null) {
                System.out.println("Gagal memuat konfigurasi. Program berhenti.");
                return;
            }
            
            // Tampilkan papan awal
            Board board = gameState.getBoard();
            System.out.println("\nKonfigurasi awal:");
            System.out.println("Ukuran papan: " + board.getGrid().length + "x" + board.getGrid()[0].length);
            System.out.println("Pintu keluar di: (" + board.getOutCoordX() + ", " + board.getOutCoordY() + ")");
            System.out.println("Konfigurasi papan:");
            board.printBoard();
            
            // Pilih dan jalankan algoritma
            int algorithmChoice = GameManager.showAlgorithmMenu();
            System.out.println("\nMenjalankan algoritma...");
            
            // Jalankan algoritma yang dipilih
            runSelectedAlgorithm(gameState, algorithmChoice);
            
        } catch (Exception e) {
            System.out.println("Terjadi kesalahan: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Menjalankan algoritma yang dipilih
     */
    private static void runSelectedAlgorithm(GameState gameState, int choice) {    
        Solver solver = null;

        switch (choice) {
            case 1:
                System.out.println("Algoritma UCS dipilih");
                solver = new UCSolver();
                break;
            case 2:
                System.out.println("Algoritma Greedy Best-First Search dipilih");
                // TODO: Implement Greedy
                System.out.println("Algoritma belum diimplementasikan");
                return;
            case 3:
                System.out.println("Algoritma A* dipilih");
                // TODO: Implement A*
                System.out.println("Algoritma belum diimplementasikan");
                return;
            default:
                System.out.println("Pilihan tidak valid");
                return;
        }
        
        // Jalankan solver dan ukur waktu eksekusi
        long startTime = System.currentTimeMillis();
        Node solution = solver.solve(gameState);
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Tampilkan hasil
        if (solution != null) {
            List<Node> path = solver.getSolutionPath();
            
            System.out.println("\n======== SOLUSI DITEMUKAN ========");
            System.out.println("Jumlah langkah: " + (path.size() - 1));
            System.out.println("Jumlah simpul yang dibuat: " + solver.getNodesExplored());
            System.out.println("Waktu eksekusi: " + solver.getExecutionTimeMs() + " ms");
            
            System.out.println("\n======== LANGKAH-LANGKAH ========");
            for (int i = 0; i < path.size(); i++) {
                Node node = path.get(i);
                System.out.println("Langkah " + i + ": ");
                node.getState().getBoard().printBoard();
                System.out.println();
            }
            
            // Tampilkan konfigurasi akhir
            System.out.println("\n======== KONFIGURASI AKHIR ========");
            solution.getState().getBoard().printBoard();
        } else {
            System.out.println("\nTidak ditemukan solusi!");
            System.out.println("Jumlah simpul yang dibuat: " + solver.getNodesExplored());
            System.out.println("Waktu eksekusi: " + solver.getExecutionTimeMs() + " ms");
        }
    }
}