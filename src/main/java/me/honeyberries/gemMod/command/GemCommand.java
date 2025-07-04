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
        // Restricts the command to users with the "gemmod.command.gem" permission.
        .requires(source -> source.getSender().hasPermission("gemmod.command.gem"))
        // Default execution of the command, displays a help message.
        .executes(ctx -> {
            sendHelp(ctx.getSource());
            return Command.SINGLE_SUCCESS;
        })
        // Adds a "help" subcommand to display help information.
        .then(Commands.literal("help")
            .executes(ctx -> {
                sendHelp(ctx.getSource());
                return Command.SINGLE_SUCCESS;
            }))
        // Adds arguments for gem type, player, and amount.
        .then(Commands.argument("gem-type", StringArgumentType.word())
            // Provides suggestions for gem types.
            .suggests((ctx, builder) -> {
                for (String type : new String[]{"air", "fire", "water", "earth", "darkness", "ice", "light"}) {
                    builder.suggest(type);
                }
                return builder.buildFuture();
            })
            // Executes the command with only gem type specified.
            .executes(ctx -> giveGem(ctx, null, 1))
            // Adds an optional player argument.
            .then(Commands.argument("player", StringArgumentType.string())
                // Provides suggestions for online player names.
                .suggests((ctx, builder) -> {
                    Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                    return builder.buildFuture();
                })
                // Executes the command with gem type and player specified.
                .executes(ctx -> giveGem(ctx, ctx.getArgument("player", String.class), 1))
                // Adds an optional amount argument.
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    // Executes the command with gem type, player, and amount specified.
                    .executes(ctx -> giveGem(
                        ctx,
                        ctx.getArgument("player", String.class),
                        ctx.getArgument("amount", Integer.class)
                    ))
                )
            ));

    /**
     * Handles the logic for giving gems to a player.
     *
     * @param ctx The command context.
     * @param playerName The name of the target player (optional).
     * @param amount The number of gems to give.
     * @return Command execution result (success or failure).
     */
    private static int giveGem(CommandContext<CommandSourceStack> ctx, String playerName, int amount) {
        CommandSourceStack source = ctx.getSource();
        CommandSender sender = source.getSender();
        String gemTypeStr = ctx.getArgument("gem-type", String.class).toLowerCase();

        // Determine the target player.
        Player target;
        if (playerName != null) {
            target = Bukkit.getPlayerExact(playerName);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found: " + playerName, NamedTextColor.RED));
                sendHelp(source);
                return 0;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage(Component.text("Console must specify a player.", NamedTextColor.RED));
            return 0;
        }

        // Validate the gem type.
        GemType gemType;
        try {
            gemType = GemType.valueOf(gemTypeStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Invalid gem type: " + gemTypeStr, NamedTextColor.RED));
            sendHelp(source);
            return 0;
        }

        // Add the gem to the target player's inventory.
        target.getInventory().addItem(GemManager.createGem(gemType, amount));
        sender.sendMessage(Component.text(String.format("Given %d %s Gem(s) to %s", amount, capitalize(gemTypeStr), target.getName()), NamedTextColor.GREEN));
        GemMod.getInstance().getLogger().info(String.format("%s gave %d %s Gem(s) to %s", sender.getName(), amount, gemTypeStr, target.getName()));
        return Command.SINGLE_SUCCESS;
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