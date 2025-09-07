package com.chunksmith.nebrixSkyblock.command;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.ui.menus.IslandMainMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Handles /island command, opening main menu. */
public final class IslandCommand implements CommandExecutor {
  private final NebrixSkyblock plugin;

  public IslandCommand(NebrixSkyblock plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Players only."));
      return true;
    }
    new IslandMainMenu(plugin, player.getUniqueId()).open(player);
    return true;
  }
}
