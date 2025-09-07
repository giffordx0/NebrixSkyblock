package com.chunksmith.nebrixSkyblock.minion;

import org.bukkit.Location;
import java.util.UUID;

public class Minion {
    private final UUID id;
    private final UUID islandOwner;   // island owner UUID
    private final UUID placer;        // who placed it
    private final String type;        // "miner"
    private Location location;
    private int level = 1;
    private long fuelSeconds = 0;
    private double fuelEfficiency = 1.0;
    private Location linkedChest = null;  // optional
    private UUID armorStandId = null;     // display entity id

    public Minion(UUID id, UUID islandOwner, UUID placer, String type, Location location) {
        this.id = id; this.islandOwner = islandOwner; this.placer = placer; this.type = type; this.location = location;
    }

    public UUID id() { return id; }
    public UUID islandOwner() { return islandOwner; }
    public UUID placer() { return placer; }
    public String type() { return type; }

    public Location loc() { return location; }
    public void setLoc(Location l) { this.location = l; }

    public int level() { return level; }
    public void setLevel(int l) { this.level = l; }

    public long fuelSeconds() { return fuelSeconds; }
    public void addFuel(long seconds, double eff) {
        this.fuelSeconds += seconds;
        this.fuelEfficiency = Math.max(this.fuelEfficiency, eff);
    }
    public void tickFuel() { if (fuelSeconds > 0) fuelSeconds--; if (fuelSeconds == 0) fuelEfficiency = 1.0; }
    public double fuelEfficiency() { return fuelEfficiency; }

    public Location chest() { return linkedChest; }
    public void setChest(Location c) { linkedChest = c; }

    public UUID standId() { return armorStandId; }
    public void setStandId(UUID id) { armorStandId = id; }
}
