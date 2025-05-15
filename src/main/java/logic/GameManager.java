package util;

import logic.GameState;

import java.io.File;
import java.net.URL;
import java.util.Scanner;

/**
 * Kelas GameManager buat mengatur operasi dasar permainan.
 * Menghandle loading konfigurasi dan interaksi menu.
 */
public class GameManager {
    private static final Scanner scanner = new Scanner(System.in);
    
    /**
     * Memuat konfigurasi permainan berdasarkan input user
     */
    public static GameState loadGameState() {
        try {
            System.out.print("Masukkan path file konfigurasi: ");
            String configPath = scanner.nextLine().trim();
            
            // Cek apakah file ada
            File configFile = new File(configPath);
            if (!configFile.exists()) {
                // Coba cari di resources jika file tidak ditemukan langsung
                try {
                    configPath = getResourcePath("config/" + configPath);
                } catch (Exception e) {
                    System.out.println("File tidak ditemukan di resources. Menggunakan path asli.");
                }
            }
            
            System.out.println("Memuat konfigurasi dari: " + configPath);
            return ConfigParser.parseConfig(configPath);
            
        } catch (Exception e) {
            System.out.println("Error saat memuat konfigurasi: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Menampilkan menu pilihan algoritma dan menjalankan solver
     * @return Algoritma yang dipilih (1-3)
     */
    public static int showAlgorithmMenu() {
        System.out.println("\nPilih algoritma yang akan digunakan:");
        System.out.println("1. Uniform Cost Search (UCS)");
        System.out.println("2. Greedy Best-First Search");
        System.out.println("3. A* Search");
        System.out.print("Pilihan: ");
        
        int choice = 0;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice < 1 || choice > 3) {
                System.out.println("Input tidak valid, menggunakan UCS.");
                choice = 1;
            }
        } catch (NumberFormatException e) {
            System.out.println("Input tidak valid, menggunakan UCS.");
            choice = 1;
        }
        
        return choice;
    }
    
    /**
     * Mendapatkan path absolut dari resource
     */
    public static String getResourcePath(String relativePath) {
        try {
            ClassLoader classLoader = GameManager.class.getClassLoader();
            URL resourceUrl = classLoader.getResource(relativePath);
            
            if (resourceUrl == null) {
                throw new RuntimeException("File tidak ditemukan: " + relativePath);
            }
            
            return new File(resourceUrl.toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("Gagal mendapatkan path resource: " + e.getMessage(), e);
        }
    }
}