package com.chunksmith.nebrixSkyblock.value;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/** Calculates island block values. */
public class ValueService {
  private final Map<Material, Integer> values = new EnumMap<>(Material.class);

  public ValueService(NebrixSkyblock plugin) {
    ConfigurationSection sec = plugin.getConfig().getConfigurationSection("value.by-block");
    if (sec != null) {
      for (String key : sec.getKeys(false)) {
        Material mat = Material.matchMaterial(key);
        if (mat != null) values.put(mat, sec.getInt(key));
      }
    }
  }

  public long compute(Map<Material, Integer> counts) {
    long total = 0;
    for (Map.Entry<Material, Integer> e : counts.entrySet()) {
      total += values.getOrDefault(e.getKey(), 0) * e.getValue();
    }
    return total;
  }

  public Map<Material, Integer> values() {
    return values;
  }
}
