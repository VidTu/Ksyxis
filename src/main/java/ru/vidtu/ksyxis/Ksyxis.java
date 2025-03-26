/*
 * MIT License
 *
 * Copyright (c) 2021-2025 VidTu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.vidtu.ksyxis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Main Ksyxis class.
 *
 * @author VidTu
 */
@ApiStatus.Internal
@NullMarked
public final class Ksyxis {
    /**
     * Amount of loaded spawn chunks by vanilla Minecraft before 1.20.5. {@code 441} ({@code 21x21}) chunks.
     */
    public static final int SPAWN_CHUNKS = (21 * 21);

    /**
     * Mixin absent error message. Shown in {@link #obtainMixinVersion(String, boolean)} when Mixin is not found.
     *
     * @see #obtainMixinVersion(String, boolean)
     * @see #handleError(String, Throwable)
     */
    private static final String MIXIN_ABSENT = "Ksyxis: No Mixin found. If you`re using old (1.15.2 or older) Forge, " +
            "please install a Mixin loader, for example MixinBootstrap, MixinBooter, UniMixins, or any other at your " +
            "choice. If you`re using new (1.16 or newer) Forge, any Fabric, any Quilt, any Ornithe, any " +
            "LegacyFabric, or any NeoForge, then something went wrong and you should report it on GitHub. Ensure to " +
            "include as much information (game version, loader type, loader version, mod version, other mods, logs, " +
            "etc.) in the bug report as possible, this error screen is NOT enough. If you don`t want any hassles and " +
            "just want to load the game without solving anything, delete the Ksyxis mod. (platform: %s; manual: %s)";

    /**
     * Mixin inject error message. Shown in {@link #init(String, boolean)} when an error occurs.
     *
     * @see #init(String, boolean)
     * @see #handleError(String, Throwable)
     */
    private static final String MIXIN_INJECT = "Ksyxis: Unable to inject the Ksyxis configuration. It`s probably a " +
            "bug or something, you should report it on GitHub. Ensure to include as much information (game version, " +
            "loader type, loader version, mod version, other mods, logs, etc.) in the bug report as possible, this " +
            "error screen is NOT enough. If you don`t want any hassles and just want to load the game without " +
            "solving anything, delete the Ksyxis mod. (platform: %s; manual: %s)";

    /**
     * Logger for this class. Using Log4j2 logger, because SLF4J is not available in older versions.
     */
    private static final Logger LOGGER = LogManager.getLogger("Ksyxis");

    /**
     * Amount of loaded chunks to report. Usually {@code 0}, {@link #SPAWN_CHUNKS} with ModernFix.
     *
     * @see #getLoadedChunks()
     */
    // This MUST be below LOGGER, otherwise deadlocks will screw us up.
    public static final int LOADED_CHUNKS = getLoadedChunks();

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    @Contract(value = "-> fail", pure = true)
    private Ksyxis() {
        throw new AssertionError("No instances.");
    }

