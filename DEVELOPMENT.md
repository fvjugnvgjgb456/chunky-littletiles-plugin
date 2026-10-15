# 开发者指南

本文档面向想要理解、修改或扩展 Chunky LittleTiles 插件的开发者。

## 前置知识

在开始之前，你需要理解：

1. **Chunky 架构**：
   - 世界加载（Region、Chunk、Block）
   - 场景构建（Octree、BVH）
   - 渲染管线（光线追踪、材质系统）
   - 扩展机制（Plugin、BlockProvider、Entity、Primitive）

2. **LittleTiles 数据格式**：
   - NBT 存储结构（`BlockEntity.content.tiles`）
   - 网格系统（grid、count、pixelLength）
   - Box 坐标表示（6/7/7+ field 格式）
   - Blockstate 映射

3. **Java 开发**：
   - Gradle 构建系统
   - JUnit 5 测试框架
   - 向量数学（AABB、坐标转换）

**推荐阅读**：
- [Chunky 源码](https://github.com/chunky-dev/chunky)（特别是 `chunky-core` 模块）
- [LittleTiles 源码](https://github.com/CreativeMD/LittleTiles)（1.6.0-pre234-BETA 分支）

---

## 架构概览

### 核心流程

```
1. Chunky 加载世界
   ↓
2. LTBlockProvider 拦截 "littletiles:tiles" 方块
   ↓
3. LTTilesBlock.createBlockEntity() 创建 LTTilesEntity
   ↓
4. LTNBTParser 解析 NBT → LittleGrid + List<LTTile>
   ↓
5. LTTilesEntity.primitives() 生成几何
   ├─ GeometryConverter: LittleBox → AABB
   ├─ MaterialResolver: blockstate → Material
   └─ Box.addXXXFaces(): AABB → 12 TexturedTriangles
   ↓
6. Chunky BVH 构建（包含 LT primitives）
   ↓
7. 渲染器光线追踪
```

### 模块划分

| 模块 | 职责 | 关键类 |
|------|------|--------|
| **插件核心** | Chunky 集成点 | `LittleTilesPlugin`, `LTBlockProvider`, `LTTilesBlock`, `LTTilesEntity` |
| **NBT 解析** | 数据提取 | `LTNBTParser`, `LittleGrid`, `LittleBox`, `LTTile` |
| **几何转换** | 坐标变换 | `GeometryConverter` |
| **材质系统** | 纹理映射 | `MaterialResolver` |
| **日志控制** | 调试开关 | `DebugConfig`, `TeeOutputStream` |
| **性能监控** | 统计数据 | `PerformanceMonitor` |

---

## 关键实现细节

### 1. BlockProvider 机制

**文件**：[LTBlockProvider.java](file:chunky-littletiles-plugin/src/main/java/dev/trae/chunky/littletiles/LTBlockProvider.java)

Chunky 通过 `BlockSpec.toBlock()` 遍历 `BlockProvider` 列表查询方块。我们的 `LTBlockProvider` 注册到列表首位，拦截 `littletiles:tiles`。

```java
public Block getBlockByTag(String name, Tag tag) {
  if ("littletiles:tiles".equals(name)) {
    return LTTilesBlock.INSTANCE;  // 返回标记 Block
  }
  return null;  // 让后续 Provider 处理
}
```

**关键点**：
- `LTTilesBlock` 设置 `invisible=true`，不加入 Octree（避免重复渲染）
- `isBlockEntity()` 返回 `true`，触发 Entity 路径
- `createBlockEntity()` 接收完整 NBT 数据

### 2. NBT 解析

**文件**：[LTNBTParser.java](file:chunky-littletiles-plugin/src/main/java/com/littletiles/chunky/LTNBTParser.java)

LittleTiles 1.6.0 NBT 结构：

```
BlockEntity
└── content (CompoundTag)
    ├── grid (IntTag, 可选, 默认 16)
    └── tiles (CompoundTag)
        ├── "blockstate1" → ListTag
        │   └── [color, box[6], ...] or [color, faceCache, box[6], ...]
        └── "blockstate2" → ListTag
            └── ...
```

**已实现的解析**：
- ✅ grid 字段缺省处理（默认 16）
- ✅ 6-field box（基础立方体）
- ✅ 7-field box（faceCache 扩展）
- ✅ blockstate → tile 映射

**未实现的解析**：
- ❌ 7+ field box（斜面、变形几何）
- ❌ children 字段（嵌套结构）
- ❌ 其他 tile 类型（门、梯子、绳索等）

**如何添加新格式支持**：

1. 在 `LTNBTParser.parseTile()` 中添加新的 `box.length()` 分支
2. 定义新的数据结构（如 `DeformedBox extends LittleBox`）
3. 在 `GeometryConverter` 中实现对应的几何转换
4. 编写单元测试验证

### 3. 几何转换

**文件**：[GeometryConverter.java](file:chunky-littletiles-plugin/src/main/java/com/littletiles/chunky/GeometryConverter.java)

**坐标转换公式**：

```java
worldCoord = blockPos + (gridCoord / grid.count)
```

- `blockPos`：方块世界坐标（整数）
- `gridCoord`：LittleBox 网格坐标（整数，0 到 grid.count-1）
- `grid.count`：网格分辨率（2/4/8/16/32 等）

**示例**：
- grid=16，blockPos=(10, 20, 30)，box=[0, 0, 0, 8, 8, 8]
- 转换结果：AABB (10.0, 20.0, 30.0) → (10.5, 20.5, 30.5)

**当前实现**：
- `littleBoxToAABB()`: LittleBox → AABB（包围盒）
- `boxToPrimitives()`: AABB → 12 TexturedTriangles（6 面 × 2 三角形）

**如何添加斜面支持**：

1. 定义 `DeformedBox` 数据结构（包含 8 个顶点）
2. 实现 `deformedBoxToPrimitives()`，根据顶点生成三角形
3. 在 `LTTilesEntity.primitives()` 中根据 tile 类型选择转换方法

### 4. 材质映射

**文件**：[MaterialResolver.java](file:chunky-littletiles-plugin/src/main/java/com/littletiles/chunky/MaterialResolver.java)

**当前策略**：

1. 接收 blockstate（如 `"minecraft:white_terracotta[facing=south]"`）
2. 去除属性（→ `"minecraft:white_terracotta"`）
3. 构造 `BlockSpec`，调用 `toBlock()`
4. Chunky 的 `MinecraftBlockProvider` 查询原版方块
5. 缓存结果（100% 命中率）

**限制**：
- 只支持原版方块（MinecraftBlockProvider 的 switch-case）
- 模组方块会返回 `UnknownBlock`（紫黑色问号方块）
- UV 映射固定，不支持自定义纹理坐标

**如何添加模组方块支持**：

1. 创建 `ModBlockProvider implements BlockProvider`
2. 维护模组 blockstate → Texture 映射表
3. 在 `MaterialResolver.resolve()` 中优先查询模组 Provider
4. 注册到 `BlockSpec.blockProviders` 列表

### 5. 日志系统

**文件**：[DebugConfig.java](file:chunky-littletiles-plugin/src/main/java/dev/trae/chunky/littletiles/DebugConfig.java)

**配置项**：

```java
VERBOSE_NBT_PARSER = false;           // 详细 NBT 解析日志
VERBOSE_ENTITY_CONSTRUCTION = false;  // Entity 创建日志
VERBOSE_PRIMITIVE_GENERATION = false; // Primitive 生成日志
ENABLE_PERFORMANCE_TIMING = true;     // 性能计时
ENABLE_SUMMARY_STATS = false;         // 汇总统计（每个 Entity）
ENABLE_WARNINGS = false;              // Warning 日志和 GUI 弹窗
```

**修改配置**：

1. 编辑 `DebugConfig.java`
2. 重新编译插件（`./gradlew build`）
3. 重启 Chunky

**日志文件位置**：
- Windows: `%USERPROFILE%\.chunky\plugins\littletiles-plugin.log`
- Linux/Mac: `~/.chunky/plugins/littletiles-plugin.log`

---

## 测试

### 运行测试

```bash
./gradlew test
```

### 测试结构

| 测试类 | 覆盖内容 |
|--------|---------|
| `LTNBTParserTest` | NBT 解析（8 个测试用例） |
| `GeometryConverterTest` | 坐标转换（7 个测试用例） |

### 添加新测试

```java
@Test
void testNewFeature() {
  // Arrange
  var input = ...;
  
  // Act
  var result = ...;
  
  // Assert
  assertEquals(expected, result);
}
```

---

## 性能分析

### 当前性能数据（参考）

**测试场景**：884 区块，227K Entity，67 万 tiles

| 指标 | 结果 |
|------|------|
| Primitive 生成速度 | 0.001 ms/tile |
| 总生成时间 | 505 ms（67 万 tiles） |
| MaterialResolver 缓存命中率 | 100.0% |
| BVH 构建时间 | 46.4 秒（807 万 primitives） |

**瓶颈**：
- BVH 构建（Chunky 核心，不在插件控制范围）
- 大规模场景内存占用（每 tile 生成 12 primitives）

### 性能监控

**文件**：[PerformanceMonitor.java](file:chunky-littletiles-plugin/src/main/java/com/littletiles/chunky/PerformanceMonitor.java)

会话结束时输出汇总报告：

```
=== LittleTiles Plugin Performance Report ===
Total Entities:    227648
Total Tiles:       672870
Total Primitives:  8074440 (12.0 primitives/tile)
Total Build Time:  505 ms (0.00 ms/entity, 0.001 ms/tile)
MaterialResolver Cache: size=122, hits=672748, misses=122, hit rate=100.0%
```

---

## 常见开发任务

### 1. 添加新的 Box 格式支持

**文件修改**：
1. [LTNBTParser.java](file:chunky-littletiles-plugin/src/main/java/com/littletiles/chunky/LTNBTParser.java) — `parseTile()` 方法
2. [GeometryConverter.java](file:chunky-littletiles-plugin/src/main/java/com/littletiles/chunky/GeometryConverter.java) — 新增转换方法
3. [LTTilesEntity.java](file:chunky-littletiles-plugin/src/main/java/dev/trae/chunky/littletiles/LTTilesEntity.java) — 调用新转换方法

**测试**：
- 单元测试（解析、转换）
- 集成测试（Chunky 场景加载）

### 2. 优化材质映射

**当前问题**：
- 模组方块显示为紫黑色
- UV 映射固定

**改进方向**：
- 创建 `CustomTextureProvider`
- 支持资源包纹理
- 实现 UV 坐标计算

### 3. 支持 children 嵌套结构

**文件修改**：
1. [LTTile.java](file:chunky-littletiles-plugin/src/main/java/com/littletiles/chunky/LTTile.java) — 添加 `children` 字段
2. [LTNBTParser.java](file:TRAE_DEV/chunky-littletiles-plugin/src/main/java/com/littletiles/chunky/LTNBTParser.java) — 递归解析 children
3. [LTTilesEntity.java](file:chunky-littletiles-plugin/src/main/java/dev/trae/chunky/littletiles/LTTilesEntity.java) — 遍历 children 生成 primitives

**注意事项**：
- children 可能有多层嵌套
- 需要考虑递归深度限制
- 坐标转换需要累加父级偏移

### 4. 添加 GUI 配置

**技术方案**：
- Chunky 使用 JavaFX 构建 GUI
- 插件可以添加自定义 Tab 到 Scene 设置面板
- 配置存储在 Scene JSON 文件中

**参考**：
- Chunky 源码 `chunky/src/java/se/llbit/chunky/ui/` 目录
- 其他 Chunky 插件（如 `chunky-octree-plugin`）

---

## 调试技巧

### 1. 启用详细日志

编辑 [DebugConfig.java](file:chunky-littletiles-plugin/src/main/java/dev/trae/chunky/littletiles/DebugConfig.java)：

```java
VERBOSE_NBT_PARSER = true;
VERBOSE_ENTITY_CONSTRUCTION = true;
VERBOSE_PRIMITIVE_GENERATION = true;
ENABLE_WARNINGS = true;
```

重新编译并查看日志文件。

### 2. 单步调试

1. 在 IDE 中打开项目（IntelliJ IDEA 推荐）
2. 配置 Remote JVM Debug
3. 启动 Chunky 时添加 JVM 参数：
   ```bash
   java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar ChunkyLauncher.jar
   ```
4. IDE 连接到端口 5005

### 3. NBT 数据检查

使用 [NBT Explorer](https://github.com/jaquadro/NBTExplorer) 手动查看存档中的 LT 方块实体：

1. 打开 `.minecraft/saves/<world>/region/` 目录
2. 找到包含 LT 方块的区块文件
3. 导航到 `Block Entities → littletiles:tiles`
4. 检查 `content.tiles` 结构

### 4. 渲染验证

1. 加载包含 LT 方块的场景
2. 检查 BVH 构建统计（depth、primitives 数量）
3. 渲染单帧（SPP=1）快速查看结果
4. 查看性能报告确认 tile 数量

---

## 代码规范

### 命名约定

- **类名**：PascalCase（`LTNBTParser`）
- **方法名**：camelCase（`parseTile`）
- **常量**：UPPER_SNAKE_CASE（`ENABLE_WARNINGS`）
- **包名**：全小写（`com.littletiles.chunky`）

### 代码风格

- **缩进**：2 空格
- **行宽**：120 字符
- **注释**：重要逻辑添加注释，简单代码不需要

### 提交信息

```
类型: 简短描述

详细说明（可选）

相关 Issue: #123
```

**类型**：
- `feat`: 新功能
- `fix`: Bug 修复
- `refactor`: 重构
- `test`: 测试
- `docs`: 文档
- `chore`: 构建/工具

---

## 技术债务

### 当前已知的设计问题

1. **每 tile 生成 12 primitives**
   - 优点：实现简单，纹理映射准确
   - 缺点：内存占用高，BVH 构建慢
   - 改进方向：共面合并、LOD（远处简化几何）

2. **Material 查询没有异步缓存**
   - 优点：100% 缓存命中率
   - 缺点：首次查询需要遍历 BlockProvider 列表
   - 改进方向：预加载常用材质

3. **日志系统绕过 Chunky**
   - 优点：可靠写入文件
   - 缺点：与 Chunky 日志系统割裂
   - 改进方向：修改 Chunky 核心或使用独立日志库（SLF4J）

4. **没有错误恢复机制**
   - 优点：失败快速
   - 缺点：单个损坏的 tile 会导致整个 Entity 解析失败
   - 改进方向：跳过损坏数据，记录错误但继续处理

---

### 外部资源
- [Chunky 文档](https://chunky-dev.github.io/docs/)
- [Chunky 源码](https://github.com/chunky-dev/chunky)
- [LittleTiles Wiki](https://github.com/CreativeMD/LittleTiles/wiki)
- [NBT 格式规范](https://minecraft.wiki/w/NBT_format)

---

## 获取帮助

1. **查看已有文档**：README、INSTALL、本文件
2. **搜索 Issues**：检查是否已有人遇到相同问题
3. **查看源码注释**：关键类和方法都有详细注释
4. **提 Issue**：提供完整错误日志和复现步骤

**不要**：
- 在没有阅读文档的情况下提问
- 发送不完整的错误信息
- 期望立即得到回复（这是个人项目）
