package com.civmc.economy;

import com.civmc.CivilizationMC;
import com.civmc.model.Civilization;
import com.civmc.model.Transaction;
import com.civmc.model.TransactionType;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class EconomyManager {
    
    private final CivilizationMC plugin;
    private final Logger logger;
    private Economy economy;
    
    public EconomyManager(CivilizationMC plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.economy = plugin.getEconomy();
    }
    
    // Player Economy Methods
    
    public boolean hasMoney(String playerUUID, double amount) {
        if (economy == null) return true; // If no economy, always return true
        
        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
            return economy.getBalance(player) >= amount;
        } catch (Exception e) {
            logger.warning("Error checking player balance for " + playerUUID + ": " + e.getMessage());
            return false;
        }
    }
    
    public double getBalance(String playerUUID) {
        if (economy == null) return 0.0;
        
        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
            return economy.getBalance(player);
        } catch (Exception e) {
            logger.warning("Error getting player balance for " + playerUUID + ": " + e.getMessage());
            return 0.0;
        }
    }
    
    public boolean withdrawMoney(String playerUUID, double amount) {
        if (economy == null) return true; // If no economy, always succeed
        
        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
            return economy.withdrawPlayer(player, amount).transactionSuccess();
        } catch (Exception e) {
            logger.warning("Error withdrawing money from " + playerUUID + ": " + e.getMessage());
            return false;
        }
    }
    
    public boolean depositMoney(String playerUUID, double amount) {
        if (economy == null) return true; // If no economy, always succeed
        
        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
            return economy.depositPlayer(player, amount).transactionSuccess();
        } catch (Exception e) {
            logger.warning("Error depositing money to " + playerUUID + ": " + e.getMessage());
            return false;
        }
    }
    
    // Civilization Bank Methods
    
    public CompletableFuture<CivBankResult> depositToCivBank(String playerUUID, String civUUID, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Civilization civ = plugin.getDataManager().getCivilization(civUUID);
                if (civ == null) {
                    return CivBankResult.CIVILIZATION_NOT_FOUND;
                }
                
                // Check if player is a member
                if (!civ.isMember(playerUUID)) {
                    return CivBankResult.NOT_A_MEMBER;
                }
                
                // Check if player has enough money
                if (!hasMoney(playerUUID, amount)) {
                    return CivBankResult.INSUFFICIENT_FUNDS;
                }
                
                // Withdraw from player
                if (!withdrawMoney(playerUUID, amount)) {
                    return CivBankResult.TRANSACTION_FAILED;
                }
                
                // Add to civilization bank
                civ.deposit(amount, playerUUID, "Bank deposit");
                plugin.getDataManager().saveCivilization(civ);
                
                // Log transaction
                logger.info("Player " + playerUUID + " deposited " + formatMoney(amount) + " to civilization " + civ.getName());
                
                return CivBankResult.SUCCESS;
                
            } catch (Exception e) {
                logger.severe("Error depositing to civilization bank: " + e.getMessage());
                e.printStackTrace();
                return CivBankResult.ERROR;
            }
        });
    }
    
    public CompletableFuture<CivBankResult> withdrawFromCivBank(String playerUUID, String civUUID, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Civilization civ = plugin.getDataManager().getCivilization(civUUID);
                if (civ == null) {
                    return CivBankResult.CIVILIZATION_NOT_FOUND;
                }
                
                // Check if player has permission to withdraw
                if (!plugin.getCivilizationManager().hasPermission(playerUUID, civUUID, "manage_bank")) {
                    return CivBankResult.NO_PERMISSION;
                }
                
                // Check if civilization has enough money
                if (civ.getBankBalance() < amount) {
                    return CivBankResult.INSUFFICIENT_FUNDS;
                }
                
                // Withdraw from civilization bank
                if (!civ.withdraw(amount, playerUUID, "Bank withdrawal")) {
                    return CivBankResult.INSUFFICIENT_FUNDS;
                }
                
                // Deposit to player
                if (!depositMoney(playerUUID, amount)) {
                    // Rollback
                    civ.deposit(amount, playerUUID, "Rollback failed withdrawal");
                    plugin.getDataManager().saveCivilization(civ);
                    return CivBankResult.TRANSACTION_FAILED;
                }
                
                plugin.getDataManager().saveCivilization(civ);
                
                // Log transaction
                logger.info("Player " + playerUUID + " withdrew " + formatMoney(amount) + " from civilization " + civ.getName());
                
                return CivBankResult.SUCCESS;
                
            } catch (Exception e) {
                logger.severe("Error withdrawing from civilization bank: " + e.getMessage());
                e.printStackTrace();
                return CivBankResult.ERROR;
            }
        });
    }
    
    public CompletableFuture<CivBankResult> transferBetweenCivs(String senderCivUUID, String receiverCivUUID, double amount, String senderPlayerUUID, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Civilization senderCiv = plugin.getDataManager().getCivilization(senderCivUUID);
                Civilization receiverCiv = plugin.getDataManager().getCivilization(receiverCivUUID);
                
                if (senderCiv == null || receiverCiv == null) {
                    return CivBankResult.CIVILIZATION_NOT_FOUND;
                }
                
                // Check if player has permission
                if (!plugin.getCivilizationManager().hasPermission(senderPlayerUUID, senderCivUUID, "manage_bank")) {
                    return CivBankResult.NO_PERMISSION;
                }
                
                // Check if sender has enough money
                if (senderCiv.getBankBalance() < amount) {
                    return CivBankResult.INSUFFICIENT_FUNDS;
                }
                
                // Transfer money
                if (!senderCiv.withdraw(amount, senderPlayerUUID, reason)) {
                    return CivBankResult.INSUFFICIENT_FUNDS;
                }
                
                receiverCiv.deposit(amount, senderPlayerUUID, reason);
                
                // Save both civilizations
                plugin.getDataManager().saveCivilization(senderCiv);
                plugin.getDataManager().saveCivilization(receiverCiv);
                
                // Log transaction
                logger.info("Civilization " + senderCiv.getName() + " transferred " + formatMoney(amount) + 
                           " to " + receiverCiv.getName() + " - Reason: " + reason);
                
                return CivBankResult.SUCCESS;
                
            } catch (Exception e) {
                logger.severe("Error transferring between civilizations: " + e.getMessage());
                e.printStackTrace();
                return CivBankResult.ERROR;
            }
        });
    }
    
    // Utility Methods
    
    public String formatMoney(double amount) {
        if (economy == null) {
            return String.format("%.2f", amount);
        }
        
        try {
            return economy.format(amount);
        } catch (Exception e) {
            return String.format("%.2f", amount);
        }
    }
    
    public String getCurrencyName() {
        if (economy == null) {
            return "Money";
        }
        
        try {
            return economy.currencyNamePlural();
        } catch (Exception e) {
            return "Money";
        }
    }
    
    public boolean isEconomyEnabled() {
        return economy != null;
    }
    
    // Admin Methods
    
    public boolean setCivBankBalance(String civUUID, double amount, String adminUUID) {
        try {
            Civilization civ = plugin.getDataManager().getCivilization(civUUID);
            if (civ == null) return false;
            
            double oldBalance = civ.getBankBalance();
            civ.setBankBalance(amount);
            
            // Add transaction record
            Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                civUUID,
                adminUUID,
                amount > oldBalance ? TransactionType.DEPOSIT : TransactionType.WITHDRAW,
                Math.abs(amount - oldBalance),
                amount,
                "Admin balance adjustment"
            );
            civ.addTransaction(transaction);
            
            plugin.getDataManager().saveCivilization(civ);
            
            logger.info("Admin " + adminUUID + " set civilization " + civ.getName() + 
                       " bank balance to " + formatMoney(amount) + " (was " + formatMoney(oldBalance) + ")");
            
            return true;
        } catch (Exception e) {
            logger.severe("Error setting civilization bank balance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean addCivBankBalance(String civUUID, double amount, String adminUUID) {
        try {
            Civilization civ = plugin.getDataManager().getCivilization(civUUID);
            if (civ == null) return false;
            
            if (amount > 0) {
                civ.deposit(amount, adminUUID, "Admin deposit");
            } else {
                civ.withdraw(Math.abs(amount), adminUUID, "Admin withdrawal");
            }
            
            plugin.getDataManager().saveCivilization(civ);
            
            logger.info("Admin " + adminUUID + " adjusted civilization " + civ.getName() + 
                       " bank balance by " + formatMoney(amount));
            
            return true;
        } catch (Exception e) {
            logger.severe("Error adjusting civilization bank balance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Tax System (Future Implementation)
    
    public void processAutomaticTaxes() {
        // This method can be called by a scheduled task to process automatic taxes
        // Implementation depends on specific tax rules
        try {
            var config = plugin.getConfigManager().getConfig();
            if (!config.getBoolean("economy.taxes.enabled", false)) {
                return;
            }
            
            double taxRate = config.getDouble("economy.taxes.rate", 0.01); // 1% default
            long taxInterval = config.getLong("economy.taxes.interval-hours", 24) * 60 * 60 * 1000; // 24 hours default
            
            for (Civilization civ : plugin.getDataManager().getAllCivilizations().values()) {
                // Check if tax is due
                long lastTax = civ.getCustomData().containsKey("last_tax") ? 
                    (Long) civ.getCustomData().get("last_tax") : civ.getCreatedAt();
                
                if (System.currentTimeMillis() - lastTax >= taxInterval) {
                    processCivilizationTax(civ, taxRate);
                }
            }
            
        } catch (Exception e) {
            logger.severe("Error processing automatic taxes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void processCivilizationTax(Civilization civ, double taxRate) {
        try {
            double claimCount = civ.getClaims().size();
            double baseTax = plugin.getConfigManager().getConfig().getDouble("economy.taxes.base-amount", 10.0);
            double totalTax = baseTax + (claimCount * taxRate);
            
            if (civ.getBankBalance() >= totalTax) {
                civ.withdraw(totalTax, "SYSTEM", "Automatic tax collection");
                civ.getCustomData().put("last_tax", System.currentTimeMillis());
                plugin.getDataManager().saveCivilization(civ);
                
                logger.info("Collected " + formatMoney(totalTax) + " tax from civilization " + civ.getName());
            } else {
                // Handle insufficient funds - could add penalties or warnings
                logger.warning("Civilization " + civ.getName() + " has insufficient funds for tax payment");
                
                // Could implement grace period or penalties here
                civ.getCustomData().put("tax_debt", 
                    (Double) civ.getCustomData().getOrDefault("tax_debt", 0.0) + totalTax);
                civ.getCustomData().put("last_tax_attempt", System.currentTimeMillis());
                plugin.getDataManager().saveCivilization(civ);
            }
            
        } catch (Exception e) {
            logger.severe("Error processing tax for civilization " + civ.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Result Enum
    
    public enum CivBankResult {
        SUCCESS, CIVILIZATION_NOT_FOUND, NOT_A_MEMBER, NO_PERMISSION, 
        INSUFFICIENT_FUNDS, TRANSACTION_FAILED, ERROR
    }
}