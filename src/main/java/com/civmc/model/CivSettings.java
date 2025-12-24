package com.civmc.model;

public class CivSettings {
    private boolean pvpInternal;
    private int taxPercent;
    private String inviteMode; // "invite-only" or "open"
    private String entryMessage;
    private String exitMessage;
    private int officerSlots;
    private boolean autoClaimEnabled;
    private int autoClaimRadius;
    
    public CivSettings() {
        this.pvpInternal = false;
        this.taxPercent = 0;
        this.inviteMode = "invite-only";
        this.entryMessage = "";
        this.exitMessage = "";
        this.officerSlots = 5;
        this.autoClaimEnabled = false;
        this.autoClaimRadius = 1;
    }
    
    // Getters and Setters
    public boolean isPvpInternal() {
        return pvpInternal;
    }
    
    public void setPvpInternal(boolean pvpInternal) {
        this.pvpInternal = pvpInternal;
    }
    
    public int getTaxPercent() {
        return taxPercent;
    }
    
    public void setTaxPercent(int taxPercent) {
        this.taxPercent = Math.max(0, Math.min(100, taxPercent));
    }
    
    public String getInviteMode() {
        return inviteMode;
    }
    
    public void setInviteMode(String inviteMode) {
        this.inviteMode = inviteMode;
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
    
    public int getOfficerSlots() {
        return officerSlots;
    }
    
    public void setOfficerSlots(int officerSlots) {
        this.officerSlots = officerSlots;
    }
    
    public boolean isAutoClaimEnabled() {
        return autoClaimEnabled;
    }
    
    public void setAutoClaimEnabled(boolean autoClaimEnabled) {
        this.autoClaimEnabled = autoClaimEnabled;
    }
    
    public int getAutoClaimRadius() {
        return autoClaimRadius;
    }
    
    public void setAutoClaimRadius(int autoClaimRadius) {
        this.autoClaimRadius = autoClaimRadius;
    }
}