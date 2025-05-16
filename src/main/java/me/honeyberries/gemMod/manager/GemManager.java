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
 * <b>GemManager</b> is a utility class for creating and managing custom gem items with specific properties.
 * </p>
 */
public class GemManager {

    /**
     * Enum representing the various types of gems available.
     * </p>
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
     * Checks if the specified player has at least one gem of the given type in their inventory.
     * </p>
     *
     * @param player  the player whose inventory to check
     * @param gemType the type of gem to look for
     * @return true if the player has at least one gem of the specified type, false otherwise
     */
    public static boolean hasGem(Player player, GemType gemType) {
        ItemStack gemItem = createGem(gemType, 1);
        return player.getInventory().containsAtLeast(gemItem, 1);
    }

    /**
     * Checks if the player is currently holding a gem of the specified type in their main hand.
     * </p>
     *
     * @param player  the player to check
     * @param gemType the type of gem to verify
     * @return true if the held item matches the specified gem type, false otherwise
     */
    public static boolean isHoldingGem(@NotNull Player player, @NotNull GemType gemType) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        return identifyGemType(heldItem) == gemType;
    }

    /**
     * Identifies the gem type of a given item based on its properties.
     * </p>
     *
     * @param item the item to identify
     * @return the GemType if the item is a gem; null if the item is not a gem
     */
    public static @Nullable GemType identifyGemType(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }

        // <i>Iterate through each gem type to check if the item matches</i>
        for (GemType gemType : GemType.values()) {
            if (item.isSimilar(createGem(gemType, 1))) {
                return gemType;
            }
        }
        // <i>Return null if no match is found</i>
        return null;
    }

    /**
     * Creates a customized gem item of the specified type and amount.
     * Sets properties such as name, lore, custom model data, and enchantments.
     * </p>
     *
     * @param gemType the type of gem to create
     * @param amount  the quantity of the item to generate
     * @return an ItemStack representing the customized gem
     */
    public static ItemStack createGem(GemType gemType, int amount) {
        ItemStack gemItemStack = new ItemStack(Material.DIAMOND, amount);
        ItemMeta gemItemMeta = Objects.requireNonNull(gemItemStack.getItemMeta());

        String customModelKey = null;

        // <i>Set gem name, lore, and custom model key based on type</i>
        switch (gemType) {
            case AIR -> {
                gemItemMeta.itemName(Component.text("Aero Gem").color(TextColor.fromHexString("#f7ded1")));
                gemItemMeta.lore(List.of(
                        Component.text("Grants immunity to fall and flight damage.").color(TextColor.fromHexString("#ddfff8")),
                        Component.text("Use this gem to dash forward with a burst of wind.").color(TextColor.fromHexString("#e7e3ff"))
                ));
                customModelKey = "airgem";
            }
            case DARKNESS -> {
                gemItemMeta.itemName(Component.text("Shadow Gem").color(TextColor.fromHexString("#3c2c90")));
                gemItemMeta.lore(List.of(
                        Component.text("Become truly invisible, not showing armor or items.").color(TextColor.fromHexString("#1b46cb")),
                        Component.text("Give players darkness and cover up their screens with shadow particles.").color(TextColor.fromHexString("#542285"))
                ));
                customModelKey = "darknessgem";
            }
            case EARTH -> {
                gemItemMeta.itemName(Component.text("Dendro Gem").color(TextColor.fromHexString("#3fd41e")));
                gemItemMeta.lore(List.of(
                        Component.text("Provides Haste II, Speed II, and Strength II.").color(TextColor.fromHexString("#4cd69b")),
                        Component.text("This ability negates damage for a short period of time.").color(TextColor.fromHexString("#72d132"))
                ));
                customModelKey = "earthgem";
            }
            case FIRE -> {
                gemItemMeta.itemName(Component.text("Pyro Gem").color(TextColor.fromHexString("#ff6a2b")));
                gemItemMeta.lore(List.of(
                        Component.text("Grants Fire Resistance.").color(TextColor.fromHexString("#dc4772")),
                        Component.text("Use the gem to launch a fireball.").color(TextColor.fromHexString("#ee3d24"))
                ));
                customModelKey = "firegem";
            }
            case ICE -> {
                gemItemMeta.itemName(Component.text("Cryo Gem").color(TextColor.fromHexString("#5fe3ff")));
                gemItemMeta.lore(List.of(
                        Component.text("Slows nearby foes and reduces damage taken.").color(TextColor.fromHexString("#cedfff")),
                        Component.text("Right-click to freeze targets in place.").color(TextColor.fromHexString("#a4acff"))
                ));
                customModelKey = "icegem";
            }
            case LIGHT -> {
                gemItemMeta.itemName(Component.text("Photo Gem").color(TextColor.fromHexString("#ffff81")));
                gemItemMeta.lore(List.of(
                        Component.text("Lets you see players through walls.").color(TextColor.fromHexString("#eee27b")),
                        Component.text("Activate the gem on a player to strike lightning on them.").color(TextColor.fromHexString("#eec04d"))
                ));
                customModelKey = "lightgem";
            }
            case WATER -> {
                gemItemMeta.itemName(Component.text("Hydro Gem").color(TextColor.fromHexString("#2373E0")));
                gemItemMeta.lore(List.of(
                        Component.text("Gives Water Breathing and Dolphin’s Grace.").color(TextColor.fromHexString("#b9daff")),
                        Component.text("Right-click to fire a high-pressure water jet.").color(TextColor.fromHexString("#40c7ff"))
                ));
                customModelKey = "watergem";
            }
        }

        // Add enchantments and flags
        gemItemMeta.addEnchant(Enchantment.MENDING, 1, true);
        gemItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        // <i>Apply meta and custom model data</i>
        gemItemStack.setItemMeta(gemItemMeta);

        if (customModelKey != null) {
            CustomModelData customModelData = CustomModelData.customModelData().addString(customModelKey).build();
            gemItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);
        }

        return gemItemStack;
    }
}