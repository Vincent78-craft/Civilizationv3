package com.civmc.events.claim;

import com.civmc.events.CivEvent;
import com.civmc.model.Claim;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Event called when a chunk is claimed
 */
public class ClaimCreateEvent extends CivEvent implements Cancellable {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Claim claim;
    private final Player claimer;
    private boolean cancelled = false;
    
    public ClaimCreateEvent(Claim claim, Player claimer) {
        this.claim = claim;
        this.claimer = claimer;
    }
    
    public Claim getClaim() {
        return claim;
    }
    
    public Player getClaimer() {
        return claimer;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}