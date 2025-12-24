package com.civmc.listeners;

import com.civmc.CivilizationMC;
import com.civmc.model.Claim;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.logging.Logger;

public class ChunkListener implements Listener {
    
    private final CivilizationMC plugin;
    private final Logger logger;
    
    public ChunkListener(CivilizationMC plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        String claimKey = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        
        Claim claim = plugin.getDataManager().getClaim(claimKey);
        if (claim != null) {
            // Perform any necessary setup for claimed chunks when they load
            // This could include:
            // - Applying chunk-specific settings
            // - Loading chunk-specific data
            // - Setting up protection systems
            
            logger.fine("Loaded claimed chunk: " + claimKey);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        String claimKey = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        
        Claim claim = plugin.getDataManager().getClaim(claimKey);
        if (claim != null) {
            // Perform any necessary cleanup for claimed chunks when they unload
            // This could include:
            // - Saving chunk-specific data
            // - Cleaning up temporary data structures
            // - Logging chunk activity
            
            logger.fine("Unloaded claimed chunk: " + claimKey);
        }
    }
}