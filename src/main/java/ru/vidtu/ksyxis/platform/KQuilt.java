/*
 * Ksyxis is a third-party mod for Minecraft Java Edition that
 * speed ups your world loading by removing spawn chunks.
 *
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

import com.google.errorprone.annotations.DoNotCall;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import ru.vidtu.ksyxis.Ksyxis;

/**
 * Main Ksyxis class for Quilt.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see Ksyxis
 * @see KFabric
 */
@SuppressWarnings("unused") // <- Quilt mod.
@ApiStatus.Internal
@NullMarked
public final class KQuilt implements ModInitializer {
    /**
     * Creates a new mod.
     *
     * @apiNote Do not call, called by Quilt
     */
    @Contract(pure = true)
    public KQuilt() {
        // Empty.
    }

    /**
     * Calls {@link Ksyxis#init(String, boolean)} with {@code platform="Quilt"} and {@code manual=false}.
     *
     * @param mod Mod container, ignored
     * @apiNote Do not call, called by Quilt
     * @see Ksyxis#init(String, boolean)
     */
    @DoNotCall("Called by Quilt")
    @Override
    public void onInitialize(ModContainer mod) {
        Ksyxis.init("Quilt/KQuilt", /*manual=*/false);
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "Ksyxis/KQuilt{}";
    }
}
