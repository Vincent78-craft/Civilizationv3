package com.civmc.data.storage;

import com.civmc.CivilizationMC;
import com.civmc.model.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class JsonStorageProvider implements StorageProvider {
    
    private final CivilizationMC plugin;
    private final Logger logger;
    private final Gson gson;
    private final File dataFolder;
    
    // File paths
    private File civilizationsFile;
    private File claimsFile;
    private File warsFile;
    private File invitationsFile;
    private File backupFolder;
    
    public JsonStorageProvider(CivilizationMC plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        
        // Configure Gson with custom serializers
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
                
        this.dataFolder = plugin.getDataFolder();
    }
    
    @Override
    public boolean initialize() {
        try {
            // Create data folder if it doesn't exist
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                logger.severe("Failed to create data folder!");
                return false;
            }
            
            // Initialize file objects
            civilizationsFile = new File(dataFolder, "civilizations.json");
            claimsFile = new File(dataFolder, "claims.json");
            warsFile = new File(dataFolder, "wars.json");
            invitationsFile = new File(dataFolder, "invitations.json");
            backupFolder = new File(dataFolder, "backups");
            
            // Create backup folder
            if (!backupFolder.exists() && !backupFolder.mkdirs()) {
                logger.warning("Failed to create backup folder!");
            }
            
            // Create empty files if they don't exist
            createFileIfNotExists(civilizationsFile, "{}");
            createFileIfNotExists(claimsFile, "{}");
            createFileIfNotExists(warsFile, "{}");
            createFileIfNotExists(invitationsFile, "{}");
            
            logger.info("JSON storage provider initialized successfully!");
            return true;
        } catch (Exception e) {
            logger.severe("Failed to initialize JSON storage provider: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public void close() {
        logger.info("JSON storage provider closed.");
    }
    
    private void createFileIfNotExists(File file, String defaultContent) throws IOException {
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(defaultContent);
            }
        }
    }
    
    private <T> T readJsonFile(File file, Type type) {
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, type);
        } catch (Exception e) {
            logger.warning("Failed to read JSON file " + file.getName() + ": " + e.getMessage());
            return null;
        }
    }
    
    private boolean writeJsonFile(File file, Object data) {
        try {
            // Create backup of existing file
            if (file.exists()) {
                File backupFile = new File(backupFolder, file.getName() + ".backup");
                Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Write to temporary file first
            File tempFile = new File(file.getParentFile(), file.getName() + ".tmp");
            try (FileWriter writer = new FileWriter(tempFile)) {
                gson.toJson(data, writer);
            }
            
            // Atomically move temp file to final location
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            return true;
        } catch (Exception e) {
            logger.severe("Failed to write JSON file " + file.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Civilizations
    @Override
    public Map<String, Civilization> loadCivilizations() {
        Type type = new TypeToken<Map<String, Civilization>>(){}.getType();
        Map<String, Civilization> result = readJsonFile(civilizationsFile, type);
        
        if (result == null) {
            result = new ConcurrentHashMap<>();
        }
        
        // Ensure thread-safe collections
        for (Civilization civ : result.values()) {
            if (civ.getOfficers() == null) civ.setOfficers(ConcurrentHashMap.newKeySet());
            if (civ.getMembers() == null) civ.setMembers(ConcurrentHashMap.newKeySet());
            if (civ.getRecruits() == null) civ.setRecruits(ConcurrentHashMap.newKeySet());
            if (civ.getClaims() == null) civ.setClaims(ConcurrentHashMap.newKeySet());
            if (civ.getAllies() == null) civ.setAllies(ConcurrentHashMap.newKeySet());
            if (civ.getWars() == null) civ.setWars(ConcurrentHashMap.newKeySet());
            if (civ.getTransactions() == null) civ.setTransactions(new ArrayList<>());
            if (civ.getCustomData() == null) civ.setCustomData(new ConcurrentHashMap<>());
            if (civ.getSettings() == null) civ.setSettings(new CivSettings());
        }
        
        logger.info("Loaded " + result.size() + " civilizations from JSON storage");
        return result;
    }
    
    @Override
    public void saveCivilizations(Map<String, Civilization> civilizations) {
        if (writeJsonFile(civilizationsFile, civilizations)) {
            logger.info("Saved " + civilizations.size() + " civilizations to JSON storage");
        }
    }
    
    @Override
    public void saveCivilization(Civilization civilization) {
        Map<String, Civilization> civilizations = loadCivilizations();
        civilizations.put(civilization.getUuid(), civilization);
        saveCivilizations(civilizations);
    }
    
    @Override
    public void deleteCivilization(String uuid) {
        Map<String, Civilization> civilizations = loadCivilizations();
        if (civilizations.remove(uuid) != null) {
            saveCivilizations(civilizations);
            logger.info("Deleted civilization " + uuid + " from JSON storage");
        }
    }
    
    // Claims
    @Override
    public Map<String, Claim> loadClaims() {
        Type type = new TypeToken<Map<String, Claim>>(){}.getType();
        Map<String, Claim> result = readJsonFile(claimsFile, type);
        
        if (result == null) {
            result = new ConcurrentHashMap<>();
        }
        
        // Ensure thread-safe collections
        for (Claim claim : result.values()) {
            if (claim.getTrusts() == null) claim.setTrusts(ConcurrentHashMap.newKeySet());
            if (claim.getFlags() == null) claim.setFlags(new ClaimFlags());
        }
        
        logger.info("Loaded " + result.size() + " claims from JSON storage");
        return result;
    }
    
    @Override
    public void saveClaims(Map<String, Claim> claims) {
        if (writeJsonFile(claimsFile, claims)) {
            logger.info("Saved " + claims.size() + " claims to JSON storage");
        }
    }
    
    @Override
    public void saveClaim(Claim claim) {
        Map<String, Claim> claims = loadClaims();
        claims.put(claim.getClaimKey(), claim);
        saveClaims(claims);
    }
    
    @Override
    public void deleteClaim(String claimKey) {
        Map<String, Claim> claims = loadClaims();
        if (claims.remove(claimKey) != null) {
            saveClaims(claims);
            logger.info("Deleted claim " + claimKey + " from JSON storage");
        }
    }
    
    // Wars
    @Override
    public Map<String, War> loadWars() {
        Type type = new TypeToken<Map<String, War>>(){}.getType();
        Map<String, War> result = readJsonFile(warsFile, type);
        
        if (result == null) {
            result = new ConcurrentHashMap<>();
        }
        
        // Ensure thread-safe collections
        for (War war : result.values()) {
            if (war.getAttackers() == null) war.setAttackers(ConcurrentHashMap.newKeySet());
            if (war.getDefenders() == null) war.setDefenders(ConcurrentHashMap.newKeySet());
        }
        
        logger.info("Loaded " + result.size() + " wars from JSON storage");
        return result;
    }
    
    @Override
    public void saveWars(Map<String, War> wars) {
        if (writeJsonFile(warsFile, wars)) {
            logger.info("Saved " + wars.size() + " wars to JSON storage");
        }
    }
    
    @Override
    public void saveWar(War war) {
        Map<String, War> wars = loadWars();
        wars.put(war.getId(), war);
        saveWars(wars);
    }
    
    @Override
    public void deleteWar(String warId) {
        Map<String, War> wars = loadWars();
        if (wars.remove(warId) != null) {
            saveWars(wars);
            logger.info("Deleted war " + warId + " from JSON storage");
        }
    }
    
    // Invitations
    @Override
    public Map<String, Invitation> loadInvitations() {
        Type type = new TypeToken<Map<String, Invitation>>(){}.getType();
        Map<String, Invitation> result = readJsonFile(invitationsFile, type);
        
        if (result == null) {
            result = new ConcurrentHashMap<>();
        }
        
        logger.info("Loaded " + result.size() + " invitations from JSON storage");
        return result;
    }
    
    @Override
    public void saveInvitations(Map<String, Invitation> invitations) {
        if (writeJsonFile(invitationsFile, invitations)) {
            logger.info("Saved " + invitations.size() + " invitations to JSON storage");
        }
    }
    
    @Override
    public void saveInvitation(Invitation invitation) {
        Map<String, Invitation> invitations = loadInvitations();
        invitations.put(invitation.getId(), invitation);
        saveInvitations(invitations);
    }
    
    @Override
    public void deleteInvitation(String inviteId) {
        Map<String, Invitation> invitations = loadInvitations();
        if (invitations.remove(inviteId) != null) {
            saveInvitations(invitations);
            logger.info("Deleted invitation " + inviteId + " from JSON storage");
        }
    }
    
    @Override
    public void backup() {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String backupSuffix = "_" + timestamp + ".backup";
            
            // Backup each data file
            Files.copy(civilizationsFile.toPath(), 
                      new File(backupFolder, "civilizations" + backupSuffix).toPath(),
                      StandardCopyOption.REPLACE_EXISTING);
            Files.copy(claimsFile.toPath(), 
                      new File(backupFolder, "claims" + backupSuffix).toPath(),
                      StandardCopyOption.REPLACE_EXISTING);
            Files.copy(warsFile.toPath(), 
                      new File(backupFolder, "wars" + backupSuffix).toPath(),
                      StandardCopyOption.REPLACE_EXISTING);
            Files.copy(invitationsFile.toPath(), 
                      new File(backupFolder, "invitations" + backupSuffix).toPath(),
                      StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("Created backup with timestamp: " + timestamp);
            
            // Clean old backups (keep only last 10)
            cleanOldBackups();
            
        } catch (Exception e) {
            logger.severe("Failed to create backup: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void cleanOldBackups() {
        File[] backupFiles = backupFolder.listFiles((dir, name) -> name.endsWith(".backup"));
        if (backupFiles != null && backupFiles.length > 40) { // 4 files * 10 backups
            Arrays.sort(backupFiles, Comparator.comparing(File::lastModified));
            
            // Delete oldest backups, keeping the newest 40 files
            for (int i = 0; i < backupFiles.length - 40; i++) {
                if (backupFiles[i].delete()) {
                    logger.info("Deleted old backup: " + backupFiles[i].getName());
                }
            }
        }
    }
    
    @Override
    public boolean migrate(StorageProvider newProvider) {
        try {
            logger.info("Starting migration from JSON to " + newProvider.getClass().getSimpleName());
            
            // Create backup before migration
            backup();
            
            // Load all data
            Map<String, Civilization> civilizations = loadCivilizations();
            Map<String, Claim> claims = loadClaims();
            Map<String, War> wars = loadWars();
            Map<String, Invitation> invitations = loadInvitations();
            
            // Initialize new provider
            if (!newProvider.initialize()) {
                logger.severe("Failed to initialize new storage provider for migration!");
                return false;
            }
            
            // Save to new provider
            newProvider.saveCivilizations(civilizations);
            newProvider.saveClaims(claims);
            newProvider.saveWars(wars);
            newProvider.saveInvitations(invitations);
            
            logger.info("Successfully migrated " + civilizations.size() + " civilizations, " +
                       claims.size() + " claims, " + wars.size() + " wars, and " +
                       invitations.size() + " invitations");
            
            return true;
        } catch (Exception e) {
            logger.severe("Migration failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}