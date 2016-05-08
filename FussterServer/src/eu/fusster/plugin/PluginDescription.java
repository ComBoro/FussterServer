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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class used for reading an input stream of a file and construction a usable
 * description about a plugin
 * 
 * @author Admin
 * @see FussterPlugin
 */
public class PluginDescription {

	private String name;
	private String version;
	private String mainClass;
	private String author;

	public PluginDescription(InputStream stream) throws PluginException {
		InputStreamReader reader = new InputStreamReader(stream);
		BufferedReader buffredReader = new BufferedReader(reader);
		String line;
		try {
			while ((line = buffredReader.readLine()) != null) {
				if (!line.contains(":"))
					continue;
				String[] split = line.split(":");
				String variable = split[0];
				String value = split[1];
				switch (variable) {
				case "main":
					this.mainClass = value;
					break;
				case "version":
					this.version = value;
					break;
				case "name":
					this.name = value;
					break;
				case "author":
					this.author = value;
					break;
				}
			}
		} catch (IOException e) {
			throw new PluginException(e.getMessage(), name, e);
		}

		if (!isValid())
			throw new PluginException("Invalid config file.");
	}

	private boolean isValid() {
		return name != null && version != null && mainClass != null
				&& !name.contains(".") && !name.contains(" ");
	}

	public String getName() {
		return name;
	}

	public String getAuthor() {
		return author;
	}

	public String getVersion() {
		return version;
	}

	public String getMain() {
		return mainClass;
	}

}
