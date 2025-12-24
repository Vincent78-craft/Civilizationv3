package com.civmc.listeners;

import com.civmc.CivilizationMC;
import com.civmc.model.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import java.util.logging.Logger;

public class EntityListener implements Listener {
    
    private final CivilizationMC plugin;
    private final Logger logger;
    
    public EntityListener(CivilizationMC plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        Entity damager = event.getDamager();
        
        Location location = damaged.getLocation();
        Claim claim = getClaim(location);
        
        if (claim == null) return;
        
        Player attacker = getPlayerFromEntity(damager);
        if (attacker == null) return;
        
        String attackerUUID = attacker.getUniqueId().toString();
        
        // Handle PvP
        if (damaged instanceof Player) {
            Player victim = (Player) damaged;
            
            if (!claim.getFlags().isPvp()) {
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.RED + "PvP is disabled in this area!");
                return;
            }
            
            // Check if both players are in the same civilization (friendly fire)
            Civilization attackerCiv = plugin.getDataManager().getPlayerCivilization(attackerUUID);
            Civilization victimCiv = plugin.getDataManager().getPlayerCivilization(victim.getUniqueId().toString());
            
            if (attackerCiv != null && victimCiv != null && attackerCiv.getUuid().equals(victimCiv.getUuid())) {
                if (!claim.getFlags().isFriendlyFire()) {
                    event.setCancelled(true);
                    attacker.sendMessage(ChatColor.RED + "You can't attack your civilization members!");
                    return;
                }
            }
        }
        
        // Handle animal/mob damage
        if (damaged instanceof Animals || damaged instanceof Villager) {
            if (!hasPermission(attacker, location, TrustFlag.KILL_ANIMALS)) {
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.RED + "You don't have permission to attack animals here!");
            }
        } else if (damaged instanceof Monster) {
            if (!hasPermission(attacker, location, TrustFlag.KILL_MONSTERS)) {
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.RED + "You don't have permission to attack monsters here!");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Location location = event.getLocation();
        
        // Remove blocks that are in protected claims
        event.blockList().removeIf(block -> {
            Claim claim = getClaim(block.getLocation());
            return claim != null && !claim.getFlags().isExplosions();
        });
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            Location location = event.getLocation();
            Claim claim = getClaim(location);
            
            if (claim != null) {
                if (event.getEntity() instanceof Monster && !claim.getFlags().isMonsterSpawn()) {
                    event.setCancelled(true);
                } else if (event.getEntity() instanceof Animals && !claim.getFlags().isAnimalSpawn()) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getEntity().getLocation();
        
        if (player != null && !hasPermission(player, location, TrustFlag.BUILD)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You don't have permission to place hanging entities here!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();
        Player player = getPlayerFromEntity(remover);
        
        if (player != null) {
            Location location = event.getEntity().getLocation();
            
            if (!hasPermission(player, location, TrustFlag.BUILD)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to break hanging entities here!");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Entity attacker = event.getAttacker();
        Player player = getPlayerFromEntity(attacker);
        
        if (player != null) {
            Location location = event.getVehicle().getLocation();
            
            if (!hasPermission(player, location, TrustFlag.BUILD)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to destroy vehicles here!");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Location location = event.getBlock().getLocation();
            
            if (!hasPermission(player, location, TrustFlag.USE)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTame(EntityTameEvent event) {
        Player player = (Player) event.getOwner();
        Location location = event.getEntity().getLocation();
        
        if (!hasPermission(player, location, TrustFlag.USE)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You don't have permission to tame animals here!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent event) {
        if (event.getBreeder() instanceof Player) {
            Player player = (Player) event.getBreeder();
            Location location = event.getEntity().getLocation();
            
            if (!hasPermission(player, location, TrustFlag.USE)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to breed animals here!");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        
        if (projectile.getShooter() instanceof Player) {
            Player player = (Player) projectile.getShooter();
            Location location = projectile.getLocation();
            
            if (!hasPermission(player, location, TrustFlag.USE)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to use projectiles here!");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        
        if (potion.getShooter() instanceof Player) {
            Player player = (Player) potion.getShooter();
            Location location = potion.getLocation();
            
            Claim claim = getClaim(location);
            if (claim != null && !claim.getFlags().isPotions()) {
                if (!hasPermission(player, location, TrustFlag.USE)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You don't have permission to use potions here!");
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
        ThrownPotion potion = event.getEntity();
        
        if (potion.getShooter() instanceof Player) {
            Player player = (Player) potion.getShooter();
            Location location = potion.getLocation();
            
            Claim claim = getClaim(location);
            if (claim != null && !claim.getFlags().isPotions()) {
                if (!hasPermission(player, location, TrustFlag.USE)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You don't have permission to use potions here!");
                }
            }
        }
    }
    
    // Utility methods
    
    private Player getPlayerFromEntity(Entity entity) {
        if (entity instanceof Player) {
            return (Player) entity;
        } else if (entity instanceof Projectile) {
            Projectile projectile = (Projectile) entity;
            if (projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        } else if (entity instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) entity;
            if (tnt.getSource() instanceof Player) {
                return (Player) tnt.getSource();
            }
        }
        return null;
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