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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.vidtu.ksyxis.Ksyxis;
import ru.vidtu.ksyxis.platform.KCompile;

/**
 * Mixin for {@code Minecraft} that disables fake load screens for single-player.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(targets = {
        // Deobfuscated.
        "net.minecraft.client.Minecraft", // Official Mojang
        "net.minecraft.client.MinecraftClient", // Fabric Yarn

        // Obfuscated.
        "net.minecraft.class_310" // Fabric Intermediary
}, remap = false)
@Pseudo
@NullMarked
public final class MinecraftMixin {
    /**
     * Logger for this class.
     */
    @Unique
    private static final Logger KSYXIS_LOGGER = LogManager.getLogger("Ksyxis/MinecraftMixin");

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private MinecraftMixin() {
        throw new AssertionError("Ksyxis: No instances.");
    }

    /**
     * Injects into {@code Minecraft.doWorldLoad} (Mojang mappings) to prevent
     * fake 500ms delay when creating the world. Used since 1.21.9 (inclusive).
     *
     * @param delay Previous constant value for logging
     * @return Always {@code 0}
     * @apiNote Do not call, called by Mixin
     */
    @ModifyConstant(method = {
            // Deobfuscated.
            "doWorldLoad(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/server/WorldStem;Ljava/util/Optional;Z)V", // Official Mojang (26.1)
            "doWorldLoad(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/server/WorldStem;Z)V", // Official Mojang
            "startIntegratedServer(Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/resource/ResourcePackManager;Lnet/minecraft/server/SaveLoader;Z)V", // Fabric Yarn

            // Obfuscated.
            "method_29610(Lnet/minecraft/class_32$class_5143;Lnet/minecraft/class_3283;Lnet/minecraft/class_6904;Z)V" // Fabric Intermediary
    }, constant = @Constant(longValue = 500L), remap = false, require = 0, expect = 0)
    private long ksyxis_doWorldLoad_closeDelayMs(final long delay) {
        // Assert.
        if (KCompile.DEBUG_ASSERTS) {
            // Should never happen on practice, constant Mixin.
            assert (delay == 500L) : "Ksyxis: Fake delay is not 500 in MinecraftMixin. (delay: " + delay + ", client: " + this + ')';
        }

        // Log. (**DEBUG**)
        if (KCompile.DEBUG_LOGS && KSYXIS_LOGGER.isDebugEnabled(Ksyxis.KSYXIS_MARKER)) {
            KSYXIS_LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Removing fake delay in MinecraftMixin. (delay: {}, client: {})", new Object[]{delay, this}); // <- Array for compat with older Log4j2.
        }

        // Remove fake delay.
        return 0L;
    }
}

