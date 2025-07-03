package me.honeyberries.gemMod.command;

import me.honeyberries.gemMod.configuration.GemModData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class GemModCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args[0].equalsIgnoreCase("reload")) {
            // Reload the plugin configuration and recipes
            GemModData.loadData();
            sender.sendMessage(Component.text("GemMod configuration reloaded and recipes updated.", NamedTextColor.GREEN));
            return true;
        }
        sendHelpMessage(sender);
        return true;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return Stream.of("reload", "help").filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }


    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Component.text("---------- GemMod Command Help ----------", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("/gemmod reload", NamedTextColor.GOLD)
            .append(Component.text(" - Reload the plugin configuration and recipes", NamedTextColor.GREEN)));
        sender.sendMessage(Component.text("/gemmod help", NamedTextColor.GOLD)
            .append(Component.text(" - Show this help message", NamedTextColor.GREEN)));
        sender.sendMessage(Component.text("----------------------------------------", NamedTextColor.AQUA));
    }
}
