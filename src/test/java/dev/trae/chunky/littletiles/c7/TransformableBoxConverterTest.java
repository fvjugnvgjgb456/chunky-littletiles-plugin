package dev.trae.chunky.littletiles.c7;

import dev.trae.chunky.littletiles.LittleBox;
import dev.trae.chunky.littletiles.LittleGrid;
import org.junit.jupiter.api.Test;
import se.llbit.chunky.resources.Texture;
import se.llbit.math.Vector3;
import se.llbit.math.primitive.Primitive;
import se.llbit.math.primitive.TexturedTriangle;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Geometry tests for the LittleTiles requestCache() port.
 * Key invariant: a transformable tile never extends outside its AABB.
 */
public class TransformableBoxConverterTest {

  private static final double EPS = 1e-6;

  private static AngleOffset[] zeroOffsets() {
    AngleOffset[] o = new AngleOffset[8];
    for (int i = 0; i < 8; i++) {
      o[i] = AngleOffset.zero();
    }
    return o;
  }

  private static AngleOffset off(int x, int y, int z) {
    return new AngleOffset(x != 0, y != 0, z != 0, (short) x, (short) y, (short) z);
  }

  /** Every triangle vertex must lie inside [min,max] (world units). */
  private static void assertInside(Collection<Primitive> prims, double[] min, double[] max) {
    for (Primitive p : prims) {
      TexturedTriangle t = (TexturedTriangle) p;
      Vector3 c1 = new Vector3(t.o);
      Vector3 c2 = new Vector3(t.o);
      c2.add(t.e1);
      Vector3 c3 = new Vector3(t.o);
      c3.add(t.e2);
      for (Vector3 v : new Vector3[] {c1, c2, c3}) {
        assertTrue(v.x >= min[0] - EPS && v.x <= max[0] + EPS, "x out of AABB: " + v.x);
        assertTrue(v.y >= min[1] - EPS && v.y <= max[1] + EPS, "y out of AABB: " + v.y);
        assertTrue(v.z >= min[2] - EPS && v.z <= max[2] + EPS, "z out of AABB: " + v.z);
      }
    }
  }

  private static double area(Collection<Primitive> prims) {
    double a = 0;
    for (Primitive p : prims) {
      TexturedTriangle t = (TexturedTriangle) p;
      Vector3 c = new Vector3();
      c.cross(t.e1, t.e2);
      a += c.length() / 2;
    }
    return a;
  }

  @Test
  public void plainBox_givesSixFacesTwelveTriangles() {
    TransformableBox box = new TransformableBox(0, 0, 0, 8, 8, 8, zeroOffsets(), Flipped.none());
    se.llbit.chunky.block.Block block = com.littletiles.chunky.MaterialResolver.resolveBlock("minecraft:stone");
    Collection<Primitive> prims = TransformableBoxConverter.boxToPrimitives(
        box, new LittleGrid(16), new Vector3(0, 0, 0), Texture.stone, -1, block, null);
    assertEquals(12, prims.size());
    assertEquals(6 * 0.25, area(prims), 1e-9); // 6 faces of 0.5 x 0.5
  }

  @Test
  public void aabbPath_matchesBoxSize() {
    LittleBox box = new LittleBox(0, 0, 0, 2, 1, 2);
    se.llbit.chunky.block.Block block = com.littletiles.chunky.MaterialResolver.resolveBlock("minecraft:stone");
    Collection<Primitive> prims = TransformableBoxConverter.aabbToPrimitives(
        box, new LittleGrid(2), new Vector3(10, 76, 3), Texture.stone, -1, block, null);
    assertEquals(12, prims.size());
    assertInside(prims, new double[] {10, 76, 3}, new double[] {11, 76.5, 4});
  }

