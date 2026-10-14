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
  public static final boolean VERBOSE_NBT_PARSER = false;
  
  /**
   * Enable detailed entity construction logs.
   */
  public static final boolean VERBOSE_ENTITY_CONSTRUCTION = false;
  
  /**
   * Enable detailed primitive generation logs.
   */
  public static final boolean VERBOSE_PRIMITIVE_GENERATION = false;
  
  /**
   * Enable performance timing logs (per-entity, per-tile).
   */
  public static final boolean ENABLE_PERFORMANCE_TIMING = true;
  
  /**
   * Enable summary statistics (counts, totals).
   */
  public static final boolean ENABLE_SUMMARY_STATS = true;
  
  /**
   * Enable warning messages (invalid data, parse failures).
   */
  public static final boolean ENABLE_WARNINGS = true;
}
