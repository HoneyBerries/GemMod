package me.honeyberries.gemMod.configuration;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import me.honeyberries.gemMod.recipe.GemRecipe;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages persistent data for the GemMod plugin, such as the crafted status of each gem.
 *
 * This class is designed to be thread-safe for use in multithreaded server environments like Folia.
 * It uses a {@link ConcurrentHashMap} for in-memory data storage and synchronizes file operations
 * to prevent race conditions and ensure data integrity.
 */
public class GemModData {

    /**
     * A reference to the main plugin instance.
     */
    private static final GemMod plugin = GemMod.getInstance();

    /**
     * The file where gem data is stored.
     */
    private static File dataFile;
    private static YamlConfiguration yamlConfig;

    /**
     * A thread-safe map that tracks whether each gem type has been crafted.
     */
    private static final Map<GemType, Boolean> gemCraftedMap = new ConcurrentHashMap<>();

    /**
     * A lock object to synchronize file I/O operations, preventing corruption in multithreaded environments.
     */
    private static final Object fileLock = new Object();

    /**
     * Loads gem data from the {@code data.yml} file into memory.
     *
     * This method initializes the data file if it doesn't exist, loads the YAML configuration,
     * populates the gem crafted status map, and updates crafting recipes accordingly.
     */
    public static void loadData() {
        synchronized (fileLock) {
            // Initialize file and config
            dataFile = new File(plugin.getDataFolder(), "data.yml");
            if (!dataFile.exists()) {
                plugin.saveResource("data.yml", false);
            }

            // Other initialization...
            yamlConfig = YamlConfiguration.loadConfiguration(dataFile);

            plugin.getLogger().info("---------- Loading gem data ----------");

            // 1. FIRST load crafted status from config
            gemCraftedMap.clear();
            for (GemType type : GemType.values()) {
                String path = "gems." + type.name().toLowerCase() + ".crafted";
                boolean crafted = yamlConfig.getBoolean(path, false);
                gemCraftedMap.put(type, crafted);
                plugin.getLogger().info(type.name() + " gem crafted: " + crafted);
            }

            // 2. THEN remove all existing recipes
            plugin.getServer().removeRecipe(GemRecipe.airGemKey);
            plugin.getServer().removeRecipe(GemRecipe.darknessGemKey);
            plugin.getServer().removeRecipe(GemRecipe.earthGemKey);
            plugin.getServer().removeRecipe(GemRecipe.fireGemKey);
            plugin.getServer().removeRecipe(GemRecipe.lightGemKey);

            // 3. FINALLY, register recipes based on loaded data
            GemRecipe.registerGemRecipes();
        }
    }

    /**
     * Sets the crafted status for a specific gem type and saves the change to the data file.
     *
     * @param type    The gem type to update.
     * @param crafted The new crafted status.
     */
    public static void setGemCrafted(GemType type, boolean crafted) {
        // Update the map (ConcurrentHashMap handles thread safety for the map operation)
        gemCraftedMap.put(type, crafted);

        synchronized (fileLock) {
            // Ensure config is loaded
            if (yamlConfig == null) {
                loadData();
                if (yamlConfig == null) {
                    plugin.getLogger().severe("Failed to load configuration for saving!");
                    return;
                }
            }

            // Save to config using a consistent path
            yamlConfig.set("gems." + type.name().toLowerCase() + ".crafted", crafted);
            saveConfig();
        }
    }

    /**
     * Saves the current configuration to the {@code data.yml} file.
     * This method is synchronized to ensure thread-safe file access.
     */
    public static void saveConfig() {
        synchronized (fileLock) {
            try {
                yamlConfig.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save configuration file: " + e.getMessage());
            }
        }
    }

    /**
     * Sets a value at a specific path in the configuration and saves the file.
     * This method is synchronized for thread safety.
     *
     * @param path  The configuration path.
     * @param value The value to set.
     */
    public static void set(@NotNull String path, @NotNull Object value) {
        synchronized (fileLock) {
            yamlConfig.set(path, value);
            saveConfig();
        }
    }

    /**
     * Returns an unmodifiable view of the gem crafted map to prevent external modification.
     *
     * @return An unmodifiable {@link Map} of the gem crafted statuses.
     */
    public static Map<GemType, Boolean> getGemCraftedMap() {
        return Collections.unmodifiableMap(gemCraftedMap);
    }

    /**
     * Updates the gem crafted map with a new set of values and saves the changes.
     *
     * @param newGemCraftedMap A map containing the new crafted statuses for each gem type.
     */
    public static void setGemCraftedMap(Map<GemType, Boolean> newGemCraftedMap) {
        // Clear and update the map instead of reassigning it
        gemCraftedMap.clear();
        gemCraftedMap.putAll(newGemCraftedMap);

        // Save all changes to the configuration file
        synchronized (fileLock) {
            for (Map.Entry<GemType, Boolean> entry : newGemCraftedMap.entrySet()) {
                String path = "gems." + entry.getKey().name().toLowerCase() + ".crafted";
                yamlConfig.set(path, entry.getValue());
            }
            saveConfig();
        }
    }
}