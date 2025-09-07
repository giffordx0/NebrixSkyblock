package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import com.chunksmith.nebrixSkyblock.util.Text;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class IslandMainMenu extends Menu {
  private final NebrixSkyblock plugin;
  private final UUID viewerId;

  public IslandMainMenu(NebrixSkyblock plugin, UUID viewerId) {
    this.plugin = plugin;
    this.viewerId = viewerId;
  }

  @Override
  protected Inventory draw(Player viewer) {
    Inventory inv =
        Bukkit.createInventory(
            null,
            9,
            Text.mini(plugin.getConfig().getString("ui.title-main", "Nebrix Skyblock")));
    inv.setItem(
        4,
        new ItemBuilder(Material.GRASS_BLOCK)
            .name("<green>Create Island</green>")
            .lore(List.of("<gray>WIP</gray>"))
            .build());
    return inv;
  }
}
