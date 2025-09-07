package com.chunksmith.nebrixSkyblock.island;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IslandManager {
    private final NebrixSkyblock plugin;
    private final Map<UUID, Island> byOwner = new HashMap<>();
    private final Map<UUID, UUID> memberToOwner = new HashMap<>();
    private final File dataFile;
    private FileConfiguration data;

    private final String overworld;
    private final int baseY;
    private final int spacing;
    private final int defaultRadius;
    private final int maxMembers;

    private final IslandGrid grid;
    private int nextIndex = 0;

    private final Queue<Island> saveQueue = new ConcurrentLinkedQueue<>();
    private int saveTaskId = -1;

    public IslandManager(NebrixSkyblock plugin) {
        this.plugin = plugin;
        this.overworld = plugin.overworld();
        this.baseY = plugin.getConfig().getInt("world.base-y", 64);
        this.spacing = plugin.getConfig().getInt("islands.spacing", 256);
        this.defaultRadius = plugin.getConfig().getInt("islands.radius", 64);
        this.maxMembers = plugin.getConfig().getInt("islands.max-members", 5);

        this.dataFile = new File(plugin.getDataFolder(), "islands.yml");
        if (!dataFile.exists()) try { dataFile.getParentFile().mkdirs(); dataFile.createNewFile(); } catch (IOException ignored) {}
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        this.grid = new IslandGrid(Bukkit.getWorld(overworld), spacing, baseY);

        loadAll();
        startSaver();
    }

    public boolean hasIsland(UUID owner) { return byOwner.containsKey(owner); }
    public Island getIsland(UUID owner) { return byOwner.get(owner); }
    public boolean isMemberOfAny(UUID player) { return memberToOwner.containsKey(player) || byOwner.containsKey(player); }
    public int maxMembers() { return maxMembers; }

    public Island islandAt(Location loc) {
        for (Island is : byOwner.values()) if (is.contains(loc)) return is;
        return null;
    }

    public Island createIsland(UUID owner, String ownerName) {
        if (byOwner.containsKey(owner)) return byOwner.get(owner);
        World w = Bukkit.getWorld(overworld);
        if (w == null) return null;

        IslandGrid alloc = new IslandGrid(w, spacing, baseY);
        for (int i = 0; i <= nextIndex; i++) alloc.next();
        Location center = alloc.next();
        nextIndex += 2;

        DefaultIslandSchematic.place(center);
        Island island = new Island(owner, ownerName, center, defaultRadius);
        byOwner.put(owner, island);
        enqueueSave(island); saveMeta();
        return island;
    }

    public void addMember(UUID owner, UUID member) {
        Island is = byOwner.get(owner);
        if (is == null) return;
        if (is.members().size() >= maxMembers) return;
        is.members().add(member);
        is.roles().put(member, Island.Role.BUILDER);
        memberToOwner.put(member, owner);
        enqueueSave(is);
    }

    public UUID islandOwnerOf(UUID member) {
        if (byOwner.containsKey(member)) return member;
        return memberToOwner.get(member);
    }

    public void teleportHome(Player p) {
        Island is = byOwner.get(p.getUniqueId());
        if (is == null) {
            UUID owner = memberToOwner.get(p.getUniqueId());
            if (owner != null) is = byOwner.get(owner);
        }
        if (is == null) return;
        Location home = is.home();
        home.getWorld().getChunkAt(home).load(true);
        p.teleport(home);
    }

    public void setHome(Player p, Location loc) {
        Island is = byOwner.get(p.getUniqueId());
        if (is == null || !is.contains(loc)) return;
        is.setHome(loc);
        enqueueSave(is);
    }

    // ---- value / limits helpers ----

    public void onBlockPlaced(Island is, Material m) {
        // limits
        Map<String,Integer> limits = getLimits();
        Integer max = limits.get(m.name());
        if (max != null) {
            int now = is.blockCounts().getOrDefault(m.name(), 0) + 1;
            is.blockCounts().put(m.name(), now);
        }
        // value
        long v = blockValue(m);
        if (v > 0) is.addValue(v);
        enqueueSave(is);
    }

    public void onBlockBroken(Island is, Material m) {
        Integer now = is.blockCounts().getOrDefault(m.name(), 0);
        if (now > 0) is.blockCounts().put(m.name(), now - 1);
        long v = blockValue(m);
        if (v > 0) is.addValue(-v);
        enqueueSave(is);
    }

    public boolean isOverLimit(Island is, Material m) {
        Integer max = getLimits().get(m.name());
        if (max == null) return false;
        int now = is.blockCounts().getOrDefault(m.name(), 0);
        return now >= max;
    }

    private Map<String,Integer> getLimits() {
        Map<String,Integer> res = new HashMap<>();
        for (String k : plugin.getConfig().getConfigurationSection("islands.block-limits").getKeys(false)) {
            res.put(k, plugin.getConfig().getInt("islands.block-limits."+k));
        }
        return res;
    }
    private long blockValue(Material m) {
        if (!plugin.getConfig().isInt("islands.block-values."+m.name())) return 0;
        return plugin.getConfig().getInt("islands.block-values."+m.name());
    }

    public Collection<Island> exportAll() { return Collections.unmodifiableCollection(byOwner.values()); }


    // ---- leveling / unlocks ----

    public int levelForXp(long xp) {
        List<Integer> t = plugin.getConfig().getIntegerList("levels.thresholds");
        int lvl = 1;
        for (int i = 0; i < t.size(); i++) if (xp >= t.get(i)) lvl = i + 1;
        return Math.max(lvl, 1);
    }

    public void addXp(Island is, long xp) {
        int newLevel = levelForXp(is.xp() + xp);
        is.addXp(xp, newLevel);
        int gateNether = plugin.getConfig().getInt("levels.unlocks.nether-level", 3);
        int gateEnd = plugin.getConfig().getInt("levels.unlocks.end-level", 6);
        if (!is.netherUnlocked() && is.level() >= gateNether) is.setNetherUnlocked(true);
        if (!is.endUnlocked() && is.level() >= gateEnd) is.setEndUnlocked(true);
        enqueueSave(is);
    }

    // ---- bank / crystals ----

    public boolean tryPay(Island is, long crystals, long coins) {
        if (is.crystals() < crystals || is.bankCoins() < coins) return false;
        is.addCrystals(-crystals);
        is.addBankCoins(-coins);
        enqueueSave(is);
        return true;
    }

    // ---- save/load ----

    private void loadAll() {
        World w = Bukkit.getWorld(overworld);
        nextIndex = data.getInt("meta.nextIndex", 0);
        if (!data.isConfigurationSection("islands")) return;
        for (String key : data.getConfigurationSection("islands").getKeys(false)) {
            UUID owner = UUID.fromString(key);
            String ownerName = data.getString("islands."+key+".ownerName", "unknown");
            int cx = data.getInt("islands."+key+".center.x");
            int cy = data.getInt("islands."+key+".center.y");
            int cz = data.getInt("islands."+key+".center.z");
            int radius = data.getInt("islands."+key+".radius", defaultRadius);
            Location center = new Location(w, cx + 0.5, cy, cz + 0.5);
            Island island = new Island(owner, ownerName, center, radius);

            island.addBankCoins(data.getLong("islands."+key+".bank", 0));
            island.addCrystals(data.getLong("islands."+key+".crystals", 0));
            long xp = data.getLong("islands."+key+".xp", 0);
            island.addXp(0, levelForXp(xp));
            island.addValue(data.getLong("islands."+key+".value", 0));
            island.setGeneratorTier(data.getString("islands."+key+".generatorTier","T1"));
            island.setNetherUnlocked(data.getBoolean("islands."+key+".nether", false));
            island.setEndUnlocked(data.getBoolean("islands."+key+".end", false));

            for (String m : data.getStringList("islands."+key+".members")) {
                UUID mu = UUID.fromString(m);
                island.members().add(mu);
                island.roles().put(mu, Island.Role.BUILDER);
                memberToOwner.put(mu, owner);
            }
            if (data.isConfigurationSection("islands."+key+".blocks")) {
                for (String mk : data.getConfigurationSection("islands."+key+".blocks").getKeys(false)) {
                    island.blockCounts().put(mk, data.getInt("islands."+key+".blocks."+mk));
                }
            }
            byOwner.put(owner, island);
        }
    }

    private void saveIsland(Island is) {
        String base = "islands."+is.ownerId();
        data.set(base+".ownerName", is.ownerName());
        data.set(base+".center.x", is.center().getBlockX());
        data.set(base+".center.y", is.center().getBlockY());
        data.set(base+".center.z", is.center().getBlockZ());
        data.set(base+".radius", is.radius());
        data.set(base+".bank", is.bankCoins());
        data.set(base+".crystals", is.crystals());
        data.set(base+".value", is.value());
        data.set(base+".xp", is.xp());
        data.set(base+".level", is.level());
        data.set(base+".generatorTier", is.generatorTier());
        data.set(base+".nether", is.netherUnlocked());
        data.set(base+".end", is.endUnlocked());
        List<String> members = new ArrayList<>();
        for (UUID m : is.members()) members.add(m.toString());
        data.set(base+".members", members);
        for (Map.Entry<String,Integer> e : is.blockCounts().entrySet()) {
            data.set(base+".blocks."+e.getKey(), e.getValue());
        }
        try { data.save(dataFile); } catch (IOException e) { plugin.getLogger().warning("Save failed: "+e.getMessage()); }
    }
    private void saveMeta() {
        data.set("meta.nextIndex", nextIndex);
        try { data.save(dataFile); } catch (IOException ignored) {}
    }
    private void enqueueSave(Island is) { saveQueue.add(is); }
    private void startSaver() {
        saveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            Island is; int cap = 0;
            while ((is = saveQueue.poll()) != null && cap++ < 10) saveIsland(is);
        }, 40L, 40L);
    }
    public void shutdown() {
        if (saveTaskId != -1) Bukkit.getScheduler().cancelTask(saveTaskId);
        Island is; while ((is = saveQueue.poll()) != null) saveIsland(is);
        saveMeta();
    }
}
