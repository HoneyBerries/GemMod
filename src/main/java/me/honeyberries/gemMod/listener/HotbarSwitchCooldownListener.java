package me.honeyberries.gemMod.listener;

import me.honeyberries.gemMod.manager.CooldownManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for updating the action bar when a player switches hotbar slots or swaps hands.
 *
 * <p>This listener is responsible for:</p>
 * <ul>
 *   <li>Detecting hotbar slot changes</li>
 *   <li>Detecting hand item swaps</li>
 *   <li>Displaying cooldown information for gems in the action bar</li>
 * </ul>
 *
 * <p>It integrates with the {@link CooldownManager} to retrieve and display cooldown data.</p>
 *
 * @author HoneyBerries
 * @since 1.0
 */
public class HotbarSwitchCooldownListener implements Listener {

    /** Reference to the cooldown manager for handling gem cooldowns. */
    private final CooldownManager cooldownManager = CooldownManager.getInstance();

    /**
     * Handles hotbar slot switching to update the action bar with cooldown information.
     *
     * <p>This method:</p>
     * <ol>
     *   <li>Detects when a player switches to a new hotbar slot</li>
     *   <li>Retrieves the item in the new slot</li>
     *   <li>Updates the action bar with cooldown information if the item is a gem</li>
     * </ol>
     *
     * @param event The {@link PlayerItemHeldEvent} triggered when the player switches hotbar slots
     */
    @EventHandler
    public void onHotbarSwitch(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (newItem != null) {
            cooldownManager.handleHotbarSwitch(player, newItem);
        }
    }

    /**
     * Handles swapping items between the main and off hand to update the action bar.
     *
     * <p>This method:</p>
     * <ol>
     *   <li>Detects when a player swaps items between their main and off hand</li>
     *   <li>Retrieves the item that will be in the main hand after the swap</li>
     *   <li>Updates the action bar with cooldown information if the item is a gem</li>
     * </ol>
     *
     * @param event The {@link PlayerSwapHandItemsEvent} triggered when the player swaps hand items
     */
    @EventHandler
    public void onHandSwitch(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = event.getMainHandItem(); // Item that will be in the main hand after the swap
        cooldownManager.handleHotbarSwitch(player, newItem);
    }
}