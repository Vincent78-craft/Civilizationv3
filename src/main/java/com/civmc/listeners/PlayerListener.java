package com.civmc.listeners;

import com.civmc.CivilizationMC;
import com.civmc.model.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.block.Action;

import java.util.logging.Logger;

public class PlayerListener implements Listener {
    
    private final CivilizationMC plugin;
    private final Logger logger;
    
    public PlayerListener(CivilizationMC plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        
        // Check for pending invitations
        var invitations = plugin.getDataManager().getPlayerInvitations(playerUUID);
        if (!invitations.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "You have " + invitations.size() + " pending civilization invitation(s)!");
            player.sendMessage(ChatColor.YELLOW + "Use /cv join to view and accept them.");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();
        
        // Check interaction permissions
        if (!hasInteractionPermission(player, location)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You don't have permission to interact here!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check chunk changes
        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) {
            return;
        }
        
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        Claim fromClaim = getClaim(from);
        Claim toClaim = getClaim(to);
        
        // Player moved to different claim
        if (fromClaim != toClaim) {
            handleClaimEntry(player, fromClaim, toClaim);
        }
    }
    
    private void handleClaimEntry(Player player, Claim fromClaim, Claim toClaim) {
        if (toClaim != null) {
            // Entering claimed territory
            Civilization civ = plugin.getDataManager().getCivilization(toClaim.getCivId());
            if (civ != null) {
                // Check if player is banned from this claim
                String playerUUID = player.getUniqueId().toString();
                
                if (!civ.isMember(playerUUID) && !toClaim.isTrusted(playerUUID, TrustFlag.ACCESS)) {
                    // Check entry permissions
                    if (!toClaim.getFlags().isPublicAccess()) {
                        player.sendMessage(ChatColor.RED + "You are not allowed to enter " + civ.getName() + " territory!");
                        
                        // Teleport back if configured
                        if (plugin.getConfigManager().getConfig().getBoolean("protection.teleport-on-entry-deny", false)) {
                            player.teleport(player.getWorld().getSpawnLocation());
                            return;
                        }
                    }
                }
                
                // Send entry message
                String message = toClaim.getFlags().getEntryMessage();
                if (message != null && !message.isEmpty()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Entering " + ChatColor.WHITE + civ.getName() + 
                            ChatColor.YELLOW + " territory");
                }
            }
        } else if (fromClaim != null) {
            // Leaving claimed territory
            Civilization civ = plugin.getDataManager().getCivilization(fromClaim.getCivId());
            if (civ != null) {
                String message = fromClaim.getFlags().getExitMessage();
                if (message != null && !message.isEmpty()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Leaving " + ChatColor.WHITE + civ.getName() + 
                            ChatColor.YELLOW + " territory");
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        
        if (!hasPermission(player, location, TrustFlag.BUILD)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You don't have permission to place fluids here!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        
        if (!hasPermission(player, location, TrustFlag.BUILD)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You don't have permission to collect fluids here!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        
        Claim claim = getClaim(location);
        if (claim != null && !claim.getFlags().isItemDrop()) {
            if (!hasPermission(player, location, TrustFlag.ACCESS)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You can't drop items here!");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Location location = event.getItem().getLocation();
        
        Claim claim = getClaim(location);
        if (claim != null && !claim.getFlags().isItemPickup()) {
            if (!hasPermission(player, location, TrustFlag.ACCESS)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        Location location = event.getHook().getLocation();
        
        if (!hasPermission(player, location, TrustFlag.USE)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You don't have permission to fish here!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBed().getLocation();
        
        if (!hasPermission(player, location, TrustFlag.USE)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You don't have permission to use beds here!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();
        
        // Check for blocked commands in claims
        Claim claim = getClaim(player.getLocation());
        if (claim != null) {
            var blockedCommands = plugin.getConfigManager().getConfig().getStringList("protection.blocked-commands-in-claims");
            
            for (String blockedCommand : blockedCommands) {
                if (message.startsWith("/" + blockedCommand.toLowerCase())) {
                    if (!hasPermission(player, player.getLocation(), TrustFlag.MANAGE)) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You can't use that command in claimed territory!");
                        return;
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        
        // Check teleportation into claims
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL ||
            event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            
            Claim claim = getClaim(to);
            if (claim != null) {
                String playerUUID = player.getUniqueId().toString();
                Civilization civ = plugin.getDataManager().getCivilization(claim.getCivId());
                
                if (civ != null && !civ.isMember(playerUUID) && 
                    !claim.isTrusted(playerUUID, TrustFlag.ACCESS) &&
                    !claim.getFlags().isTeleportation()) {
                    
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You can't teleport into " + civ.getName() + " territory!");
                }
            }
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
    
    private boolean hasInteractionPermission(Player player, Location location) {
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
        
        // Check for specific interaction types
        org.bukkit.Material material = location.getBlock().getType();
        
        // Containers require USE permission
        if (isContainer(material)) {
            return claim.isTrusted(playerUUID, TrustFlag.USE);
        }
        
        // Buttons, levers, pressure plates require USE permission
        if (isRedstoneComponent(material)) {
            return claim.isTrusted(playerUUID, TrustFlag.USE);
        }
        
        // Doors require ACCESS permission
        if (isDoor(material)) {
            return claim.isTrusted(playerUUID, TrustFlag.ACCESS);
        }
        
        // Default to USE permission
        return claim.isTrusted(playerUUID, TrustFlag.USE);
    }
    
    private boolean isContainer(org.bukkit.Material material) {
        return material.name().contains("CHEST") || 
               material.name().contains("BARREL") ||
               material.name().contains("HOPPER") ||
               material.name().contains("DISPENSER") ||
               material.name().contains("DROPPER") ||
               material.name().contains("FURNACE") ||
               material.name().contains("SHULKER_BOX") ||
               material == org.bukkit.Material.ENDER_CHEST ||
               material == org.bukkit.Material.BREWING_STAND ||
               material == org.bukkit.Material.ANVIL ||
               material == org.bukkit.Material.ENCHANTING_TABLE;
    }
    
    private boolean isRedstoneComponent(org.bukkit.Material material) {
        return material.name().contains("BUTTON") ||
               material.name().contains("LEVER") ||
               material.name().contains("PRESSURE_PLATE") ||
               material.name().contains("TRIPWIRE_HOOK") ||
               material == org.bukkit.Material.REPEATER ||
               material == org.bukkit.Material.COMPARATOR;
    }
    
    private boolean isDoor(org.bukkit.Material material) {
        return material.name().contains("DOOR") ||
               material.name().contains("GATE") ||
               material.name().contains("TRAPDOOR");
    }
    
    private Claim getClaim(Location location) {
        if (location == null || location.getWorld() == null) return null;
        
        int chunkX = location.getChunk().getX();
        int chunkZ = location.getChunk().getZ();
        String worldName = location.getWorld().getName();
        
        return plugin.getDataManager().getClaim(worldName, chunkX, chunkZ);
    }
}