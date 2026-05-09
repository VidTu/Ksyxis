/*
 * Ksyxis is a third-party mod for Minecraft Java Edition that
 * speed ups your world loading by removing unneeded chunks.
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

package ru.vidtu.ksyxis.compile;

import com.google.errorprone.annotations.CompileTimeConstant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

/**
 * A class that contains compile-time constants.
 * <p>
 * <b>Note:</b> This class is NEVER found in the final JAR. It <b>MUST NOT</b>
 * contain any references that are not inlined by the Java compiler.
 *
 * @author VidTu
 * @apiNote Internal use only
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.28">Compile-time References</a>
 * @see KCompileVariables
 */
@ApiStatus.Internal
@NullMarked
public final class KCompileConstants {
    /**
     * Amount of spawn chunks in versions {@code <1.20.5}.
     * <p>
     * Equals to {@code 441} chunks. ({@code 21x21})
     */
    @CompileTimeConstant
    public static final int CHUNK_AMOUNT_V1 = (21 * 21);

    /**
     * Amount of spawn chunks in versions {@code <1.20.5}.
     * <p>
     * Equals to {@code 192} chunks. ({@code 12} chunks to each side, inclusive)
     */
    @CompileTimeConstant
    public static final int CHUNK_BLOCK_RADIUS_V1 = 192;

    /**
     * Ticket level for spawn chunks in versions {@code <1.20.5}.
     * <p>
     * Equals to {@code 11}.
     */
    @CompileTimeConstant
    public static final int TICKET_LEVEL_V1 = 11;

    /**
     * Ticket level for "player" chunks in versions {@code >=1.21.9}.
     * <p>
     * Equals to {@code 3}.
     */
    @CompileTimeConstant
    public static final int TICKET_LEVEL_V3 = 3;

    /**
     * Maximum allowed spawn chunks game-rule value in {@code 1.20.5 -> 1.21.8}.
     * <p>
     * Equals to {@code 32}.
     */
    @CompileTimeConstant
    public static final int MAXIMUM_SPAWN_CHUNKS_V2 = 32;

    /**
     * Fake world creation delay found in versions {@code >=1.21.9}.
     * <p>
     * Equals to {@code 500} millis.
     */
    @CompileTimeConstant
    public static final long FAKE_DELAY_MS_V3 = 500L;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private KCompileConstants() {
        throw new AssertionError("Ksyxis: Compile-time code.");
    }
}
