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

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

final class PaperCommodore extends Commodore implements Listener {

  static {
    try {
      Class.forName("com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent");
    } catch (ClassNotFoundException e) {
      throw new UnsupportedOperationException("Not running on modern Paper!", e);
    }
  }

  private final List<CommodoreCommand> commands = new ArrayList<>();

  PaperCommodore(Plugin plugin) {
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @Override
  public void register(LiteralCommandNode<?> node) {
    Objects.requireNonNull(node, "node");
    commands.add(new CommodoreCommand(node, null));
  }

  @Override
  public void register(Command command, LiteralCommandNode<?> node,
      Predicate<? super Player> permissionTest) {
    Objects.requireNonNull(command, "command");
    Objects.requireNonNull(node, "node");
    Objects.requireNonNull(permissionTest, "permissionTest");

    Collection<String> aliases = getAliases(command);
    if (!aliases.contains(node.getLiteral())) {
      node = renameLiteralNode(node, command.getName());
    }

    for (String alias : aliases) {
      if (node.getLiteral().equals(alias)) {
        commands.add(new CommodoreCommand(node, permissionTest));
      } else {
        LiteralCommandNode<Object> redirectNode = LiteralArgumentBuilder.literal(alias)
            .redirect((LiteralCommandNode<Object>) node)
            .build();
        commands.add(new CommodoreCommand(redirectNode, permissionTest));
      }
    }
  }

  @EventHandler
  @SuppressWarnings("deprecation") // draft API, ok...
  public void onPlayerSendCommandsEvent(AsyncPlayerSendCommandsEvent<?> event) {
    if (event.isAsynchronous() || !event.hasFiredAsync()) {
      for (CommodoreCommand command : commands) {
        command.apply(event.getPlayer(), event.getCommandNode());
      }
    }
  }

  private record CommodoreCommand(LiteralCommandNode<?> node,
                                  Predicate<? super Player> permissionTest) {

    @SuppressWarnings("rawtypes")
    public void apply(Player player, RootCommandNode<?> root) {
      if (permissionTest != null && !permissionTest.test(player)) {
        return;
      }
      removeChild(root, node.getName());
      root.addChild((CommandNode) node);
    }
  }

}