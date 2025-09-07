package com.chunksmith.nebrixSkyblock.storage;

import com.chunksmith.nebrixSkyblock.NebrixSkyblock;

/** Placeholder storage service. */
public class StorageService {
  public StorageService(NebrixSkyblock plugin) {}
  private final NebrixSkyblock plugin;

  public StorageService(NebrixSkyblock plugin) {
    this.plugin = plugin;
  }

  public void flush() {
    // no-op
  }
}
