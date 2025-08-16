package me.honeyberries.gemMod;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.settings.PacketEventsSettings;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.honeyberries.gemMod.command.GemCommand;
import me.honeyberries.gemMod.command.GemModCommand;
import me.honeyberries.gemMod.configuration.GemModData;
import me.honeyberries.gemMod.listener.*;
import me.honeyberries.gemMod.task.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Main class for the GemMod plugin.
 * <p>
 * This plugin introduces a variety of magical gems into Minecraft, each granting unique abilities to players.
 * It handles the plugin's lifecycle, including command registration, event listeners, and task scheduling.
 * <p>
 * The available gems and their features include:
 * <ul>
 *   <li><b>Air Gem:</b> Provides a powerful velocity boost, allowing for a double jump-like ability.</li>
 *   <li><b>Darkness Gem:</b> Grants temporary invisibility and conceals the player's armor and held items.</li>
 *   <li><b>Earth Gem:</b> Bestows temporary invulnerability by applying a strong resistance effect.</li>
 *   <li><b>Fire Gem:</b> Allows the player to launch destructive fireball projectiles.</li>
 *   <li><b>Light Gem:</b> Can strike a targeted player with lightning and cause other nearby entities to glow.</li>
 * </ul>
 *
 * This class is responsible for the core initialization and management of the plugin's components.
 *
 * @author HoneyBerries
 * @version 1.0
 */
public final class GemMod extends JavaPlugin {

    /**
     * A map to track the enabled status of various features.
     * This allows for dynamic feature management and debugging.
     */
    private final Map<String, Boolean> enabledFeatures = new ConcurrentHashMap<>();
    private String resourcePackSha1;
    private static final String RESOURCE_PACK_URL = "https://honeyberries.net/data/gemmodassets.zip";

