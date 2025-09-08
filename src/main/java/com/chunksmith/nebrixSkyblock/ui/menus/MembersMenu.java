package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.island.IslandMember;
import com.chunksmith.nebrixSkyblock.island.IslandRole;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import com.chunksmith.nebrixSkyblock.util.Text;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.SkullMeta;

/** Displays and manages island members. */
public class MembersMenu extends Menu {
  private final Island island;
  private final Map<Integer, UUID> memberSlots = new HashMap<>();

  public MembersMenu(NebrixSkyblock plugin, Island island) {
    super(plugin);
    this.island = island;
  }

  @Override
  public Component title() {
    return Text.mini("<yellow>Members</yellow>");
  }

  @Override
  public Inventory build(Player viewer) {
    memberSlots.clear();
    Inventory inv = Bukkit.createInventory(null, 54, title());
    int index = 0;
    for (UUID uuid : island.members().keySet()) {
      if (index >= 45) break;
      OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
      ItemBuilder builder =
          new ItemBuilder(Material.PLAYER_HEAD)
              .name("<white>" + (op.getName() != null ? op.getName() : uuid) + "</white>")
              .lore(
                  List.of(
                      "<gray>" + island.members().get(uuid).role().name() + "</gray>",
                      "<gray>L-Click: remove</gray>",
                      "<gray>R-Click: promote</gray>"));
      var stack = builder.build();
      SkullMeta meta = (SkullMeta) stack.getItemMeta();
      meta.setOwningPlayer(op);
      stack.setItemMeta(meta);
      inv.setItem(index, stack);
      memberSlots.put(index, uuid);
      index++;
    }
    inv.setItem(45, new ItemBuilder(Material.ARROW).name("<yellow>Back</yellow>").build());
    inv.setItem(49, new ItemBuilder(Material.EMERALD).name("<green>Invite Player</green>").build());
    return inv;
  }

  @Override
  public void onClick(Player player, int slot, InventoryClickEvent event) {
    if (slot == 45) {
      plugin.menus().open(player, new IslandMainMenu(plugin));
      return;
    }
    if (slot == 49) {
      player.closeInventory();
      plugin.menus().close(player);
      player.performCommand("island invite");
      return;
    }
    UUID targetId = memberSlots.get(slot);
    if (targetId == null) return;
    IslandMember viewerMember = island.members().get(player.getUniqueId());
    IslandMember targetMember = island.members().get(targetId);
    if (event.isLeftClick()) {
      if (viewerMember.role() == IslandRole.OWNER
          || (viewerMember.role() == IslandRole.OFFICER
              && targetMember.role() == IslandRole.MEMBER)) {
        island.members().remove(targetId);
        plugin.storage().saveIsland(island);
        player.sendMessage(Text.mini("<yellow>Member removed.</yellow>"));
        plugin.menus().open(player, new MembersMenu(plugin, island));
      } else {
        player.sendMessage(Text.mini("<red>No permission.</red>"));
      }
    } else if (event.isRightClick()) {
      if (targetMember.role() == IslandRole.MEMBER
          && (viewerMember.role() == IslandRole.OWNER
              || viewerMember.role() == IslandRole.OFFICER)) {
        island.members().put(targetId, new IslandMember(targetId, IslandRole.OFFICER));
        plugin.storage().saveIsland(island);
        player.sendMessage(Text.mini("<green>Member promoted.</green>"));
        plugin.menus().open(player, new MembersMenu(plugin, island));
      } else {
        player.sendMessage(Text.mini("<red>Cannot promote.</red>"));
      }
    }
  }
}
