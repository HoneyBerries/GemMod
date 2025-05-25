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
 * Manages persistent data storage for the GemMod plugin.
 * <p>
 * This class is thread-safe and designed to work in multithreaded environments like Folia servers.
 * It uses ConcurrentHashMap for in-memory storage and synchronization for file operations
 * to prevent concurrency issues when multiple threads access or modify the data simultaneously.
 * <p>
 * The data is stored in data.yml and includes information about which gems have been crafted.
 */
public class GemModData {

    // Reference to the main plugin instance
    private static final GemMod plugin = GemMod.getInstance();

    // Data file for storing gem mod data
    private static File dataFile;
    private static YamlConfiguration yamlConfig;

    /**
     * Thread-safe map to track which gems have been crafted.
     * ConcurrentHashMap provides thread safety for map operations without needing explicit synchronization.
     */
    private static final Map<GemType, Boolean> gemCraftedMap = new ConcurrentHashMap<>();

    /**
     * Lock object for synchronizing file operations.
     * This ensures that only one thread at a time can read from or write to the data.yml file,
     * preventing file corruption or inconsistent states in a multithreaded environment.
     */
    private static final Object fileLock = new Object();


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
     * Sets the gem-crafted status in the configuration.
     *
     * @param type    the gem type
     * @param crafted the crafted status
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
     * Saves the current configuration to the data.yml file.
     * If the save fails, a warning message is logged.
     * This method is synchronized to prevent concurrent file access.
     */
    public static void saveConfig() {
        synchronized (fileLock) {
            try {
                yamlConfig.save(dataFile);
            } catch (IOException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to save configuration file.");
            }
        }
    }

    /**
     * Sets a value in the configuration and saves the updated configuration file.
     * This method is synchronized to prevent concurrent file access.
     *
     * @param path  the configuration path
     * @param value the value to set
     */
    public static void set(@NotNull String path, @NotNull Object value) {
        synchronized (fileLock) {
            yamlConfig.set(path, value);
            saveConfig();
        }
    }

    /**
     * Gets an unmodifiable view of the gem crafted map to prevent external modification.
     *
     * @return an unmodifiable view of the gem crafted map
     */
    public static Map<GemType, Boolean> getGemCraftedMap() {
        return Collections.unmodifiableMap(gemCraftedMap);
    }

    /**
     * Updates the gem crafted map with values from the provided map.
     * This method is thread-safe as it uses ConcurrentHashMap's thread-safe operations.
     *
     * @param newGemCraftedMap the map containing new values to set
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
