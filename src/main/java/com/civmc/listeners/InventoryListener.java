package com.civmc.listeners;

import com.civmc.CivilizationMC;
import com.civmc.model.Claim;
import com.civmc.model.Civilization;
import com.civmc.model.TrustFlag;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

public class InventoryListener implements Listener {
    
    private final CivilizationMC plugin;
    
    public InventoryListener(CivilizationMC plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();
        
        // Check if it's a container that has a location
        if (holder instanceof org.bukkit.block.BlockState) {
            org.bukkit.block.BlockState blockState = (org.bukkit.block.BlockState) holder;
            Location location = blockState.getLocation();
            
            if (!hasPermission(player, location, TrustFlag.USE)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to access containers here!");
                return;
            }
        }
        
        // Handle specific inventory types
        InventoryType type = event.getInventory().getType();
        
        // These require special handling if they have locations
        if (type == InventoryType.CHEST || 
            type == InventoryType.DISPENSER ||
            type == InventoryType.DROPPER ||
            type == InventoryType.HOPPER ||
            type == InventoryType.BARREL ||
            type == InventoryType.SHULKER_BOX ||
            type == InventoryType.FURNACE ||
            type == InventoryType.BLAST_FURNACE ||
            type == InventoryType.SMOKER ||
            type == InventoryType.BREWING ||
            type == InventoryType.ENCHANTING ||
            type == InventoryType.ANVIL ||
            type == InventoryType.BEACON ||
            type == InventoryType.CARTOGRAPHY ||
            type == InventoryType.GRINDSTONE ||
            type == InventoryType.LECTERN ||
            type == InventoryType.LOOM ||
            type == InventoryType.SMITHING ||
            type == InventoryType.STONECUTTER) {
            
            // Already handled above for block-based containers
            return;
        }
        
        // Handle entity-based containers (like horses, llamas, etc.)
        if (holder instanceof org.bukkit.entity.Entity) {
            org.bukkit.entity.Entity entity = (org.bukkit.entity.Entity) holder;
            Location location = entity.getLocation();
            
            if (!hasPermission(player, location, TrustFlag.USE)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to access this entity's inventory!");
            }
        }
    }
    
    private boolean hasPermission(Player player, Location location, TrustFlag flag) {
        if (player.hasPermission("civilization.bypass.protection")) {
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Claim claim = getClaim(location);
        
        if (claim == null) {
            return plugin.getConfigManager().getConfig().getBoolean("protection.allow-wilderness-interaction", true);
        }
        
        // Check if player is a member of the civilization
        Civilization civ = plugin.getDataManager().getCivilization(claim.getCivId());
        if (civ != null && civ.isMember(playerUUID)) {
            return true;
        }
        
        // Check trust permissions
        return claim.isTrusted(playerUUID, flag);
    }
    
    private Claim getClaim(Location location) {
        if (location == null || location.getWorld() == null) return null;
        
        int chunkX = location.getChunk().getX();
        int chunkZ = location.getChunk().getZ();
        String worldName = location.getWorld().getName();
        
        return plugin.getDataManager().getClaim(worldName, chunkX, chunkZ);
    }
}