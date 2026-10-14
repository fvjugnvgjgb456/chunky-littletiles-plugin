package dev.trae.chunky.littletiles;

/**
 * Represents a 3D axis-aligned bounding box in grid coordinates.
 * Uses half-open interval semantics: [min, max).
 * All coordinates are in grid units (0 to grid.count).
 */
public class LittleBox {
  
  public final int minX;
  public final int minY;
  public final int minZ;
  public final int maxX;
  public final int maxY;
  public final int maxZ;
  
  public LittleBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    this.minX = minX;
    this.minY = minY;
    this.minZ = minZ;
    this.maxX = maxX;
    this.maxY = maxY;
    this.maxZ = maxZ;
  }
  
  /**
   * Parse a LittleBox from NBT int array.
   * Expected format: [minX, minY, minZ, maxX, maxY, maxZ]
   * 
   * @param data int array with at least 6 elements
   * @return parsed LittleBox
   * @throws IllegalArgumentException if data length < 6
   */
  public static LittleBox fromArray(int[] data) {
    if (data == null || data.length < 6) {
      throw new IllegalArgumentException("LittleBox data must have at least 6 elements, got: " + 
          (data == null ? "null" : data.length));
    }
    return new LittleBox(data[0], data[1], data[2], data[3], data[4], data[5]);
  }
  
  /**
   * Check if this box is valid (min < max for all axes).
   */
  public boolean isValid() {
    return minX < maxX && minY < maxY && minZ < maxZ;
  }
  
  /**
   * Get the volume of this box in grid units.
   */
  public int getVolume() {
    return (maxX - minX) * (maxY - minY) * (maxZ - minZ);
  }
  
  @Override
  public String toString() {
    return String.format("LittleBox{[%d,%d,%d] → [%d,%d,%d]}", 
        minX, minY, minZ, maxX, maxY, maxZ);
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LittleBox that = (LittleBox) o;
    return minX == that.minX && minY == that.minY && minZ == that.minZ &&
           maxX == that.maxX && maxY == that.maxY && maxZ == that.maxZ;
  }
  
  @Override
  public int hashCode() {
    int result = minX;
    result = 31 * result + minY;
    result = 31 * result + minZ;
    result = 31 * result + maxX;
    result = 31 * result + maxY;
    result = 31 * result + maxZ;
    return result;
  }
}
