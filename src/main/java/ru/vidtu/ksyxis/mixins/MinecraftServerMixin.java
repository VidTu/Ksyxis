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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.ksyxis.Ksyxis;

/**
 * Mixin that does some hacky things with the server to skip spawn chunk loading.
 *
 * @author VidTu
 */
@Mixin(targets = {
        // Deobfuscated
        "net.minecraft.server.MinecraftServer", // Basically everywhere (It should be deobfuscated)

        // Obfuscated
        "net.minecraft.src.C_4977_" // Forge SRG (pre-1.20) :skull:
}, remap = false)
@Pseudo
public abstract class MinecraftServerMixin {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private MinecraftServerMixin() {
        throw new AssertionError("No instances.");
    }

    // 1.14+

    // Injects into MinecraftServer.prepareLevels (Mojang mappings) to warn about possible pigs.
    @SuppressWarnings({"UnresolvedMixinReference"})
    @Inject(method = {
            // Deobfuscated
            "prepareLevels(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Official Mojang
            "prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V", // Fabric Yarn
            "loadInitialChunks(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge MCP

            // Obfuscated
            "method_3774(Lnet/minecraft/class_3949;)V", // Fabric Intermediary
            "func_213186_a(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge SRG (1.16.x)
            "m_129940_(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Forge SRG (1.20.x)
            "m_wcdfzsgy(Lnet/minecraft/unmapped/C_jnfclwgd;)V" // Quilt Hashed
    }, at = @At("HEAD"), remap = false, require = 0)
    public void ksyxis$prepareLevels$head(CallbackInfo ci) {
        Ksyxis.world();
    }

    // Injects into MinecraftServer.prepareLevels (Mojang mappings) to prevent loading chunks at the spawn.
    @SuppressWarnings({"UnresolvedMixinReference"})
    @ModifyConstant(method = {
            // Deobfuscated
            "prepareLevels(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Official Mojang
            "prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V", // Fabric Yarn
            "loadInitialChunks(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge MCP

            // Obfuscated
            "method_3774(Lnet/minecraft/class_3949;)V", // Fabric Intermediary
            "func_213186_a(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge SRG (1.16.x)
            "m_129940_(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Forge SRG (1.20.x)
            "m_wcdfzsgy(Lnet/minecraft/unmapped/C_jnfclwgd;)V" // Quilt Hashed
    }, constant = @Constant(intValue = 11), remap = false, require = 0)
    public int ksyxis$prepareLevels$addRegionTicket(int constant) {
        // Add zero-level ticket.
        return 0;
    }

    // Injects into MinecraftServer.prepareLevels (Mojang mappings) to prevent game freezing
    // while trying to wait for 441 chunks that will never load.
    @SuppressWarnings({"UnresolvedMixinReference"})
    @ModifyConstant(method = {
            // Deobfuscated
            "prepareLevels(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Official Mojang
            "prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V", // Fabric Yarn
            "loadInitialChunks(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge MCP

            // Obfuscated
            "method_3774(Lnet/minecraft/class_3949;)V", // Fabric Intermediary
            "func_213186_a(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge SRG (1.16.x)
            "m_129940_(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Forge SRG (1.20.x)
            "m_wcdfzsgy(Lnet/minecraft/unmapped/C_jnfclwgd;)V" // Quilt Hashed
    }, constant = @Constant(intValue = 441), remap = false, require = 0)
    public int ksyxis$prepareLevels$getTickingGenerated(int constant) {
        // Wait for 0 chunks to load.
        return 0;
    }

    // 1.13

    // Injects into MinecraftServer.initialWorldChunkLoad (Forge MCP mappings) to warn about possible pigs.
    @SuppressWarnings({"UnresolvedMixinReference"})
    @Inject(method = {
            // Deobfuscated
            "initialWorldChunkLoad(Lnet/minecraft/world/storage/WorldSavedDataStorage;)V", // Forge MCP

            // Obfuscated
            "method_20317(Lnet/minecraft/class_4070;)V", // Legacy Fabric Intermediary
            "func_71222_d(Lnet/minecraft/world/storage/WorldSavedDataStorage;)V" // Forge SRG
    }, at = @At("HEAD"), remap = false, require = 0)
    public void ksyxis$initialWorldChunkLoad$head(CallbackInfo ci) {
        // Warn people.
        Ksyxis.world();
    }

    // Injects into MinecraftServer.initialWorldChunkLoad (Forge MCP mappings) to prevent loading chunks at the spawn.
    @SuppressWarnings({"UnresolvedMixinReference"})
    @ModifyConstant(method = {
            // Deobfuscated
            "initialWorldChunkLoad(Lnet/minecraft/world/storage/WorldSavedDataStorage;)V", // Forge MCP (1.13)
            "initialWorldChunkLoad()V", // Forge MCP (1.12)
            "prepareWorlds()V", // Legacy Fabric Yarn (1.12)

            // Obfuscated
            "method_20317(Lnet/minecraft/class_4070;)V", // Legacy Fabric Intermediary (1.13)
            "method_3019()V", // Legacy Fabric Intermediary (1.12)
            "func_71222_d(Lnet/minecraft/world/storage/WorldSavedDataStorage;)V", // Forge SRG (1.13)
            "func_71222_d()V" // Forge SRG (1.12)
    }, constant = {@Constant(intValue = -192), @Constant(intValue = 192)}, remap = false, require = 0)
    public int ksyxis$initialWorldChunkLoad$loop(int constant) {
        // Loop from 1 to -1. (don't loop)
        return constant < 0 ? 1 : -1;
    }
}
