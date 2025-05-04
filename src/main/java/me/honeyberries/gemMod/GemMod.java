package me.honeyberries.gemMod;

import me.honeyberries.gemMod.command.GemCommand;
import me.honeyberries.gemMod.listener.GemUsageListener;
import me.honeyberries.gemMod.listener.HotbarSwitchCooldownListener;
import me.honeyberries.gemMod.manager.CooldownManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;

/**
 * Main class for the GemMod Bukkit plugin.
 * Handles plugin lifecycle events, command registration, and listener setup.
 */
public final class GemMod extends JavaPlugin {

    private CooldownManager cooldownManager;

    /**
     * Called when the plugin is enabled.
     * Registers commands, initializes managers, and sets up event listeners.
     */
    @Override
    public void onEnable() {
        // Plugin startup logic; logging plugin start
        getLogger().info("GemMod has been enabled!");
        
        // Register the /gem command
        Objects.requireNonNull(getCommand("gem")).setExecutor(new GemCommand());

        // Initialize the cooldown manager instance
        this.cooldownManager = CooldownManager.getInstance();

        // Register the hotbar switch cooldown listener
        getServer().getPluginManager().registerEvents(new HotbarSwitchCooldownListener(), this);

        // Register the gem usage listener for handling gem abilities
        getServer().getPluginManager().registerEvents(new GemUsageListener(), this);

        // ...existing scheduling tasks if any...
        // For example, future tasks can be scheduled here.
    }

    /**
     * Gets the cooldown manager instance.
     *
     * @return the cooldown manager
     */
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    /**
     * Called when the plugin is disabled.
     * Logs the shutdown and cancels any running tasks.
     */
    @Override
    public void onDisable() {
        // Plugin shutdown logic; logging plugin stop
        getLogger().info("GemMod has been disabled!");

        // Ensure all scheduled tasks are cancelled to prevent memory leaks
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
