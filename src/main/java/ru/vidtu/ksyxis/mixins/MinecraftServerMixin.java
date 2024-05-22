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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.ksyxis.Ksyxis;

/**
 * Mixin that does some hacky things with the server to skip spawn chunk loading.
 *
 * @author VidTu
 */
@SuppressWarnings({"UnresolvedMixinReference", "MethodMayBeStatic", "DollarSignInName", "SpellCheckingInspection"}) // <- Multi-version and Mixin (x3).
@Mixin(targets = {
        // Deobfuscated
        "net.minecraft.server.MinecraftServer", // Basically everywhere (It should be deobfuscated)

        // Obfuscated
        "net.minecraft.src.C_4977_" // Forge SRG (pre-1.20) :skull:
}, remap = false)
@Pseudo
public final class MinecraftServerMixin {
    /**
     * Logger. Using Log4j logger, because SLF4J may not be available in older versions.
     */
    @Unique
    private static final Logger KSYXIS$LOGGER = LogManager.getLogger("Ksyxis/MinecraftServerMixin");

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private MinecraftServerMixin() {
        throw new AssertionError("No instances.");
    }

    // 1.20.6

    // Injects into MinecraftServer.prepareLevels (Mojang mappings) to override spawnChunkRadius gamerule.
    @ModifyVariable(method = {
            // Deobfuscated
            "prepareLevels(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Official Mojang
            "prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V", // Fabric Yarn
            "loadInitialChunks(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge MCP

            // Obfuscated
            "method_3774(Lnet/minecraft/class_3949;)V", // Fabric Intermediary
            "func_213186_a(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge SRG (1.16.x)
            "m_129940_(Lnet/minecraft/src/C_21_;)V", // Forge SRG (1.17.x)
            "m_129940_(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Forge SRG (1.20.x)
            "m_wcdfzsgy(Lnet/minecraft/unmapped/C_jnfclwgd;)V", // Quilt Hashed
            "m_4020281(Lnet/minecraft/unmapped/C_9126287;)V" // Ornithe Feather
    }, at = @At("STORE"), remap = false, require = 0, expect = 0, index = 5)
    public int ksyxis$prepareLevels$spawnChunkRadius$getInt(int spawnChunkRadius) {
        // Report spawn chunks gamerule as 0.
        KSYXIS$LOGGER.debug("Ksyxis: Reporting 0 as spawnChunkRadius gamerule instead of {} (expected 0 to 32) in MinecraftServerMixin.", new Object[]{spawnChunkRadius}); // <- Array for compat with log4j 2.0-beta.9.
        return 0;
    }

    // 1.14 -> 1.20.4

    // Injects into MinecraftServer.prepareLevels (Mojang mappings) to warn about possible pigs.
    @Inject(method = {
            // Deobfuscated
            "prepareLevels(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Official Mojang
            "prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V", // Fabric Yarn
            "loadInitialChunks(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge MCP

            // Obfuscated
            "method_3774(Lnet/minecraft/class_3949;)V", // Fabric Intermediary
            "func_213186_a(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge SRG (1.16.x)
            "m_129940_(Lnet/minecraft/src/C_21_;)V", // Forge SRG (1.17.x)
            "m_129940_(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Forge SRG (1.20.x)
            "m_wcdfzsgy(Lnet/minecraft/unmapped/C_jnfclwgd;)V", // Quilt Hashed
            "m_4020281(Lnet/minecraft/unmapped/C_9126287;)V" // Ornithe Feather
    }, at = @At("HEAD"), remap = false, require = 0, expect = 0)
    public void ksyxis$prepareLevels$head(CallbackInfo ci) {
        KSYXIS$LOGGER.info("Ksyxis: Hey. This is Ksyxis. We will now load the world and will try to do it quickly. If the game is not responding after this, it's probably us to blame or delete for good. This message appears always, even if the mod works flawlessly. (modern)");
    }

    // Injects into MinecraftServer.prepareLevels (Mojang mappings) to prevent loading chunks at the spawn.
    @ModifyConstant(method = {
            // Deobfuscated
            "prepareLevels(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Official Mojang
            "prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V", // Fabric Yarn
            "loadInitialChunks(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge MCP

            // Obfuscated
            "method_3774(Lnet/minecraft/class_3949;)V", // Fabric Intermediary
            "func_213186_a(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge SRG (1.16.x)
            "m_129940_(Lnet/minecraft/src/C_21_;)V", // Forge SRG (1.17.x)
            "m_129940_(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Forge SRG (1.20.x)
            "m_wcdfzsgy(Lnet/minecraft/unmapped/C_jnfclwgd;)V", // Quilt Hashed
            "m_4020281(Lnet/minecraft/unmapped/C_9126287;)V" // Ornithe Feather
    }, constant = @Constant(intValue = 11), remap = false, require = 0, expect = 0)
    public int ksyxis$prepareLevels$addRegionTicket(int constant) {
        // Add zero-level ticket.
        KSYXIS$LOGGER.debug("Ksyxis: Adding zero-level ticket instead of {} (expected 11) ticket in MinecraftServerMixin.", new Object[]{constant}); // <- Array for compat with log4j 2.0-beta.9.
        return 0;
    }

