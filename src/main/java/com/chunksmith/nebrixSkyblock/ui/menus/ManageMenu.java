package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import com.chunksmith.nebrixSkyblock.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ManageMenu extends Menu {
  private final NebrixSkyblock plugin;

  public ManageMenu(NebrixSkyblock plugin) {
    this.plugin = plugin;
  }

  @Override
  protected Inventory draw(Player viewer) {
    return Bukkit.createInventory(
        null, 9, Text.mini(plugin.getConfig().getString("ui.title-main", "Manage")));
  }
}
