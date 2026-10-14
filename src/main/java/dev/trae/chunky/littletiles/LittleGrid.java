package dev.trae.chunky.littletiles;

/**
 * Represents the grid precision for LittleTiles structures.
 * Default count is 16, meaning each block is divided into 16x16x16 sub-voxels.
 */
public class LittleGrid {
  
  public static final int DEFAULT_COUNT = 16;
  
  public final int count;
  public final double pixelLength;
  
  public LittleGrid(int count) {
    if (count <= 0 || (count & (count - 1)) != 0) {
      throw new IllegalArgumentException("Grid count must be a positive power of 2, got: " + count);
    }
    this.count = count;
    this.pixelLength = 1.0 / count;
  }
  
  public static LittleGrid getDefault() {
    return new LittleGrid(DEFAULT_COUNT);
  }
  
  public boolean isDefault() {
    return count == DEFAULT_COUNT;
  }
  
  @Override
  public String toString() {
    return "LittleGrid{count=" + count + ", pixelLength=" + pixelLength + "}";
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LittleGrid that = (LittleGrid) o;
    return count == that.count;
  }
  
  @Override
  public int hashCode() {
    return Integer.hashCode(count);
  }
}
