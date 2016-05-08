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

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import eu.fusster.ui.ServerUI;

public class Loader {

	String path;

	/**
	 * A file/directory loader that uses the path where the .jar was ran from.
	 */
	public Loader() {
		File f = new File(System.getProperty("java.class.path"));
		File dir = f.getAbsoluteFile().getParentFile();
		path = dir.toString().split(";")[0];
	}

	/**
	 * Loads a file with a specific name and if there s no file it creates new
	 * 
	 * @param name
	 *            the name of the file
	 * @return the actual {@link File}
	 */
	public File loadFile(String name) {
		try {
			File file = new File(path, name);

			if (!file.exists())
				file.createNewFile();
			return file;
		} catch (IOException io) {
			io.printStackTrace();
			ServerUI.error(io.getMessage());
			return null;
		}
	}

	/**
	 * Loads a directory with a specific name and if there s no directory it
	 * creates new
	 * 
	 * @param name
	 *            the name of the directory
	 * @return the actual {@link File}
	 */
	public File loadDirectory(String name) {
		File file = new File(path, name);

		if (file.exists())
			if (file.isDirectory())
				return file;

		file.mkdir();
		return file;
	}
	
	public static Image loadImage(String path) throws NullPointerException{
		return Toolkit.getDefaultToolkit().getImage(path);
	}

	public String getPath() {
		return path;
	}

}
