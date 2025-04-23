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
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.ksyxis.Ksyxis;

/**
 * Mixin for {@code ServerLevel} that disables waiting for spawn chunks and sets {@code spawnChunkRadius} to {@code 0}.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(targets = {
        // Deobfuscated.
        "net.minecraft.server.MinecraftServer", // Basically everywhere

        // Obfuscated.
        "net.minecraft.src.C_4977_" // Forge SRG
}, remap = false)
@Pseudo
@NullMarked
public final class MinecraftServerMixin {
    /**
     * Logger for this class. Using Log4j2 logger, because SLF4J is not available in older versions.
     */
    @Unique
    private static final Logger KSYXIS_LOGGER = LogManager.getLogger("Ksyxis/MinecraftServerMixin");

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private MinecraftServerMixin() {
        throw new AssertionError("No instances.");
    }

    /**
     * Injects into {@code MinecraftServer.prepareLevels} (Mojang mappings) to override
     * the {@code spawnChunkRadius} gamerule. Used since 1.20.6 (inclusive).
     *
     * @param spawnChunkRadius Previous {@code spawnChunkRadius} value for logging
     * @return Always {@code 0}
     */
    @Contract(pure = true)
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
    private int ksyxis_prepareLevels_spawnChunkRadius_getInt(int spawnChunkRadius) {
        // Report spawnChunkRadius gamerule as 0. Also log. (**DEBUG**)
        if (!KSYXIS_LOGGER.isDebugEnabled(Ksyxis.KSYXIS_MARKER)) return 0;
        KSYXIS_LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Reporting 0 as spawnChunkRadius gamerule in MinecraftServerMixin. (previousSpawnChunks: {}, expectedPreviousSpawnChunks: from 0 to 32, server: {})", new Object[]{spawnChunkRadius, this}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.
        return 0;
    }

    /**
     * Injects into {@code MinecraftServer.prepareLevels} (Mojang mappings) to warn about possible issues.
     * Used in 1.14 (inclusive) through 1.20.4 (inclusive).
     *
     * @param ci Callback data, ignored
     */
    @Contract(pure = true)
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
    private void ksyxis_prepareLevels_head(CallbackInfo ci) {
        // Log.
        KSYXIS_LOGGER.info(Ksyxis.KSYXIS_MARKER, "Ksyxis: Hey. This is Ksyxis. We will now load the world and will try to do it quickly. If the game is not responding after this, it's probably us to blame or delete for good. This message appears always, even if the mod works flawlessly. (injector: modern, ci: {}, server: {})", new Object[]{ci, this}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.
    }

    /**
     * Injects into {@code MinecraftServer.prepareLevels} (Mojang mappings) to prevent loading chunks at the spawn.
     * Used in 1.14 (inclusive) through 1.20.4 (inclusive).
     *
     * @param constant Previous constant value for logging
     * @return Always {@code 0}
     */
    @Contract(pure = true)
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
    private int ksyxis_prepareLevels_addRegionTicket(int constant) {
        // Add zero-level ticket. Also log. (**DEBUG**)
        if (!KSYXIS_LOGGER.isDebugEnabled(Ksyxis.KSYXIS_MARKER)) return 0;
        KSYXIS_LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Adding zero-level ticket in MinecraftServerMixin. (previousTicketLevel: {}, expectedPreviousTicketLevel: 11, server: {})", new Object[]{constant, this}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.
        return 0;
    }

    /**
     * Injects into {@code MinecraftServer.prepareLevels} (Mojang mappings) to prevent game freezing
     * while trying to wait for {@link Ksyxis#SPAWN_CHUNKS} chunks that will never load. Returns {@code 0}.
     * Does nothing with ModernFix and returns {@link Ksyxis#SPAWN_CHUNKS}.
     *
     * @param constant Previous constant value for logging
     * @return Always {@code 0} without ModernFix, always {@link Ksyxis#SPAWN_CHUNKS} with ModernFix
     */
    @Contract(pure = true)
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
    }, constant = @Constant(intValue = Ksyxis.SPAWN_CHUNKS), remap = false, require = 0, expect = 0)
    private int ksyxis_prepareLevels_getTickingGenerated(int constant) {
        // Wait for 0 chunks to load. Also log. (**DEBUG**)
        if (!KSYXIS_LOGGER.isDebugEnabled(Ksyxis.KSYXIS_MARKER)) return Ksyxis.LOADED_CHUNKS;
        KSYXIS_LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Reporting fake loaded chunks in MinecraftServerMixin. (fakeLoaded: {}, expectedFakeLoaded: 0 or 441, reallyLoaded: {}, expectedReallyLoaded: 441, server: {})", new Object[]{Ksyxis.LOADED_CHUNKS, constant, this}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.
        return Ksyxis.LOADED_CHUNKS;
    }

    /**
     * Injects into {@code MinecraftServer.initialWorldChunkLoad} (Forge MCP mappings) to warn about possible issues.
     * Used before 1.13.2 (inclusive).
     *
     * @param ci Callback data, ignored
     */
    @Contract(pure = true)
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
    private void ksyxis_initialWorldChunkLoad_head(CallbackInfo ci) {
        // Log.
        KSYXIS_LOGGER.info(Ksyxis.KSYXIS_MARKER, "Ksyxis: Hey. This is Ksyxis. We will now load the world and will try to do it quickly. If the game is not responding after this, it's probably us to blame or delete for good. This message appears always, even if the mod works flawlessly. (injector: legacy, ci: {}, server: {})", new Object[]{ci, this}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.
    }

    /**
     * Injects into {@code MinecraftServer.initialWorldChunkLoad} (Forge MCP mappings) to prevent loading
     * chunks at the spawn. Used before 1.13.2 (inclusive).
     *
     * @param constant Previous constant value for logging
     * @return Either {@code 1} or {@code -1}
     */
    @Contract(pure = true)
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
    private int ksyxis_initialWorldChunkLoad_loop(int constant) {
        // Loop from 1 to -1 to prevent looping. Also log. (**DEBUG**)
        int report = ((constant < 0) ? 1 : -1);
        if (!KSYXIS_LOGGER.isDebugEnabled(Ksyxis.KSYXIS_MARKER)) return report;
        KSYXIS_LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Hijacking loop constant to prevent looping in MinecraftServerMixin. (from: {}, to: {}, server: {})", new Object[]{constant, report, this}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.
        return report;
    }
}
