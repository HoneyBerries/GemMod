package me.honeyberries.gemMod.listener;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import me.honeyberries.gemMod.manager.GemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Listener for Air Gem effects.
 * Cancels fall damage for players carrying the Air Gem.
 */
public class AirGemListener implements Listener {

    /** Reference to the main plugin instance. */
    private final GemMod plugin = GemMod.getInstance();

    /**
     * Handles player damage events.
     * Cancels fall damage if the player has the Air Gem in their inventory.
     *
     * @param event The EntityDamageEvent triggered when an entity takes damage.
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // Check if the damaged entity is a player
        if (event.getEntity() instanceof Player player) {
            // Check if the player has the Air Gem in their inventory
            if (GemManager.hasGem(player, GemType.AIR)) {
                // Only cancel damage if the cause is falling
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL || event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL) {
                    event.setCancelled(true); // Cancel the fall damage
                    plugin.getLogger().info("Fall damage cancelled for player: " + player.getName());
                }
            }
        }
    }
}
