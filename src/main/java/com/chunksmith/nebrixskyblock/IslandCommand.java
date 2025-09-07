package com.chunksmith.nebrixskyblock;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Simple placeholder command that will eventually open the skyblock GUI.
 */
public final class IslandCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            player.sendMessage("Skyblock features are under construction.");
        } else {
            sender.sendMessage("This command can only be run by a player.");
        }
        return true;
    }
}

