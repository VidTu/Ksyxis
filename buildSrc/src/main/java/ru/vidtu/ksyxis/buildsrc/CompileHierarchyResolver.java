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

import com.google.errorprone.annotations.MustBeClosed;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.classfile.ClassHierarchyResolver;
import java.lang.constant.ClassDesc;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// A class hierarchy resolver that is designed to run after Gradle's `JavaCompile` tasks.
/// It can resolve hierarchies from the destination (just compiled) classes and the classpath JARs.
///
/// When the resolver is done being used, call the [#close()] method.
///
/// Instances of this class are **not** thread-safe.
///
/// @author VidTu
/// @apiNote Internal use only
@ApiStatus.Internal
@NullMarked
final class CompileHierarchyResolver implements ClassHierarchyResolver, Closeable {
    /// Compilation destination folder with classes.
    private final Path classes;

    /// A collection of the classpath entries. (JARs)
    private final Iterable<File> classpath;

    /// Delegate resolver.
    private final ClassHierarchyResolver delegate;

    /// Cache of open JAR file systems.
    private final Map<File, FileSystem> systems = new HashMap<>(0);

    /// Creates a new resolver.
    ///
    /// When the resolver is done being used, call the [#close()] method.
    ///
    /// @param classes   Compilation destination folder with classes
    /// @param classpath A collection of the classpath entries (JARs)
    @Contract(pure = true)
    CompileHierarchyResolver(final Path classes, final Iterable<File> classpath) {
        // Validate.
        assert (classes != null) : "Ksyxis: Parameter 'classes' is null. (classpath: " + classpath + ", resolver: " + this + ')';
        assert (classpath != null) : "Ksyxis: Parameter 'classpath' is null. (classes: " + classes + ", resolver: " + this + ')';

        // Assign.
        this.classes = classes;
        this.classpath = classpath;

        // Create the resolver.
        this.delegate = ClassHierarchyResolver.ofResourceParsing(this::resolveClassData);
    }

    /// Resolves the class info using [#delegate] which in turn is
    /// [parses][ClassHierarchyResolver#ofResourceParsing(ClassLoader)]
    /// the result from the [#resolveClassData(ClassDesc)].
    ///
    /// @throws RuntimeException If any error was encountered during classpath/classes scanning
    @CheckReturnValue
    @Override
    public ClassHierarchyInfo getClassInfo(final ClassDesc classDesc) {
        // Wrap.
        try {
            // Validate.
            assert (classDesc != null) : "Ksyxis: Parameter 'classDesc' is null. (resolver: " + this + ')';

            // Delegate.
            return this.delegate.getClassInfo(classDesc); // Implicit NPE for 'classDesc'
        } catch (final Throwable t) {
            // Rethrow.
            throw new RuntimeException("Ksyxis: Unable to resolve the class hierarchy info. (classDesc: " + classDesc + ", resolver: " + this + ')', t);
        }
    }

