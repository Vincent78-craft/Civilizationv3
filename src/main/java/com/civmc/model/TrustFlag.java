package com.civmc.model;

public enum TrustFlag {
    BUILD("Build"),
    CONTAINER("Container Access"),
    INTERACT("Interact"),
    USE("Use"),
    ACCESS("Access"),
    REDSTONE("Redstone"),
    KILL_MONSTERS("Kill Monsters"),
    KILL_ANIMALS("Kill Animals"),
    MANAGE("Manage"),
    ALL("All Permissions");
    
    private final String displayName;
    
    TrustFlag(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static TrustFlag fromString(String flag) {
        for (TrustFlag tf : values()) {
            if (tf.name().equalsIgnoreCase(flag)) {
                return tf;
            }
        }
        return null;
    }
}