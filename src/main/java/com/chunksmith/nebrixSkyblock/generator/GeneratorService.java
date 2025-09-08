package com.chunksmith.nebrixSkyblock.generator;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import java.util.EnumMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/** Handles cobble/basalt generator rolls. */
public class GeneratorService {
  private final Random random;
  private final Map<Material, Integer> cobble;
  private final Map<Material, Integer> basalt;
  private final Map<Integer, Map<Material, Double>> tierMult = new TreeMap<>();

  public GeneratorService(Map<Material, Integer> cobble, Map<Material, Integer> basalt, Random random) {
    this.random = random;
    this.cobble = cobble;
    this.basalt = basalt;
  }

  public GeneratorService(NebrixSkyblock plugin) {
    this(plugin, new Random());
  }

  public GeneratorService(NebrixSkyblock plugin, Random random) {
    this(
        loadTable(plugin.getConfig().getConfigurationSection("generator.cobble.table")),
        loadTable(plugin.getConfig().getConfigurationSection("generator.basalt.table")),
        random);
    ConfigurationSection tiers = plugin.getConfig().getConfigurationSection("generator.tiers");
    if (tiers != null) {
      for (String key : tiers.getKeys(false)) {
        ConfigurationSection multSec = tiers.getConfigurationSection(key + ".weight-mult");
        if (multSec == null) continue;
        Map<Material, Double> map = new EnumMap<>(Material.class);
        for (String m : multSec.getKeys(false)) {
          Material mat = Material.matchMaterial(m);
          if (mat != null) map.put(mat, multSec.getDouble(m));
        }
        tierMult.put(Integer.parseInt(key), map);
      }
    }
  }

  private static Map<Material, Integer> loadTable(ConfigurationSection sec) {
    Map<Material, Integer> map = new EnumMap<>(Material.class);
    if (sec != null) {
      for (String key : sec.getKeys(false)) {
        Material mat = Material.matchMaterial(key);
        if (mat != null) {
          map.put(mat, sec.getInt(key));
        }
      }
    }
    return map;
  }

  public Material rollCobble(int tier) {
    return roll(cobble, tier);
  }

  public Material rollBasalt(int tier) {
    return roll(basalt, tier);
  }

  private Material roll(Map<Material, Integer> base, int tier) {
    NavigableMap<Double, Material> dist = new TreeMap<>();
    double total = 0;
    for (Map.Entry<Material, Integer> e : base.entrySet()) {
      double weight = e.getValue();
      Map<Material, Double> mult = tierMult.get(tier);
      if (mult != null && mult.containsKey(e.getKey())) {
        weight *= mult.get(e.getKey());
      }
      if (weight <= 0) continue;
      total += weight;
      dist.put(total, e.getKey());
    }
    double r = random.nextDouble() * total;
    return dist.higherEntry(r).getValue();
  }

  public Map<Material, Integer> cobbleTable() {
    return cobble;
  }
}
