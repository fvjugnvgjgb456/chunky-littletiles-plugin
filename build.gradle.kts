plugins {
    id("java")
}

group = "dev.trae"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    // Chunky core dependency - using composite build
    compileOnly("se.llbit:chunky-core")
    
    // Test dependencies - need Chunky at test runtime
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.25.1")
    testImplementation("se.llbit:chunky-core") // Test needs Chunky classes
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set("chunky-littletiles-plugin")
    archiveVersion.set(project.version.toString())
    
    // plugin.json is automatically included from src/main/resources
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
