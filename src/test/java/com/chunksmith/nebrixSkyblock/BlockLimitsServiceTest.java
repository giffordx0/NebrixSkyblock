package com.chunksmith.nebrixSkyblock;

import com.chunksmith.nebrixSkyblock.limits.BlockLimitsService;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BlockLimitsServiceTest {
  @Test
  void enforcesLimits() {
    Map<Material, Integer> map = new EnumMap<>(Material.class);
    map.put(Material.HOPPER, 2);
    BlockLimitsService svc = new BlockLimitsService(map);
    UUID island = UUID.randomUUID();
    assertTrue(svc.canPlace(island, Material.HOPPER));
    assertTrue(svc.canPlace(island, Material.HOPPER));
    assertFalse(svc.canPlace(island, Material.HOPPER));
  }
}
