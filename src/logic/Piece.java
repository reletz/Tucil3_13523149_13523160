package logic;

/**
 * Kelas Piece ini buat nampung Block yang bakal ditaruh di papan.
 * Masing-masing Block punya label dan bentuk khusus.
 */
public class Piece {
  final private char label;
  final private boolean[][] shape;

  /**
   * Bikin Block baru dengan label dan bentuk yang dikasih.
   * 
   * @param label Karakter yang dipake buat nunjukin Block ini
   * @param shape Bentuk 2D dari Block ini (true artinya ada, false artinya kosong)
   */
  public Piece(char label, boolean[][] shape) {
    this.label = label;
    this.shape = shape;
  }

  /**
   * Ngambil label dari Block ini.
   * 
   * @return Karakter label Block
   */
  public char getLabel() {
    return label;
  }

  /**
   * Ngambil bentuk dari Block ini.
   * 
   * @return Array 2D yang nunjukin bentuk Block
   */
  public boolean[][] getShape() {
    return shape;
  }

  /**
   * Nampilin Block ke layar konsol.
   * Buat ngeliat gimana bentuk Blocknya kalo diprint.
   */
  public void printBlock() {
    for (boolean[] row: shape) {
      for (boolean cell: row) {
        System.out.print(cell ? label : ' ');
      }
      System.out.println();
    }
  }
}
