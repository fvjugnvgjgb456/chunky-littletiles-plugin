package dev.trae.chunky.littletiles;

import org.junit.jupiter.api.Test;
import se.llbit.chunky.entity.Entity;
import se.llbit.math.Vector3;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.StringTag;

import static org.junit.jupiter.api.Assertions.*;

class LTTilesBlockTest {
  
  @Test
  void testBlockIsInvisible() {
    CompoundTag tag = new CompoundTag();
    tag.add("Name", new StringTag("littletiles:tiles"));
    
    LTTilesBlock block = new LTTilesBlock(tag);
    
    assertTrue(block.invisible, "LTTilesBlock should be invisible (not added to Octree)");
  }
  
  @Test
  void testIsBlockEntity() {
    CompoundTag tag = new CompoundTag();
    tag.add("Name", new StringTag("littletiles:tiles"));
    
    LTTilesBlock block = new LTTilesBlock(tag);
    
    assertTrue(block.isBlockEntity(), "LTTilesBlock should return true for isBlockEntity()");
  }
  
  @Test
  void testCreateBlockEntity() {
    CompoundTag tag = new CompoundTag();
    tag.add("Name", new StringTag("littletiles:tiles"));
    
    LTTilesBlock block = new LTTilesBlock(tag);
    
    Vector3 position = new Vector3(10, 64, 20);
    CompoundTag entityTag = new CompoundTag();
    
    Entity entity = block.createBlockEntity(position, entityTag);
    
    assertNotNull(entity, "createBlockEntity should return a non-null Entity");
    assertTrue(entity instanceof LTTilesEntity, "Entity should be instance of LTTilesEntity");
  }
  
  @Test
  void testBlockName() {
    CompoundTag tag = new CompoundTag();
    tag.add("Name", new StringTag("littletiles:tiles"));
    
    LTTilesBlock block = new LTTilesBlock(tag);
    
    assertEquals("littletiles:tiles", block.name, "Block name should be littletiles:tiles");
  }
}
