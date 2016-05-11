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

package eu.fusster.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import eu.fusster.Fusster;
import eu.fusster.command.Command;
import eu.fusster.command.CommandMap;
import eu.fusster.command.CommandSender;
import eu.fusster.player.Player;
import eu.fusster.player.event.PlayerDisconnectEvent;
import eu.fusster.player.event.PlayerJoinEvent;

public class PluginMap {

	private Map<FussterPlugin, Set<Object>> synmap;

	/**
	 * Generates an empty synchronised map.
	 */
	public PluginMap() {
		synmap = Collections
				.synchronizedMap(new HashMap<FussterPlugin, Set<Object>>());
	}

	/**
	 * Links an Object to a FussterPlugin
	 * 
	 * @param plugin
	 *            The plugin the object is going to be linked with
	 * @param link
	 *            The actual object being linked
	 */
	public void link(FussterPlugin plugin, Object link) {
		if (!synmap.containsKey(plugin))
			register(plugin);
		synmap.get(plugin).add(link);
	}

	/**
	 * Unlinks an {@link Object} from a {@link FussterPlugin}
	 * 
	 * @param plugin
	 *            The plugin the object is going to be unlicked from
	 * @param link
	 *            The actual object being unlinked
	 */
	public void unlink(FussterPlugin plugin, Object link) {
		if (synmap.containsKey(plugin))
			synmap.get(plugin).remove(link);
	}

	public void clear() {
		getPlugins().forEach(this::unregister);
		synmap.clear();
	}

	/**
	 * Registers a plugin into the map with an empty set of commands.
	 * 
	 * @param toRegister
	 *            The plugin to register
	 */
	public void register(FussterPlugin toRegister) {
		synchronized (synmap) {
			if (synmap.containsKey(toRegister))
				return;
			synmap.put(toRegister, new HashSet<Object>());
		}
	}

	/**
	 * Unregisters a plugin from the map.
	 * 
	 * @param plugin
	 *            The plugin to unregister
	 */
	public void unregister(FussterPlugin plugin) {
		unregister(plugin, false);
	}

	/**
	 * Unregisters a plugin from the map.
	 * 
	 * @param plugin
	 *            The plugin to unregister
	 * @param remove
	 *            if it should be removed from the map as well
	 */
	public void unregister(FussterPlugin plugin, boolean remove) {
		synchronized (synmap) {
			if (!synmap.containsKey(plugin))
				return;
			for (Object object : synmap.get(plugin)) {
				if (object instanceof JComponent) { // Remove plugin created
													// tabs
					Fusster.getServerUI().getConsoleTabbedPane()
							.remove((JComponent) object);
				} else if (object instanceof String) { // Remove potencial
														// properties
					String str = (String) object;
					if (Fusster.containsKey(str))
						Fusster.getProperties().remove(str);
				}
			}

			// Remove from map
			if (remove)
				synmap.remove(plugin);
		}
	}

	/**
	 * @return The set of plugins
	 */
	public Set<FussterPlugin> getPlugins() {
		synchronized (synmap) {
			return synmap.keySet();
		}
	}

	/**
	 * 
	 * @return The whole map of plugins and commands
	 * @see #getPlugins() if you want to get only the set of plugins
	 */
	public Map<FussterPlugin, Set<Object>> getMap() {
		synchronized (synmap) {
			return synmap;
		}
	}

