/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.common;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Stub annotation of the Forge mod for Ksyxis hacky compat.
 * <p>
 * This is an injection annotation for Ksyxis on Forge 1.13+ and NeoForge 1.20.1, see {@code KsyxisForge} class.
 * <p>
 * This is a declarative annotation (for the mod to be listed) for Ksyxis on Forge 1.8 -> 1.12.2,
 * injection performed in {@link IFMLLoadingPlugin}.
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
     * @return Modern mod ID, "{@code ksyxis}" by Ksyxis
     */
    String value();

    /**
     * Gets the mod ID for legacy (1.12) Forge.
     *
     * @return Legacy mod ID, "{@code ksyxis}" by Ksyxis
     */
    String modid();

    /**
     * Gets the legacy remote version acceptance range. (client &lt;-&gt; server check)
     * <p>
     * This is set to lift the requirements for installation onto client when running on server.
     *
     * @return Legacy remote version acceptance range, "{@code *}" by Ksyxis
     */
    String acceptableRemoteVersions();
}