package me.honeyberries.gemMod.task;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class WaterGemTask {

    private static final GemMod plugin = GemMod.getInstance();

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
