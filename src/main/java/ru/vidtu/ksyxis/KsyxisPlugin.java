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

package ru.vidtu.ksyxis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.List;
import java.util.Set;

/**
 * Mixin plugin that silences the console {@link ClassNotFoundException} errors.
 *
 * @author VidTu
 */
public final class KsyxisPlugin implements IMixinConfigPlugin {
    /**
     * Logger. Using Log4j logger, because SLF4J may not be available in older versions.
     */
    private static final Logger LOGGER = LogManager.getLogger("Ksyxis");

    @Override
    public void onLoad(String mixinPackage) {
        // NO-OP
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Not ours - don't touch.
        if (!mixinClassName.startsWith("ru.vidtu.ksyxis.mixins.")) {
            return true;
        }

        // Check if class exists.
        try {
            // Only apply if node exists.
            ClassNode node = MixinService.getService().getBytecodeProvider().getClassNode(targetClassName);
            return node != null;
        } catch (ClassNotFoundException e) {
            // Not found - don't apply.
            LOGGER.debug("Ksyxis: Not applying {} to {}, class not found.", new Object[]{mixinClassName, targetClassName, e}); // <- Array for compat with log4j 2.0-beta.9.
            return false;
        } catch (Throwable t) {
            // Try my lucky day, apply anyway.
            LOGGER.debug("Ksyxis: Unexpected error while trying to guess whether to apply {} to {}, will apply anyway.", new Object[]{mixinClassName, targetClassName, t}); // <- Array for compat with log4j 2.0-beta.9.
            return true;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // NO-OP
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // NO-OP
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // NO-OP
    }
}
