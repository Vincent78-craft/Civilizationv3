package com.civmc.events.civilization;

import com.civmc.events.CivEvent;
import com.civmc.model.Civilization;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Event called when a civilization is created
 */
public class CivilizationCreateEvent extends CivEvent implements Cancellable {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Civilization civilization;
    private final Player creator;
    private boolean cancelled = false;
    
    public CivilizationCreateEvent(Civilization civilization, Player creator) {
        this.civilization = civilization;
        this.creator = creator;
    }
    
    public Civilization getCivilization() {
        return civilization;
    }
    
    public Player getCreator() {
        return creator;
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