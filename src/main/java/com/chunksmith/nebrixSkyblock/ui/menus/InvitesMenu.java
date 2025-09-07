package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class InvitesMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final UUID viewerId;
    private final Menu parent;

    // in-memory invites: target -> (owner inviting)
    private static final Map<UUID, UUID> pending = new HashMap<>();

    public InvitesMenu(NebrixSkyblock plugin, UUID viewerId, Menu parent) {
        super(plugin.getConfig().getString("gui.title-members", "§8Nebrix • Members & Invites"), 54);
        this.plugin = plugin;
        this.viewerId = viewerId;
        this.parent = parent;
    }

    @Override
    public void draw(Player viewer) {
        inv.clear();
        inv.setItem(0, new ItemBuilder(Material.LIME_DYE).name("&aInvite Player")
                .lore(List.of("&7Invite an online player to", "&7join your island.")).build());
        inv.setItem(8, new ItemBuilder(Material.ARROW).name(plugin.getConfig().getString("gui.back-name", "&7« Back")).build());

        Island is = plugin.islands().getIsland(viewerId);
        int slot = 9;
        if (is != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(is.ownerId());
            inv.setItem(slot++, new ItemBuilder(Material.PLAYER_HEAD).head(owner).name("&eOwner: &f" + owner.getName()).build());
            for (UUID m : is.members()) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(m);
                inv.setItem(slot++, new ItemBuilder(Material.PLAYER_HEAD).head(op).name("&aMember: &f" + op.getName()).build());
            }
        }

        inv.setItem(45, new ItemBuilder(Material.PAPER).name("&bYour Pending Invite")
                .lore(List.of("&7If someone invited you, accept here.")).build());
        UUID inviter = pending.get(viewer.getUniqueId());
        if (inviter != null) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(inviter);
            inv.setItem(46, new ItemBuilder(Material.GREEN_WOOL).name("&aAccept from &f" + op.getName()).build());
            inv.setItem(47, new ItemBuilder(Material.RED_WOOL).name("&cDecline").build());
        }
    }

    @Override
    public void onClick(Player p, int slot, ItemStack clicked, ClickType type) {
        if (slot == 8) { parent.open(p); return; }

        if (slot == 0) {
            new PlayerSelectMenu(plugin, p.getUniqueId(), target -> {
                if (!plugin.islands().hasIsland(p.getUniqueId())) { p.sendMessage("§cCreate an island first."); return; }
                if (plugin.islands().maxMembers() <= plugin.islands().getIsland(p.getUniqueId()).members().size()) {
                    p.sendMessage("§cIsland member limit reached."); return;
                }
                pending.put(target, p.getUniqueId());
                Player tp = Bukkit.getPlayer(target);
                if (tp != null) tp.sendMessage("§dNebrix: §f" + p.getName() + " invited you to their island. Open /is to accept.");
                p.sendMessage("§aInvite sent.");
                draw(p); p.updateInventory();
            }).open(p);
            return;
        }
        if (slot == 46) {
            UUID inviter = pending.remove(p.getUniqueId());
            if (inviter == null) return;
            Island ownerIsland = plugin.islands().getIsland(inviter);
            if (ownerIsland == null) { p.sendMessage("§cThat island no longer exists."); return; }
            if (plugin.islands().isMemberOfAny(p.getUniqueId())) { p.sendMessage("§cYou are already in an island."); return; }
            plugin.islands().addMember(ownerIsland.ownerId(), p.getUniqueId());
            p.sendMessage("§aJoined " + Bukkit.getOfflinePlayer(ownerIsland.ownerId()).getName() + "'s island.");
            draw(p); p.updateInventory();
            return;
        }
        if (slot == 47) {
            if (pending.remove(p.getUniqueId()) != null) p.sendMessage("§eInvite declined.");
            draw(p); p.updateInventory();
        }
    }
}