package me.honeyberries.gemMod.listener;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.logging.Logger;

/**
 * Listener for Darkness Gem passive effects when attacking other players.
 *
 * <p>This class handles the passive effect of the Darkness Gem, which is separate from its
 * active ability (invisibility). When a player with the Darkness Gem in their inventory
 * attacks another player, this listener:</p>
 * <ul>
 *   <li>Applies a blindness effect to the attacked player</li>
 *   <li>Creates a dense cloud of black particles in front of the attacked player's view</li>
 *   <li>Maintains these effects for a short duration</li>
 * </ul>
 *
 * <p>This creates a "blinding" effect that obscures the attacked player's vision,
 * giving the attacker a tactical advantage.</p>
 *
 * @author HoneyBerries
 * @since 1.0
 */
public class DarknessGemListener implements Listener {

    /** Reference to the main plugin instance. */
    private final GemMod plugin = GemMod.getInstance();

    /** Logger for recording events related to the Darkness Gem passive effect. */
    private final Logger logger = plugin.getLogger();

    private final Integer DARKNESS_GEM_PASSIVE_EFFECT_DURATION = 5 * 20; // 5 seconds in ticks

    /**
     * Handles player-to-player attack events for Darkness Gem passive effect.
     *
     * <p>This method is triggered when a player attacks another player. If the attacker
     * has the Darkness Gem in their inventory, this method:</p>
     * <ol>
     *   <li>Applies a level 1 blindness effect to the damaged player for 5 seconds (100 ticks)</li>
     *   <li>Creates a repeating task that spawns black particles in front of the damaged player's view</li>
     *   <li>Maintains the particle effect for 5 seconds</li>
     *   <li>Logs the application of the effect</li>
     * </ol>
     *
     * <p>The particle effect creates a dense cloud that significantly impairs the damaged player's
     * vision, making it difficult for them to see and fight back effectively.</p>
     *
     * @param event The EntityDamageByEntityEvent triggered when an entity is damaged by another entity.
     */
    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        // Check if the damaged entity is a player and if the attacker is also a player
        if (event.getEntity() instanceof Player damagedPlayer && event.getDamager() instanceof Player attacker) {
            logger.info("Player " + attacker.getName() + " attacked player " + damagedPlayer.getName());

            // Check if the attacker has the Darkness Gem in their inventory
            if (GemManager.hasGem(attacker, GemType.DARKNESS)) {
                logger.info("Attacker " + attacker.getName() + " has Darkness Gem - applying blindness effect");

                // Apply blindness effect to the damaged player (level 1, 5 seconds)
                damagedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));
                logger.info("Applied blindness effect to " + damagedPlayer.getName() + " for 5 seconds");

                // Spawn particles directly in front of the player's view to obstruct vision


                // Create a denser cloud of black particles
                logger.info("Starting particle blindness task for " + damagedPlayer.getName());
                ScheduledTask particleBlindnessTask = damagedPlayer.getScheduler().runAtFixedRate(plugin, scheduledTask -> {
                    if (!damagedPlayer.isOnline()) {
                        logger.info("Player " + damagedPlayer.getName() + " went offline, cancelling particle blindness task");
                        scheduledTask.cancel();
                        return;
                    }

                    // Create a denser cloud of darker particles
                    Particle.DustOptions options = new Particle.DustOptions(Color.BLACK, 2.0f);

                    // Get the player's eye location and direction
                    Location eyeLocation = damagedPlayer.getEyeLocation();
                    Vector direction = eyeLocation.getDirection();

                    // Create a wall of particles in front of the player
                    for (int i = 0; i < 160; i++) {
                        // Position particles 0.5-1.5 blocks in front of the player's face
                        double distance = 0.5 + (Math.random());
                        double spread = 0.5;

                        double x = eyeLocation.getX() + (direction.getX() * distance) + ((Math.random() - 0.5) * spread);
                        double y = eyeLocation.getY() + (direction.getY() * distance) + ((Math.random() - 0.5) * spread);
                        double z = eyeLocation.getZ() + (direction.getZ() * distance) + ((Math.random() - 0.5) * spread);

                        // Spawn particle only for the damaged player to see
                        damagedPlayer.spawnParticle(Particle.DUST, x, y, z, 1, 0, 0, 0, 0, options);
                    }
                }, null, 1L, 1);

                // Cancel the task after the duration
                logger.info(String.format("Scheduling particle effect removal for " +
                        "%s in %d seconds", damagedPlayer.getName(),
                        Math.round(DARKNESS_GEM_PASSIVE_EFFECT_DURATION / 20.0)));
                plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {

                assert particleBlindnessTask != null;
                particleBlindnessTask.cancel();
                logger.info("Particle blindness effect removed from " + damagedPlayer.getName());
            }, DARKNESS_GEM_PASSIVE_EFFECT_DURATION);

                logger.info("Darkness Gem passive effect successfully applied: " + damagedPlayer.getName() + " blinded by " + attacker.getName());
            }
        }
    }
}