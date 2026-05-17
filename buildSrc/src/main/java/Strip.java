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

import com.google.errorprone.annotations.MustBeClosed;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.classfile.Annotation;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassFileBuilder;
import java.lang.classfile.ClassHierarchyResolver;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.FieldBuilder;
import java.lang.classfile.FieldElement;
import java.lang.classfile.FieldTransform;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.MethodElement;
import java.lang.classfile.MethodTransform;
import java.lang.classfile.TypeAnnotation;
import java.lang.classfile.attribute.DeprecatedAttribute;
import java.lang.classfile.attribute.InnerClassInfo;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute;
import java.lang.classfile.attribute.SourceFileAttribute;
import java.lang.constant.ClassDesc;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/// A build-time class that performs metadata stripping
/// from Java classes to reduce the final JAR size.
///
/// When the strip is done being used, call the [#close()] method.
///
/// @author VidTu
@NullMarked
public final class Strip implements Closeable {
    /// An immutable set of annotations VM names to strip.
    ///
    /// @see #STRIPPED_PACKAGES
    /// @see #shouldStripTyped(String)
    /// @see #shouldStripTypeless(String)
    @Unmodifiable
    private static final Set<String> STRIPPED_ANNOTATIONS = Set.of(
            "java/lang/Deprecated"
    );

    /// An immutable list of annotation VM prefixes (packages) to strip.
    ///
    /// Individual VM annotations are cached via [#STRIPPED_CACHE].
    ///
    /// @see #STRIPPED_ANNOTATIONS
    /// @see #STRIPPED_CACHE
    /// @see #shouldStripTyped(String)
    /// @see #shouldStripTypeless(String)
    @Unmodifiable
    private static final List<String> STRIPPED_PACKAGES = List.of(
            "com/google/errorprone/annotations/",
            "org/intellij/lang/annotations/",
            "org/jetbrains/annotations/",
            "org/jspecify/annotations/"
    );

    /// A mutable cache for individual annotations for [#STRIPPED_PACKAGES].
    ///
    /// @see #STRIPPED_PACKAGES
    /// @see #shouldStripTyped(String)
    /// @see #shouldStripTypeless(String)
    private static final Map<String, Boolean> STRIPPED_CACHE = new HashMap<>(32);

    /// A `SourceFile` attribute made up of an empty string. (`""`)
    ///
    /// When no source is specified, Java doesn't display line numbers in stack-traces. We want line numbers in
    /// stack-traces, but we want to save JAR size at any cost, but free of charge. Empty string will do!
    ///
    /// @see #stripClass(ClassBuilder, ClassElement)
    private static final SourceFileAttribute EMPTY_SOURCE = SourceFileAttribute.of("");

    /// A [ClassTransform] that strips all [strippable][#shouldStripTypeless(String)] annotations from all elements
    /// in the class, including the class itself, class [fields][ClassTransform#transformingFields(FieldTransform)],
    /// class [methods][ClassTransform#transformingMethods(MethodTransform)] (including their parameters).
    /// Method code is not being transformed. (for now)
    ///
    /// Additionally, this transform strips all [@deprecated][DeprecatedAttribute] attributes
    /// and replaces the [source file][SourceFileAttribute] attributes with an empty string.
    ///
    /// @see #stripClass(ClassBuilder, ClassElement)
    /// @see #stripField(FieldBuilder, FieldElement)
    /// @see #stripMethod(MethodBuilder, MethodElement)
    private static final ClassTransform STRIP_ATTRIBUTES_TRANSFORM;

    static {
        // Create the transforms.
        final ClassTransform stripClass = Strip::stripClass;
        final ClassTransform stripFields = ClassTransform.transformingFields(Strip::stripField);
        final ClassTransform stripMethods = ClassTransform.transformingMethods(Strip::stripMethod);

        // Merge the transforms.
        STRIP_ATTRIBUTES_TRANSFORM = stripClass.andThen(stripFields).andThen(stripMethods);
    }

    /// Class-file context with a [new pool][ClassFile.ConstantPoolSharingOption#NEW_POOL] for each class
    /// and a custom [hierarchy resolver][ClassFile.ClassHierarchyResolverOption] with the code class-path.
    private final ClassFile context;

    /// Cache of open JAR file systems.
    private final Map<File, FileSystem> systems = new HashMap<>(0);

