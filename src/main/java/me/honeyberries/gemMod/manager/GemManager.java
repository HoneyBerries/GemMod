package me.honeyberries.gemMod.manager;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.format.TextColor;

/**
 * Manages the creation and identification of custom gem items.
 * This utility class provides methods to create gems with specific attributes and to check for their presence in a player's inventory.
 */
public class GemManager {

    /**
     * Represents the different types of gems available in the plugin.
     */
    public enum GemType {
        AIR,
        DARKNESS,
        EARTH,
        FIRE,
        ICE,
        LIGHT,
        WATER
    }

    /**
     * Checks if a player has at least one gem of a specific type in their inventory.
     *
     * @param player  The player whose inventory is to be checked.
     * @param gemType The type of gem to look for.
     * @return {@code true} if the player has the gem, {@code false} otherwise.
     */
    public static boolean hasGem(Player player, GemType gemType) {
        ItemStack gemItem = createGem(gemType, 1);
        return player.getInventory().containsAtLeast(gemItem, 1);
    }

    /**
     * Checks if the player is holding a specific type of gem in their main hand.
     *
     * @param player  The player to check.
     * @param gemType The type of gem to verify.
     * @return {@code true} if the player is holding the specified gem, {@code false} otherwise.
     */
    public static boolean isHoldingGem(@NotNull Player player, @NotNull GemType gemType) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        return identifyGemType(heldItem) == gemType;
    }

    /**
     * Identifies the type of a gem based on its item properties.
     *
     * @param item The item to identify.
     * @return The {@link GemType} if the item is a valid gem, or {@code null} if it is not.
     */
    public static @Nullable GemType identifyGemType(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }

        // Iterate through each gem type to check if the item matches.
        for (GemType gemType : GemType.values()) {
            if (item.isSimilar(createGem(gemType, 1))) {
                return gemType;
            }
        }
        // Return null if no match is found.
        return null;
    }

    /**
     * Creates a custom gem {@link ItemStack} with predefined properties such as name, lore, and enchantments.
     *
     * @param gemType The type of gem to create.
     * @param amount  The number of gems to create.
     * @return A new {@link ItemStack} representing the specified gem.
     */
    public static ItemStack createGem(GemType gemType, int amount) {
        ItemStack gemItemStack = new ItemStack(Material.DIAMOND, amount);
        ItemMeta gemItemMeta = Objects.requireNonNull(gemItemStack.getItemMeta());

        String customModelKey = null;

        // Set gem properties based on its type.
        switch (gemType) {
            case AIR -> {
                gemItemMeta.itemName(Component.text("Air Gem").color(TextColor.fromHexString("#f7ded1")));
                gemItemMeta.lore(List.of(
                        Component.text("Grants immunity to fall and flight damage.").color(TextColor.fromHexString("#ddfff8")),
                        Component.text("Use this gem to dash forward with a burst of wind.").color(TextColor.fromHexString("#e7e3ff"))
                ));
                customModelKey = "airgem";
            }
            case DARKNESS -> {
                gemItemMeta.itemName(Component.text("Darkness Gem").color(TextColor.fromHexString("#3c2c90")));
                gemItemMeta.lore(List.of(
                        Component.text("Become truly invisible, not showing armor or items.").color(TextColor.fromHexString("#1b46cb")),
                        Component.text("Give players darkness and cover up their screens with shadow particles.").color(TextColor.fromHexString("#542285"))
                ));
                customModelKey = "darknessgem";
            }
            case EARTH -> {
                gemItemMeta.itemName(Component.text("Earth Gem").color(TextColor.fromHexString("#3fd41e")));
                gemItemMeta.lore(List.of(
                        Component.text("Provides Haste II, Speed II, and Strength II.").color(TextColor.fromHexString("#4cd69b")),
                        Component.text("This ability negates damage for a short period of time.").color(TextColor.fromHexString("#72d132"))
                ));
                customModelKey = "earthgem";
            }
            case FIRE -> {
                gemItemMeta.itemName(Component.text("Fire Gem").color(TextColor.fromHexString("#ff6a2b")));
                gemItemMeta.lore(List.of(
                        Component.text("Grants Fire Resistance.").color(TextColor.fromHexString("#dc4772")),
                        Component.text("Use the gem to launch a fireball.").color(TextColor.fromHexString("#ee3d24"))
                ));
                customModelKey = "firegem";
            }
            case ICE -> {
                gemItemMeta.itemName(Component.text("Ice Gem").color(TextColor.fromHexString("#5fe3ff")));
                gemItemMeta.lore(List.of(
                        Component.text("Slows nearby foes and reduces damage taken.").color(TextColor.fromHexString("#cedfff")),
                        Component.text("Right-click to freeze targets in place.").color(TextColor.fromHexString("#a4acff"))
                ));
                customModelKey = "icegem";
            }
            case LIGHT -> {
                gemItemMeta.itemName(Component.text("Light Gem").color(TextColor.fromHexString("#ffff81")));
                gemItemMeta.lore(List.of(
                        Component.text("Lets you see players through walls.").color(TextColor.fromHexString("#eee27b")),
                        Component.text("Activate the gem on a player to strike lightning on them.").color(TextColor.fromHexString("#eec04d"))
                ));
                customModelKey = "lightgem";
            }
            case WATER -> {
                gemItemMeta.itemName(Component.text("Water Gem").color(TextColor.fromHexString("#2373E0")));
                gemItemMeta.lore(List.of(
                        Component.text("Gives Water Breathing and Dolphin’s Grace.").color(TextColor.fromHexString("#b9daff")),
                        Component.text("Right-click to fire a high-pressure water jet.").color(TextColor.fromHexString("#40c7ff"))
                ));
                customModelKey = "watergem";
            }
        }

        // Add a cosmetic enchantment and hide it from the tooltip.
        gemItemMeta.addEnchant(Enchantment.MENDING, 1, true);
        gemItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        // Apply the metadata to the item.
        gemItemStack.setItemMeta(gemItemMeta);

        if (customModelKey != null) {
            CustomModelData customModelData = CustomModelData.customModelData().addString(customModelKey).build();
            gemItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);
        }

        return gemItemStack;
    }
}