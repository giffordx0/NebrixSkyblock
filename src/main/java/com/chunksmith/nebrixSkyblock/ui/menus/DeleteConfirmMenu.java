package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.island.IslandMember;
import com.chunksmith.nebrixSkyblock.island.IslandRole;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import com.chunksmith.nebrixSkyblock.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/** Confirmation dialog for island deletion. */
public class DeleteConfirmMenu extends Menu {
  private final Island island;

  public DeleteConfirmMenu(NebrixSkyblock plugin, Island island) {
    super(plugin);
    this.island = island;
  }

  @Override
  public Component title() {
    return Text.mini("<red>Confirm Delete</red>");
  }

  @Override
  public Inventory build(Player viewer) {
    Inventory inv = Bukkit.createInventory(null, 9, title());
    inv.setItem(3, new ItemBuilder(Material.GREEN_WOOL).name("<green>Confirm</green>").build());
    inv.setItem(5, new ItemBuilder(Material.RED_WOOL).name("<red>Cancel</red>").build());
    return inv;
  }

  @Override
  public void onClick(Player player, int slot, InventoryClickEvent event) {
    if (slot == 3) {
      IslandMember member = island.members().get(player.getUniqueId());
      if (member.role() != IslandRole.OWNER) {
        player.sendMessage(Text.mini("<red>Only the owner can delete the island.</red>"));
        return;
      }
      plugin.islands().deleteIsland(island.id());
      plugin.storage().flush();
      plugin.menus().close(player);
      player.closeInventory();
      player.sendMessage(Text.mini("<red>Island deleted.</red>"));
    } else if (slot == 5) {
      plugin.menus().open(player, new IslandMainMenu(plugin));
    }
  }
}
