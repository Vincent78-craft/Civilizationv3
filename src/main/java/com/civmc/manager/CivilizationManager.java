package com.civmc.manager;

import com.civmc.CivilizationMC;
import com.civmc.model.*;
import com.civmc.events.CivEvent;
import com.civmc.events.civilization.*;
import com.civmc.events.claim.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CivilizationManager {
    
    private final CivilizationMC plugin;
    private final Logger logger;
    
    // Cached configurations
    private int maxCivilizationNameLength;
    private int minCivilizationNameLength;
    private int maxMembersPerCiv;
    private int maxClaimsPerCiv;
    private double createCost;
    private double claimCost;
    private int homeWarmupTime;
    private int homeCooldownTime;
    
    // Player cooldowns and warmups
    private final Map<String, Long> homeCooldowns = new HashMap<>(); // playerUUID -> cooldown end time
    private final Map<String, BukkitRunnable> homeWarmups = new HashMap<>(); // playerUUID -> warmup task
    
    public CivilizationManager(CivilizationMC plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        loadConfiguration();
    }
    
    private void loadConfiguration() {
        var config = plugin.getConfigManager().getConfig();
        maxCivilizationNameLength = config.getInt("civilization.max-name-length", 20);
        minCivilizationNameLength = config.getInt("civilization.min-name-length", 3);
        maxMembersPerCiv = config.getInt("civilization.max-members", 50);
        maxClaimsPerCiv = config.getInt("civilization.max-claims", 100);
        createCost = config.getDouble("economy.create-cost", 1000.0);
        claimCost = config.getDouble("economy.claim-cost", 100.0);
        homeWarmupTime = config.getInt("teleport.home-warmup-seconds", 5);
        homeCooldownTime = config.getInt("teleport.home-cooldown-seconds", 300);
        
        logger.info("CivilizationManager configuration loaded");
    }
    
    public void reloadConfiguration() {
        loadConfiguration();
    }
    
    // Civilization Management
    
    public CompletableFuture<CreateCivilizationResult> createCivilization(String playerUUID, String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate name
                var nameValidation = validateCivilizationName(name);
                if (nameValidation != CreateCivilizationResult.SUCCESS) {
                    return nameValidation;
                }
                
                // Check if player is already in a civilization
                if (plugin.getDataManager().getPlayerCivilization(playerUUID) != null) {
                    return CreateCivilizationResult.ALREADY_IN_CIVILIZATION;
                }
                
                // Check if name is taken
                if (plugin.getDataManager().isCivilizationNameTaken(name)) {
                    return CreateCivilizationResult.NAME_TAKEN;
                }
                
                // Check economy
                if (!plugin.getEconomyManager().hasMoney(playerUUID, createCost)) {
                    return CreateCivilizationResult.INSUFFICIENT_FUNDS;
                }
                
                // Create civilization
                Civilization civilization = new Civilization(name, playerUUID);
                
                // Charge money
                if (!plugin.getEconomyManager().withdrawMoney(playerUUID, createCost)) {
                    return CreateCivilizationResult.INSUFFICIENT_FUNDS;
                }
                
                // Save civilization
                plugin.getDataManager().saveCivilization(civilization);
                
                // Fire event on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    CivilizationCreateEvent event = new CivilizationCreateEvent(civilization, Bukkit.getPlayer(UUID.fromString(playerUUID)));
                    Bukkit.getPluginManager().callEvent(event);
                });
                
                logger.info("Player " + playerUUID + " created civilization '" + name + "'");
                return CreateCivilizationResult.SUCCESS;
                
            } catch (Exception e) {
                logger.severe("Error creating civilization: " + e.getMessage());
                e.printStackTrace();
                return CreateCivilizationResult.ERROR;
            }
        });
    }
    
    private CreateCivilizationResult validateCivilizationName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return CreateCivilizationResult.INVALID_NAME;
        }
        
        name = name.trim();
        
        if (name.length() < minCivilizationNameLength) {
            return CreateCivilizationResult.NAME_TOO_SHORT;
        }
        
        if (name.length() > maxCivilizationNameLength) {
            return CreateCivilizationResult.NAME_TOO_LONG;
        }
        
        // Check for invalid characters
        if (!name.matches("^[a-zA-Z0-9_\\-\\s]+$")) {
            return CreateCivilizationResult.INVALID_NAME;
        }
        
        return CreateCivilizationResult.SUCCESS;
    }
    
    public CompletableFuture<Boolean> disbandCivilization(String playerUUID, String civUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Civilization civ = plugin.getDataManager().getCivilization(civUUID);
                if (civ == null) return false;
                
                // Check if player is leader
                if (!civ.getLeaderUUID().equals(playerUUID)) {
                    return false;
                }
                
                // Fire event
                CivilizationDisbandEvent event = new CivilizationDisbandEvent(civ, Bukkit.getPlayer(UUID.fromString(playerUUID)));
                Bukkit.getPluginManager().callEvent(event);
                
                if (event.isCancelled()) {
                    return false;
                }
                
                // Return bank balance to leader if economy is enabled
                if (civ.getBankBalance() > 0) {
                    plugin.getEconomyManager().depositMoney(playerUUID, civ.getBankBalance());
                }
                
                // Unclaim all chunks
                Set<String> claimKeys = new HashSet<>(civ.getClaims());
                for (String claimKey : claimKeys) {
                    unclaimChunk(civUUID, claimKey);
                }
                
                // End all wars
                Set<String> wars = new HashSet<>(civ.getWars());
                for (String warId : wars) {
                    endWar(warId, "Civilization disbanded");
                }
                
                // Remove from allies
                for (String allyUUID : civ.getAllies()) {
                    Civilization ally = plugin.getDataManager().getCivilization(allyUUID);
                    if (ally != null) {
                        ally.getAllies().remove(civUUID);
                        plugin.getDataManager().saveCivilization(ally);
                    }
                }
                
                // Delete civilization
                plugin.getDataManager().deleteCivilization(civUUID);
                
                logger.info("Civilization '" + civ.getName() + "' has been disbanded by " + playerUUID);
                return true;
                
            } catch (Exception e) {
                logger.severe("Error disbanding civilization: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }
    
    public boolean renameCivilization(String playerUUID, String civUUID, String newName) {
        try {
            Civilization civ = plugin.getDataManager().getCivilization(civUUID);
            if (civ == null) return false;
            
            // Check permissions
            CivRole role = civ.getPlayerRole(playerUUID);
            if (role != CivRole.LEADER && role != CivRole.OFFICER) {
                return false;
            }
            
            // Validate name
            if (validateCivilizationName(newName) != CreateCivilizationResult.SUCCESS) {
                return false;
            }
            
            // Check if name is taken
            if (plugin.getDataManager().isCivilizationNameTaken(newName)) {
                return false;
            }
            
            String oldName = civ.getName();
            civ.setName(newName);
            plugin.getDataManager().saveCivilization(civ);
            
            // Fire event
            CivilizationRenameEvent event = new CivilizationRenameEvent(civ, oldName, newName, Bukkit.getPlayer(UUID.fromString(playerUUID)));
            Bukkit.getPluginManager().callEvent(event);
            
            return true;
        } catch (Exception e) {
            logger.severe("Error renaming civilization: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Member Management
    
    public boolean invitePlayer(String inviterUUID, String targetUUID, String civUUID) {
        try {
            Civilization civ = plugin.getDataManager().getCivilization(civUUID);
            if (civ == null) return false;
            
            // Check permissions
            CivRole role = civ.getPlayerRole(inviterUUID);
            if (role == null || role == CivRole.RECRUIT) {
                return false;
            }
            
            // Check if target is already in a civilization
            if (plugin.getDataManager().getPlayerCivilization(targetUUID) != null) {
                return false;
            }
            
            // Check member limit
            if (civ.getTotalMemberCount() >= maxMembersPerCiv) {
                return false;
            }
            
            // Create invitation
            Invitation invitation = new Invitation(civUUID, inviterUUID, targetUUID, System.currentTimeMillis() + 300000); // 5 minutes
            plugin.getDataManager().saveInvitation(invitation);
            
            // Fire event
            CivilizationInviteEvent event = new CivilizationInviteEvent(civ, invitation, 
                    Bukkit.getPlayer(UUID.fromString(inviterUUID)), 
                    Bukkit.getPlayer(UUID.fromString(targetUUID)));
            Bukkit.getPluginManager().callEvent(event);
            
            return true;
        } catch (Exception e) {
            logger.severe("Error inviting player: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean joinCivilization(String playerUUID, String inviteId) {
        try {
            Invitation invitation = plugin.getDataManager().getInvitation(inviteId);
            if (invitation == null || invitation.isExpired()) {
                return false;
            }
            
            if (!invitation.getTargetUUID().equals(playerUUID)) {
                return false;
            }
            
            Civilization civ = plugin.getDataManager().getCivilization(invitation.getCivUUID());
            if (civ == null) return false;
            
            // Check if player is already in a civilization
            if (plugin.getDataManager().getPlayerCivilization(playerUUID) != null) {
                return false;
            }
            
            // Add player as recruit
            civ.addMember(playerUUID, CivRole.RECRUIT);
            plugin.getDataManager().saveCivilization(civ);
            
            // Delete invitation
            plugin.getDataManager().deleteInvitation(inviteId);
            
            // Fire event
            CivilizationJoinEvent event = new CivilizationJoinEvent(civ, Bukkit.getPlayer(UUID.fromString(playerUUID)));
            Bukkit.getPluginManager().callEvent(event);
            
            return true;
        } catch (Exception e) {
            logger.severe("Error joining civilization: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean leaveCivilization(String playerUUID) {
        try {
            Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
            if (civ == null) return false;
            
            // Leaders cannot leave, they must transfer leadership or disband
            if (civ.getLeaderUUID().equals(playerUUID)) {
                return false;
            }
            
            // Remove player
            civ.removeMember(playerUUID);
            plugin.getDataManager().saveCivilization(civ);
            
            // Fire event
            CivilizationLeaveEvent event = new CivilizationLeaveEvent(civ, Bukkit.getPlayer(UUID.fromString(playerUUID)));
            Bukkit.getPluginManager().callEvent(event);
            
            return true;
        } catch (Exception e) {
            logger.severe("Error leaving civilization: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean kickMember(String kicker, String target, String civUUID) {
        try {
            Civilization civ = plugin.getDataManager().getCivilization(civUUID);
            if (civ == null) return false;
            
            CivRole kickerRole = civ.getPlayerRole(kicker);
            CivRole targetRole = civ.getPlayerRole(target);
            
            // Check permissions
            if (kickerRole == null || targetRole == null) return false;
            if (kickerRole == CivRole.RECRUIT || kickerRole == CivRole.MEMBER) return false;
            if (targetRole == CivRole.LEADER) return false; // Cannot kick leader
            if (kickerRole == CivRole.OFFICER && targetRole == CivRole.OFFICER) return false; // Officers cannot kick other officers
            
            // Remove player
            civ.removeMember(target);
            plugin.getDataManager().saveCivilization(civ);
            
            // Fire event
            CivilizationKickEvent event = new CivilizationKickEvent(civ, 
                    Bukkit.getPlayer(UUID.fromString(kicker)), 
                    Bukkit.getPlayer(UUID.fromString(target)));
            Bukkit.getPluginManager().callEvent(event);
            
            return true;
        } catch (Exception e) {
            logger.severe("Error kicking member: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Claim Management
    
    public ClaimResult claimChunk(String playerUUID, Location location) {
        try {
            Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
            if (civ == null) {
                return ClaimResult.NOT_IN_CIVILIZATION;
            }
            
            // Check permissions
            CivRole role = civ.getPlayerRole(playerUUID);
            if (role == CivRole.RECRUIT) {
                return ClaimResult.NO_PERMISSION;
            }
            
            // Check claim limit
            if (civ.getClaims().size() >= maxClaimsPerCiv) {
                return ClaimResult.CLAIM_LIMIT_REACHED;
            }
            
            int chunkX = location.getChunk().getX();
            int chunkZ = location.getChunk().getZ();
            String world = location.getWorld().getName();
            
            // Check if chunk is already claimed
            Claim existingClaim = plugin.getDataManager().getClaim(world, chunkX, chunkZ);
            if (existingClaim != null) {
                return ClaimResult.ALREADY_CLAIMED;
            }
            
            // Check economy
            if (!plugin.getEconomyManager().hasMoney(playerUUID, claimCost)) {
                return ClaimResult.INSUFFICIENT_FUNDS;
            }
            
            // Check adjacency (optional rule)
            if (plugin.getConfigManager().getConfig().getBoolean("claims.require-adjacency", true)) {
                if (!isAdjacentToCivilization(civ.getUuid(), world, chunkX, chunkZ)) {
                    return ClaimResult.NOT_ADJACENT;
                }
            }
            
            // Charge money
            if (!plugin.getEconomyManager().withdrawMoney(playerUUID, claimCost)) {
                return ClaimResult.INSUFFICIENT_FUNDS;
            }
            
            // Create claim
            Claim claim = new Claim(world, chunkX, chunkZ, civ.getUuid());
            plugin.getDataManager().saveClaim(claim);
            
            // Fire event
            ClaimCreateEvent event = new ClaimCreateEvent(claim, Bukkit.getPlayer(UUID.fromString(playerUUID)));
            Bukkit.getPluginManager().callEvent(event);
            
            return ClaimResult.SUCCESS;
            
        } catch (Exception e) {
            logger.severe("Error claiming chunk: " + e.getMessage());
            e.printStackTrace();
            return ClaimResult.ERROR;
        }
    }
    
    private boolean isAdjacentToCivilization(String civUUID, String world, int chunkX, int chunkZ) {
        // Check if this is the first claim for the civilization
        Civilization civ = plugin.getDataManager().getCivilization(civUUID);
        if (civ.getClaims().isEmpty()) {
            return true; // First claim is always allowed
        }
        
        // Check adjacent chunks
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] dir : directions) {
            Claim adjacentClaim = plugin.getDataManager().getClaim(world, chunkX + dir[0], chunkZ + dir[1]);
            if (adjacentClaim != null && adjacentClaim.getCivId().equals(civUUID)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean unclaimChunk(String playerUUID, Location location) {
        try {
            int chunkX = location.getChunk().getX();
            int chunkZ = location.getChunk().getZ();
            String world = location.getWorld().getName();
            String claimKey = world + ":" + chunkX + ":" + chunkZ;
            
            return unclaimChunk(playerUUID, claimKey);
        } catch (Exception e) {
            logger.severe("Error unclaiming chunk: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean unclaimChunk(String civUUID, String claimKey) {
        try {
            Claim claim = plugin.getDataManager().getClaim(claimKey);
            if (claim == null || !claim.getCivId().equals(civUUID)) {
                return false;
            }
            
            // Delete claim
            plugin.getDataManager().deleteClaim(claimKey);
            
            // Fire event
            ClaimDeleteEvent event = new ClaimDeleteEvent(claim);
            Bukkit.getPluginManager().callEvent(event);
            
            return true;
        } catch (Exception e) {
            logger.severe("Error unclaiming chunk: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Home Management
    
    public boolean setCivilizationHome(String playerUUID, Location location) {
        try {
            Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
            if (civ == null) return false;
            
            // Check permissions
            CivRole role = civ.getPlayerRole(playerUUID);
            if (role != CivRole.LEADER && role != CivRole.OFFICER) {
                return false;
            }
            
            // Check if location is in claimed territory
            int chunkX = location.getChunk().getX();
            int chunkZ = location.getChunk().getZ();
            String world = location.getWorld().getName();
            
            Claim claim = plugin.getDataManager().getClaim(world, chunkX, chunkZ);
            if (claim == null || !claim.getCivId().equals(civ.getUuid())) {
                return false;
            }
            
            // Set home
            CivHome home = new CivHome(location.getWorld().getName(), 
                    location.getX(), location.getY(), location.getZ(), 
                    location.getYaw(), location.getPitch());
            
            civ.setHome(home);
            plugin.getDataManager().saveCivilization(civ);
            
            return true;
        } catch (Exception e) {
            logger.severe("Error setting civilization home: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public void teleportToHome(String playerUUID) {
        try {
            Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
            if (player == null) return;
            
            Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
            if (civ == null) {
                player.sendMessage(ChatColor.RED + "You are not in a civilization!");
                return;
            }
            
            CivHome home = civ.getHome();
            if (home == null) {
                player.sendMessage(ChatColor.RED + "Your civilization has no home set!");
                return;
            }
            
            // Check cooldown
            if (homeCooldowns.containsKey(playerUUID) && homeCooldowns.get(playerUUID) > System.currentTimeMillis()) {
                long remaining = (homeCooldowns.get(playerUUID) - System.currentTimeMillis()) / 1000;
                player.sendMessage(ChatColor.RED + "You must wait " + remaining + " seconds before using home again!");
                return;
            }
            
            // Cancel existing warmup
            BukkitRunnable existingWarmup = homeWarmups.remove(playerUUID);
            if (existingWarmup != null) {
                existingWarmup.cancel();
            }
            
            Location startLocation = player.getLocation().clone();
            player.sendMessage(ChatColor.YELLOW + "Teleporting to home in " + homeWarmupTime + " seconds. Don't move!");
            
            BukkitRunnable warmupTask = new BukkitRunnable() {
                int countdown = homeWarmupTime;
                
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        cancel();
                        homeWarmups.remove(playerUUID);
                        return;
                    }
                    
                    // Check if player moved
                    if (startLocation.distance(player.getLocation()) > 0.5) {
                        player.sendMessage(ChatColor.RED + "Teleportation cancelled - you moved!");
                        cancel();
                        homeWarmups.remove(playerUUID);
                        return;
                    }
                    
                    countdown--;
                    
                    if (countdown <= 0) {
                        // Teleport
                        World world = Bukkit.getWorld(home.getWorldName());
                        if (world != null) {
                            Location homeLocation = new Location(world, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch());
                            player.teleport(homeLocation);
                            player.sendMessage(ChatColor.GREEN + "Teleported to civilization home!");
                            
                            // Set cooldown
                            homeCooldowns.put(playerUUID, System.currentTimeMillis() + (homeCooldownTime * 1000L));
                        } else {
                            player.sendMessage(ChatColor.RED + "Home world not found!");
                        }
                        
                        cancel();
                        homeWarmups.remove(playerUUID);
                    } else if (countdown <= 3) {
                        player.sendMessage(ChatColor.YELLOW + "Teleporting in " + countdown + "...");
                    }
                }
            };
            
            homeWarmups.put(playerUUID, warmupTask);
            warmupTask.runTaskTimer(plugin, 0L, 20L);
            
        } catch (Exception e) {
            logger.severe("Error teleporting to home: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // War Management
    
    public boolean declareWar(String declarer, String targetCivUUID, String reason) {
        try {
            Civilization declarerCiv = plugin.getDataManager().getPlayerCivilization(declarer);
            Civilization targetCiv = plugin.getDataManager().getCivilization(targetCivUUID);
            
            if (declarerCiv == null || targetCiv == null) return false;
            
            // Check permissions
            CivRole role = declarerCiv.getPlayerRole(declarer);
            if (role != CivRole.LEADER && role != CivRole.OFFICER) {
                return false;
            }
            
            // Check if already at war
            if (declarerCiv.getWars().stream().anyMatch(warId -> {
                War war = plugin.getDataManager().getWar(warId);
                return war != null && war.isInvolvedCiv(targetCivUUID) && war.getState() == WarState.ACTIVE;
            })) {
                return false;
            }
            
            // Create war
            War war = new War(declarerCiv.getUuid(), targetCivUUID, reason);
            plugin.getDataManager().saveWar(war);
            
            // Add to civilizations
            declarerCiv.getWars().add(war.getId());
            targetCiv.getWars().add(war.getId());
            
            plugin.getDataManager().saveCivilization(declarerCiv);
            plugin.getDataManager().saveCivilization(targetCiv);
            
            return true;
        } catch (Exception e) {
            logger.severe("Error declaring war: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean endWar(String warId, String reason) {
        try {
            War war = plugin.getDataManager().getWar(warId);
            if (war == null) return false;
            
            war.setState(WarState.ENDED);
            war.setEndReason(reason);
            war.setEndTime(System.currentTimeMillis());
            
            plugin.getDataManager().saveWar(war);
            return true;
        } catch (Exception e) {
            logger.severe("Error ending war: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Utility Methods
    
    public List<Civilization> getTopCivilizations(int limit) {
        return plugin.getDataManager().getAllCivilizations().values().stream()
                .sorted((a, b) -> Integer.compare(b.getTotalMemberCount(), a.getTotalMemberCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    public boolean hasPermission(String playerUUID, String civUUID, String permission) {
        Civilization civ = plugin.getDataManager().getCivilization(civUUID);
        if (civ == null) return false;
        
        CivRole role = civ.getPlayerRole(playerUUID);
        if (role == null) return false;
        
        // Define permission hierarchy
        return switch (permission) {
            case "disband", "transfer_leadership" -> role == CivRole.LEADER;
            case "invite", "kick_members", "promote", "demote", "set_home", "manage_bank", "declare_war" -> 
                role == CivRole.LEADER || role == CivRole.OFFICER;
            case "claim", "unclaim", "use_home" -> 
                role == CivRole.LEADER || role == CivRole.OFFICER || role == CivRole.MEMBER;
            default -> false;
        };
    }
    
    // Result Enums
    
    public enum CreateCivilizationResult {
        SUCCESS, ALREADY_IN_CIVILIZATION, NAME_TAKEN, INVALID_NAME, NAME_TOO_SHORT, 
        NAME_TOO_LONG, INSUFFICIENT_FUNDS, ERROR
    }
    
    public enum ClaimResult {
        SUCCESS, NOT_IN_CIVILIZATION, NO_PERMISSION, ALREADY_CLAIMED, 
        CLAIM_LIMIT_REACHED, INSUFFICIENT_FUNDS, NOT_ADJACENT, ERROR
    }
}