package com.chunksmith.nebrixSkyblock.limits;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/** Tracks per-island block limits. */
public class BlockLimitsService {
  private final Map<Material, Integer> limits = new EnumMap<>(Material.class);
  private final Map<UUID, Map<Material, Integer>> usage = new HashMap<>();

  public BlockLimitsService(NebrixSkyblock plugin) {
    ConfigurationSection sec = plugin.getConfig().getConfigurationSection("limits.by-block");
    if (sec != null) {
      for (String key : sec.getKeys(false)) {
        Material mat = Material.matchMaterial(key);
        if (mat != null) limits.put(mat, sec.getInt(key));
      }
    }
  }

  public BlockLimitsService(Map<Material, Integer> limits) {
    this.limits.putAll(limits);
  }

  public boolean canPlace(UUID islandId, Material material) {
    int limit = limits.getOrDefault(material, Integer.MAX_VALUE);
    Map<Material, Integer> map = usage.computeIfAbsent(islandId, k -> new EnumMap<>(Material.class));
    int used = map.getOrDefault(material, 0);
    if (used >= limit) {
      return false;
    }
    map.put(material, used + 1);
    return true;
  }

  public Map<Material, Integer> limits() {
    return limits;
  }
}
