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

package eu.fusster;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;

import net.comboro.Client;
import net.comboro.ClientListener;
import net.comboro.Server;
import net.comboro.ServerListener;
import eu.fusster.command.CommandMap;
import eu.fusster.command.defaults.ThisCommand;
import eu.fusster.player.Player;
import eu.fusster.player.PlayerManager;
import eu.fusster.plugin.FussterPlugin;
import eu.fusster.plugin.PluginLoader;
import eu.fusster.plugin.PluginMap;
import eu.fusster.ui.BetterUI;

public final class Fusster {

	private static BetterUI betterUI;
	private static ServerInfo serverInfo;
	private static PluginLoader pluginLoader;
	private static PluginMap pluginMap;
	private static boolean offline = false;
	private static String publicIP = "127.0.0.1";
	private static String localIP;
	private static int port;
	
	public static final Color error = new Color(178, 34, 34);

	public static final String BAN_MESSEGE = "The Ban Hammer has spoken.";

	private static Properties properties = new Properties();

	public static void main(String[] args) {
		// Load GUI
		betterUI = new BetterUI();
		// Load the server configuration
		serverInfo = new ServerInfo();
		// Get config port
		port = serverInfo.getPort();
		// Basic auto response
		initProperties();
		// Register default commands
		CommandMap.addDefaults();
		// Get the plublic ip
		findIP();
		// Start the server from the config file port
		Server.addListener(listener());
		Server.start(getPort());
		// Loader the basic Plugin Map
		pluginMap = new PluginMap();
		// Loader the loader loading plugins from 'plugins'
		pluginLoader = new PluginLoader(pluginMap);
		// Load all plugins
		pluginLoader.loadAll();
	}
	
	private static void findIP(){
		try {
			publicIP = new BufferedReader(new InputStreamReader(new URL("http://bot.whatismyipaddress.com/").openStream()))
					.readLine();
			localIP = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			Fusster.setOffline(true);
		}
	}
	
	private static void initProperties(){
		properties.setProperty("name", serverInfo.getName());
		properties.setProperty("version", ServerInfo.VERSION);
		properties.setProperty("players", "0");
		properties.setProperty("maxPlayers",
				String.valueOf(serverInfo.getMaxPlayers()));
	}
	
	/**
	 * Gets the public address of the server
	 * @return The public address of the server
	 */
	public static String getIP(){
		return publicIP;
	}

	/**
	 * Set a property in the server properties
	 * 
	 * @param key
	 *            an indetifier
	 * @param value
	 *            value licked to the indetifier
	 */
	public static void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	/**
	 * Check if a key is contained in the properties
	 * 
	 * @param key
	 *            an indetifier
	 * @return if the properties contain the key
	 */
	public static boolean containsKey(String key) {
		return properties.containsKey(key);
	}

	/**
	 * Returns a server property
	 * 
	 * @param key
	 *            an indetifier
	 * @return A value matching the key given
	 * @see #setProperty(String, String)
	 */
	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

	/**
	 * Gets the {@link Properties} instance.
	 * 
	 * @return the {@link Properties} instance
	 */
	public static Properties getProperties() {
		return properties;
	}

	/**
	 * @return an instance of the {@link ServerUI}
	 */
	public static BetterUI getServerUI() {
		return betterUI;
	}

	/**
	 * @return the server debugging state
	 */
	public static boolean isDebugging() {
		return serverInfo.isDebugging();
	}

	/**
	 * Change the debugging state of the server
	 * 
	 * @param debugging
	 *            the new debugging state
	 */
	public static void setDebugging(boolean debugging) {
		serverInfo.setDebugging(debugging);
	}

	/**
	 * @return The {@link Vector} of type {@link String} containing all the
	 *         banned Internet Protocols
	 */
	public static Vector<String> getBanList() {
		return serverInfo.getBanList();
	}

	/**
	 * Prevent an {@link InetAddress} from connecting to the server
	 * 
	 * @param string
	 *            the String representing an {@link InetAddress}
	 */
	public static void ban(String string) {
		serverInfo.ban(string);
	}

	/**
	 * Unban an {@link InetAddress}
	 * 
	 * @param string
	 *            the {@link InetAddress} as String
	 * @return if the unban was successful
	 */
	public static boolean unban(String string) {
		return serverInfo.unban(string);
	}

