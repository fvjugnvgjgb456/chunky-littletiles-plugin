package dev.trae.chunky.littletiles;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test to verify plugin structure.
 */
public class LittleTilesPluginTest {
    
    @Test
    public void testPluginInstantiation() {
        LittleTilesPlugin plugin = new LittleTilesPlugin();
        assertNotNull(plugin, "Plugin should be instantiable");
    }
}
