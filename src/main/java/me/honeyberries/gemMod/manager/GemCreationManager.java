package me.honeyberries.gemMod.manager;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;

/**
 * Utility class for creating gem items with unique names, lore, and custom model data.
 */
public class GemCreationManager {

    /**
     * Creates an Air Gem item.
     *
     * @param amount number of items to create
     * @return configured ItemStack for an Air Gem
     */
    public static ItemStack createAirGem(int amount) {
        ItemStack airItemStack = new ItemStack(Material.DIAMOND, amount);
        ItemMeta airItemMeta = Objects.requireNonNull(airItemStack.getItemMeta());
        airItemMeta.itemName(Component.text("Aero Gem").color(NamedTextColor.AQUA));
        List<Component> lore = List.of(
                Component.text("Gem of the Air Elemental"),
                Component.text("Grants the power of wind and air manipulation."),
                Component.text("Can be used to create powerful wind spells.")
        );
        airItemMeta.lore(lore);
        airItemMeta.addEnchant(Enchantment.MENDING, 1, true);
        airItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        airItemStack.setItemMeta(airItemMeta);

        // Set custom model data for resource pack support
        CustomModelData customModelData = CustomModelData.customModelData().addString("airgem").build();
        airItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);

        return airItemStack;
    }

    /**
     * Creates a Darkness Gem item.
     *
     * @param amount number of items to create
     * @return configured ItemStack for a Darkness Gem
     */
    public static ItemStack createDarknessGem(int amount) {
        ItemStack darknessItemStack = new ItemStack(Material.DIAMOND, amount);
        ItemMeta darknessItemMeta = Objects.requireNonNull(darknessItemStack.getItemMeta());
        darknessItemMeta.itemName(Component.text("Shadow Gem").color(NamedTextColor.DARK_PURPLE));
        List<Component> lore = List.of(
                Component.text("Gem of the Dark Elemental"),
                Component.text("Grants the power of darkness to its wielder."),
                Component.text("Can be used to create powerful shadow-based spells.")
        );
        darknessItemMeta.lore(lore);
        darknessItemMeta.addEnchant(Enchantment.MENDING, 1, true);
        darknessItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        darknessItemStack.setItemMeta(darknessItemMeta);

        // Set custom model data for client-side customizations
        CustomModelData customModelData = CustomModelData.customModelData().addString("darknessgem").build();
        darknessItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);

        return darknessItemStack;
    }

    /**
     * Creates an Earth Gem item.
     *
     * @param amount number of items to create
     * @return configured ItemStack for an Earth Gem
     */
    public static ItemStack createEarthGem(int amount) {
        ItemStack earthItemStack = new ItemStack(Material.DIAMOND, amount);
        ItemMeta earthItemMeta = Objects.requireNonNull(earthItemStack.getItemMeta());
        earthItemMeta.itemName(Component.text("Dendro Gem").color(NamedTextColor.GREEN));
        List<Component> lore = List.of(
                Component.text("Gem of the Nature Elemental"),
                Component.text("Grants the power of nature to its wielder."),
                Component.text("Can be used to create powerful plant-based spells.")
        );
        earthItemMeta.lore(lore);
        earthItemMeta.addEnchant(Enchantment.MENDING, 1, true);
        earthItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        earthItemStack.setItemMeta(earthItemMeta);

        CustomModelData customModelData = CustomModelData.customModelData().addString("earthgem").build();
        earthItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);

        return earthItemStack;
    }

    /**
     * Creates a Fire Gem item.
     *
     * @param amount number of items to create
     * @return configured ItemStack for a Fire Gem
     */
    public static ItemStack createFireGem(int amount) {
        ItemStack fireItemStack = new ItemStack(Material.DIAMOND, amount);
        ItemMeta fireItemMeta = Objects.requireNonNull(fireItemStack.getItemMeta());
        fireItemMeta.itemName(Component.text("Pyro Gem").color(NamedTextColor.RED));
        List<Component> lore = List.of(
                Component.text("Gem of the Fire Elemental"),
                Component.text("Grants the power of fire to its wielder."),
                Component.text("Can be used to create powerful fire-based spells.")
        );
        fireItemMeta.lore(lore);
        fireItemMeta.addEnchant(Enchantment.MENDING, 1, true);
        fireItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        fireItemStack.setItemMeta(fireItemMeta);

        CustomModelData customModelData = CustomModelData.customModelData().addString("firegem").build();
        fireItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);

        return fireItemStack;
    }

    /**
     * Creates an Ice Gem item.
     *
     * @param amount number of items to create
     * @return configured ItemStack for an Ice Gem
     */
    public static ItemStack createIceGem(int amount) {
        ItemStack iceItemStack = new ItemStack(Material.DIAMOND, amount);
        ItemMeta iceItemMeta = Objects.requireNonNull(iceItemStack.getItemMeta());
        iceItemMeta.itemName(Component.text("Cryo Gem").color(NamedTextColor.AQUA));
        List<Component> lore = List.of(
                Component.text("Gem of the Ice Elemental"),
                Component.text("Grants the power of frost to its wielder."),
                Component.text("Can be used to create powerful ice-based spells.")
        );
        iceItemMeta.lore(lore);
        iceItemMeta.addEnchant(Enchantment.MENDING, 1, true);
        iceItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        iceItemStack.setItemMeta(iceItemMeta);

        CustomModelData customModelData = CustomModelData.customModelData().addString("icegem").build();
        iceItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);

        return iceItemStack;
    }

    /**
     * Creates a Light Gem item.
     *
     * @param amount number of items to create
     * @return configured ItemStack for a Light Gem
     */
    public static ItemStack createLightGem(int amount) {
        ItemStack photoItemStack = new ItemStack(Material.DIAMOND, amount);
        ItemMeta photoItemMeta = Objects.requireNonNull(photoItemStack.getItemMeta());
        photoItemMeta.itemName(Component.text("Photo Gem").color(NamedTextColor.YELLOW));
        List<Component> lore = List.of(
                Component.text("Gem of the Light Elemental"),
                Component.text("Grants the power of light to its wielder."),
                Component.text("Can be used to create powerful light-based spells.")
        );
        photoItemMeta.lore(lore);
        photoItemMeta.addEnchant(Enchantment.MENDING, 1, true);
        photoItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        photoItemStack.setItemMeta(photoItemMeta);

        CustomModelData customModelData = CustomModelData.customModelData().addString("lightgem").build();
        photoItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);

        return photoItemStack;
    }

    /**
     * Creates a Water Gem item.
     *
     * @param amount number of items to create
     * @return configured ItemStack for a Water Gem
     */
    public static ItemStack createWaterGem(int amount) {
        ItemStack waterItemStack = new ItemStack(Material.DIAMOND, amount);
        ItemMeta waterItemMeta = Objects.requireNonNull(waterItemStack.getItemMeta());
        waterItemMeta.itemName(Component.text("Hydro Gem").color(NamedTextColor.BLUE));
        List<Component> lore = List.of(
                Component.text("Gem of the Water Elemental"),
                Component.text("Grants the power of water to its wielder."),
                Component.text("Can be used to create powerful water-based spells.")
        );
        waterItemMeta.lore(lore);
        waterItemMeta.addEnchant(Enchantment.MENDING, 1, true);
        waterItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        waterItemStack.setItemMeta(waterItemMeta);

        CustomModelData customModelData = CustomModelData.customModelData().addString("watergem").build();
        waterItemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);

        return waterItemStack;
    }
}
