package me.honeyberries.gemMod.manager;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.honeyberries.gemMod.GemMod;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all gem cooldowns and updates the player's action bar with cooldown information.
 * Handles thread-safe operations and scheduled updates.
 */
public class CooldownManager {

    /**
     * Enum representing available gem types.
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

    /** Singleton instance */
    private static final CooldownManager INSTANCE = new CooldownManager();

    /** Reference to the main plugin instance */
    private final GemMod plugin = GemMod.getInstance();

    /**
     * Mapping of player UUIDs to their gem cooldown expiry times.
     */
    private final Map<UUID, Map<GemType, Long>> cooldowns = new ConcurrentHashMap<>();

    /**
     * Stores scheduled tasks for updating action bars.
     */
    private final Map<UUID, ScheduledTask> actionBarTasks = new ConcurrentHashMap<>();

    /**
     * Determines the gem type for a given item.
     *
     * @param item the item to identify
     * @return the GemType if found; null otherwise
     */
    public @Nullable GemType identifyGemType(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }
        return switch (item) {
            case ItemStack ignored when item.isSimilar(GemCreationManager.createAirGem(1)) -> GemType.AIR;
            case ItemStack ignored when item.isSimilar(GemCreationManager.createDarknessGem(1)) -> GemType.DARKNESS;
            case ItemStack ignored when item.isSimilar(GemCreationManager.createEarthGem(1)) -> GemType.EARTH;
            case ItemStack ignored when item.isSimilar(GemCreationManager.createFireGem(1)) -> GemType.FIRE;
            case ItemStack ignored when item.isSimilar(GemCreationManager.createIceGem(1)) -> GemType.ICE;
            case ItemStack ignored when item.isSimilar(GemCreationManager.createLightGem(1)) -> GemType.LIGHT;
            case ItemStack ignored when item.isSimilar(GemCreationManager.createWaterGem(1)) -> GemType.WATER;
            default -> null;
        };
    }

    /**
     * Checks if a player has at least one gem of the specified type in their inventory.
     *
     * @param player the player to check
     * @param gemType the gem type to look for
     * @return true if the gem exists; false otherwise
     */
    public boolean hasGem(Player player, GemType gemType) {
        ItemStack gemItem = switch (gemType) {
            case AIR -> GemCreationManager.createAirGem(1);
            case DARKNESS -> GemCreationManager.createDarknessGem(1);
            case EARTH -> GemCreationManager.createEarthGem(1);
            case FIRE -> GemCreationManager.createFireGem(1);
            case ICE -> GemCreationManager.createIceGem(1);
            case LIGHT -> GemCreationManager.createLightGem(1);
            case WATER -> GemCreationManager.createWaterGem(1);
        };
        return player.getInventory().containsAtLeast(gemItem, 1);
    }

    /**
     * Checks if the player is holding the specified gem in their main hand.
     *
     * @param player the player to check
     * @param gemType the gem type being checked
     * @return true if the item matches the gem type; false otherwise
     */
    public boolean isHoldingGem(@NotNull Player player, @NotNull GemType gemType) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        return identifyGemType(heldItem) == gemType;
    }

    /**
     * Returns the singleton instance of the CooldownManager.
     *
     * @return the active CooldownManager instance.
     */
    public static synchronized CooldownManager getInstance() {
        return INSTANCE;
    }

    /**
     * Sets a cooldown for a player and gem type while optionally showing the cooldown timer.
     *
     * @param player the player to set the cooldown for
     * @param gemType the gem type to apply the cooldown
     * @param durationMillis duration in milliseconds for the cooldown
     * @param showActionBar whether to display the cooldown in the player's action bar
     */
    public void setCooldown(Player player, GemType gemType, long durationMillis, boolean showActionBar) {
        UUID uuid = player.getUniqueId();
        Map<GemType, Long> playerCooldowns = cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        playerCooldowns.put(gemType, System.currentTimeMillis() + durationMillis);
        if (showActionBar) {
            showCooldownActionBar(player);
        }
    }

    /**
     * Deprecated: Sets a cooldown and shows the action bar.
     *
     * @param player the player to set the cooldown for
     * @param gemType the gem type concerned
     * @param durationMillis cooldown duration in milliseconds
     * @deprecated Use {@link #setCooldown(Player, GemType, long, boolean)} instead.
     */
    @Deprecated
    public void setCooldown(Player player, GemType gemType, long durationMillis) {
        setCooldown(player, gemType, durationMillis, true);
    }

    /**
     * Removes the cooldown for the given player and gem type.
     * Also cancels any active action bar display for that gem.
     *
     * @param player the player to update
     * @param gemType the gem type to remove from cooldown tracking
     */
    public void removeCooldown(Player player, GemType gemType) {
        UUID uuid = player.getUniqueId();
        Map<GemType, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns != null) {
            playerCooldowns.remove(gemType);
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(uuid);
            }
        }
        // Clear action bar if the cooldown gem is in use.
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (gemType == identifyGemType(heldItem)) {
            cancelActionBarTask(player);
            player.sendActionBar(Component.empty());
        }
    }

    /**
     * Determines if the player is currently on cooldown for the specified gem type.
     *
     * @param player the player being checked
     * @param gemType the gem type to evaluate
     * @return true if on cooldown; false otherwise.
     */
    public boolean isOnCooldown(Player player, GemType gemType) {
        return getRemainingCooldown(player, gemType) > 0;
    }

    /**
     * Returns the remaining cooldown time in milliseconds for the specified player and gem type.
     *
     * @param player the player being checked
     * @param gemType the gem type of interest
     * @return remaining time in milliseconds, or 0 if expired.
     */
    public long getRemainingCooldown(Player player, GemType gemType) {
        UUID uuid = player.getUniqueId();
        Map<GemType, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) {
            return 0;
        }
        Long expiry = playerCooldowns.get(gemType);
        long now = System.currentTimeMillis();
        return (expiry != null && now < expiry) ? expiry - now : 0;
    }

    /**
     * Updates the action bar for the player when switching hotbar slots.
     *
     * @param player the player who switched slots
     * @param newItem the new item in the hotbar slot
     */
    public void handleHotbarSwitch(Player player, ItemStack newItem) {
        GemType gemType = identifyGemType(newItem);
        if (gemType != null && isOnCooldown(player, gemType)) {
            showCooldownActionBar(player);
        } else {
            cancelActionBarTask(player);
            player.sendActionBar(Component.empty());
        }
    }

    /**
     * Cancels the scheduled action bar update task for the player.
     *
     * @param player the player whose task should be canceled.
     */
    private void cancelActionBarTask(Player player) {
        UUID uuid = player.getUniqueId();
        ScheduledTask oldTask = actionBarTasks.remove(uuid);
        if (oldTask != null) {
            oldTask.cancel();
        }
    }

    /**
     * Displays and updates the cooldown timer in the player's action bar.
     * The task runs periodically to update the remaining seconds.
     *
     * @param player the player to display the cooldown for.
     */
    public void showCooldownActionBar(Player player) {
        UUID uuid = player.getUniqueId();
        cancelActionBarTask(player);
        ScheduledTask task = player.getScheduler().runAtFixedRate(
                plugin,
                scheduledTask -> {
                    ItemStack currentHeldItem = player.getInventory().getItemInMainHand();
                    GemType gemType = identifyGemType(currentHeldItem);
                    if (gemType == null) {
                        player.sendActionBar(Component.empty());
                        return;
                    }
                    long timeLeft = getRemainingCooldown(player, gemType);
                    if (timeLeft <= 0) {
                        player.sendActionBar(Component.empty());
                        Map<GemType, Long> playerCooldowns = cooldowns.get(uuid);
                        if (playerCooldowns != null) {
                            playerCooldowns.remove(gemType);
                            if (playerCooldowns.isEmpty()) {
                                cooldowns.remove(uuid);
                            }
                        }
                        scheduledTask.cancel();
                        actionBarTasks.remove(uuid);
                        return;
                    }
                    long seconds = timeLeft / 1000;
                    player.sendActionBar(Component.text(String.format("Cooldown: %ds", seconds), NamedTextColor.GOLD));
                },
                null,
                1, 1
        );
        actionBarTasks.put(uuid, task);
    }
}
