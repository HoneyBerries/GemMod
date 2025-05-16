package me.honeyberries.gemMod.configuration;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import me.honeyberries.gemMod.recipe.GemRecipe;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GemModData {

    // Reference to the main plugin instance
    private static final GemMod plugin = GemMod.getInstance();

    // Data file for storing gem mod data
    private static File dataFile;
    private static YamlConfiguration yamlConfig;
    private static Map<GemType, Boolean> gemCraftedMap = new HashMap<>();


    public static void loadData() {
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

        // 3. FINALLY register recipes based on loaded data
        GemRecipe.registerGemRecipes();
    }

    /**
     * Sets the gem-crafted status in the configuration.
     *
     * @param type    the gem type
     * @param crafted the crafted status
     */
    public static void setGemCrafted(GemType type, boolean crafted) {
        // Update the map
        gemCraftedMap.put(type, crafted);

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


    /**
     * Saves the current configuration to the data.yml file.
     * If the save fails, a warning message is logged.
     */
    public static void saveConfig() {
        try {
            yamlConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Failed to save configuration file.");
        }
    }

    /**
     * Sets a value in the configuration and saves the updated configuration file.
     *
     * @param path  the configuration path
     * @param value the value to set
     */
    public static void set(@NotNull String path, @NotNull Object value) {
        yamlConfig.set(path, value);
        saveConfig();
    }

    /**
     * Gets the value of a specific gem type from the configuration.
     *
     * @return the value of the specified gem type
     */
    public static Map<GemType, Boolean> getGemCraftedMap() {
        return gemCraftedMap;
    }


    /**
     * Sets the value of a specific gem type in the configuration and saves the updated configuration file.
     *
     * @param gemCraftedMap the value to set
     */
    public static void setGemCraftedMap(Map<GemType, Boolean> gemCraftedMap) {
        GemModData.gemCraftedMap = gemCraftedMap;
    }


}
