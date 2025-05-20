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
  public PrimaryPiece(char label, int size, boolean isHorizontal) {
    super(label, size, isHorizontal);
    this.isHighlighted = false;
  }
  
  /**
   * Bikin Block utama baru dengan label, bentuk, dan status highlight yang dikasih.
   * 
   * @param label Karakter yang dipake buat nunjukin Block ini
   * @param shape Bentuk 2D dari Block ini
   * @param isHighlighted Apakah Block ini perlu di-highlight atau nggak
   */
  public PrimaryPiece(char label, int size, boolean isHorizontal, boolean isHighlighted) {
    super(label, size, isHorizontal);
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
}