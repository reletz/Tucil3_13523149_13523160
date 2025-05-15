import logic.Board;
import logic.GameState;
import util.GameManager;

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
            
            // TODO: Implement solver runners
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
        switch (choice) {
            case 1:
                System.out.println("Algoritma UCS dipilih");
                // TODO: Implement UCS
                break;
            case 2:
                System.out.println("Algoritma Greedy Best-First Search dipilih");
                // TODO: Implement Greedy
                break;
            case 3:
                System.out.println("Algoritma A* dipilih");
                // TODO: Implement A*
                break;
        }
    }
}
