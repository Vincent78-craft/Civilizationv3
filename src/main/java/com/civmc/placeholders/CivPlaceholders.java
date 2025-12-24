package com.civmc.placeholders;

import com.civmc.CivilizationMC;
import com.civmc.model.Civilization;
import com.civmc.model.CivRole;
import com.civmc.model.Claim;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CivPlaceholders extends PlaceholderExpansion {
    
    private final CivilizationMC plugin;
    
    public CivPlaceholders() {
        this.plugin = CivilizationMC.getInstance();
    }
    
    @Override
    public String getIdentifier() {
        return "civ";
    }
    
    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public boolean canRegister() {
        return plugin != null;
    }
    
    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return "";
        
        String playerUUID = player.getUniqueId().toString();
        Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
        
        // Parse placeholder parameters
        String[] parts = params.split("_");
        String identifier = parts[0].toLowerCase();
        
        switch (identifier) {
            // Player-specific placeholders
            case "name":
                return civ != null ? civ.getName() : "";
            
            case "has":
                return civ != null ? "true" : "false";
            
            case "role":
                if (civ == null) return "";
                CivRole role = civ.getPlayerRole(playerUUID);
                return role != null ? role.name().toLowerCase() : "";
            
            case "level":
                return civ != null ? String.valueOf(civ.getLevel()) : "0";
            
            case "bank":
                if (civ == null) return "0";
                return plugin.getEconomyManager().formatMoney(civ.getBankBalance());
            
            case "members":
                return civ != null ? String.valueOf(civ.getTotalMemberCount()) : "0";
            
            case "claims":
                return civ != null ? String.valueOf(civ.getClaims().size()) : "0";
            
            case "created":
                if (civ == null) return "";
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                return sdf.format(new Date(civ.getCreatedAt()));
            
            case "leader":
                if (civ == null) return "";
                return plugin.getServer().getOfflinePlayer(java.util.UUID.fromString(civ.getLeaderUUID())).getName();
            
            case "wars":
                return civ != null ? String.valueOf(civ.getWars().size()) : "0";
            
            case "allies":
                return civ != null ? String.valueOf(civ.getAllies().size()) : "0";
            
            // Location-based placeholders
            case "location":
                if (parts.length < 2 || !(player instanceof Player)) return "";
                Player onlinePlayer = (Player) player;
                
                switch (parts[1].toLowerCase()) {
                    case "claimed":
                        Claim claim = getClaim(onlinePlayer.getLocation());
                        return claim != null ? "true" : "false";
                    
                    case "owner":
                        claim = getClaim(onlinePlayer.getLocation());
                        if (claim != null) {
                            Civilization ownerCiv = plugin.getDataManager().getCivilization(claim.getCivId());
                            return ownerCiv != null ? ownerCiv.getName() : "";
                        }
                        return "";
                    
                    case "trusted":
                        claim = getClaim(onlinePlayer.getLocation());
                        if (claim != null) {
                            Civilization ownerCiv = plugin.getDataManager().getCivilization(claim.getCivId());
                            if (ownerCiv != null && ownerCiv.isMember(playerUUID)) {
                                return "true";
                            }
                            // Check trust flags would require more specific parameters
                        }
                        return "false";
                }
                break;
            
            // Civilization-specific placeholders (by name)
            case "civ":
                if (parts.length < 3) return "";
                String civName = parts[1];
                String civParam = parts[2].toLowerCase();
                
                Civilization targetCiv = plugin.getDataManager().getCivilizationByName(civName);
                if (targetCiv == null) return "";
                
                switch (civParam) {
                    case "level":
                        return String.valueOf(targetCiv.getLevel());
                    case "members":
                        return String.valueOf(targetCiv.getTotalMemberCount());
                    case "claims":
                        return String.valueOf(targetCiv.getClaims().size());
                    case "bank":
                        return plugin.getEconomyManager().formatMoney(targetCiv.getBankBalance());
                    case "leader":
                        return plugin.getServer().getOfflinePlayer(java.util.UUID.fromString(targetCiv.getLeaderUUID())).getName();
                    case "wars":
                        return String.valueOf(targetCiv.getWars().size());
                    case "allies":
                        return String.valueOf(targetCiv.getAllies().size());
                    case "created":
                        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
                        return sdf2.format(new Date(targetCiv.getCreatedAt()));
                }
                break;
            
            // Top civilizations
            case "top":
                if (parts.length < 3) return "";
                
                try {
                    int position = Integer.parseInt(parts[1]) - 1; // Convert to 0-based index
                    String topParam = parts[2].toLowerCase();
                    
                    var topCivs = plugin.getCivilizationManager().getTopCivilizations(10);
                    if (position < 0 || position >= topCivs.size()) return "";
                    
                    Civilization topCiv = topCivs.get(position);
                    
                    switch (topParam) {
                        case "name":
                            return topCiv.getName();
                        case "level":
                            return String.valueOf(topCiv.getLevel());
                        case "members":
                            return String.valueOf(topCiv.getTotalMemberCount());
                        case "claims":
                            return String.valueOf(topCiv.getClaims().size());
                        case "bank":
                            return plugin.getEconomyManager().formatMoney(topCiv.getBankBalance());
                        case "leader":
                            return plugin.getServer().getOfflinePlayer(java.util.UUID.fromString(topCiv.getLeaderUUID())).getName();
                    }
                } catch (NumberFormatException e) {
                    return "";
                }
                break;
            
            // Global statistics
            case "total":
                if (parts.length < 2) return "";
                
                switch (parts[1].toLowerCase()) {
                    case "civilizations":
                        return String.valueOf(plugin.getDataManager().getAllCivilizations().size());
                    case "claims":
                        return String.valueOf(plugin.getDataManager().getAllClaims().size());
                    case "wars":
                        return String.valueOf(plugin.getDataManager().getAllWars().size());
                    case "players":
                        long totalPlayers = plugin.getDataManager().getAllCivilizations().values().stream()
                                .mapToLong(Civilization::getTotalMemberCount)
                                .sum();
                        return String.valueOf(totalPlayers);
                }
                break;
            
            // Server statistics
            case "server":
                if (parts.length < 2) return "";
                
                switch (parts[1].toLowerCase()) {
                    case "online":
                        long onlineInCivs = plugin.getServer().getOnlinePlayers().stream()
                                .mapToLong(p -> plugin.getDataManager().getPlayerCivilization(p.getUniqueId().toString()) != null ? 1 : 0)
                                .sum();
                        return String.valueOf(onlineInCivs);
                }
                break;
                
            default:
                return "";
        }
        
        return "";
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String params) {
        return onRequest(player, params);
    }
    
    private Claim getClaim(org.bukkit.Location location) {
        if (location == null || location.getWorld() == null) return null;
        
        int chunkX = location.getChunk().getX();
        int chunkZ = location.getChunk().getZ();
        String worldName = location.getWorld().getName();
        
        return plugin.getDataManager().getClaim(worldName, chunkX, chunkZ);
    }
}