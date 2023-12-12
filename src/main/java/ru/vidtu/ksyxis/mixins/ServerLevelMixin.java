/*
 * MIT License
 *
 * Copyright (c) 2021-2023 VidTu
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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

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
        "net.minecraft.unmapped.C_bdwnwhiu" // Quilt Hashed
}, remap = false)
@Pseudo
public final class ServerLevelMixin {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private ServerLevelMixin() {
        throw new AssertionError("No instances.");
    }

    // Injects into ServerLevel.setDefaultSpawnPos (Mojang mappings) to prevent loading chunks at the spawn after setting it.
    @SuppressWarnings({"UnresolvedMixinReference"})
    @ModifyConstant(method = {
            // Deobfuscated
            "setDefaultSpawnPos(Lnet/minecraft/core/BlockPos;F)V", // Official Mojang
            "setDefaultSpawnPos(Lnet/minecraft/core/BlockPos;)V", // Official Mojang (Old)
            "setSpawnPos(Lnet/minecraft/util/math/BlockPos;F)V", // Fabric Yarn
            "setSpawnPos(Lnet/minecraft/util/math/BlockPos;)V", // Fabric Yarn (Old)
            "setSpawnLocation(Lnet/minecraft/util/math/BlockPos;F)V", // Forge MCP
            "setSpawnLocation(Lnet/minecraft/util/math/BlockPos;)V", // Forge MCP (Old)

            // Obfuscated
            "method_8554(Lnet/minecraft/class_2338;F)V", // Fabric Intermediary
            "method_8554(Lnet/minecraft/class_2338;)V", // Fabric Intermediary (Old)
            "m_8733_(Lnet/minecraft/core/BlockPos;F)V", // Forge SRG (1.20.x)
            "m_8733_(Lnet/minecraft/src/C_4675_;F)V", // Forge SRG (1.17.x)
            "func_241124_a__(Lnet/minecraft/util/math/BlockPos;F)V", // Forge SRG (1.16.x)
            "func_241124_a__(Lnet/minecraft/util/math/BlockPos;)V" // Forge SRG (1.16.x/Old)
    }, constant = @Constant(intValue = 11), remap = false, require = 0, expect = 0)
    public int ksyxis$setDefaultSpawnPos$addRegionTicket(int constant) {
        // Add zero-level ticket.
        return 0;
    }
}
