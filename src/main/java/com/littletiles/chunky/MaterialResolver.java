package com.littletiles.chunky;

import se.llbit.chunky.block.Block;
import se.llbit.chunky.block.BlockSpec;
import se.llbit.chunky.world.Material;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.StringTag;
import se.llbit.nbt.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves Minecraft blockstate names to Chunky Materials.
 * 
 * Supports LittleTiles color tinting:
 * - If color == -1 (0xFFFFFFFF): returns base Material (no tint)
 * - If color != -1: returns TintedMaterial wrapping the base Material
 * 
 * Uses Chunky's BlockSpec mechanism to query the default MinecraftBlockProvider,
 * extracting Material/Texture information for LittleTiles tiles.
 */
public class MaterialResolver {
  
  /**
   * Cache (blockstate, color) → Material mappings to avoid repeated queries.
   * Key format: "{blockstate}#{color_hex}" for tinted materials,
   *             "{blockstate}" for untinted materials.
   */
  private static final Map<String, Material> MATERIAL_CACHE = new HashMap<>();
  
  /**
   * Performance statistics
   */
  private static int cacheHits = 0;
  private static int cacheMisses = 0;
  
  /**
   * Resolve a blockstate and return its texture.
   * 
   * @param blockstateName Fully qualified blockstate name (namespace:id)
   * @return The corresponding Texture
   */
  public static se.llbit.chunky.resources.Texture resolveTexture(String blockstateName) {
    Material material = resolve(blockstateName);
    return material.texture;
  }
  
  /**
   * Resolve a blockstate name (e.g. "minecraft:white_terracotta") to a Chunky Material.
   * 
   * @param blockstateName Fully qualified blockstate name (namespace:id), may include properties like [facing=south]
   * @return The corresponding Material, or a fallback UnknownBlock if not found
   */
  public static Material resolve(String blockstateName) {
    // Check cache first
    Material cached = MATERIAL_CACHE.get(blockstateName);
    if (cached != null) {
      cacheHits++;
      return cached;
    }
    cacheMisses++;
    
    // Strip blockstate properties (e.g. "minecraft:magenta_glazed_terracotta[facing=south]" -> "minecraft:magenta_glazed_terracotta")
    // MinecraftBlockProvider only matches the base block name without properties
    String baseBlockName = blockstateName;
    int bracketIndex = blockstateName.indexOf('[');
    if (bracketIndex > 0) {
      baseBlockName = blockstateName.substring(0, bracketIndex);
    }
    
    // Construct a BlockSpec NBT tag for the blockstate
    CompoundTag blockTag = new CompoundTag();
    blockTag.add("Name", new StringTag(baseBlockName));
    // Note: We're not parsing Properties from the blockstate string for now
    // Most blocks don't need properties for correct texture rendering
    
    BlockSpec spec = new BlockSpec(blockTag);
    Block block = spec.toBlock();
    
    // Cache and return the Material (Block extends Material)
    MATERIAL_CACHE.put(blockstateName, block);
    return block;
  }
  
  /**
   * Clear the material cache (useful for testing or if texture pack changes).
   */
  public static void clearCache() {
    MATERIAL_CACHE.clear();
    cacheHits = 0;
    cacheMisses = 0;
  }
  
  /**
   * Get cache statistics for performance analysis.
   */
  public static String getCacheStats() {
    int total = cacheHits + cacheMisses;
    double hitRate = total > 0 ? (cacheHits * 100.0 / total) : 0;
    return String.format("MaterialResolver Cache: size=%d, hits=%d, misses=%d, hit rate=%.1f%%",
        MATERIAL_CACHE.size(), cacheHits, cacheMisses, hitRate);
  }
}
