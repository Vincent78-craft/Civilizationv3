package com.civmc.events.civilization;

import com.civmc.events.CivEvent;
import com.civmc.model.Civilization;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Event called when a civilization is disbanded
 */
public class CivilizationDisbandEvent extends CivEvent implements Cancellable {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Civilization civilization;
    private final Player leader;
    private boolean cancelled = false;
    
    public CivilizationDisbandEvent(Civilization civilization, Player leader) {
        this.civilization = civilization;
        this.leader = leader;
    }
    
    public Civilization getCivilization() {
        return civilization;
    }
    
    public Player getLeader() {
        return leader;
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