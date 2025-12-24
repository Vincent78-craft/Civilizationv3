package com.civmc.gui;

import com.civmc.CivilizationMC;
import com.civmc.model.Civilization;
import com.civmc.model.CivRole;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public class CivilizationInfoGUI extends CivGUI {
    
    private final Civilization civilization;
    
    public CivilizationInfoGUI(CivilizationMC plugin, Player player, Civilization civilization) {
        super(plugin, player, "&6" + civilization.getName() + " - Information", 54);
        this.civilization = civilization;
    }
    
    @Override
    protected void setupGUI() {
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);
        
        // Civilization banner/icon
        setItem(13, Material.WHITE_BANNER, "&6" + civilization.getName(), 
                "&7Level: &f" + civilization.getLevel(),
                "&7Created: &f" + new Date(civilization.getCreatedAt()).toString(),
                "&7UUID: &f" + civilization.getUuid());
        setUnclickable(13);
        
        // Members info
        String leaderName = plugin.getServer().getOfflinePlayer(UUID.fromString(civilization.getLeaderUUID())).getName();
        setItem(20, Material.PLAYER_HEAD, "&eMembers (" + civilization.getTotalMemberCount() + ")",
                "&7Leader: &f" + leaderName,
                "&7Officers: &f" + civilization.getOfficers().size(),
                "&7Members: &f" + civilization.getMembers().size(),
                "&7Recruits: &f" + civilization.getRecruits().size(),
                "",
                "&7Click to view all members");
        setClickAction(20, p -> MembersGUI.open(plugin, player, civilization));
        
        // Claims info
        setItem(21, Material.MAP, "&eClaims (" + civilization.getClaims().size() + ")",
                "&7Total chunks claimed: &f" + civilization.getClaims().size(),
                "",
                "&7Click to view all claims");
        setClickAction(21, p -> ClaimsGUI.open(plugin, player, civilization));
        
        // Bank info
        setItem(22, Material.GOLD_INGOT, "&eBank",
                "&7Balance: &f" + plugin.getEconomyManager().formatMoney(civilization.getBankBalance()),
                "&7Currency: &f" + plugin.getEconomyManager().getCurrencyName(),
                "",
                "&7Recent transactions: &f" + civilization.getTransactions().size());
        setUnclickable(22);
        
        // Wars info
        setItem(23, Material.IRON_SWORD, "&eWars (" + civilization.getWars().size() + ")",
                "&7Active wars: &f" + civilization.getWars().size(),
                "",
                "&7Click to view war details");
        setClickAction(23, p -> {
            // TODO: Implement wars GUI
            p.sendMessage("&cWars GUI not yet implemented!");
        });
        
        // Allies info
        setItem(24, Material.EMERALD, "&eAllies (" + civilization.getAllies().size() + ")",
                "&7Allied civilizations: &f" + civilization.getAllies().size(),
                "",
                "&7Click to view allies");
        setClickAction(24, p -> {
            // TODO: Implement allies GUI
            p.sendMessage("&cAllies GUI not yet implemented!");
        });
        
        // Settings (only for members with permission)
        String playerUUID = player.getUniqueId().toString();
        CivRole playerRole = civilization.getPlayerRole(playerUUID);
        
        if (playerRole == CivRole.LEADER || playerRole == CivRole.OFFICER) {
            setItem(31, Material.REDSTONE, "&eSettings",
                    "&7Click to manage civilization settings",
                    "&7(Leaders and Officers only)");
            setClickAction(31, p -> {
                // TODO: Implement settings GUI
                p.sendMessage("&cSettings GUI not yet implemented!");
            });
        } else {
            setItem(31, Material.BARRIER, "&cSettings",
                    "&7You don't have permission to view settings",
                    "&7Only leaders and officers can access this");
            setUnclickable(31);
        }
        
        // Statistics
        setItem(40, Material.BOOK, "&eStatistics",
                "&7Level: &f" + civilization.getLevel(),
                "&7Total Members: &f" + civilization.getTotalMemberCount(),
                "&7Total Claims: &f" + civilization.getClaims().size(),
                "&7Bank Balance: &f" + plugin.getEconomyManager().formatMoney(civilization.getBankBalance()),
                "&7Active Wars: &f" + civilization.getWars().size(),
                "&7Allies: &f" + civilization.getAllies().size(),
                "&7Age: &f" + getAgeString());
        setUnclickable(40);
        
        // Close button
        addCloseButton(49);
        
        // Player's role in this civilization (if member)
        if (playerRole != null) {
            setItem(10, getRoleMaterial(playerRole), "&eYour Role",
                    "&7You are a " + formatRole(playerRole) + " &7in this civilization");
            setUnclickable(10);
        }
    }
    
    private String getAgeString() {
        long ageMillis = System.currentTimeMillis() - civilization.getCreatedAt();
        long ageDays = ageMillis / (24 * 60 * 60 * 1000);
        
        if (ageDays == 0) {
            return "Less than a day";
        } else if (ageDays == 1) {
            return "1 day";
        } else if (ageDays < 7) {
            return ageDays + " days";
        } else if (ageDays < 30) {
            return (ageDays / 7) + " weeks";
        } else if (ageDays < 365) {
            return (ageDays / 30) + " months";
        } else {
            return (ageDays / 365) + " years";
        }
    }
}