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
package dev.demeng.pluginbase.commands.bukkit.adventure;

import dev.demeng.pluginbase.commands.bukkit.BukkitCommandActor;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.process.ResponseHandler;
import dev.demeng.pluginbase.lib.adventure.audience.Audience;
import dev.demeng.pluginbase.lib.adventure.text.ComponentLike;
import java.util.function.Function;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Adds support for returning {@link ComponentLike} from methods to respond directly to actors.
 */
public final class ComponentResponseHandler implements ResponseHandler<ComponentLike> {

  private final Function<CommandSender, Audience> audiences;

  public ComponentResponseHandler(Function<CommandSender, Audience> audiences) {
    this.audiences = audiences;
  }

  @Override
  public void handleResponse(ComponentLike response, @NotNull CommandActor actor,
      @NotNull ExecutableCommand command) {
    BukkitCommandActor bActor = (BukkitCommandActor) actor;
    if (response != null) {
      audiences.apply(bActor.getSender()).sendMessage(response);
    }
  }
}
