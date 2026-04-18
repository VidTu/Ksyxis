package org.embeddedt.modernfix.core;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

/**
 * Stub entrypoint interface for ModernFix. This is a
 * compatibility hack for Ksyxis, see {@code Ksyxis} class.
 *
 * @author VidTu
 * @author embeddedt
 */
public final class ModernFixMixinPlugin {
    /**
     * Singleton instance.
     */
    public static ModernFixMixinPlugin instance;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Contract(value = "-> fail", pure = true)
    private ModernFixMixinPlugin() {
        throw new AssertionError("Ksyxis: Stub.");
    }

    /**
     * Checks whether the ModernFix option is enabled.
     *
     * @param option Option to check
     * @return Whether the ModernFix option is enabled
     */
    public boolean isOptionEnabled(final String option) {
        throw new AssertionError("Ksyxis: Stub.");
    }
}
