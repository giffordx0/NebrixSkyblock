package com.chunksmith.nebrixSkyblock.ui.menus;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.minion.Minion;
import com.chunksmith.nebrixSkyblock.minion.MinionListener;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.UUID;

public class MinionsMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final Menu parent;

    public MinionsMenu(NebrixSkyblock plugin, Menu parent) {
        super("§8Nebrix • Minions", 54);
        this.plugin = plugin; this.parent = parent;
    }

    @Override
    public void draw(Player viewer) {
        inv.clear();
        set(8, new ItemBuilder(Material.ARROW).name(plugin.getConfig().getString("gui.back-name","&7« Back")).build());

        UUID islandOwner = plugin.islands().islandOwnerOf(viewer.getUniqueId());
        int slot = 9;
        for (Minion m : plugin.minions().allOwnedBy(islandOwner)) {
            String fuel = m.fuelSeconds() > 0 ? "§a" + (m.fuelSeconds()/60) + "m" : "§cNo Fuel";
            inv.setItem(slot++, new ItemBuilder(Material.ARMOR_STAND)
                    .name("&dMiner &7(Lv "+m.level()+")")
                    .lore(List.of("&7Fuel: "+fuel, "&7Chest: " + (m.chest()==null? "§cNone":"§aLinked"), "", "&eClick to manage"))
                    .build());
            if (slot >= inv.getSize()) break;
        }

        // Admin give egg (ops only)
        if (viewer.hasPermission("nebrix.admin")) {
            inv.setItem(0, new ItemBuilder(Material.SPAWNER).name("&cGive Minion Egg").lore(List.of("&7Admin only.")).build());
        }
    }

    @Override
    public void onClick(Player p, int slot, ItemStack clicked, ClickType type) {
        if (slot == 8) { parent.open(p); return; }
        if (slot == 0 && p.hasPermission("nebrix.admin")) {
            // give egg
            MinionListener helper = new MinionListener(plugin);
            ItemStack egg = helper.makeEgg();
            p.getInventory().addItem(egg);
            p.sendMessage("§dGiven Minion Egg.");
            return;
        }
        // open the first minion (simple UX: list order)
        UUID owner = plugin.islands().islandOwnerOf(p.getUniqueId());
        int index = slot - 9;
        if (index < 0) return;
        int i = 0;
        for (Minion m : plugin.minions().allOwnedBy(owner)) {
            if (i++ == index) { new com.chunksmith.nebrixSkyblock.ui.menus.MinionMenu(plugin, m).open(p); return; }
        }
    }
}
