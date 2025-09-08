package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.island.IslandMember;
import com.chunksmith.nebrixSkyblock.island.IslandRole;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import com.chunksmith.nebrixSkyblock.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MembersMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final Island island;

    public MembersMenu(NebrixSkyblock plugin, Island island) {
        this.plugin = plugin;
        this.island = island;
    }

    @Override
    protected Inventory draw(Player viewer) {
        Inventory inv = Bukkit.createInventory(
                null,
                54, // 6 rows for more members
                Text.mini("<blue>Island Members</blue>"));

        int slot = 0;
        for (Map.Entry<UUID, IslandMember> entry : island.members().entrySet()) {
            if (slot >= 45) break; // Leave space for controls

            UUID memberId = entry.getKey();
            IslandMember member = entry.getValue();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberId);

            String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
            boolean isOnline = offlinePlayer.isOnline();

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Role: <white>" + formatRole(member.role()) + "</white></gray>");
            lore.add("<gray>Status: " + (isOnline ? "<green>Online" : "<red>Offline") + "</gray>");
            lore.add("");

            if (canViewerModifyMember(viewer, member)) {
                if (member.role() != IslandRole.OWNER) {
                    lore.add("<red>Right-click to remove</red>");
                    if (member.role() != IslandRole.OFFICER) {
                        lore.add("<yellow>Shift-click to promote</yellow>");
                    }
                }
            } else {
                lore.add("<gray>View only</gray>");
            }

            Material headMaterial = isOnline ? Material.PLAYER_HEAD : Material.SKELETON_SKULL;

            inv.setItem(slot, new ItemBuilder(headMaterial)
                    .name("<white>" + playerName + "</white>")
                    .lore(lore)
                    .build());

            slot++;
        }

        // Add member button (if has permission)
        if (canViewerInvite(viewer)) {
            inv.setItem(49, new ItemBuilder(Material.LIME_CONCRETE)
                    .name("<green>Invite Player</green>")
                    .lore(List.of(
                            "<gray>Add a new member to your island</gray>",
                            "",
                            "<yellow>Click to invite a player</yellow>"
                    ))
                    .build());
        }

        // Back button
        inv.setItem(53, new ItemBuilder(Material.ARROW)
                .name("<yellow>Back to Island Menu</yellow>")
                .build());

        return inv;
    }

    @Override
    protected void click(Player player, InventoryClickEvent event) {
        int slot = event.getSlot();

        if (slot == 49 && canViewerInvite(player)) {
            // Invite player - for now just show a message
            player.sendMessage(Text.mini("<yellow>Player invitation system coming soon! Use /island invite <player> for now.</yellow>"));
            return;
        }

        if (slot == 53) {
            // Back to main menu
            new IslandMainMenu(plugin).open(player);
            return;
        }

        // Handle member clicks
        if (slot < 45) {
            List<UUID> memberIds = new ArrayList<>(island.members().keySet());
            if (slot < memberIds.size()) {
                UUID targetId = memberIds.get(slot);
                IslandMember targetMember = island.members().get(targetId);

                if (!canViewerModifyMember(player, targetMember)) {
                    player.sendMessage(Text.mini("<red>You don't have permission to modify this member!</red>"));
                    return;
                }

                if (targetMember.role() == IslandRole.OWNER) {
                    player.sendMessage(Text.mini("<red>Cannot modify the island owner!</red>"));
                    return;
                }

                if (event.isRightClick()) {
                    // Remove member
                    removeMember(player, targetId, targetMember);
                } else if (event.isShiftClick() && targetMember.role() == IslandRole.MEMBER) {
                    // Promote to officer
                    promoteMember(player, targetId, targetMember);
                }
            }
        }
    }

    private void removeMember(Player player, UUID targetId, IslandMember targetMember) {
        try {
            plugin.islands().removeMember(island, targetId);

            OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);
            String targetName = target.getName() != null ? target.getName() : "Unknown";

            player.sendMessage(Text.mini("<red>" + targetName + " has been removed from the island.</red>"));

            // Notify target if online
            if (target.isOnline()) {
                ((Player) target).sendMessage(Text.mini("<red>You have been removed from " +
                        Bukkit.getOfflinePlayer(island.owner()).getName() + "'s island.</red>"));
            }

            // Refresh menu
            open(player);
        } catch (Exception e) {
            player.sendMessage(Text.mini("<red>Failed to remove member: " + e.getMessage() + "</red>"));
        }
    }

    private void promoteMember(Player player, UUID targetId, IslandMember targetMember) {
        island.members().put(targetId, new IslandMember(targetId, IslandRole.OFFICER));
        plugin.storage().saveIsland(island);

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);
        String targetName = target.getName() != null ? target.getName() : "Unknown";

        player.sendMessage(Text.mini("<green>" + targetName + " has been promoted to Officer!</green>"));

        // Notify target if online
        if (target.isOnline()) {
            ((Player) target).sendMessage(Text.mini("<green>You have been promoted to Officer on " +
                    Bukkit.getOfflinePlayer(island.owner()).getName() + "'s island!</green>"));
        }

        // Refresh menu
        open(player);
    }

    private boolean canViewerModifyMember(Player viewer, IslandMember targetMember) {
        IslandMember viewerMember = island.members().get(viewer.getUniqueId());
        if (viewerMember == null) return false;

        // Only owner can modify officers, owner and officers can modify members
        return switch (viewerMember.role()) {
            case OWNER -> targetMember.role() != IslandRole.OWNER;
            case OFFICER -> targetMember.role() == IslandRole.MEMBER;
            case MEMBER -> false;
        };
    }

    private boolean canViewerInvite(Player viewer) {
        IslandMember viewerMember = island.members().get(viewer.getUniqueId());
        if (viewerMember == null) return false;

        return viewerMember.role() == IslandRole.OWNER || viewerMember.role() == IslandRole.OFFICER;
    }

    private String formatRole(IslandRole role) {
        return switch (role) {
            case OWNER -> "<gold>Owner</gold>";
            case OFFICER -> "<yellow>Officer</yellow>";
            case MEMBER -> "<green>Member</green>";
        };
    }
}
