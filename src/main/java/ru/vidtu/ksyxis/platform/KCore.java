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

package ru.vidtu.ksyxis.platform;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import ru.vidtu.ksyxis.Ksyxis;

import java.util.Map;

/**
 * Main Ksyxis class for Legacy Forge. (coremod hook)
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class KCore implements IFMLLoadingPlugin {
    /**
     * Creates a new coremod.
     */
    @Contract(pure = true)
    public KCore() {
        // Empty
    }

    /**
     * Always returns {@code null}
     *
     * @return Always {@code null}
     */
    @Contract(value = "-> null", pure = true)
    @Override
    @Nullable
    public String getAccessTransformerClass() {
        return null;
    }

    /**
     * Always returns {@code null}
     *
     * @return Always {@code null}
     */
    @Contract(value = "-> null", pure = true)
    @Override
    public String @Nullable [] getASMTransformerClass() {
        return null;
    }

    /**
     * Always returns {@code null}
     *
     * @return Always {@code null}
     */
    @Contract(value = "-> null", pure = true)
    @Override
    @Nullable
    public String getModContainerClass() {
        return null;
    }

    /**
     * Always returns {@code null}
     *
     * @return Always {@code null}
     */
    @Contract(value = "-> null", pure = true)
    @Override
    @Nullable
    public String getSetupClass() {
        return null;
    }

    /**
     * Calls {@link Ksyxis#init(String, boolean)} with {@code platform="LegacyForge"} and {@code manual=true}.
     *
     * @param data Injection data, ignored
     */
    @Override
    public void injectData(Map<String, Object> data) {
        Ksyxis.init("LegacyForge/KCore", /*manual=*/true);
    }
}
