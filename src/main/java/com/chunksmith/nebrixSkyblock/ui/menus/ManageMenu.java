package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ManageMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final Menu parent;

    public ManageMenu(NebrixSkyblock plugin, Menu parent) {
        super(plugin.getConfig().getString("gui.title-manage","§8Nebrix • Manage Island"), 54);
        this.plugin = plugin; this.parent = parent;
    }

    @Override
    public void draw(Player viewer) {
        inv.clear();
        set(10, new ItemBuilder(Material.ANVIL).name("&eUpgrades").lore(List.of("&7Radius & Generator tiers.")).build());
        set(12, new ItemBuilder(Material.BREWING_STAND).name("&dBoosters").lore(List.of("&7Timed island buffs.")).build());
        set(14, new ItemBuilder(Material.PAPER).name("&bMissions").lore(List.of("&7Complete objectives for XP & crystals.")).build());
        set(16, new ItemBuilder(Material.ENDER_CHEST).name("&6Bank").lore(List.of("&7Deposit/withdraw island coins.")).build());
        set(28, new ItemBuilder(Material.REPEATER).name("&aPermissions").lore(List.of("&7Member roles and actions.")).build());
        set(30, new ItemBuilder(Material.DEEPSLATE_COAL_ORE).name("&fGenerators").lore(List.of("&7Cobble/Basalt tier info.")).build());
        set(32, new ItemBuilder(Material.BEACON).name("&9Top Islands").lore(List.of("&7View leaderboards.")).build());
        set(34, new ItemBuilder(Material.ARMOR_STAND).name("&dMinions").lore(List.of("&7Place and manage minions.")).build());
        set(8,  new ItemBuilder(Material.ARROW).name(plugin.getConfig().getString("gui.back-name","&7« Back")).build());
    }

    @Override
    public void onClick(Player p, int slot, ItemStack clicked, ClickType type) {
        switch (slot) {
            case 10 -> new com.chunksmith.nebrixSkyblock.ui.menus.UpgradesMenu(plugin, this).open(p);
            case 12 -> new com.chunksmith.nebrixSkyblock.ui.menus.BoostersMenu(plugin, this).open(p);
            case 14 -> new com.chunksmith.nebrixSkyblock.ui.menus.MissionsMenu(plugin, this).open(p);
            case 16 -> new com.chunksmith.nebrixSkyblock.ui.menus.BankMenu(plugin, this).open(p);
            case 28 -> new com.chunksmith.nebrixSkyblock.ui.menus.PermissionsMenu(plugin, this).open(p);
            case 30 -> new com.chunksmith.nebrixSkyblock.ui.menus.GeneratorsMenu(plugin, this).open(p);
            case 32 -> new com.chunksmith.nebrixSkyblock.ui.menus.TopMenu(plugin, this).open(p);
            case 34 -> new com.chunksmith.nebrixSkyblock.ui.menus.MinionsMenu(plugin, this).open(p);
            case 8  -> parent.open(p);
            default -> {}
        }
    }
}
