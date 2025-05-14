package logic;

/**
 * Kelas PrimaryPiece ini buat bikin Block utama di papan.
 * Block utama dipake buat dikeluarin (?? awokawok)
 */
public class PrimaryPiece extends Piece {
  
  // Status highlight dari Block utama ini
  private final boolean isHighlighted;
  
  /**
   * Bikin Block utama baru dengan label dan bentuk yang dikasih.
   * 
   * @param label Karakter yang dipake buat nunjukin Block ini
   * @param shape Bentuk 2D dari Block ini
   */
  public PrimaryPiece(char label, boolean[][] shape) {
    super(label, shape);
    this.isHighlighted = false;
  }
  
  /**
   * Bikin Block utama baru dengan label, bentuk, dan status highlight yang dikasih.
   * 
   * @param label Karakter yang dipake buat nunjukin Block ini
   * @param shape Bentuk 2D dari Block ini
   * @param isHighlighted Apakah Block ini perlu di-highlight atau nggak
   */
  public PrimaryPiece(char label, boolean[][] shape, boolean isHighlighted) {
    super(label, shape);
    this.isHighlighted = isHighlighted;
  }
  
  /**
   * Ngecek apakah Block ini lagi di-highlight atau nggak.
   * 
   * @return True kalo Block lagi di-highlight, false kalo nggak
   */
  public boolean isHighlighted() {
    return isHighlighted;
  }
  
  /**
   * Nampilin Block ke layar konsol dengan gaya khusus kalo di-highlight.
   * Block yang di-highlight bakal keliatan beda dari Block biasa.
   * 
   * @implNote Block yang di-highlight ditampilin pake tanda bintang (*) ngapit labelnya,
   *           sedangkan Block yang nggak di-highlight pake tampilan standar dari parent class.
   */
  @Override
  public void printBlock() {
    if (!isHighlighted) {
      super.printBlock();
      return;
    }
    
    // Tampilan khusus buat Block yang di-highlight
    for (boolean[] row : getShape()) {
      for (boolean cell : row) {
        // Format khusus buat Block yang di-highlight (contoh: pake *)
        System.out.print(cell ? "*" + getLabel() + "*" : "   ");
      }
      System.out.println();
    }
  }
}