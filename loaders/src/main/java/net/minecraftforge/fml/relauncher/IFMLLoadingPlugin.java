/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.relauncher;

import net.minecraftforge.fml.common.Mod;

import java.util.Map;

/**
 * Stub interface of the legacy (1.12) Forge coremod for Ksyxis hacky compat. This is an injection interface for
 * Ksyxis for Forge 1.8 -> 1.12.2, see {@code KsyxisLegacyForge} class.
 *
 * @author VidTu
 * @author MinecraftForge
 */
public interface IFMLLoadingPlugin {
    /**
     * Gets the access transformer class. This is a NO-OP method for Ksyxis, it always returns {@code null}.
     *
     * @return Access transformer class, {@code null} by Ksyxis
     */
    String getAccessTransformerClass();

    /**
     * Gets the ASM transformer classes. This is a NO-OP method for Ksyxis, it always returns {@code null}.
     *
     * @return Access transformer classes array, {@code null} by Ksyxis
     */
    String[] getASMTransformerClass();

    /**
     * Gets the virtual mod container class. This is a NO-OP method for Ksyxis, it always returns {@code null}.
     *
     * @return Virtual mod container class, {@code null} by Ksyxis
     */
    String getModContainerClass();

    /**
     * Gets the mod setting up class. This is a NO-OP method for Ksyxis, it always returns {@code null}.
     *
     * @return Mod setting up class, {@code null} by Ksyxis
     */
    String getSetupClass();

    /**
     * Injects the data. This is an injection point for Ksyxis for Forge 1.8 -> 1.12.2, see {@code KsyxisLegacyForge}
     * class. For the declaration point, see {@link Mod}.
     *
     * @param data Stub injection data, ignored by Ksyxis
     */
    void injectData(Map<String, Object> data);
}
