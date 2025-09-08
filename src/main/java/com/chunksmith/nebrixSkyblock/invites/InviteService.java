package com.chunksmith.nebrixSkyblock.invites;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InviteService {
    private final Set<Invite> invites = ConcurrentHashMap.newKeySet();
    private final Map<Invite, BukkitTask> expiryTasks = new ConcurrentHashMap<>();
    private final long INVITE_EXPIRE_TIME = 5 * 60 * 1000; // 5 minutes in milliseconds

    public void add(Invite invite) {
        // Remove any existing invite between these players
        removeInvite(invite.from(), invite.to());

        invites.add(invite);

        // Schedule expiry task
        BukkitTask task = Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("NebrixSkyblock"),
                () -> {
                    if (invites.remove(invite)) {
                        expiryTasks.remove(invite);

                        // Notify players of expiry
                        var inviter = Bukkit.getPlayer(invite.from());
                        var invitee = Bukkit.getPlayer(invite.to());

                        if (inviter != null) {
                            inviter.sendMessage("§cYour island invitation to " +
                                    (invitee != null ? invitee.getName() : "a player") + " has expired.");
                        }

                        if (invitee != null) {
                            invitee.sendMessage("§cThe island invitation from " +
                                    (inviter != null ? inviter.getName() : "a player") + " has expired.");
                        }
                    }
                },
                INVITE_EXPIRE_TIME / 50 // Convert to ticks (20 ticks per second)
        );

        expiryTasks.put(invite, task);
    }

    public boolean has(Invite invite) {
        return invites.contains(invite);
    }

    public void remove(Invite invite) {
        if (invites.remove(invite)) {
            BukkitTask task = expiryTasks.remove(invite);
            if (task != null) {
                task.cancel();
            }
        }
    }

    public void removeInvite(UUID from, UUID to) {
        Invite toRemove = new Invite(from, to);
        remove(toRemove);
    }

    public Set<Invite> getInvitesFor(UUID playerId) {
        Set<Invite> playerInvites = new HashSet<>();
        for (Invite invite : invites) {
            if (invite.to().equals(playerId)) {
                playerInvites.add(invite);
            }
        }
        return playerInvites;
    }

    public Set<Invite> getInvitesFrom(UUID playerId) {
        Set<Invite> playerInvites = new HashSet<>();
        for (Invite invite : invites) {
            if (invite.from().equals(playerId)) {
                playerInvites.add(invite);
            }
        }
        return playerInvites;
    }

    public void clearInvitesFor(UUID playerId) {
        Set<Invite> toRemove = new HashSet<>();

        // Find all invites involving this player
        for (Invite invite : invites) {
            if (invite.from().equals(playerId) || invite.to().equals(playerId)) {
                toRemove.add(invite);
            }
        }

        // Remove them
        toRemove.forEach(this::remove);
    }

    public int getInviteCount() {
        return invites.size();
    }

    public void cleanup() {
        // Cancel all expiry tasks
        expiryTasks.values().forEach(BukkitTask::cancel);
        expiryTasks.clear();
        invites.clear();
    }
}
