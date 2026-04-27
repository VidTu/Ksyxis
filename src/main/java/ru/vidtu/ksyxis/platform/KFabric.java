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
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.launch.MixinBootstrap;

/**
 * Main Ksyxis class for Fabric.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class KFabric implements ModInitializer {
    /**
     * Creates a new mod.
     *
     * @apiNote Do not call, called by Fabric
     */
    @Contract(pure = true)
    public KFabric() {
        // Empty.
    }

    /**
     * Logs the platform info.
     *
     * @apiNote Do not call, called by Fabric
     */
    @DoNotCall("Called by Fabric")
    @Override
    public void onInitialize() {
        // Log. (there's no sense in keeping the logger after that)
        final Logger logger = LogManager.getLogger("Ksyxis/KFabric");
        if (KCompile.DEBUG_LOGS) {
            logger.info(KPlugin.MARKER, "Ksyxis: Ready to remove unneeded chunks. (platform: fabric, version: " + KCompile.VERSION + ", mixin: {})", new Object[]{MixinBootstrap.VERSION}); // <- Array for compat with older Log4j2.
        } else {
            logger.info("Ksyxis: Ready to remove unneeded chunks. (platform: fabric, version: " + KCompile.VERSION + ", mixin: {})", new Object[]{MixinBootstrap.VERSION}); // <- Array for compat with older Log4j2.
        }
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "Ksyxis/KFabric{}";
    }
}
