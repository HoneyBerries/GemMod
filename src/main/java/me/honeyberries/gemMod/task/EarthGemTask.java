package me.honeyberries.gemMod.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.function.Consumer;

/**
 * Task that periodically grants Haste IV to players holding an Earth gem.
 * Executed globally, but all entity-touching work is re-scheduled on the
 * player's own scheduler to satisfy Folia's threading rules.
 * <p>
 * This task runs continuously and checks all online players to see if they
 * have an Earth gem in their inventory. Players with the gem receive a
 * continuous Haste IV effect for improved mining speed.
 */
public class EarthGemTask implements Consumer<ScheduledTask> {

    /**
     * Reference to the main plugin instance
     */
    private final Plugin plugin = GemMod.getInstance();

    /**
     * Accepts a scheduled task and processes it by checking all online players
     * for Earth gems and applying effects accordingly.
     *
     * @param ignored The scheduled task being processed (ignored in implementation)
     */
    @Override
    public void accept(ScheduledTask ignored) {
        // Iterate through all online players
        for (Player player : Bukkit.getOnlinePlayers()) {

            // Schedule per-player effect application on their own thread
            player.getScheduler().runAtFixedRate(plugin, task -> {
                // Check if player has an Earth gem
                if (GemManager.hasGem(player, GemType.EARTH)) {
                    // Apply Haste IV effect for 15 seconds (300 ticks)
                    // Parameters: Effect type, duration (ticks), amplifier (level-1), 
                    // ambient (false), particles (false), icon (true)
                    player.addPotionEffect(
                            new PotionEffect(PotionEffectType.HASTE, 15 * 20, 3, false, false, true)
                    );
                }
            }, null, 1L, 1L); // Run every tick
        }
    }
}