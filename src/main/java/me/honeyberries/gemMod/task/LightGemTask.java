package me.honeyberries.gemMod.task;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import me.honeyberries.gemMod.util.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Manages the passive glowing effect of the Light Gem.
 * <p>
 * This task runs periodically to give players holding a Light Gem the ability
 * to see other players through walls by applying a glowing effect that is only
 * visible to them.
 *
 * @author HoneyBerries
 * @version 1.0
 */
public class LightGemTask {

    /**
     * A reference to the main plugin instance.
     */
    private static final GemMod plugin = GemMod.getInstance();


    /**
     * Starts a recurring task that manages the Light Gem's glowing effect.
     * <p>
     * The task runs every second to check all online players. If a player is holding
     * a Light Gem, it makes all other players glow for them. If they are not,
     * it removes any glowing effects they may have been seeing.
     */
    public static void startLightGemTask() {
        LogUtil.verbose("Starting Light Gem passive effect task");

        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                boolean hasLightGem = GemManager.hasGem(player, GemType.LIGHT);

                // Apply or remove glowing effects for other players
                for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                    if (otherPlayer.equals(player)) continue;

                    setPlayerGlowing(player, otherPlayer, hasLightGem);
                }
            }
        }, 20L, 20L);

        LogUtil.verbose("Light Gem passive effect task started successfully");
    }

    /**
     * Sets the glowing status of a target player for a specific viewer.
     *
     * @param viewer  The player who will see the glowing effect.
     * @param target  The player who will be made to glow.
     * @param glowing {@code true} to enable glowing, {@code false} to disable it.
     */
    private static void setPlayerGlowing(final Player viewer, final Player target, final boolean glowing) {
        final EntityData<@NotNull Byte> entityData = getPlayerEntityData(target, glowing);
        final List<EntityData<?>> metadata = List.of(entityData);

        final int id = target.getEntityId();
        final WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(id, metadata);

        final PacketEventsAPI<?> packetEvents = PacketEvents.getAPI();
        final PlayerManager manager = packetEvents.getPlayerManager();
        manager.sendPacket(viewer, packet);
      }

    /**
     * Creates the entity metadata required to toggle the glowing effect.
     *
     * @param player  The player whose metadata flags are being modified.
     * @param glowing {@code true} to set the glowing flag, {@code false} to unset it.
     * @return A new {@link EntityData} object with the updated flags.
     */
    private static @NotNull EntityData<@NotNull Byte> getPlayerEntityData(Player player, boolean glowing) {

        final byte flags = (byte) ((player.getFireTicks() > 0 ? 0x01 : 0) |
                (player.isSneaking() ? 0x02 : 0) | (player.isSprinting() ? 0x08 : 0) |
                (player.isSwimming() ? 0x10 : 0) |
                (player.isInvisible() ? 0x20 : 0) | (glowing ? 0x40 : 0) |
                (player.isGliding() ? 0x80 : 0));
        return new EntityData<>(0, EntityDataTypes.BYTE, flags);

    }
}