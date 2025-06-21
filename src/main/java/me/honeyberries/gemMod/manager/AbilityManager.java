package me.honeyberries.gemMod.manager;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.honeyberries.gemMod.GemMod;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
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
 *   <li><b>Air Gem:</b> Provides a velocity boost (double jump)</li>
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
    public static final long AIR_COOLDOWN_MILLIS = 15_000; // 30 seconds
    public static final long DARKNESS_COOLDOWN_MILLIS = 60_000; // 75 seconds
    public static final long EARTH_COOLDOWN_MILLIS = 70_000; // 70 seconds
    public static final long FIREBALL_COOLDOWN_MILLIS = 20_000; // 30 seconds
    public static final long LIGHT_COOLDOWN_MILLIS = 15_000; // 45 seconds

    // Duration of ability effects
    public static final int INVISIBILITY_DURATION_TICKS = 15 * 20; // 15 seconds in ticks
    public static final int INVULNERABILITY_DURATION_TICKS = 10 * 20; // 10 seconds in ticks


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

        // Check for cooldown
        long remainingCooldown = cooldownManager.getRemainingCooldown(player, GemType.AIR);
        if (remainingCooldown > 0) {
            long secondsLeft = remainingCooldown / 1000;
            logger.info("Air Gem on cooldown for " + player.getName() + ": " + secondsLeft + "s remaining");

            // Check if the player has permission to bypass cooldown
            if (!player.hasPermission("gemmod.cooldown.bypass")) {
                player.sendMessage(Component.text(String.format("Double jump is on cooldown! %ds left.", secondsLeft), NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                logger.info("Air Gem ability denied for " + player.getName() + " due to cooldown");
                return;
            } else {
                logger.info("Player " + player.getName() + " bypassing Air Gem cooldown with permission");
            }
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

        // Check for cooldown
        long remainingCooldown = cooldownManager.getRemainingCooldown(player, GemType.DARKNESS);
        if (remainingCooldown > 0) {
            long secondsLeft = remainingCooldown / 1000;
            logger.info("Darkness Gem on cooldown for " + player.getName() + ": " + secondsLeft + "s remaining");

            // Check if the player has permission to bypass cooldown
            if (!player.hasPermission("gemmod.cooldown.bypass")) {
                player.sendMessage(Component.text("Darkness Gem is on cooldown! " + secondsLeft + "s left.", NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                logger.info("Darkness Gem ability denied for " + player.getName() + " due to cooldown");
                return;
            } else {
                logger.info("Player " + player.getName() + " bypassing Darkness Gem cooldown with permission");
            }
        }

        // Apply invisibility potion effect (no particles; visible icon)
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, INVISIBILITY_DURATION_TICKS, 0, false, false, true));
        logger.info("Applied invisibility effect to " + player.getName() + " for " + (INVISIBILITY_DURATION_TICKS / 20) + " seconds");

        // Create a repeating task to hide player's equipment
        logger.info("Starting equipment hiding task for " + player.getName());
        ScheduledTask playerHideTask = player.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            if (!player.isOnline()) {
                logger.info("Player " + player.getName() + " went offline, cancelling equipment hiding task");
                scheduledTask.cancel();
                return;
            }

            // Hide stuff that makes players visible
            player.setArrowsInBody(0, false);
            player.setBeeStingersInBody(0);
            player.setVisualFire(TriState.FALSE);

            // Hide player's equipment from other players while invisible
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
        }, 1, 1);

        // Set cooldown for Darkness Gem usage
        cooldownManager.setCooldown(player, GemType.DARKNESS, DARKNESS_COOLDOWN_MILLIS, true);
        logger.info("Set Darkness Gem cooldown for " + player.getName() + " for " + (DARKNESS_COOLDOWN_MILLIS / 1000) + " seconds");

        // Schedule task to cancel invisibility and restore equipment after duration
        logger.info("Scheduling invisibility removal task for " + player.getName() + " in " + (INVISIBILITY_DURATION_TICKS / 20) + " seconds");

        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> {

            logger.info("Removing darkness gem effect from " + player.getName());
            playerHideTask.cancel();
            player.removePotionEffect(PotionEffectType.INVISIBILITY);

            player.setVisualFire(TriState.NOT_SET);

            // Restore equipment visibility after invisibility ends
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
        }, INVISIBILITY_DURATION_TICKS);

        // Schedule cooldown removal
        long cooldownTicks = DARKNESS_COOLDOWN_MILLIS / 50;
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
            logger.info("Removing Darkness Gem cooldown for " + player.getName());
            cooldownManager.removeCooldown(player, GemType.DARKNESS);
        }, cooldownTicks);

        // Notify the player that the ability is active
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1.0f, 1.0f);
        player.sendMessage(Component.text(String.format("You are now Invisible for %d seconds!", INVISIBILITY_DURATION_TICKS / 20), TextColor.fromHexString("#12375e")));
        logger.info("Darkness Gem ability successfully activated for " + player.getName());
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

        // Check for cooldown
        long remainingCooldown = cooldownManager.getRemainingCooldown(player, GemType.EARTH);
        if (remainingCooldown > 0) {
            long secondsLeft = remainingCooldown / 1000;
            logger.info("Earth Gem on cooldown for " + player.getName() + ": " + secondsLeft + "s remaining");

            // Check if the player has permission to bypass cooldown
            if (!player.hasPermission("gemmod.cooldown.bypass")) {
                player.sendMessage(Component.text(String.format("Damage Resistance is on cooldown! %ds left.", secondsLeft)).color(NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                logger.info("Earth Gem ability denied for " + player.getName() + " due to cooldown");
                return;
            } else {
                logger.info("Player " + player.getName() + " bypassing Earth Gem cooldown with permission");
            }
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

        // Check for cooldown
        long remainingCooldown = cooldownManager.getRemainingCooldown(player, GemType.FIRE);
        if (remainingCooldown > 0) {
            long secondsLeft = remainingCooldown / 1000;
            logger.info("Fire Gem on cooldown for " + player.getName() + ": " + secondsLeft + "s remaining");

            // Check if the player has permission to bypass cooldown
            if (!player.hasPermission("gemmod.cooldown.bypass")) {
                player.sendMessage(Component.text(String.format("Fireball is on cooldown! %ds left.", secondsLeft), NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                logger.info("Fire Gem ability denied for " + player.getName() + " due to cooldown");
                return;
            } else {
                logger.info("Player " + player.getName() + " bypassing Fire Gem cooldown with permission");
            }
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

        // Check for cooldown
        long remainingCooldown = cooldownManager.getRemainingCooldown(player, GemType.LIGHT);
        if (remainingCooldown > 0) {
            long secondsLeft = remainingCooldown / 1000;
            logger.info("Light Gem on cooldown for " + player.getName() + ": " + secondsLeft + "s remaining");

            // Check if the player has permission to bypass cooldown
            if (!player.hasPermission("gemmod.cooldown.bypass")) {
                player.sendMessage(Component.text(String.format("Light Gem is on cooldown! %ds left.", secondsLeft), NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                logger.info("Light Gem ability denied for " + player.getName() + " due to cooldown");
                return;
            } else {
                logger.info("Player " + player.getName() + " bypassing Light Gem cooldown with permission");
            }
        }

        // Check if the player has a target
        if (player.getTargetEntity(120) != null && player.getTargetEntity(120) instanceof LivingEntity targetEntity) {
            logger.info("Player " + player.getName() + " targeting player " + targetEntity.getName() + " with Light Gem");
        } else {
            player.sendMessage(Component.text("You must be looking at another player/mob to use the Light Gem!", NamedTextColor.RED));
            logger.info("No valid target found for " + player.getName() + " to use Light Gem");
            return;
        }

        logger.info("Player " + player.getName() + " targeting player " + targetEntity.getName() + " with Light Gem");

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

}
