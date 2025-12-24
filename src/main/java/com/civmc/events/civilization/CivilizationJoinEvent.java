package com.civmc.events.civilization;

import com.civmc.events.CivEvent;
import com.civmc.model.Civilization;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Event called when a player joins a civilization
 */
public class CivilizationJoinEvent extends CivEvent implements Cancellable {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Civilization civilization;
    private final Player player;
    private boolean cancelled = false;
    
    public CivilizationJoinEvent(Civilization civilization, Player player) {
        this.civilization = civilization;
        this.player = player;
    }
    
    public Civilization getCivilization() {
        return civilization;
    }
    
    public Player getPlayer() {
        return player;
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