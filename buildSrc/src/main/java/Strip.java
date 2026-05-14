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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.lang.classfile.Annotation;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassFileBuilder;
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
import java.nio.file.Files;
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
/// @author VidTu
@NullMarked
public final class Strip {
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
    private static final SourceFileAttribute EMPTY_SOURCE = SourceFileAttribute.of("");

    /// A [ClassTransform] that strips all [strippable][#shouldStripTypeless(String)] annotations from all elements
    /// in the class, including the class itself, class [fields][ClassTransform#transformingFields(FieldTransform)],
    /// class [methods][ClassTransform#transformingMethods(MethodTransform)] (including their parameters).
    /// Method code is not being transformed. (for now)
    ///
    /// Additionally, this transform strips all [@deprecated][DeprecatedAttribute] attributes
    /// and replaces the [source file][SourceFileAttribute] attributes with an empty string.
    private static final ClassTransform STRIP_ATTRIBUTES_TRANSFORM;
    static {
        // Create the transforms.
        final ClassTransform stripClass = (final ClassBuilder builder, final ClassElement element) -> {
            switch (element) {
                // Remove the deprecated attribute, it serves no VM purpose.
                case final DeprecatedAttribute _ -> {}

                // Strip the annotations. This doesn't remove all annotations,
                // only those subjected by the "shouldStripTyped(String)" method.
                case final RuntimeInvisibleAnnotationsAttribute attribute -> stripInvisible(builder, attribute);
                case final RuntimeInvisibleTypeAnnotationsAttribute attribute -> stripInvisibleType(builder, attribute);
                case final RuntimeVisibleAnnotationsAttribute attribute -> stripVisible(builder, attribute);
                case final RuntimeVisibleTypeAnnotationsAttribute attribute -> stripVisibleType(builder, attribute);

                // Strip the information about inner classes, this often contains information
                // about annotations like @ApiStatus.Internal (their outer/inner data).
                // Note, as with the annotations, only selective classes are stripped.
                case final InnerClassesAttribute attribute -> stripInnerClasses(builder, attribute);

                // Strip the source file attribute.
                case final SourceFileAttribute _ -> builder.with(EMPTY_SOURCE);

                // Pass all other elements as-is.
                default -> builder.with(element);
            }
        };
        final ClassTransform stripFields = ClassTransform.transformingFields((final FieldBuilder builder, final FieldElement element) -> {
            switch (element) {
                // Remove the deprecated attribute, it serves no VM purpose.
                case final DeprecatedAttribute _ -> {}

                // Strip the annotations. This doesn't remove all annotations,
                // only those subjected by the "shouldStripTyped(String)" method.
                case final RuntimeInvisibleAnnotationsAttribute attribute -> stripInvisible(builder, attribute);
                case final RuntimeInvisibleTypeAnnotationsAttribute attribute -> stripInvisibleType(builder, attribute);
                case final RuntimeVisibleAnnotationsAttribute attribute -> stripVisible(builder, attribute);
                case final RuntimeVisibleTypeAnnotationsAttribute attribute -> stripVisibleType(builder, attribute);

                // Pass all other elements as-is.
                default -> builder.with(element);
            }
        });
        final ClassTransform stripMethods = ClassTransform.transformingMethods((final MethodBuilder builder, final MethodElement element) -> {
            switch (element) {
                // Remove the deprecated attribute, it serves no VM purpose.
                case final DeprecatedAttribute _ -> {}

                // Strip the annotations. This doesn't remove all annotations,
                // only those subjected by the "shouldStripTyped(String)" method.
                case final RuntimeInvisibleAnnotationsAttribute attribute -> stripInvisible(builder, attribute);
                case final RuntimeInvisibleParameterAnnotationsAttribute attribute -> stripInvisibleParameter(builder, attribute);
                case final RuntimeInvisibleTypeAnnotationsAttribute attribute -> stripInvisibleType(builder, attribute);
                case final RuntimeVisibleAnnotationsAttribute attribute -> stripVisible(builder, attribute);
                case final RuntimeVisibleParameterAnnotationsAttribute attribute -> stripVisibleParameter(builder, attribute);
                case final RuntimeVisibleTypeAnnotationsAttribute attribute -> stripVisibleType(builder, attribute);

                // Pass all other elements as-is.
                default -> builder.with(element);
            }
        });

        // Merge the transforms.
        STRIP_ATTRIBUTES_TRANSFORM = stripClass.andThen(stripFields).andThen(stripMethods);
    }

