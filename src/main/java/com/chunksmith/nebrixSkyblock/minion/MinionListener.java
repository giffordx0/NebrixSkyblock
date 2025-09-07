package com.chunksmith.nebrixSkyblock.minion;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.ui.menus.MinionMenu;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class MinionListener implements Listener {
    private final NebrixSkyblock plugin;
    private final NamespacedKey eggKey;

    public MinionListener(NebrixSkyblock plugin) {
        this.plugin = plugin;
        this.eggKey = new NamespacedKey(plugin, "minion-egg");
    }

    // Right-click block with minion egg to place
    @EventHandler(ignoreCancelled = true)
    public void onPlace(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack it = e.getPlayer().getInventory().getItemInMainHand();
        if (it == null || !it.hasItemMeta()) return;

        PersistentDataContainer pdc = it.getItemMeta().getPersistentDataContainer();
        String kind = pdc.get(eggKey, PersistentDataType.STRING);
        if (kind == null) return; // not our egg

        Player p = e.getPlayer();
        Block b = e.getClickedBlock();
        if (b == null) return;

        // must be on island and have build perms
        Island is = plugin.islands().islandAt(b.getLocation());
        if (is == null || !is.canBuild(p.getUniqueId())) { p.sendMessage("§cYou must place this on your island."); return; }

        // place minion at block top center
        Location at = b.getLocation().add(0.5, 1.0, 0.5);
        Minion m = plugin.minions().placeMiner(is.ownerId(), p.getUniqueId(), at);
        p.sendMessage("§dMinion placed.");
        // consume one
        it.setAmount(it.getAmount()-1);
        e.setCancelled(true);
    }

    // Right-click its ArmorStand to open GUI
    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractAtEntityEvent e) {
        if (!(e.getRightClicked() instanceof ArmorStand as)) return;
        // find minion by stand id
        UUID sid = as.getUniqueId();
        for (Minion m : plugin.minions().allOwnedBy(plugin.islands().islandOwnerOf(e.getPlayer().getUniqueId()))) {
            if (sid.equals(m.standId())) {
                new MinionMenu(plugin, m).open(e.getPlayer());
                e.setCancelled(true);
                return;
            }
        }
        // if not found by owner, check globally (so members can open)
        for (Minion m : plugin.minions().allOwnedBy(plugin.islands().islandOwnerOf(e.getPlayer().getUniqueId()))) {
            if (sid.equals(m.standId())) {
                new MinionMenu(plugin, m).open(e.getPlayer());
                e.setCancelled(true);
                return;
            }
        }
    }

    // Utility to create an egg item (used by Admin GUI)
    public ItemStack makeEgg() {
        Material mat = Material.matchMaterial(plugin.getConfig().getString("minions.item.material","ZOMBIE_SPAWN_EGG"));
        ItemStack egg = new ItemStack(mat == null ? Material.ZOMBIE_SPAWN_EGG : mat, 1);
        var meta = egg.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("minions.item.name","&dNebrix Minion &7(Miner)")));
        meta.getPersistentDataContainer().set(eggKey, PersistentDataType.STRING, "miner");
        var lore = plugin.getConfig().getStringList("minions.item.lore");
        if (lore != null && !lore.isEmpty()) {
            java.util.List<String> out = new java.util.ArrayList<>();
            for (String s : lore) out.add(ChatColor.translateAlternateColorCodes('&', s));
            meta.setLore(out);
        }
        egg.setItemMeta(meta);
        return egg;
    }
}
