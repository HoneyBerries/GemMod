package me.honeyberries.gemMod.manager;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.honeyberries.gemMod.GemMod;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import me.honeyberries.gemMod.manager.GemManager.GemType;

/**
 * Helper class for executing the abilities associated with different gems.
 */
public class AbilityManager {

    // Static references
    private static final CooldownManager cooldownManager = CooldownManager.getInstance();
    private static final GemMod plugin = GemMod.getInstance();

    // Duration of cooldown constants
    private static final long AIR_COOLDOWN_MILLIS = 30_000; // 30 seconds
    private static final long DARKNESS_COOLDOWN_MILLIS = 75_000; // 75 seconds
    private static final long EARTH_COOLDOWN_MILLIS = 70_000; // 70 seconds
    private static final long FIREBALL_COOLDOWN_MILLIS = 30_000; // 30 seconds

    // Duration of ability effects
    private static final int INVISIBILITY_DURATION_TICKS = 15 * 20; // 15 seconds in ticks
    private static final int INVULNERABILITY_DURATION_TICKS = 10 * 20; // 10 seconds in ticks


    /**
     * Triggers the Air Gem ability, providing the player with a velocity boost.
     *
     * @param player the player using the Air Gem
     */
    public static void handleAirGemAbility(Player player) {
        long remainingCooldown = cooldownManager.getRemainingCooldown(player, GemType.AIR);
        if (remainingCooldown > 0) {
            long secondsLeft = remainingCooldown / 1000;

            // Check if the player has permission to bypass cooldown
            if (!player.hasPermission("gemmod.cooldown.bypass")) {
                player.sendMessage(Component.text(String.format("Double jump is on cooldown! %ds left.", secondsLeft), NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
        }

        // Calculate boost vector (upward and forward)
        double boostSpeed = 3.0; // Adjust strength as needed
        Vector direction = player.getLocation().getDirection().normalize();
        Vector velocity = direction.multiply(boostSpeed);
        player.setVelocity(velocity);

        // Set cooldown and provide feedback
        cooldownManager.setCooldown(player, GemType.AIR, AIR_COOLDOWN_MILLIS, true);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_THROW, 1.0f, 1.0f);
        player.sendMessage(Component.text("You used the Double Jump!", TextColor.fromHexString("#90e1e1")));
    }

    /**
     * Triggers the Darkness Gem ability to grant temporary invisibility.
     *
     * @param player the player using the Darkness Gem
     */
    public static void handleDarknessGemAbility(Player player) {
        long remainingCooldown = cooldownManager.getRemainingCooldown(player, GemType.DARKNESS);

        // Check if the player has permission to bypass cooldown
        if (remainingCooldown > 0) {
            long secondsLeft = remainingCooldown / 1000;
            if (!player.hasPermission("gemmod.cooldown.bypass")) {
                player.sendMessage(Component.text("Darkness Gem is on cooldown! " + secondsLeft + "s left.", NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
        }

        // Apply invisibility potion effect (no particles; visible icon)
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, INVISIBILITY_DURATION_TICKS, 0, false, false, true));

        // Create a repeating task to hide player's equipment
        ScheduledTask equipmentHideTask = player.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            if (!player.isOnline()) {
                scheduledTask.cancel();
                return;
            }

            // Hide player's equipment from other players
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

        // Schedule task to cancel invisibility and restore equipment after duration
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> {
            equipmentHideTask.cancel();
            player.removePotionEffect(PotionEffectType.INVISIBILITY);

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

        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task ->
                cooldownManager.removeCooldown(player, GemType.DARKNESS), cooldownTicks);

        // Notify the player that the ability is active
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1.0f, 1.0f);
        player.sendMessage(Component.text(String.format("You are now Invisible for %d seconds!", INVISIBILITY_DURATION_TICKS / 20), TextColor.fromHexString("#231e5e")));
    }


    /**
     * Triggers the Earth Gem ability to grant temporary invulnerability.
     *
     * @param player the player using the Earth Gem
     */
    public static void handleEarthGemAbility(Player player) {
        long remainingCooldown = cooldownManager.getRemainingCooldown(player, GemType.EARTH);
        if (remainingCooldown > 0) {
            long secondsLeft = remainingCooldown / 1000;

            // Check if the player has permission to bypass cooldown
            if (!player.hasPermission("gemmod.cooldown.bypass")) {
                player.sendMessage(Component.text(String.format("Damage Resistance is on cooldown! %ds left.", secondsLeft)).color(NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
        }

        // Give resistance 255
        player.getScheduler().run(plugin, scheduledTask -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, INVULNERABILITY_DURATION_TICKS, 254, false, true, true));
        }, null);

        // Set cooldown for Earth Gem usage
        cooldownManager.setCooldown(player, GemType.EARTH, EARTH_COOLDOWN_MILLIS, true);

        // Provide feedback to the player
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendMessage(Component.text(String.format("You are now invulnerable for %d seconds!", INVULNERABILITY_DURATION_TICKS / 20), TextColor.fromHexString("#3ad422")));
    }


    public static void handleFireGemAbility(Player player) {
        long remainingCooldown = cooldownManager.getRemainingCooldown(player, GemType.FIRE);
        if (remainingCooldown > 0) {
            long secondsLeft = remainingCooldown / 1000;

            // Check if the player has permission to bypass cooldown
            if (!player.hasPermission("gemmod.cooldown.bypass")) {
                player.sendMessage(Component.text(String.format("Fireball is on cooldown! %ds left.", secondsLeft), NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
        }

        // Fireball logic here

        // Create a fireball entity and launch it
        Fireball fireball = player.launchProjectile(Fireball.class, player.getLocation().getDirection().multiply(2));
        fireball.setIsIncendiary(true);
        fireball.setYield(4F); // Adjust explosion power as needed


        // Set cooldown for Fire Gem usage
        cooldownManager.setCooldown(player, GemType.FIRE, FIREBALL_COOLDOWN_MILLIS, true);

        // Provide feedback to the player
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
        player.sendMessage(Component.text("You threw a Fireball!").color(TextColor.fromHexString("#f0590e")));
    }


}
