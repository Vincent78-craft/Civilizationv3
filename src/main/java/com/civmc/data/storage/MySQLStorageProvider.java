package com.civmc.data.storage;

import com.civmc.CivilizationMC;
import com.civmc.model.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MySQLStorageProvider implements StorageProvider {
    
    private final CivilizationMC plugin;
    
    public MySQLStorageProvider(CivilizationMC plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean initialize() {
        plugin.getLogger().warning("MySQL storage provider is not yet implemented! Using JSON instead.");
        return false;
    }
    
    @Override
    public void close() {
        // TODO: Implement MySQL storage
    }
    
    @Override
    public Map<String, Civilization> loadCivilizations() {
        return new ConcurrentHashMap<>();
    }
    
    @Override
    public void saveCivilizations(Map<String, Civilization> civilizations) {
        // TODO: Implement MySQL storage
    }
    
    @Override
    public void saveCivilization(Civilization civilization) {
        // TODO: Implement MySQL storage
    }
    
    @Override
    public void deleteCivilization(String uuid) {
        // TODO: Implement MySQL storage
    }
    
    @Override
    public Map<String, Claim> loadClaims() {
        return new ConcurrentHashMap<>();
    }
    
    @Override
    public void saveClaims(Map<String, Claim> claims) {
        // TODO: Implement MySQL storage
    }
    
    @Override
    public void saveClaim(Claim claim) {
        // TODO: Implement MySQL storage
    }
    
    @Override
    public void deleteClaim(String claimKey) {
        // TODO: Implement MySQL storage
    }
    
    @Override
    public Map<String, War> loadWars() {
        return new ConcurrentHashMap<>();
    }
    
    @Override
    public void saveWars(Map<String, War> wars) {
        // TODO: Implement MySQL storage
    }
    
    @Override
    public void saveWar(War war) {
        // TODO: Implement MySQL storage
    }
    
    @Override
    public void deleteWar(String warId) {
        // TODO: Implement MySQL storage
    }
    
    @Override
    public Map<String, Invitation> loadInvitations() {
        return new ConcurrentHashMap<>();
    }
    
    @Override
    public void saveInvitations(Map<String, Invitation> invitations) {
        // TODO: Implement MySQL storage
    }
    
    @Override
    public void saveInvitation(Invitation invitation) {
        // TODO: Implement MySQL storage
    }
    
    @Override
    public void deleteInvitation(String inviteId) {
        // TODO: Implement MySQL storage
    }
    
    @Override
    public void backup() {
        // TODO: Implement MySQL backup
    }
    
    @Override
    public boolean migrate(StorageProvider newProvider) {
        // TODO: Implement MySQL migration
        return false;
    }
}