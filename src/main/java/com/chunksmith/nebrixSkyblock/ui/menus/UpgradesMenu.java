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

public class UpgradesMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final Menu parent;

    public UpgradesMenu(NebrixSkyblock plugin, Menu parent) {
        super(plugin.getConfig().getString("gui.title-upgrades","§8Nebrix • Upgrades"), 27);
        this.plugin = plugin; this.parent = parent;
    }

    @Override
    public void draw(Player viewer) {
        inv.clear();
        UUID owner = plugin.islands().islandOwnerOf(viewer.getUniqueId());
        if (owner == null) { viewer.sendMessage("§cNo island."); parent.open(viewer); return; }
        Island is = plugin.islands().getIsland(owner);

        int step = plugin.getConfig().getInt("upgrades.radius.step", 8);
        int max  = plugin.getConfig().getInt("upgrades.radius.max", 160);
        int next = Math.min(is.radius() + step, max);
        long cCost = plugin.getConfig().getLong("upgrades.radius.cost-per-step.crystals", 50);
        long $Cost = plugin.getConfig().getLong("upgrades.radius.cost-per-step.coins", 25000);

        set(10, new ItemBuilder(Material.OAK_FENCE).name("&eRadius: &f" + is.radius())
                .lore(List.of("&7Next: &f" + next, "&7Cost: &d" + cCost + " crystals &7/ &6" + $Cost + " coins",
                        "", "&eClick to upgrade")).build());

        String tier = is.generatorTier();
        String nextTier = nextTier(tier);
        set(12, new ItemBuilder(Material.BLAST_FURNACE).name("&eGenerator Tier: &f" + tier)
                .lore(nextTier == null
                        ? List.of("&7Max tier reached.")
                        : List.of("&7Next: &f" + nextTier,
                        "&7Cost: &d"+ plugin.getConfig().getLong("upgrades.generator.cost-per-tier."+nextTier+".crystals",150)
                                +" crystals &7/ &6"+ plugin.getConfig().getLong("upgrades.generator.cost-per-tier."+nextTier+".coins",100000)+" coins",
                        "", "&eClick to upgrade")).build());

        set(8, new ItemBuilder(Material.ARROW).name(plugin.getConfig().getString("gui.back-name","&7« Back")).build());
    }

    private String nextTier(String current) {
        var tiers = plugin.getConfig().getStringList("upgrades.generator.tiers");
        for (int i = 0; i < tiers.size()-1; i++) if (tiers.get(i).equalsIgnoreCase(current)) return tiers.get(i+1);
        return null;
    }

    @Override
    public void onClick(Player p, int slot, ItemStack clicked, ClickType type) {
        UUID owner = plugin.islands().islandOwnerOf(p.getUniqueId());
        Island is = plugin.islands().getIsland(owner);
        switch (slot) {
            case 10 -> {
                int step = plugin.getConfig().getInt("upgrades.radius.step", 8);
                int max  = plugin.getConfig().getInt("upgrades.radius.max", 160);
                if (is.radius() >= max) { p.sendMessage("§eMax radius."); return; }
                long c = plugin.getConfig().getLong("upgrades.radius.cost-per-step.crystals",50);
                long $ = plugin.getConfig().getLong("upgrades.radius.cost-per-step.coins",25000);
                if (!plugin.islands().tryPay(is, c, $)) { p.sendMessage("§cNot enough crystals/coins."); return; }
                is.setRadius(Math.min(is.radius() + step, max)); p.sendMessage("§aRadius upgraded to " + is.radius()); draw(p);
            }
            case 12 -> {
                String nextTier = nextTier(is.generatorTier());
                if (nextTier == null) { p.sendMessage("§eMax tier."); return; }
                long c = plugin.getConfig().getLong("upgrades.generator.cost-per-tier."+nextTier+".crystals",150);
                long $ = plugin.getConfig().getLong("upgrades.generator.cost-per-tier."+nextTier+".coins",100000);
                if (!plugin.islands().tryPay(is, c, $)) { p.sendMessage("§cNot enough crystals/coins."); return; }
                is.setGeneratorTier(nextTier); p.sendMessage("§aGenerator upgraded to " + nextTier); draw(p);
            }
            case 8 -> parent.open(p);
            default -> {}
        }
    }
}
