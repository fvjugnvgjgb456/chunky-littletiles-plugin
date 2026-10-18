package dev.trae.chunky.littletiles;

import dev.trae.chunky.littletiles.c7.BasicBoxParser;
import dev.trae.chunky.littletiles.c7.BoxParser;
import dev.trae.chunky.littletiles.c7.LegacySliceParser;
import dev.trae.chunky.littletiles.c7.TransformableBoxParser;
import se.llbit.log.Log;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.IntArrayTag;
import se.llbit.nbt.ListTag;
import se.llbit.nbt.NamedTag;
import se.llbit.nbt.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Parses LittleTiles NBT data to extract grid, tiles, and geometry.
 * 
 * Based on LittleTiles 1.6.0-pre234-BETA NBT format:
 * - grid (int, optional): defaults to 16 if missing
 * - content (CompoundTag): blockstate → [color, box1, box2, ...]
 * 
 * C7: Uses parser chain (TransformableBoxParser → BasicBoxParser) for graceful fallback.
 */
public class LTNBTParser {
  
  /**
   * Box parser chain: try in order, first successful parse wins.
   * 1. BasicBoxParser: 6-field AABB (C1-C6 original format)
   * 2. TransformableBox: 7+ field + indicator < 0 (C7 advanced geometry)
   * 3. LegacySlice: 7+ field + indicator >= 0 (fallback, extract AABB)
   */
  private static final List<BoxParser> BOX_PARSERS = Arrays.asList(
      new BasicBoxParser(),
      new TransformableBoxParser(),
      new LegacySliceParser()
  );
  
  /**
   * Parse result containing grid and tiles.
   */
  public static class ParseResult {
    public final LittleGrid grid;
    public final List<LTTile> tiles;
    public final List<ChildStructure> children;
    
    public ParseResult(LittleGrid grid, List<LTTile> tiles) {
      this(grid, tiles, new ArrayList<>());
    }
    
    public ParseResult(LittleGrid grid, List<LTTile> tiles, List<ChildStructure> children) {
      this.grid = grid;
      this.tiles = tiles;
      this.children = children;
    }
    
    @Override
    public String toString() {
      return String.format("ParseResult{grid=%s, tiles=%d, children=%d}", 
          grid, tiles.size(), children.size());
    }
  }
  
  /**
   * Represents a child structure from the children field.
   * Each child is a StructureParentCollection with its own tiles.
   */
  public static class ChildStructure {
    public final int index;          // Structure index (for debugging)
    public final int attribute;      // Attribute bitmask (EMISSIVE, NOCOLLISION, etc.)
    public final List<LTTile> tiles; // Geometry data (same format as main content)
    
    public ChildStructure(int index, int attribute, List<LTTile> tiles) {
      this.index = index;
      this.attribute = attribute;
      this.tiles = tiles;
    }
    
    /** Check if this structure is self-emissive (fullbright). */
    public boolean isEmissive() {
      return (attribute & 0x00000008) != 0;
    }
    
    /** Check if this structure has no collision (may skip in some render modes). */
    public boolean isNoCollision() {
      return (attribute & 0x00000002) != 0;
    }
    
    @Override
    public String toString() {
      return String.format("ChildStructure{index=%d, attribute=0x%08X, tiles=%d}", 
          index, attribute, tiles.size());
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
    
    // 4. Parse children field (structures like doors, windows)
    List<ChildStructure> children = parseChildren(content, grid);
    
    // 5. Iterate over each blockstate group in tiles
    for (NamedTag entry : tilesData) {
      String blockstate = entry.name;
      
      // Skip special keys (children is parsed separately above)
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
        
        // C7: Use parser chain for all box formats
        LittleBox box = parseBox(boxData, blockstate, i);
        if (box != null) {
          if (box.isValid()) {
            tiles.add(new LTTile(blockstate, color, box));
          } else {
            // Box parsed but failed validation
            if (DebugConfig.ENABLE_WARNINGS) {
              String warnMsg = "LTNBTParser: box[" + i + "] parsed but invalid: " + box;
              System.out.println("[WARNING] " + warnMsg);
            }
          }
        }
      }
    }
    
    String msg = "LTNBTParser: parsed " + tiles.size() + " tiles, " + 
                 children.size() + " children with grid=" + grid.count;
    // Summary only (no per-blockstate logging)
    if (DebugConfig.VERBOSE_NBT_PARSER) {
      Log.info(msg);
      System.out.println(msg);
    }
    
    ParseResult result = new ParseResult(grid, tiles, children);
    
    System.out.println(String.format(
        "Parsed Entity: grid=%d, tiles=%d, children=%d",
        result.grid.count, result.tiles.size(), result.children.size()));
    
    // Debug: log first tile's color
    if (!result.tiles.isEmpty() && DebugConfig.VERBOSE_NBT_PARSER) {
      LTTile firstTile = result.tiles.get(0);
      System.out.println(String.format(
          "  First tile: blockstate=%s, color=0x%08X",
          firstTile.blockstate, firstTile.color));
    }
    
