package me.honeyberries.gemMod.manager;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

/**
 * Utility class for creating and managing custom gem items with specific properties.
 */
public class GemManager {

    /**
     * Enum representing the various types of gems available.
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
     *
     * @param item the item to identify
     * @return the GemType if the item is a gem; null if the item is not a gem
     */
    public static @Nullable GemType identifyGemType(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }

        // Iterate through each gem type to check if the item matches
        for (GemType gemType : GemType.values()) {
            if (item.isSimilar(createGem(gemType, 1))) {
                return gemType;
            }
        }
        // Return null if no match is found
        return null;
    }

    /**
     * Creates a customized gem item of the specified type and amount.
     * Sets properties such as name, lore, custom model data, and enchantments.
     *
     * @param gemType the type of gem to create
     * @param amount  the quantity of the item to generate
     * @return an ItemStack representing the customized gem
     */
    public static ItemStack createGem(GemType gemType, int amount) {
        // Initialize item as a diamond (can be changed to a different material if desired)
        ItemStack gemItemStack = new ItemStack(Material.DIAMOND, amount);
        ItemMeta gemItemMeta = Objects.requireNonNull(gemItemStack.getItemMeta());

        // Configure properties based on the gem type
        switch (gemType) {
            case AIR -> {
                gemItemMeta.itemName(Component.text("Aero Gem").color(NamedTextColor.AQUA));
                List<Component> lore = List.of(
                        Component.text("Gem of the Air Elemental"),
                        Component.text("Grants the power of wind and air manipulation."),
                        Component.text("Can be used to create powerful wind spells.")
                );
                gemItemMeta.lore(lore);
                // Assign custom model data for specific rendering
                CustomModelData customModelData = CustomModelData.customModelData().addString("airgem").build();
                gemItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);
            }
            case DARKNESS -> {
                gemItemMeta.itemName(Component.text("Shadow Gem").color(NamedTextColor.DARK_PURPLE));
                List<Component> lore = List.of(
                        Component.text("Gem of the Dark Elemental"),
                        Component.text("Grants the power of darkness to its wielder."),
                        Component.text("Can be used to create powerful shadow-based spells.")
                );
                gemItemMeta.lore(lore);
                CustomModelData customModelData = CustomModelData.customModelData().addString("darknessgem").build();
                gemItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);
            }
            case EARTH -> {
                gemItemMeta.itemName(Component.text("Dendro Gem").color(NamedTextColor.GREEN));
                List<Component> lore = List.of(
                        Component.text("Gem of the Nature Elemental"),
                        Component.text("Grants the power of nature to its wielder."),
                        Component.text("Can be used to create powerful plant-based spells.")
                );
                gemItemMeta.lore(lore);
                CustomModelData customModelData = CustomModelData.customModelData().addString("earthgem").build();
                gemItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);
            }
            case FIRE -> {
                gemItemMeta.itemName(Component.text("Pyro Gem").color(NamedTextColor.RED));
                List<Component> lore = List.of(
                        Component.text("Gem of the Fire Elemental"),
                        Component.text("Grants the power of fire to its wielder."),
                        Component.text("Can be used to create powerful fire-based spells.")
                );
                gemItemMeta.lore(lore);
                CustomModelData customModelData = CustomModelData.customModelData().addString("firegem").build();
                gemItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);
            }
            case ICE -> {
                gemItemMeta.itemName(Component.text("Cryo Gem").color(NamedTextColor.AQUA));
                List<Component> lore = List.of(
                        Component.text("Gem of the Ice Elemental"),
                        Component.text("Grants the power of frost to its wielder."),
                        Component.text("Can be used to create powerful ice-based spells.")
                );
                gemItemMeta.lore(lore);
                CustomModelData customModelData = CustomModelData.customModelData().addString("icegem").build();
                gemItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);
            }
            case LIGHT -> {
                gemItemMeta.itemName(Component.text("Photo Gem").color(NamedTextColor.YELLOW));
                List<Component> lore = List.of(
                        Component.text("Gem of the Light Elemental"),
                        Component.text("Grants the power of light to its wielder."),
                        Component.text("Can be used to create powerful light-based spells.")
                );
                gemItemMeta.lore(lore);
                CustomModelData customModelData = CustomModelData.customModelData().addString("lightgem").build();
                gemItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);
            }
            case WATER -> {
                gemItemMeta.itemName(Component.text("Hydro Gem").color(NamedTextColor.BLUE));
                List<Component> lore = List.of(
                        Component.text("Gem of the Water Elemental"),
                        Component.text("Grants the power of water to its wielder."),
                        Component.text("Can be used to create powerful water-based spells.")
                );
                gemItemMeta.lore(lore);
                CustomModelData customModelData = CustomModelData.customModelData().addString("watergem").build();
                gemItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);
            }
        }

        // Add an enchantment for visual effect and hide it for aesthetic purposes
        gemItemMeta.addEnchant(Enchantment.MENDING, 1, true);
        gemItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        // Apply the meta data to the item
        gemItemStack.setItemMeta(gemItemMeta);

        return gemItemStack;
    }
}