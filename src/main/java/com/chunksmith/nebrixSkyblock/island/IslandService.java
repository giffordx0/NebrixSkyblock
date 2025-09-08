package com.chunksmith.nebrixSkyblock.island;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/** Manages islands and allocation. */
public class IslandService {
  private final NebrixSkyblock plugin;
  private final IslandGrid grid = new IslandGrid();
  private final Map<UUID, Island> islands = new HashMap<>();

  public IslandService(NebrixSkyblock plugin) {
    this.plugin = plugin;
  }

  public Island createIsland(UUID owner) {
    World world = plugin.worlds().overworld();
    Point p = grid.next();
    int spacing = plugin.getConfig().getInt("islands.spacing", 300);
    Location center =
        new Location(
            world,
            p.x * spacing + 0.5,
            plugin.getConfig().getInt("islands.base-y", 96),
            p.y * spacing + 0.5);
    Island island = new Island(UUID.randomUUID(), owner, center);
    islands.put(island.id(), island);
    return island;
  }

  public Island get(UUID id) {
    return islands.get(id);
  }

  public Map<UUID, Island> all() {
    return islands;
  }

  public Island byPlayer(UUID uuid) {
    for (Island island : islands.values()) {
      if (island.members().containsKey(uuid)) {
        return island;
      }
    }
    return null;
  }

  public void deleteIsland(UUID id) {
    islands.remove(id);
  }

  public void teleportHome(Player player, Island island) {
    if (island != null) {
      player.teleport(island.center());
    }
  }
}
