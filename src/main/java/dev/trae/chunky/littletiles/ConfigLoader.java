package dev.trae.chunky.littletiles;

import se.llbit.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuration loader for LittleTiles plugin.
 * Loads configuration from external properties file on plugin startup.
 * 
 * Configuration file location: ~/.chunky/plugins/littletiles-plugin-config.properties
 * If file does not exist, uses default values from bundled template.
 */
public class ConfigLoader {
  
  private static ConfigLoader instance;
  private final Properties config;
  private boolean loadSuccess = false;
  
  // Configuration keys
  public static final String KEY_ENABLE_FILE_LOGGING = "ENABLE_FILE_LOGGING";
  public static final String KEY_VERBOSE_NBT_PARSER = "VERBOSE_NBT_PARSER";
  public static final String KEY_VERBOSE_ENTITY_CONSTRUCTION = "VERBOSE_ENTITY_CONSTRUCTION";
  public static final String KEY_VERBOSE_PRIMITIVE_GENERATION = "VERBOSE_PRIMITIVE_GENERATION";
  public static final String KEY_VERBOSE_BOX_PARSING = "VERBOSE_BOX_PARSING";
  public static final String KEY_ENABLE_PERFORMANCE_TIMING = "ENABLE_PERFORMANCE_TIMING";
  public static final String KEY_ENABLE_SUMMARY_STATS = "ENABLE_SUMMARY_STATS";
  public static final String KEY_ENABLE_WARNINGS = "ENABLE_WARNINGS";
  public static final String KEY_ENABLE_TRANSFORMABLE_BOX = "ENABLE_TRANSFORMABLE_BOX";
  
  private ConfigLoader() {
    config = new Properties();
    loadConfig();
  }
  
  /**
   * Get singleton instance.
   */
  public static ConfigLoader getInstance() {
    if (instance == null) {
      instance = new ConfigLoader();
    }
    return instance;
  }
  
  /**
   * Load configuration from external file.
   * Falls back to bundled template if external file does not exist.
   */
  private void loadConfig() {
    // Try to load from user's Chunky plugins directory
    Path externalConfigPath = getExternalConfigPath();
    
    if (Files.exists(externalConfigPath)) {
      try (FileInputStream fis = new FileInputStream(externalConfigPath.toFile())) {
        config.load(fis);
        loadSuccess = true;
        Log.info("[LittleTiles ConfigLoader] Loaded configuration from: " + externalConfigPath);
        return;
      } catch (IOException e) {
        Log.warn("[LittleTiles ConfigLoader] Failed to load external config: " + e.getMessage());
      }
    }
    
    // Fall back to bundled template
    try (InputStream is = getClass().getClassLoader()
        .getResourceAsStream("littletiles-plugin-config.properties")) {
      if (is != null) {
        config.load(is);
        Log.info("[LittleTiles ConfigLoader] Using default configuration (bundled template)");
        Log.info("[LittleTiles ConfigLoader] To customize, create: " + externalConfigPath);
      } else {
        Log.warn("[LittleTiles ConfigLoader] Bundled config not found, using hardcoded defaults");
      }
    } catch (IOException e) {
      Log.warn("[LittleTiles ConfigLoader] Failed to load bundled config: " + e.getMessage());
    }
  }
  
  /**
   * Get the path to the external configuration file.
   * @return Path to ~/.chunky/plugins/littletiles-plugin-config.properties
   */
  private Path getExternalConfigPath() {
    String userHome = System.getProperty("user.home");
    return Paths.get(userHome, ".chunky", "plugins", "littletiles-plugin-config.properties");
  }
  
  /**
   * Get boolean configuration value.
   * @param key Configuration key
   * @param defaultValue Default value if key not found or invalid
   * @return Configuration value
   */
  public boolean getBoolean(String key, boolean defaultValue) {
    String value = config.getProperty(key);
    if (value == null) {
      return defaultValue;
    }
    
    // Parse boolean (true/false, case-insensitive)
    String normalized = value.trim().toLowerCase();
    if ("true".equals(normalized)) {
      return true;
    } else if ("false".equals(normalized)) {
      return false;
    } else {
      Log.warn("[LittleTiles ConfigLoader] Invalid boolean value for " + key + ": " + value 
          + " (expected true/false), using default: " + defaultValue);
      return defaultValue;
    }
  }
  
  /**
   * Get integer configuration value.
   * @param key Configuration key
   * @param defaultValue Default value if key not found or invalid
   * @return Configuration value
   */
  public int getInt(String key, int defaultValue) {
    String value = config.getProperty(key);
    if (value == null) {
      return defaultValue;
    }
    
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      Log.warn("[LittleTiles ConfigLoader] Invalid integer value for " + key + ": " + value 
          + ", using default: " + defaultValue);
      return defaultValue;
    }
  }
  
  /**
   * Get string configuration value.
   * @param key Configuration key
   * @param defaultValue Default value if key not found
   * @return Configuration value
   */
  public String getString(String key, String defaultValue) {
    return config.getProperty(key, defaultValue);
  }
  
  /**
   * Check if configuration was loaded successfully from external file.
   * @return true if external config was loaded, false if using defaults
   */
  public boolean isLoadSuccess() {
    return loadSuccess;
  }
  
  /**
   * Print current configuration summary to log.
   * Useful for debugging configuration issues.
   */
  public void printConfigSummary() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n=== LittleTiles Plugin Configuration ===\n");
    sb.append("Source: ").append(loadSuccess ? "External file" : "Bundled defaults").append("\n");
    sb.append(KEY_ENABLE_FILE_LOGGING).append(" = ")
        .append(getBoolean(KEY_ENABLE_FILE_LOGGING, true)).append("\n");
    sb.append(KEY_VERBOSE_NBT_PARSER).append(" = ")
        .append(getBoolean(KEY_VERBOSE_NBT_PARSER, false)).append("\n");
    sb.append(KEY_VERBOSE_ENTITY_CONSTRUCTION).append(" = ")
        .append(getBoolean(KEY_VERBOSE_ENTITY_CONSTRUCTION, false)).append("\n");
    sb.append(KEY_VERBOSE_PRIMITIVE_GENERATION).append(" = ")
        .append(getBoolean(KEY_VERBOSE_PRIMITIVE_GENERATION, false)).append("\n");
    sb.append(KEY_VERBOSE_BOX_PARSING).append(" = ")
        .append(getBoolean(KEY_VERBOSE_BOX_PARSING, false)).append("\n");
    sb.append(KEY_ENABLE_PERFORMANCE_TIMING).append(" = ")
        .append(getBoolean(KEY_ENABLE_PERFORMANCE_TIMING, true)).append("\n");
    sb.append(KEY_ENABLE_SUMMARY_STATS).append(" = ")
        .append(getBoolean(KEY_ENABLE_SUMMARY_STATS, false)).append("\n");
    sb.append(KEY_ENABLE_WARNINGS).append(" = ")
        .append(getBoolean(KEY_ENABLE_WARNINGS, false)).append("\n");
    sb.append(KEY_ENABLE_TRANSFORMABLE_BOX).append(" = ")
        .append(getBoolean(KEY_ENABLE_TRANSFORMABLE_BOX, true)).append("\n");
    sb.append("==========================================");
    
    Log.info(sb.toString());
  }
}
