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

package brunonova.drmips.pc;

import brunonova.drmips.simulator.Data;
import com.jtattoo.plaf.hifi.HiFiLookAndFeel;
import com.jtattoo.plaf.mint.MintLookAndFeel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;

/**
 * Utility functions.
 * 
 * @author Bruno Nova
 */
public class Util {
	/** The index of the binary format in combo boxes. */
	public static final int BINARY_FORMAT_INDEX = 0;
	/** The index of the decimal format in combo boxes. */
	public static final int DECIMAL_FORMAT_INDEX = 1;
	/** The index of the hexadecimal format in combo boxes. */
	public static final int HEXADECIMAL_FORMAT_INDEX = 2;
	/** The index of the instruction performance mode type in combo boxes. */
	public static final int INSTRUCTION_PERFORMANCE_TYPE_INDEX = 0;
	/** The index of the CPU performance mode type in combo boxes. */
	public static final int CPU_PERFORMANCE_TYPE_INDEX = 1;
	
	/** The constant that represents the left side of the split pane. */
	public static final int LEFT = 0;
	/** The constant that represents the right side of the split pane. */
	public static final int RIGHT = 1;
	
	/** Class logger. */
	private static final Logger LOG = Logger.getLogger(Util.class.getName());

	/** Color of a wire in the datapath. */
	public static Color wireColor;
	/** Color of the control path in the datapath. */
	public static Color controlPathColor;
	/** Color of the critical path in the datapath. */
	public static Color criticalPathColor;
	/** Color of a "irrelevant" wire in the datapath. */
	public static Color irrelevantColor;
	/** Color of a register/address being read. */
	public static Color readColor;
	/** Color of a register/address being written. */
	public static Color writeColor;
	/** Color of a register/address being read and written at the same time. */
	public static Color rwColor;
	/** Color of an instruction being executed (unicycle). */
	public static Color instColor;
	/** Color of the IF pipeline stage. */
	public static Color ifColor;
	/** Color of the ID pipeline stage. */
	public static Color idColor;
	/** Color of the EX pipeline stage. */
	public static Color exColor;
	/** Color of the MEM pipeline stage. */
	public static Color memColor;
	/** Color of the WB pipeline stage. */
	public static Color wbColor;

	static { // Initialize the colors
		setColorsLightTheme();
	}

	/**
	 * Converts the specified color to an "rgb(R,G,B)" string (for use in HTML).
	 * @param color The color.
	 * @return String in the format "rgb(R,G,B)".
	 */
	public static String colorToRGBString(Color color) {
		return "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
	}

	/**
	 * Centers the given window on the screen.
	 * @param window Window to center.
	 */
	public static void centerWindow(Window window) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		window.setLocation((int)screen.getWidth() / 2 - window.getWidth() / 2, (int)screen.getHeight() / 2 - window.getHeight() / 2);
	}
	
	/**
	 * Configures the given window to be closed when the Escape button is pressed.
	 * @param <W> A window (JFrame, JDialog, etc.).
	 * @param window Window to configure.
	 */
	public static <W extends Window & RootPaneContainer> void enableCloseWindowWithEscape(final W window) {
		Action closeAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
			}
		};
		
		window.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
		window.getRootPane().getActionMap().put("close", closeAction);
	}
	
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
	 * Sets the program's light look and feel.
	 */
	@SuppressWarnings("UseSpecificCatch")
	public static void setLightLookAndFeel() {
		try {
			Properties props = new Properties();
			props.put("logoString", "");
			props.put("windowDecoration", "off");
			MintLookAndFeel.setCurrentTheme(props);
			UIManager.setLookAndFeel("com.jtattoo.plaf.mint.MintLookAndFeel");
			setColorsLightTheme();
		} catch (Exception ex) {
			LOG.log(Level.WARNING, "error setting light LookAndFeel", ex);
		}
	}
	
	/**
	 * Sets the program's dark look and feel.
	 */
	@SuppressWarnings("UseSpecificCatch")
	public static void setDarkLookAndFeel() {
		try {
			Properties props = new Properties();
			props.put("logoString", "");
			props.put("windowDecoration", "off");
			HiFiLookAndFeel.setCurrentTheme(props);
			UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
			setColorsDarkTheme();
		} catch (Exception ex) {
			LOG.log(Level.WARNING, "error setting dark LookAndFeel", ex);
		}
	}

	public static void setColorsLightTheme() {
		wireColor = Color.BLACK;
		controlPathColor = new Color(0, 130, 200);
		criticalPathColor = Color.RED;
		irrelevantColor = new Color(150, 150, 150);
		readColor = new Color(128, 255, 128);
		writeColor = new Color(255, 128, 128);
		rwColor = new Color(255, 255, 128);
		instColor = new Color(200, 200, 200);
		ifColor = new Color(128, 255, 255);
		idColor = readColor;
		exColor = rwColor;
		memColor = writeColor;
		wbColor = new Color(255, 128, 255);
	}

	public static void setColorsDarkTheme() {
		wireColor = Color.WHITE;
		controlPathColor = new Color(0, 170, 230);
		criticalPathColor = Color.RED;
		irrelevantColor = Color.GRAY;
		readColor = new Color(0, 128, 0);
		writeColor = new Color(128, 0, 0);
		rwColor = new Color(128, 128, 0);
		instColor = new Color(110, 110, 110);
		ifColor = new Color(0, 128, 128);
		idColor = readColor;
		exColor = rwColor;
		memColor = writeColor;
		wbColor = new Color(128, 0, 128);
	}
}
