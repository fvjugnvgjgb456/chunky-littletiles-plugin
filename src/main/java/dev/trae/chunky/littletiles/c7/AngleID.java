package dev.trae.chunky.littletiles.c7;

/**
 * Enumeration of the 8 corners of a box.
 * 
 * Naming convention: {E|W}{U|D}{N|S}
 * - E/W: East/West (X axis)
 * - U/D: Up/Down (Y axis)
 * - N/S: North/South (Z axis)
 * 
 * Order matches Minecraft-Littletiles-Reader AngleID enumeration.
 */
public enum AngleID {
  EUN(0, "East Up North"),
  EUS(1, "East Up South"),
  EDN(2, "East Down North"),
  EDS(3, "East Down South"),
  WUN(4, "West Up North"),
  WUS(5, "West Up South"),
  WDN(6, "West Down North"),
  WDS(7, "West Down South");
  
  private final int index;
  private final String description;
  
  AngleID(int index, String description) {
    this.index = index;
    this.description = description;
  }
  
  /**
   * Get the index of this corner (0-7).
   */
  public int getIndex() {
    return index;
  }
  
  /**
   * Get the human-readable description.
   */
  public String getDescription() {
    return description;
  }
  
  /**
   * Get AngleID by index.
   * 
   * @param index corner index (0-7)
   * @return corresponding AngleID
   * @throws IllegalArgumentException if index is out of range
   */
  public static AngleID fromIndex(int index) {
    for (AngleID angle : values()) {
      if (angle.index == index) {
        return angle;
      }
    }
    throw new IllegalArgumentException("Invalid AngleID index: " + index);
  }
  
  /**
   * Check if this corner is on the East side (X+).
   */
  public boolean isEast() {
    return this == EUN || this == EUS || this == EDN || this == EDS;
  }
  
  /**
   * Check if this corner is on the West side (X-).
   */
  public boolean isWest() {
    return !isEast();
  }
  
  /**
   * Check if this corner is on the Up side (Y+).
   */
  public boolean isUp() {
    return this == EUN || this == EUS || this == WUN || this == WUS;
  }
  
  /**
   * Check if this corner is on the Down side (Y-).
   */
  public boolean isDown() {
    return !isUp();
  }
  
  /**
   * Check if this corner is on the North side (Z+).
   */
  public boolean isNorth() {
    return this == EUN || this == EDN || this == WUN || this == WDN;
  }
  
  /**
   * Check if this corner is on the South side (Z-).
   */
  public boolean isSouth() {
    return !isNorth();
  }
  
  @Override
  public String toString() {
    return name() + "(" + index + ")";
  }
}
