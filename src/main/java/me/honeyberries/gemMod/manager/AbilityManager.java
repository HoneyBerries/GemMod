package me.honeyberries.gemMod.manager;

import me.honeyberries.gemMod.GemMod;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Helper class for executing the abilities associated with different gems.
 */
public class AbilityManager {

    // Static references
    private static final CooldownManager cooldownManager = CooldownManager.getInstance();
    private static final GemMod plugin = GemMod.getInstance();

    // Duration and cooldown constants
    private static final long AIR_COOLDOWN_MILLIS = 30_000; // 30 seconds
    private static final long DARKNESS_COOLDOWN_MILLIS = 75_000; // 75 seconds
    private static final int INVISIBILITY_DURATION_TICKS = 15 * 20; // 15 seconds in ticks

    /**
     * Triggers the Air Gem ability, providing the player with a velocity boost.
     *
     * @param player the player using the Air Gem
     */
    public static void handleAirGemAbility(Player player) {
        long remainingCooldown = cooldownManager.getRemainingCooldown(player, CooldownManager.GemType.AIR);
        if (remainingCooldown > 0) {
            long secondsLeft = remainingCooldown / 1000;
            if (!player.hasPermission("gemmod.cooldown.bypass")) {
                player.sendMessage(Component.text(String.format("Double jump is on cooldown! %ds left.", secondsLeft), NamedTextColor.RED));
                return;
            }
        }

        // Calculate boost vector (upward & forward)
        double boostSpeed = 3.0; // Adjust strength as needed
        Vector direction = player.getLocation().getDirection().normalize();
        Vector velocity = direction.multiply(boostSpeed);
        player.setVelocity(velocity);

        // Set cooldown and provide feedback
        cooldownManager.setCooldown(player, CooldownManager.GemType.AIR, AIR_COOLDOWN_MILLIS, true);
        player.sendMessage(Component.text("You used the Double Jump!", NamedTextColor.AQUA));
    }

    /**
     * Triggers the Darkness Gem ability to grant temporary invisibility.
     *
     * @param player the player using the Darkness Gem
     */
    public static void handleDarknessGemAbility(Player player) {
        long remainingCooldown = cooldownManager.getRemainingCooldown(player, CooldownManager.GemType.DARKNESS);
        if (remainingCooldown > 0) {
            long secondsLeft = remainingCooldown / 1000;
            if (!player.hasPermission("gemmod.cooldown.bypass")) {
                player.sendMessage(Component.text("Darkness Gem is on cooldown! " + secondsLeft + "s left.", NamedTextColor.RED));
                return;
            }
        }

        // Apply invisibility potion effect (no particles; visible icon)
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, INVISIBILITY_DURATION_TICKS, 0, false, false, true));

        // Schedule a task to hide player's equipment by sending null equipment to other players
        player.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            if (!player.isOnline()) {
                scheduledTask.cancel(); // Stop if player logs off
                return;
            }
            // Loop through online players and hide player's armor and hand items
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
        }, 1, INVISIBILITY_DURATION_TICKS);

        // Set cooldown for Darkness Gem usage
        cooldownManager.setCooldown(player, CooldownManager.GemType.DARKNESS, DARKNESS_COOLDOWN_MILLIS, true);

        // Schedule a delayed task to restore player's equipment visibility and remove invisibility effect
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> {
            if (!player.isOnline()) {
                return;
            }
            // Remove invisibility and restore equipment for other players
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.isOnline()) {
                    p.sendEquipmentChange(player, EquipmentSlot.HAND, player.getInventory().getItemInMainHand());
                    p.sendEquipmentChange(player, EquipmentSlot.OFF_HAND, player.getInventory().getItemInOffHand());
                    p.sendEquipmentChange(player, EquipmentSlot.HEAD, player.getInventory().getHelmet());
                    p.sendEquipmentChange(player, EquipmentSlot.CHEST, player.getInventory().getChestplate());
                    p.sendEquipmentChange(player, EquipmentSlot.LEGS, player.getInventory().getLeggings());
                    p.sendEquipmentChange(player, EquipmentSlot.FEET, player.getInventory().getBoots());
                }
            }
        }, INVISIBILITY_DURATION_TICKS);

        // Schedule a cooldown removal task to clear the cooldown state after its duration
        long cooldownTicks = DARKNESS_COOLDOWN_MILLIS / 50; // Convert ms to ticks
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
            cooldownManager.removeCooldown(player, CooldownManager.GemType.DARKNESS);
        }, cooldownTicks);

        // Notify the player that the ability is active
        player.sendMessage(Component.text(String.format("You are now Invisible for %d seconds!", INVISIBILITY_DURATION_TICKS / 20), NamedTextColor.DARK_PURPLE));
    }
}
