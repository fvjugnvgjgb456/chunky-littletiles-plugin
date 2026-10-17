package dev.trae.chunky.littletiles.c7;

/**
 * Represents per-corner offset for a TransformableBox.
 * Each of the 8 corners can have independent x/y/z offsets.
 * 
 * Based on Minecraft-Littletiles-Reader AngleOffset structure.
 * 
 * @see TransformableBox
 */
public class AngleOffset {
  
  // Enable flags (bit flags from indicator field)
  public final boolean xEnable;
  public final boolean yEnable;
  public final boolean zEnable;
  
  // Offset values (int16, from packed int32 array)
  public final short xOffset;
  public final short yOffset;
  public final short zOffset;
  
  /**
   * Create an AngleOffset with all fields.
   * 
   * @param xEnable whether x offset is enabled
   * @param yEnable whether y offset is enabled
   * @param zEnable whether z offset is enabled
   * @param xOffset x offset value (int16)
   * @param yOffset y offset value (int16)
   * @param zOffset z offset value (int16)
   */
  public AngleOffset(boolean xEnable, boolean yEnable, boolean zEnable,
                     short xOffset, short yOffset, short zOffset) {
    this.xEnable = xEnable;
    this.yEnable = yEnable;
    this.zEnable = zEnable;
    this.xOffset = xOffset;
    this.yOffset = yOffset;
    this.zOffset = zOffset;
  }
  
  /**
   * Create a zero offset (no offset enabled).
   */
  public static AngleOffset zero() {
    return new AngleOffset(false, false, false, (short) 0, (short) 0, (short) 0);
  }
  
  /**
   * Check if this offset has any non-zero component.
   */
  public boolean hasOffset() {
    return xEnable || yEnable || zEnable;
  }
  
  /**
   * Get the effective x offset (0 if disabled).
   */
  public int getEffectiveX() {
    return xEnable ? xOffset : 0;
  }
  
  /**
   * Get the effective y offset (0 if disabled).
   */
  public int getEffectiveY() {
    return yEnable ? yOffset : 0;
  }
  
  /**
   * Get the effective z offset (0 if disabled).
   */
  public int getEffectiveZ() {
    return zEnable ? zOffset : 0;
  }
  
  @Override
  public String toString() {
    return String.format("AngleOffset{x=%s(%d), y=%s(%d), z=%s(%d)}",
        xEnable ? "E" : "-", xOffset,
        yEnable ? "E" : "-", yOffset,
        zEnable ? "E" : "-", zOffset);
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AngleOffset that = (AngleOffset) o;
    return xEnable == that.xEnable && yEnable == that.yEnable && zEnable == that.zEnable &&
           xOffset == that.xOffset && yOffset == that.yOffset && zOffset == that.zOffset;
  }
  
  @Override
  public int hashCode() {
    int result = (xEnable ? 1 : 0);
    result = 31 * result + (yEnable ? 1 : 0);
    result = 31 * result + (zEnable ? 1 : 0);
    result = 31 * result + xOffset;
    result = 31 * result + yOffset;
    result = 31 * result + zOffset;
    return result;
  }
}
