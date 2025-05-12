package me.honeyberries.gemMod;

import me.honeyberries.gemMod.command.GemCommand;
import me.honeyberries.gemMod.listener.*;
import me.honeyberries.gemMod.task.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;

/**
 * Main class for the GemMod Bukkit plugin.
 * Handles plugin lifecycle events, command registration, and listener setup.
 */
public final class GemMod extends JavaPlugin {

    /**
     * Called when the plugin is enabled.
     * Registers commands, initializes managers, and sets up event listeners.
     */
    @Override
    public void onEnable() {
        getLogger().info("GemMod has been enabled!");
        // Initialize managers and other components

        // Register commands
        registerCommands();
        // Register event listeners
        registerEventListeners();
        // Register recurring tasks
        scheduleTasks();
    }

    /**
     * Registers all plugin commands
     */
    private void registerCommands() {
        // Register the /gem command with the GemCommand executor
        Objects.requireNonNull(getCommand("gem")).setExecutor(new GemCommand());
    }

    /**
     * Registers all event listeners
     */
    private void registerEventListeners() {
        // Register general listeners
        getServer().getPluginManager().registerEvents(new HotbarSwitchCooldownListener(), this);
        getServer().getPluginManager().registerEvents(new GemUsageListener(), this);

        // Register gem-specific listeners
        getServer().getPluginManager().registerEvents(new AirGemListener(), this);
        getServer().getPluginManager().registerEvents(new DarknessGemListener(), this);

    }

    /**
     * Schedules all recurring tasks
     */
    private void scheduleTasks() {
        // Schedule the EarthGemTask to run every tick
        EarthGemTask.startEarthGemTask();
        // Schedule the FireGemTask to run every tick
        FireGemTask.startFireGemTask();
    }

    /**
     * Called when the plugin is disabled.
     * Logs the shutdown and cancel any running tasks.
     */
    @Override
    public void onDisable() {
        getLogger().info("GemMod has been disabled! Bye!");
        getServer().getGlobalRegionScheduler().cancelTasks(this);
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