package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.island.IslandRole;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import com.chunksmith.nebrixSkyblock.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import java.util.List;

public class SettingsMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final Island island;

    public SettingsMenu(NebrixSkyblock plugin, Island island) {
        this.plugin = plugin;
        this.island = island;
    }

    @Override
    protected Inventory draw(Player viewer) {
        Inventory inv = Bukkit.createInventory(
                null,
                27,
                Text.mini("<red>Island Settings</red>"));

        // PvP Setting
        inv.setItem(10, new ItemBuilder(island.settings().pvp() ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD)
                .name(island.settings().pvp() ? "<green>PvP: Enabled</green>" : "<red>PvP: Disabled</red>")
                .lore(List.of(
                        "<gray>Allow players to fight on your island</gray>",
                        "",
                        island.settings().pvp() ? "<red>Click to disable PvP</red>" : "<green>Click to enable PvP</green>"
                ))
                .build());

        // Back button
        inv.setItem(22, new ItemBuilder(Material.ARROW)
                .name("<yellow>Back to Island Menu</yellow>")
                .build());

        return inv;
    }

    @Override
    protected void click(Player player, InventoryClickEvent event) {
        if (!canPlayerModifySettings(player)) {
            player.sendMessage(Text.mini("<red>You don't have permission to change island settings!</red>"));
            return;
        }

        switch (event.getSlot()) {
            case 10 -> {
                // Toggle PvP
                boolean newPvp = !island.settings().pvp();
                island.settings().setPvp(newPvp);
                plugin.storage().saveIsland(island);

                player.sendMessage(Text.mini(newPvp ?
                        "<green>PvP enabled on your island!</green>" :
                        "<red>PvP disabled on your island!</red>"));

                // Refresh menu
                open(player);
            }
            case 22 -> {
                // Back to main menu
                new IslandMainMenu(plugin).open(player);
            }
        }
    }

    private boolean canPlayerModifySettings(Player player) {
        var member = island.members().get(player.getUniqueId());
        return member != null && (member.role() == IslandRole.OWNER || member.role() == IslandRole.OFFICER);
    }
}
