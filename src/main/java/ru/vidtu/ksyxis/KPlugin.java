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

package ru.vidtu.ksyxis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.MixinService;

import java.util.List;
import java.util.Set;

/**
 * Mixin plugin that silences the console {@link ClassNotFoundException} errors from Ksyxis.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see Ksyxis
 */
@ApiStatus.Internal
@NullMarked
public final class KPlugin implements IMixinConfigPlugin {
    /**
     * Mixin plugin error message. Shown in {@link #shouldApplyMixin(String, String)} when an error occurs.
     *
     * @see #shouldApplyMixin(String, String)
     * @see Ksyxis#handleError(String, Throwable)
     */
    private static final String PLUGIN_ERROR = "Ksyxis: Unable to apply the Ksyxis plugin. It`s probably a bug or " +
            "something, you should report it via GitHub. Ensure to include as much information (game version, loader " +
            "type, loader version, mod version, other mods, logs, etc.) in the bug report as possible, this error " +
            "screen is NOT enough. If you don`t want any hassles and just want to load the game without solving " +
            "anything, delete the Ksyxis mod. (provider: %s, plugin: %s, targetClassName: %s, mixinClassName: %s)";

    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LogManager.getLogger("Ksyxis/KPlugin");

    /**
     * Current Mixin bytecode provider.
     */
    private final IClassBytecodeProvider provider = MixinService.getService().getBytecodeProvider();

    /**
     * Creates a new plugin.
     */
    @Contract(pure = true)
    public KPlugin() {
        // Empty
    }

    /**
     * Checks if the mixin should be applied. A mixin is applied, if its class node exists.
     *
     * @param targetClassName Fully qualified class name of the target class
     * @param mixinClassName  Fully qualified class name of the mixin
     * @return Whether the Mixin should be applied
     * @throws RuntimeException If any unexpected exception occurs (should never be thrown, app is exited)
     * @see #provider
     * @see IClassBytecodeProvider#getClassNode(String)
     * @see Ksyxis#handleError(String, Throwable)
     * @see #PLUGIN_ERROR
     */
    @CheckReturnValue
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Wrap to handle exceptions as a control flow.
        try {
            // If the Mixin class is not from Ksyxis, don't touch it and allow it to be applied.
            if (!mixinClassName.startsWith("ru.vidtu.ksyxis.mixins.")) {
                // Log. (**TRACE**)
                if (!LOGGER.isTraceEnabled(Ksyxis.KSYXIS_MARKER)) return true;
                LOGGER.trace(Ksyxis.KSYXIS_MARKER, "Ksyxis: Applying mixin, because it's not a part of Ksyxis. (provider: {}, plugin: {}, targetClassName: {}, mixinClassName: {})", new Object[]{this.provider, this, targetClassName, mixinClassName}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.
                return true;
            }

            // Get the node. This method should never return null. It returns the class node, if the class exists.
            // It throws ClassNotFoundException if the class doesn't exist. We handle the exception below.
            ClassNode node = this.provider.getClassNode(targetClassName);

            // It returned null...
            if (node == null) {
                throw new NullPointerException("Ksyxis: Bytecode provider returned null. (provider: " + this.provider + ", plugin: " + this + ", targetClassName: " + targetClassName + ", mixinClassName: " + mixinClassName + ')');
            }

            // Didn't throw - class exists. Log and apply. (**DEBUG**)
            if (!LOGGER.isDebugEnabled(Ksyxis.KSYXIS_MARKER)) return true;
            LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Bytecode provider returned a valid node, applying mixin... (provider: {}, plugin: {}, targetClassName: {}, mixinClassName: {})", new Object[]{this.provider, this, targetClassName, mixinClassName}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.
            return true;
        } catch (ClassNotFoundException cnfe) {
            // Provider threw an ClassNotFoundException. Don't apply mixin to avoid warnings. Log. (**DEBUG**)
            if (!LOGGER.isDebugEnabled(Ksyxis.KSYXIS_MARKER)) return false;
            LOGGER.debug(Ksyxis.KSYXIS_MARKER, "Ksyxis: Bytecode provider threw an exception, mixin WON'T be applied. (provider: {}, plugin: {}, targetClassName: {}, mixinClassName: {})", new Object[]{this.provider, this, targetClassName, mixinClassName, cnfe}); // <- Array for compat with Log4j2 2.0-beta.9 used in older MC versions.
            return false;
        } catch (Throwable t) {
            // Format the message.
            String message = String.format(PLUGIN_ERROR, this.provider, this, targetClassName, mixinClassName);

            // Handle the error.
            throw Ksyxis.handleError(message, t);
        }
    }

    /**
     * Does nothing.
     *
     * @param mixinPackage Ignored
     */
    @Contract(pure = true)
    @Override
    public void onLoad(String mixinPackage) {
        // NO-OP
    }

    /**
     * Does nothing. Always returns {@code null}.
     *
     * @return Always {@code null}
     */
    @Contract(value = "-> null", pure = true)
    @Override
    @Nullable
    public String getRefMapperConfig() {
        return null;
    }

    /**
     * Does nothing.
     *
     * @param myTargets    Ignored
     * @param otherTargets Ignored
     */
    @Contract(pure = true)
    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // NO-OP
    }

    /**
     * Does nothing. Always returns {@code null}.
     *
     * @return Always {@code null}
     */
    @Contract(value = "-> null", pure = true)
    @Override
    @Nullable
    public List<String> getMixins() {
        return null;
    }

    /**
     * Does nothing.
     *
     * @param targetClassName Ignored
     * @param targetClass     Ignored
     * @param mixinClassName  Ignored
     * @param mixinInfo       Ignored
     */
    @Contract(pure = true)
    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // NO-OP
    }

    /**
     * Does nothing.
     *
     * @param targetClassName Ignored
     * @param targetClass     Ignored
     * @param mixinClassName  Ignored
     * @param mixinInfo       Ignored
     */
    @Contract(pure = true)
    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // NO-OP
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "Ksyxis/KPlugin{" +
                "provider=" + this.provider +
                '}';
    }
}
