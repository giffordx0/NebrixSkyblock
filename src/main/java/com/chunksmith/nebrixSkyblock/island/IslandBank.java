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
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot deposit negative amount");
        }
        coins += amount;
    }

    public void depositCrystals(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot deposit negative amount");
        }
        crystals += amount;
    }

    public boolean withdrawCoins(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot withdraw negative amount");
        }
        if (coins >= amount) {
            coins -= amount;
            return true;
        }
        return false;
    }

    public boolean withdrawCrystals(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot withdraw negative amount");
        }
        if (crystals >= amount) {
            crystals -= amount;
            return true;
        }
        return false;
    }

    public void setCoins(long coins) {
        if (coins < 0) {
            throw new IllegalArgumentException("Coins cannot be negative");
        }
        this.coins = coins;
    }

    public void setCrystals(long crystals) {
        if (crystals < 0) {
            throw new IllegalArgumentException("Crystals cannot be negative");
        }
        this.crystals = crystals;
    }

    public boolean canAfford(long coinCost, long crystalCost) {
        return coins >= coinCost && crystals >= crystalCost;
    }

    public boolean purchase(long coinCost, long crystalCost) {
        if (canAfford(coinCost, crystalCost)) {
            coins -= coinCost;
            crystals -= crystalCost;
            return true;
        }
        return false;
    }
}