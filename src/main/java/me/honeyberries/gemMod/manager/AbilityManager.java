package me.honeyberries.gemMod.manager;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.honeyberries.gemMod.GemMod;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import java.util.logging.Logger;

/**
 * <b>AbilityManager</b> is responsible for executing the special abilities associated with different gem types.
 * </p>
 * <ul>
 *   <li><b>Air Gem:</b> Provides a velocity boost</li>
 *   <li><b>Darkness Gem:</b> Grants temporary invisibility and hides equipment</li>
 *   <li><b>Earth Gem:</b> Grants temporary invulnerability through resistance effect</li>
 *   <li><b>Fire Gem:</b> Launches a powerful fireball projectile</li>
 *   <li><b>Light Gem:</b> Strikes a targeted player with lightning</li>
 * </ul>
 * <p>
 * Each ability has its own cooldown period and visual/audio feedback for players.
 * </p>
 */
public class AbilityManager {

    // Static references
    private static final CooldownManager cooldownManager = CooldownManager.getInstance();
    private static final GemMod plugin = GemMod.getInstance();
    private static final Logger logger = plugin.getLogger();

    // Duration of cooldown constants
    private static final long AIR_COOLDOWN_MILLIS = 15_000; // 15 seconds
    private static final long DARKNESS_COOLDOWN_MILLIS = 60_000; // 60 seconds
    private static final long EARTH_COOLDOWN_MILLIS = 70_000; // 70 seconds
    private static final long FIREBALL_COOLDOWN_MILLIS = 20_000; // 20 seconds
    private static final long LIGHT_COOLDOWN_MILLIS = 30_000; // 30 seconds
    private static final long WATER_COOLDOWN_MILLIS = 45_000; // 45 seconds

    // Duration of ability effects
    private static final int INVISIBILITY_DURATION_TICKS = 15 * 20; // 15 seconds in ticks
    private static final int INVULNERABILITY_DURATION_TICKS = 10 * 20; // 10 seconds in ticks
    private static final int WATER_FREEZE_DURATION_TICKS = 10 * 20; // 10 seconds in ticks


    /**
     * Checks if a gem ability is on cooldown for a given player.
     * If it is, it sends a message to the player and logs the event.
     *
     * @param player      The player to check.
     * @param gemType     The type of gem ability.
     * @param abilityName The display name of the ability.
     * @return {@code true} if the ability is on cooldown and the player cannot bypass it, {@code false} otherwise.
     */
    private static boolean isAbilityOnCooldown(Player player, GemType gemType, String abilityName) {
        long remainingCooldown = cooldownManager.getRemainingCooldown(player, gemType);
        if (remainingCooldown <= 0) {
            return false;
        }

        long secondsLeft = remainingCooldown / 1000;
        logger.info(String.format("%s on cooldown for %s: %ds remaining", abilityName, player.getName(), secondsLeft));

        if (player.hasPermission("gemmod.cooldown.bypass")) {
            logger.info(String.format("Player %s bypassing %s cooldown with permission", player.getName(), abilityName));
            return false;
        }

        player.sendMessage(Component.text(String.format("%s is on cooldown! %ds left.", abilityName, secondsLeft), NamedTextColor.RED));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        logger.info(String.format("%s ability denied for %s due to cooldown", abilityName, player.getName()));
        return true;
    }


