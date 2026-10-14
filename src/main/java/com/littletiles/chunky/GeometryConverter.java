package com.littletiles.chunky;

import dev.trae.chunky.littletiles.LittleBox;
import dev.trae.chunky.littletiles.LittleGrid;
import se.llbit.chunky.resources.Texture;
import se.llbit.chunky.world.Material;
import se.llbit.math.Vector3;
import se.llbit.math.Vector4;
import se.llbit.math.primitive.Box;
import se.llbit.math.primitive.Primitive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 将 LittleTiles 网格坐标转换为 Chunky 世界坐标的几何体。
 * 
 * 坐标转换公式（基于 LT-GRID-COORD.md）：
 *   worldCoord = blockPos + (gridCoord / grid.count)
 */
public class GeometryConverter {

    /**
     * 将 LittleBox 转换为带材质的 Chunky Primitives（TexturedTriangles）。
     * 
     * @param box LittleTiles 网格坐标的 box（半开区间 [min, max)）
     * @param grid 网格精度
     * @param blockPos 方块在世界中的位置
     * @param blockstate tile 的 blockstate 名称（如 "minecraft:white_terracotta"）
     * @return 带材质的 Chunky Primitives（TexturedTriangles）
     */
    public static Collection<Primitive> boxToPrimitives(
        LittleBox box, 
        LittleGrid grid, 
        Vector3 blockPos, 
        String blockstate
    ) {
        // 计算世界坐标的边界
        double xmin = blockPos.x + (box.minX / (double) grid.count);
        double ymin = blockPos.y + (box.minY / (double) grid.count);
        double zmin = blockPos.z + (box.minZ / (double) grid.count);
        
        double xmax = blockPos.x + (box.maxX / (double) grid.count);
        double ymax = blockPos.y + (box.maxY / (double) grid.count);
        double zmax = blockPos.z + (box.maxZ / (double) grid.count);
        
        // 创建 Box
        Box boxGeometry = new Box(xmin, xmax, ymin, ymax, zmin, zmax);
        
        // 查询 blockstate 对应的 Material
        Material material = MaterialResolver.resolve(blockstate);
        Texture texture = material.texture;
        
        // UV 坐标（使用全纹理，Box 的 addXXXFaces 方法期望 Vector4(u0, u1, v0, v1)）
        Vector4 uv = new Vector4(0, 1, 0, 1);
        
        // 将 Box 分解为带材质的 TexturedTriangles
        List<Primitive> primitives = new ArrayList<>();
        boxGeometry.addFrontFaces(primitives, texture, uv);
        boxGeometry.addBackFaces(primitives, texture, uv);
        boxGeometry.addLeftFaces(primitives, texture, uv);
        boxGeometry.addRightFaces(primitives, texture, uv);
        boxGeometry.addTopFaces(primitives, texture, uv);
        boxGeometry.addBottomFaces(primitives, texture, uv);
        
        return primitives;
    }
    
    /**
     * 将 LittleBox 转换为简单的 Box Primitive（无材质，用于测试）。
     * 
     * @deprecated 使用 boxToPrimitives() 获取带材质的 primitives
     */
    @Deprecated
    public static Primitive boxToPrimitive(LittleBox box, LittleGrid grid, Vector3 blockPos) {
        double xmin = blockPos.x + (box.minX / (double) grid.count);
        double ymin = blockPos.y + (box.minY / (double) grid.count);
        double zmin = blockPos.z + (box.minZ / (double) grid.count);
        
        double xmax = blockPos.x + (box.maxX / (double) grid.count);
        double ymax = blockPos.y + (box.maxY / (double) grid.count);
        double zmax = blockPos.z + (box.maxZ / (double) grid.count);
        
        return new Box(xmin, xmax, ymin, ymax, zmin, zmax);
    }
    
    /**
     * 将单个网格坐标转换为世界坐标。
     * 
     * @param gridCoord 网格坐标（0 到 grid.count）
     * @param grid 网格精度
     * @param blockCoord 方块坐标
     * @return 世界坐标
     */
    public static double gridToWorld(int gridCoord, LittleGrid grid, double blockCoord) {
        return blockCoord + (gridCoord / (double) grid.count);
    }
}
