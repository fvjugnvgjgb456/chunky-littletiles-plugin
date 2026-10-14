package com.littletiles.chunky;

import dev.trae.chunky.littletiles.LittleBox;
import dev.trae.chunky.littletiles.LittleGrid;
import org.junit.jupiter.api.Test;
import se.llbit.math.Vector3;
import se.llbit.math.primitive.Primitive;

import static org.junit.jupiter.api.Assertions.*;

class GeometryConverterTest {

    private static final double EPSILON = 1e-6;

    @Test
    void testBoxToPrimitive_FullBlock() {
        // 完整方块：grid=16, box=[0,0,0]→[16,16,16]
        LittleGrid grid = new LittleGrid(16);
        LittleBox box = new LittleBox(0, 0, 0, 16, 16, 16);
        Vector3 blockPos = new Vector3(10, 20, 30);
        
        Primitive primitive = GeometryConverter.boxToPrimitive(box, grid, blockPos);
        
        // 验证 bounds
        var bounds = primitive.bounds();
        assertEquals(10.0, bounds.xmin, EPSILON);
        assertEquals(11.0, bounds.xmax, EPSILON);
        assertEquals(20.0, bounds.ymin, EPSILON);
        assertEquals(21.0, bounds.ymax, EPSILON);
        assertEquals(30.0, bounds.zmin, EPSILON);
        assertEquals(31.0, bounds.zmax, EPSILON);
    }

    @Test
    void testBoxToPrimitive_HalfBlock() {
        // 半个方块：grid=16, box=[0,0,0]→[8,8,8]
        LittleGrid grid = new LittleGrid(16);
        LittleBox box = new LittleBox(0, 0, 0, 8, 8, 8);
        Vector3 blockPos = new Vector3(0, 0, 0);
        
        Primitive primitive = GeometryConverter.boxToPrimitive(box, grid, blockPos);
        
        var bounds = primitive.bounds();
        assertEquals(0.0, bounds.xmin, EPSILON);
        assertEquals(0.5, bounds.xmax, EPSILON);
        assertEquals(0.0, bounds.ymin, EPSILON);
        assertEquals(0.5, bounds.ymax, EPSILON);
        assertEquals(0.0, bounds.zmin, EPSILON);
        assertEquals(0.5, bounds.zmax, EPSILON);
    }

    @Test
    void testBoxToPrimitive_Offset() {
        // 偏移方块：grid=16, box=[8,0,0]→[16,8,8]
        LittleGrid grid = new LittleGrid(16);
        LittleBox box = new LittleBox(8, 0, 0, 16, 8, 8);
        Vector3 blockPos = new Vector3(5, 10, 15);
        
        Primitive primitive = GeometryConverter.boxToPrimitive(box, grid, blockPos);
        
        var bounds = primitive.bounds();
        assertEquals(5.5, bounds.xmin, EPSILON);
        assertEquals(6.0, bounds.xmax, EPSILON);
        assertEquals(10.0, bounds.ymin, EPSILON);
        assertEquals(10.5, bounds.ymax, EPSILON);
        assertEquals(15.0, bounds.zmin, EPSILON);
        assertEquals(15.5, bounds.zmax, EPSILON);
    }

    @Test
    void testBoxToPrimitive_DifferentGrids() {
        // 测试不同 grid 精度
        Vector3 blockPos = new Vector3(0, 0, 0);
        
        // grid=2
        LittleGrid grid2 = new LittleGrid(2);
        LittleBox box2 = new LittleBox(0, 0, 0, 1, 2, 2);
        Primitive prim2 = GeometryConverter.boxToPrimitive(box2, grid2, blockPos);
        assertEquals(0.0, prim2.bounds().xmin, EPSILON);
        assertEquals(0.5, prim2.bounds().xmax, EPSILON);
        assertEquals(0.0, prim2.bounds().ymin, EPSILON);
        assertEquals(1.0, prim2.bounds().ymax, EPSILON);
        
        // grid=4
        LittleGrid grid4 = new LittleGrid(4);
        LittleBox box4 = new LittleBox(0, 0, 0, 4, 4, 1);
        Primitive prim4 = GeometryConverter.boxToPrimitive(box4, grid4, blockPos);
        assertEquals(0.0, prim4.bounds().xmin, EPSILON);
        assertEquals(1.0, prim4.bounds().xmax, EPSILON);
        assertEquals(0.0, prim4.bounds().zmin, EPSILON);
        assertEquals(0.25, prim4.bounds().zmax, EPSILON);
    }

    @Test
    void testGridToWorld() {
        LittleGrid grid = new LittleGrid(16);
        
        // 0 → 0.0
        assertEquals(10.0, GeometryConverter.gridToWorld(0, grid, 10.0), EPSILON);
        
        // 8 → 0.5
        assertEquals(10.5, GeometryConverter.gridToWorld(8, grid, 10.0), EPSILON);
        
        // 16 → 1.0
        assertEquals(11.0, GeometryConverter.gridToWorld(16, grid, 10.0), EPSILON);
        
        // grid=8 测试
        LittleGrid grid8 = new LittleGrid(8);
        assertEquals(5.5, GeometryConverter.gridToWorld(4, grid8, 5.0), EPSILON);
    }

    @Test
    void testBoxToPrimitive_Boundary() {
        // 边界测试：min=max（理论上不应该出现，但测试健壮性）
        LittleGrid grid = new LittleGrid(16);
        LittleBox box = new LittleBox(8, 8, 8, 8, 8, 8);
        Vector3 blockPos = new Vector3(0, 0, 0);
        
        Primitive primitive = GeometryConverter.boxToPrimitive(box, grid, blockPos);
        
        var bounds = primitive.bounds();
        assertEquals(0.5, bounds.xmin, EPSILON);
        assertEquals(0.5, bounds.xmax, EPSILON);
        assertEquals(0.5, bounds.ymin, EPSILON);
        assertEquals(0.5, bounds.ymax, EPSILON);
        assertEquals(0.5, bounds.zmin, EPSILON);
        assertEquals(0.5, bounds.zmax, EPSILON);
    }
}
