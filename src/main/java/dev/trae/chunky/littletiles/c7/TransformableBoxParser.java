package dev.trae.chunky.littletiles.c7;

import dev.trae.chunky.littletiles.DebugConfig;
import dev.trae.chunky.littletiles.LittleBox;

/**
 * Transformable box parser for 8+ field format with angle offsets (C7).
 * 
 * 1.18+ format: [faceCache, minX, minY, minZ, maxX, maxY, maxZ, indicator, packed_offsets...]
 * 
 * Format validation:
 * - Length >= 8 (face cache + 6 coords + indicator)
 * - indicator (field 7) < 0 (bit 31 set)
 * 
 * Legacy slice format (7 fields or indicator >= 0) is NOT handled here.
 */
public class TransformableBoxParser implements BoxParser {
  
  @Override
  public LittleBox parse(int[] data) throws BoxParseException {
    // Check minimum length (face cache + 6 coords + indicator)
    if (data == null || data.length < 8) {
      throw new BoxParseException("Not a transformable box (length < 8)");
    }
    
    // 1.18+ format: indicator is at index 7
    int indicator = data[7];
    
    // Check indicator bit 31 (negative flag)
    if (indicator >= 0) {
      throw new BoxParseException("Not a transformable box (indicator >= 0)");
    }
    
    // Check feature toggle
    if (!DebugConfig.ENABLE_TRANSFORMABLE_BOX) {
      throw new BoxParseException("Transformable box feature disabled");
    }
    
    try {
      // Parse bit fields (skip face cache at data[0])
      BitFieldParser.ParseResult result = BitFieldParser.parse(data);
      
      // Create TransformableBox (skip face cache at data[0])
      TransformableBox box = new TransformableBox(
          data[1], data[2], data[3],  // min (skip data[0] = face cache)
          data[4], data[5], data[6],  // max
          result.offsets,
          result.flipped
      );
      
      if (DebugConfig.VERBOSE_NBT_PARSER) {
        System.out.println("TransformableBoxParser: parsed box with " + 
                           countEnabledOffsets(result.offsets) + " enabled offsets, " +
                           "flipped=" + result.flipped.hasFlip());
      }
      
      return box;
      
    } catch (IllegalArgumentException e) {
      throw new BoxParseException("Failed to parse transformable box: " + e.getMessage());
    }
  }
  
  private int countEnabledOffsets(AngleOffset[] offsets) {
    int count = 0;
    for (AngleOffset offset : offsets) {
      if (offset.hasOffset()) {
        count++;
      }
    }
    return count;
  }
}
