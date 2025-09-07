package com.chunksmith.nebrixSkyblock.gen;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CobbleGenListener implements Listener {
    private final NebrixSkyblock plugin;
    private volatile List<Map.Entry<Material, Integer>> weightedTable = new ArrayList<>();

    public CobbleGenListener(NebrixSkyblock plugin) {
        this.plugin = plugin;
        rebuildTable();
    }

    private void rebuildTable() {
        List<Map.Entry<Material, Integer>> table = new ArrayList<>();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("cobblegen.table");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                try {
                    Material m = Material.valueOf(key.toUpperCase(Locale.ROOT));
                    int w = sec.getInt(key);
                    if (w > 0) table.add(Map.entry(m, w));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        if (table.isEmpty()) table.add(Map.entry(Material.COBBLESTONE, 100));
        weightedTable = table;
    }

    private Material roll(Random rng, List<Map.Entry<Material, Integer>> table) {
        int total = 0;
        for (var e : table) total += e.getValue();
        int r = rng.nextInt(Math.max(1, total)) + 1;
        int acc = 0;
        for (var e : table) {
            acc += e.getValue();
            if (r <= acc) return e.getKey();
        }
        return Material.COBBLESTONE;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent e) {
        Block b = e.getBlock();
        if (b.getWorld() == null || !b.getWorld().getName().equalsIgnoreCase(plugin.overworld())) return;

        switch (e.getNewState().getType()) {
            case COBBLESTONE, STONE -> {
                Island is = plugin.islands().islandAt(b.getLocation());
                if (is == null) return;
                Material out = roll(ThreadLocalRandom.current(), weightedTable);
                e.getNewState().setType(out);
            }
            default -> {}
        }
    }
}
