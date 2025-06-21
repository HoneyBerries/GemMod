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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Logger;

/**
 * Manages the passive ability of the Light Gem, which makes other players glow.
 *
 * <p>The Light Gem provides two abilities:</p>
 * <ul>
 *   <li><b>Active ability:</b> Strike a targeted player with lightning (handled by {@code AbilityManager})</li>
 *   <li><b>Passive ability:</b> Make all other players glow with an aqua outline (handled by this class)</li>
 * </ul>
 *
 * <p>The passive ability allows the Light Gem holder to see other players through walls and at a distance,
 * providing enhanced awareness of player positions. The glowing effect is only visible to the player holding
 * the Light Gem and does not affect gameplay for others.</p>
 *
 * @author HoneyBerries
 * @since 1.0
 */
public class LightGemTask {

    /** Reference to the main plugin instance. */
    private static final GemMod plugin = GemMod.getInstance();

    /** Logger for recording events related to the Light Gem passive effect. */
    private static final Logger logger = plugin.getLogger();


    /**
     * Starts the Light Gem passive effect recurring task.
     *
     * <p>This method schedules a task that runs every second (20 ticks) and:</p>
     * <ol>
     *   <li>Checks all online players to see who has a Light Gem in their inventory</li>
     *   <li>For players with the Light Gem, makes all other players glow with an aqua outline (only visible to them)</li>
     *   <li>For players without the Light Gem, removes any glowing effects they previously saw</li>
     * </ol>
     *
     */
    public static void startLightGemTask() {
        logger.info("Starting Light Gem passive effect task");

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

        logger.info("Light Gem passive effect task started successfully");
    }

    private static void setPlayerGlowing(final Player viewer, final Player target, final boolean glowing) {
        final EntityData<Byte> entityData = getPlayerEntityData(target, glowing);
        final List<EntityData<?>> metadata = List.of(entityData);

        final int id = target.getEntityId();
        final WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(id, metadata);

        final PacketEventsAPI<?> packetEvents = PacketEvents.getAPI();
        final PlayerManager manager = packetEvents.getPlayerManager();
        manager.sendPacket(viewer, packet);
      }

    private static @NotNull EntityData<Byte> getPlayerEntityData(Player player1, boolean glowing) {

        final byte flags = (byte) ((player1.getFireTicks() > 0 ? 0x01 : 0) |
                (player1.isSneaking() ? 0x02 : 0) | (player1.isSprinting() ? 0x08 : 0) |
                (player1.isSwimming() ? 0x10 : 0) |
                (player1.isInvisible() ? 0x20 : 0) | (glowing ? 0x40 : 0) |
                (player1.isGliding() ? 0x80 : 0));
        return new EntityData<>(0, EntityDataTypes.BYTE, flags);

    }

}