package com.chunksmith.nebrixSkyblock.protection;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ProtectionListener implements Listener {
    private final NebrixSkyblock plugin;

    public ProtectionListener(NebrixSkyblock plugin) { this.plugin = plugin; }

    private boolean isOverworld(World w) {
        return w != null && w.getName().equalsIgnoreCase(plugin.overworld());
    }

    private Island islandAt(org.bukkit.Location loc) { return plugin.islands().islandAt(loc); }

    private boolean canBuild(Player p, org.bukkit.Location loc) {
        Island is = islandAt(loc);
        if (is == null) return false;
        if (p.hasPermission("nebrix.admin")) return true;
        return is.canBuild(p.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (!isOverworld(e.getBlock().getWorld())) return;
        Island is = islandAt(e.getBlock().getLocation());
        if (is == null || !canBuild(e.getPlayer(), e.getBlock().getLocation())) { e.setCancelled(true); return; }
        if (plugin.islands().isOverLimit(is, e.getBlock().getType())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cIsland limit reached for " + e.getBlock().getType().name() + ".");
            return;
        }
        plugin.islands().onBlockPlaced(is, e.getBlock().getType());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (!isOverworld(e.getBlock().getWorld())) return;
        Island is = islandAt(e.getBlock().getLocation());
        if (is == null || !canBuild(e.getPlayer(), e.getBlock().getLocation())) { e.setCancelled(true); return; }
        plugin.islands().onBlockBroken(is, e.getBlock().getType());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        if (!isOverworld(e.getClickedBlock().getWorld())) return;
        if (!canBuild(e.getPlayer(), e.getClickedBlock().getLocation())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucket(PlayerBucketEmptyEvent e) {
        if (e.getBlockClicked() == null) return;
        if (!isOverworld(e.getBlockClicked().getWorld())) return;
        if (!canBuild(e.getPlayer(), e.getBlockClicked().getLocation())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        if (e.getDamager() instanceof Player) {
            if (isOverworld(victim.getWorld())) e.setCancelled(true);
        }
    }
}
