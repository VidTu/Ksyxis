/*
 * Ksyxis is a third-party mod for Minecraft Java Edition that
 * speed ups your world loading by removing spawn chunks.
 *
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

import com.google.errorprone.annotations.DoNotCall;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.vidtu.ksyxis.Ksyxis;

/**
 * Mixin for {@code ServerLevel} that disables spawn chunk tickets and sets {@code spawnChunkRadius} to {@code 0}.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(targets = {
        // Deobfuscated.
        "net.minecraft.server.level.ServerLevel", // Official Mojang
        "net.minecraft.server.world.ServerWorld", // Fabric Yarn
        "net.minecraft.world.server.ServerWorld", // Forge MCP

        // Obfuscated.
        "net.minecraft.class_3218", // Fabric Intermediary
        "net.minecraft.src.C_12_", // Forge SRG
        "net.minecraft.unmapped.C_bdwnwhiu", // Quilt Hashed
        "net.minecraft.unmapped.C_3865296" // Ornithe
}, remap = false)
@Pseudo
@NullMarked
public final class ServerLevelMixin {
    /**
     * Logger for this class.
     */
    @Unique
    private static final Logger KSYXIS_LOGGER = LogManager.getLogger("Ksyxis/ServerLevelMixin");

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private ServerLevelMixin() {
        throw new AssertionError("Ksyxis: No instances.");
    }

    /**
     * Injects into {@code ServerLevel.setDefaultSpawnPos} (Mojang mappings) to override
     * the {@code spawnChunkRadius} gamerule. Used since 1.20.6 (inclusive).
     *
     * @param spawnChunkRadius Previous {@code spawnChunkRadius} value for logging
     * @return Always {@code 0}
     * @apiNote Do not call, called by Mixin
     */
    @DoNotCall("Called by Mixin")
    @Contract(pure = true)
    @ModifyVariable(method = {
            // Deobfuscated
            "setDefaultSpawnPos(Lnet/minecraft/core/BlockPos;F)V", // Official Mojang
            "setDefaultSpawnPos(Lnet/minecraft/core/BlockPos;)V", // Official Mojang (Old)
            "setSpawnPos(Lnet/minecraft/util/math/BlockPos;F)V", // Fabric Yarn
            "setSpawnPos(Lnet/minecraft/util/math/BlockPos;)V", // Fabric Yarn (Old)
            "setSpawnLocation(Lnet/minecraft/util/math/BlockPos;F)V", // Forge MCP
            "setSpawnLocation(Lnet/minecraft/util/math/BlockPos;)V", // Forge MCP (Old)
            "setSpawnPoint(Lnet/minecraft/util/math/BlockPos;F)V", // Ornithe
            "setSpawnPoint(Lnet/minecraft/util/math/BlockPos;)V", // Ornithe (Old)

            // Obfuscated
            "method_8554(Lnet/minecraft/class_2338;F)V", // Fabric Intermediary
            "method_8554(Lnet/minecraft/class_2338;)V", // Fabric Intermediary (Old)
            "m_8733_(Lnet/minecraft/core/BlockPos;F)V", // Forge SRG (1.20.x)
            "m_8733_(Lnet/minecraft/src/C_4675_;F)V", // Forge SRG (1.17.x)
            "func_241124_a__(Lnet/minecraft/util/math/BlockPos;F)V", // Forge SRG (1.16.x)
            "func_241124_a__(Lnet/minecraft/util/math/BlockPos;)V", // Forge SRG (1.16.x/Old)
            "m_3711633(Lnet/minecraft/unmapped/C_3674802;)V", // Ornithe
            "m_3711633(Lnet/minecraft/unmapped/C_3674802;F)V" // Ornithe
    }, at = @At("STORE"), remap = false, require = 0, expect = 0, index = 5)
    private int ksyxis_setDefaultSpawnPos_spawnChunkRadius_getInt(int spawnChunkRadius) {
        // Report spawnChunkRadius gamerule as 0. Also log. (**DEBUG**)
        if (!KSYXIS_LOGGER.isDebugEnabled(Ksyxis.KSYXIS_MARKER)) return 0;
        KSYXIS_LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Reporting 0 as spawnChunkRadius gamerule in ServerLevelMixin. (previousSpawnChunks: {}, expectedPreviousSpawnChunks: from 0 to 32, level: {})", new Object[]{spawnChunkRadius, this}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.
        return 0;
    }

    /**
     * Injects into {@code ServerLevel.setDefaultSpawnPos} (Mojang mappings) to prevent loading chunks at the spawn
     * after setting it. Used in 1.14 (inclusive) through 1.20.4 (inclusive). Always returns {@code 0}.
     *
     * @param constant Previous constant value for logging
     * @return Always {@code 0}
     * @apiNote Do not call, called by Mixin
     */
    @DoNotCall("Called by Mixin")
    @Contract(pure = true)
    @ModifyConstant(method = {
            // Deobfuscated
            "setDefaultSpawnPos(Lnet/minecraft/core/BlockPos;F)V", // Official Mojang
            "setDefaultSpawnPos(Lnet/minecraft/core/BlockPos;)V", // Official Mojang (Old)
            "setSpawnPos(Lnet/minecraft/util/math/BlockPos;F)V", // Fabric Yarn
            "setSpawnPos(Lnet/minecraft/util/math/BlockPos;)V", // Fabric Yarn (Old)
            "setSpawnLocation(Lnet/minecraft/util/math/BlockPos;F)V", // Forge MCP
            "setSpawnLocation(Lnet/minecraft/util/math/BlockPos;)V", // Forge MCP (Old)
            "setSpawnPoint(Lnet/minecraft/util/math/BlockPos;F)V", // Ornithe
            "setSpawnPoint(Lnet/minecraft/util/math/BlockPos;)V", // Ornithe (Old)

            // Obfuscated
            "method_8554(Lnet/minecraft/class_2338;F)V", // Fabric Intermediary
            "method_8554(Lnet/minecraft/class_2338;)V", // Fabric Intermediary (Old)
            "m_8733_(Lnet/minecraft/core/BlockPos;F)V", // Forge SRG (1.20.x)
            "m_8733_(Lnet/minecraft/src/C_4675_;F)V", // Forge SRG (1.17.x)
            "func_241124_a__(Lnet/minecraft/util/math/BlockPos;F)V", // Forge SRG (1.16.x)
            "func_241124_a__(Lnet/minecraft/util/math/BlockPos;)V", // Forge SRG (1.16.x/Old)
            "m_3711633(Lnet/minecraft/unmapped/C_3674802;)V", // Ornithe
            "m_3711633(Lnet/minecraft/unmapped/C_3674802;F)V" // Ornithe
    }, constant = @Constant(intValue = 11), remap = false, require = 0, expect = 0)
    private int ksyxis_setDefaultSpawnPos_addRegionTicket(int constant) {
        // Add zero-level chunk loading ticket. Also log. (**DEBUG**)
        if (!KSYXIS_LOGGER.isDebugEnabled(Ksyxis.KSYXIS_MARKER)) return 0;
        KSYXIS_LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Adding zero-level ticket in ServerLevelMixin. (previousTicketLevel: {}, expectedPreviousTicketLevel: 11, level: {})", new Object[]{constant, this}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.
        return 0;
    }
}
