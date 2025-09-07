package com.chunksmith.nebrixSkyblock.protect;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.util.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

/** Comprehensive protection listener for island areas. */
public class ProtectionListener implements Listener {
    private final NebrixSkyblock plugin;

    public ProtectionListener(NebrixSkyblock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        Island island = findIslandAtLocation(location);
        if (island == null) return; // Not in any island

        if (!plugin.islands().canPlayerAccess(player.getUniqueId(), island)) {
            event.setCancelled(true);
            player.sendMessage(Text.mini("<red>You cannot break blocks on this island!</red>"));
            return;
        }

        // Check block limits when placing valuable blocks
        if (isLimitedBlock(event.getBlock().getType())) {
            // This is handled by the limits service during placement, but we could track removal here
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        Island island = findIslandAtLocation(location);
        if (island == null) return; // Not in any island

        if (!plugin.islands().canPlayerAccess(player.getUniqueId(), island)) {
            event.setCancelled(true);
            player.sendMessage(Text.mini("<red>You cannot place blocks on this island!</red>"));
            return;
        }

        // Check block limits
        Material material = event.getBlock().getType();
        if (isLimitedBlock(material)) {
            if (!plugin.limits().canPlace(island.id(), material)) {
                event.setCancelled(true);
                player.sendMessage(Text.mini("<red>Block limit reached for " + material.name() + "!</red>"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();

        Island island = findIslandAtLocation(location);
        if (island == null) return; // Not in any island

        // Allow interaction for island members
        if (plugin.islands().canPlayerAccess(player.getUniqueId(), island)) {
            return;
        }

        // Check if it's a protected interaction
        Material material = event.getClickedBlock().getType();
        if (isProtectedBlock(material)) {
            event.setCancelled(true);
            player.sendMessage(Text.mini("<red>You cannot interact with this on someone else's island!</red>"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Location location = event.getBlock().getLocation();
        Island island = findIslandAtLocation(location);

        if (island != null && !plugin.islands().canPlayerAccess(event.getPlayer().getUniqueId(), island)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Text.mini("<red>You cannot use buckets on this island!</red>"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Location location = event.getBlock().getLocation();
        Island island = findIslandAtLocation(location);

        if (island != null && !plugin.islands().canPlayerAccess(event.getPlayer().getUniqueId(), island)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Text.mini("<red>You cannot use buckets on this island!</red>"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker) || !(event.getEntity() instanceof Player victim)) {
            return;
        }

        Location location = victim.getLocation();
        Island island = findIslandAtLocation(location);

        if (island == null) return; // Not in any island

        // Check PvP settings
        if (!island.settings().pvp()) {
            event.setCancelled(true);
            attacker.sendMessage(Text.mini("<red>PvP is disabled on this island!</red>"));
            return;
        }

        // Both players must be island members to fight
        boolean attackerMember = plugin.islands().canPlayerAccess(attacker.getUniqueId(), island);
        boolean victimMember = plugin.islands().canPlayerAccess(victim.getUniqueId(), island);

        if (!attackerMember || !victimMember) {
            event.setCancelled(true);
            attacker.sendMessage(Text.mini("<red>You can only fight other island members!</red>"));
        }
    }

    private Island findIslandAtLocation(Location location) {
        // Check all islands to see if this location is within any island's radius
        for (Island island : plugin.islands().getIslands()) {
            if (plugin.islands().isInIslandRadius(island, location)) {
                return island;
            }
        }
        return null;
    }

    private boolean isProtectedBlock(Material material) {
        return switch (material) {
            case CHEST, TRAPPED_CHEST, ENDER_CHEST, SHULKER_BOX,
                 FURNACE, BLAST_FURNACE, SMOKER,
                 CRAFTING_TABLE, ANVIL, CHIPPED_ANVIL, DAMAGED_ANVIL,
                 ENCHANTING_TABLE, BREWING_STAND,
                 LEVER, STONE_BUTTON, OAK_BUTTON, SPRUCE_BUTTON, BIRCH_BUTTON,
                 JUNGLE_BUTTON, ACACIA_BUTTON, DARK_OAK_BUTTON, CRIMSON_BUTTON, WARPED_BUTTON,
                 OAK_DOOR, SPRUCE_DOOR, BIRCH_DOOR, JUNGLE_DOOR, ACACIA_DOOR, DARK_OAK_DOOR,
                 CRIMSON_DOOR, WARPED_DOOR, IRON_DOOR,
                 OAK_TRAPDOOR, SPRUCE_TRAPDOOR, BIRCH_TRAPDOOR, JUNGLE_TRAPDOOR,
                 ACACIA_TRAPDOOR, DARK_OAK_TRAPDOOR, CRIMSON_TRAPDOOR, WARPED_TRAPDOOR, IRON_TRAPDOOR,
                 OAK_FENCE_GATE, SPRUCE_FENCE_GATE, BIRCH_FENCE_GATE, JUNGLE_FENCE_GATE,
                 ACACIA_FENCE_GATE, DARK_OAK_FENCE_GATE, CRIMSON_FENCE_GATE, WARPED_FENCE_GATE -> true;
            default -> material.name().contains("SHULKER_BOX");
        };
    }

    private boolean isLimitedBlock(Material material) {
        return plugin.limits().limits().containsKey(material);
    }
}