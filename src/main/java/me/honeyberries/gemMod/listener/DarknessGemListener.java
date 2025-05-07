package me.honeyberries.gemMod.listener;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Listener for Darkness Gem effects.
 * Applies blindness and visual effects to entities attacked by players with the Darkness Gem.
 */
public class DarknessGemListener implements Listener {

    /** Reference to the main plugin instance. */
    private final me.honeyberries.gemMod.GemMod plugin = GemMod.getInstance();

    /**
     * Handles entity damage by entity events.
     * Applies blindness and smoke particles to living entities when attacked.
     *
     * @param event The EntityDamageByEntityEvent triggered when an entity is damaged by another entity.
     */
    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        // Check if the damaged entity is a player and if the attacker is also a player
        if (event.getEntity() instanceof Player damagedPlayer && event.getDamager() instanceof Player attacker) {

            // Check if the attacker has the Darkness Gem in their inventory
            if (GemManager.hasGem(attacker, GemType.DARKNESS)) {

                // Apply blindness effect to the damaged player
                damagedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));

                Particle.DustOptions options = new Particle.DustOptions(Color.fromRGB(11, 36, 77), 1.2f);

                // Spawn smoke particles around the damaged player
                ScheduledTask particleBlindnessTask = damagedPlayer.getScheduler().runAtFixedRate(plugin, scheduledTask -> {
                    for (int i = 0; i < 20; i++) {
                        if (!damagedPlayer.isOnline()) {
                            scheduledTask.cancel();
                            return;
                        }
                        double randX = damagedPlayer.getLocation().getX() + (Math.random() - 0.5);
                        double randY = damagedPlayer.getLocation().getY() + (Math.random() - 0.5);
                        double randZ = damagedPlayer.getLocation().getZ() + (Math.random() - 0.5);
                        damagedPlayer.getWorld().spawnParticle(Particle.DUST, randX, randY, randZ, 1, options);
                    }
                }, null, 1L, 1); // 1 tick delay, repeat every tick


                // Cancel the task after 5 seconds
                plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
                    assert particleBlindnessTask != null;
                    particleBlindnessTask.cancel();
                }, 5 * 20L); // 5 seconds in ticks
            }

            plugin.getLogger().info("Blindness applied to " + damagedPlayer.getName() + " by " + attacker.getName());
        }
    }
}
