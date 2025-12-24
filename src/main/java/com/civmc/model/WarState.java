package com.civmc.model;

public enum WarState {
    PREP("Preparation"),
    WAR("Active War"),
    ACTIVE("Active War"),
    ENDED("War Ended");
    
    private final String displayName;
    
    WarState(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}