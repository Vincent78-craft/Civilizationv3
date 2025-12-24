package com.civmc.data;

import com.civmc.CivilizationMC;
import com.civmc.data.storage.StorageProvider;
import com.civmc.data.storage.JsonStorageProvider;
import com.civmc.data.storage.SQLiteStorageProvider;
import com.civmc.data.storage.MySQLStorageProvider;
import com.civmc.model.*;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class DataManager {
    
    private final CivilizationMC plugin;
    private final Logger logger;
    private StorageProvider storageProvider;
    
    // In-memory caches
    private final Map<String, Civilization> civilizations = new ConcurrentHashMap<>();
    private final Map<String, Claim> claims = new ConcurrentHashMap<>(); // key: world:x:z
    private final Map<String, War> wars = new ConcurrentHashMap<>();
    private final Map<String, Invitation> invitations = new ConcurrentHashMap<>();
    
    // Player mappings
    private final Map<String, String> playerToCiv = new ConcurrentHashMap<>(); // playerUUID -> civUUID
    
    public DataManager(CivilizationMC plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    public boolean initialize() {
        try {
            String storageType = plugin.getConfigManager().getStorageType();
            
            switch (storageType.toUpperCase()) {
                case "JSON":
                    storageProvider = new JsonStorageProvider(plugin);
                    break;
                case "SQLITE":
                    storageProvider = new SQLiteStorageProvider(plugin);
                    break;
                case "MYSQL":
                    storageProvider = new MySQLStorageProvider(plugin);
                    break;
                default:
                    logger.warning("Unknown storage type: " + storageType + ". Using JSON.");
                    storageProvider = new JsonStorageProvider(plugin);
                    break;
            }
            
            if (!storageProvider.initialize()) {
                logger.severe("Failed to initialize storage provider!");
                return false;
            }
            
            loadData();
            
            logger.info("DataManager initialized with " + storageType + " storage.");
            return true;
        } catch (Exception e) {
            logger.severe("Failed to initialize DataManager: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void loadData() {
        try {
            // Load civilizations
            Map<String, Civilization> loadedCivs = storageProvider.loadCivilizations();
            civilizations.clear();
            civilizations.putAll(loadedCivs);
            
            // Build player -> civ mapping
            playerToCiv.clear();
            for (Civilization civ : civilizations.values()) {
                for (String playerUUID : civ.getAllMembers()) {
                    playerToCiv.put(playerUUID, civ.getUuid());
                }
            }
            
            // Load claims
            Map<String, Claim> loadedClaims = storageProvider.loadClaims();
            claims.clear();
            claims.putAll(loadedClaims);
            
            // Load wars
            Map<String, War> loadedWars = storageProvider.loadWars();
            wars.clear();
            wars.putAll(loadedWars);
            
            // Load invitations
            Map<String, Invitation> loadedInvitations = storageProvider.loadInvitations();
            invitations.clear();
            invitations.putAll(loadedInvitations);
            
            // Clean expired invitations
            cleanExpiredInvitations();
            
            logger.info("Loaded " + civilizations.size() + " civilizations, " + 
                       claims.size() + " claims, " + 
                       wars.size() + " wars, " + 
                       invitations.size() + " invitations");
                       
        } catch (Exception e) {
            logger.severe("Failed to load data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void saveAll() {
        try {
            CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> {
                    try {
                        storageProvider.saveCivilizations(civilizations);
                    } catch (Exception e) {
                        logger.severe("Failed to save civilizations: " + e.getMessage());
                        e.printStackTrace();
                    }
                }),
                CompletableFuture.runAsync(() -> {
                    try {
                        storageProvider.saveClaims(claims);
                    } catch (Exception e) {
                        logger.severe("Failed to save claims: " + e.getMessage());
                        e.printStackTrace();
                    }
                }),
                CompletableFuture.runAsync(() -> {
                    try {
                        storageProvider.saveWars(wars);
                    } catch (Exception e) {
                        logger.severe("Failed to save wars: " + e.getMessage());
                        e.printStackTrace();
                    }
                }),
                CompletableFuture.runAsync(() -> {
                    try {
                        storageProvider.saveInvitations(invitations);
                    } catch (Exception e) {
                        logger.severe("Failed to save invitations: " + e.getMessage());
                        e.printStackTrace();
                    }
                })
            ).join();
            
            logger.info("All data saved successfully.");
        } catch (Exception e) {
            logger.severe("Failed to save data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void close() {
        try {
            saveAll();
            if (storageProvider != null) {
                storageProvider.close();
            }
        } catch (Exception e) {
            logger.severe("Error closing DataManager: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void cleanExpiredInvitations() {
        invitations.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    // Civilization methods
    public Civilization getCivilization(String uuid) {
        return civilizations.get(uuid);
    }
    
    public Civilization getCivilizationByName(String name) {
        return civilizations.values().stream()
                .filter(civ -> civ.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
    
    public Civilization getPlayerCivilization(String playerUUID) {
        String civUUID = playerToCiv.get(playerUUID);
        return civUUID != null ? civilizations.get(civUUID) : null;
    }
    
    public void saveCivilization(Civilization civilization) {
        civilizations.put(civilization.getUuid(), civilization);
        
        // Update player mappings
        playerToCiv.entrySet().removeIf(entry -> entry.getValue().equals(civilization.getUuid()));
        for (String playerUUID : civilization.getAllMembers()) {
            playerToCiv.put(playerUUID, civilization.getUuid());
        }
        
        // Async save
        if (plugin.getConfigManager().getConfig().getBoolean("performance.async-saves", true)) {
            CompletableFuture.runAsync(() -> {
                try {
                    storageProvider.saveCivilization(civilization);
                } catch (Exception e) {
                    logger.severe("Failed to save civilization " + civilization.getName() + ": " + e.getMessage());
                }
            });
        } else {
            try {
                storageProvider.saveCivilization(civilization);
            } catch (Exception e) {
                logger.severe("Failed to save civilization " + civilization.getName() + ": " + e.getMessage());
            }
        }
    }
    
    public void deleteCivilization(String uuid) {
        Civilization civ = civilizations.remove(uuid);
        if (civ != null) {
            // Remove player mappings
            for (String playerUUID : civ.getAllMembers()) {
                playerToCiv.remove(playerUUID);
            }
            
            // Remove claims
            civ.getClaims().forEach(claims::remove);
            
            // Async delete
            CompletableFuture.runAsync(() -> {
                try {
                    storageProvider.deleteCivilization(uuid);
                } catch (Exception e) {
                    logger.severe("Failed to delete civilization: " + e.getMessage());
                }
            });
        }
    }
    
    public Map<String, Civilization> getAllCivilizations() {
        return new ConcurrentHashMap<>(civilizations);
    }
    
    // Claim methods
    public Claim getClaim(String world, int chunkX, int chunkZ) {
        String key = world + ":" + chunkX + ":" + chunkZ;
        return claims.get(key);
    }
    
    public Claim getClaim(String claimKey) {
        return claims.get(claimKey);
    }
    
    public void saveClaim(Claim claim) {
        String key = claim.getClaimKey();
        claims.put(key, claim);
        
        // Add to civilization claims
        Civilization civ = getCivilization(claim.getCivId());
        if (civ != null) {
            civ.getClaims().add(key);
            saveCivilization(civ);
        }
        
        // Async save
        if (plugin.getConfigManager().getConfig().getBoolean("performance.async-saves", true)) {
            CompletableFuture.runAsync(() -> {
                try {
                    storageProvider.saveClaim(claim);
                } catch (Exception e) {
                    logger.severe("Failed to save claim: " + e.getMessage());
                }
            });
        } else {
            try {
                storageProvider.saveClaim(claim);
            } catch (Exception e) {
                logger.severe("Failed to save claim: " + e.getMessage());
            }
        }
    }
    
    public void deleteClaim(String claimKey) {
        Claim claim = claims.remove(claimKey);
        if (claim != null) {
            // Remove from civilization
            Civilization civ = getCivilization(claim.getCivId());
            if (civ != null) {
                civ.getClaims().remove(claimKey);
                saveCivilization(civ);
            }
            
            CompletableFuture.runAsync(() -> {
                try {
                    storageProvider.deleteClaim(claimKey);
                } catch (Exception e) {
                    logger.severe("Failed to delete claim: " + e.getMessage());
                }
            });
        }
    }
    
    public Set<Claim> getCivilizationClaims(String civUUID) {
        return claims.values().stream()
                .filter(claim -> claim.getCivId().equals(civUUID))
                .collect(java.util.stream.Collectors.toSet());
    }
    
    public Map<String, Claim> getAllClaims() {
        return new ConcurrentHashMap<>(claims);
    }
    
    // War methods
    public War getWar(String warId) {
        return wars.get(warId);
    }
    
    public void saveWar(War war) {
        wars.put(war.getId(), war);
        
        CompletableFuture.runAsync(() -> {
            try {
                storageProvider.saveWar(war);
            } catch (Exception e) {
                logger.severe("Failed to save war: " + e.getMessage());
            }
        });
    }
    
    public void deleteWar(String warId) {
        wars.remove(warId);
        
        CompletableFuture.runAsync(() -> {
            try {
                storageProvider.deleteWar(warId);
            } catch (Exception e) {
                logger.severe("Failed to delete war: " + e.getMessage());
            }
        });
    }
    
    public List<War> getCivilizationWars(String civUUID) {
        return wars.values().stream()
                .filter(war -> war.isInvolvedCiv(civUUID))
                .collect(java.util.stream.Collectors.toList());
    }
    
    public Map<String, War> getAllWars() {
        return new ConcurrentHashMap<>(wars);
    }
    
    // Invitation methods
    public Invitation getInvitation(String inviteId) {
        return invitations.get(inviteId);
    }
    
    public void saveInvitation(Invitation invitation) {
        invitations.put(invitation.getId(), invitation);
        
        CompletableFuture.runAsync(() -> {
            try {
                storageProvider.saveInvitation(invitation);
            } catch (Exception e) {
                logger.severe("Failed to save invitation: " + e.getMessage());
            }
        });
    }
    
    public void deleteInvitation(String inviteId) {
        invitations.remove(inviteId);
        
        CompletableFuture.runAsync(() -> {
            try {
                storageProvider.deleteInvitation(inviteId);
            } catch (Exception e) {
                logger.severe("Failed to delete invitation: " + e.getMessage());
            }
        });
    }
    
    public List<Invitation> getPlayerInvitations(String playerUUID) {
        cleanExpiredInvitations();
        return invitations.values().stream()
                .filter(invitation -> invitation.getTargetUUID().equals(playerUUID))
                .collect(java.util.stream.Collectors.toList());
    }
    
    public Map<String, Invitation> getAllInvitations() {
        return new ConcurrentHashMap<>(invitations);
    }
    
    // Utility methods
    public boolean isCivilizationNameTaken(String name) {
        return civilizations.values().stream()
                .anyMatch(civ -> civ.getName().equalsIgnoreCase(name));
    }
    
    public StorageProvider getStorageProvider() {
        return storageProvider;
    }
}