package com.chunksmith.nebrixSkyblock;

import com.chunksmith.nebrixSkyblock.command.IslandCommand;
import com.chunksmith.nebrixSkyblock.generator.BasaltGenListener;
import com.chunksmith.nebrixSkyblock.generator.CobbleGenListener;
import com.chunksmith.nebrixSkyblock.generator.GeneratorService;
import com.chunksmith.nebrixSkyblock.invites.InviteService;
import com.chunksmith.nebrixSkyblock.island.IslandService;
import com.chunksmith.nebrixSkyblock.limits.BlockLimitsService;
import com.chunksmith.nebrixSkyblock.protect.ProtectionListener;
import com.chunksmith.nebrixSkyblock.storage.StorageService;
import com.chunksmith.nebrixSkyblock.ui.Menu;
import com.chunksmith.nebrixSkyblock.value.ValueService;
import com.chunksmith.nebrixSkyblock.world.WorldService;
import org.bukkit.plugin.java.JavaPlugin;

/** Main plugin entry. */
public class NebrixSkyblock extends JavaPlugin implements com.chunksmith.nebrixSkyblock.api.NebrixSkyblockAPI {
    private WorldService worlds;
    private IslandService islands;
    private BlockLimitsService limits;
    private ValueService values;
    private GeneratorService generators;
    private InviteService invites;
    private StorageService storage;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize services in proper order
        this.worlds = new WorldService(this);
        this.storage = new StorageService(this);
        this.islands = new IslandService(this);
        this.limits = new BlockLimitsService(this);
        this.values = new ValueService(this);
        this.generators = new GeneratorService(this);
        this.invites = new InviteService();

        // Register commands
        getCommand("island").setExecutor(new IslandCommand(this));

        // Register event listeners
        var pm = getServer().getPluginManager();
        pm.registerEvents(new Menu.MenuListener(), this);
        pm.registerEvents(new CobbleGenListener(this), this);
        pm.registerEvents(new BasaltGenListener(this), this);
        pm.registerEvents(new ProtectionListener(this), this);

        getLogger().info("NebrixSkyblock enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.flush();
        }
        getLogger().info("NebrixSkyblock disabled!");
    }

    public WorldService worlds() {
        return worlds;
    }

    @Override
    public IslandService islands() {
        return islands;
    }

    public BlockLimitsService limits() {
        return limits;
    }

    public ValueService values() {
        return values;
    }

    public GeneratorService generators() {
        return generators;
    }

    public StorageService storage() {
        return storage;
    }

    public InviteService invites() {
        return invites;
    }
}