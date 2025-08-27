package me.honeyberries.gemMod;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.honeyberries.gemMod.command.GemCommand;
import me.honeyberries.gemMod.command.GemModCommand;
import me.honeyberries.gemMod.configuration.GemModData;
import me.honeyberries.gemMod.listener.*;
import me.honeyberries.gemMod.recipe.GemRecipe;
import me.honeyberries.gemMod.task.*;
import me.honeyberries.gemMod.util.LogUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class for the GemMod plugin.
 * This plugin introduces a variety of magical gems into Minecraft, each granting unique abilities.
 */
public final class GemMod extends JavaPlugin {

    private final Map<String, Boolean> enabledFeatures = new ConcurrentHashMap<>();
    private String resourcePackSha1;
    private static final String RESOURCE_PACK_URL = "https://download.mc-packs.net/pack/a11dbe3d078cb0e7bcb38492e1d6f8058c2cdac5.zip";

    @Override
    public void onLoad() {
        // Initialize the PacketEvents API
        LogUtil.info("Initializing PacketEvents API...");
        registerComponent("packetEvents", "PacketEvents API loaded successfully pre-enable", () -> {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().getSettings().checkForUpdates(false);
            PacketEvents.getAPI().load();
        });
    }

    @Override
    public void onEnable() {
        LogUtil.info("\n---------- GemMod Enabled ----------\n");

        // Load configuration data first so we can honor resourcepack-url and verbose-logging
        registerComponent("configuration", "Configuration files loaded successfully", GemModData::loadData);

        // Compute SHA-1 for resource pack using configured URL if present
        resourcePackSha1 = computeResourcePackSha1();
        if (resourcePackSha1 != null) {
            LogUtil.info("Resource pack SHA-1: " + resourcePackSha1);
        } else {
            LogUtil.warn("Resource pack SHA-1 could not be determined. Resource pack enforcement may fail.");
        }

        // Initialize PacketEvents API if previously loaded
        if (isFeatureEnabled("packetEvents")) {
            registerComponent("packetEventsInit", "PacketEvents API ready", () -> PacketEvents.getAPI().init());
        }

        // Register components
        registerCommands();
        registerEventListeners();
        registerRecipes();
        scheduleTasks();

        // Log which features were successfully enabled
        logEnabledFeatures();

        LogUtil.info("\n\n---------- GemMod Initialization Complete ----------\n\n");
    }

    @Override
    public void onDisable() {
        LogUtil.info("---------- GemMod Disabling ----------");

        try {
            LogUtil.info("Cancelling all scheduled tasks...");
            getServer().getGlobalRegionScheduler().cancelTasks(this);
            LogUtil.info("Tasks cancelled");
        } catch (Exception e) {
            LogUtil.severe("Error cancelling tasks: " + e.getMessage());
        }

        // Disable the PacketEvents API if it was enabled
        if (isFeatureEnabled("packetEvents")) {
            try {
                PacketEvents.getAPI().terminate();
                LogUtil.info("PacketEvents API terminated");
            } catch (Exception e) {
                LogUtil.severe("Error terminating PacketEvents API: " + e.getMessage());
            }
        }

        LogUtil.info("GemMod disabled. Thank you for using GemMod!");
    }

    private void registerCommands() {
        LogUtil.info("Registering commands...");
        registerComponent("gemmodCommand", "Registered /gemmod command", () ->
                getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                        commands -> commands.registrar().register(GemModCommand.getBuildCommand())));

        registerComponent("gemCommand", "Registered /gem command", () ->
                getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                        commands -> commands.registrar().register(GemCommand.getBuildCommand())));
    }

    private void registerEventListeners() {
        LogUtil.info("Registering event listeners...");
        registerComponent("resourcepack", "Registered resource pack download listener (PlayerJoinListener)",
                () -> getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this));

        registerComponent("hotbarCooldown", "Registered HotbarSwitchCooldownListener",
                () -> getServer().getPluginManager().registerEvents(new HotbarSwitchCooldownListener(), this));

        registerComponent("gemUsage", "Registered GemUsageListener",
                () -> getServer().getPluginManager().registerEvents(new GemUsageListener(), this));

        registerComponent("airGem", "Registered AirGemListener",
                () -> getServer().getPluginManager().registerEvents(new AirGemListener(), this));

        registerComponent("darknessGem", "Registered DarknessGemListener",
                () -> getServer().getPluginManager().registerEvents(new DarknessGemListener(), this));

        registerComponent("gemCrafting", "Registered GemCraftListener",
                () -> getServer().getPluginManager().registerEvents(new GemCraftListener(), this));
    }

    private void registerRecipes() {
        LogUtil.info("Registering gem recipes...");
        registerComponent("gemRecipes", "Gem recipes registered.", GemRecipe::registerGemRecipes);
    }

    private void scheduleTasks() {
        LogUtil.info("Scheduling recurring tasks...");
        registerComponent("earthGemTask", "Started Earth Gem task", EarthGemTask::startEarthGemTask);
        registerComponent("fireGemTask", "Started Fire Gem task", FireGemTask::startFireGemTask);
        registerComponent("waterGemTask", "Started Water Gem task", WaterGemTask::startWaterGemTask);

        if (isFeatureEnabled("packetEvents")) {
            registerComponent("lightGemTask", "Started Light Gem task", LightGemTask::startLightGemTask);
        } else {
            LogUtil.warn("Skipping Light Gem task: PacketEvents is not enabled.");
            setFeatureEnabled("lightGem", false);
        }
    }

    private void registerComponent(String feature, String successMessage, Runnable registrationLogic) {
        try {
            registrationLogic.run();
            LogUtil.info(successMessage);
            setFeatureEnabled(feature, true);
        } catch (Exception e) {
            LogUtil.severe("Failed to initialize " + feature + ": " + e.getMessage());
            setFeatureEnabled(feature, false);
        }
    }

    private String computeResourcePackSha1() {
        try (InputStream in = URI.create(getResourcePackUrl()).toURL().openStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] sha1Bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : sha1Bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            LogUtil.severe("Failed to compute SHA-1 for resource pack: " + e.getMessage());
            return null;
        }
    }

    private void logEnabledFeatures() {
        LogUtil.info("\n---------- GemMod Feature Status ----------");
        enabledFeatures.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    NamedTextColor color = entry.getValue() ? NamedTextColor.GREEN : NamedTextColor.RED;
                    getComponentLogger().info(
                            Component.text(entry.getKey() + ": " + (entry.getValue() ? "ENABLED :)" : "DISABLED :("), color)
                    );
                });
    }

    public String getResourcePackUrl() {
        // prefer configured URL from data.yml if present
        String configured = GemModData.getResourcePackUrl();
        return configured != null ? configured : RESOURCE_PACK_URL;
    }

    public String getResourcePackSha1() {
        return resourcePackSha1;
    }

    public static GemMod getInstance() {
        return getPlugin(GemMod.class);
    }

    public boolean isFeatureEnabled(String feature) {
        return enabledFeatures.getOrDefault(feature, false);
    }

    private void setFeatureEnabled(String feature, boolean enabled) {
        enabledFeatures.put(feature, enabled);
    }
}
