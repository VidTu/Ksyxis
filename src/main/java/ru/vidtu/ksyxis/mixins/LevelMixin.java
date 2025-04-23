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
 *
 * SPDX-License-Identifier: MIT
 */

package ru.vidtu.ksyxis.mixins;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vidtu.ksyxis.Ksyxis;

/**
 * Mixin for {@code ServerLevel} that disables spawn chunk tickets in older versions.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(targets = {
        // Deobfuscated
        "net.minecraft.world.World", // Forge MCP + Forge SRG

        // Obfuscated
        "net.minecraft.class_1150", // Legacy Yarn
        "net.minecraft.unmapped.C_5553933" // Ornithe
}, remap = false)
@Pseudo
@NullMarked
public final class LevelMixin {
    /**
     * Logger for this class. Using Log4j2 logger, because SLF4J is not available in older versions.
     */
    @Unique
    private static final Logger KSYXIS_LOGGER = LogManager.getLogger("Ksyxis/LevelMixin");

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private LevelMixin() {
        throw new AssertionError("No instances.");
    }

    /**
     * Injects into {@code isSpawnChunk(int, int)} to always return {@code false} to prevent loading spawn chunks.
     * Used before 1.13.2 (inclusive).
     *
     * @param x   Chunk X, used only for logging
     * @param z   Chunk Z, used only for logging
     * @param cir Callback data to set {@code false} into
     */
    @Inject(method = {
            // Deobfuscated
            "isSpawnChunk(II)Z", // Forge MCP

            // Obfuscated
            "func_72916_c(II)Z", // Forge SRG
            "method_3671(II)Z", // Legacy Fabric Intermediary
            "m_4821236(II)Z" // Ornithe
    }, at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private void ksyxis_isSpawnChunk_head(int x, int z, CallbackInfoReturnable<Boolean> cir) {
        // Log. (**TRACE**)
        if (KSYXIS_LOGGER.isTraceEnabled(Ksyxis.KSYXIS_MARKER)) {
            KSYXIS_LOGGER.trace(Ksyxis.KSYXIS_MARKER, "Ksyxis: Forcing chunk to be not spawn chunk in LevelMixin. (x: {}, z: {}, cir: {}, level: {})", new Object[]{x, z, cir, this}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.
        }

        // Always force false to remove any spawn chunks from the world and allow them to be unloaded.
        cir.setReturnValue(false);
    }
}
