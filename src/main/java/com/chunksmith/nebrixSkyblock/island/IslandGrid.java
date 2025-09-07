package com.chunksmith.nebrixSkyblock.island;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.World;

/** Outward square-spiral allocator for island centers. */
public class IslandGrid {
    private int x = 0, z = 0;
    private int dx = 0, dz = -1;
    private int steps = 0, legLen = 1, legProg = 0;

    private final World world;
    private final int spacing;
    private final int baseY;

    public IslandGrid(World world, int spacing, int baseY) {
        this.world = world;
        this.spacing = spacing;
        this.baseY = baseY;
    }

    public Location next() {
        x += dx; z += dz; legProg++;
        if (legProg == legLen) {
            legProg = 0;
            if (dx == 0 && dz == -1) { dx = 1; dz = 0; }
            else if (dx == 1 && dz == 0) { dx = 0; dz = 1; }
            else if (dx == 0 && dz == 1) { dx = -1; dz = 0; }
            else if (dx == -1 && dz == 0) { dx = 0; dz = -1; }
            steps++; if (steps % 2 == 0) legLen++;
        }
        return new Location(world, x * spacing + 0.5, baseY, z * spacing + 0.5);
    }
}