	/**
	 * @return The maximum amount of players
	 */
	public static int getMaxPlayers() {
		return serverInfo.getMaxPlayers();
	}

	/**
	 * @return The port that the server is currently running on
	 */
	public static int getPort() {
		return port;
	}

	/**
	 * @return The name of the server
	 */
	public static String getName() {
		return serverInfo.getName();
	}

	/**
	 * @return if the server is offline
	 */
	public static boolean isOffline() {
		return offline;
	}

	/**
	 * Set the server as online or offline.
	 * 
	 * @param offline
	 *            set the server's offline state
	 */
	public static void setOffline(boolean offline) {
		Fusster.offline = offline;
	}

	/**
	 * @return The default {@link PluginMap} initialised in the main method
	 */
	public static PluginMap getPluginMap() {
		return pluginMap;
	}

	/**
	 * @return The default {@link PluginLoader} initialised in the main method
	 */
	public static PluginLoader getPluginLoader() {
		return pluginLoader;
	}

	/**
	 * Shuts down the server and disconnects all players sending them the server
	 * close message.
	 * 
	 * @param halt
	 *            if the FussterServer proccess should be terminated
	 */
	public static void shutdown(boolean halt) {
		serverInfo.logger.close();
		properties.clear();
		if (pluginLoader != null)
			getPluginLoader().unloadAll();
		PlayerManager.disconnectAll("Server shutting down");
		Server.stop();
		PlayerManager.getPlayers().clear();
		if (halt)
			Runtime.getRuntime().halt(0);
	}
	
	public static void append(String append){
		betterUI.append(append);
	}
	
	public static void append(String append, Color color){
		betterUI.append(append, color);
	}
	
	public static void debug(String append){
		if(betterUI.isDebugging()){
			append(append);
		}
	}
	
	public static void debug(String append, Color color){
		if(betterUI.isDebugging()){
			append(append, color);
		}
	}
	
	public static void debug(FussterPlugin fp, String string) {
		append("["+fp.getDescription().getName()+"] " + string);
	}
	
	public static void error(String error){
		debug(error, Fusster.error);
	}

	public static void log(String string) {
		if(serverInfo.logger !=  null)
			serverInfo.logger.print(string);
	}
	
	public static void updatePlayersPane(){
		betterUI.updatePlayersPane();
	}
	
	private static ServerListener listener(){
		return new ServerListener() {
			
			@Override
			public void onClientConnect(Client client) {
				betterUI.updateClientsPane();
				client.addListener(new ClientListener() {
					@Override
					public void onInput(String input) {
						if (Fusster.containsKey(input)) {
							client.send(Fusster.getProperty(input));
						} else if (input.startsWith("reg:")) {
							client.removeListener(this);
							new Player(client, input.substring(4));
						} else{
							debug(client + input);
						}
					}

					@Override
					public void onClose(String reason) {
						debug(client + " disconnected.");
					}

					@Override
					public void afterClose() {
						betterUI.updateClientsPane();
					}
				});
			}

			@Override
			public void onServerStart(int port) {
				Color c = new Color(255,20,147);
				append("Server successfully started.", c);
				append("Server version: " + ServerInfo.VERSION,c);
				append("Server name: "  + Fusster.getName() , c);
				append("Public IP : " + ((Fusster.getIP().equals("127.0.0.1")) ? "Offline" : publicIP) ,c);
				append("Local IP : " + localIP,c);
				append("Port : " + Fusster.getPort(),c);
				append("Max Players: " + Fusster.getMaxPlayers(),c);
				append("Used Memmory : " + ThisCommand.usedMem() + " MB.",c);
			}

			@Override
			public void onServerStartFail(IOException e) {
				append("Failed to start server. Enter a new port:");
				String cmd = CommandMap.nextCommand();
				try{
					int port = Integer.parseInt(cmd);
					Server.start(port);
				} catch(IllegalArgumentException iae){
					System.out.println(cmd + " is not a valid port.");
					onServerStartFail(e);
				}
				
			}

			@Override
			public void onError(IOException e) {
				if(e instanceof SocketException) return;
				e.printStackTrace();
			}
		};
	}

}
