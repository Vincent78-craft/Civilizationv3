package com.civmc.gui;

import com.civmc.CivilizationMC;
import com.civmc.model.Civilization;
import com.civmc.model.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClaimsGUI extends CivGUI {
    
    private final Civilization civilization;
    private int page = 0;
    private final int claimsPerPage = 28; // 4 rows of 7 items
    
    public ClaimsGUI(CivilizationMC plugin, Player player, Civilization civilization) {
        super(plugin, player, "&6" + civilization.getName() + " - Claims", 54);
        this.civilization = civilization;
    }
    
    @Override
    protected void setupGUI() {
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);
        
        // Get all claims
        Set<String> claimKeys = civilization.getClaims();
        List<Claim> claims = new ArrayList<>();
        
        for (String claimKey : claimKeys) {
            Claim claim = plugin.getDataManager().getClaim(claimKey);
            if (claim != null) {
                claims.add(claim);
            }
        }
        
        // Sort claims by world, then by coordinates
        claims.sort((a, b) -> {
            int worldCompare = a.getWorld().compareTo(b.getWorld());
            if (worldCompare != 0) return worldCompare;
            
            int xCompare = Integer.compare(a.getChunkX(), b.getChunkX());
            if (xCompare != 0) return xCompare;
            
            return Integer.compare(a.getChunkZ(), b.getChunkZ());
        });
        
        // Calculate pagination
        int totalPages = claims.isEmpty() ? 1 : (int) Math.ceil((double) claims.size() / claimsPerPage);
        int startIndex = page * claimsPerPage;
        int endIndex = Math.min(startIndex + claimsPerPage, claims.size());
        
        if (claims.isEmpty()) {
            // No claims
            setItem(22, Material.BARRIER, "&cNo Claims",
                    "&7This civilization has no claimed chunks",
                    "&7Use /cv claim to claim chunks");
            setUnclickable(22);
        } else {
            // Display claims
            int slot = 10; // Starting slot (skip border)
            for (int i = startIndex; i < endIndex; i++) {
                Claim claim = claims.get(i);
                
                // Skip border slots
                if (slot % 9 == 0 || slot % 9 == 8) {
                    slot++;
                }
                
                // Display claim
                Material claimMaterial = getClaimMaterial(claim.getWorld());
                
                setItem(slot, claimMaterial, "&e" + claim.getWorld() + " (" + claim.getChunkX() + ", " + claim.getChunkZ() + ")",
                        "&7World: &f" + claim.getWorld(),
                        "&7Coordinates: &f" + claim.getChunkX() + ", " + claim.getChunkZ(),
                        "&7Trusts: &f" + claim.getTrusts().size(),
                        "&7Claimed: &f" + formatDate(claim.getCreatedAt()),
                        "",
                        "&7Left-click to teleport",
                        "&7Right-click to manage (if you have permission)");
                
                final Claim finalClaim = claim;
                setClickAction(slot, p -> handleClaimClick(finalClaim, false));
                
                slot++;
            }
        }
        
        // Navigation
        if (page > 0) {
            setItem(45, Material.ARROW, "&ePrevious Page", "&7Click to go to page " + page);
            setClickAction(45, p -> {
                this.page--;
                setupGUI();
            });
        }
        
        if (page < totalPages - 1) {
            setItem(53, Material.ARROW, "&eNext Page", "&7Click to go to page " + (page + 2));
            setClickAction(53, p -> {
                this.page++;
                setupGUI();
            });
        }
        
        // Page info
        setItem(49, Material.BOOK, "&ePage " + (page + 1) + " of " + totalPages,
                "&7Total claims: &f" + claims.size(),
                "&7Showing: &f" + (claims.isEmpty() ? "0" : (startIndex + 1) + "-" + endIndex));
        setUnclickable(49);
        
        // Statistics
        setItem(47, Material.MAP, "&eStatistics",
                "&7Total Claims: &f" + claims.size(),
                "&7Worlds: &f" + getUniqueWorlds(claims),
                "&7Claim Cost: &f" + plugin.getEconomyManager().formatMoney(
                        plugin.getConfigManager().getConfig().getDouble("economy.claim-cost", 100.0)));
        setUnclickable(47);
        
        // Back to civilization info
        addBackButton(46, () -> new CivilizationInfoGUI(plugin, player, civilization).open());
        
        // Close button
        addCloseButton(52);
    }
    
    private Material getClaimMaterial(String worldName) {
        switch (worldName.toLowerCase()) {
            case "world":
                return Material.GRASS_BLOCK;
            case "world_nether":
            case "nether":
                return Material.NETHERRACK;
            case "world_the_end":
            case "the_end":
            case "end":
                return Material.END_STONE;
            default:
                return Material.MAP;
        }
    }
    
    private String formatDate(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long days = diff / (24 * 60 * 60 * 1000);
        
        if (days == 0) {
            return "Today";
        } else if (days == 1) {
            return "Yesterday";
        } else if (days < 7) {
            return days + " days ago";
        } else if (days < 30) {
            return (days / 7) + " weeks ago";
        } else {
            return (days / 30) + " months ago";
        }
    }
    
    private int getUniqueWorlds(List<Claim> claims) {
        return (int) claims.stream()
                .map(Claim::getWorld)
                .distinct()
                .count();
    }
    
    private void handleClaimClick(Claim claim, boolean rightClick) {
        String playerUUID = player.getUniqueId().toString();
        
        if (!rightClick) {
            // Left click - teleport to claim
            if (player.hasPermission("civilization.claims.teleport") || 
                civilization.isMember(playerUUID)) {
                
                // Get world and calculate center coordinates
                org.bukkit.World world = plugin.getServer().getWorld(claim.getWorld());
                if (world != null) {
                    int centerX = claim.getChunkX() * 16 + 8;
                    int centerZ = claim.getChunkZ() * 16 + 8;
                    int y = world.getHighestBlockYAt(centerX, centerZ) + 1;
                    
                    org.bukkit.Location teleportLocation = new org.bukkit.Location(world, centerX, y, centerZ);
                    player.teleport(teleportLocation);
                    player.sendMessage("&aTeleported to claim at " + claim.getChunkX() + ", " + claim.getChunkZ() + " in " + claim.getWorld());
                } else {
                    player.sendMessage("&cWorld '" + claim.getWorld() + "' not found!");
                }
            } else {
                player.sendMessage("&cYou don't have permission to teleport to claims!");
            }
        } else {
            // Right click - manage claim (future implementation)
            if (plugin.getCivilizationManager().hasPermission(playerUUID, civilization.getUuid(), "manage_claims")) {
                player.sendMessage("&cClaim management GUI not yet implemented!");
                // TODO: Open claim management GUI
            } else {
                player.sendMessage("&cYou don't have permission to manage claims!");
            }
        }
    }
    
    public static void open(CivilizationMC plugin, Player player, Civilization civilization) {
        new ClaimsGUI(plugin, player, civilization).open();
    }
}