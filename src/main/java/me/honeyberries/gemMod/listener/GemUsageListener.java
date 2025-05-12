package me.honeyberries.gemMod.listener;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.AbilityManager;
import me.honeyberries.gemMod.manager.CooldownManager;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import me.honeyberries.gemMod.manager.GemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.Event.Result;

import java.util.logging.Level;

/**
 * Listener that handles gem usage by players.
 * Processes right-click actions to trigger the associated gem ability.
 */
public class GemUsageListener implements Listener {

    // Reference to the main plugin instance
    private final GemMod plugin = GemMod.getInstance();

    // Reference to the cooldown manager handling gem cooldowns
    private final CooldownManager cooldownManager = CooldownManager.getInstance();

    /**
     * Processes player interactions to detect gem usage.
     *
     * @param event the player interaction event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onGemUse(PlayerInteractEvent event) {

        // Only proceed on right-click actions (air or block)
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Retrieve player and the item in their main hand (potential gem).
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
            default:
                plugin.getLogger().log(Level.SEVERE, "Unknown gem type: " + mainHandGem);
                player.sendMessage("Unknown gem type!");
        }
    }
}
