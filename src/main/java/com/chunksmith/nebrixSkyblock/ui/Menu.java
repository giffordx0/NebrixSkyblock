package com.chunksmith.nebrixSkyblock.ui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/** Base class for simple Chest-GUI menus. */
public abstract class Menu {
    protected final String title;
    protected final int size; // multiple of 9
    protected Inventory inv;

    protected Menu(String title, int size) {
        this.title = title;
        this.size = size;
    }

    public void open(Player p) {
        inv = Bukkit.createInventory(p, size, title);
        draw(p);
        p.openInventory(inv);
        MenuListener.track(p.getUniqueId(), this);
    }

    public void set(int slot, ItemStack stack) {
        if (inv != null && slot >= 0 && slot < inv.getSize()) inv.setItem(slot, stack);
    }

    /** Fill inventory before opening (or when redrawing). */
    public abstract void draw(Player viewer);

    /** Handle clicks (override in subclasses). */
    public void onClick(Player p, int slot, ItemStack clicked, ClickType type) {}

    /** Handle close (override in subclasses). */
    public void onClose(Player p) {}

    public String title() { return title; }
}