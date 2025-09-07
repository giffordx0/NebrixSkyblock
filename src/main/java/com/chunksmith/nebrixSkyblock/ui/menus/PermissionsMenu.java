package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PermissionsMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final Menu parent;

    public PermissionsMenu(NebrixSkyblock plugin, Menu parent) {
        super(plugin.getConfig().getString("gui.title-perms","§8Nebrix • Permissions"), 54);
        this.plugin = plugin; this.parent = parent;
    }

    @Override
    public void draw(Player viewer) {
        inv.clear();
        set(8, new ItemBuilder(Material.ARROW).name(plugin.getConfig().getString("gui.back-name","&7« Back")).build());
        UUID owner = plugin.islands().islandOwnerOf(viewer.getUniqueId());
        Island is = plugin.islands().getIsland(owner);

        int slot = 9;
        for (UUID m : is.members()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(m);
            Island.Role role = is.roles().getOrDefault(m, Island.Role.BUILDER);
            inv.setItem(slot++, new ItemBuilder(Material.PLAYER_HEAD).name("&f"+op.getName())
                    .lore(List.of("&7Role: &a"+role.name(), "", "&eClick to toggle BUILDER/VISITOR")).build());
        }
    }

    @Override
    public void onClick(Player p, int slot, ItemStack clicked, ClickType type) {
        if (slot == 8) { parent.open(p); return; }
        if (clicked == null || clicked.getItemMeta() == null) return;
        String name = clicked.getItemMeta().getDisplayName().replace("§f","");
        UUID owner = plugin.islands().islandOwnerOf(p.getUniqueId());
        Island is = plugin.islands().getIsland(owner);

        for (UUID m : is.members()) {
            if (Objects.equals(Bukkit.getOfflinePlayer(m).getName(), name)) {
                Island.Role r = is.roles().getOrDefault(m, Island.Role.BUILDER);
                is.roles().put(m, r == Island.Role.BUILDER ? Island.Role.VISITOR : Island.Role.BUILDER);
                p.sendMessage("§aSet "+name+" to "+is.roles().get(m));
                draw(p); p.updateInventory();
                return;
            }
        }
    }
}
