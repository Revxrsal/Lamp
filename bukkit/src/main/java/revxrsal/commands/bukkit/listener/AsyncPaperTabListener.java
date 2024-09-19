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
package revxrsal.commands.bukkit.listener;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.stream.StringStream;

import java.util.List;

/**
 * Provides asynchronous tab completions
 *
 * @param <A> Actor type
 */
public final class AsyncPaperTabListener<A extends BukkitCommandActor> implements Listener {

    private final Lamp<A> lamp;
    private final ActorFactory<A> actorFactory;

    public AsyncPaperTabListener(Lamp<A> lamp, ActorFactory<A> actorFactory) {
        this.lamp = lamp;
        this.actorFactory = actorFactory;
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncTabComplete(AsyncTabCompleteEvent event) {
        String buf = event.getBuffer();
        if ((!event.isCommand() && !buf.startsWith("/")) || buf.indexOf(' ') == -1) {
            return;
        }
        StringStream stream = StringStream.create(
                buf.startsWith("/") ? buf.substring(1) : buf
        );
        A actor = actorFactory.create(event.getSender(), lamp);
        try {
            List<String> complete = lamp.autoCompleter().complete(actor, stream);
            if (complete.isEmpty()) {
                return;
            }

            // https://bugs.mojang.com/browse/MC-165562
            if (complete.size() == 1 && complete.get(0).isEmpty()) {
                complete.set(0, " ");
            }

            for (String s : complete) {
                event.getCompletions().add(s);
            }
            event.setHandled(true);
        } catch (Throwable ignored) {
        }
    }
}