    /// Creates a new strip.
    ///
    /// When the strip is done being used, call the [#close()] method.
    ///
    /// @param classesFolder Compilation destination folder with all the compiles classes for [hierarchy resolving][ClassFile.ClassHierarchyResolverOption]
    /// @param classpathJars A collection of the JARs on the class-path
    @Contract(pure = true)
    public Strip(final File classesFolder, final Iterable<File> classpathJars) {
        // Validate.
        assert (classesFolder != null) : "Ksyxis: Parameter 'classesFolder' is null. (classpathJars: " + classpathJars + ", strip: " + this + ')';
        assert (classpathJars != null) : "Ksyxis: Parameter 'classpathJars' is null. (classesFolder: " + classesFolder + ", strip: " + this + ')';

        // Create the resolver from the compilation folder.
        final Path classesFolderPath = classesFolder.toPath(); // Implicit NPE for 'classesFolder'
        final ClassHierarchyResolver classPathResolver = ClassHierarchyResolver.ofResourceParsing((final ClassDesc desc)
                -> this.resolveClassData(desc, classesFolderPath, classpathJars));

        // Create the general resolver. It:
        // 1. Searches the system classes. (Java classes)
        // 2. Searches the compilation class-path. (see above)
        // 3. Caches that data for future re-use.
        final ClassHierarchyResolver resolver = ClassHierarchyResolver.defaultResolver()
                .orElse(classPathResolver)
                .cached();

        // Create the context.
        this.context = ClassFile.of(ClassFile.ConstantPoolSharingOption.NEW_POOL, ClassFile.ClassHierarchyResolverOption.of(resolver));
    }

    /// Reads the class-file bytecode from the file, strips the decoration attributes via
    /// [#STRIP_ATTRIBUTES_TRANSFORM], and writes the bytecode into the same file.
    ///
    /// @param classFile File to strip
    /// @throws RuntimeException If class transformation fails
    public void stripBytecode(final File classFile) {
        // Wrap.
        try {
            // Validate.
            assert (classFile != null) : "Ksyxis: Parameter 'classFile' is null. (strip: " + this + ')';
            assert (classFile.isFile()) : "Class-file is not a file. (classFile: " + classFile + ", strip: " + this + ')';

            // Parse.
            final Path classFilePath = classFile.toPath(); // Implicit NPE for 'classFile'
            final ClassModel input = this.context.parse(classFilePath);

            // Transform.
            final byte[] output = this.context.transformClass(input, STRIP_ATTRIBUTES_TRANSFORM);

            // Validate.
            final List<VerifyError> errors = this.context.verify(output);
            if (!errors.isEmpty()) {
                final RuntimeException wrapper = new RuntimeException("Ksyxis: Class failed verification, see suppressed errors for more details.");
                for (final VerifyError error : errors) {
                    wrapper.addSuppressed(error);
                }
                throw wrapper;
            }

            // Write.
            Files.write(classFilePath, output);
        } catch (final Throwable t) {
            // Rethrow.
            throw new RuntimeException("Ksyxis: Unable to strip class-file bytecode. (classFile: " + classFile + ", strip: " + this + ')', t);
        }
    }

