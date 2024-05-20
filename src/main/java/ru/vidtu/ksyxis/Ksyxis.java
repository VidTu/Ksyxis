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
import org.spongepowered.asm.mixin.Mixin;
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
    private static final String MIXIN_ABSENT = "Ksyxis: No Mixin found. If you're using old (1.15.2 or older) Forge, please install a Mixin loader, for example MixinBootstrap, MixinBooter, UniMixins, or any other if you find. If you're using new (1.16 or newer) Forge, any Fabric, any Quilt, or any NeoForge, then something went wrong and you should report it on GitHub. If you don't want any hassles and just want to load the game without solving anything, delete the Ksyxis mod. ({})";

    /**
     * Mixin inject error message.
     */
    private static final String MIXIN_INJECT = "Ksyxis: Unable to inject the Ksyxis configuration. It's probably a bug or something, you should report it on GitHub. If you don't want any hassles and just want to load the game without solving anything, delete the Ksyxis mod. ({})";

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
     * Initializer for super ancient loaders.
     *
     * @param platform Current platform
     */
    public static void legacyInit(String platform) {
        // Verify Mixin.
        verifyMixins(platform);

        try {
            // Bootstrap Mixin.
            MixinBootstrap.init();

            // Add the config.
            Mixins.addConfiguration("ksyxis.mixins.json");
        } catch (Throwable t) {
            // Log.
            LOGGER.error(MIXIN_INJECT, platform, t);

            // Try to display LWJGL3 message box from TinyFD.
            try {
                Class<?> tinyFd = Class.forName("org.lwjgl.util.tinyfd.TinyFileDialogs");
                Method tinyFdMessageBox = tinyFd.getMethod("tinyfd_messageBox", CharSequence.class, CharSequence.class, CharSequence.class, CharSequence.class, boolean.class);
                tinyFdMessageBox.invoke(null, "Minecraft | Ksyxis Mod", MIXIN_INJECT, "ok", "error", false);
            } catch (Throwable th) {
                t.addSuppressed(th);
            }

            // Try to display LWJGL2 alert from Sys.
            try {
                Class<?> sys = Class.forName("org.lwjgl.Sys");
                Method sysAlert = sys.getMethod("alert", String.class, String.class);
                sysAlert.invoke(null, "Minecraft | Ksyxis Mod", MIXIN_INJECT);
            } catch (Throwable th) {
                t.addSuppressed(th);
            }

            // Log again. (with suppressed errors)
            LOGGER.error(MIXIN_INJECT, platform, t);

            // Throw.
            throw new RuntimeException(MIXIN_INJECT.replace("{}", platform), t);
        }

        // Log the info.
        LOGGER.info("Ksyxis: Ready. As always, it will speed up and might or might not break your world loading. (legacy; {})", platform);
    }

    /**
     * Checks the mixin presence and logs the info on startup.
     *
     * @param platform Current platform
     */
    public static void init(String platform) {
        // Verify Mixin.
        verifyMixins(platform);

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
        LOGGER.info("Ksyxis: Ready. As always, it will speed up and might or might not break your world loading. (modern; {})", platform);
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

    /**
     * Verifies the {@link Mixin} presence.
     *
     * @param platform Current platform
     * @throws RuntimeException If {@link Mixin} class can't be found
     */
    private static void verifyMixins(String platform) {
        // Check for Mixin.
        try {
            // Try to load the class.
            Class.forName("org.spongepowered.asm.mixin.Mixin");

            // Class found.
            LOGGER.debug("Ksyxis: Found Mixin library. ({})", platform);
        } catch (Throwable t) {
            // Log.
            LOGGER.error(MIXIN_ABSENT, platform, t);

            // Try to display LWJGL3 message box from TinyFD.
            try {
                Class<?> tinyFd = Class.forName("org.lwjgl.util.tinyfd.TinyFileDialogs");
                Method tinyFdMessageBox = tinyFd.getMethod("tinyfd_messageBox", CharSequence.class, CharSequence.class, CharSequence.class, CharSequence.class, boolean.class);
                tinyFdMessageBox.invoke(null, "Minecraft | Ksyxis Mod", MIXIN_ABSENT, "ok", "error", false);
            } catch (Throwable th) {
                t.addSuppressed(th);
            }

            // Try to display LWJGL2 alert from Sys.
            try {
                Class<?> sys = Class.forName("org.lwjgl.Sys");
                Method sysAlert = sys.getMethod("alert", String.class, String.class);
                sysAlert.invoke(null, "Minecraft | Ksyxis Mod", MIXIN_ABSENT);
            } catch (Throwable th) {
                t.addSuppressed(th);
            }

            // Log again. (with suppressed errors)
            LOGGER.error(MIXIN_ABSENT, platform, t);

            // Throw.
            throw new RuntimeException(MIXIN_ABSENT.replace("{}", platform), t);
        }
    }
}
