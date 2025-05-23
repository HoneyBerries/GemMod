package me.honeyberries.gemMod.recipe;

import me.honeyberries.gemMod.configuration.GemModData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import me.honeyberries.gemMod.manager.GemManager;
import me.honeyberries.gemMod.manager.GemManager.*;
import java.util.logging.Logger;
import me.honeyberries.gemMod.GemMod;
import org.bukkit.inventory.ShapedRecipe;

/**
 * Manages crafting recipes for the various gem items in the GemMod plugin.
 * <p>
 * This class is responsible for:
 * - Defining the crafting recipes for each gem type (Air, Darkness, Earth, Fire, Light)
 * - Registering these recipes with the Bukkit server
 * - Providing utility methods for recipe-related operations
 * <p>
 * Each gem has a unique crafting recipe that players can use to create gems in-game.
 * The recipes are designed to be balanced and require appropriate materials that match
 * the theme of each gem type.
 */
public class GemRecipe {

    /** Reference to the main plugin instance */
    private static final GemMod plugin = GemMod.getInstance();

    /** Logger for recording events related to gem recipes */
    private static final Logger logger = plugin.getLogger();

    /** Namespaced keys for the gem crafting recipes */
    public static NamespacedKey airGemKey = new NamespacedKey(plugin, "air_gem");
    public static NamespacedKey darknessGemKey = new NamespacedKey(plugin, "darkness_gem");
    public static NamespacedKey earthGemKey = new NamespacedKey(plugin, "earth_gem");
    public static NamespacedKey fireGemKey = new NamespacedKey(plugin, "fire_gem");
    public static NamespacedKey lightGemKey = new NamespacedKey(plugin, "light_gem");

    /**
     * Registers all gem crafting recipes with the server.
     * <p>
     * This method should be called during plugin initialization to make
     * all gem recipes available to players.
     */
    public static void registerGemRecipes() {

        // Only register recipes for gems that haven't been crafted yet
        for (GemType gemType : GemType.values()) {
            if (!GemModData.getGemCraftedMap().getOrDefault(gemType, false)) {
                switch (gemType) {
                    case AIR -> registerAirGemRecipe();
                    case DARKNESS -> registerDarknessGemRecipe();
                    case EARTH -> registerEarthGemRecipe();
                    case FIRE -> registerFireGemRecipe();
                    case LIGHT -> registerLightGemRecipe();
                }
            }
        }

        logger.info("Gem crafting recipes registered based on crafted status");
    }

    /**
     * Registers the crafting recipe for the Air Gem.
     * <p>
     * The recipe uses feathers, breeze rods, and a heavy core.
     * It removes any existing recipe with the same key before registering.
     */
    private static void registerAirGemRecipe() {
        // Remove the old recipe if it exists
        plugin.getServer().removeRecipe(airGemKey);

        // Create a new recipe for the Air Gem
        ShapedRecipe airGemRecipe = new ShapedRecipe(airGemKey, GemManager.createGem(GemType.AIR, 1));
        airGemRecipe.shape("FRF", "RCR", "FRF");

        // Set the ingredients for the Air Gem recipe
        airGemRecipe.setIngredient('F', Material.FEATHER);
        airGemRecipe.setIngredient('R', Material.BREEZE_ROD);
        airGemRecipe.setIngredient('C', Material.HEAVY_CORE);

        // Register the Air Gem recipe with the server
        plugin.getServer().addRecipe(airGemRecipe);
    }

    /**
     * Registers the crafting recipe for the Darkness Gem.
     * <p>
     * The recipe uses echo shards, soul lanterns, crying obsidian, a nether star, and soul torches.
     * It removes any existing recipe with the same key before registering.
     */
    private static void registerDarknessGemRecipe() {
        // Remove the old recipe if it exists
        plugin.getServer().removeRecipe(darknessGemKey);

        // Create a new recipe for the Darkness Gem
        ShapedRecipe darknessGemRecipe = new ShapedRecipe(darknessGemKey,
                GemManager.createGem(GemType.DARKNESS, 1));

        darknessGemRecipe.shape("SLS", "BNB", "STS");

        // Set the ingredients for the Darkness Gem recipe
        darknessGemRecipe.setIngredient('S', Material.ECHO_SHARD);
        darknessGemRecipe.setIngredient('L', Material.SOUL_LANTERN);
        darknessGemRecipe.setIngredient('B', Material.CRYING_OBSIDIAN);
        darknessGemRecipe.setIngredient('N', Material.NETHER_STAR);
        darknessGemRecipe.setIngredient('T', Material.SOUL_TORCH);

        // Register the Darkness Gem recipe with the server
        plugin.getServer().addRecipe(darknessGemRecipe);
    }

