package dev.trae.chunky.littletiles;

import se.llbit.chunky.block.Block;
import se.llbit.chunky.entity.Entity;
import se.llbit.chunky.resources.Texture;
import se.llbit.log.Log;
import se.llbit.math.Vector3;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.Tag;

/**
 * Block representation for LittleTiles structures.
 * Marked as invisible (not added to Octree) and rendered as Entity.
 */
public class LTTilesBlock extends Block {
  
  private final Tag blockTag;
  
  public LTTilesBlock(Tag tag) {
    super("littletiles:tiles", Texture.air);
    this.blockTag = tag;
    this.invisible = true; // Not added to voxel octree
  }
  
  @Override
  public boolean isBlockEntity() {
    return true;
  }
  
  @Override
  public Entity createBlockEntity(Vector3 position, CompoundTag entityTag) {
    if (DebugConfig.VERBOSE_ENTITY_CONSTRUCTION) {
      Log.info(String.format("LTTilesBlock.createBlockEntity() at (%.1f, %.1f, %.1f)",
          position.x, position.y, position.z));
    }
    return new LTTilesEntity(position, entityTag);
  }
  
  public Tag getBlockTag() {
    return blockTag;
  }
}
