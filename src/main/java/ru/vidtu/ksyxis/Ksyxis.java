/*
 * MIT License
 *
 * Copyright (c) 2021-2024 VidTu
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
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Main Ksyxis class.
 *
 * @author VidTu
 */
public final class Ksyxis {
    /**
     * Logger. Using Log4j logger, because SLF4J may not be available in older versions.
     */
    private static final Logger LOGGER = LogManager.getLogger("Ksyxis");

    /**
     * Mixin absent error message.
     */
    private static final String MIXIN_ABSENT = "Ksyxis: No Mixin found. If you`re using old (1.15.2 or older) Forge, please install a Mixin loader, for example MixinBootstrap, MixinBooter, UniMixins, or any other at your choice. If you`re using new (1.16 or newer) Forge, any Fabric, any Quilt, any Ornithe, any LegacyFabric, or any NeoForge, then something went wrong and you should report it on GitHub. Ensure to include as much information (game version, loader type, loader version, mod version, other mods, logs, etc.) in the bug report as possible, this error screen is not enough. If you don`t want any hassles and just want to load the game without solving anything, delete the Ksyxis mod. (platform: %s; manual: %s)";

    /**
     * Mixin inject error message.
     */
    private static final String MIXIN_INJECT = "Ksyxis: Unable to inject the Ksyxis configuration. It`s probably a bug or something, you should report it on GitHub. Ensure to include as much information (game version, loader type, loader version, mod version, other mods, logs, etc.) in the bug report as possible, this error screen is not enough. If you don`t want any hassles and just want to load the game without solving anything, delete the Ksyxis mod. (platform: %s; manual: %s, mixin: %s)";

    /**
     * How much chunks loaded should be reported to be loaded.
     * Usually {@code 0}.
     */
    private static int loadedChunks = 0;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private Ksyxis() {
        throw new AssertionError("No instances.");
    }

    /**
     * Initialize the mod.
     *
     * @param platform Current platform
     * @param manual   Whether to inject Mixin configuration manually
     */
    public static void init(String platform, boolean manual) {
        String mixinVersion = "UNKNOWN";
        try {
            // Log.
            LOGGER.info("Ksyxis: Booting... (platform: {}, manual: {})", new Object[]{platform, manual}); // <- Array for compat with log4j 2.0-beta.9.

            // Check for Mixin.
            try {
                // Try to load the class.
                Class<?> clazz = Class.forName("org.spongepowered.asm.launch.MixinBootstrap");
                Field field = clazz.getField("VERSION");
                mixinVersion = String.valueOf(field.get(null));
            } catch (Throwable t) {
                // Log.
                String message = String.format(MIXIN_ABSENT, platform, manual);
                LOGGER.error(message, t);

                // Try to display LWJGL3 message box from TinyFD.
                try {
                    Class<?> tinyFd = Class.forName("org.lwjgl.util.tinyfd.TinyFileDialogs");
                    Method tinyFdMessageBox = tinyFd.getMethod("tinyfd_messageBox", CharSequence.class, CharSequence.class, CharSequence.class, CharSequence.class, boolean.class);
                    tinyFdMessageBox.invoke(null, "Minecraft | Ksyxis Mod", message, "ok", "error", false);
                } catch (Throwable th) {
                    t.addSuppressed(th);
                }

                // Try to display LWJGL2 alert from Sys.
                try {
                    Class<?> sys = Class.forName("org.lwjgl.Sys");
                    Method sysAlert = sys.getMethod("alert", String.class, String.class);
                    sysAlert.invoke(null, "Minecraft | Ksyxis Mod", message);
                } catch (Throwable th) {
                    t.addSuppressed(th);
                }

                // Log again. (with suppressed errors)
                LOGGER.error(message, t);

                // Throw.
                throw new RuntimeException(message, t);
            }

            // Log.
            LOGGER.info("Ksyxis: Found Mixin library. (version: {})", new Object[]{mixinVersion}); // <- Array for compat with log4j 2.0-beta.9.

            // Bootstrap Mixin and add config.
            if (manual) {
                MixinBootstrap.init();
                LOGGER.debug("Ksyxis: Mixin Bootstrap success.");
                Mixins.addConfiguration("ksyxis.mixins.json");
                LOGGER.debug("Ksyxis: Mixin config added.");
            }

            // Try to fix compat with ModernFix.
            try {
                Class<?> modernFixPluginClass = Class.forName("org.embeddedt.modernfix.core.ModernFixMixinPlugin");
                Field modernFixPluginField = modernFixPluginClass.getDeclaredField("instance");
                Method modernFixIsOptionEnabled = modernFixPluginClass.getMethod("isOptionEnabled", String.class);
                Object modernFix = modernFixPluginField.get(null);
                boolean removeSpawnChunks = (boolean) modernFixIsOptionEnabled.invoke(modernFix, "perf.remove_spawn_chunks.MinecraftServer");
                if (removeSpawnChunks) {
                    loadedChunks = 441;
                    LOGGER.debug("Ksyxis: Will report 441 loaded chunks to prevent deadlocks with ModernFix.");
                } else {
                    LOGGER.debug("Ksyxis: Not providing compat with ModernFix, removeSpawnChunks is disabled.");
                }
            } catch (Throwable t) {
                LOGGER.debug("Ksyxis: Not providing compat with ModernFix, not found.", t);
            }

            // Log the info.
            LOGGER.info("Ksyxis: Ready. As always, this mod will speed up your world loading and might or might not break it.");
        } catch (Throwable t) {
            // Log.
            String message = String.format(MIXIN_INJECT, platform, manual, mixinVersion);
            LOGGER.error(message, t);

            // Try to display LWJGL3 message box from TinyFD.
            try {
                Class<?> tinyFd = Class.forName("org.lwjgl.util.tinyfd.TinyFileDialogs");
                Method tinyFdMessageBox = tinyFd.getMethod("tinyfd_messageBox", CharSequence.class, CharSequence.class, CharSequence.class, CharSequence.class, boolean.class);
                tinyFdMessageBox.invoke(null, "Minecraft | Ksyxis Mod", message, "ok", "error", false);
            } catch (Throwable th) {
                t.addSuppressed(th);
            }

            // Try to display LWJGL2 alert from Sys.
            try {
                Class<?> sys = Class.forName("org.lwjgl.Sys");
                Method sysAlert = sys.getMethod("alert", String.class, String.class);
                sysAlert.invoke(null, "Minecraft | Ksyxis Mod", message);
            } catch (Throwable th) {
                t.addSuppressed(th);
            }

            // Log again. (with suppressed errors)
            LOGGER.error(message, t);

            // Throw.
            throw new RuntimeException(message, t);
        }
    }

    /**
     * Gets the amount of reported loaded chunks.
     *
     * @return Loaded chunks, usually {@code 0}
     * @implNote Used for compatibility with ModernFix
     */
    public static int loadedChunks() {
        return loadedChunks;
    }
}
