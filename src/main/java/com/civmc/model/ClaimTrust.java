package com.civmc.model;

import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class ClaimTrust {
    private String playerUUID;
    private Set<TrustFlag> flags;
    private long expiresAt; // 0 = never expires
    
    public ClaimTrust() {
        this.flags = ConcurrentHashMap.newKeySet();
        this.expiresAt = 0;
    }
    
    public ClaimTrust(String playerUUID, Set<TrustFlag> flags) {
        this();
        this.playerUUID = playerUUID;
        this.flags.addAll(flags);
    }
    
    public ClaimTrust(String playerUUID, Set<TrustFlag> flags, long expiresAt) {
        this(playerUUID, flags);
        this.expiresAt = expiresAt;
    }
    
    public boolean hasFlag(TrustFlag flag) {
        return flags.contains(TrustFlag.ALL) || flags.contains(flag);
    }
    
    public boolean isExpired() {
        return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
    }
    
    // Getters and Setters
    public String getPlayerUUID() {
        return playerUUID;
    }
    
    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }
    
    public Set<TrustFlag> getFlags() {
        return flags;
    }
    
    public void setFlags(Set<TrustFlag> flags) {
        this.flags = flags != null ? flags : ConcurrentHashMap.newKeySet();
    }
    
    public long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}