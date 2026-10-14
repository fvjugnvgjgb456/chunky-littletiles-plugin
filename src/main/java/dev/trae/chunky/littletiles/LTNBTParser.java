package dev.trae.chunky.littletiles;

import se.llbit.log.Log;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.IntArrayTag;
import se.llbit.nbt.ListTag;
import se.llbit.nbt.NamedTag;
import se.llbit.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses LittleTiles NBT data to extract grid, tiles, and geometry.
 * 
 * Based on LittleTiles 1.6.0-pre234-BETA NBT format:
 * - grid (int, optional): defaults to 16 if missing
 * - content (CompoundTag): blockstate → [color, box1, box2, ...]
 * 
 * This parser handles basic 6-field box format only.
 * Extended boxes (7+ fields) and children structures are not supported in this version.
 */
public class LTNBTParser {
  
  /**
   * Parse result containing grid and tiles.
   */
  public static class ParseResult {
    public final LittleGrid grid;
    public final List<LTTile> tiles;
    
    public ParseResult(LittleGrid grid, List<LTTile> tiles) {
      this.grid = grid;
      this.tiles = tiles;
    }
    
    @Override
    public String toString() {
      return String.format("ParseResult{grid=%s, tiles=%d}", grid, tiles.size());
    }
  }
  
  /**
   * Parse LittleTiles NBT data.
   * 
   * @param nbt the block entity NBT tag
   * @return parsed result with grid and tiles
   */
  public static ParseResult parse(CompoundTag nbt) {
    if (nbt == null) {
      if (DebugConfig.ENABLE_WARNINGS) {
        String warnMsg = "LTNBTParser: null NBT tag";
        Log.warn(warnMsg);
        System.out.println("[WARNING] " + warnMsg);
      }
      return new ParseResult(LittleGrid.getDefault(), new ArrayList<>());
    }
    
    // Debug: removed after diagnosis
    
    // 1. Parse grid field (default 16 if missing)
    LittleGrid grid = parseGrid(nbt);
    
    // 2. Parse content field
    Tag contentTag = nbt.get("content");
    if (contentTag == null || contentTag.isError()) {
      if (DebugConfig.ENABLE_WARNINGS) {
        String warnMsg = "LTNBTParser: missing or invalid 'content' field";
        Log.warn(warnMsg);
        System.out.println("[WARNING] " + warnMsg);
      }
      return new ParseResult(grid, new ArrayList<>());
    }
    
    CompoundTag content = (CompoundTag) contentTag;
    List<LTTile> tiles = new ArrayList<>();
    
    // 3. Get the 'tiles' CompoundTag from content (this is the actual tile container)
    Tag tilesTag = content.get("tiles");
    if (tilesTag == null || tilesTag.isError()) {
      if (DebugConfig.VERBOSE_NBT_PARSER) {
        Log.info("LTNBTParser: no 'tiles' field in content, trying direct parsing (legacy format?)");
      }
      // Fallback: try parsing content directly as blockstate map
      tilesTag = contentTag;
    } else if (!(tilesTag instanceof CompoundTag)) {
        if (DebugConfig.ENABLE_WARNINGS) {
          String warnMsg = "LTNBTParser: 'tiles' field is not CompoundTag, type: " + tilesTag.getClass().getSimpleName();
          Log.warn(warnMsg);
          System.out.println("[WARNING] " + warnMsg);
        }
        return new ParseResult(grid, new ArrayList<>());
      }
    
    CompoundTag tilesData = (CompoundTag) tilesTag;
    
    // 4. Iterate over each blockstate group in tiles
    for (NamedTag entry : tilesData) {
      String blockstate = entry.name;
      
      // Skip special keys
      if ("children".equals(blockstate)) {
        continue;
      }
      
      Tag valueTag = entry.tag;
        if (!valueTag.isList()) {
          if (DebugConfig.ENABLE_WARNINGS) {
            String warnMsg = "LTNBTParser: blockstate '" + blockstate + "' value is not a ListTag, type: " + valueTag.getClass().getSimpleName();
            Log.warn(warnMsg);
            System.out.println("[WARNING] " + warnMsg);
          }
          continue;
        }
      
      ListTag boxList = (ListTag) valueTag;
        if (boxList.size() < 2) {
          if (DebugConfig.ENABLE_WARNINGS) {
            String warnMsg = "LTNBTParser: blockstate '" + blockstate + "' has insufficient elements";
            Log.warn(warnMsg);
            System.out.println("[WARNING] " + warnMsg);
          }
          continue;
        }
      
      // 4. First element is color
      int color = parseColor(boxList.get(0));
      
      // 5. Remaining elements are boxes
      for (int i = 1; i < boxList.size(); i++) {
        Tag boxTag = boxList.get(i);
        
        // Get int array without checking length first
        int[] boxData = boxTag.intArray();
          if (boxData == null) {
            if (DebugConfig.ENABLE_WARNINGS) {
              String warnMsg = "LTNBTParser: box[" + i + "] is not IntArrayTag";
              Log.warn(warnMsg);
              System.out.println("[WARNING] " + warnMsg);
            }
            continue;
          }
        
        if (boxData.length == 6) {
          // Basic box format: [minX, minY, minZ, maxX, maxY, maxZ]
          try {
            LittleBox box = LittleBox.fromArray(boxData);
              if (!box.isValid()) {
                if (DebugConfig.ENABLE_WARNINGS) {
                  String warnMsg = "LTNBTParser: invalid box " + box + ", skipping";
                  Log.warn(warnMsg);
                  System.out.println("[WARNING] " + warnMsg);
                }
                continue;
              }
            tiles.add(new LTTile(blockstate, color, box));
            } catch (IllegalArgumentException e) {
              if (DebugConfig.ENABLE_WARNINGS) {
                String warnMsg = "LTNBTParser: failed to parse box[" + i + "]: " + e.getMessage();
                Log.warn(warnMsg);
                System.out.println("[WARNING] " + warnMsg);
              }
            }
        } else if (boxData.length == 7) {
          // Extended box format: [faceCache, minX, minY, minZ, maxX, maxY, maxZ]
          // Skip the first element (faceCache) and take elements 1-6
          int[] basicBox = new int[6];
          System.arraycopy(boxData, 1, basicBox, 0, 6);
          try {
            LittleBox box = LittleBox.fromArray(basicBox);
              if (!box.isValid()) {
                if (DebugConfig.ENABLE_WARNINGS) {
                  String warnMsg = "LTNBTParser: invalid extended box " + box + ", skipping";
                  Log.warn(warnMsg);
                  System.out.println("[WARNING] " + warnMsg);
                }
                continue;
              }
            tiles.add(new LTTile(blockstate, color, box));
            } catch (IllegalArgumentException e) {
              if (DebugConfig.ENABLE_WARNINGS) {
                String warnMsg = "LTNBTParser: failed to parse extended box[" + i + "]: " + e.getMessage();
                Log.warn(warnMsg);
                System.out.println("[WARNING] " + warnMsg);
              }
            }
        } else if (boxData.length > 7) {
          // Deformed box format (slopes, etc.) - skip for now (C3 scope)
          if (DebugConfig.VERBOSE_NBT_PARSER) {
            Log.info("LTNBTParser: box[" + i + "] has deformed data (length=" + boxData.length + "), skipping (C3 scope)");
          }
        } else {
            if (DebugConfig.ENABLE_WARNINGS) {
              String warnMsg = "LTNBTParser: box[" + i + "] has invalid length " + boxData.length + ", expected 6 or 7";
              Log.warn(warnMsg);
              System.out.println("[WARNING] " + warnMsg);
            }
          }
      }
    }
    
    String msg = "LTNBTParser: parsed " + tiles.size() + " tiles with grid=" + grid.count;
    // Summary only (no per-blockstate logging)
    if (DebugConfig.VERBOSE_NBT_PARSER) {
      Log.info(msg);
      System.out.println(msg);
    }
    
    return new ParseResult(grid, tiles);
  }
  