    /**
     * Triggers the Air Gem ability, providing the player with a velocity boost (Double Jump).
     * </p>
     * <ol>
     *   <li>Checks if the player is on cooldown for the Air Gem ability</li>
     *   <li>If not on cooldown or if player has bypass permission, applies a velocity boost in the direction the player is facing</li>
     *   <li>Sets a cooldown for the ability</li>
     *   <li>Provides audio-visual feedback to the player</li>
     * </ol>
     *
     * @param player The player using the Air Gem ability
     */
    public static void handleAirGemAbility(Player player) {
        logger.info("Player " + player.getName() + " attempting to use Air Gem ability");
        if (isAbilityOnCooldown(player, GemType.AIR, "Double jump")) {
            return;
        }

        // Calculate boost vector (upward and forward)
        double boostSpeed = 5.0; // Adjust strength as needed
        Vector direction = player.getLocation().getDirection().normalize();
        Vector velocity = direction.multiply(boostSpeed);
        player.setVelocity(velocity);
        logger.info("Applied velocity boost to " + player.getName() + ": " + velocity);

        // Set cooldown and provide feedback
        cooldownManager.setCooldown(player, GemType.AIR, AIR_COOLDOWN_MILLIS, true);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_THROW, 1.0f, 1.0f);
        player.sendMessage(Component.text("You used the Double Jump!", TextColor.fromHexString("#90e1e1")));
        logger.info("Air Gem ability successfully used by " + player.getName() + ", cooldown set for " + (AIR_COOLDOWN_MILLIS / 1000) + "s");
    }


    /**
     * Triggers the Darkness Gem ability to grant temporary invisibility and equipment hiding.
     * </p>
     * <ol>
     *   <li>Checks if the player is on cooldown for the Darkness Gem ability</li>
     *   <li>If not on cooldown or if player has bypass permission, applies invisibility effect</li>
     *   <li>Creates a repeating task to hide the player's equipment from other players</li>
     *   <li>Sets a cooldown for the ability</li>
     *   <li>Schedules a task to remove the invisibility and restore equipment visibility after duration</li>
     *   <li>Provides audio-visual feedback to the player</li>
     * </ol>
     *
     * @param player The player using the Darkness Gem ability
     */
    public static void handleDarknessGemAbility(Player player) {
        logger.info("Player " + player.getName() + " attempting to use Darkness Gem ability");
        if (isAbilityOnCooldown(player, GemType.DARKNESS, "Darkness Gem")) {
            return;
        }

        // Apply invisibility and start hiding equipment
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, INVISIBILITY_DURATION_TICKS, 0, false, false, true));
        logger.info("Applied invisibility effect to " + player.getName() + " for " + (INVISIBILITY_DURATION_TICKS / 20) + " seconds");

        ScheduledTask playerHideTask = startEquipmentHidingTask(player);

        // Set cooldown
        cooldownManager.setCooldown(player, GemType.DARKNESS, DARKNESS_COOLDOWN_MILLIS, true);
        logger.info("Set Darkness Gem cooldown for " + player.getName() + " for " + (DARKNESS_COOLDOWN_MILLIS / 1000) + " seconds");

        // Schedule task to remove effects after duration
        scheduleEffectRemoval(player, playerHideTask);

        // Notify the player
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1.0f, 1.0f);
        player.sendMessage(Component.text(String.format("You are now Invisible for %d seconds!", INVISIBILITY_DURATION_TICKS / 20), TextColor.fromHexString("#12375e")));
        logger.info("Darkness Gem ability successfully activated for " + player.getName());
    }

    private static ScheduledTask startEquipmentHidingTask(Player player) {
        logger.info("Starting equipment hiding task for " + player.getName());
        return player.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            if (!player.isOnline()) {
                logger.info("Player " + player.getName() + " went offline, cancelling equipment hiding task");
                scheduledTask.cancel();
                return;
            }
            hidePlayerEquipment(player);
        }, 1, 1);
    }

    private static void scheduleEffectRemoval(Player player, ScheduledTask playerHideTask) {
        logger.info("Scheduling invisibility removal task for " + player.getName() + " in " + (INVISIBILITY_DURATION_TICKS / 20) + " seconds");
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> {
            logger.info("Removing darkness gem effect from " + player.getName());
            playerHideTask.cancel();
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            showPlayerEquipment(player);
        }, INVISIBILITY_DURATION_TICKS);
    }

    private static void hidePlayerEquipment(Player player) {
        player.setArrowsInBody(0, false);
        player.setBeeStingersInBody(0);
        player.setVisualFire(TriState.FALSE);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player)) {
                p.sendEquipmentChange(player, EquipmentSlot.HAND, null);
                p.sendEquipmentChange(player, EquipmentSlot.OFF_HAND, null);
                p.sendEquipmentChange(player, EquipmentSlot.HEAD, null);
                p.sendEquipmentChange(player, EquipmentSlot.CHEST, null);
                p.sendEquipmentChange(player, EquipmentSlot.LEGS, null);
                p.sendEquipmentChange(player, EquipmentSlot.FEET, null);
            }
        }
    }

    private static void showPlayerEquipment(Player player) {
        player.setVisualFire(TriState.NOT_SET);
        logger.info("Restoring equipment visibility for " + player.getName());
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOnline() && !p.equals(player)) {
                p.sendEquipmentChange(player, EquipmentSlot.HAND, player.getInventory().getItemInMainHand());
                p.sendEquipmentChange(player, EquipmentSlot.OFF_HAND, player.getInventory().getItemInOffHand());
                p.sendEquipmentChange(player, EquipmentSlot.HEAD, player.getInventory().getHelmet());
                p.sendEquipmentChange(player, EquipmentSlot.CHEST, player.getInventory().getChestplate());
                p.sendEquipmentChange(player, EquipmentSlot.LEGS, player.getInventory().getLeggings());
                p.sendEquipmentChange(player, EquipmentSlot.FEET, player.getInventory().getBoots());
            }
        }
    }



    /**
     * Triggers the Earth Gem ability to grant temporary invulnerability through maximum resistance.
     * </p>
     * <ol>
     *   <li>Checks if the player is on cooldown for the Earth Gem ability</li>
     *   <li>If not on cooldown or if player has bypass permission, applies a maximum level resistance effect</li>
     *   <li>Sets a cooldown for the ability</li>
     *   <li>Provides audio-visual feedback to the player</li>
     * </ol>
     * <p>
     * <b>Note:</b> The resistance level of 254 effectively makes the player invulnerable to most damage sources.
     * </p>
     *
     * @param player The player using the Earth Gem ability
     */
    public static void handleEarthGemAbility(Player player) {
        logger.info("Player " + player.getName() + " attempting to use Earth Gem ability");
        if (isAbilityOnCooldown(player, GemType.EARTH, "Damage Resistance")) {
            return;
        }

        // Apply maximum resistance effect for invulnerability
        logger.info("Applying invulnerability effect to " + player.getName() + " for " + (INVULNERABILITY_DURATION_TICKS / 20) + " seconds");
        player.getScheduler().run(plugin, scheduledTask -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, INVULNERABILITY_DURATION_TICKS, 254, false, true, true));
            logger.info("Resistance effect level 255 applied to " + player.getName());
        }, null);

        // Set cooldown for Earth Gem usage
        cooldownManager.setCooldown(player, GemType.EARTH, EARTH_COOLDOWN_MILLIS, true);
        logger.info("Set Earth Gem cooldown for " + player.getName() + " for " + (EARTH_COOLDOWN_MILLIS / 1000) + " seconds");

        // Provide feedback to the player
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendMessage(Component.text(String.format("You are now invulnerable for %d seconds!", INVULNERABILITY_DURATION_TICKS / 20), TextColor.fromHexString("#3ad422")));
        logger.info("Earth Gem ability successfully activated for " + player.getName());
    }


    /**
     * Triggers the Fire Gem ability to launch a powerful explosive fireball.
     * </p>
     * <ol>
     *   <li>Checks if the player is on cooldown for the Fire Gem ability</li>
     *   <li>If not on cooldown or if player has bypass permission, creates and launches a fireball projectile</li>
     *   <li>Sets the fireball to be incendiary (causes fire) and with high explosive yield</li>
     *   <li>Sets a cooldown for the ability</li>
     *   <li>Provides audio-visual feedback to the player</li>
     * </ol>
     *
     * @param player The player using the Fire Gem ability
     */
    public static void handleFireGemAbility(Player player) {
        logger.info("Player " + player.getName() + " attempting to use Fire Gem ability");
        if (isAbilityOnCooldown(player, GemType.FIRE, "Fireball")) {
            return;
        }

        // Launch a fireball with high yield and incendiary effect</i>
        Vector velocity = player.getLocation().getDirection().multiply(player.getVelocity().length() + 3);

        Fireball fireball = player.launchProjectile(Fireball.class, velocity);
        fireball.setIsIncendiary(true);
        fireball.setYield(6F); // Explosion power (6 is quite powerful)
        logger.info("Launched fireball from " + player.getName() + " with velocity " + velocity + " and yield 6.0");

        // Set cooldown for Fire Gem usage
        cooldownManager.setCooldown(player, GemType.FIRE, FIREBALL_COOLDOWN_MILLIS, true);
        logger.info("Set Fire Gem cooldown for " + player.getName() + " for " + (FIREBALL_COOLDOWN_MILLIS / 1000) + " seconds");

        // Provide feedback to the player
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
        player.sendMessage(Component.text("You threw a Fireball!").color(TextColor.fromHexString("#f0590e")));
        logger.info("Fire Gem ability successfully activated for " + player.getName());
    }


    /**
     * Handles the Light Gem ability, which strikes a targeted player with multiple lightning bolts.
     * </p>
     * <ol>
     *   <li>Checks if the player is on cooldown for the Light Gem ability</li>
     *   <li>If not on cooldown or if player has bypass permission, checks if the player is targeting another player</li>
     *   <li>If a valid player target is found, strikes that player with multiple lightning bolts (10 strikes)</li>
     *   <li>Sets a cooldown for the ability</li>
     *   <li>Provides feedback to the player</li>
     * </ol>
     * <p>
     * <b>Note:</b> The ability requires line of sight to another player and has a maximum range of 120 blocks.
     * </p>
     *
     * @param player The player using the Light Gem ability
     */
    public static void handleLightGemAbility(Player player) {
        logger.info("Player " + player.getName() + " attempting to use Light Gem ability");
        if (isAbilityOnCooldown(player, GemType.LIGHT, "Light Gem")) {
            return;
        }

        // Check if the player has a target
        if (!(player.getTargetEntity(120) instanceof LivingEntity targetEntity)) {
            player.sendMessage(Component.text("You must be looking at another player/mob to use the Light Gem!", NamedTextColor.RED));
            logger.info("No valid target found for " + player.getName() + " to use Light Gem");
            return;
        }

        logger.info("Player " + player.getName() + " targeting entity " + targetEntity.getName() + " with Light Gem");

        //Strike the targeted player with lightning multiple times over a short duration
        logger.info("Striking " + targetEntity.getName() + " with lightning bolts");

        // Strike the target player with lightning and apply damage
        targetEntity.damage(60, player); // Damage the target entity with 60 health points (30 hearts)
        targetEntity.getWorld().strikeLightningEffect(targetEntity.getLocation());

        // Set cooldown for Light Gem usage
        cooldownManager.setCooldown(player, GemType.LIGHT, LIGHT_COOLDOWN_MILLIS, true);
        logger.info("Set Light Gem cooldown for " + player.getName() + " for " + (LIGHT_COOLDOWN_MILLIS / 1000) + " seconds");

        // Provide feedback to the player
        player.sendMessage(Component.text()
                    .append(Component.text("You struck ", TextColor.fromHexString("#ffef4f")))
                    .append(Component.text(targetEntity.getName(), NamedTextColor.GREEN))
                    .append(Component.text(" with lightning!", TextColor.fromHexString("#ffef4f")))
                );
        logger.info("Light Gem ability successfully used by " + player.getName() + " on " + targetEntity.getName());
    }


    /**
     * Handles the Water Gem ability, which freezes a targeted player in place for a short duration.
     * </p>
     * <ol>
     *   <li>Checks if the player is on cooldown for the Water Gem ability</li>
     *   <li>If not on cooldown or if player has bypass permission, checks if the player is targeting another player or mob</li>
     *   <li>If a valid target is found, teleports the target back to their original location every tick for 10 seconds</li>
     *   <li>Sets a cooldown for the ability</li>
     *   <li>Provides feedback to both the caster and the target</li>
     * </ol>
     * <p>
     * <b>Note:</b> The ability requires line of sight to another entity and has a maximum range of 120 blocks.
     * </p>
     *
     * @param player The player using the Water Gem ability
     */
    public static void handleWaterGemAbility(Player player) {
        logger.info("Player " + player.getName() + " attempting to use Water Gem ability");
        if (isAbilityOnCooldown(player, GemType.WATER, "Water Gem")) {
            return;
        }

        // Find a valid target
        if (!(player.getTargetEntity(120) instanceof LivingEntity targetEntity)) {
            player.sendMessage(Component.text("You must be looking at another player/mob to use the Water Gem!", NamedTextColor.RED));
            logger.info("No valid target found for " + player.getName() + " to use Water Gem");
            return;
        }
        logger.info("Player " + player.getName() + " targeting entity " + targetEntity.getName() + " with Water Gem");

        // Set cooldown immediately
        cooldownManager.setCooldown(player, GemType.WATER, WATER_COOLDOWN_MILLIS, true);
        logger.info("Set Water Gem cooldown for " + player.getName() + " for " + (WATER_COOLDOWN_MILLIS / 1000) + " seconds");

        // Schedule all targetEntity operations on its region thread for safety
        targetEntity.getScheduler().run(plugin, scheduledTask -> {
            Location freezeLocation = targetEntity.getLocation();
            String targetName = targetEntity.getName();

            // Task to repeatedly teleport the target back to the freeze location
            ScheduledTask freezeTask = targetEntity.getScheduler().runAtFixedRate(plugin, t -> {
                targetEntity.teleportAsync(freezeLocation);
            }, null, 1, 1);

            // Task to cancel the freeze effect after the duration
            targetEntity.getScheduler().runDelayed(plugin, t -> {
                if (freezeTask != null) {
                    freezeTask.cancel();
                }
                logger.info("Unfroze entity " + targetName);
            }, null, WATER_FREEZE_DURATION_TICKS);

            // Play sound and notify target
            targetEntity.getWorld().playSound(targetEntity.getLocation(), Sound.ENTITY_DOLPHIN_SPLASH, 1.0f, 1.0f);
            if (targetEntity instanceof Player targetPlayer) {
                targetPlayer.sendMessage(Component.text()
                        .append(Component.text("You have been frozen by ", NamedTextColor.BLUE))
                        .append(Component.text(player.getName(), NamedTextColor.GREEN))
                        .append(Component.text(" for " + (WATER_FREEZE_DURATION_TICKS / 20) + " seconds!", NamedTextColor.BLUE))
                );
            }
            logger.info("Froze entity " + targetName);

            // Notify the caster (run on main thread for safety, though not strictly necessary)
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> {
                player.sendMessage(Component.text("You froze ", TextColor.fromHexString("#40c7ff"))
                        .append(Component.text(targetName, NamedTextColor.GREEN))
                        .append(Component.text(" for " + (WATER_FREEZE_DURATION_TICKS / 20) + " seconds!", TextColor.fromHexString("#41c7ff")))
                );
            });
        }, null);
    }
}