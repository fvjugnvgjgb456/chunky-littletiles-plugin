package com.littletiles.chunky;

import dev.trae.chunky.littletiles.DebugConfig;
import dev.trae.chunky.littletiles.LittleBox;
import dev.trae.chunky.littletiles.LittleGrid;
import dev.trae.chunky.littletiles.c7.TransformableBox;
import dev.trae.chunky.littletiles.c7.TransformableBoxConverter;
import se.llbit.chunky.block.Block;
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
     * 自动检测 box 类型：
     * - TransformableBox（7+ field）: 使用 TransformableBoxConverter（支持角点偏移和面翻转）
     * - LittleBox（6 field）: 使用基础 Box 转换（标准 AABB）
     * 
     * 特殊处理：
     * - Fluid 方块（water/lava）: 直接返回原版 Box primitives，保留折射/发光属性
     * 
     * @param box LittleTiles 网格坐标的 box（半开区间 [min, max)）
     * @param grid 网格精度
     * @param blockPos 方块在世界中的位置
     * @param blockstate tile 的 blockstate 名称（如 "minecraft:white_terracotta"）
     * @param color ARGB color (0xAARRGGBB), -1 means no tint
     * @return 带材质的 Chunky Primitives（TexturedTriangles 或 Box）
     */
    public static Collection<Primitive> boxToPrimitives(
        LittleBox box, 
        LittleGrid grid, 
        Vector3 blockPos, 
        String blockstate,
        int color
    ) {
        // Debug: log color parameter received
        if (DebugConfig.VERBOSE_PRIMITIVE_GENERATION) {
            System.out.println(String.format(
                "[GeometryConverter.boxToPrimitives] blockstate=%s, color=0x%08X",
                blockstate, color
            ));
        }
        
        // C10: Resolve vanilla Block instance for material properties
        // Users adjust vanilla blocks in Materials Tab, tiles automatically inherit changes
        se.llbit.chunky.block.Block block = MaterialResolver.resolveBlock(blockstate);
        Texture texture = block.texture;
        
        // Special handling for Fluid blocks: use vanilla Material directly
        // This preserves Water's refractive property and Lava's emissive property
        boolean isFluid = MaterialResolver.isFluidBlock(blockstate);
        Material directMaterial = isFluid ? MaterialResolver.resolve(blockstate) : null;
        
        if (isFluid && DebugConfig.VERBOSE_PRIMITIVE_GENERATION) {
            System.out.println(String.format(
                "[GeometryConverter] Fluid block detected: %s, using directMaterial",
                blockstate
            ));
        }
        
        // Check if this is a TransformableBox (C7 advanced geometry)
        if (box instanceof TransformableBox) {
            TransformableBox tbox = (TransformableBox) box;
            if (DebugConfig.VERBOSE_PRIMITIVE_GENERATION) {
                System.out.println(String.format(
                    "[GeometryConverter] TransformableBox, block=%s",
                    block.name
                ));
            }
            return TransformableBoxConverter.boxToPrimitives(
                tbox, grid, blockPos, texture, color, block, directMaterial);
        }
        
        // Basic AABB: same emitter as TransformableBox, so UVs are block-relative
        if (DebugConfig.VERBOSE_PRIMITIVE_GENERATION) {
            System.out.println(String.format(
                "[GeometryConverter] Basic AABB, block=%s",
                block.name
            ));
        }
        return TransformableBoxConverter.aabbToPrimitives(
            box, grid, blockPos, texture, color, block, directMaterial);
    }
    
    /**
     * 将 LittleBox 转换为带材质的 Chunky Primitives（无 color 参数，默认白色）。
     * 
     * @deprecated 使用 boxToPrimitives(box, grid, blockPos, blockstate, color) 代替
     */
    @Deprecated
    public static Collection<Primitive> boxToPrimitives(
        LittleBox box, 
        LittleGrid grid, 
        Vector3 blockPos, 
        String blockstate
    ) {
        return boxToPrimitives(box, grid, blockPos, blockstate, -1);
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
