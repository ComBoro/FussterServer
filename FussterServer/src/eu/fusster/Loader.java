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

/**
 * A file/directory loader that uses the path_bin where the .jar was ran from.
 */
public class Loader {

	public static final String path_bin = new File(
			System.getProperty("java.class.path")).getAbsoluteFile()
			.getParentFile().toString().split(";")[0];

	public static final String path_src = new File(path_bin).getParent()
			+ "/src/";

	/**
	 * Loads a directory with a specific name and if there s no directory it
	 * creates new
	 * 
	 * @param name
	 *            the name of the directory
	 * @return the actual {@link File}
	 */
	public static File loadDirectory(String name) {
		File file = new File(path_bin, name);

		if (file.exists())
			if (file.isDirectory())
				return file;

		file.mkdir();
		return file;
	}

	/**
	 * Loads a file with a specific name and if there s no file it creates new
	 * 
	 * @param name
	 *            the name of the file
	 * @return the actual {@link File}
	 */
	public static File loadFile(String name) {
		try {
			File file = new File(path_bin, name);

			if (!file.exists())
				file.createNewFile();
			return file;
		} catch (IOException io) {
			io.printStackTrace();
			return null;
		}
	}

	public static Image loadImage(String path) throws NullPointerException {
		return Toolkit.getDefaultToolkit().getImage(path);
	}

}
