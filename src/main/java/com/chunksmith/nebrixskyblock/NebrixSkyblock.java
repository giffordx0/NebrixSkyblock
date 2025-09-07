package com.chunksmith.nebrixSkyblock;

import com.chunksmith.nebrixSkyblock.commands.IslandCommand;
import com.chunksmith.nebrixSkyblock.gen.GeneratorListener;
import com.chunksmith.nebrixSkyblock.island.IslandManager;
import com.chunksmith.nebrixSkyblock.leaderboard.LeaderboardManager;
import com.chunksmith.nebrixSkyblock.minion.MinionListener;
import com.chunksmith.nebrixSkyblock.minion.MinionManager;
import com.chunksmith.nebrixSkyblock.protection.ProtectionListener;
import com.chunksmith.nebrixSkyblock.ui.MenuListener;
import com.chunksmith.nebrixSkyblock.world.VoidWorldGenerator;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class NebrixSkyblock extends JavaPlugin {

    private IslandManager islandManager;
    private LeaderboardManager leaderboard;

    // ADD THIS:
    private MinionManager minionManager;

    public String overworld() { return getConfig().getString("world.overworld", "nebrix_islands"); }
    public String nether()     { return getConfig().getString("world.nether", "nebrix_islands_nether"); }
    public String end()        { return getConfig().getString("world.end", "nebrix_islands_end"); }

    // ADD THIS:
    public MinionManager minions() { return minionManager; }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ensureWorld(overworld(), World.Environment.NORMAL);
        ensureWorld(nether(), World.Environment.NETHER);
        ensureWorld(end(), World.Environment.THE_END);

        this.islandManager = new IslandManager(this);
        this.leaderboard   = new LeaderboardManager(this);

        // ADD THIS:
        this.minionManager = new MinionManager(this);

        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new GeneratorListener(this), this);

        // ADD THIS:
        getServer().getPluginManager().registerEvents(new MinionListener(this), this);

        IslandCommand islandCommand = new IslandCommand(this);
        getCommand("island").setExecutor(islandCommand);
        getCommand("island").setTabCompleter(islandCommand);
    }

    @Override
    public void onDisable() {
        if (islandManager != null) islandManager.shutdown();
        if (leaderboard != null) leaderboard.shutdown();

        // ADD THIS:
        if (minionManager != null) minionManager.shutdown();
    }

    private void ensureWorld(String name, World.Environment env) {
        int baseY = getConfig().getInt("world.base-y", 64);
        boolean pvp = getConfig().getBoolean("world.pvp", false);
        boolean mobs = getConfig().getBoolean("world.mob-spawning", false);

        World w = Bukkit.getWorld(name);
        if (w == null) {
            WorldCreator wc = new WorldCreator(name);
            wc.environment(env);
            wc.type(WorldType.NORMAL);
            wc.generateStructures(false);
            if (env == World.Environment.NORMAL) wc.generator(new VoidWorldGenerator());
            w = Bukkit.createWorld(wc);
        }
        if (w != null && env == World.Environment.NORMAL) {
            w.setPVP(pvp);
            w.setSpawnFlags(mobs, mobs);
            w.setGameRule(GameRule.DO_MOB_SPAWNING, mobs);
            w.setSpawnLocation(0, baseY, 0);
        }
    }

    public IslandManager islands() { return islandManager; }
    public LeaderboardManager leaderboard() { return leaderboard; }
}
