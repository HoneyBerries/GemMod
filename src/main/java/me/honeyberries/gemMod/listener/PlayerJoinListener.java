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
 * <p>
 * This listener ensures that players are prompted to download the required resource pack upon joining
 * and handles their response. If a player disconnects during this process, it gracefully cleans up resources.
 *
 * @author HoneyBerries
 * @version 1.0
 */
public class PlayerJoinListener implements Listener {

    private final GemMod plugin = GemMod.getInstance();

    public PlayerJoinListener() {
    }

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
        String sha1 = plugin.getResourcePackSha1();
        if (sha1 == null) {
            plugin.getLogger().warning("Resource pack SHA-1 is not available. Skipping resource pack enforcement.");
            return;
        }
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
                .prompt(Component.text()
                    .append(Component.text("Please Download this Resource Pack.\n").color(NamedTextColor.AQUA))
                    .append(Component.text("It Will Make Your Experience Much Better\n").color(NamedTextColor.GOLD))
                    .append(Component.text("Trust :)").color(NamedTextColor.LIGHT_PURPLE))
                    .build())
                .replace(false)
                .required(true)
                .callback((uuid, status, ignored) -> {
                    packStatus.set(status);
                    latch.countDown();
                })
                .build();

        audience.sendResourcePacks(rpRequest);

        try {
            // Wait up to 60 seconds for the player's response.
            boolean finished = latch.await(60, TimeUnit.SECONDS);

            if (!finished) {
                plugin.getLogger().warning("Player " + playerId + " did not respond to resource pack request in time.");
                event.getConnection().disconnect(
                        Component.text("Timed out waiting for resource pack response.", NamedTextColor.RED)
                );
                return;
            }

            ResourcePackStatus status = packStatus.get();

            // Kick the player if they declined the pack.
            if (status == ResourcePackStatus.DECLINED) {
                event.getConnection().disconnect(
                        Component.text("You must accept the GemMod resource pack to play, sorry :(.", NamedTextColor.RED)
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            latches.remove(playerId);
        }
    }

    /**
     * Handles player disconnections to clean up any pending resource pack latches.
     * <p>
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