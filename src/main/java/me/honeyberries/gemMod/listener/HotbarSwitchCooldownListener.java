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
 * Displays cooldown information for gems if applicable.
 */
public class HotbarSwitchCooldownListener implements Listener {

    private final CooldownManager cooldownManager = CooldownManager.getInstance();

    /**
     * Handles hotbar slot switching to update the action bar with cooldown info.
     *
     * @param event the PlayerItemHeldEvent triggered when the player switches hotbar slots
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
     * Handles swapping items between main and off hand to update the action bar.
     *
     * @param event the PlayerSwapHandItemsEvent triggered when the player swaps hand items
     */
    @EventHandler
    public void onHandSwitch(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = event.getMainHandItem(); // Item that will be in the main hand after swap
        cooldownManager.handleHotbarSwitch(player, newItem);
    }
}