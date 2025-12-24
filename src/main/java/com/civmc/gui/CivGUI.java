package com.civmc.gui;

import com.civmc.CivilizationMC;
import com.civmc.model.Civilization;
import com.civmc.model.CivRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Consumer;

public abstract class CivGUI implements Listener {
    
    protected final CivilizationMC plugin;
    protected final Player player;
    protected final String title;
    protected final int size;
    protected Inventory inventory;
    
    protected final Map<Integer, Consumer<Player>> clickActions = new HashMap<>();
    protected final Set<Integer> unclickableSlots = new HashSet<>();
    
    public CivGUI(CivilizationMC plugin, Player player, String title, int size) {
        this.plugin = plugin;
        this.player = player;
        this.title = ChatColor.translateAlternateColorCodes('&', title);
        this.size = size;
        
        // Register this GUI as an event listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        createInventory();
        setupGUI();
    }
    
    private void createInventory() {
        if (size % 9 == 0) {
            inventory = Bukkit.createInventory(null, size, title);
        } else {
            inventory = Bukkit.createInventory(null, InventoryType.CHEST, title);
        }
    }
    
    protected abstract void setupGUI();
    
    public void open() {
        player.openInventory(inventory);
    }
    
    public void close() {
        player.closeInventory();
    }
    
    protected void setItem(int slot, Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(loreList);
            }
            
            item.setItemMeta(meta);
        }
        
        inventory.setItem(slot, item);
    }
    
    protected void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }
    
    protected void setClickAction(int slot, Consumer<Player> action) {
        clickActions.put(slot, action);
    }
    
    protected void setUnclickable(int slot) {
        unclickableSlots.add(slot);
    }
    
    protected void fillBorder(Material material) {
        ItemStack borderItem = new ItemStack(material);
        ItemMeta meta = borderItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GRAY + "");
            borderItem.setItemMeta(meta);
        }
        
        // Top and bottom rows
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderItem);
            inventory.setItem(size - 9 + i, borderItem);
        }
        
        // Left and right columns
        for (int i = 9; i < size - 9; i += 9) {
            inventory.setItem(i, borderItem);
            inventory.setItem(i + 8, borderItem);
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player clickedPlayer = (Player) event.getWhoClicked();
        
        if (!clickedPlayer.equals(player) || !event.getInventory().equals(inventory)) {
            return;
        }
        
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        if (unclickableSlots.contains(slot)) {
            return;
        }
        
        Consumer<Player> action = clickActions.get(slot);
        if (action != null) {
            try {
                action.accept(clickedPlayer);
            } catch (Exception e) {
                plugin.getLogger().warning("Error executing GUI action: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player closedPlayer = (Player) event.getPlayer();
        
        if (closedPlayer.equals(player) && event.getInventory().equals(inventory)) {
            onClose();
        }
    }
    
    protected void onClose() {
        // Unregister this GUI as an event listener
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
    }
    
    // Utility methods for common GUI elements
    
    protected void addBackButton(int slot, Runnable action) {
        setItem(slot, Material.ARROW, "&cBack", "&7Click to go back");
        setClickAction(slot, p -> {
            action.run();
        });
    }
    
    protected void addCloseButton(int slot) {
        setItem(slot, Material.BARRIER, "&cClose", "&7Click to close");
        setClickAction(slot, p -> p.closeInventory());
    }
    
    protected void addInfoItem(int slot, Material material, String name, String... info) {
        List<String> lore = new ArrayList<>();
        for (String line : info) {
            lore.add("&7" + line);
        }
        
        setItem(slot, material, "&e" + name, lore.toArray(new String[0]));
        setUnclickable(slot);
    }
    
    protected String formatRole(CivRole role) {
        switch (role) {
            case LEADER:
                return ChatColor.RED + "Leader";
            case OFFICER:
                return ChatColor.GOLD + "Officer";
            case MEMBER:
                return ChatColor.GREEN + "Member";
            case RECRUIT:
                return ChatColor.GRAY + "Recruit";
            default:
                return ChatColor.GRAY + "Unknown";
        }
    }
    
    protected Material getRoleMaterial(CivRole role) {
        switch (role) {
            case LEADER:
                return Material.DIAMOND_HELMET;
            case OFFICER:
                return Material.GOLDEN_HELMET;
            case MEMBER:
                return Material.IRON_HELMET;
            case RECRUIT:
                return Material.LEATHER_HELMET;
            default:
                return Material.BARRIER;
        }
    }
    
    // Static factory methods for common GUIs
    
    public static void openCivilizationInfo(CivilizationMC plugin, Player player, Civilization civ) {
        new CivilizationInfoGUI(plugin, player, civ).open();
    }
    
    public static void openMembersGUI(CivilizationMC plugin, Player player, Civilization civ) {
        new MembersGUI(plugin, player, civ).open();
    }
    
    public static void openClaimsGUI(CivilizationMC plugin, Player player, Civilization civ) {
        new ClaimsGUI(plugin, player, civ).open();
    }
}