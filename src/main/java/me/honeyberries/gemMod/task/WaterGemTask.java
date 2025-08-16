package me.honeyberries.gemMod.task;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Manages the passive effects of the Water Gem by granting aquatic abilities to its holder.
 *
 * This task runs periodically to ensure that any player holding a Water Gem
 * receives continuous Water Breathing and Dolphin's Grace effects.
 *
 * @author HoneyBerries
 * @version 1.0
 */
public class WaterGemTask {

    /**
     * A reference to the main plugin instance.
     */
    private static final GemMod plugin = GemMod.getInstance();

    /**
     * Starts a recurring task that grants aquatic abilities to players holding a Water Gem.
     *
     * The task runs every tick to check all online players. If a player has a
     * Water Gem, it applies Water Breathing and Dolphin's Grace effects to them.
     */
    public static void startWaterGemTask() {
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (GemManager.hasGem(player, GemManager.GemType.WATER)) {
                    player.getScheduler().run(plugin, scheduledPlayerTask -> {
                        player.addPotionEffects(
                                List.of(
                                        new PotionEffect(PotionEffectType.WATER_BREATHING, 15 * 20, 0, true, false, true),
                                        new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 15 * 20, 0, true, false, true)
                                )
                        );
                    }, null);
                }
            }
        }, 1, 1); // Run every tick (1 tick delay, 1 tick interval)
    }


}
