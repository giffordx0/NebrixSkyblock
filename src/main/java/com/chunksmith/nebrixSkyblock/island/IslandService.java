package com.chunksmith.nebrixSkyblock.island;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.Point;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Manages islands and allocation. */
public class IslandService {
    private final NebrixSkyblock plugin;
    private final IslandGrid grid = new IslandGrid();
    private final Map<UUID, Island> islands = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerToIsland = new ConcurrentHashMap<>();

    public IslandService(NebrixSkyblock plugin) {
        this.plugin = plugin;
    }

    public Island createIsland(UUID owner) {
        return createIsland(owner, true);
    }

    public Island createIsland(UUID owner, boolean generateStructure) {
        // Check if player already has an island
        if (hasIsland(owner)) {
            throw new IllegalStateException("Player already owns an island");
        }

        World world = plugin.worlds().overworld();
        if (world == null) {
            throw new IllegalStateException("Overworld not available");
        }

        Point p = grid.next();
        int spacing = plugin.getConfig().getInt("islands.spacing", 300);
        int baseY = plugin.getConfig().getInt("islands.base-y", 96);
        Location center = new Location(world, p.x * spacing + 0.5, baseY, p.y * spacing + 0.5);

        Island island = new Island(UUID.randomUUID(), owner, center);
        islands.put(island.id(), island);
        playerToIsland.put(owner, island.id());

        // Save to storage
        plugin.storage().saveIsland(island);
        plugin.storage().savePlayerIslandMapping(owner, island.id());

        if (generateStructure) {
            generateStarterIsland(island);
        }

        return island;
    }

    private void generateStarterIsland(Island island) {
        Location center = island.center();

        // Generate starter island async to avoid blocking main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                // Create a simple 3x3 grass platform with a tree
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Location loc = center.clone().add(x, -1, z);
                        loc.getBlock().setType(Material.GRASS_BLOCK);

                        if (x == 0 && z == 0) {
                            // Place a small tree
                            loc.add(0, 1, 0).getBlock().setType(Material.OAK_LOG);
                            loc.add(0, 1, 0).getBlock().setType(Material.OAK_LOG);

                            // Add leaves
                            for (int lx = -1; lx <= 1; lx++) {
                                for (int lz = -1; lz <= 1; lz++) {
                                    for (int ly = 0; ly <= 1; ly++) {
                                        Location leafLoc = loc.clone().add(lx, ly, lz);
                                        if (leafLoc.getBlock().getType() == Material.AIR) {
                                            leafLoc.getBlock().setType(Material.OAK_LEAVES);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Place a chest with starter items
                Location chestLoc = center.clone().add(1, 0, 1);
                chestLoc.getBlock().setType(Material.CHEST);

                plugin.getLogger().info("Generated starter island for " + island.owner());
            }
        }.runTaskLater(plugin, 1L); // Run next tick
    }

    public void loadIsland(Island island) {
        islands.put(island.id(), island);
        playerToIsland.put(island.owner(), island.id());

        // Map all members to this island
        island.members().keySet().forEach(memberId ->
                playerToIsland.put(memberId, island.id()));
    }

    public Island get(UUID id) {
        return islands.get(id);
    }

    public Island getByOwner(UUID owner) {
        UUID islandId = playerToIsland.get(owner);
        return islandId != null ? islands.get(islandId) : null;
    }

    public Island getByPlayer(UUID playerId) {
        UUID islandId = plugin.storage().getPlayerIsland(playerId);
        if (islandId == null) {
            islandId = playerToIsland.get(playerId);
        }
        return islandId != null ? islands.get(islandId) : null;
    }

    public boolean hasIsland(UUID playerId) {
        return getByPlayer(playerId) != null;
    }

    public void teleportToIsland(Player player, Island island) {
        if (island == null) return;

        Location spawn = island.center().clone();
        spawn.setY(spawn.getY() + 1); // Stand on top of the platform

        // Ensure the location is safe
        while (spawn.getBlock().getType() != Material.AIR && spawn.getY() < 256) {
            spawn.add(0, 1, 0);
        }

        player.teleport(spawn);
    }

    public boolean canPlayerAccess(UUID playerId, Island island) {
        if (island == null) return false;
        return island.members().containsKey(playerId);
    }

    public void addMember(Island island, UUID playerId, IslandRole role) {
        island.members().put(playerId, new IslandMember(playerId, role));
        playerToIsland.put(playerId, island.id());
        plugin.storage().saveIsland(island);
        plugin.storage().savePlayerIslandMapping(playerId, island.id());
    }

    public void removeMember(Island island, UUID playerId) {
        if (island.owner().equals(playerId)) {
            throw new IllegalArgumentException("Cannot remove island owner");
        }

        island.members().remove(playerId);
        playerToIsland.remove(playerId);
        plugin.storage().saveIsland(island);
    }

    public boolean isInIslandRadius(Island island, Location location) {
        if (!location.getWorld().equals(island.center().getWorld())) {
            return false;
        }

        int radius = getIslandRadius(island);
        return location.distanceSquared(island.center()) <= radius * radius;
    }

    public int getIslandRadius(Island island) {
        int baseRadius = plugin.getConfig().getInt("islands.base-radius", 50);
        int radiusPerLevel = plugin.getConfig().getInt("islands.radius-per-level", 10);
        return baseRadius + (island.upgrades().radiusLevel() - 1) * radiusPerLevel;
    }

    public Map<UUID, Island> all() {
        return new HashMap<>(islands);
    }

    public Collection<Island> getIslands() {
        return islands.values();
    }

    public void deleteIsland(UUID islandId) {
        Island island = islands.remove(islandId);
        if (island != null) {
            // Remove all member mappings
            island.members().keySet().forEach(playerToIsland::remove);

            // TODO: Implement actual block cleanup
            plugin.getLogger().info("Island " + islandId + " deleted (blocks not cleared yet)");
        }
    }
}