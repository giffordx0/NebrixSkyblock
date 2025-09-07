package com.chunksmith.nebrixSkyblock.leaderboard;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;

import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {
    private final NebrixSkyblock plugin;

    public enum Mode { VALUE, LEVEL }

    public LeaderboardManager(NebrixSkyblock plugin) { this.plugin = plugin; }

    public List<Map.Entry<UUID,Long>> topValue(int limit) {
        return plugin.islands().exportAll().stream()
                .sorted((a,b) -> Long.compare(b.value(), a.value()))
                .limit(limit)
                .map(i -> Map.entry(i.ownerId(), i.value()))
                .collect(Collectors.toList());
    }
    public List<Map.Entry<UUID,Integer>> topLevel(int limit) {
        return plugin.islands().exportAll().stream()
                .sorted((a,b) -> Integer.compare(b.level(), a.level()))
                .limit(limit)
                .map(i -> Map.entry(i.ownerId(), i.level()))
                .collect(Collectors.toList());
    }
    public void shutdown() {}
}
