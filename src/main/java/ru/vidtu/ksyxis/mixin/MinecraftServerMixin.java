/*
 * Ksyxis is a third-party mod for Minecraft Java Edition that
 * speed ups your world loading by removing spawn chunks.
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
import ru.vidtu.ksyxis.platform.KCompile;

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
     * Logger for this class.
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
        throw new AssertionError("Ksyxis: No instances.");
    }

    /**
     * Injects into {@code MinecraftServer.prepareLevels} (Mojang mappings) to override
     * the {@code spawnChunkRadius} gamerule. Used since 1.20.6 (inclusive).
     *
     * @param spawnChunks Previous {@code spawnChunkRadius} value for logging
     * @return Always {@code 0}
     * @apiNote Do not call, called by Mixin
     */
    @DoNotCall("Called by Mixin")
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
    private int ksyxis_prepareLevels_spawnChunkRadius_getInt(final int spawnChunks) {
        // Log. (**DEBUG**)
        if (KCompile.DEBUG_LOGS && KSYXIS_LOGGER.isDebugEnabled(Ksyxis.KSYXIS_MARKER)) {
            KSYXIS_LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Reporting 0 as spawnChunkRadius gamerule in MinecraftServerMixin. (spawnChunks: {}, server: {})", new Object[]{spawnChunks, this}); // <- Array for compat with older Log4j2.
        }

        // Report spawnChunkRadius gamerule as 0.
        return 0;
    }

    /**
     * Injects into {@code MinecraftServer.prepareLevels} (Mojang mappings) to warn
     * about possible issues. Used in 1.14 (inclusive) through 1.20.4 (inclusive).
     *
     * @param ci Callback data, ignored
     * @apiNote Do not call, called by Mixin
     */
    @DoNotCall("Called by Mixin")
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
    private void ksyxis_prepareLevels_head(final CallbackInfo ci) {
        // Log.
        KSYXIS_LOGGER.info(Ksyxis.KSYXIS_MARKER, "Ksyxis: Speeding up the world loading... Delete the mod, if it got stuck after this message. (method: modern, ci: {}, server: {})", new Object[]{ci, this}); // <- Array for compat with older Log4j2.
    }

    /**
     * Injects into {@code MinecraftServer.prepareLevels} (Mojang mappings) to prevent
     * loading chunks at the spawn. Used in 1.14 (inclusive) through 1.20.4 (inclusive).
     *
     * @param ticket Previous constant value for logging
     * @return Always {@code 0}
     * @apiNote Do not call, called by Mixin
     */
    @DoNotCall("Called by Mixin")
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
    private int ksyxis_prepareLevels_addRegionTicket(final int ticket) {
        // Log. (**DEBUG**)
        if (KCompile.DEBUG_LOGS && KSYXIS_LOGGER.isDebugEnabled(Ksyxis.KSYXIS_MARKER)) {
            KSYXIS_LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Adding zero-level ticket in MinecraftServerMixin. (ticket: {}, server: {})", new Object[]{ticket, this}); // <- Array for compat with older Log4j2.
        }

        // Add zero-level ticket.
        return 0;
    }

    /**
     * Injects into {@code MinecraftServer.prepareLevels} (Mojang mappings) to prevent
     * game freezing while trying to wait for {@code 441} chunks that will never load.
     * Returns {@code 0}. Does nothing with ModernFix and returns {@code 441}.
     *
     * @param oldChunks Previous constant value for logging
     * @return Always {@code 0} without ModernFix, always {@code 441} with ModernFix
     * @apiNote Do not call, called by Mixin
     * @see Ksyxis#LOADED_CHUNKS
     */
    @DoNotCall("Called by Mixin")
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
    }, constant = @Constant(intValue = 441), remap = false, require = 0, expect = 0)
    private int ksyxis_prepareLevels_getTickingGenerated(final int oldChunks) {
        // Get the amount of chunks to wait.
        final int chunks = Ksyxis.LOADED_CHUNKS;

        // Log. (**DEBUG**)
        if (KCompile.DEBUG_LOGS && KSYXIS_LOGGER.isDebugEnabled(Ksyxis.KSYXIS_MARKER)) {
            KSYXIS_LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Reporting fake loaded chunks in MinecraftServerMixin. (oldChunks: {}, chunks: {}, server: {})", new Object[]{oldChunks, chunks, this}); // <- Array for compat with older Log4j2.
        }

        // Wait for 0 OR 441 chunks to load.
        return chunks;
    }

    /**
     * Injects into {@code MinecraftServer.initialWorldChunkLoad} (Forge MCP mappings)
     * to warn about possible issues. Used before 1.13.2 (inclusive).
     *
     * @param ci Callback data, ignored
     * @apiNote Do not call, called by Mixin
     */
    @DoNotCall("Called by Mixin")
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
    private void ksyxis_initialWorldChunkLoad_head(final CallbackInfo ci) {
        // Log.
        KSYXIS_LOGGER.info(Ksyxis.KSYXIS_MARKER, "Ksyxis: Speeding up the world loading... Delete the mod, if it got stuck after this message. (method: legacy, ci: {}, server: {})", new Object[]{ci, this}); // <- Array for compat with older Log4j2.
    }

    /**
     * Injects into {@code MinecraftServer.initialWorldChunkLoad} (Forge MCP mappings)
     * to prevent loading chunks at the spawn. Used before 1.13.2 (inclusive).
     *
     * @param oldLoop Previous constant value for logging
     * @return Either {@code 1} or {@code -1}
     * @apiNote Do not call, called by Mixin
     */
    @DoNotCall("Called by Mixin")
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
    private int ksyxis_initialWorldChunkLoad_loop(final int oldLoop) {
        // Loop from 1 to -1 to actually prevent looping.
        final int loop = ((oldLoop < 0) ? 1 : -1);

        // Log. (**DEBUG**)
        if (KCompile.DEBUG_LOGS && KSYXIS_LOGGER.isDebugEnabled(Ksyxis.KSYXIS_MARKER)) {
            KSYXIS_LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Hijacking loop constant to prevent looping in MinecraftServerMixin. (oldLoop: {}, loop: {}, server: {})", new Object[]{oldLoop, loop, this}); // <- Array for compat with older Log4j2.
        }

        // Return.
        return loop;
    }
}
