package revxrsal.commands.bukkit.brigadier;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * A utility for providing information about the server implementation
 * and version
 */
final class BukkitVersion {

    private static final boolean IS_PAPER;

    private static final int MAJOR_VERSION, MINOR_VERSION, PATCH_NUMBER;

    /**
     * The current version string, for example 1_17_R1
     */
    private static final String VERSION = fetchVersion();

    /**
     * The version where NMS no longer uses versions in the package names
     */
    private static final int UNVERSION_NMS = 17;

    /**
     * The CraftBukkit package
     */
    private static final String CB_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

    static {
        Pattern dot = Pattern.compile(".", Pattern.LITERAL);
        String[] version;
        String bukkitVersion = Bukkit.getServer().getBukkitVersion();
        if (bukkitVersion.indexOf('-') == -1)
            version = dot.split(bukkitVersion);
        else
            version = dot.split(bukkitVersion.substring(0, bukkitVersion.indexOf('-')));

        if (version.length == 2)
            version = new String[]{version[0], version[1], "0"};
        MAJOR_VERSION = Integer.parseInt(version[0]);
        MINOR_VERSION = Integer.parseInt(version[1]);
        String minorSlice = version[2];
        if (minorSlice.indexOf('-') != -1)
            minorSlice = minorSlice.substring(0, minorSlice.indexOf('-'));
        PATCH_NUMBER = Integer.parseInt(minorSlice);

        IS_PAPER = isClassPresent("com.destroystokyo.paper.PaperConfig");
    }

    private BukkitVersion() {
    }

    /**
     * Returns the version of the server, e.g. v1_16_R2
     *
     * @return The server version
     */
    private static @NotNull String fetchVersion() {
        Server server = Bukkit.getServer();
        try {
            String packageName = server.getClass().getPackage().getName();
            return packageName.substring(packageName.lastIndexOf('.') + 1);
        } catch (Throwable throwable) {
            return "";
        }
    }

    /**
     * Tests whether is the given version supported by this server
     */
    public static boolean supports(int major, int minor) {
        return supports(major, minor, 0);
    }

    /**
     * Tests whether is the given version supported by this server
     */
    public static boolean supports(int major, int minor, int patch) {
        if (MAJOR_VERSION > major) {
            return true;
        } else if (MAJOR_VERSION == major) {
            if (MINOR_VERSION > minor) {
                return true;
            } else if (MINOR_VERSION == minor) {
                return PATCH_NUMBER >= patch;
            }
        }
        return false;
    }

    /**
     * Returns the major version for the current server. For example,
     * in 1.11.2 this would be 11.
     *
     * @return The current version.
     */
    public static int minorVersion() {
        return MINOR_VERSION;
    }

    /**
     * Returns the patch number for the current server. For example,
     * in 1.11.2 this would be 2.
     *
     * @return The current version.
     */
    public static int patchNumber() {
        return PATCH_NUMBER;
    }

    /**
     * Returns the current version string, for example v1_17_R1
     *
     * @return The current version string
     */
    public static @NotNull String version() {
        return VERSION;
    }

    /**
     * Returns the NMS class with the given name. The name must not contain
     * the net.minecraft.server prefix.
     */
    public static @NotNull Class<?> findNmsClass(@NotNull String... names) {
        for (String name : names) {
            Class<?> c = classOrNull("net.minecraft.server." + name);
            if (c != null)
                return c;
            c = classOrNull("net.minecraft.server." + VERSION + "." + name);
            if (c != null)
                return c;
            c = classOrNull("net.minecraft." + name);
            if (c != null)
                return c;
        }
        throw new IllegalStateException("Class not found. Names searched: " + Arrays.toString(names) + ".");
    }

    /**
     * Finds and returns the specified class from the CraftBukkit implementation.
     *
     * @param name The name of the class to find.
     * @return The found class.
     * @throws ClassNotFoundException If the class cannot be found.
     */
    @SneakyThrows
    public static @NotNull Class<?> findOcbClass(@NotNull String name) {
        return Class.forName(CB_PACKAGE + '.' + name);
    }

    /**
     * Tests whether this version is running PaperSpigot
     *
     * @return If this is PaperSpigot
     */
    public static boolean isPaper() {
        return IS_PAPER;
    }

    /**
     * Tests whether is brigadier supported or not
     *
     * @return if brigadier is supported or not
     */
    public static boolean isBrigadierSupported() {
        if (supports(1, 19, 1))
            return isPaper();
        else
            return supports(1, 13);
    }

    private static boolean isClassPresent(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static @Nullable Class<?> classOrNull(@NotNull String name) {
        try {
            return Class.forName(name);
        } catch (Throwable t) {
            return null;
        }
    }
}
