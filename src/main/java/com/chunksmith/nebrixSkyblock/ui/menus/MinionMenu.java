package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.minion.Minion;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MinionMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final Minion m;

    public MinionMenu(NebrixSkyblock plugin, Minion m) {
        super("§8Nebrix • Minion", 27);
        this.plugin = plugin; this.m = m;
    }

    @Override
    public void draw(Player viewer) {
        inv.clear();
        set(10, new ItemBuilder(Material.COAL).name("&aAdd Fuel")
                .lore(List.of("&7Feed fuel from your hand.", "&7Accepts: COAL, COAL_BLOCK, BLAZE_ROD, LAVA_BUCKET")).build());
        set(12, new ItemBuilder(Material.CHEST).name("&eLink Chest")
                .lore(List.of("&7Click a chest to link.", m.chest()==null? "&cNot linked":"&aLinked")).build());
        set(14, new ItemBuilder(Material.ANVIL).name("&bUpgrade")
                .lore(List.of("&7Level: &f"+m.level(), "&7Cost: &d"
                        + plugin.getConfig().getLong("minions.types.miner.upgrade-cost.crystals",25)
                        + " crystals &7/ &6"
                        + plugin.getConfig().getLong("minions.types.miner.upgrade-cost.coins",15000) + " coins")).build());
        set(16, new ItemBuilder(Material.BARRIER).name("&cPick Up").lore(List.of("&7Return minion as egg.")).build());
    }

    @Override
    public void onClick(Player p, int slot, ItemStack clicked, ClickType type) {
        switch (slot) {
            case 10 -> {
                ItemStack in = p.getInventory().getItemInMainHand();
                if (in == null || in.getType() == Material.AIR) { p.sendMessage("§cHold a fuel item."); return; }
                boolean ok = plugin.minions().tryFeedFuel(m, in.getType());
                if (!ok) { p.sendMessage("§cThat item is not a valid fuel."); return; }
                // consume one (except lava bucket: give back empty bucket)
                if (in.getType() == Material.LAVA_BUCKET) {
                    in.setType(Material.BUCKET);
                } else {
                    in.setAmount(in.getAmount()-1);
                }
                p.sendMessage(ChatColor.LIGHT_PURPLE + "Fueled. Remaining: " + m.fuelSeconds() + "s");
                draw(p); p.updateInventory();
            }
            case 12 -> {
                p.closeInventory();
                p.sendMessage("§eClick any chest to link. You have 10s.");
                // one-time listener for their next interact
                var task = new Object(){ int id = -1; };
                task.id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    // timeout
                }, 200L);
                plugin.getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
                    @org.bukkit.event.EventHandler
                    public void onClick(org.bukkit.event.player.PlayerInteractEvent e) {
                        if (!e.getPlayer().getUniqueId().equals(p.getUniqueId())) return;
                        if (e.getClickedBlock() == null) return;
                        Block b = e.getClickedBlock();
                        if (!(b.getState() instanceof Chest chest)) return;
                        m.setChest(b.getLocation());
                        p.sendMessage("§aLinked to chest.");
                        org.bukkit.event.HandlerList.unregisterAll(this);
                        plugin.getServer().getScheduler().cancelTask(task.id);
                    }
                }, plugin);
            }
            case 14 -> {
                boolean ok = plugin.minions().tryUpgrade(m);
                if (ok) p.sendMessage("§aMinion upgraded to Lv " + m.level());
                else p.sendMessage("§cCannot upgrade (max level or not enough resources).");
                draw(p); p.updateInventory();
            }
            case 16 -> {
                boolean ok = plugin.minions().pickup(m.id());
                if (ok) {
                    // give egg back
                    var helper = new com.chunksmith.nebrixSkyblock.minion.MinionListener(plugin);
                    p.getInventory().addItem(helper.makeEgg());
                    p.sendMessage("§cMinion picked up.");
                    p.closeInventory();
                }
            }
            default -> {}
        }
    }
}
