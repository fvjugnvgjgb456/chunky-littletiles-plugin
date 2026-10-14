package dev.trae.chunky.littletiles;

import com.littletiles.chunky.MaterialResolver;
import se.llbit.log.Log;

/**
 * Performance monitoring for LittleTiles plugin.
 * Collects and reports performance metrics.
 */
public class PerformanceMonitor {
  
  private static long totalEntities = 0;
  private static long totalTiles = 0;
  private static long totalPrimitives = 0;
  private static long totalBuildTimeMs = 0;
  
  /**
   * Record entity processing metrics.
   */
  public static void recordEntity(int tileCount, int primitiveCount, long buildTimeMs) {
    totalEntities++;
    totalTiles += tileCount;
    totalPrimitives += primitiveCount;
    totalBuildTimeMs += buildTimeMs;
  }
  
  /**
   * Print comprehensive performance report.
   */
  public static void printReport() {
    String report = generateReport();
    Log.info(report);
    System.out.println(report);
  }
  
  /**
   * Generate performance report string.
   */
  public static String generateReport() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n=== LittleTiles Plugin Performance Report ===\n");
    sb.append(String.format("Total Entities:    %d\n", totalEntities));
    sb.append(String.format("Total Tiles:       %d\n", totalTiles));
    sb.append(String.format("Total Primitives:  %d (%.1f primitives/tile)\n", 
        totalPrimitives, totalTiles > 0 ? (double)totalPrimitives / totalTiles : 0));
    sb.append(String.format("Total Build Time:  %d ms (%.2f ms/entity, %.3f ms/tile)\n",
        totalBuildTimeMs,
        totalEntities > 0 ? (double)totalBuildTimeMs / totalEntities : 0,
        totalTiles > 0 ? (double)totalBuildTimeMs / totalTiles : 0));
    sb.append(MaterialResolver.getCacheStats()).append("\n");
    sb.append("============================================\n");
    return sb.toString();
  }
  
  /**
   * Reset all performance counters.
   */
  public static void reset() {
    totalEntities = 0;
    totalTiles = 0;
    totalPrimitives = 0;
    totalBuildTimeMs = 0;
    MaterialResolver.clearCache();
  }
}
