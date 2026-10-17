package dev.trae.chunky.littletiles.c7;

/**
 * Runtime detection for JTS Topology Suite availability.
 * 
 * JTS is declared as compileOnly dependency (optional at runtime).
 * This class checks if JTS classes are available in the classpath.
 * 
 * Usage:
 * <pre>
 * if (JTSAvailability.isAvailable()) {
 *     // Use JTS-based TransformableBox conversion
 * } else {
 *     // Fallback to basic AABB rendering
 * }
 * </pre>
 */
public class JTSAvailability {
  
  private static final boolean IS_AVAILABLE;
  
  static {
    boolean available;
    try {
      // Try to load a core JTS class
      Class.forName("org.locationtech.jts.geom.Geometry");
      available = true;
    } catch (ClassNotFoundException e) {
      available = false;
    }
    IS_AVAILABLE = available;
  }
  
  /**
   * Check if JTS Topology Suite is available at runtime.
   * 
   * @return true if JTS classes are present in classpath
   */
  public static boolean isAvailable() {
    return IS_AVAILABLE;
  }
  
  /**
   * Get a human-readable status message about JTS availability.
   * 
   * @return status message for logging
   */
  public static String getStatusMessage() {
    return IS_AVAILABLE 
        ? "JTS Topology Suite detected (advanced geometry enabled)"
        : "JTS Topology Suite not found (using basic AABB fallback)";
  }
}
