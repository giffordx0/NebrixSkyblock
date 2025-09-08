package com.chunksmith.nebrixSkyblock.storage;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.island.IslandMember;
import com.chunksmith.nebrixSkyblock.island.IslandRole;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/** Handles persistent storage of island data. */
public class StorageService {
    private final NebrixSkyblock plugin;
    private final File dataFolder;
    private final File islandsFile;
    private final File playerDataFile;
    private final Gson gson;
    private final Map<UUID, UUID> playerToIsland = new ConcurrentHashMap<>();

    public StorageService(NebrixSkyblock plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        this.islandsFile = new File(dataFolder, "islands.json");
        this.playerDataFile = new File(dataFolder, "players.json");

        // Setup Gson with custom serializers
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Location.class, (JsonSerializer<Location>) (src, typeOfSrc, context) -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("world", src.getWorld().getName());
                    map.put("x", src.getX());
                    map.put("y", src.getY());
                    map.put("z", src.getZ());
                    map.put("yaw", src.getYaw());
                    map.put("pitch", src.getPitch());
                    return context.serialize(map);
                })
                .registerTypeAdapter(Location.class, (JsonDeserializer<Location>) (json, typeOfT, context) -> {
                    Map<String, Object> map = context.deserialize(json, Map.class);
                    String worldName = (String) map.get("world");
                    World world = plugin.getServer().getWorld(worldName);
                    if (world == null) {
                        plugin.getLogger().warning("Unknown world: " + worldName);
                        return null;
                    }
                    double x = ((Number) map.get("x")).doubleValue();
                    double y = ((Number) map.get("y")).doubleValue();
                    double z = ((Number) map.get("z")).doubleValue();
                    float yaw = map.containsKey("yaw") ? ((Number) map.get("yaw")).floatValue() : 0f;
                    float pitch = map.containsKey("pitch") ? ((Number) map.get("pitch")).floatValue() : 0f;
                    return new Location(world, x, y, z, yaw, pitch);
                })
                .registerTypeAdapter(UUID.class, (JsonSerializer<UUID>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.toString()))
                .registerTypeAdapter(UUID.class, (JsonDeserializer<UUID>) (json, typeOfT, context) ->
                        UUID.fromString(json.getAsString()))
                .create();

        createDirectories();
        loadData();
    }

    private void createDirectories() {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().severe("Could not create data directory!");
        }
    }

    private void loadData() {
        loadPlayerData();
        loadIslands();
    }

    private void loadPlayerData() {
        if (!playerDataFile.exists()) return;

        try {
            String json = Files.readString(playerDataFile.toPath());
            Map<String, String> data = gson.fromJson(json, Map.class);
            if (data != null) {
                data.forEach((playerUuid, islandUuid) -> {
                    try {
                        playerToIsland.put(UUID.fromString(playerUuid), UUID.fromString(islandUuid));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID in player data: " + playerUuid + " -> " + islandUuid);
                    }
                });
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player data", e);
        }
    }

    private void loadIslands() {
        if (!islandsFile.exists()) return;

        try {
            String json = Files.readString(islandsFile.toPath());
            Map<String, IslandData> data = gson.fromJson(json, Map.class);
            if (data != null) {
                for (Map.Entry<String, IslandData> entry : data.entrySet()) {
                    try {
                        UUID islandId = UUID.fromString(entry.getKey());
                        IslandData islandData = entry.getValue();
                        Island island = deserializeIsland(islandId, islandData);
                        plugin.islands().loadIsland(island);
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Failed to load island: " + entry.getKey(), e);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load islands", e);
        }
    }

    public void saveIsland(Island island) {
        try {
            Map<String, IslandData> data = loadExistingIslands();
            data.put(island.id().toString(), serializeIsland(island));

            String json = gson.toJson(data);
            Files.writeString(islandsFile.toPath(), json);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save island: " + island.id(), e);
        }
    }

    public void savePlayerIslandMapping(UUID playerId, UUID islandId) {
        playerToIsland.put(playerId, islandId);
        savePlayerData();
    }

    public UUID getPlayerIsland(UUID playerId) {
        return playerToIsland.get(playerId);
    }

    private void savePlayerData() {
        try {
            Map<String, String> data = new HashMap<>();
            playerToIsland.forEach((player, island) ->
                    data.put(player.toString(), island.toString()));

            String json = gson.toJson(data);
            Files.writeString(playerDataFile.toPath(), json);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data", e);
        }
    }

    private Map<String, IslandData> loadExistingIslands() {
        if (!islandsFile.exists()) {
            return new HashMap<>();
        }

        try {
            String json = Files.readString(islandsFile.toPath());
            Map<String, IslandData> data = gson.fromJson(json, Map.class);
            return data != null ? data : new HashMap<>();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load existing islands", e);
            return new HashMap<>();
        }
    }

    private IslandData serializeIsland(Island island) {
        IslandData data = new IslandData();
        data.owner = island.owner();
        data.center = island.center();
        data.pvp = island.settings().pvp();
        data.coins = island.bank().coins();
        data.crystals = island.bank().crystals();
        data.radiusLevel = island.upgrades().radiusLevel();
        data.memberLevel = island.upgrades().memberLevel();
        data.generatorTier = island.upgrades().generatorTier();

        data.members = new HashMap<>();
        island.members().forEach((uuid, member) ->
                data.members.put(uuid.toString(), member.role().name()));

        return data;
    }

    private Island deserializeIsland(UUID id, IslandData data) {
        Island island = new Island(id, data.owner, data.center);

        // Load settings
        island.settings().setPvp(data.pvp);

        // Load bank
        island.bank().depositCoins(data.coins);
        island.bank().depositCrystals(data.crystals);

        // Load upgrades
        island.upgrades().setRadiusLevel(data.radiusLevel);
        island.upgrades().setMemberLevel(data.memberLevel);
        island.upgrades().setGeneratorTier(data.generatorTier);

        // Load members
        island.members().clear();
        if (data.members != null) {
            data.members.forEach((uuidStr, roleStr) -> {
                try {
                    UUID memberUuid = UUID.fromString(uuidStr);
                    IslandRole role = IslandRole.valueOf(roleStr);
                    island.members().put(memberUuid, new IslandMember(memberUuid, role));
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid member data: " + uuidStr + " -> " + roleStr);
                }
            });
        }

        return island;
    }

    public void flush() {
        savePlayerData();
        // Save all islands
        plugin.islands().all().values().forEach(this::saveIsland);
        plugin.getLogger().info("All data flushed to disk");
    }

    private static class IslandData {
        UUID owner;
        Location center;
        boolean pvp;
        long coins;
        long crystals;
        int radiusLevel = 1;
        int memberLevel = 1;
        int generatorTier = 1;
        Map<String, String> members;
    }
}
