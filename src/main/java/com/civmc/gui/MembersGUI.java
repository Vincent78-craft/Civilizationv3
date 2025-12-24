package com.civmc.gui;

import com.civmc.CivilizationMC;
import com.civmc.model.Civilization;
import com.civmc.model.CivRole;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class MembersGUI extends CivGUI {
    
    private final Civilization civilization;
    private int page = 0;
    private final int membersPerPage = 28; // 4 rows of 7 items
    
    public MembersGUI(CivilizationMC plugin, Player player, Civilization civilization) {
        super(plugin, player, "&6" + civilization.getName() + " - Members", 54);
        this.civilization = civilization;
    }
    
    @Override
    protected void setupGUI() {
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);
        
        // Get all members sorted by role
        List<MemberInfo> members = getAllMembers();
        
        // Calculate pagination
        int totalPages = (int) Math.ceil((double) members.size() / membersPerPage);
        int startIndex = page * membersPerPage;
        int endIndex = Math.min(startIndex + membersPerPage, members.size());
        
        // Display members
        int slot = 10; // Starting slot (skip border)
        for (int i = startIndex; i < endIndex; i++) {
            MemberInfo member = members.get(i);
            
            // Skip border slots
            if (slot % 9 == 0 || slot % 9 == 8) {
                slot++;
            }
            
            // Display member
            setItem(slot, Material.PLAYER_HEAD, formatRole(member.role) + " &f" + member.name,
                    "&7Role: " + formatRole(member.role),
                    "&7Status: " + (plugin.getServer().getPlayer(UUID.fromString(member.uuid)) != null ? 
                            "&aOnline" : "&7Offline"),
                    "",
                    "&7Click to manage (if you have permission)");
            
            final String memberUUID = member.uuid;
            final CivRole memberRole = member.role;
            
            setClickAction(slot, p -> handleMemberClick(memberUUID, memberRole));
            
            slot++;
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
                "&7Total members: &f" + members.size(),
                "&7Showing: &f" + (startIndex + 1) + "-" + endIndex);
        setUnclickable(49);
        
        // Back to civilization info
        addBackButton(46, () -> new CivilizationInfoGUI(plugin, player, civilization).open());
        
        // Close button
        addCloseButton(52);
    }
    
    private List<MemberInfo> getAllMembers() {
        List<MemberInfo> members = new ArrayList<>();
        
        // Add leader
        String leaderUUID = civilization.getLeaderUUID();
        String leaderName = plugin.getServer().getOfflinePlayer(UUID.fromString(leaderUUID)).getName();
        members.add(new MemberInfo(leaderUUID, leaderName, CivRole.LEADER));
        
        // Add officers
        for (String officerUUID : civilization.getOfficers()) {
            String officerName = plugin.getServer().getOfflinePlayer(UUID.fromString(officerUUID)).getName();
            members.add(new MemberInfo(officerUUID, officerName, CivRole.OFFICER));
        }
        
        // Add members
        for (String memberUUID : civilization.getMembers()) {
            String memberName = plugin.getServer().getOfflinePlayer(UUID.fromString(memberUUID)).getName();
            members.add(new MemberInfo(memberUUID, memberName, CivRole.MEMBER));
        }
        
        // Add recruits
        for (String recruitUUID : civilization.getRecruits()) {
            String recruitName = plugin.getServer().getOfflinePlayer(UUID.fromString(recruitUUID)).getName();
            members.add(new MemberInfo(recruitUUID, recruitName, CivRole.RECRUIT));
        }
        
        return members;
    }
    
    private void handleMemberClick(String memberUUID, CivRole memberRole) {
        String playerUUID = player.getUniqueId().toString();
        CivRole playerRole = civilization.getPlayerRole(playerUUID);
        
        if (playerRole == null) {
            player.sendMessage("&cYou are not a member of this civilization!");
            return;
        }
        
        // Only leaders and officers can manage members
        if (playerRole != CivRole.LEADER && playerRole != CivRole.OFFICER) {
            player.sendMessage("&cYou don't have permission to manage members!");
            return;
        }
        
        // Leaders can manage everyone except themselves
        // Officers can only manage members and recruits
        if (playerRole == CivRole.OFFICER && (memberRole == CivRole.LEADER || memberRole == CivRole.OFFICER)) {
            player.sendMessage("&cYou can only manage members and recruits!");
            return;
        }
        
        // Can't manage yourself
        if (memberUUID.equals(playerUUID)) {
            player.sendMessage("&cYou can't manage yourself!");
            return;
        }
        
        // Open member management GUI
        new MemberManagementGUI(plugin, player, civilization, memberUUID, memberRole).open();
    }
    
    public static void open(CivilizationMC plugin, Player player, Civilization civilization) {
        new MembersGUI(plugin, player, civilization).open();
    }
    
    private static class MemberInfo {
        final String uuid;
        final String name;
        final CivRole role;
        
        MemberInfo(String uuid, String name, CivRole role) {
            this.uuid = uuid;
            this.name = name;
            this.role = role;
        }
    }
}