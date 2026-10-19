package com.littletiles.chunky;

import se.llbit.chunky.block.Block;
import se.llbit.chunky.block.BlockSpec;
import se.llbit.chunky.block.minecraft.Lava;
import se.llbit.chunky.block.minecraft.Water;
import se.llbit.chunky.world.Material;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.StringTag;
import se.llbit.nbt.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Resolves Minecraft blockstate names to Chunky Materials.
 * 
 * Supports LittleTiles color tinting:
 * - If color == -1 (0xFFFFFFFF): returns base Material (no tint)
 * - If color != -1: returns TintedMaterial wrapping the base Material
 * 
 * C9 Extensions:
 * - Fluid blocks: colored_water, colored_lava, colored_white_lava
 * - LT colored blocks: colored_clean, colored_brick, etc.
 * 
 * Uses Chunky's BlockSpec mechanism to query the default MinecraftBlockProvider,
 * extracting Material/Texture information for LittleTiles tiles.
 */
public class MaterialResolver {
  
  /**
   * LittleTiles Fluid blocks (C9)
   */
  private static final Set<String> FLUID_BLOCKS = Set.of(
    "littletiles:colored_water",
    "littletiles:colored_lava",
    "littletiles:colored_white_lava"
  );
  
  /**
   * LittleTiles colored blocks (C9) - 16 texture variants.
   * These are mapped to similar vanilla blocks for rendering.
   */
  private static final Set<String> LT_COLORED_BLOCKS = Set.of(
    "littletiles:colored_clean",
    "littletiles:colored_floor",
    "littletiles:colored_grainy_big",
    "littletiles:colored_grainy",
    "littletiles:colored_grainy_low",
    "littletiles:colored_brick",
    "littletiles:colored_brick_big",
    "littletiles:colored_bordered",
    "littletiles:colored_chiseled",
    "littletiles:colored_broken_brick_big",
    "littletiles:colored_clay",
    "littletiles:colored_strips",
    "littletiles:colored_gravel",
    "littletiles:colored_sand",
    "littletiles:colored_stone",
    "littletiles:colored_cork"
  );
  
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
    
    // C9: Handle LittleTiles Fluid blocks
    if (FLUID_BLOCKS.contains(baseBlockName)) {
      Material material = resolveFluidMaterial(baseBlockName);
      MATERIAL_CACHE.put(blockstateName, material);
      return material;
    }
    
