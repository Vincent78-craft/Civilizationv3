package com.civmc;

import com.civmc.commands.CivCommand;
import com.civmc.commands.CivAdminCommand;
import com.civmc.config.ConfigManager;
import com.civmc.data.DataManager;
import com.civmc.economy.EconomyManager;
import com.civmc.listeners.*;
import com.civmc.manager.CivilizationManager;
import com.civmc.messages.MessageManager;
import com.civmc.placeholders.CivPlaceholders;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class CivilizationMC extends JavaPlugin {
    
    private static CivilizationMC instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private DataManager dataManager;
    private CivilizationManager civilizationManager;
    private EconomyManager economyManager;
    private Economy economy;
    private Logger logger;
    
    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        
        logger.info("Starting CivilizationMC v" + getDescription().getVersion());
        
        // Initialize managers
        if (!initializeManagers()) {
            logger.severe("Failed to initialize managers. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Setup Vault economy
        if (!setupEconomy()) {
            logger.severe("Vault economy not found! Plugin requires Vault with an economy plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Setup PlaceholderAPI if present
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CivPlaceholders().register();
            logger.info("PlaceholderAPI integration enabled!");
        }
        
        // Start auto-save task
        startAutoSaveTask();
        
        logger.info("CivilizationMC has been enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        logger.info("Disabling CivilizationMC...");
        
        // Save all data
        if (dataManager != null) {
            dataManager.saveAll();
            dataManager.close();
        }
        
        // Cancel all tasks
        Bukkit.getScheduler().cancelTasks(this);
        
        logger.info("CivilizationMC has been disabled.");
    }
    
    private boolean initializeManagers() {
        try {
            // Config manager
            configManager = new ConfigManager(this);
            if (!configManager.load()) {
                return false;
            }
            
            // Message manager
            messageManager = new MessageManager(this);
            if (!messageManager.load()) {
                return false;
            }
            
            // Data manager
            dataManager = new DataManager(this);
            if (!dataManager.initialize()) {
                return false;
            }
            
            // Civilization manager
            civilizationManager = new CivilizationManager(this);
            
            // Economy manager
            economyManager = new EconomyManager(this);
            
            return true;
        } catch (Exception e) {
            logger.severe("Error initializing managers: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        
        economy = rsp.getProvider();
        return economy != null;
    }
    
    private void registerCommands() {
        getCommand("cv").setExecutor(new CivCommand(this));
        getCommand("cvadmin").setExecutor(new CivAdminCommand(this));
        
        logger.info("Commands registered successfully!");
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunkListener(this), this);
        
        logger.info("Event listeners registered successfully!");
    }
    
    private void startAutoSaveTask() {
        int interval = configManager.getConfig().getInt("autosave-minutes", 10) * 20 * 60;
        
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (dataManager != null) {
                dataManager.saveAll();
                logger.info("Auto-saved civilization data.");
            }
        }, interval, interval);
        
        logger.info("Auto-save task started with interval: " + (interval / 1200) + " minutes");
    }
    
    // Getters
    public static CivilizationMC getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public CivilizationManager getCivilizationManager() {
        return civilizationManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public Economy getEconomy() {
        return economy;
    }
}