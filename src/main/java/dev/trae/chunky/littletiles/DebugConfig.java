package dev.trae.chunky.littletiles;

/**
 * Debug logging configuration.
 * Controls verbosity levels for different components.
 */
public class DebugConfig {
  
  /**
   * Enable detailed NBT parser logs (per-tile, per-box).
   * WARNING: Generates 100+ MB logs in large scenes.
   */
  public static final boolean VERBOSE_NBT_PARSER = true;
  
  /**
   * Enable detailed entity construction logs.
   */
  public static final boolean VERBOSE_ENTITY_CONSTRUCTION = true;
  
  /**
   * Enable verbose logging for primitive generation (TransformableBox conversion).
   * Logs each box's AABB, grid, and corner coordinates.
   */
  public static final boolean VERBOSE_PRIMITIVE_GENERATION = true;
  
  /**
   * Enable verbose logging for NBT box parsing.
   * Logs raw box data arrays to diagnose parsing issues.
   */
  public static final boolean VERBOSE_BOX_PARSING = true;
  
  /**
   * Enable performance timing logs (per-entity, per-tile).
   */
  public static final boolean ENABLE_PERFORMANCE_TIMING = true;
  
  /**
   * Enable summary statistics (counts, totals).
   * WARNING: Generates ~30KB per 1000 entities in large scenes.
   */
  public static final boolean ENABLE_SUMMARY_STATS = true;
  
  /**
   * Enable warning messages (invalid data, parse failures).
   * WARNING: Each warning triggers a GUI popup in Chunky.
   */
  public static final boolean ENABLE_WARNINGS = false;
  
  /**
   * Enable TransformableBox support (C7 advanced geometry).
   * When disabled, all 7+ field boxes are treated as basic AABB (C1-C6 behavior).
   */
  public static final boolean ENABLE_TRANSFORMABLE_BOX = true;
}
