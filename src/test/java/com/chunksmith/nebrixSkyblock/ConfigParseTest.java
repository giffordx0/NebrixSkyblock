package com.chunksmith.nebrixSkyblock;

import java.io.InputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigParseTest {
  @Test
  void parsesGeneratorTable() {
    Yaml yaml = new Yaml();
    InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml");
    Map<String, Object> root = yaml.load(in);
    Map<String, Object> gen = (Map<String, Object>) root.get("generator");
    Map<String, Integer> cobble = (Map<String, Integer>) ((Map<String, Object>) gen.get("cobble")).get("table");
    assertEquals(70, cobble.get("COBBLESTONE"));
    assertEquals(1, cobble.get("EMERALD_ORE"));
  }
}
