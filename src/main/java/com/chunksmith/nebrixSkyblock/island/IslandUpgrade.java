package com.chunksmith.nebrixSkyblock.island;

/** Tracks upgrade levels for an island. */
public class IslandUpgrade {
  private int radiusLevel = 1;
  private int memberLevel = 1;
  private int generatorTier = 1;

  public int radiusLevel() {
    return radiusLevel;
  }

  public void setRadiusLevel(int radiusLevel) {
    this.radiusLevel = radiusLevel;
  }

  public int memberLevel() {
    return memberLevel;
  }

  public void setMemberLevel(int memberLevel) {
    this.memberLevel = memberLevel;
  }

  public int generatorTier() {
    return generatorTier;
  }

  public void setGeneratorTier(int generatorTier) {
    this.generatorTier = generatorTier;
  }
}
