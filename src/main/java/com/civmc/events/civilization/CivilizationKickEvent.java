package com.civmc.events.civilization;

import com.civmc.events.CivEvent;
import com.civmc.model.Civilization;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Event called when a player is kicked from a civilization
 */
public class CivilizationKickEvent extends CivEvent {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Civilization civilization;
    private final Player kicker;
    private final Player kicked;
    
    public CivilizationKickEvent(Civilization civilization, Player kicker, Player kicked) {
        this.civilization = civilization;
        this.kicker = kicker;
        this.kicked = kicked;
    }
    
    public Civilization getCivilization() {
        return civilization;
    }
    
    public Player getKicker() {
        return kicker;
    }
    
    public Player getKicked() {
        return kicked;
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}