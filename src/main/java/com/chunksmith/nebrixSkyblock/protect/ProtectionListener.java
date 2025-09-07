package com.chunksmith.nebrixSkyblock.protect;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/** Basic protection listener stub. */
public class ProtectionListener implements Listener {
  private final NebrixSkyblock plugin;

  public ProtectionListener(NebrixSkyblock plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    // stub for protection
  }
}
