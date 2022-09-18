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

package dev.demeng.pluginbase.commands.bukkit.brigadier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;

final class ReflectionUtil {

  private static final String SERVER_VERSION = getServerVersion();
  private static final int MINECRAFT_VERSION = getMinecraftVersion();

  private static String getServerVersion() {
    Class<?> server = Bukkit.getServer().getClass();
    if (!server.getSimpleName().equals("CraftServer")) {
      return ".";
    }
    if (server.getName().equals("org.bukkit.craftbukkit.CraftServer")) {
      // Non versioned class
      return ".";
    } else {
      String version = server.getName().substring("org.bukkit.craftbukkit".length());
      return version.substring(0, version.length() - "CraftServer".length());
    }
  }

  public static String mc(String name) {
    return "net.minecraft." + name;
  }

  public static String nms(String className) {
    return "net.minecraft.server" + SERVER_VERSION + className;
  }

  public static Class<?> mcClass(String className) throws ClassNotFoundException {
    return Class.forName(mc(className));
  }

  public static Class<?> nmsClass(String className) throws ClassNotFoundException {
    return Class.forName(nms(className));
  }

  public static String obc(String className) {
    return "org.bukkit.craftbukkit" + SERVER_VERSION + className;
  }

  public static Class<?> obcClass(String className) throws ClassNotFoundException {
    return Class.forName(obc(className));
  }

  private static int getMinecraftVersion() {
    try {
      final Matcher matcher = Pattern.compile("\\(MC: (\\d)\\.(\\d+)\\.?(\\d+?)?( .*)?\\)")
          .matcher(Bukkit.getVersion());
      if (matcher.find()) {
        return Integer.parseInt(matcher.toMatchResult().group(2), 10);
      } else {
        throw new IllegalArgumentException(
            String.format("No match found in '%s'", Bukkit.getVersion()));
      }
    } catch (final IllegalArgumentException ex) {
      throw new RuntimeException("Failed to determine Minecraft version", ex);
    }
  }

  public static int minecraftVersion() {
    return MINECRAFT_VERSION;
  }

  private ReflectionUtil() {
  }

}