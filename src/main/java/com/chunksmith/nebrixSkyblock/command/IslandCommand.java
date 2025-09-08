package com.chunksmith.nebrixSkyblock.command;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.invites.Invite;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.island.IslandMember;
import com.chunksmith.nebrixSkyblock.island.IslandRole;
import com.chunksmith.nebrixSkyblock.ui.menus.IslandMainMenu;
import com.chunksmith.nebrixSkyblock.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/** Handles /island command and subcommands. */
public final class IslandCommand implements CommandExecutor, TabCompleter {
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
    if (args.length == 0 || args[0].equalsIgnoreCase("menu")) {
      plugin.menus().open(player, new IslandMainMenu(plugin));
      return true;
    }
    String sub = args[0].toLowerCase(Locale.ROOT);
    switch (sub) {
      case "create" -> handleCreate(player);
      case "home" -> handleHome(player);
      case "invite" -> handleInvite(player, args);
      case "accept" -> handleAccept(player);
      case "decline" -> handleDecline(player);
      case "kick" -> handleKick(player, args);
      case "help" -> player.sendMessage(
          Text.mini("<yellow>/island [menu|create|home|invite|accept|decline|kick]</yellow>"));
      default -> player.sendMessage(Text.mini("<red>Unknown subcommand.</red>"));
    }
    return true;
  }

  private void handleCreate(Player player) {
    if (plugin.islands().byPlayer(player.getUniqueId()) != null) {
      player.sendMessage(Text.mini("<red>You already have an island.</red>"));
      return;
    }
    Island island = plugin.islands().createIsland(player.getUniqueId());
    plugin.storage().saveIsland(island);
    player.sendMessage(Text.mini("<green>Island created!</green>"));
    plugin.menus().open(player, new IslandMainMenu(plugin));
  }

  private void handleHome(Player player) {
    Island island = plugin.islands().byPlayer(player.getUniqueId());
    if (island == null) {
      player.sendMessage(Text.mini("<red>You don't have an island.</red>"));
      return;
    }
    plugin.islands().teleportHome(player, island);
    player.sendMessage(Text.mini("<yellow>Teleported home.</yellow>"));
  }

  private void handleInvite(Player player, String[] args) {
    if (args.length < 2) {
      player.sendMessage(Text.mini("<red>/island invite <player></red>"));
      return;
    }
    Island island = plugin.islands().byPlayer(player.getUniqueId());
    if (island == null) {
      player.sendMessage(Text.mini("<red>You don't have an island.</red>"));
      return;
    }
    IslandMember member = island.members().get(player.getUniqueId());
    if (member.role() == IslandRole.MEMBER) {
      player.sendMessage(Text.mini("<red>No permission.</red>"));
      return;
    }
    Player target = Bukkit.getPlayer(args[1]);
    if (target == null) {
      player.sendMessage(Text.mini("<red>Player not found.</red>"));
      return;
    }
    plugin.invites().addInvite(island.id(), player.getUniqueId(), target.getUniqueId());
    player.sendMessage(Text.mini("<green>Invite sent to " + target.getName() + "</green>"));
    target.sendMessage(Text.mini("<yellow>You have been invited to an island. /island accept</yellow>"));
  }

  private void handleAccept(Player player) {
    Invite invite = plugin.invites().getInvite(player.getUniqueId());
    if (invite == null) {
      player.sendMessage(Text.mini("<red>No pending invite.</red>"));
      return;
    }
    Island island = plugin.islands().get(invite.islandId());
    if (island == null) {
      player.sendMessage(Text.mini("<red>Island no longer exists.</red>"));
      plugin.invites().removeInvite(player.getUniqueId());
      return;
    }
    island.members().put(player.getUniqueId(), new IslandMember(player.getUniqueId(), IslandRole.MEMBER));
    plugin.invites().removeInvite(player.getUniqueId());
    plugin.storage().saveIsland(island);
    player.sendMessage(Text.mini("<green>Joined island.</green>"));
  }

  private void handleDecline(Player player) {
    Invite invite = plugin.invites().getInvite(player.getUniqueId());
    if (invite == null) {
      player.sendMessage(Text.mini("<red>No pending invite.</red>"));
      return;
    }
    plugin.invites().removeInvite(player.getUniqueId());
    player.sendMessage(Text.mini("<yellow>Invite declined.</yellow>"));
  }

  private void handleKick(Player player, String[] args) {
    if (args.length < 2) {
      player.sendMessage(Text.mini("<red>/island kick <player></red>"));
      return;
    }
    Island island = plugin.islands().byPlayer(player.getUniqueId());
    if (island == null) {
      player.sendMessage(Text.mini("<red>You don't have an island.</red>"));
      return;
    }
    IslandMember member = island.members().get(player.getUniqueId());
    if (member.role() == IslandRole.MEMBER) {
      player.sendMessage(Text.mini("<red>No permission.</red>"));
      return;
    }
    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
    UUID targetId = target.getUniqueId();
    IslandMember toKick = island.members().get(targetId);
    if (toKick == null || targetId.equals(player.getUniqueId())) {
      player.sendMessage(Text.mini("<red>That player is not on your island.</red>"));
      return;
    }
    if (toKick.role() == IslandRole.OWNER) {
      player.sendMessage(Text.mini("<red>You can't kick the owner.</red>"));
      return;
    }
    if (member.role() == IslandRole.OFFICER && toKick.role() != IslandRole.MEMBER) {
      player.sendMessage(Text.mini("<red>You can't kick that player.</red>"));
      return;
    }
    island.members().remove(targetId);
    plugin.storage().saveIsland(island);
    player.sendMessage(Text.mini("<yellow>Kicked " + target.getName() + ".</yellow>"));
    if (target.isOnline()) {
      target.getPlayer().sendMessage(Text.mini("<red>You were removed from the island.</red>"));
    }
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String alias, String[] args) {
    if (args.length == 1) {
      return List.of("menu", "create", "home", "invite", "accept", "decline", "kick", "help")
          .stream()
          .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT)))
          .toList();
    }
    if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("kick"))) {
      String prefix = args[1].toLowerCase(Locale.ROOT);
      List<String> names = new ArrayList<>();
      for (Player p : Bukkit.getOnlinePlayers()) {
        if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
          names.add(p.getName());
        }
      }
      return names;
    }
    return List.of();
  }
}
