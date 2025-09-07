package com.chunksmith.nebrixSkyblock.generator;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

public class BasaltGenListener implements Listener {
  private final NebrixSkyblock plugin;

  public BasaltGenListener(NebrixSkyblock plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onForm(BlockFormEvent event) {
    if (event.getBlock().getWorld() != plugin.worlds().nether()) return;
    if (event.getNewState().getType() != Material.BASALT) return;
    Material m = plugin.generators().rollBasalt(1);
    event.getBlock().setType(m);
  }
}