  /**
   * Parse grid field from NBT, defaulting to 16 if missing.
   */
  private static LittleGrid parseGrid(CompoundTag nbt) {
    Tag gridTag = nbt.get("grid");
    if (gridTag == null || gridTag.isError()) {
      return LittleGrid.getDefault();
    }
    
    int gridCount = gridTag.intValue(16);
    try {
      return new LittleGrid(gridCount);
    } catch (IllegalArgumentException e) {
      if (DebugConfig.ENABLE_WARNINGS) {
        String warnMsg = "LTNBTParser: invalid grid value " + gridCount + ", using default";
        Log.warn(warnMsg);
        System.out.println("[WARNING] " + warnMsg);
      }
      return LittleGrid.getDefault();
    }
  }
  
  /**
   * Parse color from the first element of the blockstate list.
   * Expected format: IntArrayTag with at least 1 element.
   */
  private static int parseColor(Tag colorTag) {
    int[] colorData = colorTag.intArray();
    if (colorData == null || colorData.length < 1) {
      if (DebugConfig.ENABLE_WARNINGS) {
        String warnMsg = "LTNBTParser: color tag is not valid IntArrayTag, using default white";
        Log.warn(warnMsg);
        System.out.println("[WARNING] " + warnMsg);
      }
      return 0xFFFFFFFF;
    }
    
    return colorData[0];
  }
}
