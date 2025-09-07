package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class IslandMainMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final UUID viewerId;

    private final String titleMain;
    private final String backText;

    public IslandMainMenu(NebrixSkyblock plugin, UUID viewerId) {
        super(plugin.getConfig().getString("gui.title-main", "§8Nebrix • Skyblock"), 27);
        this.plugin = plugin;
        this.viewerId = viewerId;
        this.titleMain = plugin.getConfig().getString("gui.title-main", "§8Nebrix • Skyblock");
        this.backText = plugin.getConfig().getString("gui.back-name", "&7« Back");
    }

    @Override
    public void draw(Player viewer) {
        inv.clear();
        boolean hasIsland = plugin.islands().hasIsland(viewerId);

        ItemStack pane = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, pane);

        if (!hasIsland) {
            set(11, new ItemBuilder(Material.GRASS_BLOCK).name("&aCreate Island")
                    .lore(List.of("&7Start your journey in the living sky.", "", "&eClick to create")).build());
            set(15, new ItemBuilder(Material.BOOK).name("&6About Nebrix Skyblock")
                    .lore(List.of("&7Islands evolve. The sky lives.", "&7Build, automate, and ascend.")).build());
            return;
        }

        set(6, new ItemBuilder(Material.BOOKSHELF).name("&6Manage Island")
                .lore(List.of("&7Upgrades, boosters, missions, bank, perms, top")).build());

        Island is = plugin.islands().getIsland(viewerId);

        set(10, new ItemBuilder(Material.ENDER_PEARL).name("&aTeleport Home")
                .lore(List.of("&7Warp to your island home.", "", "&eClick to teleport")).build());
        set(12, new ItemBuilder(Material.RED_BED).name("&eSet Home")
                .lore(List.of("&7Set your island home to your", "&7current position.", "", "&eClick to set")).build());
        set(14, new ItemBuilder(Material.COMPASS).name("&bIslands Spawn")
                .lore(List.of("&7Go to the Nebrix islands spawn.")).build());
        set(16, new ItemBuilder(Material.PLAYER_HEAD).name("&dMembers & Invites")
                .lore(List.of("&7Manage members, send invites,", "&7accept pending invites.")).build());

        if (viewer.hasPermission("nebrix.admin")) {
            set(4, new ItemBuilder(Material.NETHER_STAR).name("&cAdmin")
                    .lore(List.of("&7Admin tools: TP, radius, regen")).build());
        }

        set(22, new ItemBuilder(Material.OAK_SAPLING).name("&2Island Info")
                .lore(List.of(
                        "&7Owner: &f" + is.ownerName(),
                        "&7Center: &f" + is.center().getBlockX() + ", " + is.center().getBlockZ(),
                        "&7Radius: &f" + is.radius())).build());
    }

    @Override
    public void onClick(Player p, int slot, ItemStack clicked, ClickType type) {
        boolean hasIsland = plugin.islands().hasIsland(viewerId);
        if (!hasIsland) {
            if (slot == 11) {
                Island island = plugin.islands().createIsland(p.getUniqueId(), p.getName());
                if (island == null) { p.sendMessage("§cFailed to create island."); return; }
                plugin.islands().teleportHome(p);
                p.sendMessage("§aIsland created!");
                draw(p); p.updateInventory();
            }
            return;
        }

        switch (slot) {
            case 4 -> { if (p.hasPermission("nebrix.admin")) new IslandAdminMenu(plugin, viewerId, this).open(p); }
            case 6 -> new com.chunksmith.nebrixSkyblock.ui.menus.ManageMenu(plugin, this).open(p);
            case 10 -> plugin.islands().teleportHome(p);
            case 12 -> {
                Island is = plugin.islands().getIsland(p.getUniqueId());
                if (is == null || !is.contains(p.getLocation())) {
                    p.sendMessage("§cYou must be on your island to set home.");
                    return;
                }
                plugin.islands().setHome(p, p.getLocation());
                p.sendMessage("§aIsland home set.");
            }
            case 14 -> {
                World w = Bukkit.getWorld(plugin.overworld()); // <-- fixed here
                if (w != null) p.teleport(w.getSpawnLocation());
            }
            case 16 -> new InvitesMenu(plugin, viewerId, this).open(p);
            default -> {}
        }
    }
}
