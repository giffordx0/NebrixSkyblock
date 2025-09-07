package com.chunksmith.nebrixSkyblock;

import com.chunksmith.nebrixSkyblock.generator.GeneratorService;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GeneratorServiceTest {
  @Test
  void rollsFromWeights() {
    Map<Material, Integer> table = new EnumMap<>(Material.class);
    table.put(Material.COBBLESTONE, 1);
    table.put(Material.DIAMOND_ORE, 0);
    GeneratorService gen = new GeneratorService(table, table, new Random(1));
    // Since DIAMOND_ORE has zero weight, result should always be COBBLESTONE
    for (int i = 0; i < 10; i++) {
      assertEquals(Material.COBBLESTONE, gen.rollCobble(1));
    }
  }
}
