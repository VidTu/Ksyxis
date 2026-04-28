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

package ru.vidtu.ksyxis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.embeddedt.modernfix.core.ModernFixMixinPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import ru.vidtu.ksyxis.platform.KCompile;
import ru.vidtu.ksyxis.platform.KPlugin;

/**
 * Ksyxis plugin for compat hacks.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class KCompat {
    /**
     * The amount of loaded chunks to report. Usually {@code 0}, because we have no spawn chunks,
     * but if ModernFix is installed, the value might be changed to {@code 441} to prevent deadlocks.
     * <p>
     * Equals to either {@code 0} or {@code 441}, depending on the configuration.
     */
    public static final int REPORT_CHUNKS;

    static {
        // Create a temporary logger. (there's no sense in keeping it after)
        final Logger logger = LogManager.getLogger("Ksyxis/KCompat");

        // Create the chunks variable.
        /*non-final*/ int chunks;
        try {
            // Check the removeSpawnChunks. ModernFix apparently did this too for some time, just in different way.
            final boolean removeSpawnChunks = ModernFixMixinPlugin.instance.isOptionEnabled("perf.remove_spawn_chunks.MinecraftServer");

            // Log.
            if (KCompile.DEBUG_LOGS) {
                logger.info(KPlugin.MARKER, "Ksyxis: Enabled compatibility hack with ModernFix. (removeSpawnChunks: {})", new Object[]{removeSpawnChunks}); // <- Array for compat with older Log4j2.
            } else {
                logger.info("Ksyxis: Enabled compatibility hack with ModernFix. (removeSpawnChunks: {})", new Object[]{removeSpawnChunks}); // <- Array for compat with older Log4j2.
            }

            // Check what amount of spawn chunks to report back to the game.
            // ModernFix needs 441, because of its own way of doing it.
            // Ksyxis needs 0, because we remove all spawn chunks.
            chunks = (removeSpawnChunks ? 441 : 0);
        } catch (final Throwable t) {
            // Log.
            if (KCompile.DEBUG_LOGS) {
                logger.info(KPlugin.MARKER, "Ksyxis: No compatibility hacks were used.", new Object[]{t}); // <- Array for compat with older Log4j2.
            } else {
                logger.info("Ksyxis: No compatibility hacks were used.");
            }

            // No ModernFix found, it's Ksyxis only, and we have 0 chunks.
            chunks = 0;
        }

        // Flush to constant.
        REPORT_CHUNKS = chunks;
    }

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private KCompat() {
        if (KCompile.DEBUG_ASSERTS) {
            throw new AssertionError("Ksyxis: No instances.");
        }
    }
}
