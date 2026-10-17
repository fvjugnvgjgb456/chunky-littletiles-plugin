package dev.trae.chunky.littletiles.c7;

import dev.trae.chunky.littletiles.LittleBox;

/**
 * Parser for legacy slice and deformed box formats.
 * 
 * Handles:
 * - 7 field + indicator >= 0: Legacy slice (old format)
 * - 8+ field + indicator >= 0: Deformed box (slopes, curves)
 * 
 * Current implementation: Extract first 6 fields as AABB, ignore deformation data.
 * 
 * Format: [minX, minY, minZ, maxX, maxY, maxZ, indicator, deformation_data...]
 */
public class LegacySliceParser implements BoxParser {
  
  @Override
  public LittleBox parse(int[] data) throws BoxParseException {
    if (data == null) {
      throw new BoxParseException("Box data is null");
    }
    
    // Must have at least 7 fields (face cache + 6 coords)
    if (data.length < 7) {
      throw new BoxParseException("Legacy slice requires at least 7 fields, got: " + data.length);
    }
    
    // 1.18+ format: [faceCache, minX, minY, minZ, maxX, maxY, maxZ, deformation...]
    // data[0] = face cache (rendering optimization, skip it)
    // data[1..6] = AABB coordinates
    int indicator = data.length > 7 ? data[7] : 0;
    
    // Legacy slice / deformed box: indicator >= 0 (or no indicator field)
    if (data.length > 7 && indicator < 0) {
      throw new BoxParseException("Not a legacy slice (indicator < 0)");
    }
    
    // Debug logging
    if (dev.trae.chunky.littletiles.DebugConfig.VERBOSE_BOX_PARSING) {
      StringBuilder sb = new StringBuilder();
      sb.append("LegacySlice raw data: [");
      for (int i = 0; i < data.length; i++) {
        if (i > 0) sb.append(", ");
        sb.append(data[i]);
      }
      sb.append("]");
      System.out.println(sb.toString());
      System.out.println(String.format(
          "  → Parsed AABB: [%d,%d,%d]→[%d,%d,%d]",
          data[1], data[2], data[3], data[4], data[5], data[6]
      ));
    }
    
    // Extract AABB from fields 1-6, skip face cache at data[0]
    return new LittleBox(data[1], data[2], data[3], data[4], data[5], data[6]);
  }
}