	/**
	 * Notifies all the server plugins that a player has joined
	 * 
	 * @param event
	 *            The {@link PlayerJoinEvent} that is made for the
	 *            {@link Player}
	 * @see PlayerJoinEvent
	 */
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		synchronized (synmap) {
			for (FussterPlugin fp : synmap.keySet())
				if (fp instanceof ServerPlugin)
					try {
						((ServerPlugin) fp).onPlayerJoinEvent(event);
					} catch (Exception exception) {
						Fusster.debug(fp,
								"Problem with Player Join Event. Message: "
										+ exception.getMessage());
					}
		}
	}

	/**
	 * Notifies all the plugins about a new command and returns a result
	 * 
	 * @param sender
	 *            The sender of the command
	 * @param label
	 *            The label of the command
	 * @param args
	 *            The arguments following the command
	 * @return if the command was used successfully
	 * @see FussterPlugin#onCommand(CommandSender, String, String[])
	 */
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		synchronized (synmap) {
			boolean success = false;
			for (FussterPlugin fp : synmap.keySet()) {
				try {
					boolean result = fp.onCommand(sender, label, args);
					if (result) {
						success = true;
						break;
					}
				} catch (Exception exception) {
					success = false;
					Fusster.debug(fp, " Error executing command with label: "
							+ label + ", args: " + args.toString()
							+ ". Message: " + exception.getMessage());
				}
			}
			return success;
		}
	}

	/**
	 * Notifies all the plugins that a player has disconnected
	 * 
	 * @param event
	 *            The {@link PlayerDisconnectEvent} that is made for the
	 *            {@link Player}
	 * @see PlayerDisconnectEvent
	 */
	public void onPlayerDisconnectEvent(PlayerDisconnectEvent event) {
		synchronized (synmap) {
			for (FussterPlugin fp : synmap.keySet())
				if (fp instanceof ServerPlugin)
					try {
						((ServerPlugin) fp).onPlayerDisconnectEvent(event);
					} catch (Exception exception) {
						Fusster.debug(fp,
								"Problem with Player Disconnect Event. Message: "
										+ exception.getMessage());
					}
		}
	}

	/**
	 * Tries to get a plugin from the list
	 * 
	 * @param name
	 *            The name of the plugin
	 * @return The plugin found by the name given
	 * @throws PluginException
	 *             if there is no plugin found
	 */
	public FussterPlugin getPlugin(String name) throws PluginException {
		synchronized (synmap) {
			for (FussterPlugin plugin : getPlugins())
				if (plugin.getDescription().getName().equals(name))
					return plugin;
			throw new PluginException("Plugin not found");
		}
	}

	/**
	 * Links a command to a plugin. When the plugin gets unloaded all the bound
	 * commands get unregistered. Also registers the plugin if the plugin isn't
	 * registered
	 * 
	 * @param plugin
	 *            The plugin that the command will be linked to
	 * @param command
	 *            The actual command to bound
	 * @param label
	 *            The label that the command should react to
	 * @see CommandMap#register(String, Command)
	 */
	public void registerCommand(FussterPlugin plugin, Command command,
			String label) {
		synchronized (synmap) {
			if (!synmap.keySet().contains(plugin))
				register(plugin);
			synmap.get(plugin).add(command);
			CommandMap.register(label, command);
		}
	}

	/**
	 * Unlinks a command from its linked plugin. The command gets unregistered
	 * afterwards.
	 * 
	 * @param plugin
	 *            The plugin that the command is linked to
	 * @param command
	 *            The actual command to unregister
	 * @see CommandMap#unregister(Command)
	 */
	public void unregisterCommand(FussterPlugin plugin, Command command) {
		synchronized (synmap) {
			if (!synmap.keySet().contains(plugin)
					&& !synmap.get(plugin).contains(command))
				return;
			synmap.get(plugin).remove(command);
			CommandMap.unregister(command);
		}
	}

	/**
	 * Clears all the commands from a plugin and unregisters them.
	 * 
	 * @param plugin
	 *            The plugin to clear
	 * @see CommandMap#unregister(Command)
	 */
	public void unrgisterAllCommands(FussterPlugin plugin) {
		synchronized (synmap) {
			for (Object object : synmap.get(plugin)) {
				if (object instanceof Command) {
					CommandMap.unregister((Command) object);
				}

			}
			synmap.get(plugin).clear();
			synmap.remove(plugin);
		}
	}

	/**
	 * @param plugin
	 *            The plugin to list all the commands from
	 * @return all the registered command by a plugin
	 */
	public Set<Command> getRegisteredCommands(FussterPlugin plugin) {
		synchronized (synmap) {
			Set<Command> cmds = new HashSet<>();
			for (Object object : synmap.get(plugin)) {
				if (object instanceof Command)
					cmds.add((Command) object);
			}
			return cmds;
		}
	}

}
