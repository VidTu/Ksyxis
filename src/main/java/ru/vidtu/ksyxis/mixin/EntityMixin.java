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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.ksyxis.Ksyxis;
import ru.vidtu.ksyxis.platform.KCompile;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * Mixin for {@code EntityMixin} that sets the changes the first entity ID from 0 to 1 on legacy
 * versions to avoid issues with data tracker treating {@code 0} as "absent entity" value.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(targets = {
        // Deobfuscated.
        "net.minecraft.entity.Entity", // Forge MCP + Forge SRG + Legacy Fabric Yarn + Ornithe Feather

        // Obfuscated.
        "net.minecraft.class_864", // Legacy Fabric Intermediary
        "net.minecraft.unmapped.C_0539808" // Ornithe Intermediary
}, remap = false)
@Pseudo
@NullMarked
public final class EntityMixin {
    /**
     * Logger for this class.
     */
    @Unique
    private static final Logger KSYXIS_LOGGER = (KCompile.DEBUG_LOGS ? LogManager.getLogger("Ksyxis/EntityMixin") : null);

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    // @ApiStatus.ScheduledForRemoval // Can't annotate this without logging in the console.
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private EntityMixin() {
        throw new AssertionError("Ksyxis: No instances.");
    }

    /**
     * Changes {@code nextEntityId} to {@code 1} so it is not {@code 0},
     * which can be lead to various issues if player's ID is {@code 0}.
     *
     * @param ci Callback data, ignored
     */
    @Inject(method = "<clinit>", at = @At("RETURN"), remap = false)
    private static void ksyxis_clinit_return(final CallbackInfo ci) {
        // Log. (**DEBUG**)
        if (KCompile.DEBUG_LOGS) {
            KSYXIS_LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Initializing Entity class, making sure first entity ID is not zero. Searching for the field in EntityMixin...");
        }

        // Create the lookup object.
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final Class<?> currentClass = lookup.lookupClass();

        // Search all fields.
        for (final String field : new String[]{
                // Deobfuscated.
                "nextEntityID", // Forge MCP
                "entityCount", // Legacy Fabric Yarn
                "nextNetworkId", // Ornithe Feather

                // Obfuscated.
                "field_70152_a", // Forge SRG
                "field_3219", // Legacy Fabric Intermediary
                "f_7990365" // Ornithe Intermediary
        }) {
            try {
                // Attempt to find and set to one.
                final MethodHandle setter = lookup.findStaticSetter(currentClass, field, int.class);
                setter.invokeExact((int) 1);

                // Log. (**DEBUG**)
                if (KCompile.DEBUG_LOGS && KSYXIS_LOGGER.isDebugEnabled(Ksyxis.KSYXIS_MARKER)) {
                    KSYXIS_LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Set first entity ID to one in EntityMixin. (field: {}, setter: {})", new Object[]{field, setter}); // <- Array for compat with older Log4j2.
                }

                // Done.
                return;
            } catch (final Throwable t) {
                // Log. (**TRACE**)
                if (KCompile.DEBUG_LOGS && KSYXIS_LOGGER.isTraceEnabled(Ksyxis.KSYXIS_MARKER)) {
                    KSYXIS_LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Field error, skipping in EntityMixin. (field: {})", new Object[]{field, t}); // <- Array for compat with older Log4j2.
                }
            }
        }
    }
}
