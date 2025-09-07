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

public class GeneratorListener implements Listener {
    private final NebrixSkyblock plugin;

    public GeneratorListener(NebrixSkyblock plugin) { this.plugin = plugin; }

    @EventHandler(ignoreCancelled = true)
    public void onForm(BlockFormEvent e) {
        Block b = e.getBlock();
        String worldName = b.getWorld().getName();
        boolean isOver = worldName.equalsIgnoreCase(plugin.overworld());
        boolean isNether = worldName.equalsIgnoreCase(plugin.nether());

        // Overworld cobble gen
        if (isOver && (e.getNewState().getType() == Material.COBBLESTONE || e.getNewState().getType() == Material.STONE)) {
            handleGen(e, "generators.cobblestone");
            return;
        }
        // Nether basalt gen
        if (isNether && e.getNewState().getType() == Material.BASALT) {
            handleGen(e, "generators.basalt");
        }
    }

    private void handleGen(BlockFormEvent e, String path) {
        Island is = plugin.islands().islandAt(e.getBlock().getLocation());
        if (is == null) return;

        String tier = is.generatorTier();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection(path + "." + tier);
        if (sec == null) return;

        // roll weighted
        int total = 0;
        List<Map.Entry<Material,Integer>> table = new ArrayList<>();
        for (String k : sec.getKeys(false)) {
            try {
                Material m = Material.valueOf(k);
                int w = sec.getInt(k);
                if (w > 0) { table.add(Map.entry(m, w)); total += w; }
            } catch (IllegalArgumentException ignored) {}
        }
        if (table.isEmpty()) return;
        int roll = ThreadLocalRandom.current().nextInt(total);
        int acc = 0;
        for (var e2 : table) {
            acc += e2.getValue();
            if (roll < acc) { e.getNewState().setType(e2.getKey()); return; }
        }
    }
}
