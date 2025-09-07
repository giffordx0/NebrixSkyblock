package com.chunksmith.nebrixSkyblock.island;

public class IslandBank {
  private long coins;
  private long crystals;

  public long coins() {
    return coins;
  }

  public long crystals() {
    return crystals;
  }

  public void depositCoins(long amount) {
    coins += amount;
  }

  public void depositCrystals(long amount) {
    crystals += amount;
  }
}
