package me.honeyberries.gemMod.command;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.configuration.GemModData;
import me.honeyberries.gemMod.manager.GemManager.GemType;
import me.honeyberries.gemMod.manager.GemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the /gem command, allowing players or admins to give gem items.
 */
public class GemCommand implements TabExecutor {

    // Reference to the main plugin instance
    private final GemMod plugin = GemMod.getInstance();


    /**
     * Processes the /gem command.
     *
     * @param sender  the source of the command
     * @param command the executed command
     * @param label   the alias used
     * @param args    command arguments, including gem type, player (optional), and amount (optional)
     * @return true once processing is complete
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        // Check permissions for command usage.
        if (!sender.hasPermission("honeyberries.command.gem")) {
            sender.sendMessage(Component.text("You don't have permission to use this command", NamedTextColor.RED));
            return true;
        }

        // Display the help message if no arguments or "help" is provided.
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            // Reload the plugin configuration and recipes
            GemModData.loadData();
            sender.sendMessage(Component.text("GemMod configuration reloaded and recipes updated.", NamedTextColor.GREEN));
            return true;
        }

        // Parse the gem type from the first argument.
        String gemType = args[0].toLowerCase();

        // Determine the target player (either provided or the sender if they are a player).
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found: " + args[1], NamedTextColor.RED));
                sendHelpMessage(sender);
                return true;
            }
        } else {
            if (sender instanceof Player playerSender) {
                target = playerSender;
            } else {
                sender.sendMessage(Component.text("Console must specify a player.", NamedTextColor.RED));
                return true;
            }
        }

        // Parse the amount from the third argument if provided; default to 1.
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount <= 0) {
                    sender.sendMessage(Component.text("Amount must be greater than 0", NamedTextColor.RED));
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid amount: " + args[2], NamedTextColor.RED));
                return true;
            }
        }

        // Process gem giving based on the gem type.
        switch (gemType) {
            case "air" -> {
                target.getInventory().addItem(GemManager.createGem(GemType.AIR, amount));
                sender.sendMessage(Component.text(String.format("Given %d Air Gem(s) to %s", amount, target.getName()), NamedTextColor.GREEN));
            }
            case "darkness" -> {
                target.getInventory().addItem(GemManager.createGem(GemType.DARKNESS, amount));
                sender.sendMessage(Component.text(String.format("Given %d Darkness Gem(s) to %s", amount, target.getName()), NamedTextColor.GREEN));
            }
            case "earth" -> {
                target.getInventory().addItem(GemManager.createGem(GemType.EARTH, amount));
                sender.sendMessage(Component.text(String.format("Given %d Earth Gem(s) to %s", amount, target.getName()), NamedTextColor.GREEN));
            }
            case "fire" -> {
                target.getInventory().addItem(GemManager.createGem(GemType.FIRE, amount));
                sender.sendMessage(Component.text(String.format("Given %d Fire Gem(s) to %s", amount, target.getName()), NamedTextColor.GREEN));
            }
            case "ice" -> {
                target.getInventory().addItem(GemManager.createGem(GemType.ICE, amount));
                sender.sendMessage(Component.text(String.format("Given %d Ice Gem(s) to %s", amount, target.getName()), NamedTextColor.GREEN));
            }
            case "light" -> {
                target.getInventory().addItem(GemManager.createGem(GemType.LIGHT, amount));
                sender.sendMessage(Component.text(String.format("Given %d Light Gem(s) to %s", amount, target.getName()), NamedTextColor.GREEN));
            }
            case "water" -> {
                target.getInventory().addItem(GemManager.createGem(GemType.WATER, amount));
                sender.sendMessage(Component.text(String.format("Given %d Water Gem(s) to %s", amount, target.getName()), NamedTextColor.GREEN));
            }
            default -> {
                sender.sendMessage(Component.text(String.format("Invalid gem type: %s", gemType), NamedTextColor.RED));
                sendHelpMessage(sender);
            }
        }
        // Log the action to the console.
        plugin.getLogger().info(String.format("%s gave %d %s Gem(s) to %s", sender.getName(), amount, gemType, target.getName()));
        return true;
    }

    /**
     * Provides tab completion suggestions for the /gem command.
     *
     * @param sender the command sender
     * @param command the executed command
     * @param alias the alias used
     * @param args command arguments
     * @return a list of suggestions for tab completion
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            // Suggest available gem types and "help"
            suggestions.addAll(List.of("air", "fire", "water", "earth", "darkness", "ice", "light", "reload", "help"));
        }
        if (args.length == 2) {
            // Suggest player names
            suggestions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        }
        return suggestions.stream().filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).toList();
    }


    /**
     * Provides the help message detailing the /gem command usage.
     *
     * @param sender the command sender to display help to.
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Component.text("---------- Gem Command Help ----------", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/gem <gem-type> <player (optional)> <amount (optional)>", NamedTextColor.AQUA)
                .append(Component.text(" - Give a gem to a player", NamedTextColor.GOLD)));
        sender.sendMessage(Component.text("/gem help", NamedTextColor.AQUA)
                .append(Component.text(" - Show this help message", NamedTextColor.GOLD)));
        sender.sendMessage(Component.text("/gem reload", NamedTextColor.AQUA)
                .append(Component.text(" - Reload the plugin configuration", NamedTextColor.GOLD)));
        sender.sendMessage(Component.text("Available gem types: air, fire, water, earth, darkness, ice, light", NamedTextColor.GREEN));
    }
}
