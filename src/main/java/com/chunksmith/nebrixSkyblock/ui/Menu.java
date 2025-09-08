package com.chunksmith.nebrixSkyblock.ui;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/** Base class for GUI menus. */
public abstract class Menu {
  protected final NebrixSkyblock plugin;

  protected Menu(NebrixSkyblock plugin) {
    this.plugin = plugin;
  }

  /** Title displayed for this menu. */
  public abstract Component title();

  /** Build inventory for the given viewer. */
  public abstract Inventory build(Player viewer);

  /** Handle a click in this menu. */
  public void onClick(Player viewer, int slot, InventoryClickEvent event) {}

  /** Whether clicks should be cancelled automatically. */
  public boolean cancelClicks() {
    return true;
  }
}
