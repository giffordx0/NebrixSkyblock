package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.island.IslandMember;
import com.chunksmith.nebrixSkyblock.island.IslandRole;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import com.chunksmith.nebrixSkyblock.util.Text;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/** Main island menu. */
public class IslandMainMenu extends Menu {

  public IslandMainMenu(NebrixSkyblock plugin) {
    super(plugin);
  }

  @Override
  public Component title() {
    return Text.mini(plugin.getConfig().getString("ui.title-main", "Island"));
  }

  @Override
  public Inventory build(Player viewer) {
    Inventory inv = Bukkit.createInventory(null, 27, title());
    Island island = plugin.islands().byPlayer(viewer.getUniqueId());
    if (island == null) {
      inv.setItem(
          13,
          new ItemBuilder(Material.GRASS_BLOCK)
              .name("<green>Create Island</green>")
              .lore(List.of("<gray>Start your adventure</gray>"))
              .build());
      return inv;
    }

    inv.setItem(
        11,
        new ItemBuilder(Material.BOOK)
            .name("<yellow>Members</yellow>")
            .lore(List.of("<gray>Manage island members</gray>"))
            .build());
    inv.setItem(
        13,
        new ItemBuilder(Material.COMPARATOR)
            .name("<yellow>Settings</yellow>")
            .lore(List.of("<gray>Island toggles</gray>"))
            .build());
    IslandMember member = island.members().get(viewer.getUniqueId());
    if (member.role() == IslandRole.OWNER) {
      inv.setItem(
          15,
          new ItemBuilder(Material.CHEST)
              .name("<yellow>Manage</yellow>")
              .lore(List.of("<gray>Admin actions</gray>"))
              .build());
    }
    return inv;
  }

  @Override
  public void onClick(Player player, int slot, InventoryClickEvent event) {
    Island island = plugin.islands().byPlayer(player.getUniqueId());
    if (island == null) {
      if (slot == 13) {
        Island created = plugin.islands().createIsland(player.getUniqueId());
        plugin.storage().saveIsland(created);
        player.sendMessage(Text.mini("<green>Island created!</green>"));
        plugin.menus().open(player, new IslandMainMenu(plugin));
      }
      return;
    }
    switch (slot) {
      case 11 -> plugin.menus().open(player, new MembersMenu(plugin, island));
      case 13 -> plugin.menus().open(player, new SettingsMenu(plugin, island));
      case 15 -> {
        IslandMember member = island.members().get(player.getUniqueId());
        if (member.role() == IslandRole.OWNER) {
          plugin.menus().open(player, new ManageMenu(plugin, island));
        }
      }
    }
  }
}
