package com.chunksmith.nebrixSkyblock.island;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

public final class DefaultIslandSchematic {
    private DefaultIslandSchematic() {}

    public static void place(Location center) {
        World w = center.getWorld();
        int baseY = center.getBlockY();
        int cx = center.getBlockX(), cz = center.getBlockZ();

        w.getBlockAt(cx, baseY - 1, cz).setType(Material.BEDROCK, false);
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++)
                w.getBlockAt(cx + dx, baseY, cz + dz).setType(Material.DIRT, false);

        w.getBlockAt(cx, baseY + 1, cz).setType(Material.OAK_SAPLING, false);

        Block chestBlock = w.getBlockAt(cx + 3, baseY + 1, cz);
        chestBlock.setType(Material.CHEST, false);
        if (chestBlock.getState() instanceof Chest chest) {
            chest.getBlockInventory().addItem(new ItemStack(Material.ICE, 1));
            chest.getBlockInventory().addItem(new ItemStack(Material.LAVA_BUCKET, 1));
            chest.getBlockInventory().addItem(new ItemStack(Material.MELON_SLICE, 1));
            chest.getBlockInventory().addItem(new ItemStack(Material.BONE_MEAL, 3));
            chest.getBlockInventory().addItem(new ItemStack(Material.OAK_SAPLING, 1));
            chest.update();
        }
    }
}