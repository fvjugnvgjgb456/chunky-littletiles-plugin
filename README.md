# Chunky LittleTiles Plugin

**Status: Pre-Alpha** — 功能非常有限，仅供测试和开发使用。

Chunky 插件，用于在静态场景中渲染 Minecraft LittleTiles 模组建筑。

## ⚠️ 当前限制（Pre-Alpha v0.1.1.1）

### 支持的功能
- ✅ 基础立方体 tile（6-field 和 7-field box 格式）
- ✅ 变形 box（TransformableBox，8 角点独立偏移）
- ✅ children 嵌套结构（门窗等 LittleTiles 结构）
- ✅ tile color 染色（自定义颜色 + 生物群系染色）
- ✅ 材质映射（blockstate → Minecraft 原版纹理）
- ✅ 斜面几何（斜坡、楔形、角落、柱子等）

### **不支持**的功能（重要！）
- ⚠️ **有色 tiles 的 Materials Tab 属性同步**（使用颜色染色的 tiles 不会同步 Materials Tab 调整，已知限制）
- ❌ **blockstate 属性**（facing/waterlogged 等）
- ❌ **流动 Fluid**（`*_flowing[facing=...]`）
- ❌ **模组方块专有纹理加载**（LT 添加的16种染色方块使用了非原版纹理，这超出了plugin的实现范围，故不会有这一功能，将来也不会有。）

### 已知问题
- 大规模场景 BVH 构建时间长（优化空间有限）
- 没有 GUI 配置界面（需要手动编辑 DebugConfig.java，会在后续alpha版本优化。）

## 版本兼容性

**开发和测试版本**：
- Minecraft 1.21.1
- LittleTiles 1.6.0-pre234-BETA (commit 2dd8a8256)
- Chunky 2.5.0-DEV (commit 527cb4a2)

**其他版本未经测试**，可能无法正常工作。

## 构建

### 前置要求

- JDK 17 或更高版本
- Gradle 8.0+
- Chunky 2.5.0-DEV 已构建（需要 `chunky-core.jar`）

### 构建步骤

```bash
cd chunky-littletiles-plugin
./gradlew build
```

插件 JAR 位置：`build/libs/chunky-littletiles-plugin-0.1.0-SNAPSHOT.jar`

## 安装

