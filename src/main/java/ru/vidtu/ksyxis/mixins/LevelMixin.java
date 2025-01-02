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

package ru.vidtu.ksyxis.mixins;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin that does some hacky things with the level to allow spawn chunks to unload in older versions.
 *
 * @author VidTu
 */
@Mixin(targets = {
        // Deobfuscated
        "net.minecraft.world.World", // Forge MCP + Forge SRG

        // Obfuscated
        "net.minecraft.class_1150", // Legacy Yarn
        "net.minecraft.unmapped.C_5553933" // Ornithe
}, remap = false)
@Pseudo
public final class LevelMixin {
    /**
     * Logger. Using Log4j logger, because SLF4J may not be available in older versions.
     */
    @Unique
    private static final Logger KSYXIS$LOGGER = LogManager.getLogger("Ksyxis/LevelMixin");

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private LevelMixin() {
        throw new AssertionError("No instances.");
    }

    // 1.8 -> 1.13

    @Inject(method = {
            // Deobfuscated
            "isSpawnChunk(II)Z", // Forge MCP

            // Obfuscated
            "func_72916_c(II)Z", // Forge SRG
            "method_3671(II)Z", // Legacy Fabric Intermediary
            "m_4821236(II)Z" // Ornithe
    }, at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    public void ksyxis$isSpawnChunk$head(int x, int z, CallbackInfoReturnable<Boolean> cir) {
        // Log but avoid useless allocations. (wrapping ints)
        if (KSYXIS$LOGGER.isTraceEnabled()) {
            KSYXIS$LOGGER.trace("Ksyxis: Forcing {}/{} to be not spawn chunk in LevelMixin. (we never knew if it was spawn chunk in the first place)", new Object[]{x, z}); // <- Array for compat with log4j 2.0-beta.9.
        }

        // Never spawn chunk.
        cir.setReturnValue(false);
    }
}
