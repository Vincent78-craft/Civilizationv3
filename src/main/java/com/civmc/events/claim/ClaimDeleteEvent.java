package com.civmc.events.claim;

import com.civmc.events.CivEvent;
import com.civmc.model.Claim;
import org.bukkit.event.HandlerList;

/**
 * Event called when a chunk is unclaimed
 */
public class ClaimDeleteEvent extends CivEvent {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Claim claim;
    
    public ClaimDeleteEvent(Claim claim) {
        this.claim = claim;
    }
    
    public Claim getClaim() {
        return claim;
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}