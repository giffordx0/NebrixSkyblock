package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.DefaultIslandSchematic;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class IslandAdminMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final UUID ownerId;
    private final Menu parent;

    public IslandAdminMenu(NebrixSkyblock plugin, UUID ownerId, Menu parent) {
        super("§8Nebrix • Admin", 27);
        this.plugin = plugin;
        this.ownerId = ownerId;
        this.parent = parent;
    }

    @Override
    public void draw(Player viewer) {
        inv.clear();
        set(10, new ItemBuilder(Material.ENDER_PEARL).name("&aTP to Island Owner").build());
        set(12, new ItemBuilder(Material.OAK_FENCE).name("&eSet Radius +8")
                .lore(List.of("&7Increase region half-size by 8.")).build());
        set(14, new ItemBuilder(Material.TNT).name("&cRegen Starter Island")
                .lore(List.of("&7Danger! This will place starter blocks at center.")).build());
        set(16, new ItemBuilder(Material.ARROW).name("&7« Back").build());
        set(6, new ItemBuilder(Material.SPAWNER).name("&cGive Minion Egg").build());
    }

    @Override
    public void onClick(Player p, int slot, ItemStack clicked, ClickType type) {
        Island is = plugin.islands().getIsland(ownerId);
        if (is == null) { p.sendMessage("§cIsland not found."); parent.open(p); return; }
        switch (slot) {
            case 10 -> {
                Player owner = Bukkit.getPlayer(ownerId);
                if (owner != null) p.teleport(owner.getLocation());
                else p.teleport(is.center());
            }
            case 12 -> {
                is.setRadius(is.radius() + 8);
                p.sendMessage("§aRadius is now " + is.radius());
            }
            case 6 -> {
                var helper = new com.chunksmith.nebrixSkyblock.minion.MinionListener(plugin);
                p.getInventory().addItem(helper.makeEgg());
                p.sendMessage("§dGiven Minion Egg.");
            }
            case 14 -> {
                DefaultIslandSchematic.place(is.center());
                p.sendMessage("§cStarter island placed at center (dangerous).");
            }
            case 16 -> parent.open(p);
            default -> {}
        }
    }
}