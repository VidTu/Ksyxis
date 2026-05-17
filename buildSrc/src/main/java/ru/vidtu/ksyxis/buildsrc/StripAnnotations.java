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

package ru.vidtu.ksyxis.buildsrc;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/// A collection of data about annotations to be stripped.
///
/// @author VidTu
/// @apiNote Internal use only
@ApiStatus.Internal
@NullMarked
public final class StripAnnotations {
    /// An immutable set of annotations VM names to strip.
    ///
    /// @see #STRIPPED_PACKAGES
    /// @see #shouldStripDescriptor(String)
    /// @see #shouldStripInternal(String)
    @Unmodifiable
    private static final Set<String> STRIPPED_ANNOTATIONS = Set.of(
            "java/lang/Deprecated"
    );

    /// An array of annotation VM prefixes (packages) to strip.
    ///
    /// Individual VM annotations are cached via [#STRIPPED_CACHE].
    ///
    /// @see #STRIPPED_ANNOTATIONS
    /// @see #STRIPPED_CACHE
    /// @see #shouldStripDescriptor(String)
    /// @see #shouldStripInternal(String)
    private static final String @Unmodifiable [] STRIPPED_PACKAGES = {
            "com/google/errorprone/annotations/",
            "org/intellij/lang/annotations/",
            "org/jetbrains/annotations/",
            "org/jspecify/annotations/"
    };

    /// A mutable cache for individual annotations for [#STRIPPED_PACKAGES].
    ///
    /// This cache is **not** thread-safe and it is the only part of the
    /// `static`(!) non-thread safe API in the *Strip* implementation.
    ///
    /// @see #STRIPPED_PACKAGES
    /// @see #shouldStripDescriptor(String)
    /// @see #shouldStripInternal(String)
    private static final Map<String, Boolean> STRIPPED_CACHE = new HashMap<>(32);

    /// An instance of this class cannot be created.
    ///
    /// @throws AssertionError Always
    /// @deprecated Always throws
    @Deprecated(forRemoval = true)
    @Contract(value = "-> fail", pure = true)
    private StripAnnotations() {
        throw new AssertionError("Ksyxis: No instances.");
    }

    /// Checks if the internal annotation VM descriptor should be stripped.
    ///
    /// Annotations will be stripped if their name is found in [#STRIPPED_ANNOTATIONS],
    /// or their name starts with a prefix (package) found in [#STRIPPED_PACKAGES].
    ///
    /// Unlike [#shouldStripInternal(String)], this method **IS** intended
    /// for whole descriptors, containing `L` at the beginning and `;` at the end.
    ///
    /// @param name Annotation internal VM descriptor to check
    /// @return `true` if the annotation should be stripped, `false` otherwise
    /// @see #STRIPPED_ANNOTATIONS
    /// @see #STRIPPED_PACKAGES
    /// @see #shouldStripInternal(String)
    @Contract(pure = true)
    static boolean shouldStripDescriptor(final String name) {
        // Validate.
        assert (name != null) : "Ksyxis: Parameter 'name' is null.";
        assert (!name.isBlank()) : "Ksyxis: Blank name. (name: " + name + ')';
        assert ((name.charAt(0) == 'L') && (name.charAt(name.length() - 1) == ';')) : "Ksyxis: Internal name used in descriptor name stripping. (name: " + name + ')';

        // Delegate.
        final String internal = name.substring(1, (name.length() - 1)); // Implicit NPE for 'name'
        return shouldStripInternal(internal);
    }

    /// Checks if the internal annotation VM name should be stripped.
    ///
    /// Annotations will be stripped if their name is found in [#STRIPPED_ANNOTATIONS],
    /// or their name starts with a prefix (package) found in [#STRIPPED_PACKAGES].
    ///
    /// Unlike [#shouldStripDescriptor(String)], this method is **NOT** intended for whole
    /// descriptors, so it should **NOT** contain `L` at the beginning and `;` at the end.
    ///
    /// @param name Annotation internal VM name to check
    /// @return `true` if the annotation should be stripped, `false` otherwise
    /// @see #STRIPPED_ANNOTATIONS
    /// @see #STRIPPED_PACKAGES
    /// @see #shouldStripDescriptor(String)
    @Contract(pure = true)
    static boolean shouldStripInternal(final String name) {
        // Validate.
        assert (name != null) : "Ksyxis: Parameter 'name' is null.";
        assert (!name.isBlank()) : "Ksyxis: Blank name. (name: " + name + ')';
        assert ((name.charAt(0) != 'L') || (name.charAt(name.length() - 1) != ';')) : "Ksyxis: Descriptor name used in internal name stripping. (name: " + name + ')';

        // Fast path for exact match: Strip if name directly matches one of desired annotations.
        if (STRIPPED_ANNOTATIONS.contains(name)) return true; // Implicit NPE for 'name'

        // Use the cache if available.
        return STRIPPED_CACHE.computeIfAbsent(name, (final String _) -> {
            // Strip if name starts with package name of desired annotations.
            for (final String pkg : STRIPPED_PACKAGES) {
                if (!name.startsWith(pkg)) continue;
                return true;
            }
            return false;
        });
    }
}
