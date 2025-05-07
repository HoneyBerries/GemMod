package me.honeyberries.gemMod;

import me.honeyberries.gemMod.command.GemCommand;
import me.honeyberries.gemMod.listener.AirGemListener;
import me.honeyberries.gemMod.listener.DarknessGemListener;
import me.honeyberries.gemMod.listener.GemUsageListener;
import me.honeyberries.gemMod.listener.HotbarSwitchCooldownListener;
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
        // Plugin startup logic; logging plugin start
        getLogger().info("GemMod has been enabled!");
        
        // Register the /gem command for development and testing
        Objects.requireNonNull(getCommand("gem")).setExecutor(new GemCommand());


        // Register the hotbar switch cooldown listener
        getServer().getPluginManager().registerEvents(new HotbarSwitchCooldownListener(), this);

        // Register the gem usage listener for handling gem abilities
        getServer().getPluginManager().registerEvents(new GemUsageListener(), this);


        // Register the Air Gem listener for fall damage cancellation
        getServer().getPluginManager().registerEvents(new AirGemListener(), this);

        // Register the Darkness Gem listener for blindness effect and particles
        getServer().getPluginManager().registerEvents(new DarknessGemListener(), this);

    }

    /**
     * Called when the plugin is disabled.
     * Logs the shutdown and cancels any running tasks.
     */
    @Override
    public void onDisable() {
        // Plugin shutdown logic; logging plugin stop
        getLogger().info("GemMod has been disabled!");

        // Ensure all scheduled tasks are canceled to prevent memory leaks
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