    /// Closes the strip, freeing any I/O resources.
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
                errors.add(new RuntimeException("Ksyxis: Unable to close the filesystem. (system: " + system + ')', t));
            }
        }

        // Clear the systems.
        systems.clear();

        // Stop if no errors.
        if (errors.isEmpty()) return;

        // Throw all errors.
        final IOException wrapper = new IOException("Ksyxis: Unable to close some filesystems, see suppressed errors for more details.");
        for (final RuntimeException error : errors) {
            wrapper.addSuppressed(error);
        }
        throw wrapper;
    }

    /// Resolves the raw class data (as an open stream) from the [descriptor][ClassDesc].
    ///
    /// This resolves only the class data from the provided classes folder and classpath of JARs,
    /// it doesn't even do the internal JVM/JDK class resolving, use [ClassHierarchyResolver#defaultResolver()]
    /// for that (you can [chain][ClassHierarchyResolver#orElse(ClassHierarchyResolver)] it).
    ///
    /// Caller must [close][InputStream#close()] the returned stream, unless it is `null`.
    ///
    /// @param desc          Class descriptor
    /// @param classesFolder Folder with all the compiled classes
    /// @param classpathJars All the classpath JARs
    /// @return A newly opened input stream with raw class data, `null` if the class wasn't found
    /// @throws RuntimeException If any error was encountered during classpath/classes scanning
    @Contract("_, _, _ -> new")
    @CheckReturnValue
    @MustBeClosed
    @Nullable
    private InputStream resolveClassData(final ClassDesc desc, final Path classesFolder,
                                         final Iterable<File> classpathJars) {
        // Wrap.
        try {
            // Validate.
            assert (desc != null) : "Ksyxis: Parameter 'desc' is null. (classesFolder: " + classesFolder + ", classpathJars: " + classpathJars + ", strip: " + this + ')';
            assert (classesFolder != null) : "Ksyxis: Parameter 'classesFolder' is null. (desc: " + desc + ", classpathJars: " + classpathJars + ", strip: " + this + ')';
            assert (classpathJars != null) : "Ksyxis: Parameter 'classpathJars' is null. (desc: " + desc + ", classesFolder: " + classesFolder + ", strip: " + this + ')';

            // Skip non-classes.
            if (!desc.isClassOrInterface()) return null; // Implicit NPE for 'desc'

            // Resolve the name.
            final String descriptor = desc.descriptorString();
            final String name = (descriptor.substring(1, descriptor.length() - 1) + ".class");

            // Resolve the file from the classes folder, if exists.
            final Path file = classesFolder.resolve(name); // Implicit NPE for 'classesFolder'
            if (Files.isRegularFile(file)) {
                // Create the stream.
                return Files.newInputStream(file);
            }

            // Search class-path JARs.
            for (final File classpathJar : classpathJars) { // Implicit NPE for 'classpathJars'
                // Validate.
                assert (classpathJar != null) : "Ksyxis: Classpath JAR is null. (desc: " + desc + ", classesFolder: " + classesFolder + ", classpathJars: " + classpathJars + ", strip: " + this + ')';

                // Get or create the file-system.
                final FileSystem fs = this.systems.computeIfAbsent(classpathJar, Strip::openJarFileSystem);

                // Search for a file, skip if doesn't exist.
                final Path innerFile = fs.getPath(name);
                if (!Files.isRegularFile(innerFile)) continue;

                // Create the stream.
                return Files.newInputStream(innerFile);
            }

            // Nothing found.
            return null;
        } catch (final Throwable t) {
            // Rethrow.
            throw new RuntimeException("Ksyxis: Unable to perform the class-path search for a class hierarchy. (desc: " + desc + ", classesFolder: " + classesFolder + ", classpathJars: " + classpathJars + ", strip: " + this + ')', t);
        }
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "Ksyxis/Strip{" +
                "context=" + this.context +
                ", systems=" + this.systems +
                '}';
    }

    /// Strips [class elements][ClassElement] from a [class builder][ClassBuilder].
    ///
    /// Can be used as a method reference of a [ClassTransform].
    ///
    /// @see #stripField(FieldBuilder, FieldElement)
    /// @see #stripMethod(MethodBuilder, MethodElement)
    /// @see #stripInvisible(Consumer, RuntimeInvisibleAnnotationsAttribute)
    /// @see #stripInvisibleType(Consumer, RuntimeInvisibleTypeAnnotationsAttribute)
    /// @see #stripVisible(Consumer, RuntimeVisibleAnnotationsAttribute)
    /// @see #stripVisibleType(Consumer, RuntimeVisibleTypeAnnotationsAttribute)
    /// @see #stripInnerClasses(Consumer, InnerClassesAttribute)
    private static void stripClass(final ClassBuilder builder, final ClassElement element) {
        // Validate.
        assert (builder != null) : "Ksyxis: Parameter 'builder' is null. (element: " + element + ')';
        assert (element != null) : "Ksyxis: Parameter 'element' is null. (builder: " + builder + ')';

        // Strip.
        switch (element) { // Implicit NPE for 'element'
            // Remove the deprecated attribute, it serves no VM purpose.
            case final DeprecatedAttribute _ -> {}

            // Strip the annotations. This doesn't remove all annotations,
            // only those subjected by the "shouldStripTyped(String)" method.
            case final RuntimeInvisibleAnnotationsAttribute attribute -> stripInvisible(builder, attribute); // Implicit NPE for 'builder'
            case final RuntimeInvisibleTypeAnnotationsAttribute attribute -> stripInvisibleType(builder, attribute); // Implicit NPE for 'builder'
            case final RuntimeVisibleAnnotationsAttribute attribute -> stripVisible(builder, attribute); // Implicit NPE for 'builder'
            case final RuntimeVisibleTypeAnnotationsAttribute attribute -> stripVisibleType(builder, attribute); // Implicit NPE for 'builder'

            // Strip the information about inner classes, this often contains information
            // about annotations like @ApiStatus.Internal (their outer/inner data).
            // Note, as with the annotations, only selective classes are stripped.
            case final InnerClassesAttribute attribute -> stripInnerClasses(builder, attribute); // Implicit NPE for 'builder'

            // Strip the source file attribute.
            case final SourceFileAttribute _ -> builder.with(EMPTY_SOURCE); // Implicit NPE for 'builder'

            // Pass all other elements as-is.
            default -> builder.with(element); // Implicit NPE for 'builder'
        }
    }

    /// Strips [field elements][FieldElement] from a [field builder][FieldBuilder].
    ///
    /// Can be used as a method reference of a [FieldTransform]
    /// or a [ClassTransform#transformingFields(FieldTransform)].
    ///
    /// @see #stripClass(ClassBuilder, ClassElement)
    /// @see #stripMethod(MethodBuilder, MethodElement)
    /// @see #stripInvisible(Consumer, RuntimeInvisibleAnnotationsAttribute)
    /// @see #stripInvisibleType(Consumer, RuntimeInvisibleTypeAnnotationsAttribute)
    /// @see #stripVisible(Consumer, RuntimeVisibleAnnotationsAttribute)
    /// @see #stripVisibleType(Consumer, RuntimeVisibleTypeAnnotationsAttribute)
    private static void stripField(final FieldBuilder builder, final FieldElement element) {
        // Validate.
        assert (builder != null) : "Ksyxis: Parameter 'builder' is null. (element: " + element + ')';
        assert (element != null) : "Ksyxis: Parameter 'element' is null. (builder: " + builder + ')';

        // Strip.
        switch (element) { // Implicit NPE for 'element'
            // Remove the deprecated attribute, it serves no VM purpose.
            case final DeprecatedAttribute _ -> {}

            // Strip the annotations. This doesn't remove all annotations,
            // only those subjected by the "shouldStripTyped(String)" method.
            case final RuntimeInvisibleAnnotationsAttribute attribute -> stripInvisible(builder, attribute); // Implicit NPE for 'builder'
            case final RuntimeInvisibleTypeAnnotationsAttribute attribute -> stripInvisibleType(builder, attribute); // Implicit NPE for 'builder'
            case final RuntimeVisibleAnnotationsAttribute attribute -> stripVisible(builder, attribute); // Implicit NPE for 'builder'
            case final RuntimeVisibleTypeAnnotationsAttribute attribute -> stripVisibleType(builder, attribute); // Implicit NPE for 'builder'

            // Pass all other elements as-is.
            default -> builder.with(element); // Implicit NPE for 'builder'
        }
    }

    /// Strips [method elements][MethodElement] from a [method builder][MethodElement].
    ///
    /// Can be used as a method reference of a [MethodTransform]
    /// or a [ClassTransform#transformingMethods(MethodTransform)].
    ///
    /// @see #stripClass(ClassBuilder, ClassElement)
    /// @see #stripField(FieldBuilder, FieldElement)
    /// @see #stripInvisible(Consumer, RuntimeInvisibleAnnotationsAttribute)
    /// @see #stripInvisibleParameter(Consumer, RuntimeInvisibleParameterAnnotationsAttribute)
    /// @see #stripInvisibleType(Consumer, RuntimeInvisibleTypeAnnotationsAttribute)
    /// @see #stripVisible(Consumer, RuntimeVisibleAnnotationsAttribute)
    /// @see #stripVisibleParameter(Consumer, RuntimeVisibleParameterAnnotationsAttribute)
    /// @see #stripVisibleType(Consumer, RuntimeVisibleTypeAnnotationsAttribute)
    private static void stripMethod(final MethodBuilder builder, final MethodElement element) {
        // Validate.
        assert (builder != null) : "Ksyxis: Parameter 'builder' is null. (element: " + element + ')';
        assert (element != null) : "Ksyxis: Parameter 'element' is null. (builder: " + builder + ')';

        // Strip.
        switch (element) { // Implicit NPE for 'element'
            // Remove the deprecated attribute, it serves no VM purpose.
            case final DeprecatedAttribute _ -> {}

            // Strip the annotations. This doesn't remove all annotations,
            // only those subjected by the "shouldStripTyped(String)" method.
            case final RuntimeInvisibleAnnotationsAttribute attribute -> stripInvisible(builder, attribute); // Implicit NPE for 'builder'
            case final RuntimeInvisibleParameterAnnotationsAttribute attribute -> stripInvisibleParameter(builder, attribute); // Implicit NPE for 'builder'
            case final RuntimeInvisibleTypeAnnotationsAttribute attribute -> stripInvisibleType(builder, attribute); // Implicit NPE for 'builder'
            case final RuntimeVisibleAnnotationsAttribute attribute -> stripVisible(builder, attribute); // Implicit NPE for 'builder'
            case final RuntimeVisibleParameterAnnotationsAttribute attribute -> stripVisibleParameter(builder, attribute); // Implicit NPE for 'builder'
            case final RuntimeVisibleTypeAnnotationsAttribute attribute -> stripVisibleType(builder, attribute); // Implicit NPE for 'builder'

            // Pass all other elements as-is.
            default -> builder.with(element);
        }
    }

    /// Strips the [inner class metadata][InnerClassInfo] with rules from [#shouldStripTypeless(String)].
    /// Non-stripped class attributers will be passed to the `builder` for processing.
    ///
    /// @param builder Consumer for non-stripped annotations, usually the [ClassFileBuilder]
    /// @param element Element to strip the annotations from
    /// @see #shouldStripTypeless(String)
    private static void stripInnerClasses(final Consumer<? super InnerClassesAttribute> builder,
                                          final InnerClassesAttribute element) {
        // Validate.
        assert (builder != null) : "Ksyxis: Parameter 'builder' is null. (element: " + element + ')';
        assert (element != null) : "Ksyxis: Parameter 'element' is null. (builder: " + builder + ')';

        // Extract.
        final List<InnerClassInfo> classes = element.classes(); // Implicit NPE for 'element'
        assert (classes != null) : "Ksyxis: Classes are null. (builder: " + builder + ", element: " + element + ')';

        // Shortcut.
        if (classes.isEmpty()) return; // PERF: Singleton. // Implicit NPE for 'classes'

        // Create a new list.
        final List<InnerClassInfo> newClasses = new ArrayList<>(classes.size()); 

        // Filter the old list into the new list.
        for (final InnerClassInfo clazz : classes) {
            // Validate.
            assert (clazz != null) : "Ksyxis: Class is null. (classes: " + classes + ", builder: " + builder + ", element: " + element + ')';

            // Check, strip if needed.
            if (shouldStripTypeless(clazz.innerClass().asInternalName())) continue; // Implicit NPE for 'clazz'

            // Add if not stripped.
            newClasses.add(clazz);
        }

        // Skip if no classes left after stripping.
        if (newClasses.isEmpty()) return;

        // Add.
        builder.accept(InnerClassesAttribute.of(newClasses)); // Implicit NPE for 'builder'
    }

    /// Strips the invisible [annotations][Annotation] via [#stripList(List)].
    /// Non-stripped annotations will be passed to the `builder` for processing.
    ///
    /// @param builder Consumer for non-stripped annotations, usually the [ClassFileBuilder]
    /// @param element Element to strip the annotations from
    /// @see #stripList(List)
    /// @see #stripVisible(Consumer, RuntimeVisibleAnnotationsAttribute)
    private static void stripInvisible(final Consumer<? super RuntimeInvisibleAnnotationsAttribute> builder,
                                       final RuntimeInvisibleAnnotationsAttribute element) {
        // Validate.
        assert (builder != null) : "Ksyxis: Parameter 'builder' is null. (element: " + element + ')';
        assert (element != null) : "Ksyxis: Parameter 'element' is null. (builder: " + builder + ')';

        // Strip.
        final List<Annotation> stripped = stripList(element.annotations()); // Implicit NPE for 'element'

        // Skip if no annotations left after stripping.
        if (stripped.isEmpty()) return; // Implicit NPE for 'stripped'

        // Add.
        builder.accept(RuntimeInvisibleAnnotationsAttribute.of(stripped)); // Implicit NPE for 'builder'
    }

    /// Strips the invisible [parameter annotations][Annotation] via [#stripParameterList(List)].
    /// Non-stripped annotations will be passed to the `builder` for processing.
    ///
    /// @param builder Consumer for non-stripped annotations, usually the [ClassFileBuilder]
    /// @param element Element to strip the annotations from
    /// @see #stripParameterList(List)
    /// @see #stripVisibleParameter(Consumer, RuntimeVisibleParameterAnnotationsAttribute)
    private static void stripInvisibleParameter(final Consumer<? super RuntimeInvisibleParameterAnnotationsAttribute> builder,
                                                final RuntimeInvisibleParameterAnnotationsAttribute element) {
        // Validate.
        assert (builder != null) : "Ksyxis: Parameter 'builder' is null. (element: " + element + ')';
        assert (element != null) : "Ksyxis: Parameter 'element' is null. (builder: " + builder + ')';

        // Strip.
        final List<List<Annotation>> stripped = stripParameterList(element.parameterAnnotations()); // Implicit NPE for 'element'

        // Skip if no annotations left after stripping.
        if (stripped.isEmpty()) return; // Implicit NPE for 'stripped'

        // Add.
        builder.accept(RuntimeInvisibleParameterAnnotationsAttribute.of(stripped)); // Implicit NPE for 'builder'
    }

    /// Strips the invisible [type annotations][TypeAnnotation] via [#stripTypeList(List)].
    /// Non-stripped annotations will be passed to the `builder` for processing.
    ///
    /// @param builder Consumer for non-stripped annotations, usually the [ClassFileBuilder]
    /// @param element Element to strip the annotations from
    /// @see #stripTypeList(List)
    /// @see #stripVisibleType(Consumer, RuntimeVisibleTypeAnnotationsAttribute)
    private static void stripInvisibleType(final Consumer<? super RuntimeInvisibleTypeAnnotationsAttribute> builder,
                                           final RuntimeInvisibleTypeAnnotationsAttribute element) {
        // Validate.
        assert (builder != null) : "Ksyxis: Parameter 'builder' is null. (element: " + element + ')';
        assert (element != null) : "Ksyxis: Parameter 'element' is null. (builder: " + builder + ')';

        // Strip.
        final List<TypeAnnotation> stripped = stripTypeList(element.annotations()); // Implicit NPE for 'element'

        // Skip if no annotations left after stripping.
        if (stripped.isEmpty()) return; // Implicit NPE for 'stripped'

        // Add.
        builder.accept(RuntimeInvisibleTypeAnnotationsAttribute.of(stripped)); // Implicit NPE for 'builder'
    }

    /// Strips the visible [annotations][Annotation] via [#stripList(List)].
    /// Non-stripped annotations will be passed to the `builder` for processing.
    ///
    /// @param builder Consumer for non-stripped annotations, usually the [ClassFileBuilder]
    /// @param element Element to strip the annotations from
    /// @see #stripList(List)
    /// @see #stripInvisible(Consumer, RuntimeInvisibleAnnotationsAttribute)
    private static void stripVisible(final Consumer<? super RuntimeVisibleAnnotationsAttribute> builder,
                                     final RuntimeVisibleAnnotationsAttribute element) {
        // Validate.
        assert (builder != null) : "Ksyxis: Parameter 'builder' is null. (element: " + element + ')';
        assert (element != null) : "Ksyxis: Parameter 'element' is null. (builder: " + builder + ')';

        // Strip.
        final List<Annotation> stripped = stripList(element.annotations()); // Implicit NPE for 'element'

        // Skip if no annotations left after stripping.
        if (stripped.isEmpty()) return; // Implicit NPE for 'stripped'

        // Add.
        builder.accept(RuntimeVisibleAnnotationsAttribute.of(stripped)); // Implicit NPE for 'builder'
    }

    /// Strips the visible [parameter annotations][Annotation] via [#stripParameterList(List)].
    /// Non-stripped annotations will be passed to the `builder` for processing.
    ///
    /// @param builder Consumer for non-stripped annotations, usually the [ClassFileBuilder]
    /// @param element Element to strip the annotations from
    /// @see #stripParameterList(List)
    /// @see #stripInvisibleParameter(Consumer, RuntimeInvisibleParameterAnnotationsAttribute)
    private static void stripVisibleParameter(final Consumer<? super RuntimeVisibleParameterAnnotationsAttribute> builder,
                                              final RuntimeVisibleParameterAnnotationsAttribute element) {
        // Validate.
        assert (builder != null) : "Ksyxis: Parameter 'builder' is null. (element: " + element + ')';
        assert (element != null) : "Ksyxis: Parameter 'element' is null. (builder: " + builder + ')';

        // Strip.
        final List<List<Annotation>> stripped = stripParameterList(element.parameterAnnotations()); // Implicit NPE for 'element'

        // Skip if no annotations left after stripping.
        if (stripped.isEmpty()) return; // Implicit NPE for 'stripped'

        // Add.
        builder.accept(RuntimeVisibleParameterAnnotationsAttribute.of(stripped)); // Implicit NPE for 'builder'
    }

    /// Strips the visible [type annotations][TypeAnnotation] via [#stripTypeList(List)].
    /// Non-stripped annotations will be passed to the `builder` for processing.
    ///
    /// @param builder Consumer for non-stripped annotations, usually the [ClassFileBuilder]
    /// @param element Element to strip the annotations from
    /// @see #stripTypeList(List)
    /// @see #stripInvisibleType(Consumer, RuntimeInvisibleTypeAnnotationsAttribute)
    private static void stripVisibleType(final Consumer<? super RuntimeVisibleTypeAnnotationsAttribute> builder,
                                         final RuntimeVisibleTypeAnnotationsAttribute element) {
        // Validate.
        assert (builder != null) : "Ksyxis: Parameter 'builder' is null. (element: " + element + ')';
        assert (element != null) : "Ksyxis: Parameter 'element' is null. (builder: " + builder + ')';

        // Strip.
        final List<TypeAnnotation> stripped = stripTypeList(element.annotations()); // Implicit NPE for 'element'

        // Skip if no annotations left after stripping.
        if (stripped.isEmpty()) return; // Implicit NPE for 'stripped'

        // Add.
        builder.accept(RuntimeVisibleTypeAnnotationsAttribute.of(stripped)); // Implicit NPE for 'builder'
    }

    /// Strips the [annotations][Annotation] from the list according
    /// to the rules described in [#shouldStripTyped(String)].
    ///
    /// @param annotations Annotations list to strip
    /// @return A new list of annotations without stripped ones, an empty list if all were stripped
    /// @see #stripParameterList(List)
    /// @see #stripTypeList(List)
    /// @see #shouldStripTyped(String)
    /// @see #stripInvisible(Consumer, RuntimeInvisibleAnnotationsAttribute)
    /// @see #stripVisible(Consumer, RuntimeVisibleAnnotationsAttribute)
    @Contract(value = "_ -> new", pure = true)
    private static List<Annotation> stripList(final List<Annotation> annotations) {
        // Validate.
        assert (annotations != null) : "Ksyxis: Parameter 'annotations' is null.";

        // Shortcut.
        if (annotations.isEmpty()) return List.of(); // PERF: Singleton. // Implicit NPE for 'annotations'

        // Create a list.
        final List<Annotation> newAnnotations = new ArrayList<>(annotations.size());

        // Filter the old list into the new list.
        for (final Annotation annotation : annotations) {
            // Validate.
            assert (annotation != null) : "Ksyxis: Annotation is null. (annotations: " + annotations + ')';

            // Check, strip if needed.
            if (shouldStripTyped(annotation.className().stringValue())) continue; // Implicit NPE for 'annotation'

            // Add if not stripped.
            newAnnotations.add(annotation);
        }

        // Return the new list.
        return newAnnotations;
    }

    /// Strips the [parameter annotations][Annotation] from the list of lists
    /// according to the rules described in [#shouldStripTyped(String)].
    ///
    /// @param annotations Parameter annotations list of lists to strip
    /// @return A new list of parameter annotations without stripped ones, an empty list if all were stripped
    /// @see #stripList(List)
    /// @see #stripTypeList(List)
    /// @see #shouldStripTyped(String)
    /// @see #stripInvisibleParameter(Consumer, RuntimeInvisibleParameterAnnotationsAttribute)
    /// @see #stripVisibleParameter(Consumer, RuntimeVisibleParameterAnnotationsAttribute)
    @Contract(value = "_ -> new", pure = true)
    private static List<List<Annotation>> stripParameterList(final List<List<Annotation>> annotations) {
        // Validate.
        assert (annotations != null) : "Ksyxis: Parameter 'annotations' is null.";

        // Shortcut.
        if (annotations.isEmpty()) return List.of(); // PERF: Singleton. // Implicit NPE for 'annotations'

        // Create a list of lists.
        final List<List<Annotation>> newAnnotations = new ArrayList<>(annotations.size());

        // See below.
        /*non-final*/ boolean noAnnotations = true;

        // Filter the old list of lists into the new list of lists.
        for (final List<Annotation> list : annotations) {
            // Validate.
            assert (list != null) : "Ksyxis: List is null. (annotations: " + annotations + ')';

            // Shortcut.
            if (list.isEmpty()) { // Implicit NPE for 'list'
                newAnnotations.add(List.of()); // PERF: Singleton.
                continue;
            }

            // Create a list.
            final List<Annotation> newList = new ArrayList<>(list.size());

            // Filter the old list into the new list.
            for (final Annotation annotation : list) {
                // Validate.
                assert (annotation != null) : "Ksyxis: Annotation is null. (list: " + list + ", annotations: " + annotations + ')';

                // Check, strip if needed.
                if (shouldStripTyped(annotation.className().stringValue())) continue; // Implicit NPE for 'annotation'

                // Add if not stripped. Set the flag.
                newList.add(annotation);
                noAnnotations = false;
            }

            // Add the new list into the new list of lists.
            newAnnotations.add(newList);
        }

        // If no annotations are preserved, we can skip parameter list fully.
        // This is done by returning an empty list.
        if (noAnnotations) return List.of(); // PERF: Singleton.

        // Return the new list of lists.
        return newAnnotations;
    }

    /// Strips the [type annotations][TypeAnnotation] from the list
    /// according to the rules described in [#shouldStripTyped(String)].
    ///
    /// @param annotations Type annotations list to strip
    /// @return A new list of type annotations without stripped ones, an empty list if all were stripped
    /// @see #stripList(List)
    /// @see #stripParameterList(List)
    /// @see #shouldStripTyped(String)
    /// @see #stripInvisibleType(Consumer, RuntimeInvisibleTypeAnnotationsAttribute)
    /// @see #stripVisibleType(Consumer, RuntimeVisibleTypeAnnotationsAttribute)
    @Contract(value = "_ -> new", pure = true)
    private static List<TypeAnnotation> stripTypeList(final List<TypeAnnotation> annotations) {
        // Validate.
        assert (annotations != null) : "Ksyxis: Parameter 'annotations' is null.";

        // Shortcut.
        if (annotations.isEmpty()) return List.of(); // PERF: Singleton. // Implicit NPE for 'annotations'

        // Create a list.
        final List<TypeAnnotation> newAnnotations = new ArrayList<>(annotations.size());

        // Filter the old list into the new list.
        for (final TypeAnnotation annotation : annotations) {
            // Validate.
            assert (annotation != null) : "Ksyxis: Annotation is null. (annotations: " + annotations + ')';

            // Check, strip if needed.
            if (shouldStripTyped(annotation.annotation().className().stringValue())) continue; // Implicit NPE for 'annotation'

            // Add if not stripped.
            newAnnotations.add(annotation);
        }

        // Return the new list.
        return newAnnotations;
    }

    /// Checks if the internal annotation VM name should be stripped.
    ///
    /// Annotations will be stripped if their name is found in [#STRIPPED_ANNOTATIONS],
    /// or their name starts with a prefix (package) found in [#STRIPPED_PACKAGES].
    ///
    /// Unlike [#shouldStripTypeless(String)], this method **IS** intended
    /// for whole types, containing `L` at the beginning and `;` at the end.
    ///
    /// @param name Annotation internal VM name to check
    /// @return `true` if the annotation should be stripped, `false` otherwise
    /// @see #STRIPPED_ANNOTATIONS
    /// @see #STRIPPED_PACKAGES
    /// @see #shouldStripTypeless(String)
    @Contract(pure = true)
    private static boolean shouldStripTyped(final String name) {
        // Validate.
        assert (name != null) : "Ksyxis: Parameter 'name' is null.";
        assert (!name.isBlank()) : "Ksyxis: Blank name. (name: " + name + ')';
        assert ((name.charAt(0) == 'L') && (name.charAt(name.length() - 1) == ';')) : "Ksyxis: Typeless name used in typed stripping. (name: " + name + ')';

        // Delegate.
        final String typeless = name.substring(1, (name.length() - 1)); // Implicit NPE for 'name'
        return shouldStripTypeless(typeless);
    }

    /// Checks if the internal annotation VM name should be stripped.
    ///
    /// Annotations will be stripped if their name is found in [#STRIPPED_ANNOTATIONS],
    /// or their name starts with a prefix (package) found in [#STRIPPED_PACKAGES].
    ///
    /// Unlike [#shouldStripTyped(String)], this method is **NOT** intended for whole
    /// types, so it should **NOT** contain `L` at the beginning and `;` at the end.
    ///
    /// @param name Annotation internal VM name to check
    /// @return `true` if the annotation should be stripped, `false` otherwise
    /// @see #STRIPPED_ANNOTATIONS
    /// @see #STRIPPED_PACKAGES
    /// @see #shouldStripTyped(String)
    @Contract(pure = true)
    private static boolean shouldStripTypeless(final String name) {
        // Validate.
        assert (name != null) : "Ksyxis: Parameter 'name' is null.";
        assert (!name.isBlank()) : "Ksyxis: Blank name. (name: " + name + ')';
        assert ((name.charAt(0) != 'L') || (name.charAt(name.length() - 1) != ';')) : "Ksyxis: Typed name used in typeless stripping. (name: " + name + ')';

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
