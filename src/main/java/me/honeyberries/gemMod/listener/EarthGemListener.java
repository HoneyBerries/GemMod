package me.honeyberries.gemMod.listener;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.CooldownManager;
import me.honeyberries.gemMod.manager.GemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import me.honeyberries.gemMod.manager.GemManager.GemType;

/**
 * Listener for Earth Gem effects.
 * Cancels damage to players holding an active Earth Gem if the cooldown is above a threshold.
 */
public class EarthGemListener implements Listener {

    /** Reference to the main plugin instance */
    private final GemMod plugin = GemMod.getInstance();

    /** Reference to the cooldown manager handling gem cooldowns */
    private final CooldownManager cooldownManager = CooldownManager.getInstance();

    /**
     * Handles the EntityDamageEvent for players holding the Earth Gem.
     * Cancels damage if the player is holding the Earth Gem and its cooldown is active.
     *
     * @param event The EntityDamageEvent triggered when an entity takes damage.
     */
    @EventHandler()
    public void onEarthGemUse(EntityDamageEvent event) {
        // Check if the damaged entity is a player
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Only check for Earth gem effects if the player has the gem and it's active
        if (!GemManager.isHoldingGem(player, GemType.EARTH)) {
            return;
        }

        // Get the remaining cooldown for the Earth Gem
        long remainingCooldown = cooldownManager.getRemainingCooldown(player, GemType.EARTH);
        if (remainingCooldown > 60_000) {
            // Cancel damage while Earth gem protection is active
            event.setCancelled(true);
            plugin.getLogger().info("Earth gem protection applied to " + player.getName());
        }
    }
}
