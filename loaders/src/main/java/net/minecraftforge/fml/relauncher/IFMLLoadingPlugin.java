/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.relauncher;

import java.util.Map;

/**
 * Emulated interface of the legacy (1.12) Forge coremod.
 *
 * @author VidTu
 * @author MinecraftForge
 */
public interface IFMLLoadingPlugin {
    String getAccessTransformerClass();

    String[] getASMTransformerClass();

    String getModContainerClass();

    String getSetupClass();

    void injectData(Map<String, Object> data);
}
