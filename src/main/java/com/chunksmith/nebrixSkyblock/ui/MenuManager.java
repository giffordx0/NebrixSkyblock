package com.chunksmith.nebrixSkyblock.ui;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/** Tracks open menus and dispatches events. */
public final class MenuManager implements Listener {
  private final Map<UUID, Menu> open = new ConcurrentHashMap<>();

  public MenuManager() {}

  public void open(Player player, Menu menu) {
    open.put(player.getUniqueId(), menu);
    Inventory inv = menu.build(player);
    player.openInventory(inv);
  }

  public Menu getOpen(Player player) {
    return open.get(player.getUniqueId());
  }

  public void close(Player player) {
    open.remove(player.getUniqueId());
  }

  @EventHandler
  public void onClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;
    Menu menu = open.get(player.getUniqueId());
    if (menu == null) return;
    Component expected = menu.title();
    if (!event.getView().title().equals(expected)) return;

    if (menu.cancelClicks()) event.setCancelled(true);

    if (event.getClickedInventory() == null) return;
    if (event.getView().getTopInventory() != event.getClickedInventory()) return;

    menu.onClick(player, event.getRawSlot(), event);
  }

  @EventHandler
  public void onClose(InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player player)) return;
    Menu menu = open.get(player.getUniqueId());
    if (menu != null && event.getView().title().equals(menu.title())) {
      close(player);
    }
  }
}
