package com.chunksmith.nebrixSkyblock.generator;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

/** Replaces cobble generator outputs with weighted rolls. */
public class CobbleGenListener implements Listener {
  private final NebrixSkyblock plugin;

  public CobbleGenListener(NebrixSkyblock plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onForm(BlockFormEvent event) {
    if (event.getBlock().getWorld() != plugin.worlds().overworld()) return;
    if (event.getNewState().getType() != Material.COBBLESTONE && event.getNewState().getType() != Material.STONE) return;
    Material m = plugin.generators().rollCobble(1);
    event.getBlock().setType(m);
  }
}
