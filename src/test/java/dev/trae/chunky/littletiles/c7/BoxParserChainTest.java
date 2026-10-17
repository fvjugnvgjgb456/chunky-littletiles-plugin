package dev.trae.chunky.littletiles.c7;

import dev.trae.chunky.littletiles.LittleBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the parser chain behavior (TransformableBoxParser → BasicBoxParser).
 */
class BoxParserChainTest {
  
  @Test
  void testBasicBoxParser_6Fields() throws BoxParser.BoxParseException {
    BasicBoxParser parser = new BasicBoxParser();
    int[] data = {0, 0, 0, 8, 8, 8};
    
    LittleBox box = parser.parse(data);
    
    assertNotNull(box);
    assertEquals(0, box.minX);
    assertEquals(8, box.maxX);
  }
  
  @Test
  void testBasicBoxParser_rejects7Fields() {
    BasicBoxParser parser = new BasicBoxParser();
    int[] data = {0, 0, 0, 8, 8, 8, 0x80000000};
    
    assertThrows(BoxParser.BoxParseException.class, () -> parser.parse(data));
  }
  
  @Test
  void testTransformableBoxParser_rejectsBasicBox() {
    TransformableBoxParser parser = new TransformableBoxParser();
    int[] data = {0, 0, 0, 8, 8, 8};
    
    assertThrows(BoxParser.BoxParseException.class, () -> parser.parse(data));
  }
  
  @Test
  void testTransformableBoxParser_rejectsPositiveIndicator() {
    TransformableBoxParser parser = new TransformableBoxParser();
    int[] data = {0, 0, 0, 8, 8, 8, 0x3F000000}; // bit 31 = 0
    
    assertThrows(BoxParser.BoxParseException.class, () -> parser.parse(data));
  }
  
  @Test
  void testTransformableBoxParser_acceptsNegativeIndicator() throws BoxParser.BoxParseException {
    TransformableBoxParser parser = new TransformableBoxParser();
    // 1.18+ format: [faceCache, minX, minY, minZ, maxX, maxY, maxZ, indicator]
    // Golden case from README: no offsets enabled (to minimize packed data)
    int[] data = {0, 0, 0, 0, 8, 8, 8, 0xBF000000}; // face cache + coords + indicator (bit 31 = 1, bits 0-23 = 0)
    
    LittleBox box = parser.parse(data);
    
    assertNotNull(box);
    assertTrue(box instanceof TransformableBox);
    TransformableBox tbox = (TransformableBox) box;
    
    // Check basic coordinates
    assertEquals(0, tbox.minX);
    assertEquals(8, tbox.maxX);
    
    // Check flipped flags (bits 24-29 all set)
    Flipped flipped = tbox.getFlipped();
    assertTrue(flipped.down);
    assertTrue(flipped.up);
    assertTrue(flipped.north);
    assertTrue(flipped.south);
    assertTrue(flipped.west);
    assertTrue(flipped.east);
    
    // All offsets should be disabled
    for (AngleOffset offset : tbox.getOffsets()) {
      assertFalse(offset.hasOffset(), "No offsets should be enabled");
    }
  }
  
  @Test
  void testParserChain_basicBoxFallsBackToBasicParser() {
    // Simulate LTNBTParser behavior: try TransformableBoxParser first, then BasicBoxParser
    int[] data = {0, 0, 0, 8, 8, 8};
    LittleBox result = null;
    
    TransformableBoxParser transformableParser = new TransformableBoxParser();
    try {
      result = transformableParser.parse(data);
    } catch (BoxParser.BoxParseException e) {
      // Expected: transformable parser rejects 6-field box
      BasicBoxParser basicParser = new BasicBoxParser();
      try {
        result = basicParser.parse(data);
      } catch (BoxParser.BoxParseException e2) {
        fail("BasicBoxParser should handle 6-field box");
      }
    }
    
    assertNotNull(result);
    assertFalse(result instanceof TransformableBox, "Should be basic LittleBox");
  }
}
