# Chunky LittleTiles Plugin

Chunky plugin for rendering LittleTiles structures.

## Building

```bash
./gradlew build
```

The plugin JAR will be generated at `build/libs/chunky-littletiles-plugin-<version>.jar`.

## Installation

1. Build the plugin
2. Copy the JAR to Chunky's plugin directory
3. Launch Chunky

## Development

### Prerequisites

- JDK 17 or later
- Gradle 8.0+
- Chunky 2.5.0-DEV built locally

### Project Structure

```
chunky-littletiles-plugin/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── dev/trae/chunky/littletiles/
│   │   └── resources/
│   │       └── plugin.json
│   └── test/
│       └── java/
│           └── dev/trae/chunky/littletiles/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

### Testing

```bash
./gradlew test
```

## License

GPL-3.0 (same as Chunky)
