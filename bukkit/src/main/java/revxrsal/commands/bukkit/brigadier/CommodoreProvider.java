/*
 * This file is part of commodore, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package revxrsal.commands.bukkit.brigadier;

import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.util.Objects;
import java.util.function.Function;

/**
 * Factory for obtaining instances of {@link Commodore}.
 */
public final class CommodoreProvider {

    private CommodoreProvider() {
        throw new AssertionError();
    }

    private static final Function<BukkitCommandHandler, Commodore> PROVIDER = checkSupported();

    @SuppressWarnings("Convert2MethodRef")
    private static Function<BukkitCommandHandler, Commodore> checkSupported() {
        try {
            Class.forName("com.mojang.brigadier.CommandDispatcher");
        } catch (Throwable e) {
            printDebugInfo(e);
            return null;
        }

        // try the reflection impl
        try {
            ReflectionCommodore.ensureSetup();
            return plugin -> new ReflectionCommodore(plugin);
        } catch (Throwable e) {
            printDebugInfo(e);
        }

        // try the paper impl
        try {
            PaperCommodore.ensureSetup();
            return plugin -> new PaperCommodore(plugin);
        } catch (Throwable e) {
            printDebugInfo(e);
        }

        return null;
    }

    private static void printDebugInfo(Throwable e) {
        if (System.getProperty("commodore.debug") != null) {
            System.err.println("Exception while initialising commodore:");
            e.printStackTrace(System.err);
        }
    }

    /**
     * Checks to see if the Brigadier command system is supported by the server.
     *
     * @return true if commodore is supported.
     */
    public static boolean isSupported() {
        return PROVIDER != null;
    }

    /**
     * Obtains a {@link Commodore} instance for the given plugin.
     *
     * @param plugin the plugin
     * @return the commodore instance
     */
    public static Commodore getCommodore(BukkitCommandHandler plugin) {
        Objects.requireNonNull(plugin, "plugin");
        return PROVIDER == null ? null : PROVIDER.apply(plugin);
    }
}