# Chunky LittleTiles Plugin
**声明** — 本插件开发过程中使用AI协助编写。
**Status: Pre-Alpha** — 功能非常有限，仅支持最基础的立方体渲染，不适合生产使用。

Chunky 插件，用于在静态场景中渲染 Minecraft LittleTiles 模组建筑。

##  当前限制（Pre-Alpha）

### 支持的功能
-  基础立方体 tile（6-field 和 7-field box 格式）
-  材质映射（blockstate → Minecraft 原版纹理）
-  简单方块几何（AABB）

### **不支持**的功能（重要！）
-  **斜面和变形几何**（7+ field box、deformed box、slopes）
-  **嵌套结构**（children 字段）
-  **复杂 tile 类型**（门、梯子、绳索等）
-  **半透明和特殊材质**（玻璃、水、发光方块可能显示不正确）
-  **动画和动态元素**（仅静态渲染）
-  **纹理细节**（UV 映射固定，法线贴图未实现）
-  **性能优化**（大规模场景可能卡顿，BVH 构建时间长）

### 已知问题
- 材质映射只支持原版方块，模组方块会显示为紫黑色问号方块
- Box 长度异常（length ≠ 6/7）的 tile 会被跳过
- 日志系统功能有限（详细日志默认关闭）
- 没有 GUI 配置界面

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

## 开发路线

### 短期目标（下一阶段）
- [ ] 支持 extended box format（7+ fields，斜面几何）
- [ ] 支持 children 嵌套结构
- [ ] 改进材质系统（模组方块支持、UV 映射）
- [ ] 性能优化（BVH 构建、内存占用）

### 长期目标
- [ ] 完整的 LittleTiles 几何支持（所有 tile 类型）
- [ ] 半透明和特殊材质渲染
- [ ] GUI 配置界面（调试选项、材质映射规则）
- [ ] 插件 API 文档和扩展机制

### 不确定是否可行
- 动画支持（LittleTiles 动态门、电梯等）
- 实时预览（Chunky 是离线渲染器）
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
│   ├── dev/trae/chunky/littletiles/   # 插件核心（BlockProvider、Entity）
│   └── com/littletiles/chunky/        # 工具类（NBT 解析、几何转换、材质）
├── src/test/java/                     # 单元测试
├── docs/                              # 开发文档（任务、研究报告）
├── DEVELOPMENT.md                     # 开发者指南
├── INSTALL.md                         # 安装指南
└── README.md                          # 本文件
```

## 许可证

GPL-3.0（与 Chunky 保持一致）

## 致谢

- [Chunky](https://github.com/chunky-dev/chunky) — 渲染引擎
- [LittleTiles](https://github.com/CreativeMD/LittleTiles) — 模组
- [CreativeCore](https://github.com/CreativeMD/CreativeCore) — LittleTiles 依赖库

## 联系

**请先阅读 Issues 中的已知问题再提问。**

如有疑问或 bug 报告，请在 GitHub Issues 中说明：
- Minecraft 版本
- LittleTiles 版本
- Chunky 版本
- 完整错误日志（`~/.chunky/plugins/littletiles-plugin.log`）
