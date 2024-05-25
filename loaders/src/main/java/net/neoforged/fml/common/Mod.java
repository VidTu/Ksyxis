/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emulated annotation of the NeoForge mod.
 * <p>
 * This is an injection annotation for Ksyxis for NeoForge 1.20.2+, see {@code KsyxisNeoForge} class.
 *
 * @author VidTu
 * @author MinecraftForge
 * @author NeoForged
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
    /**
     * Gets the mod ID for NeoForge.
     *
     * @return Modern mod ID, "{@code ksyxis}" by Ksyxis
     */
    String value();
}