package me.honeyberries.gemMod;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.settings.PacketEventsSettings;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.honeyberries.gemMod.command.GemCommand;
import me.honeyberries.gemMod.configuration.GemModData;
import me.honeyberries.gemMod.listener.*;
import me.honeyberries.gemMod.task.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;

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

    @Override
    public void onLoad() {

        // Initialize the PacketEvents API
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEventsSettings settings = PacketEvents.getAPI().getSettings();

        settings.checkForUpdates(false); // Enable debug mode for detailed logging

        //On Bukkit, calling this here is essential, hence the name "load"
        PacketEvents.getAPI().load();
        getLogger().info("PacketEvents API loaded successfully pre-enable");
    }

    /**
     * Called when the plugin is enabled.
     * <p>
     * Initializes the plugin by:
     * </p>
     * <ol>
     *   <li>Setting up the GlowingEntities dependency</li>
     *   <li>Registering commands</li>
     *   <li>Setting up event listeners</li>
     *   <li>Scheduling recurring tasks</li>
     * </ol>
     */
    @Override
    public void onEnable() {
        getLogger().info("---------- GemMod Enabled ----------");
        getLogger().info("Loading plugin data...");
        GemModData.loadData();
        getLogger().info("Configuration files loaded successfully");

        // Initialize PacketEvents API
        PacketEvents.getAPI().init();
        getLogger().info("PacketEvents API ready");

        // Register commands, event listeners, and tasks
        getLogger().info("Registering commands, event listeners, and tasks...");
        registerCommands();
        registerEventListeners();
        scheduleTasks();

        getLogger().info("---------- GemMod Initialization Complete ----------");
    }

    /**
     * Registers all plugin commands.
     * <p>
     * Currently registers:
     * </p>
     * <ul>
     *   <li><b>/gem</b> command: Main command for gem-related operations</li>
     * </ul>
     */
    private void registerCommands() {
        // Register the /gem command with the GemCommand executor
        Objects.requireNonNull(getCommand("gem")).setExecutor(new GemCommand());
        getLogger().info("Registered /gem command");
    }

    /**
     * Registers all event listeners for the plugin.
     * <p>
     * This includes:
     * </p>
     * <ul>
     *   <li>General listeners for gem usage and cooldown management</li>
     *   <li>Gem-specific listeners for special ability triggers</li>
     * </ul>
     */
    private void registerEventListeners() {
        // Register general listeners
        getLogger().info("Registering general event listeners...");
        getServer().getPluginManager().registerEvents(new HotbarSwitchCooldownListener(), this);
        getServer().getPluginManager().registerEvents(new GemUsageListener(), this);

        // Register gem-specific listeners
        getLogger().info("Registering gem-specific event listeners...");
        getServer().getPluginManager().registerEvents(new AirGemListener(), this);
        getServer().getPluginManager().registerEvents(new DarknessGemListener(), this);

        // Register Gem Crafting Listener
        getLogger().info("Registering gem crafting event listener...");
        getServer().getPluginManager().registerEvents(new GemCraftListener(), this);

        getLogger().info("All event listeners registered successfully");
    }

    /**
     * Schedules all recurring tasks needed for gem functionality.
     * <p>
     * This includes tasks for:
     * </p>
     * <ul>
     *   <li>Earth Gem: Monitoring and applying resistance effects</li>
     *   <li>Fire Gem: Handling fireball mechanics</li>
     *   <li>Light Gem: Managing glowing effects on players</li>
     * </ul>
     */
    private void scheduleTasks() {
        getLogger().info("Starting Earth Gem task...");
        EarthGemTask.startEarthGemTask();

        getLogger().info("Starting Fire Gem task...");
        FireGemTask.startFireGemTask();

        getLogger().info("Starting Light Gem task...");
        LightGemTask.startLightGemTask();

        getLogger().info("All recurring tasks scheduled successfully");
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

        getLogger().info("Cancelling all scheduled tasks...");
        getServer().getGlobalRegionScheduler().cancelTasks(this);

        getLogger().info("Tasks cancelled");

        // Disable the PacketEvents API
        PacketEvents.getAPI().terminate();
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


}

