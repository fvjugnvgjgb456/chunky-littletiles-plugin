package dev.trae.chunky.littletiles.c7;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BitFieldParser.
 * 
 * Test data extracted from Minecraft-Littletiles-Reader README.md verification example.
 * The reference implementation (C++, MATLAB, Python) has verified these results.
 */
class BitFieldParserTest {
  
  /**
   * Golden test case from README.md.
   * 
   * SNBT: {tiles:[{bBox:[I;0,0,0,2,1,2,-2135499923,-65537,65537,...]}], grid:2, ...}
   * 
   * Indicator: -2135499923 (0x80B6DB6D in hex, 0b10000000101101101101101101101 in binary)
   * 
   * Expected offsets (verified by C++/MATLAB/Python):
   * - EUN(0): x=-2, y=0, z=-1
   * - EUS(1): x=2,  y=0, z=1
   * - EDN(2): x=-1, y=0, z=-1
   * - EDS(3): x=1,  y=0, z=1
   * - WUN(4): x=-2, y=0, z=-1
   * - WUS(5): x=2,  y=0, z=1
   * - WDN(6): x=-2, y=0, z=-1
   * - WDS(7): x=2,  y=0, z=1
   */
  @Test
  void testGoldenCase_README() {
    // Box array from README (1.18+ format with face cache)
    // Indicator 0x80B6DB6D enables x and z for all 8 corners (y disabled)
    // That's 16 int16 values, packed into 8 int32
    // Offsets: EUN(-2,-1), EUS(2,1), EDN(-1,-1), EDS(1,1), WUN(-2,-1), WUS(2,1), WDN(-2,-1), WDS(2,1)
    int[] data = {
      0,                          // face cache (1.18+ format)
      0, 0, 0, 2, 1, 2,          // minX, minY, minZ, maxX, maxY, maxZ
      -2135499923,                // indicator (0x80B6DB6D)
      // Pack offsets: [xOffset, zOffset] for each corner (EUN→EUS→...→WDS)
      // EUN: x=-2, z=-1 → pack(-2, -1)
      ((-2 & 0xFFFF) << 16) | (-1 & 0xFFFF),  // 0xFFFEFFFF = -65537
      // EUS: x=2, z=1 → pack(2, 1)
      ((2 & 0xFFFF) << 16) | (1 & 0xFFFF),     // 0x00020001 = 131073
      // EDN: x=-1, z=-1 → pack(-1, -1)
      ((-1 & 0xFFFF) << 16) | (-1 & 0xFFFF),   // 0xFFFFFFFF = -1
      // EDS: x=1, z=1 → pack(1, 1)
      ((1 & 0xFFFF) << 16) | (1 & 0xFFFF),     // 0x00010001 = 65537
      // WUN: x=-2, z=-1 → pack(-2, -1)
      ((-2 & 0xFFFF) << 16) | (-1 & 0xFFFF),   // 0xFFFEFFFF = -65537
      // WUS: x=2, z=1 → pack(2, 1)
      ((2 & 0xFFFF) << 16) | (1 & 0xFFFF),     // 0x00020001 = 131073
      // WDN: x=-2, z=-1 → pack(-2, -1)
      ((-2 & 0xFFFF) << 16) | (-1 & 0xFFFF),   // 0xFFFEFFFF = -65537
      // WDS: x=2, z=1 → pack(2, 1)
      ((2 & 0xFFFF) << 16) | (1 & 0xFFFF)      // 0x00020001 = 131073
    };
    
    BitFieldParser.ParseResult result = BitFieldParser.parse(data);
    
    // Verify EUN (corner 0): x=-2, y=0(disabled), z=-1
    AngleOffset eun = result.offsets[0];
    assertTrue(eun.xEnable, "EUN x should be enabled");
    assertFalse(eun.yEnable, "EUN y should be disabled");
    assertTrue(eun.zEnable, "EUN z should be enabled");
    assertEquals(-2, eun.xOffset, "EUN x offset");
    assertEquals(0, eun.yOffset, "EUN y offset");
    assertEquals(-1, eun.zOffset, "EUN z offset");
    
    // Verify EUS (corner 1): x=2, y=0(disabled), z=1
    AngleOffset eus = result.offsets[1];
    assertTrue(eus.xEnable);
    assertFalse(eus.yEnable);
    assertTrue(eus.zEnable);
    assertEquals(2, eus.xOffset);
    assertEquals(0, eus.yOffset);
    assertEquals(1, eus.zOffset);
    
    // Verify EDN (corner 2): x=-1, y=0(disabled), z=-1
    AngleOffset edn = result.offsets[2];
    assertTrue(edn.xEnable);
    assertFalse(edn.yEnable);
    assertTrue(edn.zEnable);
    assertEquals(-1, edn.xOffset);
    assertEquals(-1, edn.zOffset);
    
    // Verify EDS (corner 3): x=1, y=0(disabled), z=1
    AngleOffset eds = result.offsets[3];
    assertTrue(eds.xEnable);
    assertFalse(eds.yEnable);
    assertTrue(eds.zEnable);
    assertEquals(1, eds.xOffset);
    assertEquals(1, eds.zOffset);
    
    // Verify WUN (corner 4): x=-2, y=0(disabled), z=-1
    AngleOffset wun = result.offsets[4];
    assertTrue(wun.xEnable);
    assertFalse(wun.yEnable);
    assertTrue(wun.zEnable);
    assertEquals(-2, wun.xOffset);
    assertEquals(-1, wun.zOffset);
    
    // Verify WUS (corner 5): x=2, y=0(disabled), z=1
    AngleOffset wus = result.offsets[5];
    assertTrue(wus.xEnable);
    assertFalse(wus.yEnable);
    assertTrue(wus.zEnable);
    assertEquals(2, wus.xOffset);
    assertEquals(1, wus.zOffset);
    
    // Verify WDN (corner 6): x=-2, y=0(disabled), z=-1
    AngleOffset wdn = result.offsets[6];
    assertTrue(wdn.xEnable);
    assertFalse(wdn.yEnable);
    assertTrue(wdn.zEnable);
    assertEquals(-2, wdn.xOffset);
    assertEquals(-1, wdn.zOffset);
    
    // Verify WDS (corner 7): x=2, y=0(disabled), z=1
    AngleOffset wds = result.offsets[7];
    assertTrue(wds.xEnable);
    assertFalse(wds.yEnable);
    assertTrue(wds.zEnable);
    assertEquals(2, wds.xOffset);
    assertEquals(1, wds.zOffset);
    
    // Verify flipped flags
    // Indicator 0x80B6DB6D, bits 24-29 are all 0 (no face flips)
    Flipped flipped = result.flipped;
    assertFalse(flipped.down,  "DOWN should not be flipped (bit 24 = 0)");
    assertFalse(flipped.up,    "UP should not be flipped (bit 25 = 0)");
    assertFalse(flipped.north, "NORTH should not be flipped (bit 26 = 0)");
    assertFalse(flipped.south, "SOUTH should not be flipped (bit 27 = 0)");
    assertFalse(flipped.west,  "WEST should not be flipped (bit 28 = 0)");
    assertFalse(flipped.east,  "EAST should not be flipped (bit 29 = 0)");
  }
  
