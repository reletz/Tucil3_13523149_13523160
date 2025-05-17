package logic;

import java.util.Objects;

/**
 * Kelas Piece ini buat nampung Block yang bakal ditaruh di papan.
 * Masing-masing Block punya label dan orientasi khusus.
 */
public class Piece {
  private final char label;           // Character identifier for this piece
  private final int size;             // Length of the piece (2 or 3 units)
  private final boolean isHorizontal; // Orientation: true for horizontal, false for vertical

  /**
   * Bikin Block baru dengan properti yang ditentukan.
   * 
   * @param label Karakter yang dipake buat nunjukin Block ini
   * @param size Panjang dari Block (2 atau 3 unit)
   * @param isHorizontal Orientasi (true untuk horizontal, false untuk vertikal)
   */
  public Piece(char label, int size, boolean isHorizontal) {
    this.label = label;
    this.size = size;
    this.isHorizontal = isHorizontal;
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
   * Ngambil ukuran dari Block ini.
   * 
   * @return Panjang Block (2 atau 3 unit)
   */
  public int getSize() {
    return size;
  }

  /**
   * Ngecek apakah Block ini orientasinya horizontal.
   * 
   * @return true jika horizontal, false jika vertikal
   */
  public boolean isHorizontal() {
    return isHorizontal;
  }

  /**
   * Membuat salinan dari Block ini.
   * 
   * @return Block baru dengan properti yang sama
   */
  public Piece copy() {
    return new Piece(label, size, isHorizontal);
  }

  /**
   * Membandingkan Block ini dengan objek lain.
   * 
   * @param obj Objek yang dibandingkan
   * @return true jika Block memiliki properti yang sama
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    
    Piece other = (Piece) obj;
    
    return 
          this.label == other.label &&
          this.size == other.size &&
          this.isHorizontal == other.isHorizontal;
  }

  /**
   * Menghasilkan hash code berdasarkan properti Block.
   * 
   * @return Hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(label, size, isHorizontal);
  }
  
  /**
   * Menghasilkan representasi string dari Block ini.
   * 
   * @return String yang menggambarkan Block
   */
  @Override
  public String toString() {
    String orientation = isHorizontal ? "horizontal" : "vertikal";
    return "Block " + label + " (" + orientation + ", ukuran " + size + ")";
  }
}