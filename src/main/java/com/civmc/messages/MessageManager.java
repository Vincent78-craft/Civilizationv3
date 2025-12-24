package com.civmc.messages;

import com.civmc.CivilizationMC;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageManager {
    
    private final CivilizationMC plugin;
    private FileConfiguration messages;
    private String currentLanguage;
    private final Pattern placeholderPattern = Pattern.compile("\\{([^}]+)\\}");
    
    public MessageManager(CivilizationMC plugin) {
        this.plugin = plugin;
    }
    
    public boolean load() {
        try {
            currentLanguage = plugin.getConfigManager().getLanguage();
            loadMessages(currentLanguage);
            
            plugin.getLogger().info("Messages loaded for language: " + currentLanguage);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load messages: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void loadMessages(String language) {
        String fileName = "messages_" + language + ".yml";
        File messagesFile = new File(plugin.getDataFolder(), fileName);
        
        plugin.getLogger().info("Loading messages from: " + fileName);
        plugin.getLogger().info("File exists: " + messagesFile.exists());
        
        // Save default file if it doesn't exist
        if (!messagesFile.exists()) {
            try {
                plugin.saveResource(fileName, false);
                plugin.getLogger().info("Created " + fileName + " from resources");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save " + fileName + ": " + e.getMessage());
                // Fallback to English
                if (!"en".equals(language)) {
                    plugin.getLogger().info("Falling back to English messages");
                    loadMessages("en");
                    return;
                }
            }
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        plugin.getLogger().info("Loaded " + messages.getKeys(false).size() + " message keys");
        
        // Load defaults from resources
        InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            messages.setDefaults(defConfig);
        }
    }
    
    public void reload() {
        currentLanguage = plugin.getConfigManager().getLanguage();
        loadMessages(currentLanguage);
        plugin.getLogger().info("Messages reloaded for language: " + currentLanguage);
    }
    
    public String getMessage(String key) {
        return getMessage(key, new HashMap<>());
    }
    
    public String getMessage(String key, Map<String, String> placeholders) {
        String message = messages.getString(key);
        
        if (message == null) {
            plugin.getLogger().warning("Missing message key: " + key + " (language: " + currentLanguage + ")");
            return "&cMissing message: " + key;
        }
        
        // Debug: log first message retrieval to verify language
        if ("prefix".equals(key)) {
            plugin.getLogger().info("DEBUG: Retrieved prefix message: " + message + " (language: " + currentLanguage + ")");
        }
        
        // Replace placeholders
        message = replacePlaceholders(message, placeholders);
        
        // Color codes
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        return message;
    }
    
    public String getMessage(String key, String... placeholders) {
        Map<String, String> placeholderMap = new HashMap<>();
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                placeholderMap.put(placeholders[i], placeholders[i + 1]);
            }
        }
        return getMessage(key, placeholderMap);
    }
    
    private String replacePlaceholders(String message, Map<String, String> placeholders) {
        Matcher matcher = placeholderPattern.matcher(message);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = placeholders.getOrDefault(placeholder, "{" + placeholder + "}");
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    public void send(CommandSender sender, String key) {
        sender.sendMessage(getMessage(key));
    }
    
    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(getMessage(key, placeholders));
    }
    
    public void send(CommandSender sender, String key, String... placeholders) {
        sender.sendMessage(getMessage(key, placeholders));
    }
    
    public void sendActionBar(Player player, String key) {
        sendActionBar(player, key, new HashMap<>());
    }
    
    public void sendActionBar(Player player, String key, Map<String, String> placeholders) {
        String message = getMessage(key, placeholders);
        player.sendActionBar(message);
    }
    
    public void sendTitle(Player player, String titleKey, String subtitleKey) {
        sendTitle(player, titleKey, subtitleKey, new HashMap<>());
    }
    
    public void sendTitle(Player player, String titleKey, String subtitleKey, Map<String, String> placeholders) {
        String title = titleKey != null ? getMessage(titleKey, placeholders) : "";
        String subtitle = subtitleKey != null ? getMessage(subtitleKey, placeholders) : "";
        
        player.sendTitle(title, subtitle, 10, 70, 20);
    }
    
    public String getPrefix() {
        return getMessage("prefix");
    }
    
    public String getCurrentLanguage() {
        return currentLanguage;
    }
    
    // Quick access methods for common messages
    public String getNoPermission() {
        return getMessage("no-permission");
    }
    
    public String getPlayerOnly() {
        return getMessage("player-only");
    }
    
    public String getMustBeLeader() {
        return getMessage("must-be-leader");
    }
    
    public String getNotInCiv() {
        return getMessage("not-in-civ");
    }
    
    public String getAlreadyInCiv() {
        return getMessage("already-in-civ");
    }
    
    public String getInvalidArguments() {
        return getMessage("invalid-arguments");
    }
    
    public String getErrorOccurred() {
        return getMessage("error-occurred");
    }
}