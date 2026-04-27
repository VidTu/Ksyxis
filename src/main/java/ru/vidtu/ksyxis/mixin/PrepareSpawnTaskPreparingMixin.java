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

package ru.vidtu.ksyxis.mixin;

import com.google.errorprone.annotations.DoNotCall;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.vidtu.ksyxis.Ksyxis;
import ru.vidtu.ksyxis.platform.KCompile;

/**
 * Mixin for {@code PrepareSpawnTask$Preparing} that disables spawn chunk tickets after removal of spawn chunks.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(targets = {
        // Deobfuscated.
        "net.minecraft.server.network.config.PrepareSpawnTask$Preparing", // Official Mojang
        "net.minecraft.server.network.PrepareSpawnTask$LoadPlayerChunks", // Fabric Yarn

        // Obfuscated.
        "net.minecraft.class_11549$class_11550" // Fabric Intermediary
}, remap = false)
@Pseudo
@NullMarked
public final class PrepareSpawnTaskPreparingMixin {
    /**
     * Logger for this class.
     */
    @Unique
    @UnknownNullability
    private static final Logger KSYXIS_LOGGER = (KCompile.DEBUG_LOGS ? LogManager.getLogger("Ksyxis/PrepareSpawnTaskPreparingMixin") : null);

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private PrepareSpawnTaskPreparingMixin() {
        if (KCompile.DEBUG_ASSERTS) {
            throw new AssertionError("Ksyxis: No instances.");
        }
    }

    /**
     * Injects into a lambda in {@code PrepareSpawnTask$Ready.tick} (Mojang mappings) to
     * prevent loading chunks when logging in the player. Used since 1.21.9 (inclusive).
     *
     * @param ticket Previous constant value for logging
     * @return Always {@code 0}
     * @apiNote Do not call, called by Mixin
     */
    @DoNotCall("Called by Mixin")
    @Contract(pure = true)
    @ModifyConstant(method = {
            // Deobfuscated.
            "lambda$tick$0(Lnet/minecraft/world/level/ChunkPos;)V", // Official Mojang

            // Obfuscated.
            "method_72300(Lnet/minecraft/class_1923;)V" // Fabric Intermediary
    }, constant = @Constant(intValue = 3), remap = false, require = 0, expect = 0)
    private int ksyxis_lambdaTick0_addTicketAndLoadWithRadius(final int ticket) {
        // Assert.
        if (KCompile.DEBUG_ASSERTS) {
            // Should never happen on practice, constant Mixin.
            assert (ticket == 3) : "Ksyxis: Added ticket level is not 3 in PrepareSpawnTaskPreparingMixin. (ticket: " + ticket + ", server: " + this + ')';
        }

        // Log. (**DEBUG**)
        if (KCompile.DEBUG_LOGS && KSYXIS_LOGGER.isDebugEnabled(Ksyxis.KSYXIS_MARKER)) {
            KSYXIS_LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Adding zero-level ticket in PrepareSpawnTaskPreparingMixin. (ticket: {}, server: {})", new Object[]{ticket, this}); // <- Array for compat with older Log4j2.
        }

        // Add zero-level ticket.
        return 0;
    }
}
