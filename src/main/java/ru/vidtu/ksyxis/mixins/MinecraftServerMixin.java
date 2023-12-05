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
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.ksyxis.Ksyxis;

/**
 * Mixin that does some hacky things with the server.
 *
 * @author VidTu
 */
@Mixin(remap = false, targets = "net.minecraft.server.MinecraftServer")
public abstract class MinecraftServerMixin {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private MinecraftServerMixin() {
        throw new AssertionError("No instances.");
    }

    // MinecraftServer -> prepareLevels -> getTickingGenerated

    @SuppressWarnings({"UnresolvedMixinReference", "UnnecessaryQualifiedMemberReference"})
    @Inject(method = {
            // Deobfuscated
            "Lnet/minecraft/server/MinecraftServer;prepareLevels(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Official Mojang
            "Lnet/minecraft/server/MinecraftServer;prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V", // Fabric Yarn
            "Lnet/minecraft/server/MinecraftServer;loadInitialChunks(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge MCP

            // Obfuscated
            "Lnet/minecraft/server/MinecraftServer;method_3774(Lnet/minecraft/class_3949;)V", // Fabric Intermediary
            "Lnet/minecraft/server/MinecraftServer;func_213186_a(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge SRG (1.16.x)
            "Lnet/minecraft/server/MinecraftServer;m_129940_(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Forge SRG (1.20.x)
            "Lnet/minecraft/server/MinecraftServer;m_wcdfzsgy(Lnet/minecraft/unmapped/C_jnfclwgd;)V" // Quilt Hashed
    }, at = @At("HEAD"), remap = false)
    public void ksyxis$prepareLevels$head(CallbackInfo ci) {
        Ksyxis.world();
    }


    // Injects into MinecraftServer.prepareLevels (Mojang mappings) to prevent loading chunks at the spawn.
    @SuppressWarnings({"UnresolvedMixinReference", "UnnecessaryQualifiedMemberReference"})
    @ModifyConstant(method = {
            // Deobfuscated
            "Lnet/minecraft/server/MinecraftServer;prepareLevels(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Official Mojang
            "Lnet/minecraft/server/MinecraftServer;prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V", // Fabric Yarn
            "Lnet/minecraft/server/MinecraftServer;loadInitialChunks(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge MCP

            // Obfuscated
            "Lnet/minecraft/server/MinecraftServer;method_3774(Lnet/minecraft/class_3949;)V", // Fabric Intermediary
            "Lnet/minecraft/server/MinecraftServer;func_213186_a(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge SRG (1.16.x)
            "Lnet/minecraft/server/MinecraftServer;m_129940_(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Forge SRG (1.20.x)
            "Lnet/minecraft/server/MinecraftServer;m_wcdfzsgy(Lnet/minecraft/unmapped/C_jnfclwgd;)V" // Quilt Hashed
    }, constant = @Constant(intValue = 11), remap = false)
    public int ksyxis$prepareLevels$addRegionTicket(int constant) {
        return 0;
    }

    // Injects into MinecraftServer.prepareLevels (Mojang mappings) to prevent game freezing
    // while trying to wait for 441 chunks that will never load.
    @SuppressWarnings({"UnresolvedMixinReference", "UnnecessaryQualifiedMemberReference"})
    @ModifyConstant(method = {
            // Deobfuscated
            "Lnet/minecraft/server/MinecraftServer;prepareLevels(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Official Mojang
            "Lnet/minecraft/server/MinecraftServer;prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V", // Fabric Yarn
            "Lnet/minecraft/server/MinecraftServer;loadInitialChunks(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge MCP

            // Obfuscated
            "Lnet/minecraft/server/MinecraftServer;method_3774(Lnet/minecraft/class_3949;)V", // Fabric Intermediary
            "Lnet/minecraft/server/MinecraftServer;func_213186_a(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge SRG (1.16.x)
            "Lnet/minecraft/server/MinecraftServer;m_129940_(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Forge SRG (1.20.x)
            "Lnet/minecraft/server/MinecraftServer;m_wcdfzsgy(Lnet/minecraft/unmapped/C_jnfclwgd;)V" // Quilt Hashed
    }, constant = @Constant(intValue = 441), remap = false)
    public int ksyxis$prepareLevels$getTickingGenerated(int constant) {
        return 0;
    }
}
