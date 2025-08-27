package me.honeyberries.gemMod.listener;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.AbilityManager;
import me.honeyberries.gemMod.manager.CooldownManager;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import me.honeyberries.gemMod.manager.GemManager;
import me.honeyberries.gemMod.util.LogUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.Event.Result;

/**
 * Handles the activation of gem abilities upon player interaction.
 * <p>
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
        // We only care about right-clicks for activating gems.
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        GemType gemType = GemManager.identifyGemType(mainHandItem);

        // If the item in the main hand is not a gem, we don't need to do anything.
        if (gemType == null) {
            return;
        }

        // If a gem is in the main hand, we cancel the event entirely.
        // This prevents the default use action of the gem itself and also blocks any off-hand item usage.
        event.setUseItemInHand(Result.DENY);
        event.setUseInteractedBlock(Result.DENY);
        event.setCancelled(true);

        // We only trigger the ability if the interaction was with the main hand.
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        LogUtil.verbose("Player " + player.getName() + " used " + gemType.name() + " gem");

        // Execute the ability associated with the identified gem type.
        switch (gemType) {
            case AIR -> AbilityManager.handleAirGemAbility(player);
            case DARKNESS -> AbilityManager.handleDarknessGemAbility(player);
            case EARTH -> AbilityManager.handleEarthGemAbility(player);
            case FIRE -> AbilityManager.handleFireGemAbility(player);
            case LIGHT -> AbilityManager.handleLightGemAbility(player);
            case WATER -> AbilityManager.handleWaterGemAbility(player);
            default -> {
                LogUtil.severe("Unknown gem type: " + gemType);
                player.sendMessage("Unknown gem type!");
            }
        }
    }
}