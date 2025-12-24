package com.civmc.model;

import java.util.Set;

public class War {
    private String id;
    private String civA;
    private String civB;
    private WarState state;
    private long startAt;
    private long endAt;
    private long warmupEndAt;
    private WarScore score;
    private String reason;
    private String endReason;
    private long endTime;
    private Set<String> attackers;
    private Set<String> defenders;
    
    public War() {
        this.score = new WarScore();
        this.state = WarState.PREP;
        this.attackers = new java.util.concurrent.ConcurrentHashMap<String, Boolean>().keySet();
        this.defenders = new java.util.concurrent.ConcurrentHashMap<String, Boolean>().keySet();
    }
    
    public War(String id, String civA, String civB, String reason) {
        this();
        this.id = id;
        this.civA = civA;
        this.civB = civB;
        this.reason = reason;
        this.startAt = System.currentTimeMillis();
    }
    
    public War(String id, String civA, String civB) {
        this(id, civA, civB, null);
    }
    
    public boolean isInvolvedCiv(String civId) {
        return civA.equals(civId) || civB.equals(civId);
    }
    
    public String getOpponentCiv(String civId) {
        if (civA.equals(civId)) return civB;
        if (civB.equals(civId)) return civA;
        return null;
    }
    
    public boolean isActive() {
        return state == WarState.WAR;
    }
    
    public boolean isPrep() {
        return state == WarState.PREP;
    }
    
    public boolean isEnded() {
        return state == WarState.ENDED;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCivA() {
        return civA;
    }
    
    public void setCivA(String civA) {
        this.civA = civA;
    }
    
    public String getCivB() {
        return civB;
    }
    
    public void setCivB(String civB) {
        this.civB = civB;
    }
    
    public WarState getState() {
        return state;
    }
    
    public void setState(WarState state) {
        this.state = state;
    }
    
    public long getStartAt() {
        return startAt;
    }
    
    public void setStartAt(long startAt) {
        this.startAt = startAt;
    }
    
    public long getEndAt() {
        return endAt;
    }
    
    public void setEndAt(long endAt) {
        this.endAt = endAt;
    }
    
    public long getWarmupEndAt() {
        return warmupEndAt;
    }
    
    public void setWarmupEndAt(long warmupEndAt) {
        this.warmupEndAt = warmupEndAt;
    }
    
    public WarScore getScore() {
        return score;
    }
    
    public void setScore(WarScore score) {
        this.score = score != null ? score : new WarScore();
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getEndReason() {
        return endReason;
    }
    
    public void setEndReason(String endReason) {
        this.endReason = endReason;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    public Set<String> getAttackers() {
        return attackers;
    }
    
    public void setAttackers(Set<String> attackers) {
        this.attackers = attackers != null ? attackers : new java.util.concurrent.ConcurrentHashMap<String, Boolean>().keySet();
    }
    
    public Set<String> getDefenders() {
        return defenders;
    }
    
    public void setDefenders(Set<String> defenders) {
        this.defenders = defenders != null ? defenders : new java.util.concurrent.ConcurrentHashMap<String, Boolean>().keySet();
    }
    
    public static class WarScore {
        private int civAScore;
        private int civBScore;
        
        public WarScore() {
            this.civAScore = 0;
            this.civBScore = 0;
        }
        
        public int getCivAScore() {
            return civAScore;
        }
        
        public void setCivAScore(int civAScore) {
            this.civAScore = civAScore;
        }
        
        public int getCivBScore() {
            return civBScore;
        }
        
        public void setCivBScore(int civBScore) {
            this.civBScore = civBScore;
        }
        
        public void addCivAScore(int points) {
            this.civAScore += points;
        }
        
        public void addCivBScore(int points) {
            this.civBScore += points;
        }
    }
}