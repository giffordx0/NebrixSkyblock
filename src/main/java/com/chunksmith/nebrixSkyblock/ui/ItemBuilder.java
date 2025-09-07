package com.chunksmith.nebrixSkyblock.ui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private final ItemStack stack;
    public ItemBuilder(Material mat) { this.stack = new ItemStack(mat); }
    public ItemBuilder(ItemStack base) { this.stack = base.clone(); }

    public ItemBuilder name(String name) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        ItemMeta meta = stack.getItemMeta();
        List<String> ll = new ArrayList<>();
        for (String s : lines) ll.add(ChatColor.translateAlternateColorCodes('&', s));
        meta.setLore(ll);
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder appendLore(String line) {
        ItemMeta meta = stack.getItemMeta();
        List<String> ll = meta.getLore();
        if (ll == null) ll = new ArrayList<>();
        ll.add(ChatColor.translateAlternateColorCodes('&', line));
        meta.setLore(ll);
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        ItemMeta meta = stack.getItemMeta(); meta.addItemFlags(flags); stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder amount(int amt) { stack.setAmount(amt); return this; }

    public ItemBuilder head(OfflinePlayer player) {
        if (stack.getType() != Material.PLAYER_HEAD) return this;
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof SkullMeta skull) { skull.setOwningPlayer(player); stack.setItemMeta(skull); }
        return this;
    }

    public ItemStack build() { return stack; }
}