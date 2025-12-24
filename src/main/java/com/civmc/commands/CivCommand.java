package com.civmc.commands;

import com.civmc.CivilizationMC;
import com.civmc.economy.EconomyManager;
import com.civmc.manager.CivilizationManager;
import com.civmc.manager.CivilizationManager.CreateCivilizationResult;
import com.civmc.manager.CivilizationManager.ClaimResult;
import com.civmc.model.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class CivCommand implements CommandExecutor, TabCompleter {
    
    private final CivilizationMC plugin;
    private final CivilizationManager civManager;
    private final EconomyManager economyManager;
    
    public CivCommand(CivilizationMC plugin) {
        this.plugin = plugin;
        this.civManager = plugin.getCivilizationManager();
        this.economyManager = plugin.getEconomyManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                return handleCreate(player, args);
            case "disband":
                return handleDisband(player, args);
            case "rename":
                return handleRename(player, args);
            case "info":
                return handleInfo(player, args);
            case "list":
                return handleList(player, args);
            case "invite":
                return handleInvite(player, args);
            case "join":
                return handleJoin(player, args);
            case "leave":
                return handleLeave(player, args);
            case "kick":
                return handleKick(player, args);
            case "promote":
                return handlePromote(player, args);
            case "demote":
                return handleDemote(player, args);
            case "transfer":
                return handleTransfer(player, args);
            case "bank":
                return handleBank(player, args);
            case "sethome":
                return handleSetHome(player, args);
            case "home":
                return handleHome(player, args);
            case "claim":
                return handleClaim(player, args);
            case "unclaim":
                return handleUnclaim(player, args);
            case "map":
                return handleMap(player, args);
            case "ally":
                return handleAlly(player, args);
            case "war":
                return handleWar(player, args);
            case "peace":
                return handlePeace(player, args);
            case "members":
                return handleMembers(player, args);
            case "claims":
                return handleClaims(player, args);
            case "settings":
                return handleSettings(player, args);
            case "upgrade":
                return handleUpgrade(player, args);
            case "help":
                showHelp(player);
                return true;
            default:
                plugin.getMessageManager().send(player, "unknown-command");
                return true;
        }
    }
    
    private boolean handleCreate(Player player, String[] args) {
        if (!player.hasPermission("civilization.create")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to create civilizations!");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /cv create <name>");
            return true;
        }
        
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String playerUUID = player.getUniqueId().toString();
        
        civManager.createCivilization(playerUUID, name).thenAccept(result -> {
            switch (result) {
                case SUCCESS:
                    player.sendMessage(ChatColor.GREEN + "Successfully created civilization '" + name + "'!");
                    break;
                case ALREADY_IN_CIVILIZATION:
                    player.sendMessage(ChatColor.RED + "You are already in a civilization!");
                    break;
                case NAME_TAKEN:
                    player.sendMessage(ChatColor.RED + "That name is already taken!");
                    break;
                case INVALID_NAME:
                    player.sendMessage(ChatColor.RED + "Invalid name! Use only letters, numbers, underscores, hyphens, and spaces.");
                    break;
                case NAME_TOO_SHORT:
                    player.sendMessage(ChatColor.RED + "Name is too short!");
                    break;
                case NAME_TOO_LONG:
                    player.sendMessage(ChatColor.RED + "Name is too long!");
                    break;
                case INSUFFICIENT_FUNDS:
                    player.sendMessage(ChatColor.RED + "You need " + economyManager.formatMoney(
                            plugin.getConfigManager().getConfig().getDouble("economy.create-cost", 1000.0)) + 
                            " to create a civilization!");
                    break;
                case ERROR:
                    player.sendMessage(ChatColor.RED + "An error occurred while creating the civilization!");
                    break;
            }
        });
        
        return true;
    }
    
    private boolean handleDisband(Player player, String[] args) {
        if (!player.hasPermission("civilization.disband")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to disband civilizations!");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
        
        if (civ == null) {
            player.sendMessage(ChatColor.RED + "You are not in a civilization!");
            return true;
        }
        
        if (!civ.getLeaderUUID().equals(playerUUID)) {
            player.sendMessage(ChatColor.RED + "Only the leader can disband the civilization!");
            return true;
        }
        
        civManager.disbandCivilization(playerUUID, civ.getUuid()).thenAccept(success -> {
            if (success) {
                player.sendMessage(ChatColor.GREEN + "Successfully disbanded civilization '" + civ.getName() + "'!");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to disband civilization!");
            }
        });
        
        return true;
    }
    
    private boolean handleInfo(Player player, String[] args) {
        if (!player.hasPermission("civilization.info")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to view civilization info!");
            return true;
        }
        
        Civilization civ;
        
        if (args.length > 1) {
            // View specific civilization
            String civName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            civ = plugin.getDataManager().getCivilizationByName(civName);
            if (civ == null) {
                player.sendMessage(ChatColor.RED + "Civilization not found!");
                return true;
            }
        } else {
            // View own civilization
            civ = plugin.getDataManager().getPlayerCivilization(player.getUniqueId().toString());
            if (civ == null) {
                player.sendMessage(ChatColor.RED + "You are not in a civilization!");
                return true;
            }
        }
        
        displayCivilizationInfo(player, civ);
        return true;
    }
    
    private void displayCivilizationInfo(Player player, Civilization civ) {
        player.sendMessage(ChatColor.GOLD + "=== " + civ.getName() + " ===");
        player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + civ.getLevel());
        player.sendMessage(ChatColor.YELLOW + "Members: " + ChatColor.WHITE + civ.getTotalMemberCount());
        player.sendMessage(ChatColor.YELLOW + "Claims: " + ChatColor.WHITE + civ.getClaims().size());
        player.sendMessage(ChatColor.YELLOW + "Bank: " + ChatColor.WHITE + economyManager.formatMoney(civ.getBankBalance()));
        player.sendMessage(ChatColor.YELLOW + "Created: " + ChatColor.WHITE + 
                new Date(civ.getCreatedAt()).toString());
        
        // Show leader and officers
        String leaderName = plugin.getServer().getOfflinePlayer(UUID.fromString(civ.getLeaderUUID())).getName();
        player.sendMessage(ChatColor.YELLOW + "Leader: " + ChatColor.WHITE + leaderName);
        
        if (!civ.getOfficers().isEmpty()) {
            List<String> officerNames = civ.getOfficers().stream()
                    .map(uuid -> plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName())
                    .collect(Collectors.toList());
            player.sendMessage(ChatColor.YELLOW + "Officers: " + ChatColor.WHITE + String.join(", ", officerNames));
        }
    }
    
    private boolean handleList(Player player, String[] args) {
        if (!player.hasPermission("civilization.list")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to list civilizations!");
            return true;
        }
        
        List<Civilization> topCivs = civManager.getTopCivilizations(10);
        
        if (topCivs.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No civilizations exist yet!");
            return true;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== Top Civilizations ===");
        for (int i = 0; i < topCivs.size(); i++) {
            Civilization civ = topCivs.get(i);
            player.sendMessage(ChatColor.YELLOW + String.valueOf(i + 1) + ". " + ChatColor.WHITE + civ.getName() + 
                    ChatColor.GRAY + " (" + civ.getTotalMemberCount() + " members, " + 
                    civ.getClaims().size() + " claims)");
        }
        
        return true;
    }
    
    private boolean handleInvite(Player player, String[] args) {
        if (!player.hasPermission("civilization.invite")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to invite players!");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /cv invite <player>");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
        
        if (civ == null) {
            player.sendMessage(ChatColor.RED + "You are not in a civilization!");
            return true;
        }
        
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }
        
        String targetUUID = target.getUniqueId().toString();
        
        if (civManager.invitePlayer(playerUUID, targetUUID, civ.getUuid())) {
            player.sendMessage(ChatColor.GREEN + "Successfully invited " + target.getName() + " to " + civ.getName() + "!");
            target.sendMessage(ChatColor.GREEN + "You have been invited to join " + civ.getName() + "!");
            target.sendMessage(ChatColor.YELLOW + "Use /cv join to accept the invitation.");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to invite player!");
        }
        
        return true;
    }
    
    private boolean handleJoin(Player player, String[] args) {
        if (!player.hasPermission("civilization.join")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to join civilizations!");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        List<Invitation> invites = plugin.getDataManager().getPlayerInvitations(playerUUID);
        
        if (invites.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You have no pending invitations!");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Pending invitations:");
            for (int i = 0; i < invites.size(); i++) {
                Invitation invite = invites.get(i);
                Civilization civ = plugin.getDataManager().getCivilization(invite.getCivUUID());
                if (civ != null) {
                    TextComponent message = new TextComponent(ChatColor.YELLOW + String.valueOf(i + 1) + ". " + 
                            ChatColor.WHITE + civ.getName() + ChatColor.GREEN + " [ACCEPT]");
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                            "/cv join " + invite.getId()));
                    message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                            new ComponentBuilder("Click to accept invitation").create()));
                    player.spigot().sendMessage(message);
                }
            }
            return true;
        }
        
        String inviteId = args[1];
        if (civManager.joinCivilization(playerUUID, inviteId)) {
            player.sendMessage(ChatColor.GREEN + "Successfully joined the civilization!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to join civilization!");
        }
        
        return true;
    }
    
    private boolean handleLeave(Player player, String[] args) {
        if (!player.hasPermission("civilization.leave")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to leave civilizations!");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
        
        if (civ == null) {
            player.sendMessage(ChatColor.RED + "You are not in a civilization!");
            return true;
        }
        
        if (civ.getLeaderUUID().equals(playerUUID)) {
            player.sendMessage(ChatColor.RED + "Leaders cannot leave! Transfer leadership or disband the civilization.");
            return true;
        }
        
        if (civManager.leaveCivilization(playerUUID)) {
            player.sendMessage(ChatColor.GREEN + "Successfully left " + civ.getName() + "!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to leave civilization!");
        }
        
        return true;
    }
    
    private boolean handleBank(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /cv bank <deposit|withdraw|balance> [amount]");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
        
        if (civ == null) {
            player.sendMessage(ChatColor.RED + "You are not in a civilization!");
            return true;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "balance":
                if (!player.hasPermission("civilization.bank.view")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to view bank balance!");
                    return true;
                }
                player.sendMessage(ChatColor.YELLOW + civ.getName() + " Bank Balance: " + 
                        ChatColor.WHITE + economyManager.formatMoney(civ.getBankBalance()));
                return true;
                
            case "deposit":
                if (!player.hasPermission("civilization.bank.deposit")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to deposit!");
                    return true;
                }
                
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /cv bank deposit <amount>");
                    return true;
                }
                
                try {
                    double amount = Double.parseDouble(args[2]);
                    if (amount <= 0) {
                        player.sendMessage(ChatColor.RED + "Amount must be positive!");
                        return true;
                    }
                    
                    economyManager.depositToCivBank(playerUUID, civ.getUuid(), amount).thenAccept(result -> {
                        switch (result) {
                            case SUCCESS:
                                player.sendMessage(ChatColor.GREEN + "Successfully deposited " + 
                                        economyManager.formatMoney(amount) + " to the civilization bank!");
                                break;
                            case INSUFFICIENT_FUNDS:
                                player.sendMessage(ChatColor.RED + "You don't have enough money!");
                                break;
                            default:
                                player.sendMessage(ChatColor.RED + "Failed to deposit money!");
                                break;
                        }
                    });
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid amount!");
                }
                return true;
                
            case "withdraw":
                if (!player.hasPermission("civilization.bank.withdraw")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to withdraw!");
                    return true;
                }
                
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /cv bank withdraw <amount>");
                    return true;
                }
                
                try {
                    double amount = Double.parseDouble(args[2]);
                    if (amount <= 0) {
                        player.sendMessage(ChatColor.RED + "Amount must be positive!");
                        return true;
                    }
                    
                    economyManager.withdrawFromCivBank(playerUUID, civ.getUuid(), amount).thenAccept(result -> {
                        switch (result) {
                            case SUCCESS:
                                player.sendMessage(ChatColor.GREEN + "Successfully withdrew " + 
                                        economyManager.formatMoney(amount) + " from the civilization bank!");
                                break;
                            case NO_PERMISSION:
                                player.sendMessage(ChatColor.RED + "You don't have permission to withdraw from the bank!");
                                break;
                            case INSUFFICIENT_FUNDS:
                                player.sendMessage(ChatColor.RED + "The civilization doesn't have enough money!");
                                break;
                            default:
                                player.sendMessage(ChatColor.RED + "Failed to withdraw money!");
                                break;
                        }
                    });
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid amount!");
                }
                return true;
                
            default:
                player.sendMessage(ChatColor.RED + "Usage: /cv bank <deposit|withdraw|balance> [amount]");
                return true;
        }
    }
    
    private boolean handleClaim(Player player, String[] args) {
        if (!player.hasPermission("civilization.claim")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to claim chunks!");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Location location = player.getLocation();
        
        ClaimResult result = civManager.claimChunk(playerUUID, location);
        
        switch (result) {
            case SUCCESS:
                player.sendMessage(ChatColor.GREEN + "Successfully claimed this chunk!");
                break;
            case NOT_IN_CIVILIZATION:
                player.sendMessage(ChatColor.RED + "You are not in a civilization!");
                break;
            case NO_PERMISSION:
                player.sendMessage(ChatColor.RED + "You don't have permission to claim chunks!");
                break;
            case ALREADY_CLAIMED:
                player.sendMessage(ChatColor.RED + "This chunk is already claimed!");
                break;
            case CLAIM_LIMIT_REACHED:
                player.sendMessage(ChatColor.RED + "Your civilization has reached the claim limit!");
                break;
            case INSUFFICIENT_FUNDS:
                player.sendMessage(ChatColor.RED + "You need " + economyManager.formatMoney(
                        plugin.getConfigManager().getConfig().getDouble("economy.claim-cost", 100.0)) + 
                        " to claim a chunk!");
                break;
            case NOT_ADJACENT:
                player.sendMessage(ChatColor.RED + "You can only claim chunks adjacent to your existing claims!");
                break;
            case ERROR:
                player.sendMessage(ChatColor.RED + "An error occurred while claiming the chunk!");
                break;
        }
        
        return true;
    }
    
    private boolean handleHome(Player player, String[] args) {
        if (!player.hasPermission("civilization.home")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use civilization home!");
            return true;
        }
        
        civManager.teleportToHome(player.getUniqueId().toString());
        return true;
    }
    
    private boolean handleSetHome(Player player, String[] args) {
        if (!player.hasPermission("civilization.sethome")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to set civilization home!");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Location location = player.getLocation();
        
        if (civManager.setCivilizationHome(playerUUID, location)) {
            player.sendMessage(ChatColor.GREEN + "Successfully set civilization home!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to set home! Make sure you're in claimed territory and have permission.");
        }
        
        return true;
    }
    
    private void showHelp(Player player) {
        plugin.getMessageManager().send(player, "help-header");
        plugin.getMessageManager().send(player, "help-create");
        plugin.getMessageManager().send(player, "help-disband");
        plugin.getMessageManager().send(player, "help-info");
        plugin.getMessageManager().send(player, "help-list");
        plugin.getMessageManager().send(player, "help-invite");
        plugin.getMessageManager().send(player, "help-join");
        plugin.getMessageManager().send(player, "help-leave");
        plugin.getMessageManager().send(player, "help-kick");
        plugin.getMessageManager().send(player, "help-promote");
        plugin.getMessageManager().send(player, "help-bank");
        plugin.getMessageManager().send(player, "help-claim");
        plugin.getMessageManager().send(player, "help-unclaim");
        plugin.getMessageManager().send(player, "help-home");
        plugin.getMessageManager().send(player, "help-sethome");
        plugin.getMessageManager().send(player, "help-members");
        plugin.getMessageManager().send(player, "help-claims");
        plugin.getMessageManager().send(player, "help-settings");
        plugin.getMessageManager().send(player, "help-upgrade");
        plugin.getMessageManager().send(player, "help-war");
        plugin.getMessageManager().send(player, "help-ally");
        plugin.getMessageManager().send(player, "help-map");
    }
    
    // TODO: Implement remaining handlers (unclaim, kick, promote, demote, transfer, map, ally, war, peace, rename)
    // For brevity, I'm showing the main structure - these would follow similar patterns
    
    private boolean handleUnclaim(Player player, String[] args) {
        if (!player.hasPermission("civilization.unclaim")) {
            plugin.getMessageManager().send(player, "no-permission");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
        
        if (civ == null) {
            plugin.getMessageManager().send(player, "not-in-civ");
            return true;
        }
        
        // Check permissions
        CivRole role = civ.getPlayerRole(playerUUID);
        if (role == CivRole.RECRUIT) {
            plugin.getMessageManager().send(player, "no-permission");
            return true;
        }
        
        Location location = player.getLocation();
        if (civManager.unclaimChunk(playerUUID, location)) {
            int chunkX = location.getChunk().getX();
            int chunkZ = location.getChunk().getZ();
            plugin.getMessageManager().send(player, "unclaim-ok", 
                    "x", String.valueOf(chunkX), 
                    "z", String.valueOf(chunkZ));
        } else {
            plugin.getMessageManager().send(player, "not-your-claim");
        }
        
        return true;
    }
    
    private boolean handleKick(Player player, String[] args) {
        if (!player.hasPermission("civilization.kick")) {
            plugin.getMessageManager().send(player, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            plugin.getMessageManager().send(player, "invalid-arguments");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
        
        if (civ == null) {
            plugin.getMessageManager().send(player, "not-in-civ");
            return true;
        }
        
        // Check permissions
        CivRole role = civ.getPlayerRole(playerUUID);
        if (role != CivRole.LEADER && role != CivRole.OFFICER) {
            plugin.getMessageManager().send(player, "must-be-officer");
            return true;
        }
        
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Joueur non trouvé!");
            return true;
        }
        
        String targetUUID = target.getUniqueId().toString();
        
        if (civManager.kickMember(playerUUID, targetUUID, civ.getUuid())) {
            plugin.getMessageManager().send(player, "member-kicked", "player", target.getName());
            plugin.getMessageManager().send(target, "left-civ", "player", "Vous avez été expulsé");
        } else {
            plugin.getMessageManager().send(player, "cannot-kick-higher-rank");
        }
        
        return true;
    }
    
    private boolean handlePromote(Player player, String[] args) {
        if (!player.hasPermission("civilization.promote")) {
            plugin.getMessageManager().send(player, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            plugin.getMessageManager().send(player, "invalid-arguments");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
        
        if (civ == null) {
            plugin.getMessageManager().send(player, "not-in-civ");
            return true;
        }
        
        // Check permissions
        CivRole role = civ.getPlayerRole(playerUUID);
        if (role != CivRole.LEADER && role != CivRole.OFFICER) {
            plugin.getMessageManager().send(player, "must-be-officer");
            return true;
        }
        
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Joueur non trouvé!");
            return true;
        }
        
        String targetUUID = target.getUniqueId().toString();
        CivRole targetRole = civ.getPlayerRole(targetUUID);
        
        if (targetRole == null) {
            player.sendMessage(ChatColor.RED + "Ce joueur n'est pas dans votre civilisation!");
            return true;
        }
        
        // Check if can promote
        if (targetRole == CivRole.LEADER) {
            player.sendMessage(ChatColor.RED + "Le leader ne peut pas être promu!");
            return true;
        }
        
        if (targetRole == CivRole.OFFICER && role != CivRole.LEADER) {
            player.sendMessage(ChatColor.RED + "Seul le leader peut promouvoir les officiers!");
            return true;
        }
        
        if (targetRole == CivRole.MEMBER && targetRole == CivRole.OFFICER) {
            player.sendMessage(ChatColor.RED + "Ce joueur est déjà au rang maximum que vous pouvez promouvoir!");
            return true;
        }
        
        civ.promoteMember(targetUUID);
        plugin.getDataManager().saveCivilization(civ);
        
        CivRole newRole = civ.getPlayerRole(targetUUID);
        String roleString = formatRole(newRole);
        
        plugin.getMessageManager().send(player, "member-promoted", 
                "player", target.getName(), 
                "role", roleString);
        plugin.getMessageManager().send(target, "member-promoted", 
                "player", "Vous avez été promu", 
                "role", roleString);
        
        return true;
    }
    
    private boolean handleDemote(Player player, String[] args) {
        if (!player.hasPermission("civilization.demote")) {
            plugin.getMessageManager().send(player, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            plugin.getMessageManager().send(player, "invalid-arguments");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
        
        if (civ == null) {
            plugin.getMessageManager().send(player, "not-in-civ");
            return true;
        }
        
        // Check permissions
        CivRole role = civ.getPlayerRole(playerUUID);
        if (role != CivRole.LEADER && role != CivRole.OFFICER) {
            plugin.getMessageManager().send(player, "must-be-officer");
            return true;
        }
        
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Joueur non trouvé!");
            return true;
        }
        
        String targetUUID = target.getUniqueId().toString();
        CivRole targetRole = civ.getPlayerRole(targetUUID);
        
        if (targetRole == null) {
            player.sendMessage(ChatColor.RED + "Ce joueur n'est pas dans votre civilisation!");
            return true;
        }
        
        // Check if can demote
        if (targetRole == CivRole.LEADER) {
            player.sendMessage(ChatColor.RED + "Le leader ne peut pas être rétrogradé!");
            return true;
        }
        
        if (targetRole == CivRole.RECRUIT) {
            player.sendMessage(ChatColor.RED + "Ce joueur est déjà au rang minimum!");
            return true;
        }
        
        if (targetRole == CivRole.OFFICER && role != CivRole.LEADER) {
            player.sendMessage(ChatColor.RED + "Seul le leader peut rétrograder les officiers!");
            return true;
        }
        
        civ.demoteMember(targetUUID);
        plugin.getDataManager().saveCivilization(civ);
        
        CivRole newRole = civ.getPlayerRole(targetUUID);
        String roleString = formatRole(newRole);
        
        plugin.getMessageManager().send(player, "member-demoted", 
                "player", target.getName(), 
                "role", roleString);
        plugin.getMessageManager().send(target, "member-demoted", 
                "player", "Vous avez été rétrogradé", 
                "role", roleString);
        
        return true;
    }
    
    private boolean handleTransfer(Player player, String[] args) {
        if (!player.hasPermission("civilization.transfer")) {
            plugin.getMessageManager().send(player, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            plugin.getMessageManager().send(player, "invalid-arguments");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
        
        if (civ == null) {
            plugin.getMessageManager().send(player, "not-in-civ");
            return true;
        }
        
        // Only leader can transfer leadership
        if (!civ.getLeaderUUID().equals(playerUUID)) {
            plugin.getMessageManager().send(player, "must-be-leader");
            return true;
        }
        
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Joueur non trouvé!");
            return true;
        }
        
        String targetUUID = target.getUniqueId().toString();
        
        if (!civ.isMember(targetUUID)) {
            player.sendMessage(ChatColor.RED + "Ce joueur n'est pas dans votre civilisation!");
            return true;
        }
        
        if (targetUUID.equals(playerUUID)) {
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous transférer le leadership à vous-même!");
            return true;
        }
        
        // Transfer leadership
        civ.setLeader(targetUUID);
        plugin.getDataManager().saveCivilization(civ);
        
        plugin.getMessageManager().send(player, "leader-transferred", "player", target.getName());
        plugin.getMessageManager().send(target, "leader-transferred", "player", "Vous êtes maintenant le leader");
        
        // Notify all members
        for (String memberUUID : civ.getAllMembers()) {
            Player member = plugin.getServer().getPlayer(UUID.fromString(memberUUID));
            if (member != null && !member.equals(player) && !member.equals(target)) {
                member.sendMessage(ChatColor.YELLOW + target.getName() + " est maintenant le leader de " + civ.getName() + "!");
            }
        }
        
        return true;
    }
    
    private boolean handleRename(Player player, String[] args) {
        if (!player.hasPermission("civilization.rename")) {
            plugin.getMessageManager().send(player, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            plugin.getMessageManager().send(player, "invalid-arguments");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
        
        if (civ == null) {
            plugin.getMessageManager().send(player, "not-in-civ");
            return true;
        }
        
        // Check permissions - only leader or officer can rename
        CivRole role = civ.getPlayerRole(playerUUID);
        if (role != CivRole.LEADER && role != CivRole.OFFICER) {
            plugin.getMessageManager().send(player, "must-be-officer");
            return true;
        }
        
        String newName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        
        if (civManager.renameCivilization(playerUUID, civ.getUuid(), newName)) {
            plugin.getMessageManager().send(player, "civ-renamed", "name", newName);
            
            // Notify all members
            for (String memberUUID : civ.getAllMembers()) {
                Player member = plugin.getServer().getPlayer(UUID.fromString(memberUUID));
                if (member != null && !member.equals(player)) {
                    plugin.getMessageManager().send(member, "civ-renamed", "name", newName);
                }
            }
        } else {
            plugin.getMessageManager().send(player, "civ-name-taken", "name", newName);
        }
        
        return true;
    }
    
    private boolean handleMap(Player player, String[] args) {
        if (!player.hasPermission("civilization.map")) {
            plugin.getMessageManager().send(player, "no-permission");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Civilization playerCiv = plugin.getDataManager().getPlayerCivilization(playerUUID);
        
        Location playerLoc = player.getLocation();
        String world = playerLoc.getWorld().getName();
        int centerX = playerLoc.getChunk().getX();
        int centerZ = playerLoc.getChunk().getZ();
        
        int radius = 5; // Show 11x11 grid (5 chunks in each direction)
        
        plugin.getMessageManager().send(player, "map-legend");
        plugin.getMessageManager().send(player, "map-own");
        plugin.getMessageManager().send(player, "map-ally");
        plugin.getMessageManager().send(player, "map-enemy");
        plugin.getMessageManager().send(player, "map-neutral");
        plugin.getMessageManager().send(player, "map-unclaimed");
        
        player.sendMessage(ChatColor.GOLD + "=== Carte des revendications ===");
        player.sendMessage(ChatColor.GRAY + "Centre: (" + centerX + ", " + centerZ + ") dans " + world);
        player.sendMessage("");
        
        // Create the map
        StringBuilder mapBuilder = new StringBuilder();
        
        for (int z = centerZ - radius; z <= centerZ + radius; z++) {
            StringBuilder row = new StringBuilder();
            
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                // Check if this is the player's current chunk
                if (x == centerX && z == centerZ) {
                    row.append(ChatColor.WHITE).append("■ ");
                    continue;
                }
                
                Claim claim = plugin.getDataManager().getClaim(world, x, z);
                
                if (claim == null) {
                    // Unclaimed
                    row.append(ChatColor.DARK_GRAY).append("■ ");
                } else {
                    Civilization claimOwner = plugin.getDataManager().getCivilization(claim.getCivId());
                    if (claimOwner == null) {
                        row.append(ChatColor.DARK_GRAY).append("■ ");
                        continue;
                    }
                    
                    if (playerCiv != null && claimOwner.getUuid().equals(playerCiv.getUuid())) {
                        // Own civilization
                        row.append(ChatColor.GREEN).append("■ ");
                    } else if (playerCiv != null && playerCiv.getAllies().contains(claimOwner.getUuid())) {
                        // Allied civilization
                        row.append(ChatColor.BLUE).append("■ ");
                    } else if (playerCiv != null && isAtWar(playerCiv, claimOwner)) {
                        // Enemy civilization
                        row.append(ChatColor.RED).append("■ ");
                    } else {
                        // Neutral civilization
                        row.append(ChatColor.YELLOW).append("■ ");
                    }
                }
            }
            
            player.sendMessage(row.toString());
        }
        
        player.sendMessage("");
        player.sendMessage(ChatColor.WHITE + "■ " + ChatColor.GRAY + "= Votre position");
        
        return true;
    }
    
    private boolean isAtWar(Civilization civ1, Civilization civ2) {
        for (String warId : civ1.getWars()) {
            War war = plugin.getDataManager().getWar(warId);
            if (war != null && war.isInvolvedCiv(civ2.getUuid()) && war.getState() == WarState.ACTIVE) {
                return true;
            }
        }
        return false;
    }
    
    private boolean handleAlly(Player player, String[] args) {
        if (!player.hasPermission("civilization.ally")) {
            plugin.getMessageManager().send(player, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            plugin.getMessageManager().send(player, "invalid-arguments");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
        
        if (civ == null) {
            plugin.getMessageManager().send(player, "not-in-civ");
            return true;
        }
        
        // Check permissions
        CivRole role = civ.getPlayerRole(playerUUID);
        if (role != CivRole.LEADER && role != CivRole.OFFICER) {
            plugin.getMessageManager().send(player, "must-be-officer");
            return true;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "add":
            case "propose":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /cv ally add <civilisation>");
                    return true;
                }
                
                String targetCivName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                Civilization targetCiv = plugin.getDataManager().getCivilizationByName(targetCivName);
                
                if (targetCiv == null) {
                    plugin.getMessageManager().send(player, "civ-not-found", "name", targetCivName);
                    return true;
                }
                
                if (targetCiv.getUuid().equals(civ.getUuid())) {
                    player.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous allier avec vous-même!");
                    return true;
                }
                
                if (civ.getAllies().contains(targetCiv.getUuid())) {
                    plugin.getMessageManager().send(player, "already-allies", "civ", targetCiv.getName());
                    return true;
                }
                
                // Simple alliance system - add each other as allies
                civ.getAllies().add(targetCiv.getUuid());
                targetCiv.getAllies().add(civ.getUuid());
                
                plugin.getDataManager().saveCivilization(civ);
                plugin.getDataManager().saveCivilization(targetCiv);
                
                plugin.getMessageManager().send(player, "ally-request-accepted", "civ", targetCiv.getName());
                
                // Notify target civilization
                for (String memberUUID : targetCiv.getAllMembers()) {
                    Player member = plugin.getServer().getPlayer(UUID.fromString(memberUUID));
                    if (member != null) {
                        plugin.getMessageManager().send(member, "ally-request-accepted", "civ", civ.getName());
                    }
                }
                break;
                
            case "remove":
            case "break":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /cv ally remove <civilisation>");
                    return true;
                }
                
                String allyName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                Civilization ally = plugin.getDataManager().getCivilizationByName(allyName);
                
                if (ally == null) {
                    plugin.getMessageManager().send(player, "civ-not-found", "name", allyName);
                    return true;
                }
                
                if (!civ.getAllies().contains(ally.getUuid())) {
                    player.sendMessage(ChatColor.RED + "Vous n'êtes pas alliés avec " + ally.getName() + "!");
                    return true;
                }
                
                civ.getAllies().remove(ally.getUuid());
                ally.getAllies().remove(civ.getUuid());
                
                plugin.getDataManager().saveCivilization(civ);
                plugin.getDataManager().saveCivilization(ally);
                
                plugin.getMessageManager().send(player, "ally-removed", "civ", ally.getName());
                
                // Notify former ally
                for (String memberUUID : ally.getAllMembers()) {
                    Player member = plugin.getServer().getPlayer(UUID.fromString(memberUUID));
                    if (member != null) {
                        plugin.getMessageManager().send(member, "ally-removed", "civ", civ.getName());
                    }
                }
                break;
                
            case "list":
                if (civ.getAllies().isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "Votre civilisation n'a aucun allié.");
                } else {
                    player.sendMessage(ChatColor.GOLD + "=== Alliés de " + civ.getName() + " ===");
                    for (String allyUUID : civ.getAllies()) {
                        Civilization allyInfo = plugin.getDataManager().getCivilization(allyUUID);
                        if (allyInfo != null) {
                            player.sendMessage(ChatColor.GREEN + "- " + allyInfo.getName() + 
                                    ChatColor.GRAY + " (" + allyInfo.getTotalMemberCount() + " membres)");
                        }
                    }
                }
                break;
                
            default:
                player.sendMessage(ChatColor.RED + "Usage: /cv ally <add|remove|list> [civilisation]");
                break;
        }
        
        return true;
    }
    
    private boolean handleWar(Player player, String[] args) {
        if (!player.hasPermission("civilization.war")) {
            plugin.getMessageManager().send(player, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            plugin.getMessageManager().send(player, "invalid-arguments");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
        
        if (civ == null) {
            plugin.getMessageManager().send(player, "not-in-civ");
            return true;
        }
        
        // Check permissions
        CivRole role = civ.getPlayerRole(playerUUID);
        if (role != CivRole.LEADER && role != CivRole.OFFICER) {
            plugin.getMessageManager().send(player, "must-be-officer");
            return true;
        }
        
        String targetCivName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Civilization targetCiv = plugin.getDataManager().getCivilizationByName(targetCivName);
        
        if (targetCiv == null) {
            plugin.getMessageManager().send(player, "civ-not-found", "name", targetCivName);
            return true;
        }
        
        if (targetCiv.getUuid().equals(civ.getUuid())) {
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas déclarer la guerre à votre propre civilisation!");
            return true;
        }
        
        // Check if already allies
        if (civ.getAllies().contains(targetCiv.getUuid())) {
            plugin.getMessageManager().send(player, "cannot-war-ally");
            return true;
        }
        
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Aucune raison donnée";
        
        if (civManager.declareWar(playerUUID, targetCiv.getUuid(), reason)) {
            plugin.getMessageManager().send(player, "war-declared", 
                    "civA", civ.getName(), 
                    "civB", targetCiv.getName());
            
            // Notify target civilization members
            for (String memberUUID : targetCiv.getAllMembers()) {
                Player member = plugin.getServer().getPlayer(UUID.fromString(memberUUID));
                if (member != null) {
                    plugin.getMessageManager().send(member, "war-declared", 
                            "civA", civ.getName(), 
                            "civB", targetCiv.getName());
                }
            }
            
            // Broadcast to server (optional)
            plugin.getServer().broadcastMessage(ChatColor.RED + "[GUERRE] " + civ.getName() + " a déclaré la guerre à " + targetCiv.getName() + "!");
        } else {
            plugin.getMessageManager().send(player, "already-at-war");
        }
        
        return true;
    }
    
    private boolean handlePeace(Player player, String[] args) {
        if (!player.hasPermission("civilization.peace")) {
            plugin.getMessageManager().send(player, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            plugin.getMessageManager().send(player, "invalid-arguments");
            return true;
        }
        
        String playerUUID = player.getUniqueId().toString();
        Civilization civ = plugin.getDataManager().getPlayerCivilization(playerUUID);
        
        if (civ == null) {
            plugin.getMessageManager().send(player, "not-in-civ");
            return true;
        }
        
        // Check permissions
        CivRole role = civ.getPlayerRole(playerUUID);
        if (role != CivRole.LEADER && role != CivRole.OFFICER) {
            plugin.getMessageManager().send(player, "must-be-officer");
            return true;
        }
        
        String targetCivName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Civilization targetCiv = plugin.getDataManager().getCivilizationByName(targetCivName);
        
        if (targetCiv == null) {
            plugin.getMessageManager().send(player, "civ-not-found", "name", targetCivName);
            return true;
        }
        
        if (targetCiv.getUuid().equals(civ.getUuid())) {
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas faire la paix avec vous-même!");
            return true;
        }
        
        // Find active war between these civilizations
        String activeWarId = null;
        for (String warId : civ.getWars()) {
            War war = plugin.getDataManager().getWar(warId);
            if (war != null && war.isInvolvedCiv(targetCiv.getUuid()) && war.getState() == WarState.ACTIVE) {
                activeWarId = warId;
                break;
            }
        }
        
        if (activeWarId == null) {
            player.sendMessage(ChatColor.RED + "Vous n'êtes pas en guerre avec " + targetCiv.getName() + "!");
            return true;
        }
        
        // End the war
        if (civManager.endWar(activeWarId, "Paix négociée")) {
            plugin.getMessageManager().send(player, "peace-accepted", "civ", targetCiv.getName());
            
            // Notify target civilization
            for (String memberUUID : targetCiv.getAllMembers()) {
                Player member = plugin.getServer().getPlayer(UUID.fromString(memberUUID));
                if (member != null) {
                    plugin.getMessageManager().send(member, "peace-accepted", "civ", civ.getName());
                }
            }
            
            // Broadcast to server
            plugin.getServer().broadcastMessage(ChatColor.GREEN + "[PAIX] " + civ.getName() + " et " + targetCiv.getName() + " ont signé la paix!");
        } else {
            plugin.getMessageManager().send(player, "error-occurred");
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return null;
        
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList(
                    "create", "disband", "rename", "info", "list", "invite", "join", "leave",
                    "kick", "promote", "demote", "transfer", "bank", "sethome", "home",
                    "claim", "unclaim", "map", "ally", "war", "peace", "help"
            );
            return subcommands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "bank":
                    return Arrays.asList("deposit", "withdraw", "balance").stream()
                            .filter(cmd -> cmd.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "invite":
                case "kick":
                case "promote":
                case "demote":
                case "transfer":
                    return plugin.getServer().getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "info":
                    return plugin.getDataManager().getAllCivilizations().values().stream()
                            .map(Civilization::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
            }
        }
        
        return null;
    }
}