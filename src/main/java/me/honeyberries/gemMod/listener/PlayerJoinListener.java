package me.honeyberries.gemMod.listener;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.honeyberries.gemMod.GemMod;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HexFormat;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Listener class for handling player join events.
 * This class ensures that players are prompted to download and use the GemMod resource pack upon joining the server.
 */
public class PlayerJoinListener implements Listener {

    // Reference to the main plugin instance
    private final GemMod plugin = GemMod.getInstance();

    /**
     * Event handler for the PlayerJoinEvent.
     * When a player joins the server, they are prompted to download and use the GemMod resource pack.
     *
     * @param event The PlayerJoinEvent triggered when a player joins the server.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // URL of the resource pack to be downloaded
        String resourcePackUrl = "https://honeyberries.net/data/gemmodassets.zip";

        // SHA-1 hash of the resource pack for validation
        byte[] sha1 = HexFormat.of().parseHex("a11dbe3d078cb0e7bcb38492e1d6f8058c2cdac5");

        // Prompt message displayed to the player
        String prompt = "Please download the GemMod resource pack to enhance your experience.\n Promise no lag! Please install!";

        // Unique identifier for the resource pack
        UUID resourcePackId = UUID.randomUUID();

        // Schedule a task to set the resource pack for the player
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            // Check if the player is online before setting the resource pack
                if (player.isOnline()) {
                    // Add the resource pack to the player
                    player.addResourcePack(resourcePackId, resourcePackUrl, sha1, prompt, true);
                } else {
                    // Log a warning if the player is not online
                    plugin.getLogger().warning("Player " + player.getName() + " is not online when trying to set resource pack.");
                }
        });
    }
}