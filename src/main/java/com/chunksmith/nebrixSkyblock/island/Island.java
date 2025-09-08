package com.chunksmith.nebrixSkyblock.island;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;

/** Represents an island. */
public class Island {
  private final UUID id;
  private final UUID owner;
  private final Location center;
  private final IslandSettings settings = new IslandSettings();
  private final IslandBank bank = new IslandBank();
  private final IslandUpgrade upgrades = new IslandUpgrade();
  private final Map<UUID, IslandMember> members = new HashMap<>();

  public Island(UUID id, UUID owner, Location center) {
    this.id = id;
    this.owner = owner;
    this.center = center;
    members.put(owner, new IslandMember(owner, IslandRole.OWNER));
  }

  public UUID id() {
    return id;
  }

  public Location center() {
    return center;
  }

  public UUID owner() {
    return owner;
  }

  public IslandSettings settings() {
    return settings;
  }

  public IslandBank bank() {
    return bank;
  }

  public IslandUpgrade upgrades() {
    return upgrades;
  }

  public Map<UUID, IslandMember> members() {
    return members;
  }
}
