package com.chunksmith.nebrixSkyblock.ui;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/** Simple menu framework. */
public abstract class Menu {
  private static final Map<Player, Menu> OPEN = new HashMap<>();

  protected abstract Inventory draw(Player viewer);

  protected void click(Player player, InventoryClickEvent event) {}

  public void open(Player player) {
    Inventory inv = draw(player);
    OPEN.put(player, this);
    player.openInventory(inv);
  }

  public static class MenuListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
      if (!(event.getWhoClicked() instanceof Player p)) return;
      Menu menu = OPEN.get(p);
      if (menu != null) {
        menu.click(p, event);
        event.setCancelled(true);
      }
    }
  }
}
