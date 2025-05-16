package me.honeyberries.gemMod.listener;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import me.honeyberries.gemMod.manager.GemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Listener for Air Gem passive effects.
 *
 * <p>This listener is responsible for:</p>
 * <ul>
 *   <li>Detecting when a player with the Air Gem takes damage</li>
 *   <li>Cancelling fall damage and fly-into-wall damage for those players</li>
 *   <li>Logging the cancellation for debugging purposes</li>
 * </ul>
 *
 * <p>The Air Gem grants players immunity to fall damage and damage from flying into walls,
 * providing a significant mobility advantage.</p>
 *
 * @author HoneyBerries
 * @since 1.0
 */
public class AirGemListener implements Listener {

    /** Reference to the main plugin instance for logging and plugin operations. */
    private final GemMod plugin = GemMod.getInstance();

    /**
     * Handles player damage events to apply Air Gem passive effects.
     *
     * <p>This method:</p>
     * <ol>
     *   <li>Checks if the damaged entity is a player</li>
     *   <li>Verifies if the player has the Air Gem in their inventory</li>
     *   <li>Cancels the event if the damage cause is {@code FALL} or {@code FLY_INTO_WALL}</li>
     *   <li>Logs the cancellation for traceability</li>
     * </ol>
     *
     * @param event The {@link EntityDamageEvent} triggered when an entity takes damage.
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // Check if the damaged entity is a player
        if (event.getEntity() instanceof Player player) {
            // Check if the player has the Air Gem in their inventory
            if (GemManager.hasGem(player, GemType.AIR)) {
                // Only cancel damage if the cause is falling or flying into a wall
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                        || event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL) {
                    event.setCancelled(true); // Cancel the fall or fly-into-wall damage
                    plugin.getLogger().info("Fall or fly-into-wall damage cancelled for player: " + player.getName());
                }
            }
        }
    }
}