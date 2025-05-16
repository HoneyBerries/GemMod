package me.honeyberries.gemMod.manager;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.honeyberries.gemMod.GemMod;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static me.honeyberries.gemMod.manager.GemManager.identifyGemType;
import me.honeyberries.gemMod.manager.GemManager.GemType;

/**
 * <b>CooldownManager</b> manages all gem cooldowns and updates the player's action bar with cooldown information.
 * Handles thread-safe operations and scheduled updates for cooldowns.
 * </p>
 */
public class CooldownManager {

    /** <b>Singleton instance</b> */
    private static final CooldownManager INSTANCE = new CooldownManager();

    /** <b>Reference to the main plugin instance</b> */
    private final GemMod plugin = GemMod.getInstance();

    /**
     * <b>Mapping of player UUIDs to their gem cooldown expiry times.</b>
     */
    private final Map<UUID, Map<GemType, Long>> cooldowns = new ConcurrentHashMap<>();

    /**
     * <b>Stores scheduled tasks for updating action bars.</b>
     */
    private final Map<UUID, ScheduledTask> actionBarTasks = new ConcurrentHashMap<>();

    /**
     * Returns the singleton instance of the CooldownManager.
     * </p>
     *
     * @return the active CooldownManager instance.
     */
    public static synchronized CooldownManager getInstance() {
        return INSTANCE;
    }

    /**
     * Sets a cooldown for a player and gem type while optionally showing the cooldown timer.
     * </p>
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
            // <i>Show cooldown in action bar if requested</i>
            showCooldownActionBar(player);
        }
    }

    /**
     * Removes the cooldown for the given player and gem type.
     * Also cancels any active action bar display for that gem.
     * </p>
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
        // <i>Clear action bar if the cooldown gem is in use</i>
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (gemType == identifyGemType(heldItem)) {
            cancelActionBarTask(player);
            player.sendActionBar(Component.empty());
        }
    }

    /**
     * Determines if the player is currently on cooldown for the specified gem type.
     * </p>
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
     * </p>
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
     * </p>
     *
     * @param player the player who switched slots
     * @param newItem the new item in the hotbar slot
     */
    public void handleHotbarSwitch(Player player, ItemStack newItem) {
        GemType gemType = identifyGemType(newItem);
        if (gemType != null && isOnCooldown(player, gemType)) {
            // <i>Show or clear cooldown action bar based on held item</i>
            showCooldownActionBar(player);
        } else {
            cancelActionBarTask(player);
            player.sendActionBar(Component.empty());
        }
    }

    /**
     * Cancels the scheduled action bar update task for the player.
     * </p>
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
     * </p>
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
                    // <i>Update action bar with cooldown timer, cancel when done</i>
                    player.sendActionBar(Component.text(String.format("Cooldown: %ds", seconds), NamedTextColor.GOLD));
                },
                null,
                1, 1
        );
        actionBarTasks.put(uuid, task);
    }
}
