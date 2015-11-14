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

package brunonova.drmips.android;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

import brunonova.drmips.simulator.Data;

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

	/**
	 * Returns the color with the given attribute ID for the current theme.
	 * @param context The current Activity/Application context.
	 * @param attrId The resource attribute ID of the desired color.
	 * @return The desired color, or <tt>0</tt> if not found or in case of error.
	 */
	public static int getThemeColor(Context context, int attrId) {
		try {
			TypedValue value = new TypedValue();
			boolean ret = context.getTheme().resolveAttribute(attrId, value, true);
			if (ret)
				return context.getResources().getColor(value.resourceId);
			else {
				Log.e(Util.class.getName(), "failed to find theme color attribute " + attrId);
				return 0; // return something in case of failure
			}
		}
		catch (Exception ex) {
			Log.e(Util.class.getName(), "error retrieving theme color attribute " + attrId, ex);
			return 0; // return something in case of failure
		}
	}
}
