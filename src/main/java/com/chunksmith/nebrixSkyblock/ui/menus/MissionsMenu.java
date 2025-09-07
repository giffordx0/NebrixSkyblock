package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MissionsMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final Menu parent;

    // naive in-memory mission progress: islandOwner -> (missionId -> progress)
    private static final Map<UUID, Map<String,Integer>> progress = new HashMap<>();

    public MissionsMenu(NebrixSkyblock plugin, Menu parent) {
        super(plugin.getConfig().getString("gui.title-missions","§8Nebrix • Missions"), 54);
        this.plugin = plugin; this.parent = parent;
    }

    @Override
    public void draw(Player viewer) {
        inv.clear();
        set(8, new ItemBuilder(Material.ARROW).name(plugin.getConfig().getString("gui.back-name","&7« Back")).build());
        UUID owner = plugin.islands().islandOwnerOf(viewer.getUniqueId());

        int slot = 9;
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("missions");
        for (String id : sec.getKeys(false)) {
            int amt = sec.getInt(id+".amount", 100);
            String mat = sec.getString(id+".material","COBBLESTONE");
            int done = progress.computeIfAbsent(owner,k->new HashMap<>()).getOrDefault(id,0);
            set(slot++, new ItemBuilder(Material.PAPER).name("&b"+id)
                    .lore(List.of("&7Objective: &f"+sec.getString(id+".objective","MINE")+" "+mat,
                            "&7Progress: &f"+done+"/"+amt,
                            "&7Reward: &b"+sec.getInt(id+".reward.xp",100)+" XP &d"+sec.getInt(id+".reward.crystals",10)+" crystals",
                            "", "&eClick to (re)start")).build());
        }
    }

    @Override
    public void onClick(Player p, int slot, ItemStack clicked, ClickType type) {
        if (slot == 8) { parent.open(p); return; }
        if (clicked == null || clicked.getItemMeta() == null) return;
        String id = clicked.getItemMeta().getDisplayName().replace("§b","");
        UUID owner = plugin.islands().islandOwnerOf(p.getUniqueId());
        progress.computeIfAbsent(owner,k->new HashMap<>()).put(id, 0);
        p.sendMessage("§bMission "+id+" started.");
    }

    // call from your block event hooks to advance missions:
    public static void addProgress(UUID owner, String id, int delta) {
        progress.computeIfAbsent(owner,k->new HashMap<>()).merge(id, delta, Integer::sum);
    }
    public static int getProgress(UUID owner, String id) {
        return progress.getOrDefault(owner, Collections.emptyMap()).getOrDefault(id,0);
    }
}
