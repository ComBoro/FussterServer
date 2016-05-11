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

package eu.fusster.player;

import eu.fusster.Fusster;

public class PlayerException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Extension of {@link Exception} concentrated for exceptions occured in
	 * player management
	 */
	public PlayerException() {
		super("[Player Exception]");
	}

	public PlayerException(String message) {
		super("[Player Exception] " + message);
	}

	@Override
	public void printStackTrace() {
		Fusster.append(super.getMessage());
	}

}
