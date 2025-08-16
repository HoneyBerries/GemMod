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
 * Manages the passive effects of the Earth Gem by granting beneficial potion effects to its holder.
 *
 * This task runs periodically to ensure that any player holding an Earth Gem
 * receives continuous Haste, Speed, and Strength effects.
 *
 * @author HoneyBerries
 * @version 1.0
 */
public class EarthGemTask {

    /**
     * A reference to the main plugin instance.
     */
    private static final Plugin plugin = GemMod.getInstance();

    /**
     * The duration of the potion effects, in ticks (15 seconds).
     */
    private static final int POTION_DURATION_TICKS = 15 * 20;

    /**
     * The amplifier for the potion effects (level 2).
     */
    private static final int POTION_AMPLIFIER = 1;

    /**
     * Starts a recurring task that grants passive effects to players holding an Earth Gem.
     *
     * The task runs every tick to check all online players. If a player has an
     * Earth Gem, it applies Haste, Speed, and Strength effects to them.
     */
    public static void startEarthGemTask() {
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (GemManager.hasGem(player, GemType.EARTH)) {
                    applyEarthPotionEffect(player);
                }
            });
        }, 1, 1); // Run every tick (1 tick delay, 1 tick interval)
    }

    /**
     * Applies Haste, Speed, and Strength potion effects to the specified player.
     *
     * The effects are applied on the player's region thread to ensure thread safety.
     *
     * @param player The player to whom the effects will be applied.
     */
    private static void applyEarthPotionEffect(Player player) {
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