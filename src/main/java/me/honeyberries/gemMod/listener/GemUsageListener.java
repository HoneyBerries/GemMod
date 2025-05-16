package me.honeyberries.gemMod.listener;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.AbilityManager;
import me.honeyberries.gemMod.manager.CooldownManager;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import me.honeyberries.gemMod.manager.GemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.Event.Result;

import java.util.logging.Level;

/**
 * Listener that handles gem usage and activation of gem abilities.
 *
 * <p>This listener is responsible for:</p>
 * <ul>
 *   <li>Detecting right-click actions with gems</li>
 *   <li>Preventing off-hand gem usage</li>
 *   <li>Triggering the appropriate gem ability</li>
 *   <li>Managing interaction cancellation</li>
 * </ul>
 *
 * <p>The listener supports all gem types:</p>
 * <ul>
 *   <li><b>Air Gem:</b> Double jump ability</li>
 *   <li><b>Darkness Gem:</b> Invisibility</li>
 *   <li><b>Earth Gem:</b> Resistance boost</li>
 *   <li><b>Fire Gem:</b> Fireball projectile</li>
 *   <li><b>Light Gem:</b> Lightning strike</li>
 * </ul>
 *
 * @author HoneyBerries
 * @since 1.0
 */
public class GemUsageListener implements Listener {

    /** Reference to the main plugin instance for logging and plugin operations. */
    private final GemMod plugin = GemMod.getInstance();

    /** Reference to the cooldown manager for handling gem ability cooldowns. */
    private final CooldownManager cooldownManager = CooldownManager.getInstance();

    /**
     * Processes player interactions to detect and handle gem usage.
     *
     * <p>This method:</p>
     * <ol>
     *   <li>Validates the interaction type (right-click only)</li>
     *   <li>Checks for gems in the player's hands</li>
     *   <li>Prevents off-hand gem usage</li>
     *   <li>Cancels default interaction behavior</li>
     *   <li>Triggers the appropriate gem ability</li>
     * </ol>
     *
     * @param event The PlayerInteractEvent triggered by player interaction
     */
    @EventHandler
    public void onGemUse(PlayerInteractEvent event) {
        // Only proceed with right-click actions (air or block)
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Retrieve the player and the item in their main hand (potential gem).
        Player player = event.getPlayer();
        ItemStack mainItem = player.getInventory().getItemInMainHand();
        GemType mainHandGem = GemManager.identifyGemType(mainItem);

        // Handle off-hand interactions: if main hand holds a gem, block off-hand use.
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            if (mainHandGem != null) {
                event.setCancelled(true);                    // Cancel event to prevent conflicts
                event.setUseItemInHand(Result.DENY);         // Deny off-hand usage
                event.setUseInteractedBlock(Result.DENY);    // Deny block interaction
            }
            return; // Ensure abilities are only processed from the main hand
        }

        // If there is no gem in the main hand, exit early.
        if (mainHandGem == null) {
            return;
        }

        // Cancel further processing of the default item use.
        event.setCancelled(true);
        event.setUseItemInHand(Result.DENY);
        event.setUseInteractedBlock(Result.DENY);

        // Execute the ability associated with the identified gem type.
        switch (mainHandGem) {
            case AIR:
                AbilityManager.handleAirGemAbility(player);
                break;
            case DARKNESS:
                AbilityManager.handleDarknessGemAbility(player);
                break;
            case EARTH:
                AbilityManager.handleEarthGemAbility(player);
                break;
            case FIRE:
                AbilityManager.handleFireGemAbility(player);
                break;
            case LIGHT:
                AbilityManager.handleLightGemAbility(player);
                break;
            default:
                plugin.getLogger().log(Level.SEVERE, "Unknown gem type: " + mainHandGem);
                player.sendMessage("Unknown gem type!");
        }
    }
}