package com.civmc.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Claim {
    private String world;
    private int chunkX;
    private int chunkZ;
    private String civId;
    private ClaimFlags flags;
    private Set<ClaimTrust> trusts;
    private long createdAt;
    
    public Claim() {
        this.trusts = ConcurrentHashMap.newKeySet();
        this.flags = new ClaimFlags();
        this.createdAt = System.currentTimeMillis();
    }
    
    public Claim(String world, int chunkX, int chunkZ, String civId) {
        this();
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.civId = civId;
    }
    
    public String getClaimKey() {
        return world + ":" + chunkX + ":" + chunkZ;
    }
    
    public boolean isTrusted(String playerUUID, TrustFlag flag) {
        return trusts.stream()
                .anyMatch(trust -> trust.getPlayerUUID().equals(playerUUID) && 
                                 trust.hasFlag(flag) && 
                                 !trust.isExpired());
    }
    
    public void addTrust(String playerUUID, Set<TrustFlag> flags) {
        removeTrust(playerUUID);
        trusts.add(new ClaimTrust(playerUUID, flags));
    }
    
    public void removeTrust(String playerUUID) {
        trusts.removeIf(trust -> trust.getPlayerUUID().equals(playerUUID));
    }
    
    public Set<ClaimTrust> getTrusts() {
        // Remove expired trusts
        trusts.removeIf(ClaimTrust::isExpired);
        return trusts;
    }
    
    // Getters and Setters
    public String getWorld() {
        return world;
    }
    
    public void setWorld(String world) {
        this.world = world;
    }
    
    public int getChunkX() {
        return chunkX;
    }
    
    public void setChunkX(int chunkX) {
        this.chunkX = chunkX;
    }
    
    public int getChunkZ() {
        return chunkZ;
    }
    
    public void setChunkZ(int chunkZ) {
        this.chunkZ = chunkZ;
    }
    
    public String getCivId() {
        return civId;
    }
    
    public void setCivId(String civId) {
        this.civId = civId;
    }
    
    public ClaimFlags getFlags() {
        return flags;
    }
    
    public void setFlags(ClaimFlags flags) {
        this.flags = flags != null ? flags : new ClaimFlags();
    }
    
    public void setTrusts(Set<ClaimTrust> trusts) {
        this.trusts = trusts != null ? trusts : ConcurrentHashMap.newKeySet();
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Claim claim = (Claim) o;
        return chunkX == claim.chunkX && 
               chunkZ == claim.chunkZ && 
               Objects.equals(world, claim.world);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(world, chunkX, chunkZ);
    }
}