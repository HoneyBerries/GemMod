package me.honeyberries.gemMod.task;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Manages the passive effects of the Fire Gem by granting fire resistance to its holder.
 *
 * This task runs periodically to ensure that any player holding a Fire Gem
 * receives a continuous fire resistance effect, protecting them from fire and lava damage.
 *
 * @author HoneyBerries
 * @version 1.0
 */
public class FireGemTask {

    /**
     * A reference to the main plugin instance.
     */
    private static final GemMod plugin = GemMod.getInstance();

    /**
     * Starts a recurring task that grants fire resistance to players holding a Fire Gem.
     *
     * The task runs every tick to check all online players. If a player has a
     * Fire Gem, it applies a fire resistance effect to them.
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