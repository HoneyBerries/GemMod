package me.honeyberries.gemMod.listener;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.configuration.GemModData;
import me.honeyberries.gemMod.manager.GemManager;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import me.honeyberries.gemMod.recipe.GemRecipe;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener class for handling gem crafting events in the GemMod plugin.
 * <p>
 * This class listens for crafting events and performs the following actions:
 * - Identifies if the crafted item is a gem.
 * - Updates the gem-crafted status in the plugin's configuration.
 * - Logs the crafting event and broadcasts a message to all players.
 * <p>
 * This ensures that each gem can only be crafted once and notifies players
 * when a gem is successfully crafted.
 */
public class GemCraftListener implements Listener {

    /** Reference to the main plugin instance */
    private static final GemMod plugin = GemMod.getInstance();

    /**
     * Handles the CraftItemEvent to detect and process gem crafting.
     * <p>
     * This method performs the following steps:
     * - Checks if the crafted item is a gem.
     * - Updates the gem-crafted status in the configuration.
     * - Logs the crafting event and broadcasts a message to all players.
     * <p>
     * If the crafted item is not a gem, the method exits without performing any actions.
     *
     * @param event The CraftItemEvent triggered when an item is crafted.
     */
    @EventHandler
    public void onGemCraft(CraftItemEvent event) {
        // Get the item that was crafted
        ItemStack craftedItem = event.getCurrentItem();

        // Identify the type of gem, if applicable
        GemType gemType = GemManager.identifyGemType(craftedItem);

        // If the crafted item is not a gem, exit the method
        if (gemType == null) {
            return;
        }

        // Update the gem-crafted status in the configuration
        GemModData.setGemCrafted(gemType, true);

        // Remove the recipe to prevent future crafting
        NamespacedKey recipeKey = getRecipeKeyForGemType(gemType);
        if (recipeKey != null) {
            plugin.getServer().removeRecipe(recipeKey);
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.75f, 1.0f);
        }

        // Log the crafting event and send a broadcast message to all players
        plugin.getLogger().info(gemType.name() + " gem crafted and recipe removed.");
        plugin.getServer().broadcast(
            Component.text("The ", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text(
                    gemType.name().charAt(0) + gemType.name().substring(1).toLowerCase(),
                    NamedTextColor.AQUA
                ))
                .append(Component.text(" Gem ", NamedTextColor.AQUA))
                .append(Component.text("has been crafted! ", NamedTextColor.GREEN))
                .append(Component.text("It cannot be crafted again!", NamedTextColor.RED))
        );
    }

    /**
     * Gets the NamespacedKey for a given gem type.
     *
     * @param gemType The type of gem
     * @return The NamespacedKey for the gem's recipe, or null if not found
     */
    private NamespacedKey getRecipeKeyForGemType(GemType gemType) {
        return switch (gemType) {
            case AIR -> GemRecipe.airGemKey;
            case DARKNESS -> GemRecipe.darknessGemKey;
            case EARTH -> GemRecipe.earthGemKey;
            case FIRE -> GemRecipe.fireGemKey;
            case LIGHT -> GemRecipe.lightGemKey;
            default -> null;
        };
    }
}
