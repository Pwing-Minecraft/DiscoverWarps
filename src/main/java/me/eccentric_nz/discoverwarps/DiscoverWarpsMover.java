/*
 *  Copyright 2014 eccentric_nz.
 */
package me.eccentric_nz.discoverwarps;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import io.papermc.lib.PaperLib;
import multiworld.MultiWorldPlugin;
import multiworld.api.MultiWorldAPI;
import multiworld.api.MultiWorldWorldData;
import multiworld.api.flag.FlagName;
import org.bukkit.*;
import org.bukkit.entity.Player;

/**
 * @author eccentric_nz
 */
public class DiscoverWarpsMover {

    private final DiscoverWarps plugin;

    public DiscoverWarpsMover(DiscoverWarps plugin) {
        this.plugin = plugin;
    }

    public void movePlayer(Player p, Location l, World from) {

        p.sendMessage(plugin.getLocalisedName() + plugin.getConfig().getString("localisation.teleport") + "...");

        Player thePlayer = p;
        Location theLocation = l;
        World to = theLocation.getWorld();
        boolean allowFlight = thePlayer.getAllowFlight();
        boolean crossWorlds = (from != to);
        boolean isSurvival = checkSurvival(to);

        // adjust location to centre of plate
        theLocation.setX(l.getX() + 0.5);
        theLocation.setZ(l.getZ() + 0.5);

        // try loading chunk
        World world = l.getWorld();
        Chunk chunk = world.getChunkAt(l);
        if (!world.isChunkLoaded(chunk)) {
            world.loadChunk(chunk);
        }

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            PaperLib.teleportAsync(thePlayer, theLocation);
            thePlayer.getWorld().playSound(theLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
        }, 5L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            PaperLib.teleportAsync(thePlayer, theLocation);
            if (plugin.getConfig().getBoolean("no_damage")) {
                thePlayer.setNoDamageTicks(plugin.getConfig().getInt("no_damage_time") * 20);
            }
            if (thePlayer.getGameMode() == GameMode.CREATIVE || (allowFlight && crossWorlds && !isSurvival)) {
                thePlayer.setAllowFlight(true);
            }
        }, 10L);
    }

    /**
     * Checks if the world the player is teleporting to is a SURVIVAL world.
     *
     * @param w the world to check
     * @return true if the world is a SURVIVAL world, otherwise false
     */
    private boolean checkSurvival(World w) {
        boolean bool = false;
        if (plugin.pm.isPluginEnabled("Multiverse-Core")) {
            MultiverseCore mv = (MultiverseCore) plugin.pm.getPlugin("Multiverse-Core");
            MultiverseWorld mvw = mv.getCore().getMVWorldManager().getMVWorld(w);
            GameMode gm = mvw.getGameMode();
            if (gm.equals(GameMode.SURVIVAL)) {
                bool = true;
            }
        }
        if (plugin.pm.isPluginEnabled("MultiWorld")) {
            MultiWorldAPI mw = ((MultiWorldPlugin) plugin.pm.getPlugin("MultiWorld")).getApi();
            MultiWorldWorldData mww = mw.getWorld(w.getName());
            if (!mww.isOptionSet(FlagName.CREATIVEWORLD)) {
                bool = true;
            }
        }
        return bool;
    }
}
