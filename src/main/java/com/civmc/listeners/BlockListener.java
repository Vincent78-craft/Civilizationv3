package com.civmc.listeners;

import com.civmc.CivilizationMC;
import com.civmc.model.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.logging.Logger;

public class BlockListener implements Listener {
    
    private final CivilizationMC plugin;
    private final Logger logger;
    
    public BlockListener(CivilizationMC plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        
        if (!hasPermission(player, location, TrustFlag.BUILD)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You don't have permission to break blocks here!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        
        if (!hasPermission(player, location, TrustFlag.BUILD)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You don't have permission to place blocks here!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        // Check if piston can extend into claimed territory
        for (var block : event.getBlocks()) {
            Location newLocation = block.getRelative(event.getDirection()).getLocation();
            if (isClaimedAndProtected(newLocation, null)) {
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        // Check if piston can pull blocks through claimed territory
        for (var block : event.getBlocks()) {
            Location newLocation = block.getRelative(event.getDirection()).getLocation();
            if (isClaimedAndProtected(newLocation, null)) {
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Location location = event.getBlock().getLocation();
        Player player = event.getPlayer();
        
        // Check fire spread and ignition
        Claim claim = getClaim(location);
        if (claim != null) {
            if (event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) {
                // Check fire spread flag
                if (!claim.getFlags().isFireSpread()) {
                    event.setCancelled(true);
                    return;
                }
            }
            
            if (player != null && !hasPermission(player, location, TrustFlag.BUILD)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to ignite blocks here!");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Location location = event.getBlock().getLocation();
        Claim claim = getClaim(location);
        
        if (claim != null && !claim.getFlags().isFireSpread()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        Location location = event.getBlock().getLocation();
        Claim claim = getClaim(location);
        
        if (claim != null) {
            Material material = event.getSource().getType();
            
            // Fire spread
            if (material == Material.FIRE && !claim.getFlags().isFireSpread()) {
                event.setCancelled(true);
                return;
            }
            
            // Other spreads (like mushrooms, etc.)
            if (!claim.getFlags().isBlockSpread()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Location fromLocation = event.getBlock().getLocation();
        Location toLocation = event.getToBlock().getLocation();
        
        Claim fromClaim = getClaim(fromLocation);
        Claim toClaim = getClaim(toLocation);
        
        // If flowing into a different claim, check permissions
        if (fromClaim != toClaim && toClaim != null) {
            if (!toClaim.getFlags().isFluidFlow()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        Location location = event.getBlock().getLocation();
        
        // Remove blocks that are in protected claims
        event.blockList().removeIf(block -> {
            Claim claim = getClaim(block.getLocation());
            return claim != null && !claim.getFlags().isExplosions();
        });
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        Player player = event.getPlayer();
        Location location = event.getLocation();
        
        if (player != null && !hasPermission(player, location, TrustFlag.BUILD)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You don't have permission to grow structures here!");
            return;
        }
        
        // Check all blocks that would be grown
        event.getBlocks().removeIf(blockState -> {
            Location blockLocation = blockState.getLocation();
            Claim claim = getClaim(blockLocation);
            
            if (claim != null) {
                if (player != null) {
                    return !hasPermission(player, blockLocation, TrustFlag.BUILD);
                } else {
                    // Natural growth - check plant growth flag
                    return !claim.getFlags().isPlantGrowth();
                }
            }
            
            return false;
        });
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        // Handle ice/snow melting, leaf decay, etc.
        Location location = event.getBlock().getLocation();
        Claim claim = getClaim(location);
        
        if (claim != null) {
            Material material = event.getBlock().getType();
            
            // Ice/Snow melting
            if ((material == Material.ICE || material == Material.SNOW_BLOCK || material == Material.SNOW) 
                    && !claim.getFlags().isIceMelt()) {
                event.setCancelled(true);
                return;
            }
            
            // Leaf decay
            if (material.name().contains("LEAVES") && !claim.getFlags().isLeafDecay()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        // Handle block formation (like cobblestone from lava+water)
        Location location = event.getBlock().getLocation();
        Claim claim = getClaim(location);
        
        if (claim != null && !claim.getFlags().isBlockForm()) {
            event.setCancelled(true);
        }
    }
    
    // Utility methods
    
    private boolean hasPermission(Player player, Location location, TrustFlag flag) {
        if (player.hasPermission("civilization.bypass.protection")) {
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Claim claim = getClaim(location);
        
        if (claim == null) {
            // Not claimed - allow in wilderness
            return plugin.getConfigManager().getConfig().getBoolean("protection.allow-wilderness-building", true);
        }
        
        // Check if player is a member of the civilization
        Civilization civ = plugin.getDataManager().getCivilization(claim.getCivId());
        if (civ != null && civ.isMember(playerUUID)) {
            return true;
        }
        
        // Check trust permissions
        return claim.isTrusted(playerUUID, flag);
    }
    
    private boolean isClaimedAndProtected(Location location, Player player) {
        Claim claim = getClaim(location);
        if (claim == null) return false;
        
        if (player != null) {
            return !hasPermission(player, location, TrustFlag.BUILD);
        }
        
        return true; // Claimed and no player to check permissions for
    }
    
    private Claim getClaim(Location location) {
        if (location == null || location.getWorld() == null) return null;
        
        int chunkX = location.getChunk().getX();
        int chunkZ = location.getChunk().getZ();
        String worldName = location.getWorld().getName();
        
        return plugin.getDataManager().getClaim(worldName, chunkX, chunkZ);
    }
}