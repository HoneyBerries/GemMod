package me.honeyberries.gemMod.listener;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import me.honeyberries.gemMod.GemMod;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handles events related to player connections, specifically for managing resource pack distribution.
 *
 * This listener ensures that players are prompted to download the required resource pack upon joining
 * and handles their response. If a player disconnects during this process, it gracefully cleans up resources.
 *
 * @author HoneyBerries
 * @version 1.0
 */
public class PlayerJoinListener implements Listener {

    private final GemMod plugin = GemMod.getInstance();

    /**
     * Tracks latches for players who are in the process of receiving a resource pack.
     * This ensures that the main thread does not hang if a player disconnects.
     */
    private final Map<UUID, CountDownLatch> latches = new ConcurrentHashMap<>();

    /**
     * Prompts a player to download the required resource pack upon joining the server.
     *
     * This event handler sends a resource pack request to the player and waits for their response.
     * If the player declines the pack, they are disconnected from the server.
     *
     * @param event The {@link AsyncPlayerConnectionConfigureEvent} triggered when a player connects.
     */
    @EventHandler
    public void onPlayerConfig(AsyncPlayerConnectionConfigureEvent event) {
        Audience audience = event.getConnection().getAudience();

        URI rpURI = URI.create("https://honeyberries.net/data/gemmodassets.zip");
        String sha1 = "a11dbe3d078cb0e7bcb38492e1d6f8058c2cdac5";
        UUID rpUUID = UUID.nameUUIDFromBytes(sha1.getBytes());

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ResourcePackStatus> packStatus = new AtomicReference<>();
        UUID playerId = event.getConnection().getProfile().getId();

        // Store the latch to allow for early release if the player disconnects.
        latches.put(playerId, latch);

        ResourcePackInfo rpInfo = ResourcePackInfo.resourcePackInfo()
                .uri(rpURI)
                .hash(sha1)
                .id(rpUUID)
                .build();

        ResourcePackRequest rpRequest = ResourcePackRequest.resourcePackRequest()
                .packs(rpInfo)
                .prompt(Component.text("Please download the resourcepack!").color(NamedTextColor.LIGHT_PURPLE))
                .replace(false)
                .required(true)
                .callback((uuid, status, ignored) -> {
                    plugin.getLogger().info("Player " + playerId + " resource pack status: " + status);
                    packStatus.set(status);
                    latch.countDown();
                })
                .build();

        audience.sendResourcePacks(rpRequest);

        try {
            // Wait up to 10 seconds for the player's response.
            boolean finished = latch.await(10, TimeUnit.SECONDS);

            if (!finished) {
                plugin.getLogger().warning("Player " + playerId + " did not respond to resource pack request in time.");
            }

            ResourcePackStatus status = packStatus.get();

            if (status == ResourcePackStatus.DECLINED) {
                event.getConnection().disconnect(
                        Component.text("You must accept the GemMod resource pack to play.", NamedTextColor.RED)
                );
            } else if (status != null && status != ResourcePackStatus.SUCCESSFULLY_LOADED) {
                plugin.getLogger().warning("Player " + playerId + " had non-kick resource pack issue: " + status);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            latches.remove(playerId);
        }
    }

    /**
     * Handles player disconnections to clean up any pending resource pack latches.
     *
     * If a player disconnects while the resource pack is being processed, this method
     * releases the corresponding latch to prevent the main thread from hanging.
     *
     * @param event The {@link PlayerConnectionCloseEvent} triggered when a player disconnects.
     */
    @EventHandler
    public void onPlayerDisconnect(PlayerConnectionCloseEvent event) {
        CountDownLatch latch = latches.remove(event.getPlayerUniqueId());
        if (latch != null) {
            latch.countDown(); // Release latch early to prevent the thread from hanging.
        }
    }
}
