# Chunky LittleTiles Plugin - Installation Guide

## Plugin Installation

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

## Next Steps

Once the plugin loads successfully, proceed to implement:
- BlockProvider (C2)
- NBT Parser (C3)
- Geometry conversion (C4)
- Entity rendering (C5)
- Rendering verification (C6)
