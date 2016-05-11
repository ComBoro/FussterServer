/*
 * This file is part of Fusster.
 *	
 * Fusster Copyright (C) ComBoro
 *
 * Fusster is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Fusster is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Fusster.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.fusster.player;

import static eu.fusster.Fusster.append;
import static eu.fusster.Fusster.error;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

import eu.fusster.Fusster;
import eu.fusster.player.event.PlayerDisconnectEvent;
import eu.fusster.player.event.PlayerJoinEvent;

/**
 * Class for managing players.
 *
 */
public class PlayerManager {

	private static Vector<Player> players = new Vector<Player>();
	
	public static final Comparator<Player> BY_NAME = new Comparator<Player>(){
		public int compare(Player arg0, Player arg1) {
			return arg0.compareTo(arg1);
		}
	};
	
	public static final Comparator<Player> BY_DATE_CONNECTED = new Comparator<Player>(){
		public int compare(Player arg0, Player arg1) {
			return arg0.client.dateCreated().compareTo(arg1.client.dateCreated());
		}
	};
	
	public static void sort(Comparator<Player> comparator){
		Collections.sort(players, comparator);
	}

	/**
	 * Registers a player to the Collection of players. Notifies all the plugins
	 * generating a {@link PlayerJoinEvent}. Adds the player to the list on the
	 * left of the GUI.
	 * 
	 * @param player
	 *            The player to register
	 */
	public static void addPlayer(Player player) {
		if (!players.contains(player)) {
			// Create event and pass to all plugins
			PlayerJoinEvent event = new PlayerJoinEvent(player);
			Fusster.getPluginMap().onPlayerJoinEvent(event);

			// Append in console
			append(event.getToAppend());

			// Notify other players if needed
			String toSend = event.getToSend();
			if (toSend != null && !toSend.equals(""))
				sendAll(toSend);

			player.send("connected");

			// Add to List and update UA
			players.addElement(player);
			Fusster.updatePlayersPane();
		} else {
			error("Player " + player.getName() + " was not added.");
		}
	}

	/**
	 * Removes a player and closes its socket
	 * 
	 * @see #removePlayer(Player, String, boolean)
	 */
	public static void removePlayer(Player player, String reason) {
		removePlayer(player, reason, true);
	}

	/**
	 * Removes a player from the Collection of players. Notifies all the plugins
	 * generating a {@link PlayerDisconnectEvent}. Removes the player from the
	 * list on the left of the GUI.
	 * 
	 * @param player
	 *            The player getting removed
	 * @param reason
	 *            The reason why the players is removed
	 * @param closeConnection
	 *            if it needs to close the connection
	 */
	public static void removePlayer(Player player, String reason,
			boolean closeConnection) {
		if (player == null)
			return;

		// Notify plugins

		PlayerDisconnectEvent event = new PlayerDisconnectEvent(player, reason);
		Fusster.getPluginMap().onPlayerDisconnectEvent(event);

		// Get name
		String name = player.getName();

		// Remove from list
		boolean status = players.removeElement(player);
		Fusster.updatePlayersPane();

		if (!status)
			error("Failed to remove player " + name + " from list.");

		// Close connection if needed
		if (closeConnection)
			player.closeConnection(event.getReason());

		// Append to Console
		Fusster.append(event.getToAppend(), java.awt.Color.RED);

		// Notify other people
		sendAll(event.getToSend());
	}

	/**
	 * Receives a name and returns a {@link Player} that was the same name.
	 * 
	 * @param name
	 *            The player name
	 * @return The player found by the name given
	 * @throws PlayerException
	 *             if no player was found
	 */
	public static Player get(String name) throws PlayerException {
		for (Player pl : getPlayers())
			if (pl.getName().equals(name))
				return pl;
		throw new PlayerException("Player not found");
	}

	/**
	 * Sends a message to all players for the Collection of players.
	 * 
	 * @param toSend
	 *            the message to send
	 */
	public static void sendAll(String toSend) {
		players.forEach(p -> p.send(toSend));
	}

	/**
	 * Sends a message to everyone except one player.
	 * 
	 * @param toSend
	 *            The message to send
	 * @param toIgnore
	 *            The player to ignore
	 */
	public static void sendOthers(String toSend, Player toIgnore) {
		for (Player pl : getPlayers()) {
			if (pl.equals(toIgnore))
				continue;
			pl.send(toSend);
		}
	}

	/**
	 * Returns all the players having the given InetAddress.
	 * 
	 * @param ip
	 *            The InetAddress to filer players
	 * @return All the players with the given InetAddress
	 */
	public static List<Player> getAll(String ip) {
		return Arrays.asList((Player[]) players.stream()
				.filter(player -> player.getIP().equals(ip)).toArray());
	}

	/**
	 * Checks if a name is available.
	 * 
	 * @param name
	 *            The name to check
	 * @return the result
	 */
	public static boolean checkAvailable(String name) {
		return players.stream().parallel().map(e -> e.getName())
				.filter(e -> e.equals(name)).count() == 0;
	}

	/**
	 * Checks if an InetAddress is banned.
	 * 
	 * @param ip
	 *            The InetAddress to check
	 * @return if the InetAddress is banned
	 */
	public static boolean isBanned(String ip) {
		return Fusster.getBanList().contains(ip);
	}

	/**
	 * @return The {@link Vector} of players
	 */
	public static Vector<Player> getPlayers() {
		return players;
	}

	/**
	 * Executes a {@link Consumer} for all the players.
	 * 
	 * @param action
	 *            The {@link Consumer} to execute
	 */
	public static void forEach(Consumer<? super Player> action) {
		players.stream().parallel().forEach(action);
	}

	/**
	 * Disconnects all the players with a given reason.
	 * 
	 * @param message
	 *            the reason for the disconnect
	 */
	public static void disconnectAll(String message) {
		Iterator<Player> it = players.iterator();
		while (it.hasNext()) {
			Player pl = it.next();
			it.remove();
			pl.closeConnection(message == null || message.equals("") ? "Server closing"
					: message);
		}
		Fusster.updatePlayersPane();
	}

}
