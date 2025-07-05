package me.honeyberries.gemMod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.manager.GemManager;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.stream.Stream;

/**
 * This class defines the /gem command using the Brigadier API.
 * The command allows players or the console to give gems to players.
 */
public class GemCommand {

    /**
     * Builds the Brigadier command structure for the /gem command.
     *
     * @return The LiteralCommandNode representing the /gem command.
     */
    public static LiteralCommandNode<CommandSourceStack> getBuildCommand() {
        return command.build();
    }

    // Defines the main /gem command structure.
    private static final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("gem")
        .requires(source -> source.getSender().hasPermission("gemmod.command.gem"))
        .executes(ctx -> {
            sendHelp(ctx.getSource());
            return Command.SINGLE_SUCCESS;
        })
        .then(Commands.literal("help")
            .executes(ctx -> {
                sendHelp(ctx.getSource());
                return Command.SINGLE_SUCCESS;
            }))
        .then(Commands.argument("gem-type", StringArgumentType.string())
            .suggests((ctx, builder) -> {
                Stream.of("air", "fire", "water", "earth", "darkness", "ice", "light")
                    .filter(p -> p.toLowerCase().startsWith(builder.getRemaining().toLowerCase(Locale.ROOT)))
                    .forEach(builder::suggest);
                return builder.buildFuture();
            })
            .executes(ctx -> {
                Player senderPlayer = ctx.getSource().getSender() instanceof Player p ? p : null;
                if (senderPlayer == null) {
                    ctx.getSource().getSender().sendMessage(Component.text("Console must specify a player.", NamedTextColor.RED));
                    return Command.SINGLE_SUCCESS;
                }
                giveGem(ctx, senderPlayer, 1);
                return Command.SINGLE_SUCCESS;
            })
            .then(Commands.argument("player", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(builder.getRemaining().toLowerCase(Locale.ROOT)))
                        .forEach(builder::suggest);
                    return builder.buildFuture();
                })
                .executes(ctx -> {
                    String playerName = ctx.getArgument("player", String.class);
                    Player target = Bukkit.getPlayerExact(playerName);
                    if (target == null) {
                        ctx.getSource().getSender().sendMessage(Component.text("Player not found: " + playerName, NamedTextColor.RED));
                        sendHelp(ctx.getSource());
                        return Command.SINGLE_SUCCESS;
                    }
                    giveGem(ctx, target, 1);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(ctx -> {
                        String playerName = ctx.getArgument("player", String.class);
                        Player target = Bukkit.getPlayerExact(playerName);
                        if (target == null) {
                            ctx.getSource().getSender().sendMessage(Component.text("Player not found: " + playerName, NamedTextColor.RED));
                            sendHelp(ctx.getSource());
                            return Command.SINGLE_SUCCESS;
                        }
                        int amount = ctx.getArgument("amount", Integer.class);
                        giveGem(ctx, target, amount);
                        return Command.SINGLE_SUCCESS;
                    })
                )
            ));

    /**
     * Handles the logic for giving gems to a player.
     *
     * @param ctx The command context.
     * @param player The target player.
     * @param amount The number of gems to give.
     */
    private static void giveGem(CommandContext<CommandSourceStack> ctx, Player player, int amount) {
        CommandSourceStack source = ctx.getSource();
        CommandSender sender = source.getSender();
        String gemTypeStr = ctx.getArgument("gem-type", String.class).toLowerCase();

        // Validate the gem type.
        GemType gemType;
        try {
            gemType = GemType.valueOf(gemTypeStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Invalid gem type: " + gemTypeStr, NamedTextColor.RED));
            sendHelp(source);
            return;
        }

        // Add the gem to the target player's inventory.
        player.getInventory().addItem(GemManager.createGem(gemType, amount));
        sender.sendMessage(Component.text(String.format("Given %d %s Gem(s) to %s", amount, capitalize(gemTypeStr), player.getName()), NamedTextColor.GREEN));
        GemMod.getInstance().getLogger().info(String.format("%s gave %d %s Gem(s) to %s", sender.getName(), amount, gemTypeStr, player.getName()));
    }

    /**
     * Sends the help message for the /gem command.
     *
     * @param source The command source to send the message to.
     */
    private static void sendHelp(CommandSourceStack source) {
        CommandSender sender = source.getSender();
        sender.sendMessage(Component.text("---------- Gem Command Help ----------", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/gem <gem-type> <player (optional)> <amount (optional)>", NamedTextColor.AQUA)
                .append(Component.text(" - Give a gem to a player", NamedTextColor.GOLD)));
        sender.sendMessage(Component.text("/gem help", NamedTextColor.AQUA)
                .append(Component.text(" - Show this help message", NamedTextColor.GOLD)));
        sender.sendMessage(Component.text("Available gem types: air, fire, water, earth, darkness, ice, light", NamedTextColor.GREEN));
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param s The string to capitalize.
     * @return The capitalized string.
     */
    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }
}