package com.civmc.model;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Civilization {
    private String uuid;
    private String name;
    private int level;
    private double bankBalance;
    private String leaderUUID;
    private Set<String> officers;
    private Set<String> members;
    private Set<String> recruits;
    private long createdAt;
    private CivHome home;
    private Set<String> claims;
    private Set<String> allies;
    private Set<String> wars;
    private CivSettings settings;
    private List<Transaction> transactions;
    private String bannerData;
    private Map<String, Object> customData;
    
    public Civilization() {
        this.uuid = UUID.randomUUID().toString();
        this.officers = ConcurrentHashMap.newKeySet();
        this.members = ConcurrentHashMap.newKeySet();
        this.recruits = ConcurrentHashMap.newKeySet();
        this.claims = ConcurrentHashMap.newKeySet();
        this.allies = ConcurrentHashMap.newKeySet();
        this.wars = ConcurrentHashMap.newKeySet();
        this.transactions = new ArrayList<>();
        this.settings = new CivSettings();
        this.customData = new ConcurrentHashMap<>();
        this.level = 1;
        this.bankBalance = 0.0;
        this.createdAt = System.currentTimeMillis();
    }
    
    public Civilization(String name, String leaderUUID) {
        this();
        this.name = name;
        this.leaderUUID = leaderUUID;
    }
    
    // Role management
    public CivRole getPlayerRole(String playerUUID) {
        if (playerUUID.equals(leaderUUID)) return CivRole.LEADER;
        if (officers.contains(playerUUID)) return CivRole.OFFICER;
        if (members.contains(playerUUID)) return CivRole.MEMBER;
        if (recruits.contains(playerUUID)) return CivRole.RECRUIT;
        return null;
    }
    
    public boolean isMember(String playerUUID) {
        return getPlayerRole(playerUUID) != null;
    }
    
    public void addMember(String playerUUID, CivRole role) {
        removeMember(playerUUID); // Remove from all roles first
        
        switch (role) {
            case OFFICER:
                officers.add(playerUUID);
                break;
            case MEMBER:
                members.add(playerUUID);
                break;
            case RECRUIT:
                recruits.add(playerUUID);
                break;
            default:
                break;
        }
    }
    
    public void removeMember(String playerUUID) {
        officers.remove(playerUUID);
        members.remove(playerUUID);
        recruits.remove(playerUUID);
    }
    
    public void promoteMember(String playerUUID) {
        CivRole currentRole = getPlayerRole(playerUUID);
        if (currentRole == null || currentRole == CivRole.LEADER) return;
        
        removeMember(playerUUID);
        
        switch (currentRole) {
            case RECRUIT:
                addMember(playerUUID, CivRole.MEMBER);
                break;
            case MEMBER:
                addMember(playerUUID, CivRole.OFFICER);
                break;
            default:
                break;
        }
    }
    
    public void demoteMember(String playerUUID) {
        CivRole currentRole = getPlayerRole(playerUUID);
        if (currentRole == null || currentRole == CivRole.RECRUIT || currentRole == CivRole.LEADER) return;
        
        removeMember(playerUUID);
        
        switch (currentRole) {
            case OFFICER:
                addMember(playerUUID, CivRole.MEMBER);
                break;
            case MEMBER:
                addMember(playerUUID, CivRole.RECRUIT);
                break;
            default:
                break;
        }
    }
    
    public void setLeader(String playerUUID) {
        String oldLeader = this.leaderUUID;
        this.leaderUUID = playerUUID;
        
        // Remove new leader from other roles
        removeMember(playerUUID);
        
        // Demote old leader to officer
        if (oldLeader != null && !oldLeader.equals(playerUUID)) {
            addMember(oldLeader, CivRole.OFFICER);
        }
    }
    
    public Set<String> getAllMembers() {
        Set<String> allMembers = new HashSet<>();
        if (leaderUUID != null) allMembers.add(leaderUUID);
        allMembers.addAll(officers);
        allMembers.addAll(members);
        allMembers.addAll(recruits);
        return allMembers;
    }
    
    public int getTotalMemberCount() {
        return getAllMembers().size();
    }
    
    // Bank operations
    public void deposit(double amount, String actorUUID, String note) {
        bankBalance += amount;
        transactions.add(new Transaction(
            UUID.randomUUID().toString(),
            System.currentTimeMillis(),
            uuid,
            actorUUID,
            TransactionType.DEPOSIT,
            amount,
            bankBalance,
            note
        ));
    }
    
    public boolean withdraw(double amount, String actorUUID, String note) {
        if (bankBalance < amount) return false;
        
        bankBalance -= amount;
        transactions.add(new Transaction(
            UUID.randomUUID().toString(),
            System.currentTimeMillis(),
            uuid,
            actorUUID,
            TransactionType.WITHDRAW,
            amount,
            bankBalance,
            note
        ));
        return true;
    }
    
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        // Keep only last 100 transactions
        if (transactions.size() > 100) {
            transactions = new ArrayList<>(transactions.subList(transactions.size() - 100, transactions.size()));
        }
    }
    
    // Getters and Setters
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public double getBankBalance() {
        return bankBalance;
    }
    
    public void setBankBalance(double bankBalance) {
        this.bankBalance = bankBalance;
    }
    
    public String getLeaderUUID() {
        return leaderUUID;
    }
    
    public void setLeaderUUID(String leaderUUID) {
        this.leaderUUID = leaderUUID;
    }
    
    public Set<String> getOfficers() {
        return officers;
    }
    
    public void setOfficers(Set<String> officers) {
        this.officers = officers != null ? officers : ConcurrentHashMap.newKeySet();
    }
    
    public Set<String> getMembers() {
        return members;
    }
    
    public void setMembers(Set<String> members) {
        this.members = members != null ? members : ConcurrentHashMap.newKeySet();
    }
    
    public Set<String> getRecruits() {
        return recruits;
    }
    
    public void setRecruits(Set<String> recruits) {
        this.recruits = recruits != null ? recruits : ConcurrentHashMap.newKeySet();
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public CivHome getHome() {
        return home;
    }
    
    public void setHome(CivHome home) {
        this.home = home;
    }
    
    public Set<String> getClaims() {
        return claims;
    }
    
    public void setClaims(Set<String> claims) {
        this.claims = claims != null ? claims : ConcurrentHashMap.newKeySet();
    }
    
    public Set<String> getAllies() {
        return allies;
    }
    
    public void setAllies(Set<String> allies) {
        this.allies = allies != null ? allies : ConcurrentHashMap.newKeySet();
    }
    
    public Set<String> getWars() {
        return wars;
    }
    
    public void setWars(Set<String> wars) {
        this.wars = wars != null ? wars : ConcurrentHashMap.newKeySet();
    }
    
    public CivSettings getSettings() {
        return settings;
    }
    
    public void setSettings(CivSettings settings) {
        this.settings = settings != null ? settings : new CivSettings();
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions != null ? transactions : new ArrayList<>();
    }
    
    public String getBannerData() {
        return bannerData;
    }
    
    public void setBannerData(String bannerData) {
        this.bannerData = bannerData;
    }
    
    public Map<String, Object> getCustomData() {
        return customData;
    }
    
    public void setCustomData(Map<String, Object> customData) {
        this.customData = customData != null ? customData : new ConcurrentHashMap<>();
    }
}