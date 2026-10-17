package dev.trae.chunky.littletiles.c7;

/**
 * Parses bit fields from LittleTiles transformable box NBT data.
 * 
 * 1.18+ NBT format (8+ field box):
 * [faceCache, minX, minY, minZ, maxX, maxY, maxZ, indicator, packed_offsets...]
 * 
 * Indicator field (int32, index 7):
 * - bit [0..23]: enable flags for 8 corners × 3 axes (x/y/z)
 *   - bit [i*3..(i*3+2)]: corner i's x/y/z enable flags (i=0..7)
 * - bit [24..29]: face flip flags (down/up/north/south/west/east)
 * - bit 31: negative flag (indicates TransformableBox)
 * 
 * Packed offsets (int32[], from index 8):
 * - Each int32 packs 2 int16 values: high 16 bits first, then low 16 bits
 * - Consumption order: AngleID ascending (EUN→EUS→...→WDS), then x→y→z within each corner
 * 
 * Based on Minecraft-Littletiles-Reader BlockTileEntities.cpp:SetAngleOffsetStateData()
 * 
 * @see TransformableBox
 * @see AngleOffset
 * @see Flipped
 */
public class BitFieldParser {
  
  /**
   * Parse result containing offsets and flipped flags.
   */
  public static class ParseResult {
    public final AngleOffset[] offsets;  // 8 corners
    public final Flipped flipped;
    
    public ParseResult(AngleOffset[] offsets, Flipped flipped) {
      this.offsets = offsets;
      this.flipped = flipped;
    }
  }
  
  /**
   * Parse transformable box bit fields from NBT int array.
   * 
   * @param data NBT int array with length >= 8 (1.18+ format with face cache)
   * @return parsed offsets and flipped flags
   * @throws IllegalArgumentException if data is invalid
   */
  public static ParseResult parse(int[] data) {
    if (data == null || data.length < 8) {
      throw new IllegalArgumentException(
          "TransformableBox data must have at least 8 elements, got: " + 
          (data == null ? "null" : data.length));
    }
    
    // 1.18+ format: indicator at index 7 (after face cache + 6 coords)
    int indicator = data[7];
    
    // Check bit 31 (negative flag)
    if (indicator >= 0) {
      throw new IllegalArgumentException(
          "Not a TransformableBox (indicator >= 0): " + indicator);
    }
    
    // Parse enable flags (bit 0-23: 8 corners × 3 axes)
    boolean[][] enableFlags = parseEnableFlags(indicator);
    
    // Parse face flip flags (bit 24-29)
    Flipped flipped = parseFlippedFlags(indicator);
    
    // Parse packed offsets (from index 8 onwards)
    int[] packedOffsets = new int[data.length - 8];
    System.arraycopy(data, 8, packedOffsets, 0, packedOffsets.length);
    
    // Unpack offsets
    AngleOffset[] offsets = unpackOffsets(enableFlags, packedOffsets);
    
    return new ParseResult(offsets, flipped);
  }
  
  /**
   * Parse enable flags from indicator field (bit 0-23).
   * 
   * @param indicator the indicator field (int32)
   * @return enable flags [corner][axis], where axis 0=x, 1=y, 2=z
   */
  private static boolean[][] parseEnableFlags(int indicator) {
    boolean[][] flags = new boolean[8][3];  // 8 corners × 3 axes
    
    for (int corner = 0; corner < 8; corner++) {
      for (int axis = 0; axis < 3; axis++) {
        int bitIndex = corner * 3 + axis;
        flags[corner][axis] = ((indicator >> bitIndex) & 1) == 1;
      }
    }
    
    return flags;
  }
  
  /**
   * Parse face flip flags from indicator field (bit 24-29).
   * 
   * @param indicator the indicator field (int32)
   * @return Flipped flags
   */
  private static Flipped parseFlippedFlags(int indicator) {
    boolean down  = ((indicator >> 24) & 1) == 1;
    boolean up    = ((indicator >> 25) & 1) == 1;
    boolean north = ((indicator >> 26) & 1) == 1;
    boolean south = ((indicator >> 27) & 1) == 1;
    boolean west  = ((indicator >> 28) & 1) == 1;
    boolean east  = ((indicator >> 29) & 1) == 1;
    
    return new Flipped(down, up, north, south, west, east);
  }
  
  /**
   * Unpack offsets from packed int32 array.
   * 
   * Each int32 packs 2 int16 values: high 16 bits first, then low 16 bits.
   * Consumption order: AngleID ascending, then x→y→z within each corner.
   * 
   * @param enableFlags enable flags [corner][axis]
   * @param packedOffsets packed int32 array
   * @return unpacked AngleOffset array (8 corners)
   * @throws IllegalArgumentException if not enough packed data
   */
  private static AngleOffset[] unpackOffsets(boolean[][] enableFlags, int[] packedOffsets) {
    // Count total enabled offsets
    int enabledCount = 0;
    for (int corner = 0; corner < 8; corner++) {
      for (int axis = 0; axis < 3; axis++) {
        if (enableFlags[corner][axis]) {
          enabledCount++;
        }
      }
    }
    
    // Check if we have enough packed data
    int requiredPackedCount = (enabledCount + 1) / 2;  // ceiling division
    if (packedOffsets.length < requiredPackedCount) {
      throw new IllegalArgumentException(
          String.format("Not enough packed offsets: need %d, got %d", 
                        requiredPackedCount, packedOffsets.length));
    }
    
    // Unpack all int16 values
    short[] unpackedValues = new short[enabledCount];
    for (int i = 0; i < enabledCount; i++) {
      int packedIndex = i / 2;
      if (i % 2 == 0) {
        // High 16 bits
        unpackedValues[i] = (short) ((packedOffsets[packedIndex] >> 16) & 0xFFFF);
      } else {
        // Low 16 bits
        unpackedValues[i] = (short) (packedOffsets[packedIndex] & 0xFFFF);
      }
    }
    
    // Consume unpacked values in order: AngleID ascending, x→y→z
    AngleOffset[] offsets = new AngleOffset[8];
    int valueIndex = 0;
    
    for (int corner = 0; corner < 8; corner++) {
      short xOffset = 0, yOffset = 0, zOffset = 0;
      
      if (enableFlags[corner][0]) {  // x
        xOffset = unpackedValues[valueIndex++];
      }
      if (enableFlags[corner][1]) {  // y
        yOffset = unpackedValues[valueIndex++];
      }
      if (enableFlags[corner][2]) {  // z
        zOffset = unpackedValues[valueIndex++];
      }
      
      offsets[corner] = new AngleOffset(
          enableFlags[corner][0],
          enableFlags[corner][1],
          enableFlags[corner][2],
          xOffset, yOffset, zOffset
      );
    }
    
    return offsets;
  }
}