    /**
     * Registers the crafting recipe for the Earth Gem.
     * <p>
     * The recipe uses emerald blocks, gold blocks, redstone blocks, diamond blocks, and a nether star.
     * It removes any existing recipe with the same key before registering.
     */
    private static void registerEarthGemRecipe() {
        // Remove the old recipe if it exists
        plugin.getServer().removeRecipe(earthGemKey);

        // Create a new recipe for the Earth Gem
        ShapedRecipe earthGemRecipe = new ShapedRecipe(earthGemKey, GemManager.createGem(GemType.EARTH, 1));
        earthGemRecipe.shape("EGR", "DND", "RGE");

        // Set the ingredients for the Earth Gem recipe
        earthGemRecipe.setIngredient('E', Material.EMERALD_BLOCK);
        earthGemRecipe.setIngredient('G', Material.GOLD_BLOCK);
        earthGemRecipe.setIngredient('R', Material.REDSTONE_BLOCK);
        earthGemRecipe.setIngredient('D', Material.DIAMOND_BLOCK);
        earthGemRecipe.setIngredient('N', Material.NETHER_STAR);

        // Register the Earth Gem recipe with the server
        plugin.getServer().addRecipe(earthGemRecipe);
    }

    /**
     * Registers the crafting recipe for the Fire Gem.
     * <p>
     * The recipe uses fire charges, blaze rods, and a nether star.
     * It removes any existing recipe with the same key before registering.
     */
    private static void registerFireGemRecipe() {
        // Remove the old recipe if it exists
        plugin.getServer().removeRecipe(fireGemKey);

        // Create a new recipe for the Fire Gem
        ShapedRecipe fireGemRecipe = new ShapedRecipe(fireGemKey, GemManager.createGem(GemType.FIRE, 1));
        fireGemRecipe.shape("FRF", "RNR", "FRF");

        // Set the ingredients for the Fire Gem recipe
        fireGemRecipe.setIngredient('F', Material.FIRE_CHARGE);
        fireGemRecipe.setIngredient('R', Material.BLAZE_ROD);
        fireGemRecipe.setIngredient('N', Material.NETHER_STAR);

        // Register the Fire Gem recipe with the server
        plugin.getServer().addRecipe(fireGemRecipe);
    }

    /**
     * Registers the crafting recipe for the Light Gem.
     * <p>
     * The recipe uses lightning rods, glowstone, diamond blocks, redstone blocks, and end rods.
     * It removes any existing recipe with the same key before registering.
     */
    private static void registerLightGemRecipe() {
        // Remove the old recipe if it exists
        plugin.getServer().removeRecipe(lightGemKey);

        // Create a new recipe for the Light Gem
        ShapedRecipe lightGemRecipe = new ShapedRecipe(lightGemKey, GemManager.createGem(GemType.LIGHT, 1));
        lightGemRecipe.shape("#/#", "GTG", "#R#");

        // Set the ingredients for the Light Gem recipe
        lightGemRecipe.setIngredient('#', Material.LIGHTNING_ROD);
        lightGemRecipe.setIngredient('/', Material.DIAMOND_BLOCK);
        lightGemRecipe.setIngredient('G', Material.GOLD_BLOCK);
        lightGemRecipe.setIngredient('T', Material.TRIDENT);
        lightGemRecipe.setIngredient('R', Material.REDSTONE_BLOCK);

        // Register the Light Gem recipe with the server
        plugin.getServer().addRecipe(lightGemRecipe);
    }
}