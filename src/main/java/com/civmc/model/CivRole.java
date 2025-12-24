package com.civmc.model;

public enum CivRole {
    LEADER(4, "Leader"),
    OFFICER(3, "Officer"), 
    MEMBER(2, "Member"),
    RECRUIT(1, "Recruit");
    
    private final int power;
    private final String displayName;
    
    CivRole(int power, String displayName) {
        this.power = power;
        this.displayName = displayName;
    }
    
    public int getPower() {
        return power;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean canManage(CivRole other) {
        return this.power > other.power;
    }
    
    public static CivRole fromString(String role) {
        for (CivRole r : values()) {
            if (r.name().equalsIgnoreCase(role)) {
                return r;
            }
        }
        return RECRUIT;
    }
}