package com.chunksmith.nebrixSkyblock;

import com.chunksmith.nebrixSkyblock.command.IslandCommand;
import com.chunksmith.nebrixSkyblock.generator.GeneratorService;
import com.chunksmith.nebrixSkyblock.invites.InviteService;
import com.chunksmith.nebrixSkyblock.island.IslandService;
import com.chunksmith.nebrixSkyblock.limits.BlockLimitsService;
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
    this.worlds = new WorldService(this);
    this.islands = new IslandService(this);
    this.limits = new BlockLimitsService(this);
    this.values = new ValueService(this);
    this.generators = new GeneratorService(this);
    this.invites = new InviteService();
    this.storage = new StorageService(this);

    getCommand("island").setExecutor(new IslandCommand(this));
    getServer().getPluginManager().registerEvents(new Menu.MenuListener(), this);
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
