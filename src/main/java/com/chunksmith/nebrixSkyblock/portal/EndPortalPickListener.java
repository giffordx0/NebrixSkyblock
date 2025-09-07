package com.chunksmith.nebrixSkyblock.portal;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EndPortalPickListener implements Listener {
    private final NebrixSkyblock plugin;
    public EndPortalPickListener(NebrixSkyblock plugin) { this.plugin = plugin; }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPick(PlayerInteractEvent e) {
        if (!plugin.getConfig().getBoolean("world.endPortalPick", true)) return;
        if (!e.getAction().name().contains("LEFT_CLICK")) return;
        Player p = e.getPlayer();
        ItemStack inHand = p.getInventory().getItemInMainHand();
        if (inHand == null || !inHand.getType().name().endsWith("_PICKAXE")) return;

        Block b = e.getClickedBlock();
        if (b == null) return;
        if (b.getType() != Material.END_PORTAL_FRAME) return;

        // allow breaking and giving back frame
        b.setType(Material.AIR, false);
        b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.END_PORTAL_FRAME, 1));
        p.sendMessage("§aEnd portal frame picked up.");
        e.setCancelled(true);
    }
}
