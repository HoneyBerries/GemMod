package me.honeyberries.gemMod.listener;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.configuration.GemModData;
import me.honeyberries.gemMod.manager.GemManager;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import me.honeyberries.gemMod.recipe.GemRecipe;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

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


        // Log the crafting event and send a broadcast message to all players
        plugin.getLogger().info(gemType.name() + " gem crafted and recipe removed.");

        String aCase = gemType.name().substring(1).toLowerCase();

        Title title = getTitle(gemType, aCase);

        Bukkit.getOnlinePlayers().forEach(p -> {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.75f, 1.0f);
            p.showTitle(title);
        });

        plugin.getServer().broadcast(
            Component.text(
                    gemType.name().charAt(0) + aCase,
                    NamedTextColor.AQUA
                )
                .append(Component.text(" Gem ", NamedTextColor.AQUA))
                .append(Component.text("is now crafted! ", NamedTextColor.GREEN))
                .append(Component.text("It cannot be crafted again!", NamedTextColor.RED))
        );
    }

    /**
     * Constructs a Title object for the gem crafting notification.
     *
     * @param gemType The type of gem that was crafted
     * @param aCase   The case to be used in the title (e.g., "Air", "Darkness")
     * @return A Title object with the appropriate text and timings
     */
    private static @NotNull Title getTitle(GemType gemType, String aCase) {
        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofSeconds(2),
                Duration.ofMillis(500)
        );
        return Title.title(
            Component.text(
                gemType.name().charAt(0) + aCase + " Gem ",
                NamedTextColor.AQUA
            ).append(Component.text("crafted!", NamedTextColor.GREEN)),
            Component.text("It cannot be crafted again!", NamedTextColor.RED),
            times
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
