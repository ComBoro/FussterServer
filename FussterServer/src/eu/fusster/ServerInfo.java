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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class ServerInfo {

	public static final String VERSION = "Alpha 01F0A";

	private int max_players, port;
	private String name;
	private boolean debugging;

	public PrintWriter logger;

	private Vector<String> banList = new Vector<String>();

	private File banListFile;

	public ServerInfo() {
		loadLogger();
		loadServerConfig();
		loadBanList();
	}

	/**
	 * Adds an String representing an {@link InetAddress} to the list of
	 * prevented users
	 *
	 * @param string
	 *            the String version of the {@link InetAddress} that is going to
	 *            be banned
	 */
	public void ban(String string) {
		if (banList.contains(string))
			return;

		banList.addElement(string);
		updateBanListFile();
	}

	/**
	 * Removes a player from the ban list
	 *
	 * @param string
	 *            the String version of the {@link InetAddress} that will be
	 *            removed from the list of prevented users
	 * @return if the removal was successful
	 */
	public boolean unban(String string) {
		boolean status = banList.removeElement(string);
		updateBanListFile();
		return status;
	}

	/**
	 * Updates the file of prevented users by clearing it and rewriting it.
	 */
	public void updateBanListFile() {
		try {
			new FileWriter(banListFile).close();

			PrintWriter writer = getWriter(banListFile);

			Set<String> set = new HashSet<String>(banList);
			Iterator<String> it = set.iterator();
			while (it.hasNext())
				writer.println(it.next());
			set.clear();

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadLogger() {
		try {
			File logs = Loader.loadDirectory("logs");
			File currentLog = new File(logs, System.currentTimeMillis()
					+ ".log");
			this.logger = new PrintWriter(currentLog);
		} catch (Exception e) {
			Fusster.error("Failed to load the logger. Exception: " + e.getMessage());
		}
	}

	/**
	 * Reads the ban list and adds every single line to the list of prevented
	 * users.
	 */
	private void loadBanList() {
		// Get Server Configuration File
		banListFile = Loader.loadFile("banlist.dat");
		// Initialise reader and writer
		BufferedReader banListReader = getReader(banListFile);

		readBanList(banListReader);
	}

	/**
	 * Loads the server configuration file and validates it if needed. In some
	 * cases it may create a default one
	 */
	/**
	 *
	 */
	private void loadServerConfig() {
		// Get Server Configuration File
		File serverConfig = Loader.loadFile("server.info");
		// Init reader and writer
		BufferedReader reader = getReader(serverConfig);
		PrintWriter writer = getWriter(serverConfig);

		// Use Reader && Writer
		boolean result = readConfig(reader);
		if (!result) {
			Fusster.error("Invalid config");
			createDefault(writer);
			writer.close();
			reader = getReader(serverConfig);
			readConfig(reader);
			closeRedaer(reader);
			return;
		}

		// Close reader && writer
		closeRedaer(reader);
		writer.close();
	}

	/**
	 * Reads the ban list file and adds it to the list of prevented users
	 *
	 * @param banListReader
	 *            the initialised {@link BufferedReader} containing the ban list
	 *            file
	 */
	private void readBanList(BufferedReader banListReader) {
		try {
			if (!banListReader.ready())
				return;

			String line = "";
			// Use set to ignore duplicates
			Set<String> temp = new HashSet<String>();
			while ((line = banListReader.readLine()) != null) {
				temp.add(line);
			}
			banList.addAll(temp);
			temp.clear();
			banListReader.close();
		} catch (IOException e) {
			Fusster.error(e.getMessage());
		}
	}

	/**
	 * Reads the config, sets the data and returns if is valid.
	 *
	 * @param reader
	 *            the initialised {@link BufferedReader} with the open
	 *            configuration file
	 * @return if the config is valid
	 */
	private boolean readConfig(BufferedReader reader) {
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("//"))
					continue;
				if (line.contains(":")) {
					String[] split = line.split(":");
					String variable = split[0].trim();
					String value = split[1].trim();
					switch (variable) {
					case "Max players":
						max_players = Integer.valueOf(value);
						break;
					case "Name":
						name = String.valueOf(value);
						break;
					case "Port":
						port = Integer.valueOf(value);
						break;
					case "Debugging":
						debugging = Boolean.valueOf(value);
						break;
					}
				}
			}
			return valid();
		} catch (IOException e) {
			return false;
		}
	}

	private boolean valid() {
		return name != null && !name.equals("") && max_players >= 0;
	}

	private void closeRedaer(Reader reader) {
		try {
			reader.close();
		} catch (IOException e) {}
	}

	private BufferedReader getReader(File file) {
		try {
			return new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	private PrintWriter getWriter(File file) {
		try {
			return new PrintWriter(new FileWriter(file, true));
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Creates a default configuration file that is valid
	 *
	 * @param writer
	 *            the initialised {@link PrintWriter} with the open
	 *            configuration file
	 */
	private void createDefault(PrintWriter writer) {
		writer.println("// Default generated Server Configuration File");
		writer.println("Name: TCP Server");
		writer.println("Port: 47247");
		writer.println("Max players: 16");
	}

	public Vector<String> getBanList() {
		return banList;
	}

	public boolean isDebugging() {
		return debugging;
	}

	public void setDebugging(boolean debugging) {
		this.debugging = debugging;
	}

	public int getMaxPlayers() {
		return max_players;
	}

	public int getPort() {
		return port;
	}

	public String getName() {
		return name;
	}
}
