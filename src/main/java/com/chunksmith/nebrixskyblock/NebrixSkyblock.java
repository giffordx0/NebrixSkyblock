package com.chunksmith.nebrixskyblock;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Primary plugin entry point for NebrixSkyblock.
 *
 * <p>This class currently only loads the configuration and registers a simple
 * placeholder command. The rest of the functionality can be built on top of
 * this foundation.</p>
 */
public final class NebrixSkyblock extends JavaPlugin {

    @Override
    public void onEnable() {
        // Ensure the default configuration is saved on first run.
        saveDefaultConfig();

        // Register the /island command handler.
        PluginCommand island = getCommand("island");
        if (island != null) {
            island.setExecutor(new IslandCommand());
        } else {
            getLogger().severe("Failed to register island command.");
        }

        getLogger().info("NebrixSkyblock enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("NebrixSkyblock disabled");
    }
}

