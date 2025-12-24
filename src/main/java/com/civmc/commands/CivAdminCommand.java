package com.civmc.commands;

import com.civmc.CivilizationMC;
import com.civmc.model.Civilization;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CivAdminCommand implements CommandExecutor, TabCompleter {
    
    private final CivilizationMC plugin;
    
    public CivAdminCommand(CivilizationMC plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("civilization.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use admin commands!");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                return handleReload(sender);
            case "save":
                return handleSave(sender);
            case "delete":
                return handleDelete(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "list":
                return handleList(sender);
            case "setlevel":
                return handleSetLevel(sender, args);
            case "setmoney":
                return handleSetMoney(sender, args);
            case "addmoney":
                return handleAddMoney(sender, args);
            case "forcejoin":
                return handleForceJoin(sender, args);
            case "backup":
                return handleBackup(sender);
            case "debug":
                return handleDebug(sender);
            case "help":
                showHelp(sender);
                return true;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown admin command. Use /cvadmin help for help.");
                return true;
        }
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("civilization.admin.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to reload!");
            return true;
        }
        
        try {
            plugin.getConfigManager().reload();
            plugin.getMessageManager().reload();
            plugin.getCivilizationManager().reloadConfiguration();
            sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to reload configuration: " + e.getMessage());
            plugin.getLogger().severe("Failed to reload configuration: " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean handleSave(CommandSender sender) {
        if (!sender.hasPermission("civilization.admin.save")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to force save!");
            return true;
        }
        
        sender.sendMessage(ChatColor.YELLOW + "Saving all data...");
        plugin.getDataManager().saveAll();
        sender.sendMessage(ChatColor.GREEN + "All data saved successfully!");
        
        return true;
    }
    
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("civilization.admin.delete")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to delete civilizations!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /cvadmin delete <civilization_name>");
            return true;
        }
        
        String civName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Civilization civ = plugin.getDataManager().getCivilizationByName(civName);
        
        if (civ == null) {
            sender.sendMessage(ChatColor.RED + "Civilization not found!");
            return true;
        }
        
        plugin.getDataManager().deleteCivilization(civ.getUuid());
        sender.sendMessage(ChatColor.GREEN + "Successfully deleted civilization '" + civ.getName() + "'!");
        plugin.getLogger().info("Admin " + sender.getName() + " deleted civilization " + civ.getName());
        
        return true;
    }
    
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /cvadmin info <civilization_name>");
            return true;
        }
        
        String civName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Civilization civ = plugin.getDataManager().getCivilizationByName(civName);
        
        if (civ == null) {
            sender.sendMessage(ChatColor.RED + "Civilization not found!");
            return true;
        }
        
        displayDetailedCivInfo(sender, civ);
        return true;
    }
    
    private void displayDetailedCivInfo(CommandSender sender, Civilization civ) {
        sender.sendMessage(ChatColor.GOLD + "=== " + civ.getName() + " (Admin Info) ===");
        sender.sendMessage(ChatColor.YELLOW + "UUID: " + ChatColor.WHITE + civ.getUuid());
        sender.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + civ.getLevel());
        sender.sendMessage(ChatColor.YELLOW + "Bank Balance: " + ChatColor.WHITE + 
                plugin.getEconomyManager().formatMoney(civ.getBankBalance()));
        sender.sendMessage(ChatColor.YELLOW + "Total Members: " + ChatColor.WHITE + civ.getTotalMemberCount());
        sender.sendMessage(ChatColor.YELLOW + "Claims: " + ChatColor.WHITE + civ.getClaims().size());
        sender.sendMessage(ChatColor.YELLOW + "Wars: " + ChatColor.WHITE + civ.getWars().size());
        sender.sendMessage(ChatColor.YELLOW + "Allies: " + ChatColor.WHITE + civ.getAllies().size());
        sender.sendMessage(ChatColor.YELLOW + "Created: " + ChatColor.WHITE + new java.util.Date(civ.getCreatedAt()));
        
        // Leader info
        String leaderName = plugin.getServer().getOfflinePlayer(java.util.UUID.fromString(civ.getLeaderUUID())).getName();
        sender.sendMessage(ChatColor.YELLOW + "Leader: " + ChatColor.WHITE + leaderName + 
                ChatColor.GRAY + " (" + civ.getLeaderUUID() + ")");
        
        // Officers
        if (!civ.getOfficers().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Officers (" + civ.getOfficers().size() + "):");
            for (String officerUUID : civ.getOfficers()) {
                String officerName = plugin.getServer().getOfflinePlayer(java.util.UUID.fromString(officerUUID)).getName();
                sender.sendMessage(ChatColor.WHITE + "  - " + officerName + ChatColor.GRAY + " (" + officerUUID + ")");
            }
        }
        
        // Recent transactions
        if (!civ.getTransactions().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Recent Transactions:");
            civ.getTransactions().stream()
                    .limit(5)
                    .forEach(transaction -> {
                        sender.sendMessage(ChatColor.WHITE + "  - " + transaction.getType() + ": " +
                                plugin.getEconomyManager().formatMoney(transaction.getAmount()) + 
                                ChatColor.GRAY + " (" + transaction.getNote() + ")");
                    });
        }
    }
    
    private boolean handleList(CommandSender sender) {
        var civilizations = plugin.getDataManager().getAllCivilizations().values();
        
        if (civilizations.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No civilizations exist.");
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "=== All Civilizations (" + civilizations.size() + ") ===");
        for (Civilization civ : civilizations) {
            sender.sendMessage(ChatColor.YELLOW + civ.getName() + ChatColor.WHITE + 
                    " (Level " + civ.getLevel() + ", " + civ.getTotalMemberCount() + " members, " +
                    civ.getClaims().size() + " claims) " + ChatColor.GRAY + "[" + civ.getUuid() + "]");
        }
        
        return true;
    }
    
    private boolean handleSetLevel(CommandSender sender, String[] args) {
        if (!sender.hasPermission("civilization.admin.level")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to set civilization levels!");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /cvadmin setlevel <civilization_name> <level>");
            return true;
        }
        
        try {
            int level = Integer.parseInt(args[args.length - 1]);
            String civName = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1));
            
            Civilization civ = plugin.getDataManager().getCivilizationByName(civName);
            if (civ == null) {
                sender.sendMessage(ChatColor.RED + "Civilization not found!");
                return true;
            }
            
            if (level < 1) {
                sender.sendMessage(ChatColor.RED + "Level must be at least 1!");
                return true;
            }
            
            civ.setLevel(level);
            plugin.getDataManager().saveCivilization(civ);
            
            sender.sendMessage(ChatColor.GREEN + "Set " + civ.getName() + " level to " + level + "!");
            plugin.getLogger().info("Admin " + sender.getName() + " set " + civ.getName() + " level to " + level);
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid level number!");
        }
        
        return true;
    }
    
    private boolean handleSetMoney(CommandSender sender, String[] args) {
        if (!sender.hasPermission("civilization.admin.money")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to modify civilization money!");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /cvadmin setmoney <civilization_name> <amount>");
            return true;
        }
        
        try {
            double amount = Double.parseDouble(args[args.length - 1]);
            String civName = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1));
            
            Civilization civ = plugin.getDataManager().getCivilizationByName(civName);
            if (civ == null) {
                sender.sendMessage(ChatColor.RED + "Civilization not found!");
                return true;
            }
            
            if (amount < 0) {
                sender.sendMessage(ChatColor.RED + "Amount cannot be negative!");
                return true;
            }
            
            String adminUUID = sender instanceof Player ? ((Player) sender).getUniqueId().toString() : "CONSOLE";
            plugin.getEconomyManager().setCivBankBalance(civ.getUuid(), amount, adminUUID);
            
            sender.sendMessage(ChatColor.GREEN + "Set " + civ.getName() + " bank balance to " + 
                    plugin.getEconomyManager().formatMoney(amount) + "!");
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount!");
        }
        
        return true;
    }
    
    private boolean handleAddMoney(CommandSender sender, String[] args) {
        if (!sender.hasPermission("civilization.admin.money")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to modify civilization money!");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /cvadmin addmoney <civilization_name> <amount>");
            return true;
        }
        
        try {
            double amount = Double.parseDouble(args[args.length - 1]);
            String civName = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1));
            
            Civilization civ = plugin.getDataManager().getCivilizationByName(civName);
            if (civ == null) {
                sender.sendMessage(ChatColor.RED + "Civilization not found!");
                return true;
            }
            
            String adminUUID = sender instanceof Player ? ((Player) sender).getUniqueId().toString() : "CONSOLE";
            plugin.getEconomyManager().addCivBankBalance(civ.getUuid(), amount, adminUUID);
            
            String action = amount > 0 ? "Added" : "Removed";
            sender.sendMessage(ChatColor.GREEN + action + " " + plugin.getEconomyManager().formatMoney(Math.abs(amount)) + 
                    " " + (amount > 0 ? "to" : "from") + " " + civ.getName() + " bank!");
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount!");
        }
        
        return true;
    }
    
    private boolean handleForceJoin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("civilization.admin.forcejoin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to force join players!");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /cvadmin forcejoin <player> <civilization_name>");
            return true;
        }
        
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }
        
        String civName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        Civilization civ = plugin.getDataManager().getCivilizationByName(civName);
        
        if (civ == null) {
            sender.sendMessage(ChatColor.RED + "Civilization not found!");
            return true;
        }
        
        // Check if player is already in a civilization
        String targetUUID = target.getUniqueId().toString();
        Civilization currentCiv = plugin.getDataManager().getPlayerCivilization(targetUUID);
        
        if (currentCiv != null) {
            // Remove from current civilization
            currentCiv.removeMember(targetUUID);
            plugin.getDataManager().saveCivilization(currentCiv);
        }
        
        // Add to new civilization
        civ.addMember(targetUUID, com.civmc.model.CivRole.RECRUIT);
        plugin.getDataManager().saveCivilization(civ);
        
        sender.sendMessage(ChatColor.GREEN + "Successfully forced " + target.getName() + " to join " + civ.getName() + "!");
        target.sendMessage(ChatColor.YELLOW + "You have been added to " + civ.getName() + " by an administrator.");
        
        plugin.getLogger().info("Admin " + sender.getName() + " forced " + target.getName() + " to join " + civ.getName());
        
        return true;
    }
    
    private boolean handleBackup(CommandSender sender) {
        if (!sender.hasPermission("civilization.admin.save")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to create backups!");
            return true;
        }
        
        sender.sendMessage(ChatColor.YELLOW + "Creating backup...");
        
        try {
            plugin.getDataManager().getStorageProvider().backup();
            sender.sendMessage(ChatColor.GREEN + "Backup created successfully!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to create backup: " + e.getMessage());
            plugin.getLogger().severe("Failed to create backup: " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean handleDebug(CommandSender sender) {
        if (!sender.hasPermission("civilization.admin.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        sender.sendMessage(ChatColor.YELLOW + "=== DEBUG INFO ===");
        sender.sendMessage("Config language: " + plugin.getConfigManager().getLanguage());
        sender.sendMessage("Message manager language: " + plugin.getMessageManager().getCurrentLanguage());
        sender.sendMessage("Test prefix: " + plugin.getMessageManager().getMessage("prefix"));
        sender.sendMessage("Test help message: " + plugin.getMessageManager().getMessage("no-permission"));
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== CivilizationMC Admin Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/cvadmin reload" + ChatColor.WHITE + " - Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/cvadmin save" + ChatColor.WHITE + " - Force save all data");
        sender.sendMessage(ChatColor.YELLOW + "/cvadmin backup" + ChatColor.WHITE + " - Create data backup");
        sender.sendMessage(ChatColor.YELLOW + "/cvadmin list" + ChatColor.WHITE + " - List all civilizations");
        sender.sendMessage(ChatColor.YELLOW + "/cvadmin info <civ>" + ChatColor.WHITE + " - Detailed civilization info");
        sender.sendMessage(ChatColor.YELLOW + "/cvadmin delete <civ>" + ChatColor.WHITE + " - Delete civilization");
        sender.sendMessage(ChatColor.YELLOW + "/cvadmin setlevel <civ> <level>" + ChatColor.WHITE + " - Set civilization level");
        sender.sendMessage(ChatColor.YELLOW + "/cvadmin setmoney <civ> <amount>" + ChatColor.WHITE + " - Set bank balance");
        sender.sendMessage(ChatColor.YELLOW + "/cvadmin addmoney <civ> <amount>" + ChatColor.WHITE + " - Add/remove money");
        sender.sendMessage(ChatColor.YELLOW + "/cvadmin forcejoin <player> <civ>" + ChatColor.WHITE + " - Force join player");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("civilization.admin")) return null;
        
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList(
                    "reload", "save", "backup", "list", "info", "delete", "setlevel", 
                    "setmoney", "addmoney", "forcejoin", "help"
            );
            return subcommands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "forcejoin":
                    return plugin.getServer().getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "info":
                case "delete":
                case "setlevel":
                case "setmoney":
                case "addmoney":
                    return plugin.getDataManager().getAllCivilizations().values().stream()
                            .map(Civilization::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
            }
        }
        
        if (args.length >= 3 && args[0].equalsIgnoreCase("forcejoin")) {
            return plugin.getDataManager().getAllCivilizations().values().stream()
                    .map(Civilization::getName)
                    .filter(name -> name.toLowerCase().startsWith(String.join(" ", 
                            Arrays.copyOfRange(args, 2, args.length)).toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return null;
    }
}