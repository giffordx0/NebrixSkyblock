package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;

public class PlayerSelectMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final UUID viewerId;
    private final Consumer<UUID> onSelect;

    public PlayerSelectMenu(NebrixSkyblock plugin, UUID viewerId, Consumer<UUID> onSelect) {
        super(plugin.getConfig().getString("gui.title-select-player", "§8Nebrix • Select Player"), 54);
        this.plugin = plugin;
        this.viewerId = viewerId;
        this.onSelect = onSelect;
    }

    @Override
    public void draw(Player viewer) {
        inv.clear();
        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(viewerId)) continue;
            inv.setItem(slot++, new ItemBuilder(Material.PLAYER_HEAD).name("&f" + online.getName()).build());
            if (slot >= inv.getSize()) break;
        }
        if (slot == 0) inv.setItem(22, new ItemBuilder(Material.BARRIER).name("&7No other players online.").build());
    }

    @Override
    public void onClick(Player p, int slot, ItemStack clicked, ClickType type) {
        ItemStack it = inv.getItem(slot);
        if (it == null || !it.hasItemMeta() || it.getItemMeta().getDisplayName() == null) return;
        String raw = it.getItemMeta().getDisplayName().replace("§f", "").replace("§r", "");
        Player target = Bukkit.getPlayerExact(raw);
        if (target == null) return;
        onSelect.accept(target.getUniqueId());
        p.closeInventory();
    }
}