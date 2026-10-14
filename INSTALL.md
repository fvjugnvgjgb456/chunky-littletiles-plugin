# 安装指南

**警告**：这是 pre-alpha 版本插件，功能非常有限，不适合生产使用。请先阅读 [README.md](README.md) 了解当前限制。

## 前置要求

- Chunky 2.5.0-DEV（其他版本未经测试）
- Minecraft 1.21.1 存档
- LittleTiles 1.6.0-pre234-BETA（存档中安装的模组版本）

## 插件安装

### Step 1: Build the Plugin

```bash
cd f:\TRAE_DEV\chunky-littletiles-plugin
.\gradlew.bat build
```

The plugin JAR will be generated at:
```
build/libs/chunky-littletiles-plugin-0.1.0-SNAPSHOT.jar
```

### Step 2: Locate Chunky Plugin Directory

Chunky's plugin directory is typically located at:

**Windows:**
```
%USERPROFILE%\.chunky\plugins\
```

**Linux/Mac:**
```
~/.chunky/plugins/
```

If the `plugins` directory doesn't exist, create it manually.

### Step 3: Copy Plugin JAR

Copy the built plugin JAR to Chunky's plugin directory:

```bash
# Windows
copy build\libs\chunky-littletiles-plugin-0.1.0-SNAPSHOT.jar %USERPROFILE%\.chunky\plugins\

# Linux/Mac
cp build/libs/chunky-littletiles-plugin-0.1.0-SNAPSHOT.jar ~/.chunky/plugins/
```

### Step 4: Launch Chunky

Start Chunky. The plugin should be automatically loaded.

### Step 5: Verify Plugin Loading

Check Chunky's console/log output for a message like:

```
LittleTiles plugin loaded
```

If you see this message, the plugin has been successfully loaded!

## Troubleshooting

### Plugin Not Loading

1. **Check plugin.json**: Ensure `plugin.json` exists in the JAR root:
   ```bash
   jar tf build\libs\chunky-littletiles-plugin-0.1.0-SNAPSHOT.jar | findstr plugin.json
   ```

2. **Check Chunky version**: Ensure you're running Chunky 2.5.0 or compatible version.

3. **Check logs**: Look for error messages in Chunky's console output.

4. **Verify JAR structure**: The main class should be at:
   ```
   dev/trae/chunky/littletiles/LittleTilesPlugin.class
   ```

### Version Mismatch

If you see a version warning, update the `targetVersion` in `src/main/resources/plugin.json` to match your Chunky version.

## Development Workflow

For rapid development iteration:

1. Make code changes
2. Rebuild: `.\gradlew.bat build`
3. Copy JAR to plugins directory
4. Restart Chunky

## 使用

1. 在 Chunky 中加载包含 LittleTiles 建筑的存档
2. 选择包含 LT 方块的区块
3. 加载场景（等待 Task 1/6 - 6/6 完成）
4. LT 方块会自动渲染

**预期行为**：
- 基础立方体 tile 会正常显示纹理
- 斜面、嵌套结构会被跳过
- 模组方块显示为紫黑色问号方块

**性能提示**：
- 大规模场景（> 1000 区块）BVH 构建可能需要数分钟
- 建议先测试小范围区块

## 调试

**日志文件位置**：

Windows:
```
%USERPROFILE%\.chunky\plugins\littletiles-plugin.log
```

Linux/Mac:
```
~/.chunky/plugins/littletiles-plugin.log
```

**查看性能统计**：

日志文件末尾会输出性能报告：
```
=== LittleTiles Plugin Performance Report ===
Total Entities:    ...
Total Tiles:       ...
Total Primitives:  ...
```

**启用详细日志**：

编辑 `DebugConfig.java` 并重新编译插件（见 [DEVELOPMENT.md](DEVELOPMENT.md)）。

## 已知问题

- 日志大小可能很大（关闭详细日志后正常）
- BVH 构建时间取决于场景复杂度
- 部分 tile 格式不支持会被跳过

详见 [README.md#当前限制](README.md#⚠️-当前限制（pre-alpha）)。
