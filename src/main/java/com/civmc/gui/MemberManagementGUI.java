package com.civmc.gui;

import com.civmc.CivilizationMC;
import com.civmc.model.Civilization;
import com.civmc.model.CivRole;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MemberManagementGUI extends CivGUI {
    
    private final Civilization civilization;
    private final String targetUUID;
    private final CivRole targetRole;
    
    public MemberManagementGUI(CivilizationMC plugin, Player player, Civilization civilization, 
                              String targetUUID, CivRole targetRole) {
        super(plugin, player, "&6Manage Member", 27);
        this.civilization = civilization;
        this.targetUUID = targetUUID;
        this.targetRole = targetRole;
    }
    
    @Override
    protected void setupGUI() {
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);
        
        String targetName = plugin.getServer().getOfflinePlayer(UUID.fromString(targetUUID)).getName();
        
        // Member info
        setItem(4, Material.PLAYER_HEAD, "&e" + targetName,
                "&7Current Role: " + formatRole(targetRole),
                "&7Status: " + (plugin.getServer().getPlayer(UUID.fromString(targetUUID)) != null ? 
                        "&aOnline" : "&7Offline"));
        setUnclickable(4);
        
        String playerUUID = player.getUniqueId().toString();
        CivRole playerRole = civilization.getPlayerRole(playerUUID);
        
        // Promote button
        if (canPromote(playerRole, targetRole)) {
            setItem(11, Material.GREEN_WOOL, "&aPromote",
                    "&7Promote " + targetName,
                    "&7Current: " + formatRole(targetRole),
                    "&7New: " + formatRole(getNextRole(targetRole)),
                    "",
                    "&7Click to promote");
            setClickAction(11, p -> handlePromote());
        } else {
            setItem(11, Material.GRAY_WOOL, "&cPromote",
                    "&7You can't promote this member",
                    "&7Either they're already at max rank",
                    "&7or you don't have permission");
            setUnclickable(11);
        }
        
        // Demote button
        if (canDemote(playerRole, targetRole)) {
            setItem(13, Material.ORANGE_WOOL, "&6Demote",
                    "&7Demote " + targetName,
                    "&7Current: " + formatRole(targetRole),
                    "&7New: " + formatRole(getPreviousRole(targetRole)),
                    "",
                    "&7Click to demote");
            setClickAction(13, p -> handleDemote());
        } else {
            setItem(13, Material.GRAY_WOOL, "&cDemote",
                    "&7You can't demote this member",
                    "&7Either they're already at minimum rank",
                    "&7or you don't have permission");
            setUnclickable(13);
        }
        
        // Kick button
        if (canKick(playerRole, targetRole)) {
            setItem(15, Material.RED_WOOL, "&cKick",
                    "&7Kick " + targetName + " from the civilization",
                    "",
                    "&4Warning: This action cannot be undone!",
                    "",
                    "&7Click to kick");
            setClickAction(15, p -> handleKick());
        } else {
            setItem(15, Material.GRAY_WOOL, "&cKick",
                    "&7You can't kick this member",
                    "&7You don't have permission");
            setUnclickable(15);
        }
        
        // Transfer leadership (only for leaders)
        if (playerRole == CivRole.LEADER && targetRole != CivRole.LEADER) {
            setItem(22, Material.DIAMOND_HELMET, "&eTransfer Leadership",
                    "&7Transfer leadership to " + targetName,
                    "&7You will become an officer",
                    "",
                    "&4Warning: This action cannot be undone!",
                    "",
                    "&7Click to transfer leadership");
            setClickAction(22, p -> handleTransferLeadership());
        }
        
        // Back button
        addBackButton(18, () -> MembersGUI.open(plugin, player, civilization));
        
        // Close button
        addCloseButton(26);
    }
    
    private boolean canPromote(CivRole playerRole, CivRole targetRole) {
        if (playerRole != CivRole.LEADER && playerRole != CivRole.OFFICER) return false;
        if (targetRole == CivRole.LEADER) return false;
        if (targetRole == CivRole.OFFICER && playerRole != CivRole.LEADER) return false;
        return true;
    }
    
    private boolean canDemote(CivRole playerRole, CivRole targetRole) {
        if (playerRole != CivRole.LEADER && playerRole != CivRole.OFFICER) return false;
        if (targetRole == CivRole.LEADER) return false;
        if (targetRole == CivRole.RECRUIT) return false;
        if (targetRole == CivRole.OFFICER && playerRole != CivRole.LEADER) return false;
        return true;
    }
    
    private boolean canKick(CivRole playerRole, CivRole targetRole) {
        if (playerRole != CivRole.LEADER && playerRole != CivRole.OFFICER) return false;
        if (targetRole == CivRole.LEADER) return false;
        if (targetRole == CivRole.OFFICER && playerRole != CivRole.LEADER) return false;
        return true;
    }
    
    private CivRole getNextRole(CivRole current) {
        switch (current) {
            case RECRUIT: return CivRole.MEMBER;
            case MEMBER: return CivRole.OFFICER;
            case OFFICER: return CivRole.LEADER;
            default: return current;
        }
    }
    
    private CivRole getPreviousRole(CivRole current) {
        switch (current) {
            case OFFICER: return CivRole.MEMBER;
            case MEMBER: return CivRole.RECRUIT;
            default: return current;
        }
    }
    
    private void handlePromote() {
        String targetName = plugin.getServer().getOfflinePlayer(UUID.fromString(targetUUID)).getName();
        
        civilization.promoteMember(targetUUID);
        plugin.getDataManager().saveCivilization(civilization);
        
        player.sendMessage("&aSuccessfully promoted " + targetName + "!");
        
        // Notify target if online
        Player target = plugin.getServer().getPlayer(UUID.fromString(targetUUID));
        if (target != null) {
            target.sendMessage("&aYou have been promoted in " + civilization.getName() + "!");
        }
        
        // Refresh GUI
        close();
        new MemberManagementGUI(plugin, player, civilization, targetUUID, getNextRole(targetRole)).open();
    }
    
    private void handleDemote() {
        String targetName = plugin.getServer().getOfflinePlayer(UUID.fromString(targetUUID)).getName();
        
        civilization.demoteMember(targetUUID);
        plugin.getDataManager().saveCivilization(civilization);
        
        player.sendMessage("&6Successfully demoted " + targetName + "!");
        
        // Notify target if online
        Player target = plugin.getServer().getPlayer(UUID.fromString(targetUUID));
        if (target != null) {
            target.sendMessage("&6You have been demoted in " + civilization.getName() + "!");
        }
        
        // Refresh GUI
        close();
        new MemberManagementGUI(plugin, player, civilization, targetUUID, getPreviousRole(targetRole)).open();
    }
    
    private void handleKick() {
        String targetName = plugin.getServer().getOfflinePlayer(UUID.fromString(targetUUID)).getName();
        
        if (plugin.getCivilizationManager().kickMember(player.getUniqueId().toString(), targetUUID, civilization.getUuid())) {
            player.sendMessage("&aSuccessfully kicked " + targetName + " from the civilization!");
            
            // Notify target if online
            Player target = plugin.getServer().getPlayer(UUID.fromString(targetUUID));
            if (target != null) {
                target.sendMessage("&cYou have been kicked from " + civilization.getName() + "!");
            }
            
            // Go back to members list
            close();
            MembersGUI.open(plugin, player, civilization);
        } else {
            player.sendMessage("&cFailed to kick " + targetName + "!");
        }
    }
    
    private void handleTransferLeadership() {
        String targetName = plugin.getServer().getOfflinePlayer(UUID.fromString(targetUUID)).getName();
        
        // Confirm action (in a real implementation, you might want a confirmation GUI)
        civilization.setLeader(targetUUID);
        plugin.getDataManager().saveCivilization(civilization);
        
        player.sendMessage("&eYou have transferred leadership of " + civilization.getName() + " to " + targetName + "!");
        
        // Notify new leader if online
        Player target = plugin.getServer().getPlayer(UUID.fromString(targetUUID));
        if (target != null) {
            target.sendMessage("&eYou are now the leader of " + civilization.getName() + "!");
        }
        
        // Close GUI and go back to members list
        close();
        MembersGUI.open(plugin, player, civilization);
    }
}