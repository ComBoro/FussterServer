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

package eu.fusster.command.defaults;

import eu.fusster.Fusster;
import eu.fusster.command.CommandSender;
import eu.fusster.player.Player;
import eu.fusster.player.PlayerException;
import eu.fusster.player.PlayerManager;
import eu.fusster.ui.ServerUI;

public class BanCommand extends DefaultCommand {

	public BanCommand() {
		super("BanCommand",
				"Bans an internet protocol from establishing a connection",
				"/ban <name>");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sender.sendMessage("Invalid arguments");
			return false;
		}

		String arg1 = args[0];

		if (arg1.equalsIgnoreCase("list")) {
			ServerUI.append(Fusster.getBanList().toString());
			return true;
		}

		String toBanIP;

		if (arg1.contains(".")) {
			toBanIP = arg1;
		} else {
			try {

				Player target = PlayerManager.get(arg1);
				PlayerManager.removePlayer(target,
						"Banned by " + sender.getName());
				toBanIP = target.getIP();
			} catch (PlayerException e) {
				sender.sendMessage("Invalid player");
				return false;
			}
		}

		Fusster.ban(toBanIP);
		
		PlayerManager.getAll(toBanIP).forEach(e -> PlayerManager.removePlayer(e, "You have been banned."));
		
		sender.sendMessage("All clients with Internet Protocol " + toBanIP
				+ " were banned from the server.");

		return false;
	}

}