    /// Resolves the raw class data (as an open stream) from the [descriptor][ClassDesc].
    ///
    /// This resolves only the class data from the provided classes folder and classpath of JARs,
    /// it doesn't even do the internal JVM/JDK class resolving, use [ClassHierarchyResolver#defaultResolver()]
    /// for that (you can [chain][ClassHierarchyResolver#orElse(ClassHierarchyResolver)] it).
    ///
    /// Caller must [close][InputStream#close()] the returned stream, unless it is `null`.
    ///
    /// @param classDesc Class descriptor
    /// @return A newly opened input stream with raw class data, `null` if the class wasn't found
    /// @throws RuntimeException If any error was encountered during classpath/classes scanning
    @Contract("_ -> new")
    @CheckReturnValue
    @MustBeClosed
    @Nullable
    private InputStream resolveClassData(final ClassDesc classDesc) {
        // Wrap.
        try {
            // Validate.
            assert (classDesc != null) : "Ksyxis: Parameter 'classDesc' is null. (resolver: " + this + ')';

            // Skip non-classes.
            if (!classDesc.isClassOrInterface()) return null; // Implicit NPE for 'classDesc'

            // Resolve the name.
            final String descriptor = classDesc.descriptorString();
            final String name = (descriptor.substring(1, descriptor.length() - 1) + ".class");

            // Resolve the file from the classes folder, if exists.
            final Path path = this.classes.resolve(name);
            if (Files.isRegularFile(path)) {
                // Create the stream.
                return Files.newInputStream(path);
            }

            // Search class-path JARs.
            for (final File jar : this.classpath) {
                // Validate.
                assert (jar != null) : "Ksyxis: Classpath JAR is null. (classDesc: " + classDesc + ", resolver: " + this + ')';

                // Get or create the file-system.
                final FileSystem system = this.systems.computeIfAbsent(jar, CompileHierarchyResolver::openJarFileSystem);

                // Search for a file, skip if doesn't exist.
                final Path innerPath = system.getPath(name);
                if (!Files.isRegularFile(innerPath)) continue;

                // Create the stream.
                return Files.newInputStream(innerPath);
            }

            // Nothing found.
            return null;
        } catch (final Throwable t) {
            // Rethrow.
            throw new RuntimeException("Ksyxis: Unable to search for a class hierarchy. (classDesc: " + classDesc + ", resolver: " + this + ')', t);
        }
    }

    /// Closes the resolver, freeing any I/O resources.
    ///
    /// This is **NOT** a terminal operation, this flushes a cache of open JAR filesystems. You *may* reuse
    /// the resolver after calling this method, as long as you close it after you're done using. It is not
    /// recommended to reuse the resolver if it ever threw a [IOException] or a similar exception.
    ///
    /// @throws IOException If an I/O error occurs
    @Override
    public void close() throws IOException {
        // Shortcut.
        final Map<File, FileSystem> systems = this.systems;
        if (systems.isEmpty()) return;

        // Create an error list.
        final List<RuntimeException> errors = new ArrayList<>(systems.size());

        // Try to close each.
        for (final FileSystem system : systems.values()) {
            // Wrap.
            try {
                // Close.
                system.close();
            } catch (final Throwable t) {
                // Store.
                errors.add(new RuntimeException("Ksyxis: Unable to close the filesystem. (system: " + system + ", resolver: " + this + ')', t));
            }
        }

        // Clear the systems.
        systems.clear();

        // Stop if no errors.
        if (errors.isEmpty()) return;

        // Throw all errors.
        final IOException wrapper = new IOException("Ksyxis: Unable to close some filesystems, see suppressed errors for more details. (resolver: " + this + ')');
        for (final RuntimeException error : errors) {
            wrapper.addSuppressed(error);
        }
        throw wrapper;
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "Ksyxis/CompileHierarchyResolver{" +
                "classes=" + this.classes +
                ", classpath=" + this.classpath +
                ", delegate=" + this.delegate +
                ", systems=" + this.systems +
                '}';
    }

    /// Opens the specified JAR file as a [ZIP file-system][FileSystems#newFileSystem(Path)].
    ///
    /// This method is essentially a wrapper for the aforementioned call with two changes:
    ///
    /// 1. It accepts a [File] instead of a [Path].
    /// 2. It wraps any [IOException] into an [UncheckedIOException].
    ///
    /// It is designed to be used as a method reference.
    ///
    /// Caller must [close][FileSystem#close()] the returned system.
    ///
    /// @return A newly created file system
    /// @throws UncheckedIOException If an I/O error occurs
    @Contract("_ -> new")
    @CheckReturnValue
    @MustBeClosed
    private static FileSystem openJarFileSystem(final File jar) {
        // Validate.
        assert (jar != null) : "Ksyxis: Parameter 'jar' is null.";

        // Wrap.
        try {
            // Open.
            return FileSystems.newFileSystem(jar.toPath()); // Implicit NPE for 'jar'
        } catch (final IOException ioe) {
            // Rethrow.
            throw new UncheckedIOException("Ksyxis: Unable to open JAR as a filesystem. (jar: " + jar + ')', ioe);
        }
    }
}
