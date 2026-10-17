package dev.trae.chunky.littletiles.c7;

import dev.trae.chunky.littletiles.LittleBox;

/**
 * Represents a transformable box with per-corner offsets and face flips.
 * 
 * Extends LittleBox (basic AABB) with deformation data:
 * - 8 corner offsets (AngleOffset array)
 * - 6 face flip flags (Flipped)
 * 
 * Based on Minecraft-Littletiles-Reader TileEntity structure.
 * 
 * NBT format (7+ field box):
 * [minX, minY, minZ, maxX, maxY, maxZ, indicator, packed_offsets...]
 * 
 * @see LittleBox
 * @see AngleOffset
 * @see Flipped
 */
public class TransformableBox extends LittleBox {
  
  private final AngleOffset[] offsets;  // 8 corners (AngleID order)
  private final Flipped flipped;
  
  /**
   * Create a TransformableBox with offsets and flips.
   * 
   * @param minX min X in grid units
   * @param minY min Y in grid units
   * @param minZ min Z in grid units
   * @param maxX max X in grid units
   * @param maxY max Y in grid units
   * @param maxZ max Z in grid units
   * @param offsets 8 corner offsets (indexed by AngleID)
   * @param flipped face flip flags
   */
  public TransformableBox(int minX, int minY, int minZ, 
                          int maxX, int maxY, int maxZ,
                          AngleOffset[] offsets, Flipped flipped) {
    super(minX, minY, minZ, maxX, maxY, maxZ);
    this.offsets = offsets;
    this.flipped = flipped;
  }
  
  /**
   * Get the angle offset for a specific corner.
   * 
   * @param corner the corner angle ID
   * @return the angle offset for that corner
   */
  public AngleOffset getOffset(AngleID corner) {
    return offsets[corner.ordinal()];
  }
  
  /**
   * Get all angle offsets (8 corners).
   * 
   * @return array of offsets indexed by AngleID ordinal
   */
  public AngleOffset[] getOffsets() {
    return offsets;
  }
  
  /**
   * Get the face flip flags.
   * 
   * @return flipped face flags
   */
  public Flipped getFlipped() {
    return flipped;
  }
  
  /**
   * Check if this box has any corner offset enabled.
   */
  public boolean hasOffsets() {
    for (AngleOffset offset : offsets) {
      if (offset.hasOffset()) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Check if this box is actually transformable (has offsets or flips).
   */
  public boolean isTransformable() {
    return hasOffsets() || flipped.hasFlip();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("TransformableBox{");
    sb.append("base=").append(super.toString());
    sb.append(", offsets=[");
    for (int i = 0; i < 8; i++) {
      if (offsets[i].hasOffset()) {
        sb.append(AngleID.fromIndex(i)).append(":").append(offsets[i]).append(",");
      }
    }
    if (hasOffsets()) {
      sb.setLength(sb.length() - 1);  // remove trailing comma
    }
    sb.append("]");
    sb.append(", flipped=").append(flipped);
    sb.append("}");
    return sb.toString();
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TransformableBox)) return false;
    if (!super.equals(o)) return false;
    
    TransformableBox that = (TransformableBox) o;
    if (!java.util.Arrays.equals(offsets, that.offsets)) return false;
    return flipped.equals(that.flipped);
  }
  
  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + java.util.Arrays.hashCode(offsets);
    result = 31 * result + flipped.hashCode();
    return result;
  }
}
