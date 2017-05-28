/*
    DrMIPS - Educational MIPS simulator
    Copyright (C) 2013-2015 Bruno Nova <brunomb.nova@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package brunonova.drmips.simulator.exceptions;

/**
 * Exception to be thrown when an instruction type is invalid.
 *
 * @author Bruno Nova
 */
public class InvalidInstructionSetException extends Exception {
	/**
	 * Exception constructor.
	 * @param msg The error message.
	 */
	public InvalidInstructionSetException(String msg) {
		super(msg);
	}

	/**
	 * Exception constructor.
	 * @param msg The error message.
	 * @param cause The throwable that caused this exception.
	 */
	public InvalidInstructionSetException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
