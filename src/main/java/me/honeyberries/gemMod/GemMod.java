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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Main class for the GemMod Bukkit plugin.
 *
 * <p>This plugin adds special gem items to Minecraft that grant players unique abilities:</p>
 * <ul>
 *   <li><b>Air Gem:</b> Provides a velocity boost (double jump)</li>
 *   <li><b>Darkness Gem:</b> Grants temporary invisibility and hides equipment</li>
 *   <li><b>Earth Gem:</b> Grants temporary invulnerability through resistance effect</li>
 *   <li><b>Fire Gem:</b> Launches a powerful fireball projectile</li>
 *   <li><b>Light Gem:</b> Strikes a targeted player with lightning and makes other players glow</li>
 * </ul>
 *
 * <p>This main class handles:</p>
 * <ul>
 *   <li>Plugin lifecycle events (enable/disable)</li>
 *   <li>Command registration</li>
 *   <li>Event listener setup</li>
 *   <li>Recurring task scheduling</li>
 *   <li>Integration with the GlowingEntities library</li>
 * </ul>
 *
 * @author HoneyBerries
 * @since 1.0
 */
public final class GemMod extends JavaPlugin {

    /**
     * A map to track the enabled status of various features.
     * This allows for dynamic feature management and debugging.
     */
    private final Map<String, Boolean> enabledFeatures = new ConcurrentHashMap<>();

    /**
     * Called when the plugin is loaded.
     * <p>
     * Initializes the PacketEvents API and prepares it for use during the plugin's lifecycle.
     * </p>
     */
    @Override
    public void onLoad() {
        try {
            // Initialize the PacketEvents API

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
     * </p>
     */
    @Override
    public void onEnable() {
        getLogger().info("\n---------- GemMod Enabled ----------\n");

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
     * <p>
     * Currently registers:
     * </p>
     * <ul>
     *   <li><b>/gem</b> command: Main command for gem-related operations</li>
     *   <li><b>/gemmod</b> command: Command for managing gem mod settings and features</li>
     * </ul>
     */
    private void registerCommands() {




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
     * This includes:
     * </p>
     * <ul>
     *   <li>General listeners for gem usage and cooldown management, etc</li>
     *   <li>Gem-specific listeners for special ability triggers</li>
     * </ul>
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
     * Schedules all recurring tasks needed for gem functionality.
     * <p>
     * This includes tasks for:
     * </p>
     * <ul>
     *   <li>Earth Gem: Monitoring and applying various beneficial effects</li>
     *   <li>Fire Gem: Handling fire resistance mechanics</li>
     *   <li>Light Gem: Managing glowing effects on players</li>
     *   <li>Water Gem: Handling water breathing and dolphin's grace</li>
     * </ul>
     */
    private void scheduleTasks() {
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
     * <p>
     * Performs cleanup operations:
     * </p>
     * <ol>
     *   <li>Cancels all scheduled tasks</li>
     *   <li>Disables the GlowingEntities integration</li>
     *   <li>Logs the shutdown process</li>
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
     * @return the GemMod plugin instance
     */
    public static GemMod getInstance() {
        return getPlugin(GemMod.class);
    }

    /**
     * Sets the enabled status of a feature.
     *
     * @param feature the feature name
     * @param enabled true if the feature is enabled, false otherwise
     */
    private void setFeatureEnabled(String feature, boolean enabled) {
        enabledFeatures.put(feature, enabled);
    }

    /**
     * Checks if a feature is enabled.
     *
     * @param feature the feature to check
     * @return true if the feature is enabled, false otherwise
     */
    public boolean isFeatureEnabled(String feature) {
        return enabledFeatures.getOrDefault(feature, false);
    }

    /**
     * Logs the status of all registered features.
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