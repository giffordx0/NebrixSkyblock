package com.chunksmith.nebrixSkyblock.command;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import com.chunksmith.nebrixSkyblock.island.IslandRole;
import com.chunksmith.nebrixSkyblock.invites.Invite;
import com.chunksmith.nebrixSkyblock.ui.menus.IslandMainMenu;
import com.chunksmith.nebrixSkyblock.util.Text;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Handles /island command with subcommands. */
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

        if (args.length == 0) {
            // Open main menu
            new IslandMainMenu(plugin).open(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create" -> handleCreate(player);
            case "delete" -> handleDelete(player);
            case "home", "tp" -> handleHome(player);
            case "invite" -> handleInvite(player, args);
            case "accept" -> handleAccept(player);
            case "deny", "decline" -> handleDeny(player);
            case "kick" -> handleKick(player, args);
            case "leave" -> handleLeave(player);
            case "info" -> handleInfo(player, args);
            case "sethome" -> handleSetHome(player);
            case "reload" -> handleReload(player, sender);
            default -> {
                sendHelp(player);
                return true;
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList(
                    "create", "delete", "home", "invite", "accept", "deny", "kick", "leave", "info", "sethome", "reload"
            );
            return subCommands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if ("invite".equals(args[0].toLowerCase()) || "kick".equals(args[0].toLowerCase())) {
                // Return online player names
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }

    private void handleCreate(Player player) {
        try {
            if (plugin.islands().hasIsland(player.getUniqueId())) {
                player.sendMessage(Text.mini("<red>You already have an island! Use /island delete to remove it first.</red>"));
                return;
            }

            Island island = plugin.islands().createIsland(player.getUniqueId());
            plugin.islands().teleportToIsland(player, island);
            player.sendMessage(Text.mini("<green>Island created successfully! Welcome to your new home!</green>"));

        } catch (Exception e) {
            player.sendMessage(Text.mini("<red>Failed to create island: " + e.getMessage() + "</red>"));
            plugin.getLogger().severe("Failed to create island for " + player.getName() + ": " + e.getMessage());
        }
    }

    private void handleDelete(Player player) {
        Island island = plugin.islands().getByPlayer(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Text.mini("<red>You don't have an island!</red>"));
            return;
        }

        if (!island.owner().equals(player.getUniqueId())) {
            player.sendMessage(Text.mini("<red>Only the island owner can delete the island!</red>"));
            return;
        }

        player.sendMessage(Text.mini("<yellow>Use the island menu (/island) to safely delete your island with confirmation.</yellow>"));
    }

    private void handleHome(Player player) {
        Island island = plugin.islands().getByPlayer(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Text.mini("<red>You don't have an island! Use /island create to make one.</red>"));
            return;
        }

        plugin.islands().teleportToIsland(player, island);
        player.sendMessage(Text.mini("<green>Welcome back to your island!</green>"));
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mini("<red>Usage: /island invite <player></red>"));
            return;
        }

        Island island = plugin.islands().getByPlayer(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Text.mini("<red>You don't have an island!</red>"));
            return;
        }

        var member = island.members().get(player.getUniqueId());
        if (member == null || (member.role() != IslandRole.OWNER && member.role() != IslandRole.OFFICER)) {
            player.sendMessage(Text.mini("<red>You don't have permission to invite players!</red>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Text.mini("<red>Player not found or not online!</red>"));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(Text.mini("<red>You cannot invite yourself!</red>"));
            return;
        }

        if (plugin.islands().hasIsland(target.getUniqueId())) {
            player.sendMessage(Text.mini("<red>That player already has an island!</red>"));
            return;
        }

        // Check if already a member
        if (island.members().containsKey(target.getUniqueId())) {
            player.sendMessage(Text.mini("<red>That player is already a member of your island!</red>"));
            return;
        }

        // Check member limit
        int maxMembers = getMaxMembers(island);
        if (island.members().size() >= maxMembers) {
            player.sendMessage(Text.mini("<red>Your island is full! Upgrade to allow more members.</red>"));
            return;
        }

        // Send invite
        Invite invite = new Invite(player.getUniqueId(), target.getUniqueId());
        plugin.invites().add(invite);

        player.sendMessage(Text.mini("<green>Invitation sent to " + target.getName() + "!</green>"));
        target.sendMessage(Text.mini("<green>" + player.getName() + " has invited you to their island!</green>"));
        target.sendMessage(Text.mini("<yellow>Use /island accept to join or /island deny to decline.</yellow>"));
    }

    private void handleAccept(Player player) {
        Invite invite = findInviteFor(player.getUniqueId());
        if (invite == null) {
            player.sendMessage(Text.mini("<red>You don't have any pending invitations!</red>"));
            return;
        }

        if (plugin.islands().hasIsland(player.getUniqueId())) {
            player.sendMessage(Text.mini("<red>You already have an island! Leave it first to join another.</red>"));
            return;
        }

        Player inviter = Bukkit.getPlayer(invite.from());
        Island island = inviter != null ? plugin.islands().getByPlayer(inviter.getUniqueId()) : null;

        if (island == null) {
            player.sendMessage(Text.mini("<red>The island invitation is no longer valid!</red>"));
            plugin.invites().remove(invite);
            return;
        }

        // Check member limit again
        int maxMembers = getMaxMembers(island);
        if (island.members().size() >= maxMembers) {
            player.sendMessage(Text.mini("<red>The island is now full!</red>"));
            plugin.invites().remove(invite);
            return;
        }

        // Add player to island
        plugin.islands().addMember(island, player.getUniqueId(), IslandRole.MEMBER);
        plugin.invites().remove(invite);

        player.sendMessage(Text.mini("<green>You have joined " + (inviter != null ? inviter.getName() : "the") + "'s island!</green>"));

        if (inviter != null) {
            inviter.sendMessage(Text.mini("<green>" + player.getName() + " has joined your island!</green>"));
        }

        // Teleport to island
        plugin.islands().teleportToIsland(player, island);
    }

    private void handleDeny(Player player) {
        Invite invite = findInviteFor(player.getUniqueId());
        if (invite == null) {
            player.sendMessage(Text.mini("<red>You don't have any pending invitations!</red>"));
            return;
        }

        Player inviter = Bukkit.getPlayer(invite.from());
        plugin.invites().remove(invite);

        player.sendMessage(Text.mini("<yellow>Island invitation declined.</yellow>"));

        if (inviter != null) {
            inviter.sendMessage(Text.mini("<yellow>" + player.getName() + " declined your island invitation.</yellow>"));
        }
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mini("<red>Usage: /island kick <player></red>"));
            return;
        }

        Island island = plugin.islands().getByPlayer(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Text.mini("<red>You don't have an island!</red>"));
            return;
        }

        var member = island.members().get(player.getUniqueId());
        if (member == null || (member.role() != IslandRole.OWNER && member.role() != IslandRole.OFFICER)) {
            player.sendMessage(Text.mini("<red>You don't have permission to kick players!</red>"));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!island.members().containsKey(target.getUniqueId())) {
            player.sendMessage(Text.mini("<red>That player is not a member of your island!</red>"));
            return;
        }

        if (target.getUniqueId().equals(island.owner())) {
            player.sendMessage(Text.mini("<red>You cannot kick the island owner!</red>"));
            return;
        }

        var targetMember = island.members().get(target.getUniqueId());
        if (member.role() == IslandRole.OFFICER && targetMember.role() == IslandRole.OFFICER) {
            player.sendMessage(Text.mini("<red>Officers cannot kick other officers!</red>"));
            return;
        }

        try {
            plugin.islands().removeMember(island, target.getUniqueId());

            String targetName = target.getName() != null ? target.getName() : "Unknown";
            player.sendMessage(Text.mini("<red>" + targetName + " has been kicked from the island.</red>"));

            if (target.isOnline()) {
                ((Player) target).sendMessage(Text.mini("<red>You have been kicked from the island.</red>"));
            }

        } catch (Exception e) {
            player.sendMessage(Text.mini("<red>Failed to kick player: " + e.getMessage() + "</red>"));
        }
    }

    private void handleLeave(Player player) {
        Island island = plugin.islands().getByPlayer(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Text.mini("<red>You're not a member of any island!</red>"));
            return;
        }

        if (island.owner().equals(player.getUniqueId())) {
            player.sendMessage(Text.mini("<red>You cannot leave your own island! Use /island delete to remove it.</red>"));
            return;
        }

        try {
            plugin.islands().removeMember(island, player.getUniqueId());
            player.sendMessage(Text.mini("<yellow>You have left the island.</yellow>"));

            // Notify island owner
            Player owner = Bukkit.getPlayer(island.owner());
            if (owner != null) {
                owner.sendMessage(Text.mini("<yellow>" + player.getName() + " has left your island.</yellow>"));
            }

        } catch (Exception e) {
            player.sendMessage(Text.mini("<red>Failed to leave island: " + e.getMessage() + "</red>"));
        }
    }

    private void handleInfo(Player player, String[] args) {
        Island island;

        if (args.length > 1) {
            // Get info about specific player's island
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            island = plugin.islands().getByPlayer(target.getUniqueId());

            if (island == null) {
                player.sendMessage(Text.mini("<red>That player doesn't have an island!</red>"));
                return;
            }
        } else {
            // Get info about player's own island
            island = plugin.islands().getByPlayer(player.getUniqueId());

            if (island == null) {
                player.sendMessage(Text.mini("<red>You don't have an island!</red>"));
                return;
            }
        }

        // Display island information
        OfflinePlayer owner = Bukkit.getOfflinePlayer(island.owner());
        String ownerName = owner.getName() != null ? owner.getName() : "Unknown";

        player.sendMessage(Text.mini(""));
        player.sendMessage(Text.mini("<green>===== Island Information =====</green>"));
        player.sendMessage(Text.mini("<gray>Owner: <white>" + ownerName + "</white></gray>"));
        player.sendMessage(Text.mini("<gray>Members: <white>" + island.members().size() + "/" + getMaxMembers(island) + "</white></gray>"));
        player.sendMessage(Text.mini("<gray>Level: <white>" + island.upgrades().radiusLevel() + "</white></gray>"));
        player.sendMessage(Text.mini("<gray>Generator Tier: <white>" + island.upgrades().generatorTier() + "</white></gray>"));
        player.sendMessage(Text.mini("<gray>PvP: " + (island.settings().pvp() ? "<green>Enabled" : "<red>Disabled") + "</gray>"));
        player.sendMessage(Text.mini("<gray>Bank: <gold>" + island.bank().coins() + " coins</gold>, <aqua>" + island.bank().crystals() + " crystals</aqua></gray>"));
        player.sendMessage(Text.mini(""));
    }

    private void handleSetHome(Player player) {
        Island island = plugin.islands().getByPlayer(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Text.mini("<red>You don't have an island!</red>"));
            return;
        }

        if (!plugin.islands().isInIslandRadius(island, player.getLocation())) {
            player.sendMessage(Text.mini("<red>You must be on your island to set the home location!</red>"));
            return;
        }

        // TODO: Implement custom home location setting
        player.sendMessage(Text.mini("<yellow>Custom home locations coming soon!</yellow>"));
    }

    private void handleReload(Player player, CommandSender sender) {
        if (!player.hasPermission("nebrixskyblock.admin")) {
            player.sendMessage(Text.mini("<red>You don't have permission to use this command!</red>"));
            return;
        }

        plugin.reloadConfig();
        sender.sendMessage(Text.mini("<green>Configuration reloaded!</green>"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(Text.mini(""));
        player.sendMessage(Text.mini("<green>===== Island Commands =====</green>"));
        player.sendMessage(Text.mini("<yellow>/island</yellow> <gray>- Open island menu</gray>"));
        player.sendMessage(Text.mini("<yellow>/island create</yellow> <gray>- Create a new island</gray>"));
        player.sendMessage(Text.mini("<yellow>/island home</yellow> <gray>- Teleport to your island</gray>"));
        player.sendMessage(Text.mini("<yellow>/island invite <player></yellow> <gray>- Invite a player</gray>"));
        player.sendMessage(Text.mini("<yellow>/island accept</yellow> <gray>- Accept an invitation</gray>"));
        player.sendMessage(Text.mini("<yellow>/island deny</yellow> <gray>- Decline an invitation</gray>"));
        player.sendMessage(Text.mini("<yellow>/island kick <player></yellow> <gray>- Remove a member</gray>"));
        player.sendMessage(Text.mini("<yellow>/island leave</yellow> <gray>- Leave an island</gray>"));
        player.sendMessage(Text.mini("<yellow>/island info [player]</yellow> <gray>- View island information</gray>"));
        player.sendMessage(Text.mini("<yellow>/island delete</yellow> <gray>- Delete your island</gray>"));
        player.sendMessage(Text.mini(""));
    }

    private int getMaxMembers(Island island) {
        int baseMembers = plugin.getConfig().getInt("islands.base-members", 1);
        int membersPerLevel = plugin.getConfig().getInt("islands.members-per-level", 1);
        return baseMembers + (island.upgrades().memberLevel() - 1) * membersPerLevel;
    }

    private Invite findInviteFor(UUID playerId) {
        return plugin.invites().getInvitesFor(playerId).stream().findFirst().orElse(null);
    }
}
