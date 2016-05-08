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

package eu.fusster.player.event;

import eu.fusster.player.Player;

public class PlayerDisconnectEvent {

	private Player player;
	private String toSend, toAppend, reason;

	/**
	 * Event containing all the essential information for plugins to work with
	 * 
	 * @param player
	 *            The player the event is constructed for
	 * @param reason
	 *            The reason that the player got disconnect
	 */
	public PlayerDisconnectEvent(Player player, String reason) {
		this.player = player;
		this.reason = reason;

		String name = player.getName();
		// To send to other clients
		this.toSend = "disc:" + name + ":" + reason;
		// To append to console
		if (reason != null)
			this.toAppend = name + " disconnected from the game. Reason: < "
					+ reason + " >.";
		else
			this.toAppend = name + " disconnected from the game.";

	}

	/**
	 * @return The player that the event is made for
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * @return the text to be appended in the console
	 */
	public String getToAppend() {
		return toAppend;
	}

	/**
	 * @param toAppend
	 *            the new String that will be appended in the console
	 */
	public void setToAppend(String toAppend) {
		this.toAppend = toAppend;
	}

	/**
	 * @return the String being send to all other users
	 */
	public String getToSend() {
		return toSend;
	}

	/**
	 * @param toSend
	 *            set the String that will be send to all other users
	 */
	public void setToSend(String toSend) {
		this.toSend = toSend;
	}

	/**
	 * @return the reason for the player to get disconnected
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @param reason
	 *            sets the reason for the player to get disconnected
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

}
