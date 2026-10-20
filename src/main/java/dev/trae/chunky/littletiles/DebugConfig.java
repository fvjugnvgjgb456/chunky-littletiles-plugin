package dev.trae.chunky.littletiles;

/**
 * Debug logging configuration.
 * Controls verbosity levels for different components.
 * 
 * Configuration is loaded from external file at plugin startup.
 * See ConfigLoader for details on configuration file location and format.
 */
public class DebugConfig {
  
  // Configuration values loaded from external config file
  // These are NOT final - they are initialized from ConfigLoader at startup
  
  /**
   * Enable detailed NBT parser logs (per-tile, per-box).
   * WARNING: Generates 100+ MB logs in large scenes.
   */
  public static boolean VERBOSE_NBT_PARSER;
  
  /**
   * Enable detailed entity construction logs.
   */
  public static boolean VERBOSE_ENTITY_CONSTRUCTION;
  
  /**
   * Enable verbose logging for primitive generation (TransformableBox conversion).
   * Logs each box's AABB, grid, and corner coordinates.
   */
  public static boolean VERBOSE_PRIMITIVE_GENERATION;
  
  /**
   * Enable verbose logging for NBT box parsing.
   * Logs raw box data arrays to diagnose parsing issues.
   */
  public static boolean VERBOSE_BOX_PARSING;
  
  /**
   * Enable performance timing logs (per-entity, per-tile).
   */
  public static boolean ENABLE_PERFORMANCE_TIMING;
  
  /**
   * Enable summary statistics (counts, totals).
   * WARNING: Generates ~30KB per 1000 entities in large scenes.
   */
  public static boolean ENABLE_SUMMARY_STATS;
  
  /**
   * Enable warning messages (invalid data, parse failures).
   * WARNING: Each warning triggers a GUI popup in Chunky.
   */
  public static boolean ENABLE_WARNINGS;
  
  /**
   * Enable TransformableBox support (C7 advanced geometry).
   * When disabled, all 7+ field boxes are treated as basic AABB (C1-C6 behavior).
   */
  public static boolean ENABLE_TRANSFORMABLE_BOX;
  
  // Static initializer - loads configuration at class initialization
  static {
    loadConfiguration();
  }
  
  /**
   * Load configuration from ConfigLoader.
   * Called automatically during class initialization.
   */
  private static void loadConfiguration() {
    ConfigLoader config = ConfigLoader.getInstance();
    
    // Load all configuration values with defaults matching original hardcoded values
    VERBOSE_NBT_PARSER = config.getBoolean(
        ConfigLoader.KEY_VERBOSE_NBT_PARSER, false);
    VERBOSE_ENTITY_CONSTRUCTION = config.getBoolean(
        ConfigLoader.KEY_VERBOSE_ENTITY_CONSTRUCTION, false);
    VERBOSE_PRIMITIVE_GENERATION = config.getBoolean(
        ConfigLoader.KEY_VERBOSE_PRIMITIVE_GENERATION, false);
    VERBOSE_BOX_PARSING = config.getBoolean(
        ConfigLoader.KEY_VERBOSE_BOX_PARSING, false);
    ENABLE_PERFORMANCE_TIMING = config.getBoolean(
        ConfigLoader.KEY_ENABLE_PERFORMANCE_TIMING, true);
    ENABLE_SUMMARY_STATS = config.getBoolean(
        ConfigLoader.KEY_ENABLE_SUMMARY_STATS, false);
    ENABLE_WARNINGS = config.getBoolean(
        ConfigLoader.KEY_ENABLE_WARNINGS, false);
    ENABLE_TRANSFORMABLE_BOX = config.getBoolean(
        ConfigLoader.KEY_ENABLE_TRANSFORMABLE_BOX, true);
    
    // Print configuration summary for debugging
    config.printConfigSummary();
  }
}
