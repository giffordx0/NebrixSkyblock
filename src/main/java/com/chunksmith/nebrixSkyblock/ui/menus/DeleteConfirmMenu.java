package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import com.chunksmith.nebrixSkyblock.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class DeleteConfirmMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final Island island;

    public DeleteConfirmMenu(NebrixSkyblock plugin, Island island) {
        this.plugin = plugin;
        this.island = island;
    }

    @Override
    protected Inventory draw(Player viewer) {
        Inventory inv = Bukkit.createInventory(
                null,
                27,
                Text.mini("<dark_red>Delete Island - Confirmation</dark_red>"));

        // Warning message
        inv.setItem(4, new ItemBuilder(Material.BARRIER)
                .name("<dark_red>DELETE ISLAND</dark_red>")
                .lore(List.of(
                        "<red>This action cannot be undone!</red>",
                        "",
                        "<gray>Your island will be permanently deleted.</gray>",
                        "<gray>All blocks, items, and progress will be lost.</gray>",
                        "<gray>All members will be removed.</gray>",
                        "",
                        "<dark_red>Are you sure you want to continue?</dark_red>"
                ))
                .build());

        // Confirm button
        inv.setItem(11, new ItemBuilder(Material.RED_CONCRETE)
                .name("<red>YES - DELETE ISLAND</red>")
                .lore(List.of(
                        "<dark_red>Click to permanently delete your island</dark_red>",
                        "",
                        "<red>THIS CANNOT BE UNDONE!</red>"
                ))
                .build());

        // Cancel button
        inv.setItem(15, new ItemBuilder(Material.GREEN_CONCRETE)
                .name("<green>NO - KEEP ISLAND</green>")
                .lore(List.of(
                        "<green>Return to island menu without deleting</green>"
                ))
                .build());

        return inv;
    }

    @Override
    protected void click(Player player, InventoryClickEvent event) {
        if (!island.owner().equals(player.getUniqueId())) {
            player.sendMessage(Text.mini("<red>Only the island owner can delete the island!</red>"));
            player.closeInventory();
            return;
        }

        switch (event.getSlot()) {
            case 11 -> {
                // Confirm deletion
                deleteIsland(player);
            }
            case 15 -> {
                // Cancel - return to main menu
                new IslandMainMenu(plugin).open(player);
            }
        }
    }

    private void deleteIsland(Player player) {
        try {
            // Notify all members before deletion
            island.members().keySet().forEach(memberId -> {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && !member.equals(player)) {
                    member.sendMessage(Text.mini("<red>The island you were a member of has been deleted.</red>"));
                }
            });

            // Delete the island
            plugin.islands().deleteIsland(island.id());

            // Teleport player to spawn (if available)
            try {
                player.teleport(player.getWorld().getSpawnLocation());
            } catch (Exception e) {
                plugin.getLogger().warning("Could not teleport player to spawn after island deletion: " + e.getMessage());
            }

            player.sendMessage(Text.mini("<red>Your island has been permanently deleted.</red>"));
            player.closeInventory();

        } catch (Exception e) {
            player.sendMessage(Text.mini("<red>Failed to delete island: " + e.getMessage() + "</red>"));
            plugin.getLogger().severe("Failed to delete island " + island.id() + ": " + e.getMessage());
        }
    }
}