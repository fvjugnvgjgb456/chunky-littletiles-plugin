package dev.trae.chunky.littletiles.c7;

import dev.trae.chunky.littletiles.DebugConfig;
import dev.trae.chunky.littletiles.LittleGrid;
import dev.trae.chunky.littletiles.VectorPool;
import se.llbit.chunky.resources.Texture;
import se.llbit.chunky.world.Material;
import se.llbit.chunky.world.material.TextureMaterial;
import se.llbit.math.Vector2;
import se.llbit.math.Vector3;
import se.llbit.math.primitive.Primitive;
import se.llbit.math.primitive.TexturedTriangle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Converts a TransformableBox to Chunky primitives.
 *
 * <p>Geometry is a port of LittleTiles' {@code LittleTransformableBox.requestCache()}
 * (LittleTiles 2dd8a8256, LittleTransformableBox.java:217-370) and the CreativeCore helpers
 * it uses (BoxFace, BoxCorner, VectorFan.cut, NormalPlaneF):
 * <ol>
 *   <li>The 8 corners are moved by their offsets; offsets may reach far outside the AABB.</li>
 *   <li>Each face that left its AABB plane becomes one or two tilted strips (split according
 *       to the face's flip bit), clipped against the 6 AABB planes.</li>
 *   <li>The 6 axis-aligned AABB faces are cut by the tilted planes.</li>
 * </ol>
 * The result is the deformed shape intersected with the AABB, so it never exceeds the box.
 *
 * <p>UVs are block-relative (LittleTiles texture convention) with the per-face orientation of
 * Chunky's {@code Box.addXxxFaces}, so a full face looks the same as on a basic tile. Tilted
 * strips use the projection of their nearest facing, like LittleTiles' {@code sortTiltedStrip}.
 */
public class TransformableBoxConverter {

  /** CreativeCore VectorFan.EPSILON (grid units). */
  private static final double EPSILON = 0.0005;
  /** Default epsilon of NormalPlaneF.isInFront, used by VectorFan.cut. */
  private static final double CUT_EPSILON = 1.0E-7;
  /** CreativeCore Maths.EPSILON, used by NormalPlaneF.isFacing. */
  private static final double FACING_EPSILON = 0.001;

  /** CreativeCore Facing order; also the flip bit order (bits 24..29). */
  static final int DOWN = 0, UP = 1, NORTH = 2, SOUTH = 3, WEST = 4, EAST = 5;
  private static final int[] FACING_AXIS = {1, 1, 2, 2, 0, 0};
  private static final boolean[] FACING_POSITIVE = {false, true, false, true, false, true};

  /** CreativeCore BoxFace corner order per facing (AngleID ordinals: EUN0 EUS1 EDN2 EDS3 WUN4 WUS5 WDN6 WDS7). */
  private static final int[][] FACE_CORNERS = {
      {7, 6, 2, 3}, // DOWN:  WDS, WDN, EDN, EDS
      {4, 5, 1, 0}, // UP:    WUN, WUS, EUS, EUN
      {0, 2, 6, 4}, // NORTH: EUN, EDN, WDN, WUN
      {5, 7, 3, 1}, // SOUTH: WUS, WDS, EDS, EUS
      {4, 6, 7, 5}, // WEST:  WUN, WDN, WDS, WUS
      {1, 3, 2, 0}, // EAST:  EUS, EDS, EDN, EUN
  };

  /**
   * A convex polygon in block-relative grid units, the facing used for its UV projection and
   * its outward normal (used to orient triangles so Chunky's normal points out of the solid).
   */
  static final class Strip {
    final double[][] points;
    final int facing;
    final double[] normal;

    Strip(double[][] points, int facing, double[] normal) {
      this.points = points;
      this.facing = facing;
      this.normal = normal;
    }
  }

  private static double[] facingNormal(int facing) {
    double[] n = new double[3];
    n[FACING_AXIS[facing]] = FACING_POSITIVE[facing] ? 1 : -1;
    return n;
  }

  /**
   * Convert a TransformableBox (offsets + flips) to textured triangles with optional color tinting or direct Material.
   * C10: Uses vanilla Block for Materials Tab integration.
   */
  public static Collection<Primitive> boxToPrimitives(
      TransformableBox box, LittleGrid grid, Vector3 blockPos, Texture texture, int color, 
      se.llbit.chunky.block.Block block, Material directMaterial) {
    if (DebugConfig.VERBOSE_PRIMITIVE_GENERATION) {
      System.out.println(String.format(
          "[TransformableBoxConverter.boxToPrimitives] block=%s, color=0x%08X",
          block.name, color));
    }
    List<Strip> strips = computeStrips(box, tiltedCorners(box), box.getFlipped());
    if (DebugConfig.VERBOSE_PRIMITIVE_GENERATION) {
      System.out.println(String.format(
          "[TransformableBox] AABB=[%d,%d,%d]->[%d,%d,%d] grid=%d -> %d strips",
          box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, grid.count, strips.size()));
    }
    return emit(strips, grid, blockPos, texture, color, block, directMaterial);
  }

  /**
   * Plain AABB through the same emitter, so basic and transformable tiles share the UV convention.
   * C10: Uses vanilla Block for Materials Tab integration.
   */
  public static Collection<Primitive> aabbToPrimitives(
      dev.trae.chunky.littletiles.LittleBox box, LittleGrid grid, Vector3 blockPos, Texture texture, int color, 
      se.llbit.chunky.block.Block block, Material directMaterial) {
    if (DebugConfig.VERBOSE_PRIMITIVE_GENERATION) {
      System.out.println(String.format(
          "[TransformableBoxConverter.aabbToPrimitives] block=%s, color=0x%08X",
          block.name, color));
    }
    List<Strip> strips = computeStrips(box, boxCorners(box), Flipped.none());
    if (DebugConfig.VERBOSE_PRIMITIVE_GENERATION) {
      System.out.println(String.format(
          "[BasicAABB] AABB=[%d,%d,%d]->[%d,%d,%d] grid=%d -> %d strips",
          box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, grid.count, strips.size()));
    }
    return emit(strips, grid, blockPos, texture, color, block, directMaterial);
  }

  /** Box corners in grid units, indexed by AngleID ordinal (E=maxX, U=maxY, N=minZ). */
  static double[][] boxCorners(dev.trae.chunky.littletiles.LittleBox box) {
    double[][] c = new double[8][];
    for (AngleID id : AngleID.values()) {
      c[id.ordinal()] = new double[] {
          id.isEast() ? box.maxX : box.minX,
          id.isUp() ? box.maxY : box.minY,
          id.isNorth() ? box.minZ : box.maxZ};
    }
    return c;
  }

  /** Corners moved by their offsets (LittleTransformableBox.getTiltedCorners). */
  static double[][] tiltedCorners(TransformableBox box) {
    double[][] c = boxCorners(box);
    for (AngleID id : AngleID.values()) {
      AngleOffset o = box.getOffset(id);
      double[] p = c[id.ordinal()];
      p[0] += o.getEffectiveX();
      p[1] += o.getEffectiveY();
      p[2] += o.getEffectiveZ();
    }
    return c;
  }

  private static boolean isFlipped(Flipped f, int facing) {
    switch (facing) {
      case DOWN: return f.down;
      case UP: return f.up;
      case NORTH: return f.north;
      case SOUTH: return f.south;
      case WEST: return f.west;
      default: return f.east;
    }
  }

  private static double bound(dev.trae.chunky.littletiles.LittleBox box, int facing) {
    switch (facing) {
      case DOWN: return box.minY;
      case UP: return box.maxY;
      case NORTH: return box.minZ;
      case SOUTH: return box.maxZ;
      case WEST: return box.minX;
      default: return box.maxX;
    }
  }

  /** Port of CreativeCore NormalPlaneF (normal is normalized; NaN normal = invalid plane). */
  static final class Plane {
    final double[] origin;
    final double[] normal;

    Plane(double[] origin, double[] normal) {
      this.origin = origin.clone();
      this.normal = normalize(normal);
    }

    boolean isInvalid() {
      return Double.isNaN(normal[0]) || Double.isNaN(normal[1]) || Double.isNaN(normal[2]);
    }

    /** null = on the plane, true = in front (outside), false = behind. */
    Boolean isInFront(double[] p, double eps) {
      double d = dot(normal, sub(p, origin));
      if (d < 0 ? d > -eps : d < eps) {
        return null;
      }
      return d > 0;
    }

    double[] intersect(double[] start, double[] end) {
      double[] dir = normalize(sub(end, start));
      double nd = dot(normal, dir);
      if (Math.abs(nd) < 1.0E-7 || Double.isNaN(nd)) {
        return null;
      }
      double t = (dot(normal, origin) - dot(normal, start)) / nd;
      return new double[] {start[0] + dir[0] * t, start[1] + dir[1] * t, start[2] + dir[2] * t};
    }

    boolean isFacing(int facing) {
      double[] f = new double[3];
      f[FACING_AXIS[facing]] = FACING_POSITIVE[facing] ? 1 : -1;
      return Math.abs(normal[0] - f[0]) < FACING_EPSILON
          && Math.abs(normal[1] - f[1]) < FACING_EPSILON
          && Math.abs(normal[2] - f[2]) < FACING_EPSILON;
    }
  }

  /**
   * Port of CreativeCore VectorFan.cutInternal: keeps the part behind the plane (opposite to
   * its normal). A polygon lying entirely on the plane is dropped, as in the original.
   */
  static double[][] cut(double[][] pts, Plane plane) {
    if (pts == null) {
      return null;
    }
    int n = pts.length;
    Boolean[] keep = new Boolean[n];
    boolean allSame = true;
    Boolean allValue = null;
    for (int i = 0; i < n; i++) {
      Boolean front = plane.isInFront(pts[i], CUT_EPSILON);
      keep[i] = front == null ? null : !front;
      if (allSame) {
        if (i == 0 || allValue == null) {
          allValue = keep[i];
        } else if (keep[i] != null && !allValue.equals(keep[i])) {
          allSame = false;
        }
      }
    }
    if (allSame) {
      return Boolean.TRUE.equals(allValue) ? pts : null;
    }

    List<double[]> right = new ArrayList<>();
    Boolean before = keep[n - 1];
    double[] beforeVec = pts[n - 1];
    for (int i = 0; i < n; i++) {
      double[] vec = pts[i];
      if (Boolean.TRUE.equals(keep[i])) {
        if (Boolean.FALSE.equals(before)) {
          addIfNotNull(right, plane.intersect(vec, beforeVec));
        }
        right.add(vec);
      } else if (Boolean.FALSE.equals(keep[i])) {
        if (Boolean.TRUE.equals(before)) {
          addIfNotNull(right, plane.intersect(vec, beforeVec));
        }
      } else {
        right.add(vec);
      }
      before = keep[i];
      beforeVec = vec;
    }
    if (right.size() >= 3 && isPointBetween(right.get(right.size() - 2), right.get(0), right.get(right.size() - 1))) {
      right.remove(right.size() - 1);
    }
    if (right.size() >= 3 && isPointBetween(right.get(right.size() - 1), right.get(1), right.get(0))) {
      right.remove(0);
    }
    if (right.size() < 3) {
      return null;
    }
    return right.toArray(new double[0][]);
  }

  /** Points of the given corners, de-duplicated (LittleTransformableBox.createStrip). */
  private static double[][] createStrip(int[] idx, double[][] corners) {
    List<double[]> pts = new ArrayList<>();
    outer:
    for (int i : idx) {
      for (double[] p : pts) {
        if (epsEq(corners[i], p, EPSILON)) {
          continue outer;
        }
      }
      pts.add(corners[i].clone());
    }
    return pts.size() < 3 ? null : pts.toArray(new double[0][]);
  }

  /** True if the triangle's corners still lie on the AABB plane of this facing axis. */
  private static boolean checkEqual(double[][] corners, double[][] boxCorners, int[] idx, int axis) {
    for (int i : idx) {
      if (corners[i][axis] != boxCorners[i][axis]) {
        return false;
      }
    }
    return true;
  }

  private static double[] triangleNormal(int[] idx, double[][] c) {
    return cross(sub(c[idx[1]], c[idx[0]]), sub(c[idx[2]], c[idx[0]]));
  }

  /** CreativeCore Facing.nearest. */
  static int nearest(double[] n) {
    int best = -1;
    double dist = Float.MIN_VALUE;
    for (int f = 0; f < 6; f++) {
      double d = n[FACING_AXIS[f]] * (FACING_POSITIVE[f] ? 1 : -1);
      if (d > dist) {
        dist = d;
        best = f;
      }
    }
    return best < 0 ? UP : best;
  }

  /**
   * Port of LittleTransformableBox.requestCache(): returns all visible polygons (axis strips
   * per facing + tilted strips) in block-relative grid units.
   */
  static List<Strip> computeStrips(
      dev.trae.chunky.littletiles.LittleBox box, double[][] corners, Flipped flipped) {
    double[][] boxC = boxCorners(box);

    Plane[] axisPlanes = new Plane[6];
    for (int f = 0; f < 6; f++) {
      double[] origin = new double[3];
      double[] normal = new double[3];
      origin[FACING_AXIS[f]] = bound(box, f);
      normal[FACING_AXIS[f]] = FACING_POSITIVE[f] ? 1 : -1;
      axisPlanes[f] = new Plane(origin, normal);
    }

    Plane[] tilted = new Plane[12];
    double[][][] strip1 = new double[6][][];
    double[][][] strip2 = new double[6][][];
    boolean[] convex = {true, true, true, true, true, true};
    double[] zero = {0, 0, 0};

    // Tilted strips against the axis box
    for (int i = 0; i < 6; i++) {
      int[] fc = FACE_CORNERS[i];
      boolean inv = isFlipped(flipped, i);
      int[] first = inv ? new int[] {fc[0], fc[1], fc[3]} : new int[] {fc[0], fc[1], fc[2]};
      int[] second = inv ? new int[] {fc[1], fc[2], fc[3]} : new int[] {fc[0], fc[2], fc[3]};
      int axis = FACING_AXIS[i];

      boolean firstSame = checkEqual(corners, boxC, first, axis);
      boolean secondSame = checkEqual(corners, boxC, second, axis);
      if (firstSame && secondSame) {
        continue;
      }
      double[] n1 = normalize(triangleNormal(first, corners));
      double[] n2 = normalize(triangleNormal(second, corners));

      if (epsEq(n1, n2, EPSILON)) {
        if (!firstSame && !epsEq(n1, zero, EPSILON)) {
          strip1[i] = createStrip(fc, corners);
          if (strip1[i] != null) {
            tilted[i * 2] = new Plane(corners[first[0]], n1);
          }
        }
      } else {
        if (!firstSame && !epsEq(n1, zero, EPSILON)) {
          strip1[i] = createStrip(first, corners);
          if (strip1[i] != null) {
            tilted[i * 2] = new Plane(corners[first[0]], n1);
          }
        }
        if (!secondSame && !epsEq(n2, zero, EPSILON)) {
          strip2[i] = createStrip(second, corners);
          if (strip2[i] != null) {
            tilted[i * 2 + 1] = new Plane(corners[second[0]], n2);
          }
        }
      }

      // Remove faces that are just a line
      if (strip1[i] != null && tilted[i * 2].isInvalid()) {
        strip1[i] = null;
        tilted[i * 2] = null;
      }
      if (strip2[i] != null && tilted[i * 2 + 1].isInvalid()) {
        strip2[i] = null;
        tilted[i * 2 + 1] = null;
      }

      if (strip1[i] != null && strip2[i] != null) {
        for (double[] p : strip2[i]) {
          if (Boolean.TRUE.equals(tilted[i * 2].isInFront(p, EPSILON))) {
            convex[i] = false;
            break;
          }
        }
      }

      // Clip tilted strips to the AABB
      for (Plane p : axisPlanes) {
        strip1[i] = cut(strip1[i], p);
        strip2[i] = cut(strip2[i], p);
      }
    }

    List<Strip> out = new ArrayList<>();

    // Axis strips against the transformed box
    for (int i = 0; i < 6; i++) {
      List<double[][]> axis = new ArrayList<>();
      int[] fc = FACE_CORNERS[i];
      axis.add(new double[][] {boxC[fc[0]].clone(), boxC[fc[1]].clone(), boxC[fc[2]].clone(), boxC[fc[3]].clone()});

      for (int j = 0; j < 6; j++) {
        Plane cp1 = null;
        Plane cp2 = null;
        boolean hasTilted = strip1[j] != null || strip2[j] != null;
        if (!hasTilted || !convex[j] || (strip1[j] != null && strip2[j] != null)) {
          cp1 = tilted[j * 2];
          cp2 = tilted[j * 2 + 1];
        } else if (strip1[j] != null) {
          cp1 = tilted[j * 2];
        } else {
          cp1 = tilted[j * 2 + 1];
        }

        if (convex[j] || cp1 == null || cp2 == null) {
          if (cp1 != null) {
            cutAxis(axis, i, cp1);
          }
          if (cp2 != null) {
            cutAxis(axis, i, cp2);
          }
        } else {
          cutAxisConcave(axis, cp1, cp2);
        }
        if (axis.isEmpty()) {
          break;
        }
      }
      for (double[][] poly : axis) {
        out.add(new Strip(poly, i, facingNormal(i)));
      }
    }

    // Tilted strips are textured like their nearest facing (LittleTiles sortTiltedStrip).
    // Tilted plane normals point out of the solid: cut() keeps the side behind them.
    for (int i = 0; i < 6; i++) {
      if (strip1[i] != null) {
        double[] n = tilted[i * 2].normal;
        out.add(new Strip(strip1[i], nearest(n), n));
      }
      if (strip2[i] != null) {
        double[] n = tilted[i * 2 + 1].normal;
        out.add(new Strip(strip2[i], nearest(n), n));
      }
    }
    return out;
  }

  /** VectorFanFaceCache.cutAxisStrip(facing, plane): intersection with the half space. */
  private static void cutAxis(List<double[][]> axis, int facing, Plane plane) {
    if (plane.isFacing(facing)) {
      return;
    }
    for (int k = axis.size() - 1; k >= 0; k--) {
      double[][] r = cut(axis.get(k), plane);
      if (r == null) {
        axis.remove(k);
      } else {
        axis.set(k, r);
      }
    }
  }

  /**
   * VectorFanFaceCache.cutAxisStrip(facing, plane1, plane2) for concave faces: the union of
   * both half spaces. LittleTiles subtracts the overlap with a 2D cut; for ray tracing the
   * overlap is coplanar with identical UVs, so both pieces are kept as-is.
   */
  private static void cutAxisConcave(List<double[][]> axis, Plane p1, Plane p2) {
    List<double[][]> result = new ArrayList<>();
    for (double[][] poly : axis) {
      double[][] a = cut(poly, p1);
      double[][] b = cut(poly, p2);
      if (a != null) {
        result.add(a);
      }
      if (b != null && (a == null || !samePolygon(a, b))) {
        result.add(b);
      }
    }
    axis.clear();
    axis.addAll(result);
  }

  /**
   * Triangulate strips (convex fans) into world-space textured triangles.
   * C10: Uses vanilla Block as Material source, enabling Materials Tab property adjustments.
   */
  private static Collection<Primitive> emit(
      List<Strip> strips, LittleGrid grid, Vector3 blockPos, Texture texture, int color, 
      se.llbit.chunky.block.Block block, Material directMaterial) {
    // Create material with optional color tinting
    // Special case: if directMaterial is provided (fluid blocks), use it directly to preserve special properties
    Material material;
    int effectiveColor = color;
    
    // Debug: log block parameter
    if (DebugConfig.VERBOSE_PRIMITIVE_GENERATION) {
      System.out.println(String.format("  [emit] C10: block=%s, color=0x%08X", 
          block.name, color));
    }
    
    // Apply default biome tinting for foliage blocks (Minecraft leaves/grass textures are grayscale)
    if ((color == -1 || color == 0xFFFFFFFF) && block.name != null) {
      String lowerBlockstate = block.name.toLowerCase();
      if (lowerBlockstate.contains("leaves") || lowerBlockstate.contains("vine") || 
          lowerBlockstate.contains("grass_block") || lowerBlockstate.contains("tall_grass") ||
          lowerBlockstate.contains("fern")) {
        effectiveColor = 0xFF48B518;  // RGB(72, 181, 24) - default grass/foliage green
        if (DebugConfig.VERBOSE_PRIMITIVE_GENERATION) {
          System.out.println(String.format("  [emit] C10: Applied foliage tint to: %s", block.name));
        }
      }
    }
    
    // C10: Determine material
    if (directMaterial != null) {
      // Fluid blocks: use Water/Lava Material directly to preserve refractive/emissive properties
      material = directMaterial;
      if (DebugConfig.VERBOSE_PRIMITIVE_GENERATION) {
        System.out.println(String.format("  [emit] Using directMaterial (fluid): %s", material.getClass().getSimpleName()));
      }
    } else {
      // C10: Use vanilla Block as Material
      // When user adjusts properties in Materials Tab, BlockPalette.updateProperties() 
      // updates the vanilla Block, and all tiles automatically inherit the changes
      if (effectiveColor == -1 || effectiveColor == 0xFFFFFFFF) {
        material = block;  // Use vanilla Block directly
      } else {
        // C10 Known Limitation: Tinted tiles use TintedTextureMaterial
        // Material properties (emittance, specular, etc.) are copied at creation time
        // and will NOT sync with Materials Tab adjustments.
        // This is a Java limitation: Material properties are public fields accessed directly,
        // and we cannot intercept field access to delegate to the vanilla Block.
        material = new TintedTextureMaterial(texture, effectiveColor);
      }
      if (DebugConfig.VERBOSE_PRIMITIVE_GENERATION) {
        System.out.println(String.format("  [emit] C10: Using material, color=0x%08X, result=%s", 
            effectiveColor, material.getClass().getSimpleName()));
      }
    }
    
    double scale = 1.0 / grid.count;
    List<Primitive> primitives = new ArrayList<>();
    
    // C11 Phase 2: Use VectorPool to reduce temporary object allocations
    if (DebugConfig.VERBOSE_PRIMITIVE_GENERATION) {
      System.out.println("[emit] C11: Starting with VectorPool optimization, strips=" + strips.size());
    }
    
    for (Strip strip : strips) {
      int n = strip.points.length;
      Vector3[] w = new Vector3[n];
      Vector2[] uv = new Vector2[n];
      
      // Obtain temporary vectors from pool
      Vector3[] wTemp = new Vector3[n];
      Vector2[] uvTemp = new Vector2[n];
      for (int k = 0; k < n; k++) {
        wTemp[k] = VectorPool.obtainVector3();
        uvTemp[k] = VectorPool.obtainVector2();
      }
      
      // Calculate world positions and UVs using pooled vectors
      for (int k = 0; k < n; k++) {
        double[] local = {strip.points[k][0] * scale, strip.points[k][1] * scale, strip.points[k][2] * scale};
        wTemp[k].set(blockPos.x + local[0], blockPos.y + local[1], blockPos.z + local[2]);
        Vector2 uvResult = uv(strip.facing, local);
        uvTemp[k].set(uvResult.x, uvResult.y);
      }
      
      // Copy to final arrays (TexturedTriangle needs owned vectors)
      for (int k = 0; k < n; k++) {
        w[k] = new Vector3(wTemp[k]);
        uv[k] = new Vector2(uvTemp[k]);
      }
      
      // Return pooled vectors
      for (int k = 0; k < n; k++) {
        VectorPool.freeVector3(wTemp[k]);
        VectorPool.freeVector2(uvTemp[k]);
      }
      
      // Generate triangles
      for (int k = 1; k + 1 < n; k++) {
        double[] e1 = {w[k].x - w[0].x, w[k].y - w[0].y, w[k].z - w[0].z};
        double[] e2 = {w[k + 1].x - w[0].x, w[k + 1].y - w[0].y, w[k + 1].z - w[0].z};
        double[] c = cross(e1, e2);
        if (dot(c, c) < 1.0E-14) {
          continue; // degenerate triangle
        }
        // TexturedTriangle's normal is (c3-c1) x (c2-c1) = -c; make it point outward.
        if (dot(c, strip.normal) > 0) {
          primitives.add(new TexturedTriangle(w[0], w[k + 1], w[k], uv[0], uv[k + 1], uv[k], material));
        } else {
          primitives.add(new TexturedTriangle(w[0], w[k], w[k + 1], uv[0], uv[k], uv[k + 1], material));
        }
      }
    }
    return primitives;
  }

  /**
   * Block-relative UV (LittleTiles convention: texture coordinate = position inside the block),
   * oriented like Minecraft's default block faces. Chunky's Texture.getColor samples row (1-v).
   */
  static Vector2 uv(int facing, double[] l) {
    double x = clamp(l[0]);
    double y = clamp(l[1]);
    double z = clamp(l[2]);
    switch (facing) {
      case DOWN: return new Vector2(x, z);
      case UP: return new Vector2(x, 1 - z);
      case NORTH: return new Vector2(1 - x, y);
      case SOUTH: return new Vector2(x, y);
      case WEST: return new Vector2(z, y);
      default: return new Vector2(1 - z, y); // EAST
    }
  }

  // ---- small vector helpers ----

  private static double clamp(double v) {
    return v < 0 ? 0 : (v > 1 ? 1 : v);
  }

  private static double[] sub(double[] a, double[] b) {
    return new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
  }

  private static double dot(double[] a, double[] b) {
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
  }

  private static double[] cross(double[] a, double[] b) {
    return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
  }

  /** Zero vector yields NaN, matching Vec3f.normalize (used to detect invalid planes). */
  private static double[] normalize(double[] v) {
    double len = Math.sqrt(dot(v, v));
    return new double[] {v[0] / len, v[1] / len, v[2] / len};
  }

  private static boolean epsEq(double[] a, double[] b, double eps) {
    for (int k = 0; k < 3; k++) {
      double d = a[k] - b[k];
      if (Double.isNaN(d) || Math.abs(d) > eps) {
        return false;
      }
    }
    return true;
  }

  private static boolean samePolygon(double[][] a, double[][] b) {
    if (a.length != b.length) {
      return false;
    }
    for (int k = 0; k < a.length; k++) {
      if (!epsEq(a[k], b[k], EPSILON)) {
        return false;
      }
    }
    return true;
  }

  /** VectorFan.isPointBetween: true if 'between' lies on the line start-end. */
  private static boolean isPointBetween(double[] start, double[] end, double[] between) {
    double[] c = cross(sub(end, start), sub(between, start));
    return Math.abs(c[0]) + Math.abs(c[1]) + Math.abs(c[2]) < EPSILON;
  }

  private static void addIfNotNull(List<double[]> list, double[] p) {
    if (p != null) {
      list.add(p);
    }
  }
}
