package revxrsal.commands.velocity;

import com.velocitypowered.api.proxy.Player;

/**
 * A parameter that allows player selectors such as '@a', '@p', '@s', '@r'
 * or player names individually.
 * <p>
 * Simply iterate over the parameter value.
 */
public interface PlayerSelector extends Iterable<Player> {

}
