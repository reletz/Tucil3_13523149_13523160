package logic;

/**
 * Kelas Board ini buat nampung papan permainan.
 * Isinya grid buat nyimpen block-block yang
 * ada di papan permainan.
 */
public class Board {
  final private int rows, cols;
  final private char[][] grid;
  final private int[] outCoord = new int[2];

  /**
   * Bikin papan permainan baru dengan ukuran yang kamu mau.
   * 
   * @param rows Jumlah baris di papan
   * @param cols Jumlah kolom di papan
   * @param kX Koordinat X buat keluar
   * @param kY Koordinat Y buat keluar
   */
  public Board(int rows, int cols, int kX, int kY) {
    this.rows = rows;
    this.cols = cols;
    this.grid = new char[rows][cols];
    this.outCoord[0] = kX;
    this.outCoord[1] = kY;

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        grid[i][j] = ' ';
      }
    }
  }

  /**
   * Ngeprint papan ke layar konsol.
   * Buat nampilin gimana bentuk papannya sekarang.
   */
  public void printBoard() {
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        System.out.print(grid[i][j]);
      }
      System.out.println();
    }
  }

  /**
   * Ngambil koordinat jalan keluar dari papan.
   * 
   * @return Array koordinat [x, y] jalan keluar
   */
  public int[] getOutCoord(){
    return outCoord;
  }

  /**
   * Ngambil koordinat X jalan keluar.
   * 
   * @return Nilai X dari jalan keluar
   */
  public int getOutCoordX(){
    return outCoord[0];
  }

  /**
   * Ngambil koordinat Y jalan keluar.
   * 
   * @return Nilai Y dari jalan keluar
   */
  public int getOutCoordY(){
    return outCoord[1];
  }

  /**
   * Ngambil grid papan permainan.
   * 
   * @return Array 2D yang isinya karakter di papan
   */
  public char[][] getGrid(){
    return grid;
  }

  /**
   * Set grid papan permainan.
   * 
   * @param Array 2D yang isinya karakter di papan
   */
  public void setGrid(char[][] newGrid) {
    for (int i = 0; i < grid.length; i++) {
      System.arraycopy(newGrid[i], 0, grid[i], 0, grid[i].length);
    }
  }
}