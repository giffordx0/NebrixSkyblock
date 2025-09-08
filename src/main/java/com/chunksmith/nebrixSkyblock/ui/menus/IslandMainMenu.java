package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.island.IslandMember;
import com.chunksmith.nebrixSkyblock.island.IslandRole;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import com.chunksmith.nebrixSkyblock.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.text.NumberFormat;
import java.util.List;
import java.util.UUID;

/** Main island menu. */
public class IslandMainMenu extends Menu {
    private final NebrixSkyblock plugin;

    public IslandMainMenu(NebrixSkyblock plugin) {
        this.plugin = plugin;
    }

    @Override
    protected Inventory draw(Player viewer) {
        Inventory inv = Bukkit.createInventory(
                null,
                27, // 3 rows
                Text.mini(plugin.getConfig().getString("ui.title-main", "Island Menu")));

        Island island = plugin.islands().getByPlayer(viewer.getUniqueId());

        if (island == null) {
            // Player doesn't have an island - show create option
            inv.setItem(13, new ItemBuilder(Material.GRASS_BLOCK)
                    .name("<green>Create Island</green>")
                    .lore(List.of(
                            "<gray>Start your skyblock adventure!</gray>",
                            "",
                            "<yellow>Click to create your island</yellow>"
                    ))
                    .build());
        } else {
            // Player has an island - show management options
            setupIslandMenu(inv, viewer, island);
        }

        return inv;
    }

    private void setupIslandMenu(Inventory inv, Player viewer, Island island) {
        NumberFormat fmt = NumberFormat.getInstance();

        // Island Info (center)
        inv.setItem(13, new ItemBuilder(Material.GRASS_BLOCK)
                .name("<green>Island Information</green>")
                .lore(List.of(
                        "<gray>Owner: <white>" + Bukkit.getOfflinePlayer(island.owner()).getName() + "</white></gray>",
                        "<gray>Members: <white>" + island.members().size() + "/" + getMaxMembers(island) + "</white></gray>",
                        "<gray>Level: <white>" + island.upgrades().radiusLevel() + "</white></gray>",
                        "<gray>Generator Tier: <white>" + island.upgrades().generatorTier() + "</white></gray>",
                        "",
                        "<yellow>Click to teleport to your island</yellow>"
                ))
                .build());

        // Bank Info
        inv.setItem(10, new ItemBuilder(Material.GOLD_INGOT)
                .name("<yellow>Island Bank</yellow>")
                .lore(List.of(
                        "<gray>Coins: <gold>" + fmt.format(island.bank().coins()) + "</gold></gray>",
                        "<gray>Crystals: <aqua>" + fmt.format(island.bank().crystals()) + "</aqua></gray>",
                        "",
                        "<yellow>Click to manage bank</yellow>"
                ))
                .build());

        // Settings
        inv.setItem(12, new ItemBuilder(Material.REDSTONE)
                .name("<red>Island Settings</red>")
                .lore(List.of(
                        "<gray>PvP: " + (island.settings().pvp() ? "<green>Enabled" : "<red>Disabled") + "</gray>",
                        "",
                        "<yellow>Click to change settings</yellow>"
                ))
                .build());

        // Members Management
        inv.setItem(14, new ItemBuilder(Material.PLAYER_HEAD)
                .name("<blue>Manage Members</blue>")
                .lore(List.of(
                        "<gray>Current members: <white>" + island.members().size() + "</white></gray>",
                        "<gray>Max members: <white>" + getMaxMembers(island) + "</white></gray>",
                        "",
                        "<yellow>Click to manage members</yellow>"
                ))
                .build());

        // Upgrades
        inv.setItem(16, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name("<purple>Island Upgrades</purple>")
                .lore(List.of(
                        "<gray>Radius Level: <white>" + island.upgrades().radiusLevel() + "</white></gray>",
                        "<gray>Member Level: <white>" + island.upgrades().memberLevel() + "</white></gray>",
                        "<gray>Generator Tier: <white>" + island.upgrades().generatorTier() + "</white></gray>",
                        "",
                        "<yellow>Click to upgrade your island</yellow>"
                ))
                .build());

        // Values/Worth
        inv.setItem(19, new ItemBuilder(Material.EMERALD)
                .name("<green>Island Worth</green>")
                .lore(List.of(
                        "<gray>Calculating island value...</gray>",
                        "",
                        "<yellow>Click to recalculate</yellow>"
                ))
                .build());

        // Delete Island (if owner)
        if (island.owner().equals(viewer.getUniqueId())) {
            inv.setItem(25, new ItemBuilder(Material.BARRIER)
                    .name("<red>Delete Island</red>")
                    .lore(List.of(
                            "<dark_red>WARNING: This action cannot be undone!</dark_red>",
                            "",
                            "<red>Click to delete your island</red>"
                    ))
                    .build());
        }
    }

    @Override
    protected void click(Player player, InventoryClickEvent event) {
        int slot = event.getSlot();
        Island island = plugin.islands().getByPlayer(player.getUniqueId());

        switch (slot) {
            case 13 -> {
                if (island == null) {
                    // Create island
                    try {
                        Island newIsland = plugin.islands().createIsland(player.getUniqueId());
                        plugin.islands().teleportToIsland(player, newIsland);
                        player.sendMessage(Text.mini("<green>Island created successfully! Welcome to your new home!</green>"));
                        player.closeInventory();
                    } catch (Exception e) {
                        player.sendMessage(Text.mini("<red>Failed to create island: " + e.getMessage() + "</red>"));
                    }
                } else {
                    // Teleport to island
                    plugin.islands().teleportToIsland(player, island);
                    player.sendMessage(Text.mini("<green>Welcome back to your island!</green>"));
                    player.closeInventory();
                }
            }
            case 10 -> {
                if (island != null) {
                    // TODO: Open bank menu
                    player.sendMessage(Text.mini("<yellow>Bank management coming soon!</yellow>"));
                }
            }
            case 12 -> {
                if (island != null) {
                    new SettingsMenu(plugin, island).open(player);
                }
            }
            case 14 -> {
                if (island != null) {
                    new MembersMenu(plugin, island).open(player);
                }
            }
            case 16 -> {
                if (island != null) {
                    // TODO: Open upgrades menu
                    player.sendMessage(Text.mini("<yellow>Upgrades menu coming soon!</yellow>"));
                }
            }
            case 19 -> {
                if (island != null) {
                    // TODO: Calculate and show island worth
                    player.sendMessage(Text.mini("<yellow>Island worth calculation coming soon!</yellow>"));
                }
            }
            case 25 -> {
                if (island != null && island.owner().equals(player.getUniqueId())) {
                    new DeleteConfirmMenu(plugin, island).open(player);
                }
            }
        }
      }
    }

    private int getMaxMembers(Island island) {
        int baseMembers = plugin.getConfig().getInt("islands.base-members", 1);
        int membersPerLevel = plugin.getConfig().getInt("islands.members-per-level", 1);
        return baseMembers + (island.upgrades().memberLevel() - 1) * membersPerLevel;
    }
}
