package com.civmc.config;

import com.civmc.CivilizationMC;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ConfigManager {
    
    private final CivilizationMC plugin;
    private FileConfiguration config;
    
    public ConfigManager(CivilizationMC plugin) {
        this.plugin = plugin;
    }
    
    public boolean load() {
        try {
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            config = plugin.getConfig();
            
            // Validate configuration
            if (!validateConfig()) {
                plugin.getLogger().severe("Configuration validation failed!");
                return false;
            }
            
            plugin.getLogger().info("Configuration loaded successfully!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load configuration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean validateConfig() {
        // Check required sections
        String[] requiredSections = {
            "language", "storage", "economy", "create-cost",
            "upgrades", "worlds", "protection", "war", "alliance"
        };
        
        for (String section : requiredSections) {
            if (!config.contains(section)) {
                plugin.getLogger().warning("Missing config section: " + section);
            }
        }
        
        // Check storage type
        String storageType = config.getString("storage.type", "JSON");
        if (!storageType.matches("(?i)(JSON|SQLITE|MYSQL)")) {
            plugin.getLogger().warning("Invalid storage type: " + storageType + ". Using JSON.");
            config.set("storage.type", "JSON");
        }
        
        return true;
    }
    
    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        plugin.getLogger().info("Configuration reloaded!");
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    // Language settings
    public String getLanguage() {
        String language = config.getString("language", "en");
        plugin.getLogger().info("DEBUG: Config language setting: " + language);
        return language;
    }
    
    // Storage settings
    public String getStorageType() {
        return config.getString("storage.type", "JSON");
    }
    
    public String getDatabaseUrl() {
        return config.getString("storage.mysql.url", "jdbc:mysql://localhost:3306/civilizations");
    }
    
    public String getDatabaseUsername() {
        return config.getString("storage.mysql.username", "root");
    }
    
    public String getDatabasePassword() {
        return config.getString("storage.mysql.password", "");
    }
    
    // Economy settings
    public boolean isEconomyEnabled() {
        return config.getBoolean("economy.enabled", true);
    }
    
    public double getCreateCost() {
        return config.getDouble("create-cost", 0.0);
    }
    
    public boolean isCostPerClaimEnabled() {
        return config.getBoolean("cost-per-claim.enabled", false);
    }
    
    public double getCostPerClaim() {
        return config.getDouble("cost-per-claim.cost", 100.0);
    }
    
    // Upgrade settings
    public Map<Integer, UpgradeLevel> getUpgradeLevels() {
        Map<Integer, UpgradeLevel> levels = new HashMap<>();
        
        if (config.contains("upgrades.levels")) {
            for (String key : config.getConfigurationSection("upgrades.levels").getKeys(false)) {
                try {
                    int level = Integer.parseInt(key);
                    String path = "upgrades.levels." + key;
                    
                    UpgradeLevel upgradeLevel = new UpgradeLevel(
                        level,
                        config.getInt(path + ".claims-max", 5),
                        config.getDouble(path + ".cost", 0.0),
                        config.getStringList(path + ".bonuses")
                    );
                    
                    levels.put(level, upgradeLevel);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid upgrade level: " + key);
                }
            }
        }
        
        // Default levels if none configured
        if (levels.isEmpty()) {
            levels.put(1, new UpgradeLevel(1, 5, 0, List.of("banner")));
            levels.put(2, new UpgradeLevel(2, 10, 500, List.of()));
            levels.put(3, new UpgradeLevel(3, 20, 1000, List.of()));
            levels.put(4, new UpgradeLevel(4, 35, 2500, List.of()));
            levels.put(5, new UpgradeLevel(5, 55, 5000, List.of()));
            levels.put(6, new UpgradeLevel(6, 80, 10000, List.of()));
            levels.put(7, new UpgradeLevel(7, 115, 17500, List.of()));
            levels.put(8, new UpgradeLevel(8, 150, 25000, List.of("tax_cap:+5%")));
        }
        
        return levels;
    }
    
    // World settings
    public boolean isWorldClaimEnabled(String worldName) {
        return config.getBoolean("worlds." + worldName + ".claim-enabled", true);
    }
    
    public List<String> getEnabledWorlds() {
        return config.getStringList("worlds.enabled");
    }
    
    // Protection settings
    public boolean isProtectionEnabled() {
        return config.getBoolean("protection.enabled", true);
    }
    
    public int getMessageCooldown() {
        return config.getInt("protection.message-cooldown-seconds", 2);
    }
    
    // War settings
    public boolean isWarEnabled() {
        return config.getBoolean("war.enabled", true);
    }
    
    public int getWarWarmupMinutes() {
        return config.getInt("war.warmup-minutes", 15);
    }
    
    public int getWarCooldownMinutes() {
        return config.getInt("war.cooldown-minutes", 60);
    }
    
    public boolean isPvpEverywhere() {
        return config.getBoolean("war.pvp-everywhere", false);
    }
    
    // Alliance settings
    public boolean isAllianceEnabled() {
        return config.getBoolean("alliance.enabled", true);
    }
    
    public int getMaxAlliances() {
        return config.getInt("alliance.max-per-civ", 3);
    }
    
    // Home settings
    public int getHomeCooldownSeconds() {
        return config.getInt("home.cooldown-seconds", 10);
    }
    
    public int getHomeWarmupSeconds() {
        return config.getInt("home.warmup-seconds", 3);
    }
    
    // Invitation settings
    public int getInviteExpireMinutes() {
        return config.getInt("invite.expire-minutes", 60);
    }
    
    // Chat settings
    public String getChatPrefix() {
        return config.getString("chat.prefix", "&8[&6CIV&8]&r");
    }
    
    public boolean isSpyChatEnabled() {
        return config.getBoolean("chat.spy-enabled", true);
    }
    
    // Bank settings
    public double getWithdrawLimit() {
        return config.getDouble("bank.withdraw-limit", 10000.0);
    }
    
    public int getWithdrawCooldownMinutes() {
        return config.getInt("bank.withdraw-cooldown-minutes", 0);
    }
    
    // Misc settings
    public int getAutosaveMinutes() {
        return config.getInt("autosave-minutes", 10);
    }
    
    public boolean isMaintenanceMode() {
        return config.getBoolean("maintenance-mode", false);
    }
    
    public int getMapRadius() {
        return config.getInt("map.ascii-radius", 5);
    }
    
    public static class UpgradeLevel {
        private final int level;
        private final int claimsMax;
        private final double cost;
        private final List<String> bonuses;
        
        public UpgradeLevel(int level, int claimsMax, double cost, List<String> bonuses) {
            this.level = level;
            this.claimsMax = claimsMax;
            this.cost = cost;
            this.bonuses = bonuses;
        }
        
        public int getLevel() {
            return level;
        }
        
        public int getClaimsMax() {
            return claimsMax;
        }
        
        public double getCost() {
            return cost;
        }
        
        public List<String> getBonuses() {
            return bonuses;
        }
    }
}