  /** README golden case: [0,0,0,2,1,2] grid 2, offsets reach outside the box on every corner. */
  @Test
  public void readmeGoldenCase_staysInsideAabb() {
    AngleOffset[] o = new AngleOffset[8];
    o[AngleID.EUN.ordinal()] = off(-2, 0, -1);
    o[AngleID.EUS.ordinal()] = off(1, 0, 1);
    o[AngleID.EDN.ordinal()] = off(-2, 0, -1);
    o[AngleID.EDS.ordinal()] = off(1, 0, 1);
    o[AngleID.WUN.ordinal()] = off(-1, 0, -1);
    o[AngleID.WUS.ordinal()] = off(2, 0, 1);
    o[AngleID.WDN.ordinal()] = off(-1, 0, -1);
    o[AngleID.WDS.ordinal()] = off(2, 0, 1);
    TransformableBox box = new TransformableBox(0, 0, 0, 2, 1, 2, o, Flipped.none());
    se.llbit.chunky.block.Block block = com.littletiles.chunky.MaterialResolver.resolveBlock("minecraft:stone");
    Collection<Primitive> prims = TransformableBoxConverter.boxToPrimitives(
        box, new LittleGrid(2), new Vector3(0, 0, 0), Texture.stone, -1, block, null);
    assertFalse(prims.isEmpty());
    assertInside(prims, new double[] {0, 0, 0}, new double[] {1, 0.5, 1});
  }

  /** Values from the 2026-10-17 log: EUN offset (6,1,2) on a 1/4-block-tall box. */
  @Test
  public void largeOffsetsFromLog_doNotRaiseTile() {
    AngleOffset[] o = zeroOffsets();
    o[AngleID.EUN.ordinal()] = off(6, 1, 2);
    o[AngleID.EUS.ordinal()] = off(6, 1, 0);
    o[AngleID.EDN.ordinal()] = off(6, 0, 2);
    TransformableBox box = new TransformableBox(2, 0, 2, 4, 1, 4, o, Flipped.none());
    se.llbit.chunky.block.Block block = com.littletiles.chunky.MaterialResolver.resolveBlock("minecraft:stone");
    Collection<Primitive> prims = TransformableBoxConverter.boxToPrimitives(
        box, new LittleGrid(4), new Vector3(7, 13, 15), Texture.stone, -1, block, null);
    assertFalse(prims.isEmpty());
    assertInside(prims, new double[] {7.5, 13, 15.5}, new double[] {8, 13.25, 16});
  }

  /** Classic slope: top-north edge dropped to the bottom. Must have a tilted face. */
  @Test
  public void slope_hasTiltedFaceInsideBox() {
    AngleOffset[] o = zeroOffsets();
    o[AngleID.EUN.ordinal()] = off(0, -16, 0);
    o[AngleID.WUN.ordinal()] = off(0, -16, 0);
    TransformableBox box = new TransformableBox(0, 0, 0, 16, 16, 16, o, Flipped.none());
    se.llbit.chunky.block.Block block = com.littletiles.chunky.MaterialResolver.resolveBlock("minecraft:stone");
    Collection<Primitive> prims = TransformableBoxConverter.boxToPrimitives(
        box, new LittleGrid(16), new Vector3(0, 0, 0), Texture.stone, -1, block, null);
    assertInside(prims, new double[] {0, 0, 0}, new double[] {1, 1, 1});
    boolean tilted = false;
    for (Primitive p : prims) {
      Vector3 n = ((TexturedTriangle) p).n;
      if (Math.abs(n.y) > 0.1 && Math.abs(n.z) > 0.1) {
        tilted = true;
      }
    }
    assertTrue(tilted, "expected a tilted triangle");
    // bottom 1 + south 1 + slope sqrt(2) + two triangular sides 0.5 each
    assertEquals(1 + 1 + Math.sqrt(2) + 1, area(prims), 1e-6);
  }

  @Test
  public void uv_isBlockRelative() {
    // A quarter-size tile at the block origin samples a quarter of the texture, not all of it.
    var uv = TransformableBoxConverter.uv(TransformableBoxConverter.SOUTH, new double[] {0.25, 0.25, 1});
    assertEquals(0.25, uv.x, EPS);
    assertEquals(0.25, uv.y, EPS);
  }
}
