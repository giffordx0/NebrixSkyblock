package com.chunksmith.nebrixSkyblock.island;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class Island {
    public enum Role { OWNER, BUILDER, VISITOR }

    private final UUID ownerId;
    private final String ownerName;

    private final Set<UUID> members = new HashSet<>();
    private final Map<UUID, Role> roles = new HashMap<>();

    private Location center;
    private Location home;
    private int radius;

    // progression
    private long value = 0;     // block-based island value
    private long xp = 0;        // island XP (missions)
    private int level = 1;
    private long crystals = 0;  // currency for upgrades/boosters
    private long bankCoins = 0; // shared economy

    // upgrades/boosters state
    private String generatorTier = "T1";
    private long boosterExpireAt = 0L;
    private String activeBoosterId = null;

    // block counts for limits
    private final Map<String, Integer> blockCounts = new HashMap<>();

    // dimension unlocks
    private boolean netherUnlocked = false;
    private boolean endUnlocked = false;

    public Island(UUID ownerId, String ownerName, Location center, int radius) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.center = center;
        this.radius = radius;
        this.home = center.clone().add(0, 2, 0);
        roles.put(ownerId, Role.OWNER);
    }

    public UUID ownerId() { return ownerId; }
    public String ownerName() { return ownerName; }
    public Location center() { return center; }
    public Location home() { return home; }
    public int radius() { return radius; }

    public void setHome(Location home) { this.home = home; }
    public void setRadius(int r) { this.radius = r; }

    public Set<UUID> members() { return members; }
    public Map<UUID, Role> roles() { return roles; }

    public long value() { return value; }
    public void addValue(long v) { value = Math.max(0, value + v); }

    public long xp() { return xp; }
    public int level() { return level; }
    public void addXp(long v, int newLevel) { xp += v; level = Math.max(level, newLevel); }

    public long crystals() { return crystals; }
    public void addCrystals(long v) { crystals = Math.max(0, crystals + v); }
    public boolean trySpendCrystals(long v) { if (crystals >= v) { crystals -= v; return true; } return false; }

    public long bankCoins() { return bankCoins; }
    public void addBankCoins(long v) { bankCoins = Math.max(0, bankCoins + v); }
    public boolean trySpendBank(long v) { if (bankCoins >= v) { bankCoins -= v; return true; } return false; }

    public String generatorTier() { return generatorTier; }
    public void setGeneratorTier(String t) { generatorTier = t; }

    public void activateBooster(String id, long expireAt) { activeBoosterId = id; boosterExpireAt = expireAt; }
    public String activeBooster() { return activeBoosterId; }
    public long boosterExpireAt() { return boosterExpireAt; }

    public Map<String,Integer> blockCounts() { return blockCounts; }

    public boolean netherUnlocked() { return netherUnlocked; }
    public void setNetherUnlocked(boolean v) { netherUnlocked = v; }

    public boolean endUnlocked() { return endUnlocked; }
    public void setEndUnlocked(boolean v) { endUnlocked = v; }

    public boolean contains(Location loc) {
        if (loc == null || center == null) return false;
        World w = Bukkit.getWorld(loc.getWorld().getName());
        if (w == null || center.getWorld() == null) return false;
        if (!loc.getWorld().equals(center.getWorld())) return false;
        return Math.abs(loc.getBlockX() - center.getBlockX()) <= radius
                && Math.abs(loc.getBlockZ() - center.getBlockZ()) <= radius;
    }

    public boolean canBuild(UUID player) {
        if (player.equals(ownerId)) return true;
        return roles.getOrDefault(player, members.contains(player) ? Role.BUILDER : Role.VISITOR) != Role.VISITOR;
    }
}
