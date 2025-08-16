package me.honeyberries.gemMod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.honeyberries.gemMod.configuration.GemModData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Defines and handles the execution of the /gemmod command using the Brigadier command framework.
 *
 * This command provides administrative functionalities, such as reloading the plugin's configuration
 * and displaying a help message.
 *
 * @author HoneyBerries
 * @version 1.0
 */
public class GemModCommand {

    /**
     * Defines the main literal for the /gemmod command and its subcommands.
     */
    private static final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("gemmod")
            // Restricts the command to users with the "gemmod.command.gemmod" permission.
            .requires(source -> source.getSender().hasPermission("gemmod.command.gemmod"))
            // Default execution of the command, displays a usage message.
            .executes(context -> {
                context.getSource().getSender().sendMessage(Component.text("Use /gemmod help for command usage.", NamedTextColor.YELLOW));
                return Command.SINGLE_SUCCESS;
            })
            // Adds a "reload" subcommand to reload the plugin configuration.
            .then(Commands.literal("reload")
                .executes(context -> {
                    // Reloads the GemMod configuration and updates recipes.
                    GemModData.loadData();
                    context.getSource().getSender().sendMessage(Component.text("GemMod configuration reloaded and recipes updated.", NamedTextColor.GREEN));
                    return Command.SINGLE_SUCCESS;
                }))
            // Adds a "help" subcommand to display help information.
            .then(Commands.literal("help")
                .executes(context -> {
                    // Sends a help message to the command sender.
                    context.getSource().getSender().sendMessage(Component.text("---------- GemMod Command Help ----------", NamedTextColor.AQUA));
                    context.getSource().getSender().sendMessage(Component.text("/gemmod reload", NamedTextColor.GOLD)
                        .append(Component.text(" - Reload the plugin configuration and recipes", NamedTextColor.GREEN)));
                    context.getSource().getSender().sendMessage(Component.text("/gemmod help", NamedTextColor.GOLD)
                        .append(Component.text(" - Show this help message", NamedTextColor.GREEN)));
                    context.getSource().getSender().sendMessage(Component.text("----------------------------------------", NamedTextColor.AQUA));
                    return Command.SINGLE_SUCCESS;
                }));

    /**
     * Builds and returns the Brigadier command structure for the /gemmod command.
     *
     * @return The fully constructed {@link LiteralCommandNode} for the command.
     */
    public static LiteralCommandNode<CommandSourceStack> getBuildCommand() {
        return command.build();
    }
}