1. 将插件 JAR 复制到 Chunky 插件目录：
   - Windows: `%USERPROFILE%\.chunky\plugins\`
   - Linux/Mac: `~/.chunky/plugins/`

2. 启动 Chunky

3. 检查日志确认插件加载：
   ```
   LittleTiles plugin loaded
   ```

完整安装指南见 [INSTALL.md](INSTALL.md)。

## 使用

1. 在 Chunky 中加载包含 LittleTiles 建筑的 Minecraft 存档
2. 选择包含 LT 方块的区块
3. 加载场景（Task 1/6 - 6/6）
4. LT 方块会自动转换为 Chunky Primitive 并渲染

**预期性能**（参考）：
- 67 万 tiles：生成 807 万 primitives，耗时 0.5 秒
- BVH 构建：46 秒（取决于场景复杂度）

**注意事项**：
- 树叶和草方块会自动应用默认绿色染色
- 自定义染色的方块会正确显示颜色
- 门窗等嵌套结构会正常渲染

## 开发路线

### 已完成任务（C1-C8）
- [x] **C1-C6**: 基础立方体渲染（BlockProvider、NBT 解析、几何转换、材质映射、性能优化）
- [x] **C7**: 高级几何支持（TransformableBox、8 角点偏移、斜面裁剪、UV 映射）
- [x] **C8**: Children 嵌套结构 & Color 染色系统（递归解析、TintedTextureMaterial、生物群系染色）

### 当前状态

**版本**: 0.1.0-SNAPSHOT  
**最后更新**: 2026-10-19 (C10 完成)  
**开发阶段**: 稳定维护

### 已完成阶段
- [x] **C1-C6**: 基础立方体渲染
- [x] **C7**: 高级几何支持（TransformableBox）
- [x] **C8**: Children 嵌套结构 & Color 染色系统
- [x] **C9**: Fluid 方块支持（水/岩浆/白岩浆）
- [x] **C10**: Chunky Materials 功能集成（无色 tiles 支持）

### 下一阶段（C11+）
- [ ] GUI 配置界面
- [ ] BVH性能优化，（可能的）GPU调用支持
- [ ] 插件 API 文档

### 不确定是否可行
- 与其他模组的兼容性

## 贡献

这是一个非常早期的项目。如果你想贡献：

1. **先开 Issue 讨论**功能或修复方案
2. 阅读 [DEVELOPMENT.md](DEVELOPMENT.md) 了解代码架构
3. 遵循现有代码风格
4. 为新功能添加单元测试

**不接受的 PR**：
- 超出当前架构能力的大规模重构
- 未经讨论的新功能
- 没有测试的代码

## 项目结构

```
chunky-littletiles-plugin/
├── src/main/java/
│   ├── dev/trae/chunky/littletiles/         # 插件核心
│   │   ├── LittleTilesPlugin.java          # 插件入口
│   │   ├── LTBlockProvider.java            # 拦截 littletiles:tiles
│   │   ├── LTTilesBlock.java               # 标记 Block
│   │   ├── LTTilesEntity.java              # Entity（生成 Primitives）
│   │   ├── LTNBTParser.java                # NBT 解析（grid/tiles/children）
│   │   ├── c7/TransformableBoxConverter.java  # C7 高级几何转换
│   │   ├── c7/TintedTextureMaterial.java      # C8 Color 染色材质
│   │   └── ...
│   └── com/littletiles/chunky/              # 工具类
│       ├── GeometryConverter.java          # 几何转换（LittleBox → AABB）
│       ├── MaterialResolver.java           # 材质映射（blockstate → Material）
│       └── ...
├── src/test/java/                          # 单元测试
├── docs/                                   # 开发文档（任务、研究报告）
├── DEVELOPMENT.md                          # 开发者指南
├── INSTALL.md                              # 安装指南
└── README.md                               # 本文件
```

## 技术亮点

### C7: 高级几何支持
- **TransformableBox 完整支持**：8 个角点独立偏移，支持任意斜面和变形几何
- **几何裁剪算法**：移植 LittleTiles `requestCache()` 算法，将斜面裁剪到 AABB 包围盒内
- **方块内相对 UV**：纹理坐标归一化到方块内，解决纹理拉伸问题
- **面积检测**：自动跳过退化面（面积为 0），避免渲染器异常

### C8: Children & Color 支持
- **Children 递归解析**：支持 StructureParentCollection 嵌套结构，最大深度 10 层
- **TintedTextureMaterial**：重写 `getColor(double u, double v)` 实现 ARGB 乘法混合
- **生物群系染色**：为树叶、草方块等自动应用默认绿色（`0xFF48B518`）

## 许可证

GPL-3.0（与 Chunky 保持一致）

## 致谢

- [Chunky](https://github.com/chunky-dev/chunky) — 渲染引擎
- [LittleTiles](https://github.com/CreativeMD/LittleTiles) — 模组
- [CreativeCore](https://github.com/CreativeMD/CreativeCore) — LittleTiles 依赖库
- [Minecraft-Littletiles-Reader](https://github.com/yourusername/Minecraft-Littletiles-Reader) — C++ 参考实现

## 联系

**请先阅读 Issues 中的已知问题再提问。**

如有疑问或 bug 报告，请在 GitHub Issues 中说明：
- Minecraft 版本
- LittleTiles 版本
- Chunky 版本
- 完整错误日志（`~/.chunky/plugins/littletiles-plugin.log`）
- 场景截图（如果有渲染问题）
