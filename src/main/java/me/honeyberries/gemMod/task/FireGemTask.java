package me.honeyberries.gemMod.task;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FireGemTask {

    private static final GemMod plugin = GemMod.getInstance();

    public static void startFireGemTask() {
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (GemManager.hasGem(player, GemType.FIRE)) {
                    // Schedule on the player's region thread
                    player.getScheduler().run(plugin, scheduledPlayerTask -> {
                        player.addPotionEffect(
                                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 15 * 20, 0, true, false, true)
                        );
                    }, null);
                }
            }

        }, 1, 1);
    }
}
