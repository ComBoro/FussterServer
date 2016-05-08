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

package eu.fusster.command;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import eu.fusster.Fusster;
import eu.fusster.command.defaults.BanCommand;
import eu.fusster.command.defaults.HelpCommand;
import eu.fusster.command.defaults.PluginsCommand;
import eu.fusster.command.defaults.ServerInfoCommand;
import eu.fusster.command.defaults.ThisCommand;
import eu.fusster.command.defaults.UnbanCommand;
import eu.fusster.player.Player;
import eu.fusster.ui.ServerUI;

public class CommandMap {
	
	private static boolean commandRequested = false;
	private static String command = "";
	private static Object lock = new Object();

	private static Map<String, Command> commands = new HashMap<String, Command>();

	public static void addDefaults() {
		register("help", new HelpCommand());
		register("plugins", new PluginsCommand());
		register("ban", new BanCommand());
		register("unban", new UnbanCommand());
		register("serverinfo", new ServerInfoCommand());
		register("this", new ThisCommand());
	}

	/**
	 * Registers a command.
	 * 
	 * @param label
	 *            The label that the command wants to be called by
	 * @param command
	 *            The {@link Command} class representative
	 * @return if the command was registered successfully
	 */
	public static boolean register(String label, Command command) {
		label = label.toLowerCase().trim();
		if (commands.containsKey(label) || commands.containsValue(command))
			return false;
		commands.put(label, command);
		return true;
	}

	/**
	 * Unregisters a command.
	 * 
	 * @param command
	 *            The command class that will be unregistered
	 */
	public static void unregister(Command command) {
		if (!commands.values().contains(command))
			return;
		commands.remove(commands.get(command));
	}

	/**
	 * Executes the command and notifies all the plugins
	 * 
	 * @param sender
	 *            The commandsender of the command. By default(
	 *            {@link ConsoleCommandSender} and {@link Player})
	 * @param commandLine
	 *            The raw command line enetered
	 */
	public static void dispatch(CommandSender sender, String commandLine) {
		if(commandRequested){
			commandRequested = false;
			command = commandLine;
			synchronized (lock) {
				lock.notify();
			}
			return;
		}
		
		ServerUI.append("Command: " + commandLine, Color.GRAY, true);
		ServerUI.append("Sender: " + sender.getName(), Color.GRAY, true);

		String[] args = commandLine.split(sender.getSeparator());

		Command command;

		if (args.length == 0)
			command = getCommand(commandLine);
		else
			command = getCommand(args[0]);

		if (command != null)
			command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
		else {
			boolean result = Fusster.getPluginMap().onCommand(sender, args[0],
					Arrays.copyOfRange(args, 1, args.length));
			if (!result)
				ServerUI.append(sender.getName() + " send invalid command.");
		}
	}

	/**
	 * Gets a command by its label.
	 * 
	 * @param name
	 *            The label of the command
	 * @return The command if found else returns null
	 */
	public static Command getCommand(String name) {
		Command target = commands.get(name.toLowerCase());
		return target;
	}
	
	public static String nextCommand(){
		commandRequested = true;
		synchronized (lock) {
			try {
				lock.wait(0);
				Fusster.getServerUI().clearCommandLine();
				return command;
			} catch (InterruptedException e) {
				return null;
			}
		}
	}

	public static Map<String, Command> getCommands() {
		return commands;
	}

}
