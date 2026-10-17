package dev.trae.chunky.littletiles.c7;

import dev.trae.chunky.littletiles.LittleBox;

/**
 * Basic box parser for 6-field AABB format (C1-C6 logic).
 * 
 * Parses: [minX, minY, minZ, maxX, maxY, maxZ]
 * 
 * This parser is isolated from C7 changes and preserves the original C1-C6 behavior.
 */
public class BasicBoxParser implements BoxParser {
  
  @Override
  public LittleBox parse(int[] data) throws BoxParseException {
    if (data == null) {
      throw new BoxParseException("Box data is null");
    }
    
    if (data.length != 6) {
      throw new BoxParseException("Basic box requires exactly 6 fields, got: " + data.length);
    }
    
    // Original C1-C6 logic: direct AABB construction
    return new LittleBox(data[0], data[1], data[2], data[3], data[4], data[5]);
  }
}
