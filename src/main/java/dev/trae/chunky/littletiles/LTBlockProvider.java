package dev.trae.chunky.littletiles;

import se.llbit.chunky.block.Block;
import se.llbit.chunky.block.BlockProvider;
import se.llbit.nbt.Tag;

import java.util.Collection;
import java.util.List;

/**
 * BlockProvider for LittleTiles blocks.
 * Intercepts "littletiles:tiles" blocks and returns custom LTTilesBlock instances.
 */
public class LTBlockProvider implements BlockProvider {
  
  private static final String LITTLETILES_TILES = "littletiles:tiles";
  
  @Override
  public Block getBlockByTag(String name, Tag tag) {
    if (LITTLETILES_TILES.equals(name)) {
      return new LTTilesBlock(tag);
    }
    return null; // Continue to next provider in chain
  }
  
  @Override
  public Collection<String> getSupportedBlocks() {
    return List.of(LITTLETILES_TILES);
  }
}
