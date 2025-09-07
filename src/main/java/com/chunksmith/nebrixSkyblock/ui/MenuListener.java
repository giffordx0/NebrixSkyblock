package com.chunksmith.nebrixSkyblock.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Concrete listener for GUI menus.
 * Tracks which Menu a player has open and forwards click/close events.
 */
public class MenuListener implements Listener {
    private static final Map<UUID, Menu> OPEN = new ConcurrentHashMap<>();

    /** Associate an open menu with a player */
    public static void track(UUID viewer, Menu menu) {
        OPEN.put(viewer, menu);
    }

    /** Remove association when closed */
    public static void untrack(UUID viewer) {
        OPEN.remove(viewer);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        Menu menu = OPEN.get(p.getUniqueId());
        if (menu == null) return;

        // make sure this click is for the tracked inventory (by title)
        if (!Objects.equals(e.getView().getTitle(), menu.title())) return;

        e.setCancelled(true);
        if (e.getClickedInventory() == null) return;

        menu.onClick(p, e.getSlot(), e.getCurrentItem(), e.getClick());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;

        Menu menu = OPEN.remove(p.getUniqueId());
        if (menu != null) menu.onClose(p);
    }
}