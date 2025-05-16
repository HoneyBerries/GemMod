package me.honeyberries.gemMod.task;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Handles the passive ability of the Earth Gem, which grants Haste to its holder.
 *
 * <p>The Earth Gem provides two abilities:</p>
 * <ul>
 *   <li><b>Active ability:</b> Temporary invulnerability (handled by {@code AbilityManager})</li>
 *   <li><b>Passive ability:</b> Grants continuous Haste to the player holding the gem (handled by this class)</li>
 * </ul>
 *
 * <p>The passive effect ensures that players with the Earth Gem mine and break blocks faster,
 * improving their efficiency. The effect is reapplied every tick to guarantee uninterrupted
 * Haste as long as the player holds the gem.</p>
 *
 * @author HoneyBerries
 * @since 1.0
 */
public class EarthGemTask {

    /** Reference to the main plugin instance. */
    private static final Plugin plugin = GemMod.getInstance();

    /** Duration of the potion effect in ticks (15 seconds). */
    private static final int POTION_DURATION_TICKS = 15 * 20;

    /** Amplifier for the potion effect (level 2). */
    private static final int POTION_AMPLIFIER = 1;

    /**
     * Starts the Earth Gem passive effect recurring task.
     *
     * <p>This method schedules a task that runs every tick (1 tick delay, 1 tick interval) and:</p>
     * <ol>
     *   <li>Checks all online players to see who has an Earth Gem in their inventory</li>
     *   <li>For players with the Earth Gem, applies a Haste potion effect (duration: 15 seconds, amplifier: 1)</li>
     *   <li>Effect is reapplied every tick to ensure it never expires</li>
     * </ol>
     *
     * <p>The potion effect is applied on the player's region thread for thread safety.</p>
     */
    public static void startEarthGemTask() {
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (GemManager.hasGem(player, GemType.EARTH)) {
                    applyHasteAndSpeedEffect(player);
                }
            });
        }, 1, 1); // Run every tick (1 tick delay, 1 tick interval)
    }

    /**
     * Applies the Haste and Speed potion effect to the specified player on their region thread.
     *
     * @param player The player to apply the effect to.
     */
    private static void applyHasteAndSpeedEffect(Player player) {
        player.getScheduler().run(plugin, scheduledPlayerTask -> {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.HASTE, POTION_DURATION_TICKS, POTION_AMPLIFIER, true, false, true
            ));
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, POTION_DURATION_TICKS, POTION_AMPLIFIER, true, false, true
            ));
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH, POTION_DURATION_TICKS, POTION_AMPLIFIER, true, false, true
            ));

        }, null);
    }
}