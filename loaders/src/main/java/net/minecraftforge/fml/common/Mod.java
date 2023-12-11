/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emulated annotation of the Forge mod.
 *
 * @author VidTu
 * @author MinecraftForge
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
    // Java annotations will simply ignore unneeded things.

    /**
     * Gets the mod ID for modern (1.13+) Forge.
     *
     * @return Modern mod ID
     */
    String value();

    /**
     * Gets the mod ID for legacy (1.12) Forge.
     *
     * @return Legacy mod ID
     */
    String modid();
}