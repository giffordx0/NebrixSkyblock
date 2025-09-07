package com.chunksmith.nebrixSkyblock.ui;

import com.chunksmith.nebrixSkyblock.util.Text;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBuilder {
  private final ItemStack stack;

  public ItemBuilder(Material mat) {
    this.stack = new ItemStack(mat);
  }

  public ItemBuilder name(String mm) {
    ItemMeta meta = stack.getItemMeta();
    meta.displayName(Text.mini(mm));
    stack.setItemMeta(meta);
    return this;
  }

  public ItemBuilder lore(List<String> lines) {
    ItemMeta meta = stack.getItemMeta();
    List<Component> lore = lines.stream().map(Text::mini).toList();
    meta.lore(lore);
    stack.setItemMeta(meta);
    return this;
  }

  public ItemStack build() {
    return stack;
  }
}
