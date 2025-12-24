package com.civmc.data.storage;

import com.civmc.model.*;

import java.util.Map;

public interface StorageProvider {
    
    boolean initialize();
    void close();
    
    // Civilizations
    Map<String, Civilization> loadCivilizations();
    void saveCivilizations(Map<String, Civilization> civilizations);
    void saveCivilization(Civilization civilization);
    void deleteCivilization(String uuid);
    
    // Claims
    Map<String, Claim> loadClaims();
    void saveClaims(Map<String, Claim> claims);
    void saveClaim(Claim claim);
    void deleteClaim(String claimKey);
    
    // Wars
    Map<String, War> loadWars();
    void saveWars(Map<String, War> wars);
    void saveWar(War war);
    void deleteWar(String warId);
    
    // Invitations
    Map<String, Invitation> loadInvitations();
    void saveInvitations(Map<String, Invitation> invitations);
    void saveInvitation(Invitation invitation);
    void deleteInvitation(String inviteId);
    
    // Utility
    void backup();
    boolean migrate(StorageProvider newProvider);
}