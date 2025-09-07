package com.chunksmith.nebrixSkyblock.commands;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.ui.menus.IslandMainMenu;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class IslandCommand implements CommandExecutor, TabCompleter {
    private final NebrixSkyblock plugin;
    public IslandCommand(NebrixSkyblock plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        new IslandMainMenu(plugin, p.getUniqueId()).open(p);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}