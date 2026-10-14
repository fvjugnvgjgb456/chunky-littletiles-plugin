package dev.trae.chunky.littletiles;

import com.littletiles.chunky.GeometryConverter;
import se.llbit.chunky.entity.Entity;
import se.llbit.json.JsonObject;
import se.llbit.json.JsonValue;
import se.llbit.log.Log;
import se.llbit.math.AABB;
import se.llbit.math.Vector3;
import se.llbit.math.primitive.Primitive;
import se.llbit.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Entity for LittleTiles structures.
 * Parses NBT data and stores tile information.
 */
public class LTTilesEntity extends Entity {
  
  private final CompoundTag entityTag;
  private final LittleGrid grid;
  private final List<LTTile> tiles;
  
  public LTTilesEntity(Vector3 position, CompoundTag entityTag) {
    super(position);
    this.entityTag = entityTag;
    
    if (DebugConfig.VERBOSE_ENTITY_CONSTRUCTION) {
      String msg = String.format("LTTilesEntity at (%.1f, %.1f, %.1f)", 
          position.x, position.y, position.z);
      Log.info(msg);
      System.out.println(msg);
    }
    
    // Parse NBT data
    LTNBTParser.ParseResult result = LTNBTParser.parse(entityTag);
    this.grid = result.grid;
    this.tiles = result.tiles;
    
    // Summary only
    if (DebugConfig.ENABLE_SUMMARY_STATS && tiles.size() > 0) {
      String parsedMsg = String.format("Parsed Entity: grid=%d, tiles=%d", grid.count, tiles.size());
      System.out.println(parsedMsg);
    }
  }
  
  @Override
  public Collection<Primitive> primitives(Vector3 offset) {
    long startTime = DebugConfig.ENABLE_PERFORMANCE_TIMING ? System.nanoTime() : 0;
    
    // Convert block position (world space) + offset
    Vector3 blockPos = new Vector3(position);
    blockPos.add(offset);
    
    List<Primitive> primitives = new ArrayList<>();
    for (LTTile tile : tiles) {
      // 为每个 tile 生成带材质的 primitives (Box → 12 TexturedTriangles)
      Collection<Primitive> tilePrimitives = GeometryConverter.boxToPrimitives(
          tile.box, grid, blockPos, tile.blockstate);
      primitives.addAll(tilePrimitives);
    }
    
    if (DebugConfig.ENABLE_PERFORMANCE_TIMING) {
      long elapsed = System.nanoTime() - startTime;
      double elapsedMs = elapsed / 1_000_000.0;
      
      if (elapsedMs > 1.0) { // Only log slow entities
        String msg = String.format("Entity primitives: %d tiles → %d prims in %.2f ms", 
            tiles.size(), primitives.size(), elapsedMs);
        System.out.println(msg);
      }
      
      PerformanceMonitor.recordEntity(tiles.size(), primitives.size(), (long)elapsedMs);
    }
    
    return primitives;
  }
  
  @Override
  public JsonValue toJson() {
    JsonObject json = new JsonObject();
    json.add("kind", "littletiles");
    json.add("position", position.toJson());
    json.add("grid", grid.count);
    json.add("tileCount", tiles.size());
    return json;
  }
  
  public static Entity fromJson(JsonObject json) {
    Vector3 position = new Vector3();
    position.fromJson(json.get("position").object());
    return new LTTilesEntity(position, new CompoundTag());
  }
  
  public LittleGrid getGrid() {
    return grid;
  }
  
  public List<LTTile> getTiles() {
    return tiles;
  }
}
