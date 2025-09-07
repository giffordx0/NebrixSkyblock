package com.chunksmith.nebrixSkyblock.world;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import org.bukkit.Bukkit;
import org.bukkit.World;

/** Provides access to configured worlds. */
public class WorldService {
  private final NebrixSkyblock plugin;
  private final String overworldName;
  private final String netherName;
  private final String endName;

  public WorldService(NebrixSkyblock plugin) {
    this.plugin = plugin;
    var cfg = plugin.getConfig();
    this.overworldName = cfg.getString("worlds.overworld", "world");
    this.netherName = cfg.getString("worlds.nether", "world_nether");
    this.endName = cfg.getString("worlds.end", "world_the_end");
  }

  public World overworld() {
    World w = Bukkit.getWorld(overworldName);
    if (w == null) {
      plugin.getLogger().warning("Missing overworld " + overworldName);
    }
    return w;
  }

  public World nether() {
    return Bukkit.getWorld(netherName);
  }

  public World end() {
    return Bukkit.getWorld(endName);
  }
}
