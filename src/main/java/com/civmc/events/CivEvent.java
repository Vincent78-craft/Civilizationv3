package com.civmc.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base class for all CivilizationMC events
 */
public abstract class CivEvent extends Event {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    protected CivEvent() {
        super();
    }
    
    protected CivEvent(boolean isAsync) {
        super(isAsync);
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}