    // C9: Handle LittleTiles colored blocks
    if (LT_COLORED_BLOCKS.contains(baseBlockName)) {
      Material material = resolveColoredBlock(baseBlockName);
      MATERIAL_CACHE.put(blockstateName, material);
      return material;
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
   * Resolve LittleTiles Fluid blocks to vanilla Minecraft fluid materials (C9).
   * 
   * Maps:
   * - colored_water → Water (refractive, translucent)
   * - colored_lava → Lava (emittance = 1.0)
   * - colored_white_lava → Lava (emittance = 15.0, bright glow)
   * 
   * @param blockstate LittleTiles fluid blockstate (littletiles:colored_water, etc.)
   * @return Vanilla Water or Lava material
   */
  private static Material resolveFluidMaterial(String blockstate) {
    if (blockstate.contains("water")) {
      // Return vanilla water material
      // Water has: refractive=true, specular=0.12, ior=1.333
      return Water.INSTANCE;
    } else if (blockstate.contains("white_lava")) {
      // White lava: bright emissive light source (level 15)
      Lava whiteLava = new Lava(0, 0);
      whiteLava.emittance = 15.0f;  // Maximum brightness
      return whiteLava;
    } else if (blockstate.contains("lava")) {
      // Regular lava: standard emittance (level 1)
      // Lava has: emittance=1.0, translucent
      return new Lava(0, 0);
    }
    
    // Fallback (should never reach here if FLUID_BLOCKS is correct)
    return Water.INSTANCE;
  }
  
  /**
   * Resolve LittleTiles colored blocks to similar vanilla textures (C9).
   * 
   * Strategy: Map LT mod textures to vanilla blocks with similar appearance.
   * Color tinting will be applied by TintedTextureMaterial in C8 code path.
   * 
   * Texture mapping:
   * - brick types → BRICKS or similar brick blocks
   * - stone/gravel/sand → corresponding vanilla types
   * - clean/floor/clay → smooth blocks (TERRACOTTA, CONCRETE)
   * - others → WHITE_CONCRETE (solid color base for tinting)
   * 
   * @param blockstate LittleTiles colored blockstate (littletiles:colored_brick, etc.)
   * @return Vanilla material with similar texture
   */
  private static Material resolveColoredBlock(String blockstate) {
    // Map to vanilla blocks based on texture type
    String vanillaBlock;
    
    if (blockstate.contains("brick")) {
      vanillaBlock = "minecraft:bricks";
    } else if (blockstate.contains("stone")) {
      vanillaBlock = "minecraft:stone";
    } else if (blockstate.contains("clay")) {
      vanillaBlock = "minecraft:terracotta";
    } else if (blockstate.contains("gravel")) {
      vanillaBlock = "minecraft:gravel";
    } else if (blockstate.contains("sand")) {
      vanillaBlock = "minecraft:sandstone";
    } else if (blockstate.contains("chiseled")) {
      vanillaBlock = "minecraft:chiseled_stone_bricks";
    } else {
      // Default: smooth solid block suitable for color tinting
      vanillaBlock = "minecraft:white_concrete";
    }
    
    // Resolve vanilla block through standard path
    CompoundTag blockTag = new CompoundTag();
    blockTag.add("Name", new StringTag(vanillaBlock));
    BlockSpec spec = new BlockSpec(blockTag);
    return spec.toBlock();
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
  
  /**
   * Check if a blockstate is a LittleTiles Fluid block.
   * Used by GeometryConverter to decide whether to use vanilla Box (preserve refractive/emissive)
   * or TextureMaterial (support color tinting).
   * 
   * @param blockstateName Fully qualified blockstate name (namespace:id)
   * @return true if this is a fluid block (water/lava)
   */
  public static boolean isFluidBlock(String blockstateName) {
    String baseBlockName = blockstateName;
    int bracketIndex = blockstateName.indexOf('[');
    if (bracketIndex > 0) {
      baseBlockName = blockstateName.substring(0, bracketIndex);
    }
    return FLUID_BLOCKS.contains(baseBlockName);
  }
  
  /**
   * Resolve vanilla Block instance for blockstate (C10 simplified).
   * Returns the vanilla Block whose properties can be adjusted in Materials Tab.
   * All tiles using this block will automatically inherit adjusted properties.
   * 
   * @param blockstate Fully qualified blockstate (e.g., "minecraft:stone[variant=smooth]")
   * @return Vanilla Block instance managed by Chunky's BlockPalette
   */
  public static se.llbit.chunky.block.Block resolveBlock(String blockstate) {
    // Strip blockstate properties for vanilla block lookup
    String baseBlockName = blockstate;
    int bracketIndex = blockstate.indexOf('[');
    if (bracketIndex > 0) {
      baseBlockName = blockstate.substring(0, bracketIndex);
    }
    
    // C10: Handle LittleTiles special blocks - map to vanilla blocks
    // This ensures LT blocks can be adjusted in Materials Tab like vanilla blocks
    String mappedBlockName = mapLittleTilesToVanilla(baseBlockName);
    
    // Use Chunky's BlockSpec to resolve vanilla block
    se.llbit.nbt.CompoundTag tag = new se.llbit.nbt.CompoundTag();
    tag.add("Name", new se.llbit.nbt.StringTag(mappedBlockName));
    se.llbit.chunky.block.BlockSpec spec = new se.llbit.chunky.block.BlockSpec(tag);
    return spec.toBlock();
  }
  
  /**
   * Map LittleTiles special blocks to vanilla Minecraft blocks (C10).
   * 
   * @param ltBlockName LittleTiles blockstate name
   * @return Vanilla block name for Materials Tab compatibility
   */
  private static String mapLittleTilesToVanilla(String ltBlockName) {
    // Fluid blocks mapping
    if (ltBlockName.equals("littletiles:colored_water")) {
      return "minecraft:water";
    } else if (ltBlockName.equals("littletiles:colored_lava") || 
               ltBlockName.equals("littletiles:colored_white_lava")) {
      return "minecraft:lava";
    }
    
    // Colored blocks mapping - use same logic as resolveColoredBlock()
    if (LT_COLORED_BLOCKS.contains(ltBlockName)) {
      if (ltBlockName.contains("brick")) {
        return "minecraft:bricks";
      } else if (ltBlockName.contains("stone")) {
        return "minecraft:stone";
      } else if (ltBlockName.contains("clay")) {
        return "minecraft:terracotta";
      } else if (ltBlockName.contains("gravel")) {
        return "minecraft:gravel";
      } else if (ltBlockName.contains("sand")) {
        return "minecraft:sandstone";
      } else if (ltBlockName.contains("chiseled")) {
        return "minecraft:chiseled_stone_bricks";
      } else {
        return "minecraft:white_concrete";
      }
    }
    
    // Not a LittleTiles special block, return as-is
    return ltBlockName;
  }
}
