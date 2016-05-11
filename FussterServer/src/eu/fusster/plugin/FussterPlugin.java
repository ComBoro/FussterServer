/*
// * This file is part of Fusster.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import javax.swing.JComponent;

import eu.fusster.Fusster;
import eu.fusster.command.Command;
import eu.fusster.command.CommandSender;

/**
 * An abstract class used to represent any non-server file that is loaded into
 * the runtime
 *
 */
public abstract class FussterPlugin {

	private File file, dataFolder, defaultConfig;
	protected Path defaultConfigPath;
	protected PluginDescription description;
	private ClassLoader classLoader;
	protected PluginLoader pluginLoader;

	public FussterPlugin() {
		final ClassLoader classLoader = this.getClass().getClassLoader();
		if (!(classLoader instanceof PluginClassLoader))
			throw new IllegalStateException("FussterPlugin requires "
					+ PluginClassLoader.class.getName());
		((PluginClassLoader) classLoader).initialize(this);
	}

	protected final void initialize(PluginLoader loader,
			PluginDescription description, File file, File dataFolder,
			File defaultConfig, ClassLoader classLoader) {
		this.classLoader = classLoader;
		this.description = description;
		this.file = file;
		this.dataFolder = dataFolder;
		this.defaultConfig = defaultConfig;
		this.defaultConfigPath = Paths.get(defaultConfig.toURI());
		this.pluginLoader = loader;

		try {
			onEnable();
		} catch (Exception e) {
			Fusster.error("Error loading plugin "
					+ getDescription().getName());
		}
	}

	/**
	 * Called when a {@link CommandSender} send a command to the server.
	 * 
	 * @param sender
	 *            The sender of the command (by default senders are: Player,
	 *            Console).
	 * @param label
	 *            The label of the command (the first String, in the example
	 *            "plugins reload all", "plugins" is the label).
	 * @param args
	 *            The arguments following the label (in the example
	 *            "plugins reload all", "reload all" are the arguments).
	 * @return Whether the command was used successfully. By default it is set
	 *         to false.
	 */
	protected abstract boolean onCommand(CommandSender sender, String label,
			String[] args) throws Exception;

	// Plugin events
	/**
	 * Called when the plugin was successfully loaded into the runtime.
	 */
	protected abstract void onEnable() throws Exception;

	/**
	 * Called when the plugin gets unloaded from the runtime.
	 * 
	 * @see PluginLoader#unload(FussterPlugin)
	 */
	protected abstract void onDisable() throws Exception;

	/**
	 * Used to register a command to the {@link PluginMap}
	 * 
	 * @param label
	 *            The label of the command as it would want to be called
	 * @param command
	 *            The actual command instance corresponding to the label
	 */
	protected final void registerCommand(String label, Command command) {
		Fusster.getPluginMap().registerCommand(this, command, label);
	}
	
	
	/**
	 * Registers a key and a value saved in the the default auto respond list
	 * 
	 * @param key
	 *            The key that represents a command label
	 * @param value
	 *            The value linked with the key
	 */
	protected final void registerPropertie(String key, String value){
		Fusster.getProperties().put(key, value);
		Fusster.getPluginMap().link(this, key);
	}
	
	/**
	 * Used to register a JComponent onto the ConsoleTabbedPane in the GUI
	 * 
	 * @param label
	 *            The label of the JComponent as it would be displayed
	 * @param component
	 *            The actual JComponent that will be added
	 */
	protected final void registerTab(String label, JComponent component){
		Fusster.getServerUI().getConsoleTabbedPane().add(label, component);
		Fusster.getPluginMap().link(this, component);
	}

	/**
	 * The default config corresponding to the server.
	 * 
	 * @return The default file named "default.cfg" located in the data folder
	 * @see #getDataFolder()
	 */
	protected final File getDefaultConfig() {
		return defaultConfig;
	}

	/**
	 * The default created {@link BufferedReader} using the input stream of the
	 * default config.
	 * 
	 * @return {@link BufferedReader} created using the input stream of the
	 *         default config
	 * @deprecated The {@link BufferedReader} may be closed or even null
	 * @throws NullPointerException
	 *             if the config is null
	 * @throws FileNotFoundException
	 *             if the file does not exist, is a directory rather than a
	 *             regular file, or for some other reason cannot be opened for
	 *             reading
	 */
	@Deprecated
	protected final BufferedReader readDefaultConfig()
			throws NullPointerException, FileNotFoundException {
		if (defaultConfig == null)
			throw new NullPointerException("The config reader is null");
		return new BufferedReader(new FileReader(defaultConfig));
	}
	
	/**
	 * Reads the config and returns all the lines in the default config file
	 * 
	 * @return All the lines in the default config file
	 * @throws IOException  if an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read
	 */
	protected final java.util.List<String> readDefaultConfigAllLines() throws IOException{
		return Files.readAllLines(defaultConfigPath);
	}

	/**
	 * The default created {@link PrintWriter} using the output stream of the
	 * default config.
	 * 
	 * @return {@link PrintWriter} created using the output stream of the
	 *         default config
	 * @deprecated The {@link FileWriter} may be closed or even null
	 * @throws NullPointerException
	 *             if the default config is null
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	@Deprecated
	protected final FileWriter writeDefaultConfig()
			throws NullPointerException, IOException {
		if (defaultConfig == null)
			throw new NullPointerException("The config writer is null");
		return new FileWriter(defaultConfig);
	}

	/**
	 * Writes a text to the default configuration file
	 * 
	 * @param text
	 *            The text to be written
	 * @throws NullPointerException
	 *             if the writer is equal to null
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	protected final void writeDefaultConfig(String text)
			throws NullPointerException, IOException {
		Files.write(Paths.get(defaultConfig.toURI()), Arrays.asList(text),
				Charset.forName("UTF-8"), StandardOpenOption.APPEND);
	}

	/**
	 * Writes an array of Strings one by one to the default configuration file
	 * 
	 * @param text
	 *            The text to be written
	 * @throws NullPointerException
	 *             if the writer is equal to null
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	protected final void writeDefaultConfig(String... text)
			throws NullPointerException, IOException {
		Files.write(Paths.get(defaultConfig.toURI()), Arrays.asList(text),
				Charset.forName("UTF-8"), StandardOpenOption.APPEND);
	}

	/**
	 * The data folder that the plugin has by default.
	 * 
	 * @return The Data Folder that corresponds to the plugin. Usually the
	 *         default data folder contains the default config file
	 */
	protected final File getDataFolder() {
		return dataFolder;
	}

	/**
	 * Obtain the actual file that the plugin was loaded from.
	 * 
	 * @return The .jar file that is the plugin
	 */
	public final File getFile() {
		return file;
	}

	/**
	 * Obtain the {@link PluginDescription} used in the Plugin.
	 * 
	 * @return The default {@link PluginDescription} used in the creating of the
	 *         plugin
	 * @see PluginDescription
	 */
	public final PluginDescription getDescription() {
		return description;
	}

	/**
	 * 
	 * @return The {@link ClassLoader} used to load that class. If loaded
	 *         correctly this must return an instance of
	 *         {@link PluginClassLoader}
	 */
	public final ClassLoader getLoader() {
		return classLoader;
	}

}
