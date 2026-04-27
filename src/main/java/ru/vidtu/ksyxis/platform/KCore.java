/*
 * Ksyxis is a third-party mod for Minecraft Java Edition that
 * speed ups your world loading by removing unneeded chunks.
 *
 * MIT License
 *
 * Copyright (c) 2021-2026 VidTu
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
 *
 * SPDX-License-Identifier: MIT
 */

package ru.vidtu.ksyxis.platform;

import com.google.errorprone.annotations.DoNotCall;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

/**
 * Main Ksyxis class for Legacy Forge. (coremod hook)
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see KForge
 */
@ApiStatus.Internal
@NullMarked
public final class KCore implements IFMLLoadingPlugin {
    /**
     * Creates a new coremod.
     *
     * @apiNote Do not call, called by FML
     */
    @Contract(pure = true)
    public KCore() {
        // Empty.
    }

    /**
     * Does nothing. Always returns {@code null}.
     *
     * @return Always {@code null}
     * @apiNote Do not call, called by FML
     */
    @DoNotCall("Called by FML")
    @Contract(value = "-> null", pure = true)
    @Override
    @Nullable
    public String getAccessTransformerClass() {
        return null;
    }

    /**
     * Does nothing. Always returns {@code null}.
     *
     * @return Always {@code null}
     * @apiNote Do not call, called by FML
     */
    @DoNotCall("Called by FML")
    @Contract(value = "-> null", pure = true)
    @Override
    public String @Nullable [] getASMTransformerClass() {
        return null;
    }

    /**
     * Does nothing. Always returns {@code null}.
     *
     * @return Always {@code null}
     * @apiNote Do not call, called by FML
     */
    @DoNotCall("Called by FML")
    @Contract(value = "-> null", pure = true)
    @Override
    @Nullable
    public String getModContainerClass() {
        return null;
    }

    /**
     * Does nothing. Always returns {@code null}.
     *
     * @return Always {@code null}
     * @apiNote Do not call, called by FML
     */
    @DoNotCall("Called by FML")
    @Contract(value = "-> null", pure = true)
    @Override
    @Nullable
    public String getSetupClass() {
        return null;
    }

    /**
     * Manually bootstraps the Mixin and injects the configuration.
     *
     * @param data Injection data, ignored
     * @apiNote Do not call, called by FML
     */
    @DoNotCall("Called by FML")
    @Override
    public void injectData(final Map<String, Object> data) {
        // Create a temporary logger. (there's no sense in keeping it after)
        final Logger logger = LogManager.getLogger("Ksyxis/KCore");
        try {
            // Log.
            if (KCompile.DEBUG_LOGS) {
                logger.info(KPlugin.MARKER, "Ksyxis: Performing manual Mixin bootstrap... (version: " + KCompile.VERSION + ", mixin: {})", new Object[]{MixinBootstrap.VERSION}); // <- Array for compat with older Log4j2.
            } else {
                logger.info("Ksyxis: Performing manual Mixin bootstrap... (version: " + KCompile.VERSION + ", mixin: {})", new Object[]{MixinBootstrap.VERSION}); // <- Array for compat with older Log4j2.
            }

            // Bootstrap Mixin.
            MixinBootstrap.init();

            // Log. (**DEBUG**)
            if (KCompile.DEBUG_LOGS) {
                logger.debug(KPlugin.MARKER, "Ksyxis: Mixin bootstrapped. Adding the config...");
            }

            // Add the config.
            Mixins.addConfiguration("ksyxis.mixins.json");

            // Log.
            if (KCompile.DEBUG_LOGS) {
                logger.info(KPlugin.MARKER, "Ksyxis: Manual Mixin bootstrap done.");
            } else {
                logger.info("Ksyxis: Manual Mixin bootstrap done.");
            }
        } catch (final Throwable t) {
            // Check if Mixin is absent.
            if (t.getClass() == NoClassDefFoundError.class && "org/spongepowered/asm/launch/MixinBootstrap".equals(t.getMessage())) {
                throw new RuntimeException("Ksyxis: No Mixin found. (core)", t);
            }

            // Rethrow.
            throw new RuntimeException("Ksyxis: Unexpected core error.", t);
        }

    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "Ksyxis/KCore{}";
    }
}
