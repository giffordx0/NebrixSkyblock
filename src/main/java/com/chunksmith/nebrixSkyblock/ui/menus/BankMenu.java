package com.chunksmith.nebrixSkyblock.ui.menus;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.ui.ItemBuilder;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class BankMenu extends Menu {
    private final NebrixSkyblock plugin;
    private final Menu parent;

    public BankMenu(NebrixSkyblock plugin, Menu parent) {
        super(plugin.getConfig().getString("gui.title-bank","§8Nebrix • Bank"), 27);
        this.plugin = plugin; this.parent = parent;
    }

    @Override
    public void draw(Player viewer) {
        inv.clear();
        UUID owner = plugin.islands().islandOwnerOf(viewer.getUniqueId());
        Island is = plugin.islands().getIsland(owner);
        set(11, new ItemBuilder(Material.GOLD_INGOT).name("&6Deposit 10k").lore(List.of("&7Bank: &6"+is.bankCoins())).build());
        set(13, new ItemBuilder(Material.NETHER_STAR).name("&dCrystals: &f"+is.crystals()).build());
        set(15, new ItemBuilder(Material.HOPPER).name("&eWithdraw 10k").build());
        set(8, new ItemBuilder(Material.ARROW).name(plugin.getConfig().getString("gui.back-name","&7« Back")).build());
    }

    @Override
    public void onClick(Player p, int slot, ItemStack clicked, ClickType type) {
        UUID owner = plugin.islands().islandOwnerOf(p.getUniqueId());
        Island is = plugin.islands().getIsland(owner);
        switch (slot) {
            case 11 -> { is.addBankCoins(10000); p.sendMessage("§6Deposited 10,000."); draw(p); }
            case 15 -> { if (is.trySpendBank(10000)) p.sendMessage("§eWithdrew 10,000."); else p.sendMessage("§cNot enough bank."); draw(p); }
            case 8 -> parent.open(p);
            default -> {}
        }
    }
}
