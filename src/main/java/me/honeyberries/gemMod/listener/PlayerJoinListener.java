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

public class PlayerJoinListener implements Listener {

    private final GemMod plugin = GemMod.getInstance();

    // Track latches for players who are in the process of receiving a resource pack
    private final Map<UUID, CountDownLatch> latches = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerConfig(AsyncPlayerConnectionConfigureEvent event) {
        Audience audience = event.getConnection().getAudience();

        URI rpURI = URI.create("https://honeyberries.net/data/gemmodassets.zip");
        String sha1 = "a11dbe3d078cb0e7bcb38492e1d6f8058c2cdac5";
        UUID rpUUID = UUID.nameUUIDFromBytes(sha1.getBytes());

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ResourcePackStatus> packStatus = new AtomicReference<>();
        UUID playerId = event.getConnection().getProfile().getId();

        // Store latch for early release on disconnect
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
            // Wait up to 10 seconds for the player's response
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

    @EventHandler
    public void onPlayerDisconnect(PlayerConnectionCloseEvent event) {
        CountDownLatch latch = latches.remove(event.getPlayerUniqueId());
        if (latch != null) {
            latch.countDown(); // Release latch early if player disconnects to prevent hanging thread
        }
    }
}
