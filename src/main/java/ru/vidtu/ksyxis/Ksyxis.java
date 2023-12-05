/*
 * MIT License
 *
 * Copyright (c) 2021-2023 VidTu
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
 */

package ru.vidtu.ksyxis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

/**
 * Main Ksyxis class.
 *
 * @author VidTu
 */
public final class Ksyxis {
    /**
     * Logger. Using Log4j logger, because SLF4J may not be available in older versions.
     */
    private static final Logger LOG = LogManager.getLogger("Ksyxis");

    /**
     * Mixin error message.
     */
    private static final String MIXIN_ERROR = "Ksyxis: No Mixin found. If you're using old (1.15.2 or older) Forge, please install a Mixin loader, for example MixinBootstrap, MixinBooter, UniMixins, or any other if you find. If you're using new (1.16 or newer) Forge, any Fabric, any Quilt, or any NeoForge, then something went wrong and you should report it on GitHub. If you don't want any hassles and just want to load the game without solving anything, delete the Ksyxis mod.";

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private Ksyxis() {
        throw new AssertionError("No instances.");
    }

    /**
     * Checks the mixin presence and logs the info on startup.
     */
    public static void init() {
        // Check for Mixin.
        try {
            Class.forName("org.spongepowered.asm.mixin.Mixin");
        } catch (Throwable t) {
            // Log.
            LOG.error(MIXIN_ERROR, t);

            // Try to display LWJGL3 message box from TinyFD.
            try {
                Class<?> tinyFd = Class.forName("org.lwjgl.util.tinyfd.TinyFileDialogs");
                Method tinyFdMessageBox = tinyFd.getMethod("tinyfd_messageBox", CharSequence.class, CharSequence.class, CharSequence.class, CharSequence.class, boolean.class);
                tinyFdMessageBox.invoke(null, "Minecraft | Ksyxis Mod", MIXIN_ERROR, "ok", "error", false);
            } catch (Throwable th) {
                t.addSuppressed(th);
            }

            // Try to display LWJGL2 alert from Sys.
            try {
                Class<?> tinyFd = Class.forName("org.lwjgl.Sys");
                Method tinyFdMessageBox = tinyFd.getMethod("alert", String.class, String.class);
                tinyFdMessageBox.invoke(null, "Minecraft | Ksyxis Mod", MIXIN_ERROR);
            } catch (Throwable th) {
                t.addSuppressed(th);
            }

            // Log again. (with suppressed errors)
            LOG.error(MIXIN_ERROR, t);

            // Throw.
            throw new RuntimeException(MIXIN_ERROR, t);
        }

        // Log the info.
        LOG.info("Ksyxis will speedup your world loading, but may break everything :P");
    }

    /**
     * Logs the info on world loading.
     */
    public static void world() {
        LOG.info("Hey. This is Ksyxis. We will now load the world and will try to do it quickly. If the game is not responding after this, it's probably us to blame. (Or delete for good)");
    }
}
