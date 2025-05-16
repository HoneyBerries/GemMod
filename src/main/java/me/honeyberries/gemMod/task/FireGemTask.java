package me.honeyberries.gemMod.task;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Handles the passive ability of the Fire Gem, which grants fire resistance to its holder.
 *
 * <p>The Fire Gem provides two abilities:</p>
 * <ul>
 *   <li><b>Active ability:</b> Launch a powerful fireball (handled by {@code AbilityManager})</li>
 *   <li><b>Passive ability:</b> Grants continuous fire resistance to the player holding the gem (handled by this class)</li>
 * </ul>
 *
 * <p>The passive effect ensures that players with the Fire Gem are immune to fire and lava damage,
 * enhancing their survivability in hazardous environments. The effect is reapplied every tick to
 * guarantee uninterrupted protection.</p>
 *
 * @author HoneyBerries
 * @since 1.0
 */
public class FireGemTask {

    /** Reference to the main plugin instance. */
    private static final GemMod plugin = GemMod.getInstance();

    /**
     * Starts the Fire Gem passive effect recurring task.
     *
     * <p>This method schedules a task that runs every tick (1 tick delay, 1 tick interval) and:</p>
     * <ol>
     *   <li>Checks all online players to see who has a Fire Gem in their inventory</li>
     *   <li>For players with the Fire Gem, applies a Fire Resistance potion effect (duration: 15 seconds)</li>
     *   <li>Effect is reapplied every tick to ensure it never expires</li>
     * </ol>
     *
     * <p>The potion effect is applied on the player's region thread for thread safety.</p>
     */
    public static void startFireGemTask() {
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (GemManager.hasGem(player, GemType.FIRE)) {
                    player.getScheduler().run(plugin, scheduledPlayerTask -> {
                        player.addPotionEffect(
                                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 15 * 20, 0, true, false, true)
                        );
                    }, null);
                }
            }
        }, 1, 1); // Run every tick (1 tick delay, 1 tick interval)
    }
}