    // Injects into MinecraftServer.prepareLevels (Mojang mappings) to prevent game freezing
    // while trying to wait for 441 chunks that will never load.
    @ModifyConstant(method = {
            // Deobfuscated
            "prepareLevels(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Official Mojang
            "prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V", // Fabric Yarn
            "loadInitialChunks(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge MCP

            // Obfuscated
            "method_3774(Lnet/minecraft/class_3949;)V", // Fabric Intermediary
            "func_213186_a(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", // Forge SRG (1.16.x)
            "m_129940_(Lnet/minecraft/src/C_21_;)V", // Forge SRG (1.17.x)
            "m_129940_(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V", // Forge SRG (1.20.x)
            "m_wcdfzsgy(Lnet/minecraft/unmapped/C_jnfclwgd;)V", // Quilt Hashed
            "m_4020281(Lnet/minecraft/unmapped/C_9126287;)V" // Ornithe Feather
    }, constant = @Constant(intValue = 441), remap = false, require = 0, expect = 0)
    public int ksyxis$prepareLevels$getTickingGenerated(int constant) {
        // Wait for 0 chunks to load.
        int report = Ksyxis.loadedChunks();
        KSYXIS$LOGGER.debug("Ksyxis: Reporting {} (expected 0 or 441) loaded chunks instead of {} (expected 441) in MinecraftServerMixin.", new Object[]{report, constant}); // <- Array for compat with log4j 2.0-beta.9.
        return report;
    }

    // 1.8 -> 1.13.2

    // Injects into MinecraftServer.initialWorldChunkLoad (Forge MCP mappings) to warn about possible pigs.
    @Inject(method = {
            // Deobfuscated
            "initialWorldChunkLoad(Lnet/minecraft/world/storage/WorldSavedDataStorage;)V", // Forge MCP (1.13)
            "initialWorldChunkLoad()V", // Forge MCP (1.12)
            "prepareWorlds()V", // Legacy Fabric Yarn (1.12)
            "prepareWorlds(Lnet/minecraft/world/storage/DimensionDataStorage;)V", // Ornithe Feather (1.13)

            // Obfuscated
            "method_20317(Lnet/minecraft/class_4070;)V", // Legacy Fabric Intermediary (1.13)
            "method_3019()V", // Legacy Fabric Intermediary (1.12)
            "func_71222_d(Lnet/minecraft/world/storage/WorldSavedDataStorage;)V", // Forge SRG (1.13)
            "func_71222_d()V", // Forge SRG (1.12)
            "m_4020281(Lnet/minecraft/unmapped/C_8054043;)V", // Ornithe Feather (1.13)
            "m_4020281(Lnet/minecraft/unmapped/C_9126287;)V" // Ornithe Feather (1.12)
    }, at = @At("HEAD"), remap = false, require = 0, expect = 0)
    public void ksyxis$initialWorldChunkLoad$head(CallbackInfo ci) {
        // Warn people.
        KSYXIS$LOGGER.info("Ksyxis: Hey. This is Ksyxis. We will now load the world and will try to do it quickly. If the game is not responding after this, it's probably us to blame or delete for good. This message appears always, even if the mod works flawlessly. (legacy)");
    }

    // Injects into MinecraftServer.initialWorldChunkLoad (Forge MCP mappings) to prevent loading chunks at the spawn.
    @ModifyConstant(method = {
            // Deobfuscated
            "initialWorldChunkLoad(Lnet/minecraft/world/storage/WorldSavedDataStorage;)V", // Forge MCP (1.13)
            "initialWorldChunkLoad()V", // Forge MCP (1.12)
            "prepareWorlds()V", // Legacy Fabric Yarn (1.12)
            "prepareWorlds(Lnet/minecraft/world/storage/DimensionDataStorage;)V", // Ornithe Feather (1.13)

            // Obfuscated
            "method_20317(Lnet/minecraft/class_4070;)V", // Legacy Fabric Intermediary (1.13)
            "method_3019()V", // Legacy Fabric Intermediary (1.12)
            "func_71222_d(Lnet/minecraft/world/storage/WorldSavedDataStorage;)V", // Forge SRG (1.13)
            "func_71222_d()V", // Forge SRG (1.12)
            "m_4020281(Lnet/minecraft/unmapped/C_8054043;)V", // Ornithe (1.13)
            "m_4020281()V" // Ornithe (1.12)
    }, constant = {@Constant(intValue = -192), @Constant(intValue = 192)}, remap = false, require = 0, expect = 0)
    public int ksyxis$initialWorldChunkLoad$loop(int constant) {
        // Loop from 1 to -1. (don't loop)
        int report = constant < 0 ? 1 : -1;
        KSYXIS$LOGGER.debug("Ksyxis: Hijacking loop constant from {} to {} to prevent looping in MinecraftServerMixin.", new Object[]{constant, report}); // <- Array for compat with log4j 2.0-beta.9.
        return report;
    }
}
