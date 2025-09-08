package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import com.chunksmith.nebrixSkyblock.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/** Simple settings menu. */
public class SettingsMenu extends Menu {
  private final Island island;

  public SettingsMenu(NebrixSkyblock plugin, Island island) {
    super(plugin);
    this.island = island;
  }

  @Override
  public Component title() {
    return Text.mini("<yellow>Settings</yellow>");
  }

  @Override
  public Inventory build(Player viewer) {
    Inventory inv = Bukkit.createInventory(null, 9, title());
    String status = island.settings().pvp() ? "<green>ON</green>" : "<red>OFF</red>";
    inv.setItem(
        4,
        new ItemBuilder(Material.DIAMOND_SWORD)
            .name("<yellow>PvP: " + status)
            .build());
    inv.setItem(8, new ItemBuilder(Material.ARROW).name("<yellow>Back</yellow>").build());
    return inv;
  }

  @Override
  public void onClick(Player player, int slot, InventoryClickEvent event) {
    if (slot == 4) {
      island.settings().setPvp(!island.settings().pvp());
      plugin.storage().saveIsland(island);
      player.sendMessage(
          Text.mini(
              "<yellow>PvP " + (island.settings().pvp() ? "enabled" : "disabled") + "</yellow>"));
      plugin.menus().open(player, new SettingsMenu(plugin, island));
    } else if (slot == 8) {
      plugin.menus().open(player, new IslandMainMenu(plugin));
    }
  }
}
