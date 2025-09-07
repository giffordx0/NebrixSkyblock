package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.leaderboard.LeaderboardManager;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TopMenu extends Menu {
    private final NebrixSkyblock plugin;
    private boolean byValue = true;
    private final Menu parent;

    public TopMenu(NebrixSkyblock plugin, Menu parent) {
        super(plugin.getConfig().getString("gui.title-top","§8Nebrix • Top Islands"), 54);
        this.plugin = plugin; this.parent = parent;
    }

    @Override
    public void draw(Player viewer) {
        inv.clear();
        inv.setItem(0, new ItemBuilder(Material.GOLD_BLOCK).name("&6Sort: Value").build());
        inv.setItem(1, new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("&bSort: Level").build());
        inv.setItem(8, new ItemBuilder(Material.ARROW).name(plugin.getConfig().getString("gui.back-name","&7« Back")).build());

        if (byValue) {
            List<Map.Entry<UUID,Long>> list = plugin.leaderboard().topValue(45);
            int slot = 9;
            for (int i = 0; i < list.size() && slot < 54; i++, slot++) {
                var e = list.get(i);
                OfflinePlayer op = Bukkit.getOfflinePlayer(e.getKey());
                inv.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                        .name("&e#" + (i+1) + " &f" + op.getName())
                        .lore(List.of("&7Value: &6" + e.getValue()))
                        .build());
            }
        } else {
            List<Map.Entry<UUID,Integer>> list = plugin.leaderboard().topLevel(45);
            int slot = 9;
            for (int i = 0; i < list.size() && slot < 54; i++, slot++) {
                var e = list.get(i);
                OfflinePlayer op = Bukkit.getOfflinePlayer(e.getKey());
                inv.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                        .name("&e#" + (i+1) + " &f" + op.getName())
                        .lore(List.of("&7Level: &b" + e.getValue()))
                        .build());
            }
        }
    }

    @Override
    public void onClick(Player p, int slot, ItemStack clicked, ClickType type) {
        if (slot == 0) { byValue = true; draw(p); p.updateInventory(); }
        else if (slot == 1) { byValue = false; draw(p); p.updateInventory(); }
        else if (slot == 8) { parent.open(p); }
    }
}
