package com.civmc.events.civilization;

import com.civmc.events.CivEvent;
import com.civmc.model.Civilization;
import com.civmc.model.Invitation;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Event called when a player is invited to a civilization
 */
public class CivilizationInviteEvent extends CivEvent implements Cancellable {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Civilization civilization;
    private final Invitation invitation;
    private final Player inviter;
    private final Player invitee;
    private boolean cancelled = false;
    
    public CivilizationInviteEvent(Civilization civilization, Invitation invitation, Player inviter, Player invitee) {
        this.civilization = civilization;
        this.invitation = invitation;
        this.inviter = inviter;
        this.invitee = invitee;
    }
    
    public Civilization getCivilization() {
        return civilization;
    }
    
    public Invitation getInvitation() {
        return invitation;
    }
    
    public Player getInviter() {
        return inviter;
    }
    
    public Player getInvitee() {
        return invitee;
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