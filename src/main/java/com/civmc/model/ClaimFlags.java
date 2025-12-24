package com.civmc.model;

public class ClaimFlags {
    private FlagValue pvp;
    private boolean explosionsBlocked;
    private AccessLevel interact;
    private AccessLevel containers;
    private AccessLevel redstone;
    private boolean mobSpawnHostile;
    private boolean mobSpawnPassive;
    private boolean fireSpread;
    private boolean blockSpread;
    private boolean fluidFlow;
    private boolean explosions;
    private boolean monsterSpawn;
    private boolean animalSpawn;
    private boolean potions;
    private String entryMessage;
    private String exitMessage;
    
    public ClaimFlags() {
        this.pvp = FlagValue.INHERIT;
        this.explosionsBlocked = true;
        this.interact = AccessLevel.MEMBERS_ONLY;
        this.containers = AccessLevel.MEMBERS_ONLY;
        this.redstone = AccessLevel.MEMBERS_ONLY;
        this.mobSpawnHostile = true;
        this.mobSpawnPassive = true;
        this.fireSpread = false;
        this.blockSpread = false;
        this.fluidFlow = false;
        this.explosions = true;
        this.monsterSpawn = true;
        this.animalSpawn = true;
        this.potions = false;
        this.entryMessage = "";
        this.exitMessage = "";
    }
    
    // Getters and Setters
    public FlagValue getPvp() {
        return pvp;
    }
    
    public void setPvp(FlagValue pvp) {
        this.pvp = pvp;
    }
    
    public boolean isExplosionsBlocked() {
        return explosionsBlocked;
    }
    
    public void setExplosionsBlocked(boolean explosionsBlocked) {
        this.explosionsBlocked = explosionsBlocked;
    }
    
    public AccessLevel getInteract() {
        return interact;
    }
    
    public void setInteract(AccessLevel interact) {
        this.interact = interact;
    }
    
    public AccessLevel getContainers() {
        return containers;
    }
    
    public void setContainers(AccessLevel containers) {
        this.containers = containers;
    }
    
    public AccessLevel getRedstone() {
        return redstone;
    }
    
    public void setRedstone(AccessLevel redstone) {
        this.redstone = redstone;
    }
    
    public boolean isMobSpawnHostile() {
        return mobSpawnHostile;
    }
    
    public void setMobSpawnHostile(boolean mobSpawnHostile) {
        this.mobSpawnHostile = mobSpawnHostile;
    }
    
    public boolean isMobSpawnPassive() {
        return mobSpawnPassive;
    }
    
    public void setMobSpawnPassive(boolean mobSpawnPassive) {
        this.mobSpawnPassive = mobSpawnPassive;
    }
    
    public boolean isFireSpread() {
        return fireSpread;
    }
    
    public void setFireSpread(boolean fireSpread) {
        this.fireSpread = fireSpread;
    }
    
    public String getEntryMessage() {
        return entryMessage;
    }
    
    public void setEntryMessage(String entryMessage) {
        this.entryMessage = entryMessage;
    }
    
    public String getExitMessage() {
        return exitMessage;
    }
    
    public void setExitMessage(String exitMessage) {
        this.exitMessage = exitMessage;
    }
    
    public boolean isBlockSpread() {
        return blockSpread;
    }
    
    public void setBlockSpread(boolean blockSpread) {
        this.blockSpread = blockSpread;
    }
    
    public boolean isFluidFlow() {
        return fluidFlow;
    }
    
    public void setFluidFlow(boolean fluidFlow) {
        this.fluidFlow = fluidFlow;
    }
    
    public boolean isExplosions() {
        return explosions;
    }
    
    public void setExplosions(boolean explosions) {
        this.explosions = explosions;
    }
    
    public boolean isMonsterSpawn() {
        return monsterSpawn;
    }
    
    public void setMonsterSpawn(boolean monsterSpawn) {
        this.monsterSpawn = monsterSpawn;
    }
    
    public boolean isAnimalSpawn() {
        return animalSpawn;
    }
    
    public void setAnimalSpawn(boolean animalSpawn) {
        this.animalSpawn = animalSpawn;
    }
    
    public boolean isPotions() {
        return potions;
    }
    
    public void setPotions(boolean potions) {
        this.potions = potions;
    }
    
    public boolean isPlantGrowth() {
        return true; // Allow plant growth by default
    }
    
    public boolean isIceMelt() {
        return true; // Allow ice melt by default
    }
    
    public boolean isLeafDecay() {
        return true; // Allow leaf decay by default
    }
    
    public boolean isBlockForm() {
        return true; // Allow block formation by default
    }
    
    public boolean isPublicAccess() {
        return interact == AccessLevel.PUBLIC;
    }
    
    public boolean isItemDrop() {
        return true; // Allow item drops by default
    }
    
    public boolean isItemPickup() {
        return true; // Allow item pickups by default
    }
    
    public boolean isTeleportation() {
        return true; // Allow teleportation by default
    }
    
    public boolean isPvp() {
        return pvp == FlagValue.ON;
    }
    
    public boolean isFriendlyFire() {
        return false; // No friendly fire by default
    }
    
    public enum FlagValue {
        ON, OFF, INHERIT
    }
    
    public enum AccessLevel {
        MEMBERS_ONLY,
        ALLIES_ALLOWED,
        PUBLIC
    }
}