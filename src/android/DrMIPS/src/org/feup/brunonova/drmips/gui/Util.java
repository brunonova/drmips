/*
    DrMIPS - Educational MIPS simulator
    Copyright (C) 2013 Bruno Nova <ei08109@fe.up.pt>

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

package org.feup.brunonova.drmips.gui;

import org.feup.brunonova.drmips.mips.Data;

/**
 * Utility functions.
 * 
 * @author Bruno Nova
 */
public class Util {
	/** The index of the binary format in spinners. */
	public static final int BINARY_FORMAT_INDEX = 0;
	/** The index of the decimal format in spinners. */
	public static final int DECIMAL_FORMAT_INDEX = 1;
	/** The index of the hexadecimal format in spinners. */
	public static final int HEXADECIMAL_FORMAT_INDEX = 2;
	/** The index of the instruction performance mode type in combo boxes. */
	public static final int INSTRUCTION_PERFORMANCE_TYPE_INDEX = 0;
	/** The index of the CPU performance mode type in combo boxes. */
	public static final int CPU_PERFORMANCE_TYPE_INDEX = 1;
	
	/**
	 * Returns a string the the given data formated in bin/dec/hex according to the selected format in the given combo box.
	 * @param data Original data.
	 * @param format The data format (<tt>Util.BINARYL_FORMAT_INDEX/Util.DECIMAL_FORMAT_INDEX/Util.HEXADECIMAL_FORMAT_INDEX</tt>).
	 * @return Data formated to the selected format, as a string.
	 */
	public static String formatDataAccordingToFormat(Data data, int format) {
		switch(format) {
			case BINARY_FORMAT_INDEX: return data.toBinary();
			case HEXADECIMAL_FORMAT_INDEX: return data.toHexadecimal();
			default: return "" + data.getValue();
		}
	}
}
