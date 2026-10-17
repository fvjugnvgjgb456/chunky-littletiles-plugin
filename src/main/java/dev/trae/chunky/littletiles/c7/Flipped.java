package dev.trae.chunky.littletiles.c7;

/**
 * Represents face flip flags for a TransformableBox.
 * Each of the 6 faces can be flipped independently.
 * 
 * Face flip changes the normal direction and affects UV coordinates.
 * 
 * Based on Minecraft-Littletiles-Reader Flipped structure.
 * 
 * @see TransformableBox
 */
public class Flipped {
  
  // Face flip flags (bit 24-29 from indicator field)
  public final boolean down;   // bit 24
  public final boolean up;     // bit 25
  public final boolean north;  // bit 26
  public final boolean south;  // bit 27
  public final boolean west;   // bit 28
  public final boolean east;   // bit 29
  
  /**
   * Create a Flipped with all face flags.
   * 
   * @param down whether DOWN face is flipped (bit 24)
   * @param up whether UP face is flipped (bit 25)
   * @param north whether NORTH face is flipped (bit 26)
   * @param south whether SOUTH face is flipped (bit 27)
   * @param west whether WEST face is flipped (bit 28)
   * @param east whether EAST face is flipped (bit 29)
   */
  public Flipped(boolean down, boolean up, boolean north, 
                 boolean south, boolean west, boolean east) {
    this.down = down;
    this.up = up;
    this.north = north;
    this.south = south;
    this.west = west;
    this.east = east;
  }
  
  /**
   * Create a Flipped with no faces flipped.
   */
  public static Flipped none() {
    return new Flipped(false, false, false, false, false, false);
  }
  
  /**
   * Check if any face is flipped.
   */
  public boolean hasFlip() {
    return down || up || north || south || west || east;
  }
  
  @Override
  public String toString() {
    if (!hasFlip()) {
      return "Flipped{none}";
    }
    StringBuilder sb = new StringBuilder("Flipped{");
    if (down) sb.append("DOWN,");
    if (up) sb.append("UP,");
    if (north) sb.append("NORTH,");
    if (south) sb.append("SOUTH,");
    if (west) sb.append("WEST,");
    if (east) sb.append("EAST,");
    sb.setLength(sb.length() - 1); // remove trailing comma
    sb.append("}");
    return sb.toString();
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Flipped flipped = (Flipped) o;
    return down == flipped.down && up == flipped.up && 
           north == flipped.north && south == flipped.south &&
           west == flipped.west && east == flipped.east;
  }
  
  @Override
  public int hashCode() {
    int result = (down ? 1 : 0);
    result = 31 * result + (up ? 1 : 0);
    result = 31 * result + (north ? 1 : 0);
    result = 31 * result + (south ? 1 : 0);
    result = 31 * result + (west ? 1 : 0);
    result = 31 * result + (east ? 1 : 0);
    return result;
  }
}