  /**
   * Test simple case: all corners have same offset.
   */
  @Test
  void testAllCornersEnabled() {
    // Indicator: all 24 bits enabled (0x00FFFFFF) + bit 31 (negative)
    // = 0x80FFFFFF = -2130706433
    int indicator = 0x80FFFFFF;
    
    // 8 corners × 3 axes = 24 offsets
    // Pack 24 int16 values into 12 int32
    // All offsets = 1
    int[] packedOffsets = new int[12];
    for (int i = 0; i < 12; i++) {
      packedOffsets[i] = (1 << 16) | 1;  // high=1, low=1
    }
    
    int[] data = new int[8 + 12];  // face cache + 6 coords + indicator + 12 packed offsets
    data[0] = 0;                    // face cache
    data[1] = 0; data[2] = 0; data[3] = 0;
    data[4] = 16; data[5] = 16; data[6] = 16;
    data[7] = indicator;
    System.arraycopy(packedOffsets, 0, data, 8, 12);
    
    BitFieldParser.ParseResult result = BitFieldParser.parse(data);
    
    // Verify all corners have offset (1, 1, 1)
    for (int i = 0; i < 8; i++) {
      AngleOffset offset = result.offsets[i];
      assertTrue(offset.xEnable, "Corner " + i + " x should be enabled");
      assertTrue(offset.yEnable, "Corner " + i + " y should be enabled");
      assertTrue(offset.zEnable, "Corner " + i + " z should be enabled");
      assertEquals(1, offset.xOffset, "Corner " + i + " x offset");
      assertEquals(1, offset.yOffset, "Corner " + i + " y offset");
      assertEquals(1, offset.zOffset, "Corner " + i + " z offset");
    }
  }
  
