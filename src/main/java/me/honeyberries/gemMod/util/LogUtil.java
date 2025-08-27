package me.honeyberries.gemMod.util;

import me.honeyberries.gemMod.GemMod;
import me.honeyberries.gemMod.configuration.GemModData;

import java.util.logging.Logger;

/**
 * Utility class for centralized logging within the GemMod plugin.
 * <p>
 * This class wraps around the plugin's {@link Logger}, providing:
 * <ul>
 *     <li>Conditional verbose logging controlled by {@code data.yml}'s
 *         {@code verbose-logging} flag.</li>
 *     <li>Convenience methods for different log severity levels
 *         (INFO, WARNING, SEVERE).</li>
 * </ul>
 * <p>
 * Using this utility ensures that all log messages are consistent,
 * and makes it easier to control verbosity globally through configuration.
 *
 * <h3>Usage Examples</h3>
 * <pre>
 *     // Only logs if verbose-logging is enabled in data.yml
 *     LogUtil.verbose("Checking player gem stats...");
 *
 *     // Always logs as INFO
 *     LogUtil.info("Gem system successfully initialized.");
 *
 *     // Always logs as WARNING
 *     LogUtil.warn("Gem configuration file missing optional section.");
 *
 *     // Always logs as SEVERE
 *     LogUtil.severe("Failed to load gem data! Plugin may not work correctly.");
 * </pre>
 *
 * <h3>Design Notes</h3>
 * <ul>
 *     <li>This class is {@code final} and has a private constructor
 *     to prevent instantiation. It is intended to be used statically.</li>
 *     <li>Centralizing logging here makes it easy to later enhance
 *     logging (e.g., color formatting, debug levels, log to file, etc.).</li>
 * </ul>
 */
public final class LogUtil {
    /** The plugin's shared logger, retrieved from {@link GemMod#getInstance()}. */
    private static final Logger LOGGER = GemMod.getInstance().getLogger();

    /** Private constructor to prevent instantiation. */
    private LogUtil() {}

    /**
     * Logs a message at INFO level, but only if verbose logging is enabled
     * in {@code data.yml}.
     *
     * @param msg the message to log
     * @see GemModData#isVerboseLoggingEnabled()
     */
    public static void verbose(String msg) {
        if (GemModData.isVerboseLoggingEnabled()) {
            LOGGER.info(msg);
        }
    }

    /**
     * Logs a message at INFO level, unconditionally.
     * <p>
     * Use this for important messages that should always be visible.
     *
     * @param msg the message to log
     */
    public static void info(String msg) {
        LOGGER.info(msg);
    }

    /**
     * Logs a message at WARNING level, unconditionally.
     * <p>
     * Use this for potential issues that are not fatal but
     * may require attention.
     *
     * @param msg the message to log
     */
    public static void warn(String msg) {
        LOGGER.warning(msg);
    }

    /**
     * Logs a message at SEVERE level, unconditionally.
     * <p>
     * Use this for critical errors that prevent the plugin
     * from functioning correctly.
     *
     * @param msg the message to log
     */
    public static void severe(String msg) {
        LOGGER.severe(msg);
    }
}
