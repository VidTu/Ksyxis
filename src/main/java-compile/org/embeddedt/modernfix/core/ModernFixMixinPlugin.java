package org.embeddedt.modernfix.core;

/**
 * Stub entrypoint interface for ModernFix. This is a
 * compatibility hack for Ksyxis, see {@code Ksyxis} class.
 *
 * @author VidTu
 * @author embeddedt
 */
public abstract class ModernFixMixinPlugin {
    public static ModernFixMixinPlugin instance;

    public abstract boolean isOptionEnabled(String option);
}
