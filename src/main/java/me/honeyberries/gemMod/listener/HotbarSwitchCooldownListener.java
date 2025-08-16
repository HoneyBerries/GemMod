package me.honeyberries.gemMod.listener;

import me.honeyberries.gemMod.manager.CooldownManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles UI updates related to gem cooldowns when a player switches items.
 *
 * This listener monitors hotbar slot changes and hand swaps to display
 * timely cooldown information in the action bar, ensuring the player is
 * always aware of their abilities' status.
 *
 * @author HoneyBerries
 * @version 1.0
 */
public class HotbarSwitchCooldownListener implements Listener {

    /**
     * A reference to the cooldown manager for handling ability cooldowns.
     */
    private final CooldownManager cooldownManager = CooldownManager.getInstance();

    /**
     * Updates the action bar with cooldown information when the player switches hotbar slots.
     *
     * When a player selects a new item in their hotbar, this method checks if the item is
     * a gem and, if so, updates the action bar to display its current cooldown status.
     *
     * @param event The {@link PlayerItemHeldEvent} triggered upon hotbar slot change.
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
     * Updates the action bar with cooldown information when the player swaps items between hands.
     *
     * When a player swaps items, this method checks the new item in the main hand and updates
     * the action bar with its cooldown status if it is a gem.
     *
     * @param event The {@link PlayerSwapHandItemsEvent} triggered upon hand swap.
     */
    @EventHandler
    public void onHandSwitch(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = event.getMainHandItem(); // Item that will be in the main hand after the swap
        cooldownManager.handleHotbarSwitch(player, newItem);
    }
}