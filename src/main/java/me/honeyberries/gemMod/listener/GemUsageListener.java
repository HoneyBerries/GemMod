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
 * Handles the activation of gem abilities upon player interaction.
 *
 * This listener detects right-click actions with a gem in the main hand,
 * cancels the default interaction, and triggers the corresponding ability.
 * It also prevents gem usage from the off-hand to avoid conflicts.
 *
 * @author HoneyBerries
 * @version 1.0
 */
public class GemUsageListener implements Listener {

    /**
     * A reference to the main plugin instance for logging and other operations.
     */
    private final GemMod plugin = GemMod.getInstance();

    /**
     * A reference to the cooldown manager for handling ability cooldowns.
     */
    private final CooldownManager cooldownManager = CooldownManager.getInstance();

    /**
     * Processes player interactions to detect and trigger gem abilities.
     *
     * This method validates that the interaction is a right-click, checks for a gem
     * in the main hand, and prevents off-hand usage. If a valid gem is used,
     * it cancels the event and calls the appropriate ability handler.
     *
     * @param event The {@link PlayerInteractEvent} triggered by the player.
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
            case WATER:
                AbilityManager.handleWaterGemAbility(player);
                break;
            default:
                plugin.getLogger().log(Level.SEVERE, "Unknown gem type: " + mainHandGem);
                player.sendMessage("Unknown gem type!");
        }
    }
}