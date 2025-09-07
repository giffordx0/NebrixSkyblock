package com.chunksmith.nebrixSkyblock.minion;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import com.chunksmith.nebrixSkyblock.island.Island;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MinionManager {
    private final NebrixSkyblock plugin;
    private final Map<UUID, Minion> minions = new HashMap<>();
    private final File minionsFile;
    private FileConfiguration data;
    private int tickTask = -1;

    public MinionManager(NebrixSkyblock plugin) {
        this.plugin = plugin;
        this.minionsFile = new File(plugin.getDataFolder(), "minions.yml");
        if (!minionsFile.exists()) {
            try { minionsFile.getParentFile().mkdirs(); minionsFile.createNewFile(); } catch (IOException ignored) {}
        }
        this.data = YamlConfiguration.loadConfiguration(minionsFile);
        loadAll();
        startTicking();
    }

    public Collection<Minion> allOwnedBy(UUID islandOwner) {
        List<Minion> list = new ArrayList<>();
        for (Minion m : minions.values()) if (m.islandOwner().equals(islandOwner)) list.add(m);
        return list;
    }

    // --- create & remove ---

    public Minion placeMiner(UUID islandOwner, UUID placer, Location at) {
        Minion m = new Minion(UUID.randomUUID(), islandOwner, placer, "miner", at.clone());
        spawnStand(m);
        minions.put(m.id(), m);
        saveMinion(m);
        return m;
    }

    public boolean pickup(UUID id) {
        Minion m = minions.remove(id);
        if (m == null) return false;
        // remove stand
        if (m.standId() != null) {
            var ent = Bukkit.getEntity(m.standId());
            if (ent != null) ent.remove();
        }
        data.set("minions."+id.toString(), null);
        saveFile();
        return true;
    }

    private void spawnStand(Minion m) {
        World w = m.loc().getWorld();
        ArmorStand as = w.spawn(m.loc().clone().add(0.5, 0, 0.5), ArmorStand.class, stand -> {
            stand.setSmall(true);
            stand.setInvisible(false);
            stand.setCustomNameVisible(true);
            stand.setCustomName("§dMinion §7(Lv " + m.level() + ")");
            stand.setGravity(false);
            stand.setInvulnerable(true);
            stand.setBasePlate(false);
            stand.setArms(false);
        });
        m.setStandId(as.getUniqueId());
    }

    // --- ticking & production ---

    private void startTicking() {
        tickTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Minion m : minions.values()) {
                // update name and fuel
                if (m.standId() != null) {
                    var ent = Bukkit.getEntity(m.standId());
                    if (ent instanceof ArmorStand as) {
                        String fuel = m.fuelSeconds() > 0 ? "§a" + fmtTime(m.fuelSeconds()) : "§cNo Fuel";
                        as.setCustomName("§dMinion §7(Lv " + m.level() + ") §8• " + fuel);
                        if (as.getLocation().distanceSquared(m.loc()) > 0.2) as.teleport(m.loc()); // keep fixed
                    }
                }

                // fuel tick
                if (m.fuelSeconds() > 0) m.tickFuel();

                // production
                if (!"miner".equalsIgnoreCase(m.type())) continue;
                if (m.fuelSeconds() <= 0) continue;

                int baseTicks = plugin.getConfig().getInt("minions.types.miner.base-interval-ticks", 200);
                double mult = Math.pow(plugin.getConfig().getDouble("minions.types.miner.interval-per-level-mult", 0.92), (m.level()-1));
                int interval = Math.max(20, (int) Math.round(baseTicks * mult / m.fuelEfficiency()));

                // use world full time to decide if it's production tick
                long t = m.loc().getWorld().getFullTime();
                if (t % interval != 0) continue;

                Material out = rollMinerOutput(m);
                if (out == null) continue;
                ItemStack drop = new ItemStack(out, 1);

                // chest deposit or drop on ground
                if (m.chest() != null) {
                    Block b = m.chest().getBlock();
                    if (b.getState() instanceof Chest chest) {
                        chest.getBlockInventory().addItem(drop);
                        continue;
                    }
                }
                m.loc().getWorld().dropItemNaturally(m.loc().clone().add(0, 0.5, 0), drop);
            }
        }, 20L, 20L);
    }

    private Material rollMinerOutput(Minion m) {
        // pick table by island generator tier
        Island is = plugin.islands().getIsland(m.islandOwner());
        if (is == null) return Material.COBBLESTONE;
        String tier = is.generatorTier();
        String path = "minions.types.miner.table-per-tier." + tier;
        if (!plugin.getConfig().isConfigurationSection(path)) return Material.COBBLESTONE;
        int total = 0;
        List<Map.Entry<Material,Integer>> table = new ArrayList<>();
        for (String k : plugin.getConfig().getConfigurationSection(path).getKeys(false)) {
            try {
                Material mat = Material.valueOf(k);
                int w = plugin.getConfig().getInt(path+"."+k, 0);
                if (w > 0) { table.add(Map.entry(mat, w)); total += w; }
            } catch (IllegalArgumentException ignored) {}
        }
        if (table.isEmpty()) return Material.COBBLESTONE;
        int r = ThreadLocalRandom.current().nextInt(total);
        int acc = 0;
        for (var e : table) { acc += e.getValue(); if (r < acc) return e.getKey(); }
        return Material.COBBLESTONE;
    }

    private String fmtTime(long sec) {
        long m = sec / 60; long s = sec % 60;
        return m + "m " + s + "s";
    }

    // --- fuel resolution ---

    public boolean tryFeedFuel(Minion m, Material mat) {
        String base = "minions.fuels."+mat.name();
        if (!plugin.getConfig().isConfigurationSection(base)) return false;
        long seconds = plugin.getConfig().getLong(base+".seconds", 0);
        double eff = plugin.getConfig().getDouble(base+".efficiency", 1.0);
        if (seconds <= 0) return false;
        m.addFuel(seconds, eff);
        saveMinion(m);
        return true;
    }

    // --- upgrades ---

    public boolean tryUpgrade(Minion m) {
        int max = plugin.getConfig().getInt("minions.types.miner.max-level", 10);
        if (m.level() >= max) return false;

        long c = plugin.getConfig().getLong("minions.types.miner.upgrade-cost.crystals", 25);
        long $ = plugin.getConfig().getLong("minions.types.miner.upgrade-cost.coins", 15000);

        var island = plugin.islands().getIsland(m.islandOwner());
        if (island == null) return false;
        if (!plugin.islands().tryPay(island, c, $)) return false;

        m.setLevel(m.level()+1);
        saveMinion(m);
        return true;
    }

    // --- persistence ---

    private void loadAll() {
        if (!data.isConfigurationSection("minions")) return;
        for (String id : data.getConfigurationSection("minions").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(id);
                UUID owner = UUID.fromString(data.getString("minions."+id+".owner"));
                UUID placer = UUID.fromString(data.getString("minions."+id+".placer"));
                String type = data.getString("minions."+id+".type","miner");
                String world = data.getString("minions."+id+".world");
                double x = data.getDouble("minions."+id+".x");
                double y = data.getDouble("minions."+id+".y");
                double z = data.getDouble("minions."+id+".z");
                int lvl = data.getInt("minions."+id+".level",1);
                long fuel = data.getLong("minions."+id+".fuel",0);
                double eff = data.getDouble("minions."+id+".eff",1.0);

                World w = Bukkit.getWorld(world);
                if (w == null) continue;
                Minion m = new Minion(uuid, owner, placer, type, new Location(w, x, y, z));
                m.setLevel(lvl);
                m.addFuel(fuel, eff);
                if (data.isConfigurationSection("minions."+id+".chest")) {
                    String cw = data.getString("minions."+id+".chest.world");
                    double cx = data.getDouble("minions."+id+".chest.x");
                    double cy = data.getDouble("minions."+id+".chest.y");
                    double cz = data.getDouble("minions."+id+".chest.z");
                    World cW = Bukkit.getWorld(cw);
                    if (cW != null) m.setChest(new Location(cW, cx, cy, cz));
                }
                minions.put(m.id(), m);
                // (re)spawn display
                spawnStand(m);
            } catch (Exception ignored) {}
        }
    }

    private void saveMinion(Minion m) {
        String k = "minions."+m.id();
        data.set(k+".owner", m.islandOwner().toString());
        data.set(k+".placer", m.placer().toString());
        data.set(k+".type", m.type());
        data.set(k+".world", m.loc().getWorld().getName());
        data.set(k+".x", m.loc().getX());
        data.set(k+".y", m.loc().getY());
        data.set(k+".z", m.loc().getZ());
        data.set(k+".level", m.level());
        data.set(k+".fuel", m.fuelSeconds());
        data.set(k+".eff", m.fuelEfficiency());
        if (m.chest() != null) {
            data.set(k+".chest.world", m.chest().getWorld().getName());
            data.set(k+".chest.x", m.chest().getX());
            data.set(k+".chest.y", m.chest().getY());
            data.set(k+".chest.z", m.chest().getZ());
        } else {
            data.set(k+".chest", null);
        }
        saveFile();
    }

    private void saveFile() { try { data.save(minionsFile); } catch (IOException ignored) {} }

    public void shutdown() {
        for (Minion m : minions.values()) saveMinion(m);
        saveFile();
        if (tickTask != -1) Bukkit.getScheduler().cancelTask(tickTask);
    }
}
