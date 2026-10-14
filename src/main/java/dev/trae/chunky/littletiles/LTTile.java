package dev.trae.chunky.littletiles;

/**
 * Represents a single LittleTiles tile with blockstate, color, and geometry.
 */
public class LTTile {
  
  public final String blockstate;
  public final int color;
  public final LittleBox box;
  
  public LTTile(String blockstate, int color, LittleBox box) {
    if (blockstate == null || blockstate.isEmpty()) {
      throw new IllegalArgumentException("Blockstate cannot be null or empty");
    }
    if (box == null) {
      throw new IllegalArgumentException("Box cannot be null");
    }
    this.blockstate = blockstate;
    this.color = color;
    this.box = box;
  }
  
  @Override
  public String toString() {
    return String.format("LTTile{blockstate='%s', color=0x%08X, box=%s}", 
        blockstate, color, box);
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LTTile ltTile = (LTTile) o;
    return color == ltTile.color && 
           blockstate.equals(ltTile.blockstate) && 
           box.equals(ltTile.box);
  }
  
  @Override
  public int hashCode() {
    int result = blockstate.hashCode();
    result = 31 * result + color;
    result = 31 * result + box.hashCode();
    return result;
  }
}
