package com.civmc.events.civilization;

import com.civmc.events.CivEvent;
import com.civmc.model.Civilization;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Event called when a civilization is renamed
 */
public class CivilizationRenameEvent extends CivEvent {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Civilization civilization;
    private final String oldName;
    private final String newName;
    private final Player renamer;
    
    public CivilizationRenameEvent(Civilization civilization, String oldName, String newName, Player renamer) {
        this.civilization = civilization;
        this.oldName = oldName;
        this.newName = newName;
        this.renamer = renamer;
    }
    
    public Civilization getCivilization() {
        return civilization;
    }
    
    public String getOldName() {
        return oldName;
    }
    
    public String getNewName() {
        return newName;
    }
    
    public Player getRenamer() {
        return renamer;
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}