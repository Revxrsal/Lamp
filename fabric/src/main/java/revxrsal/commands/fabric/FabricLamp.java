/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
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
package revxrsal.commands.fabric;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.fabric.actor.FabricCommandActor;

/**
 * Includes modular building blocks for hooking into the Fabric
 * platform.
 * <p>
 * Accept individual functions using {@link Lamp.Builder#accept(LampBuilderVisitor)}
 */
public final class FabricLamp {

    /**
     * Returns a {@link Lamp.Builder} that contains the default registrations
     * for the Fabric platform
     *
     * @param config The config
     * @param <A>    The actor type
     * @return A {@link Lamp.Builder}
     */
    public static <A extends FabricCommandActor> Lamp.Builder<A> builder(
            @NotNull FabricLampConfig<A> config
    ) {
        return Lamp.<A>builder()
                .accept(config);
    }

    /**
     * Returns a {@link Lamp.Builder} that contains the default registrations
     * for the Fabric platform
     *
     * @return A {@link Lamp.Builder}
     */
    public static Lamp.Builder<FabricCommandActor> builder() {
        return builder(FabricLampConfig.createDefault());
    }
}
