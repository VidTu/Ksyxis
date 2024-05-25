/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.relauncher;

import net.minecraftforge.fml.common.Mod;

import java.util.Map;

/**
 * Stub interface of the legacy (1.12) Forge coremod for Ksyxis hacky compat.
 * <p>
 * This is an injection interface for Ksyxis for Forge 1.8 -> 1.12.2, see {@code KsyxisLegacyForge} class.
 *
 * @author VidTu
 * @author MinecraftForge
 */
public interface IFMLLoadingPlugin {
    /**
     * Gets the access transformer class.
     * <p>
     * This is a NO-OP method for Ksyxis.
     *
     * @return Access transformer class, {@code null} by Ksyxis
     */
    String getAccessTransformerClass();

    /**
     * Gets the ASM transformer classes.
     * <p>
     * This is a NO-OP method for Ksyxis.
     *
     * @return Access transformer classes array, an empty ({@code new String[0]}) array by Ksyxis
     */
    String[] getASMTransformerClass();

    /**
     * Gets the virtual mod container class.
     * <p>
     * This is a NO-OP method for Ksyxis.
     *
     * @return Virtual mod container class, {@code null} by Ksyxis
     */
    String getModContainerClass();

    /**
     * Gets the mod setting up class.
     * <p>
     * This is a NO-OP method for Ksyxis.
     *
     * @return Mod setting up class, {@code null} by Ksyxis
     */
    String getSetupClass();

    /**
     * Injects the data.
     * <p>
     * This is an injection point for Ksyxis for Forge 1.8 -> 1.12.2, see {@code KsyxisLegacyForge} class.
     * <p>
     * For the declaration point, see {@link Mod}.
     *
     * @param data Dummy (stub) injection data, ignored by Ksyxis
     */
    void injectData(Map<String, Object> data);
}
