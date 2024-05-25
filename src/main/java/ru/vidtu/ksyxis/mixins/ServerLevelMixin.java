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

package ru.vidtu.ksyxis.mixins;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mixin that does some hacky things with the level to allow spawn chunks to unload after changing spawn point.
 *
 * @author VidTu
 */
@Mixin(targets = {
        // Deobfuscated
        "net.minecraft.server.level.ServerLevel", // Official Mojang
        "net.minecraft.server.world.ServerWorld", // Fabric Yarn
        "net.minecraft.world.server.ServerWorld", // Forge MCP

        // Obfuscated
        "net.minecraft.class_3218", // Fabric Intermediary
        "net.minecraft.src.C_12_", // Forge SRG
        "net.minecraft.unmapped.C_bdwnwhiu", // Quilt Hashed
        "net.minecraft.unmapped.C_3865296" // Ornithe
}, remap = false)
@Pseudo
public final class ServerLevelMixin {
    /**
     * Logger. Using Log4j logger, because SLF4J may not be available in older versions.
     */
    @Unique
    private static final Logger KSYXIS$LOGGER = LogManager.getLogger("Ksyxis/ServerLevelMixin");

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private ServerLevelMixin() {
        throw new AssertionError("No instances.");
    }

    // 1.20.6+

    // Injects into ServerLevel.setDefaultSpawnPos (Mojang mappings) to override spawnChunkRadius gamerule.
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
    public int ksyxis$setDefaultSpawnPos$spawnChunkRadius$getInt(int spawnChunkRadius) {
        // Report spawn chunks gamerule as 0.
        KSYXIS$LOGGER.debug("Ksyxis: Reporting 0 as spawnChunkRadius gamerule instead of {} (expected 0 to 32) in ServerLevelMixin.", new Object[]{spawnChunkRadius}); // <- Array for compat with log4j 2.0-beta.9.
        return 0;
    }

    // 1.14 -> 1.20.4

    // Injects into ServerLevel.setDefaultSpawnPos (Mojang mappings) to prevent loading chunks at the spawn after setting it.
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
    public int ksyxis$setDefaultSpawnPos$addRegionTicket(int constant) {
        // Add zero-level ticket.
        KSYXIS$LOGGER.debug("Ksyxis: Adding zero-level ticket instead of {} (expected 11) ticket in ServerLevelMixin.", new Object[]{constant}); // <- Array for compat with log4j 2.0-beta.9.
        return 0;
    }
}
