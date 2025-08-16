package me.honeyberries.gemMod.listener;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import me.honeyberries.gemMod.manager.GemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Handles events related to the Air Gem's passive effects.
 *
 * This listener is responsible for mitigating damage taken by players who possess an Air Gem.
 * Specifically, it cancels damage from falling and flying into walls, enhancing the user's mobility and safety.
 *
 * @author HoneyBerries
 * @version 1.0
 */
public class AirGemListener implements Listener {

    /**
     * A reference to the main plugin instance, used for logging and other plugin-related operations.
     */
    private final GemMod plugin = GemMod.getInstance();

    /**
     * Prevents fall and fly-into-wall damage for players holding an Air Gem.
     *
     * This event handler checks if the damaged entity is a player and if they have an Air Gem
     * in their inventory. If both conditions are met, and the damage cause is either {@code FALL} or
     * {@code FLY_INTO_WALL}, the event is cancelled.
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