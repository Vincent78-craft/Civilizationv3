package com.civmc.data.storage;

import com.civmc.CivilizationMC;
import com.civmc.model.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SQLiteStorageProvider implements StorageProvider {
    
    private final CivilizationMC plugin;
    
    public SQLiteStorageProvider(CivilizationMC plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean initialize() {
        plugin.getLogger().warning("SQLite storage provider is not yet implemented! Using JSON instead.");
        return false;
    }
    
    @Override
    public void close() {
        // TODO: Implement SQLite storage
    }
    
    @Override
    public Map<String, Civilization> loadCivilizations() {
        return new ConcurrentHashMap<>();
    }
    
    @Override
    public void saveCivilizations(Map<String, Civilization> civilizations) {
        // TODO: Implement SQLite storage
    }
    
    @Override
    public void saveCivilization(Civilization civilization) {
        // TODO: Implement SQLite storage
    }
    
    @Override
    public void deleteCivilization(String uuid) {
        // TODO: Implement SQLite storage
    }
    
    @Override
    public Map<String, Claim> loadClaims() {
        return new ConcurrentHashMap<>();
    }
    
    @Override
    public void saveClaims(Map<String, Claim> claims) {
        // TODO: Implement SQLite storage
    }
    
    @Override
    public void saveClaim(Claim claim) {
        // TODO: Implement SQLite storage
    }
    
    @Override
    public void deleteClaim(String claimKey) {
        // TODO: Implement SQLite storage
    }
    
    @Override
    public Map<String, War> loadWars() {
        return new ConcurrentHashMap<>();
    }
    
    @Override
    public void saveWars(Map<String, War> wars) {
        // TODO: Implement SQLite storage
    }
    
    @Override
    public void saveWar(War war) {
        // TODO: Implement SQLite storage
    }
    
    @Override
    public void deleteWar(String warId) {
        // TODO: Implement SQLite storage
    }
    
    @Override
    public Map<String, Invitation> loadInvitations() {
        return new ConcurrentHashMap<>();
    }
    
    @Override
    public void saveInvitations(Map<String, Invitation> invitations) {
        // TODO: Implement SQLite storage
    }
    
    @Override
    public void saveInvitation(Invitation invitation) {
        // TODO: Implement SQLite storage
    }
    
    @Override
    public void deleteInvitation(String inviteId) {
        // TODO: Implement SQLite storage
    }
    
    @Override
    public void backup() {
        // TODO: Implement SQLite backup
    }
    
    @Override
    public boolean migrate(StorageProvider newProvider) {
        // TODO: Implement SQLite migration
        return false;
    }
}