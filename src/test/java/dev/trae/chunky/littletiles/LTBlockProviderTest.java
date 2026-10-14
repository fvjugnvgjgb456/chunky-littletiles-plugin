package dev.trae.chunky.littletiles;

import org.junit.jupiter.api.Test;
import se.llbit.chunky.block.Block;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.SpecificTag;
import se.llbit.nbt.StringTag;

import static org.junit.jupiter.api.Assertions.*;

class LTBlockProviderTest {
  
  @Test
  void testRecognizesLittleTilesBlock() {
    LTBlockProvider provider = new LTBlockProvider();
    
    CompoundTag tag = new CompoundTag();
    tag.add("Name", new StringTag("littletiles:tiles"));
    
    Block block = provider.getBlockByTag("littletiles:tiles", tag);
    
    assertNotNull(block, "Provider should return a block for littletiles:tiles");
    assertTrue(block instanceof LTTilesBlock, "Block should be instance of LTTilesBlock");
  }
  
  @Test
  void testIgnoresOtherBlocks() {
    LTBlockProvider provider = new LTBlockProvider();
    
    CompoundTag tag = new CompoundTag();
    tag.add("Name", new StringTag("minecraft:stone"));
    
    Block block = provider.getBlockByTag("minecraft:stone", tag);
    
    assertNull(block, "Provider should return null for non-LittleTiles blocks");
  }
  
  @Test
  void testGetSupportedBlocks() {
    LTBlockProvider provider = new LTBlockProvider();
    
    var supported = provider.getSupportedBlocks();
    
    assertEquals(1, supported.size(), "Should support exactly one block type");
    assertTrue(supported.contains("littletiles:tiles"), "Should support littletiles:tiles");
  }
}
