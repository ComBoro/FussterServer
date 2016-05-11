package eu.fusster.plugin;

import eu.fusster.player.Player;
import eu.fusster.player.event.PlayerDisconnectEvent;
import eu.fusster.player.event.PlayerJoinEvent;

public abstract class ServerPlugin extends FussterPlugin {

	/**
	 * Called when a player disconnects from the server.
	 * 
	 * @param event
	 *            Details about the Player and other info.
	 * @see PlayerDisconnectEvent
	 */
	protected abstract void onPlayerDisconnectEvent(PlayerDisconnectEvent event)
			throws Exception;

	/**
	 * Called when a player registers on the server.
	 * 
	 * @param event
	 *            Details about the {@link Player} and other info.
	 * @see PlayerJoinEvent
	 */
	protected abstract void onPlayerJoinEvent(PlayerJoinEvent event)
			throws Exception;

}