    /// An instance of this class cannot be created.
    ///
    /// @throws AssertionError Always
    /// @deprecated Always throws
    @Deprecated(forRemoval = true)
    @Contract(value = "-> fail", pure = true)
    private Strip() {
        throw new AssertionError("Ksyxis: No instances.");
    }

    /// Reads the class-file bytecode from the file, strips the decoration attributes via
    /// [#STRIP_ATTRIBUTES_TRANSFORM], and writes the bytecode into the same file.
    ///
    /// @param classFile Path of the file to strip
    /// @throws IllegalArgumentException If a JVM bytecode parsing/writing/tranformation fails
    /// @throws IOException              On I/O error
    public static void stripBytecode(final Path classFile) throws IOException {
        final ClassFile of = ClassFile.of(ClassFile.ConstantPoolSharingOption.NEW_POOL);
        final ClassModel input = of.parse(classFile); // Implicit NPE for 'classFile'
        final byte[] output = of.transformClass(input, STRIP_ATTRIBUTES_TRANSFORM);
        Files.write(classFile, output);
    }

    /// Strips the [inner class metadata][InnerClassInfo] with rules from [#shouldStripTypeless(String)].
    /// Non-stripped class attributers will be passed to the `builder` for processing.
    ///
    /// @param builder Consumer for non-stripped annotations, usually the [ClassFileBuilder]
    /// @param element Element to strip the annotations from
    /// @see #shouldStripTypeless(String)
    private static void stripInnerClasses(final Consumer<? super InnerClassesAttribute> builder,
                                          final InnerClassesAttribute element) {
        // Extract.
        final List<InnerClassInfo> classes = element.classes(); // Implicit NPE for 'element'

        // Create a new list.
        final List<InnerClassInfo> newClasses = new ArrayList<>(classes.size());

        // Filter the old list into the new list.
        for (final InnerClassInfo clazz : classes) {
            if (shouldStripTypeless(clazz.innerClass().asInternalName())) continue;
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
        // Strip.
        final List<Annotation> stripped = stripList(element.annotations()); // Implicit NPE for 'element'

        // Skip if no annotations left after stripping.
        if (stripped.isEmpty()) return;

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
        // Strip.
        final List<List<Annotation>> stripped = stripParameterList(element.parameterAnnotations()); // Implicit NPE for 'element'

        // Skip if no annotations left after stripping.
        if (stripped.isEmpty()) return;

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
        // Strip.
        final List<TypeAnnotation> stripped = stripTypeList(element.annotations()); // Implicit NPE for 'element'

        // Skip if no annotations left after stripping.
        if (stripped.isEmpty()) return;

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
        // Strip.
        final List<Annotation> stripped = stripList(element.annotations()); // Implicit NPE for 'element'

        // Skip if no annotations left after stripping.
        if (stripped.isEmpty()) return;

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
        // Strip.
        final List<List<Annotation>> stripped = stripParameterList(element.parameterAnnotations()); // Implicit NPE for 'element'

        // Skip if no annotations left after stripping.
        if (stripped.isEmpty()) return;

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
        // Strip.
        final List<TypeAnnotation> stripped = stripTypeList(element.annotations()); // Implicit NPE for 'element'

        // Skip if no annotations left after stripping.
        if (stripped.isEmpty()) return;

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
        // Create a list.
        final List<Annotation> newAnnotations = new ArrayList<>(annotations.size()); // Implicit NPE for 'annotations'

        // Filter the old list into the new list.
        for (final Annotation annotation : annotations) {
            if (shouldStripTyped(annotation.className().stringValue())) continue;
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
        // Create a list of lists.
        final List<List<Annotation>> newAnnotations = new ArrayList<>(annotations.size()); // Implicit NPE for 'annotations'

        // See below.
        /*non-final*/ boolean noAnnotations = true;

        // Filter the old list of lists into the new list of lists.
        for (final List<Annotation> list : annotations) {
            // Create a list.
            final List<Annotation> newList = new ArrayList<>(list.size()); // Implicit NPE for 'list'

            // Filter the old list into the new list.
            for (final Annotation annotation : list) {
                if (shouldStripTyped(annotation.className().stringValue())) continue;
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
        // Create a list.
        final List<TypeAnnotation> newAnnotations = new ArrayList<>(annotations.size()); // Implicit NPE for 'annotations'

        // Filter the old list into the new list.
        for (final TypeAnnotation annotation : annotations) {
            if (shouldStripTyped(annotation.annotation().className().stringValue())) continue;
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
        return shouldStripTypeless(name.substring(1, name.length() - 1)); // Implicit NPE for 'name'
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
