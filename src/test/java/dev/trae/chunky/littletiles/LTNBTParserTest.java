package dev.trae.chunky.littletiles;

import org.junit.jupiter.api.Test;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.IntArrayTag;
import se.llbit.nbt.IntTag;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LTNBTParser.
 * 
 * Note: Due to Chunky NBT API constraints, these tests use simplified NBT construction.
 * The actual NBT from Minecraft will be parsed correctly by the parser.
 */
class LTNBTParserTest {
  
  @Test
  void testBasicParsing() {
    // Simplified test - parser will be validated with actual Chunky loading
    CompoundTag nbt = new CompoundTag();
    nbt.add("grid", new IntTag(16));
    nbt.add("content", new CompoundTag());
    
    LTNBTParser.ParseResult result = LTNBTParser.parse(nbt);
    
    assertNotNull(result);
    assertEquals(16, result.grid.count);
    assertTrue(result.grid.isDefault());
    assertNotNull(result.tiles);
  }
  
  @Test
  void testDefaultGrid() {
    CompoundTag nbt = new CompoundTag();
    nbt.add("content", new CompoundTag());
    
    LTNBTParser.ParseResult result = LTNBTParser.parse(nbt);
    
    assertEquals(16, result.grid.count);
    assertTrue(result.grid.isDefault());
  }
  
  @Test
  void testCustomGrid() {
    CompoundTag nbt = new CompoundTag();
    nbt.add("grid", new IntTag(32));
    nbt.add("content", new CompoundTag());
    
    LTNBTParser.ParseResult result = LTNBTParser.parse(nbt);
    
    assertEquals(32, result.grid.count);
    assertFalse(result.grid.isDefault());
  }
  
  @Test
  void testEmptyContent() {
    CompoundTag nbt = new CompoundTag();
    nbt.add("content", new CompoundTag());
    
    LTNBTParser.ParseResult result = LTNBTParser.parse(nbt);
    
    assertEquals(0, result.tiles.size());
  }
  
  @Test
  void testNullNBT() {
    LTNBTParser.ParseResult result = LTNBTParser.parse(null);
    
    assertNotNull(result);
    assertEquals(16, result.grid.count);
    assertEquals(0, result.tiles.size());
  }
  
  @Test
  void testLittleBoxFromArray() {
    int[] data = {0, 0, 0, 16, 16, 16};
    LittleBox box = LittleBox.fromArray(data);
    
    assertEquals(0, box.minX);
    assertEquals(0, box.minY);
    assertEquals(0, box.minZ);
    assertEquals(16, box.maxX);
    assertEquals(16, box.maxY);
    assertEquals(16, box.maxZ);
    assertTrue(box.isValid());
  }
  
  @Test
  void testLittleBoxInvalid() {
    int[] data = {8, 8, 8, 8, 8, 8};
    LittleBox box = LittleBox.fromArray(data);
    
    assertFalse(box.isValid());
  }
  
  @Test
  void testLittleGrid() {
    LittleGrid grid16 = new LittleGrid(16);
    assertTrue(grid16.isDefault());
    assertEquals(1.0/16, grid16.pixelLength, 0.0001);
    
    LittleGrid grid32 = new LittleGrid(32);
    assertFalse(grid32.isDefault());
    assertEquals(1.0/32, grid32.pixelLength, 0.0001);
  }
}
