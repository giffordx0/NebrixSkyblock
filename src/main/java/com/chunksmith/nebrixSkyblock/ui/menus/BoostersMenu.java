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

import java.util.List;
import java.util.UUID;

public class BoostersMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final Menu parent;

    public BoostersMenu(NebrixSkyblock plugin, Menu parent) {
        super(plugin.getConfig().getString("gui.title-boosters","§8Nebrix • Boosters"), 27);
        this.plugin = plugin; this.parent = parent;
    }

    @Override
    public void draw(Player viewer) {
        inv.clear();
        set(8, new ItemBuilder(Material.ARROW).name(plugin.getConfig().getString("gui.back-name","&7« Back")).build());
        UUID owner = plugin.islands().islandOwnerOf(viewer.getUniqueId());
        Island is = plugin.islands().getIsland(owner);

        int slot = 10;
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("boosters");
        for (String id : sec.getKeys(false)) {
            double mult = sec.getDouble(id+".multiplier",1.25);
            int secDur  = sec.getInt(id+".seconds",1800);
            long c = sec.getLong(id+".cost.crystals",50);
            long $ = sec.getLong(id+".cost.coins",30000);
            set(slot++, new ItemBuilder(Material.BREWING_STAND).name("&d" + id)
                    .lore(List.of("&7x"+mult+" for "+secDur+"s","&7Cost: &d"+c+" crystals &7/ &6"+$+" coins","", "&eClick to activate")).build());
        }
    }

    @Override
    public void onClick(Player p, int slot, ItemStack clicked, ClickType type) {
        if (slot == 8) { parent.open(p); return; }
        if (clicked == null || clicked.getItemMeta() == null) return;
        String id = clicked.getItemMeta().getDisplayName().replace("§d","");
        UUID owner = plugin.islands().islandOwnerOf(p.getUniqueId());
        Island is = plugin.islands().getIsland(owner);
        long c = plugin.getConfig().getLong("boosters."+id+".cost.crystals",50);
        long $ = plugin.getConfig().getLong("boosters."+id+".cost.coins",30000);
        int secDur = plugin.getConfig().getInt("boosters."+id+".seconds",1800);
        if (!plugin.islands().tryPay(is, c, $)) { p.sendMessage("§cNot enough crystals/coins."); return; }
        long expire = System.currentTimeMillis() + secDur*1000L;
        is.activateBooster(id, expire);
        p.sendMessage("§dBooster "+id+" active for "+secDur+"s.");
    }
}