    return result;
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
   * Parse a box using the parser chain (C7).
   * 
   * Parser chain: TransformableBoxParser → BasicBoxParser
   * - First parser that succeeds returns the box
   * - If all fail, returns null and logs warning
   * 
   * @param boxData NBT int array
   * @param blockstate blockstate name for logging
   * @param index box index for logging
   * @return parsed LittleBox or null if all parsers fail
   */
  private static LittleBox parseBox(int[] boxData, String blockstate, int index) {
    // Try each parser in order
    for (BoxParser parser : BOX_PARSERS) {
      try {
        LittleBox result = parser.parse(boxData);
        if (DebugConfig.VERBOSE_NBT_PARSER) {
          System.out.println("LTNBTParser: " + parser.getClass().getSimpleName() + 
                             " successfully parsed box[" + index + "] (length=" + boxData.length + ")");
        }
        return result;
      } catch (BoxParser.BoxParseException e) {
        // This parser can't handle it, try next
        if (DebugConfig.VERBOSE_NBT_PARSER) {
          System.out.println("LTNBTParser: " + parser.getClass().getSimpleName() + 
                             " skipped box[" + index + "]: " + e.getMessage());
        }
      }
    }
    
    // All parsers failed
    if (DebugConfig.ENABLE_WARNINGS) {
      String warnMsg = "LTNBTParser: failed to parse box[" + index + "] " +
                       "(blockstate=" + blockstate + ", length=" + boxData.length + "), " +
                       "all parsers rejected";
      Log.warn(warnMsg);
      System.out.println("[WARNING] " + warnMsg);
    }
    return null;
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
  
  /**
   * Parse children field (nested structures like doors, windows).
   * 
   * NBT format:
   * children: ListTag<CompoundTag> [
   *   {
   *     "index": int,     // Structure index
   *     "type": int,      // Attribute bitmask
   *     "tiles": CompoundTag { "block_id": ListTag<IntArrayTag> [...] }
   *   }
   * ]
   * 
   * Recursion depth is limited to 10 to prevent stack overflow.
   * 
   * @param content the content CompoundTag (may contain "children" field)
   * @param grid the grid size for parsing child tiles
   * @return list of parsed child structures
   */
  private static List<ChildStructure> parseChildren(CompoundTag content, LittleGrid grid) {
    return parseChildrenRecursive(content, grid, 0);
  }
  
  /**
   * Recursive helper for parseChildren with depth limit.
   * 
   * @param content the content CompoundTag
   * @param grid the grid size
   * @param depth current recursion depth
   * @return list of child structures
   */
  private static List<ChildStructure> parseChildrenRecursive(
      CompoundTag content, LittleGrid grid, int depth) {
    
    List<ChildStructure> result = new ArrayList<>();
    
    // Depth limit to prevent stack overflow
    if (depth >= 10) {
      if (DebugConfig.ENABLE_WARNINGS) {
        String warnMsg = "LTNBTParser: children recursion depth limit reached (10), skipping deeper children";
        Log.warn(warnMsg);
        System.out.println("[WARNING] " + warnMsg);
      }
      return result;
    }
    
    Tag childrenTag = content.get("children");
    if (childrenTag == null || !childrenTag.isList()) {
      return result; // No children, not an error
    }
    
    ListTag childrenList = (ListTag) childrenTag;
    if (DebugConfig.VERBOSE_NBT_PARSER) {
      System.out.println("LTNBTParser: parsing " + childrenList.size() + 
                         " children at depth " + depth);
    }
    
    for (int i = 0; i < childrenList.size(); i++) {
      Tag childTag = childrenList.get(i);
      if (!(childTag instanceof CompoundTag)) {
        if (DebugConfig.ENABLE_WARNINGS) {
          String warnMsg = "LTNBTParser: child[" + i + "] is not CompoundTag";
          Log.warn(warnMsg);
          System.out.println("[WARNING] " + warnMsg);
        }
        continue;
      }
      
      CompoundTag childNBT = (CompoundTag) childTag;
      
      // Extract index and attribute
      int index = childNBT.get("index").intValue(-1);
      int attribute = childNBT.get("type").intValue(0);
      
      // Parse child's tiles (same format as main content)
      Tag childTilesTag = childNBT.get("tiles");
      List<LTTile> childTiles = new ArrayList<>();
      
      if (childTilesTag != null && childTilesTag instanceof CompoundTag) {
        CompoundTag childTilesData = (CompoundTag) childTilesTag;
        
        // Parse each blockstate in child's tiles
        for (NamedTag entry : childTilesData) {
          String blockstate = entry.name;
          
          // Skip special keys
          if ("children".equals(blockstate)) {
            continue;
          }
          
          Tag valueTag = entry.tag;
          if (!valueTag.isList()) {
            continue;
          }
          
          ListTag boxList = (ListTag) valueTag;
          if (boxList.size() < 2) {
            continue;
          }
          
          // First element is color
          int color = parseColor(boxList.get(0));
          
          // Remaining elements are boxes
          for (int j = 1; j < boxList.size(); j++) {
            int[] boxData = boxList.get(j).intArray();
            if (boxData != null) {
              LittleBox box = parseBox(boxData, blockstate, j);
              if (box != null && box.isValid()) {
                childTiles.add(new LTTile(blockstate, color, box));
              }
            }
          }
        }
        
        // Recursively parse nested children (if any)
        List<ChildStructure> nestedChildren = parseChildrenRecursive(childTilesData, grid, depth + 1);
        for (ChildStructure nested : nestedChildren) {
          result.add(nested);
        }
      }
      
      if (!childTiles.isEmpty()) {
        ChildStructure child = new ChildStructure(index, attribute, childTiles);
        result.add(child);
        
        if (DebugConfig.VERBOSE_NBT_PARSER) {
          System.out.println("LTNBTParser: parsed child[" + i + "] " + child);
        }
      }
    }
    
    return result;
  }
}