    /**
     * Computes the SHA-1 hash of the resource pack at the given URL.
     */
    private String computeResourcePackSha1() {
        try (InputStream in = URI.create(RESOURCE_PACK_URL).toURL().openStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] sha1Bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : sha1Bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            getLogger().severe("Failed to compute SHA-1 for resource pack: " + e.getMessage());
            return null;
        }
    }

    public String getResourcePackSha1() {
        return resourcePackSha1;
    }

    /**
     * Called when the plugin is loaded.
     *
     * Initializes the PacketEvents API and prepares it for use during the plugin's lifecycle.
     */
    @Override
    public void onLoad() {
        try {
            // Initialize the PacketEvents API
            getLogger().info("Initializing PacketEvents API...");
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEventsSettings settings = PacketEvents.getAPI().getSettings();

            settings.checkForUpdates(false); // Disable update checks for stability

            // Load the PacketEvents API
            PacketEvents.getAPI().load();
            getLogger().info("PacketEvents API loaded successfully pre-enable");
            setFeatureEnabled("packetEvents", true);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to load PacketEvents API", e);
            setFeatureEnabled("packetEvents", false);
        }
    }

    /**
     * Called when the plugin is enabled.
     * <p>
     * Initializes the plugin by loading configuration, registering commands, event listeners,
     * and scheduling recurring tasks.
     */
    @Override
    public void onEnable() {
        getLogger().info("\n---------- GemMod Enabled ----------\n");

        // Compute SHA-1 for resource pack
        resourcePackSha1 = computeResourcePackSha1();
        if (resourcePackSha1 != null) {
            getLogger().info("Resource pack SHA-1: " + resourcePackSha1);
        } else {
            getLogger().warning("Resource pack SHA-1 could not be determined. Resource pack enforcement may fail.");
        }

        // Load configuration data
        try {
            getLogger().info("Loading plugin data...");
            GemModData.loadData();
            getLogger().info("Configuration files loaded successfully");
            setFeatureEnabled("configuration", true);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to load configuration data", e);
            setFeatureEnabled("configuration", false);
        }

        // Initialize PacketEvents API if previously loaded
        if (isFeatureEnabled("packetEvents")) {
            try {
                PacketEvents.getAPI().init();
                getLogger().info("PacketEvents API ready");
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to initialize PacketEvents API", e);
                setFeatureEnabled("packetEvents", false);
            }
        }

        // Register commands, event listeners, and tasks
        getLogger().info("Registering commands, event listeners, and tasks...");
        registerCommands();
        registerEventListeners();
        scheduleTasks();

        // Log which features were successfully enabled
        logEnabledFeatures();

        getLogger().info("\n\n---------- GemMod Initialization Complete ----------\n\n");
    }

    /**
     * Registers all plugin commands.
     *
     * This method sets up the command executors for the following commands:
     * <ul>
     *   <li><b>/gem:</b> The primary command for all gem-related operations.</li>
     *   <li><b>/gemmod:</b> A command for managing the plugin's settings and features.</li>
     * </ul>
     */
    private void registerCommands() {
        getLogger().info("Registering commands...");
        try {
            // Register the /gemmod command
            getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                    commands -> {
            commands.registrar().register(GemModCommand.getBuildCommand());
            });
            getLogger().info("Registered /gemmod command");
            setFeatureEnabled("gemmodCommand", true);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register /gemmod command", e);
            setFeatureEnabled("gemmodCommand", false);
        }

        try {
            // Register the /gem command with the GemCommand executor
            getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
commands -> {
                commands.registrar().register(GemCommand.getBuildCommand());
            });
            getLogger().info("Registered /gem command");
            setFeatureEnabled("gemCommand", true);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register /gem command", e);
            setFeatureEnabled("gemCommand", false);
        }
    }

    /**
     * Registers all event listeners for the plugin.
     * <p>
     * This includes listeners for general gameplay events, such as gem usage and cooldowns,
     * as well as specific listeners for each gem's unique abilities.
     */
    private void registerEventListeners() {
        // Register general listeners
        getLogger().info("Registering general event listeners...");
        try {
            getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
            setFeatureEnabled("resourcepack", true);
            getLogger().info("Registered resource pack download listener (PlayerJoinListener)");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register resource pack download listener", e);
            setFeatureEnabled("resourcepack", false);
        }

        try {
            getServer().getPluginManager().registerEvents(new HotbarSwitchCooldownListener(), this);
            setFeatureEnabled("hotbarCooldown", true);
            getLogger().info("Registered HotbarSwitchCooldownListener");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register HotbarSwitchCooldownListener", e);
            setFeatureEnabled("hotbarCooldown", false);
        }

        try {
            getServer().getPluginManager().registerEvents(new GemUsageListener(), this);
            setFeatureEnabled("gemUsage", true);
            getLogger().info("Registered GemUsageListener");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register GemUsageListener", e);
            setFeatureEnabled("gemUsage", false);
        }

        // Register gem-specific listeners
        getLogger().info("Registering gem-specific event listeners...");

        try {
            getServer().getPluginManager().registerEvents(new AirGemListener(), this);
            setFeatureEnabled("airGem", true);
            getLogger().info("Registered AirGemListener");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register AirGemListener", e);
            setFeatureEnabled("airGem", false);
        }

        try {
            getServer().getPluginManager().registerEvents(new DarknessGemListener(), this);
            setFeatureEnabled("darknessGem", true);
            getLogger().info("Registered DarknessGemListener");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register DarknessGemListener", e);
            setFeatureEnabled("darknessGem", false);
        }

        // Register Gem Crafting Listener
        try {
            getLogger().info("Registering gem crafting event listener...");
            getServer().getPluginManager().registerEvents(new GemCraftListener(), this);
            setFeatureEnabled("gemCrafting", true);
            getLogger().info("Registered GemCraftListener");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register GemCraftListener", e);
            setFeatureEnabled("gemCrafting", false);
        }

        getLogger().info("Event listener registration complete");
    }

    /**
     * Schedules all recurring tasks required for the gems' functionality.
     *
     * This includes tasks for managing passive effects, such as:
     * <ul>
     *   <li>The Earth Gem's resistance and other buffs.</li>
     *   <li>The Fire Gem's fire resistance.</li>
     *   <li>The Light Gem's glowing effect on entities.</li>
     *   <li>The Water Gem's water breathing and enhanced swimming abilities.</li>
     * </ul>
     */
    private void scheduleTasks() {
        getLogger().info("Scheduling recurring tasks...");
        try {
            getLogger().info("Starting Earth Gem task...");
            EarthGemTask.startEarthGemTask();
            setFeatureEnabled("earthGem", true);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to start Earth Gem task", e);
            setFeatureEnabled("earthGem", false);
        }

        try {
            getLogger().info("Starting Fire Gem task...");
            FireGemTask.startFireGemTask();
            setFeatureEnabled("fireGem", true);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to start Fire Gem task", e);
            setFeatureEnabled("fireGem", false);
        }


        try {
            getLogger().info("Starting Water Gem task...");
            WaterGemTask.startWaterGemTask();
            setFeatureEnabled("waterGem", true);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to start Water Gem task", e);
            setFeatureEnabled("waterGem", false);
        }

        if (isFeatureEnabled("packetEvents")) {
            try {
                getLogger().info("Starting Light Gem task...");
                LightGemTask.startLightGemTask();
                setFeatureEnabled("lightGem", true);
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to start Light Gem task", e);
                setFeatureEnabled("lightGem", false);
            }
        } else {
            getLogger().warning("Skipping Light Gem task: PacketEvents is not enabled.");
            setFeatureEnabled("lightGem", false);
        }

        getLogger().info("Task scheduling complete");
    }

    /**
     * Called when the plugin is disabled.
     *
     * This method performs all necessary cleanup operations, including:
     * <ol>
     *   <li>Cancelling all active Bukkit tasks to prevent memory leaks.</li>
     *   <li>Terminating the PacketEvents API to ensure a clean shutdown.</li>
     *   <li>Logging the shutdown process for administrative purposes.</li>
     * </ol>
     */
    @Override
    public void onDisable() {
        getLogger().info("---------- GemMod Disabling ----------");

        try {
            getLogger().info("Cancelling all scheduled tasks...");
            getServer().getGlobalRegionScheduler().cancelTasks(this);
            getLogger().info("Tasks cancelled");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error cancelling tasks", e);
        }

        // Disable the PacketEvents API if it was enabled
        if (isFeatureEnabled("packetEvents")) {
            try {
                PacketEvents.getAPI().terminate();
                getLogger().info("PacketEvents API terminated");
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Error terminating PacketEvents API", e);
            }
        }

        getLogger().info("GemMod disabled. Thank you for using GemMod!");
    }

    /**
     * Retrieves the singleton instance of the GemMod plugin.
     *
     * @return The singleton instance of the {@link GemMod} plugin.
     */
    public static GemMod getInstance() {
        return getPlugin(GemMod.class);
    }

    /**
     * Sets the enabled status of a specific feature.
     *
     * @param feature The name of the feature to update.
     * @param enabled {@code true} to enable the feature, {@code false} to disable it.
     */
    private void setFeatureEnabled(String feature, boolean enabled) {
        enabledFeatures.put(feature, enabled);
    }

    /**
     * Checks if a specific feature is currently enabled.
     *
     * @param feature The name of the feature to check.
     * @return {@code true} if the feature is enabled, {@code false} otherwise.
     */
    public boolean isFeatureEnabled(String feature) {
        return enabledFeatures.getOrDefault(feature, false);
    }

    /**
     * Logs the current status of all registered features to the console.
     * This is useful for debugging and verifying that all components have been loaded correctly.
     */
    private void logEnabledFeatures() {
        getLogger().info("\n---------- GemMod Feature Status ----------");

        for (Map.Entry<String, Boolean> entry : enabledFeatures.entrySet()) {
            NamedTextColor color = entry.getValue() ? NamedTextColor.GREEN : NamedTextColor.RED;
            getComponentLogger().info(
                Component.text(entry.getKey() + ": " + (entry.getValue() ? "ENABLED :)" : "DISABLED :("), color)
            );
        }
    }
}