  /**
   * Test case: no offsets, only face flips.
   */
  @Test
  void testNoOffsetsOnlyFlips() {
    // Indicator: no offsets (bits 0-23 = 0), all faces flipped (bits 24-29 = 1)
    // 0x80000000 (bit 31) | (0x3F << 24) (bits 24-29)
    // = 0b10111111000000000000000000000000 = 0xBF000000 (in unsigned)
    // As signed int32: -1090519040
    int indicator = (int) 0xBF000000;
    
    int[] data = {
      0,                          // face cache (1.18+ format)
      0, 0, 0, 16, 16, 16,
      indicator
      // No packed offsets needed when no enable bits are set
    };
    
    BitFieldParser.ParseResult result = BitFieldParser.parse(data);
    
    // Verify all corners have zero offset
    for (int i = 0; i < 8; i++) {
      AngleOffset offset = result.offsets[i];
      assertFalse(offset.hasOffset(), "Corner " + i + " should have no offset");
    }
    
    // Verify all faces flipped (bits 24-29 = 1)
    Flipped flipped = result.flipped;
    assertTrue(flipped.down,  "DOWN should be flipped (bit 24 = 1)");
    assertTrue(flipped.up,    "UP should be flipped (bit 25 = 1)");
    assertTrue(flipped.north, "NORTH should be flipped (bit 26 = 1)");
    assertTrue(flipped.south, "SOUTH should be flipped (bit 27 = 1)");
    assertTrue(flipped.west,  "WEST should be flipped (bit 28 = 1)");
    assertTrue(flipped.east,  "EAST should be flipped (bit 29 = 1)");
  }
  
  /**
   * Test invalid data: length < 7.
   */
  @Test
  void testInvalidLength() {
    int[] data = {0, 0, 0, 16, 16, 16};  // Only 6 elements
    
    assertThrows(IllegalArgumentException.class, () -> {
      BitFieldParser.parse(data);
    });
  }
  
  /**
   * Test invalid data: indicator >= 0 (not a TransformableBox).
   */
  @Test
  void testNotTransformableBox() {
    int[] data = {
      0, 0, 0, 16, 16, 16,
      123  // Positive indicator
    };
    
    assertThrows(IllegalArgumentException.class, () -> {
      BitFieldParser.parse(data);
    });
  }
  
  /**
   * Test insufficient packed offsets.
   */
  @Test
  void testInsufficientPackedOffsets() {
    // Enable 3 offsets, but provide only 1 packed int32
    // Need 2 packed int32 (ceiling of 3/2)
    int indicator = 0x80000007;  // Enable x/y/z for corner 0
    
    int[] data = {
      0, 0, 0, 16, 16, 16,
      indicator,
      0x00010001  // Only 1 packed int32, need 2
    };
    
    assertThrows(IllegalArgumentException.class, () -> {
      BitFieldParser.parse(data);
    });
  }
  
  /**
   * Test int16 packing: negative values.
   */
  @Test
  void testNegativeOffsets() {
    // Enable only corner 0, axis x
    // Indicator: bit 0 = 1, bit 31 = 1 → 0x80000001 = -2147483647
    int indicator = 0x80000001;
    
    // Pack offset = -1 (0xFFFF) into high 16 bits
    int packed = 0xFFFF0000;
    
    int[] data = {
      0,                          // face cache (1.18+ format)
      0, 0, 0, 16, 16, 16,
      indicator,
      packed
    };
    
    BitFieldParser.ParseResult result = BitFieldParser.parse(data);
    
    AngleOffset offset = result.offsets[0];
    assertTrue(offset.xEnable);
    assertEquals(-1, offset.xOffset, "Should correctly parse int16 -1");
  }
}
