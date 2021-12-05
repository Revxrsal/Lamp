package revxrsal.commands.bukkit;

import org.bukkit.entity.Player;

/**
 * A parameter that allows player selectors such as '@a', '@p', '@s', '@r'
 * or player names individually.
 * <p>
 * Simply iterate over the parameter value.
 *
 * @deprecated Use {@link EntitySelector} with {@link Player} type.
 */
@Deprecated
public interface PlayerSelector extends Iterable<Player> {

}
