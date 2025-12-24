package com.civmc.events.civilization;

import com.civmc.events.CivEvent;
import com.civmc.model.Civilization;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Event called when a player leaves a civilization
 */
public class CivilizationLeaveEvent extends CivEvent {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Civilization civilization;
    private final Player player;
    
    public CivilizationLeaveEvent(Civilization civilization, Player player) {
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
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}