    /**
     * Initialize the mod.
     *
     * @param platform Current platform
     * @param manual   Whether to inject Mixin configuration manually
     * @throws RuntimeException If any unexpected exception occurs
     * @see #obtainMixinVersion(String, boolean)
     * @see #getLoadedChunks()
     */
    public static void init(String platform, boolean manual) {
        try {
            // Log.
            long start = System.nanoTime();
            LOGGER.info("Ksyxis: Booting... (platform: {}, manual: {})", new Object[]{platform, manual}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.

            // Obtain Mixin version.
            String mixinVersion = obtainMixinVersion(platform, manual);

            // Log. (**DEBUG**)
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Ksyxis: Found Mixin library. (mixinVersion: {})", new Object[]{mixinVersion}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.
            }

            // Bootstrap Mixin and add the config. (if manual)
            if (manual) {
                MixinBootstrap.init();
                LOGGER.debug("Ksyxis: Mixin Bootstrap success.");
                Mixins.addConfiguration("ksyxis.mixins.json");
                LOGGER.debug("Ksyxis: Mixin config added.");
            }

            // Log the info.
            LOGGER.info("Ksyxis: Ready. As always, this mod will speed up your world loading and might or might not break it. (mixinVersion: {}, time: {} ms)", new Object[]{mixinVersion, (System.nanoTime() - start) / 1_000_000L}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.
        } catch (Throwable t) {
            // Format the message.
            String message = String.format(MIXIN_INJECT, platform, manual);

            // Handle the error.
            throw handleError(message, t);
        }
    }

    /**
     * Logs the error, shows the friendly UI if possible, and throws the exception.
     *
     * @param message Error details to log
     * @param error   Exception to log
     * @return Never returns normally, can be used for throw block
     * @throws RuntimeException Wrapper for {@code message} and {@code error}
     */
    @Contract("_, _ -> fail")
    @CheckReturnValue
    static RuntimeException handleError(String message, Throwable error) {
        // Log.
        LOGGER.error(message, error);
        error.printStackTrace();

        // Try to display LWJGL3 message box from TinyFD.
        try {
            Class<?> tinyFd = Class.forName("org.lwjgl.util.tinyfd.TinyFileDialogs");
            Method tinyFdMessageBox = tinyFd.getMethod("tinyfd_messageBox", CharSequence.class,
                    CharSequence.class, CharSequence.class, CharSequence.class, boolean.class);
            tinyFdMessageBox.invoke(null, "Minecraft | Ksyxis Mod", message, "ok", "error", false);
        } catch (Throwable th) {
            // Suppress for logging.
            error.addSuppressed(new RuntimeException("Unable to display the LWJGL3 error message. Maybe it's LWJGL2 or server here.", th));
        }

        // Log again with suppressed errors.
        LOGGER.error(message, error);
        error.printStackTrace();

        // Try to display LWJGL2 alert from Sys.
        try {
            Class<?> sys = Class.forName("org.lwjgl.Sys");
            Method sysAlert = sys.getMethod("alert", String.class, String.class);
            sysAlert.invoke(null, "Minecraft | Ksyxis Mod", message);
        } catch (Throwable th) {
            // Suppress for logging.
            error.addSuppressed(new RuntimeException("Unable to display the LWJGL2 error message. Maybe it's LWJGL3 or server here.", th));
        }

        // Log again with suppressed errors.
        LOGGER.error(message, error);
        error.printStackTrace();

        // Try to die. Some smart guys at Forge 1.8.9 thought it's a good idea to prevent shutting down.
        // See below how we're bypassing that restriction, because Java 8 is not encapsulated.
        try {
            System.exit(-2037852655); // "Ksyxis".hashCode()
        } catch (Throwable th) {
            // Suppress for logging.
            error.addSuppressed(new RuntimeException("Unable to exit the game normally.", th));
        }

        // Log again with suppressed errors.
        LOGGER.error(message, error);
        error.printStackTrace();

        // Try to die via reflection.
        try {
            Class<?> shutdownClass = Class.forName("java.lang.Shutdown");
            Method shutdownMethod = shutdownClass.getDeclaredMethod("exit", int.class);
            shutdownMethod.setAccessible(true);
            shutdownMethod.invoke(null, -2037852655); // "Ksyxis".hashCode()
        } catch (Throwable th) {
            // Suppress for logging.
            error.addSuppressed(new RuntimeException("Unable to exit the game reflectively.", th));
        }

        // Log again with suppressed errors.
        LOGGER.error(message, error);
        error.printStackTrace();

        // Throw.
        throw new RuntimeException(message, error);
    }

    /**
     * Tries to obtain current Mixin version from {@link MixinBootstrap#VERSION} without javac inlining.
     * If Mixin is not found, logs the error, shows the friendly UI if possible, and throws the exception.
     *
     * @param platform Current platform (for errors and logging)
     * @param manual   Whether the Mixin configuration will be injected manually (for errors and logging)
     * @return Current Mixin version
     * @throws RuntimeException If Mixin is not installed or Mixin version can't be obtained
     * @see MixinBootstrap#VERSION
     * @see #init(String, boolean)
     * @see #handleError(String, Throwable)
     */
    @CheckReturnValue
    private static String obtainMixinVersion(String platform, boolean manual) {
        // Check for Mixin.
        try {
            // Get the field.
            Field field = MixinBootstrap.class.getField("VERSION");

            // Extract the field value without javac inlining.
            Object obj = field.get(null);
            return String.valueOf(obj);
        } catch (Throwable t) {
            // Format the message.
            String message = String.format(MIXIN_ABSENT, platform, manual);

            // Handle the error.
            throw handleError(message, t);
        }
    }

    /**
     * Evaluates and returns the amount of loaded chunks to report. Usually {@code 0}, because we have no spawn chunks,
     * but if ModernFix is installed, the value might be changed to {@link #SPAWN_CHUNKS} to prevent deadlocks.
     *
     * @return Either {@code 0} or {@link #SPAWN_CHUNKS}, depending on the configuration
     * @see #LOADED_CHUNKS
     */
    @Contract(pure = true)
    private static int getLoadedChunks() {
        try {
            // Load the ModernFix plugin config.
            Class<?> modernFixPluginClass = Class.forName("org.embeddedt.modernfix.core.ModernFixMixinPlugin");
            Field modernFixPluginField = modernFixPluginClass.getDeclaredField("instance");
            Method modernFixIsOptionEnabled = modernFixPluginClass.getMethod("isOptionEnabled", String.class);
            Object modernFix = modernFixPluginField.get(null);

            // Check the removeSpawnChunks. ModernFix apparently did this too for some time, just in different way.
            boolean removeSpawnChunks = (boolean) modernFixIsOptionEnabled.invoke(modernFix, "perf.remove_spawn_chunks.MinecraftServer");

            // Log. (**DEBUG**)
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Ksyxis: ModernFix's 'removeSpawnChunks' option is: {}", new Object[]{removeSpawnChunks}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.
            }

            // Check what amount of spawn chunks to report back to the game.
            // ModernFix needs 441, because of its own way of doing it.
            // Ksyxis needs 0, because we remove all spawn chunks.
            return (removeSpawnChunks ? SPAWN_CHUNKS : 0);
        } catch (Throwable t) {
            // Log. (**DEBUG**)
            LOGGER.debug("Ksyxis: Unable to provide compat for ModernFix, it's probably not installed.", t);

            // No ModernFix found, it's Ksyxis only and we have 0 chunks.
            return 0;
        }
    }
}
