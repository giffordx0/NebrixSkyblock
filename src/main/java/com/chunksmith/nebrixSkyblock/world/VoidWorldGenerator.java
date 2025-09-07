package com.chunksmith.nebrixSkyblock.world;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class VoidWorldGenerator extends ChunkGenerator {
    @Override
    public ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int chunkX, int chunkZ, @NotNull BiomeGrid biome) {
        // Empty chunk = true void
        return createChunkData(world);
    }
}