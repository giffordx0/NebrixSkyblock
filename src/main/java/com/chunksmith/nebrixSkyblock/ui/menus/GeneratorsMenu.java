package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class GeneratorsMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final Menu parent;

    public GeneratorsMenu(NebrixSkyblock plugin, Menu parent) {
        super(plugin.getConfig().getString("gui.title-generators","§8Nebrix • Generators"), 27);
        this.plugin = plugin; this.parent = parent;
    }

    @Override
    public void draw(Player viewer) {
        inv.clear();
        UUID owner = plugin.islands().islandOwnerOf(viewer.getUniqueId());
        Island is = plugin.islands().getIsland(owner);

        set(11, new ItemBuilder(Material.COBBLESTONE).name("&fCobble Tier: &a"+is.generatorTier())
                .lore(List.of("&7Overworld generator output.")).build());
        set(15, new ItemBuilder(Material.BASALT).name("&fBasalt Tier: &a"+is.generatorTier())
                .lore(List.of("&7Nether generator output.")).build());
        set(8, new ItemBuilder(Material.ARROW).name(plugin.getConfig().getString("gui.back-name","&7« Back")).build());
    }

    @Override
    public void onClick(Player p, int slot, ItemStack clicked, ClickType type) {
        if (slot == 8) parent.open(p);
    }
}
