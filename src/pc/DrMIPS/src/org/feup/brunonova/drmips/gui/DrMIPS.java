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

import java.io.File;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;


/**
 * Entry point of the program.
 * <p>It also contains some global constants and parameters.</p>
 * 
 * @author Bruno Nova
 */
public class DrMIPS {
	/** The name of this program. */
	public static final String PROGRAM_NAME = "DrMIPS";
	/** The version of the program, as a string. */
	public static String VERSION = "1.1.0";
	
	/** Reference to the user preferences. */
	public static Preferences prefs = Preferences.userNodeForPackage(DrMIPS.class);
	/** The full path to the program's jar folder, or '.' if not running from a jar (when running from the IDE, for example). */
	public static String path = ".";
	/** The CPU file loaded by default. */
	public static final String DEFAULT_CPU = "cpu" + File.separator + "unicycle.cpu";
	/** The name of the documentation directory. */
	public static final String DOC_DIR = "doc";
	/** The default format with which the registers are displayed. */
	public static final int DEFAULT_REGISTER_FORMAT = Util.DECIMAL_FORMAT_INDEX;
	/** The default format with which the datapath data is displayed. */
	public static final int DEFAULT_DATAPATH_DATA_FORMAT = Util.DECIMAL_FORMAT_INDEX;
	/** The default format with which the assembled code data is displayed. */
	public static final int DEFAULT_ASSEMBLED_CODE_FORMAT = Util.DECIMAL_FORMAT_INDEX;
	/** The default format with which the data memory values are displayed. */
	public static final int DEFAULT_DATA_MEMORY_FORMAT = Util.DECIMAL_FORMAT_INDEX;
	/** The default performance mode type. */
	public static final int DEFAULT_PERFORMANCE_TYPE = Util.CPU_PERFORMANCE_TYPE_INDEX;
	/** The default side of the code tab. */
	public static final int DEFAULT_CODE_TAB_SIDE = Util.LEFT;
	/** The default side of the datapath tab. */
	public static final int DEFAULT_DATAPATH_TAB_SIDE = Util.LEFT;
	/** The default side of the registers tab. */
	public static final int DEFAULT_REGISTERS_TAB_SIDE = Util.RIGHT;
	/** The default side of the assembled code tab. */
	public static final int DEFAULT_ASSEMBLED_CODE_TAB_SIDE = Util.LEFT;
	/** The default side of the data memory tab. */
	public static final int DEFAULT_DATA_MEMORY_TAB_SIDE = Util.LEFT;
	/** The default status of the "reset data before assembling" checkbox. */
	public static final boolean DEFAULT_ASSEMBLE_RESET = true;
	/** The default theme. */
	public static final boolean DEFAULT_DARK_THEME = false;
	/** The default tabs/windows layout used. */
	public static final boolean DEFAULT_INTERNAL_WINDOWS = false;
	/** Whether the control path is shown by default. */
	public static final boolean DEFAULT_SHOW_CONTROL_PATH = true;
	/** Whether arrows are shown on the wires by default. */
	public static final boolean DEFAULT_SHOW_ARROWS = true;
	/** Whether the performance mode is enabled by default. */
	public static final boolean DEFAULT_PERFORMANCE_MODE = false;
	/** Whether the in/out tips are shown by default. */
	public static final boolean DEFAULT_OVERLAYED_DATA = true;
	/** The key of the language preference. */
	public static final String LANG_PREF = "lang";
	/** The key of the last loaded CPU preference. */
	public static final String LAST_CPU_PREF = "last_cpu";
	/** The prefix of the recent files preferences. */
	public static final String RECENT_FILES_PREF = "recent";
	/** The maximum number of recent files to track. */
	public static final int MAX_RECENT_FILES = 10;
	/** The prefix of the recent CPUs preferences. */
	public static final String RECENT_CPUS_PREF = "recent_cpu";
	/** The maximum number of recent CPUs to track. */
	public static final int MAX_RECENT_CPUS = 10;
	/** The key of the registers display format preference. */
	public static final String REGISTER_FORMAT_PREF = "reg_format";
	/** The key of the datapath display data format preference. */
	public static final String DATAPATH_DATA_FORMAT_PREF = "datapath_format";
	/** The key of the assembled code display data format preference. */
	public static final String ASSEMBLED_CODE_FORMAT_PREF = "assembled_code_format";
	/** The key of the data memory display data format preference. */
	public static final String DATA_MEMORY_FORMAT_PREF = "data_memory_format";
	/** The key of the code tab side preference. */
	public static final String CODE_TAB_SIDE_PREF = "code_tab_side";
	/** The key of the datapath tab side preference. */
	public static final String DATAPATH_TAB_SIDE_PREF = "datapath_tab_side";
	/** The key of the registers tab side preference. */
	public static final String REGISTERS_TAB_SIDE_PREF = "reg_tab_side";
	/** The key of the assembled code tab side preference. */
	public static final String ASSEMBLED_CODE_TAB_SIDE_PREF = "assembled_code_tab_side";
	/** The key of the data memory tab side preference. */
	public static final String DATA_MEMORY_TAB_SIDE_PREF = "data_mem_tab_side";
	/** The key of the divider location preference. */
	public static final String DIVIDER_LOCATION_PREF = "div_location";
	/** The key of the assemble reset preference. */
	public static final String ASSEMBLE_RESET_PREF = "assemble_reset";
	/** The key of the dark theme preference. */
	public static final String DARK_THEME_PREF = "dark_theme";
	/** The key of the internal windows preference. */
	public static final String INTERNAL_WINDOWS_PREF = "internal_windows";
	/** The key of the show control path preference. */
	public static final String SHOW_CONTROL_PATH_PREF = "show_control_path";
	/** The key of the show arrows preference. */
	public static final String SHOW_ARROWS_PREF = "show_arrows";
	/** The key of the performance mode preference. */
	public static final String PERFORMANCE_MODE_PREF = "performance_mode";
	/** The key of the performance mode type preference. */
	public static final String PERFORMANCE_TYPE_PREF = "performance_type";
	/** The key of the overlayed data preference. */
	public static final String OVERLAYED_DATA_PREF = "overlayed_data";
	/** The number of milliseconds before tooltips are displayed. */
	public static final int TOOLTIP_INITIAL_DELAY = 200;
	/** The number of milliseconds tooltips are shown before disappearing. */
	public static final int TOOLTIP_DISMISS_DELAY = 30000;
	
	/** "Loading" dialog. */
	private static DlgLoading dlgLoading = null;
	/** Class logger. */
	private static final Logger LOG = Logger.getLogger(DrMIPS.class.getName());
	
	public static void main(String[] args) {
		try { // Display a "loading" dialog
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					dlgLoading = new DlgLoading();
					dlgLoading.setVisible(true);
					Util.centerWindow(dlgLoading);
				}
			});		
		} catch (Exception ex) {
			LOG.log(Level.WARNING, "error displaying \"loading\" dialog", ex);
		}
		
		// Find the path to the program
		try {
			URI uri = DrMIPS.class.getProtectionDomain().getCodeSource().getLocation().toURI(); // get the path to the jar (if running from a jar)
			String p = uri.toString();
			p = p.startsWith("file://") ? p.substring(5) : uri.getPath(); // uri to String (careful with Windows network (UNC) paths)
			if(p.toLowerCase().endsWith(".jar")) { // if running from a jar, get the path of the parent dir
				File f = (new File(p)).getParentFile();
				if(f != null) path = f.getCanonicalPath();
				LOG.log(Level.INFO, "running from jar file; path to jar folder: {0}", path);
			}
			else
				LOG.info("not running from jar file");
		} catch (Exception ex) {
			LOG.log(Level.WARNING, "error finding the path of the program", ex);
		}
		
		// Set the theme
		if(prefs.getBoolean(DARK_THEME_PREF, DEFAULT_DARK_THEME))
			Util.setDarkLookAndFeel();
		else
			Util.setLightLookAndFeel();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SwingUtilities.updateComponentTreeUI(dlgLoading);
			}
		});
		
		// Load the strings
		if(!Lang.loadPreferredLanguage()) {
			JOptionPane.showMessageDialog(null, "Error opening language file " + Lang.getLanguage() + "!", PROGRAM_NAME, JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		// Translate the "loading" window
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				dlgLoading.setProgressBarText(Lang.t("loading"));
			}
		});
		
		// Change tooltip delays
		ToolTipManager.sharedInstance().setInitialDelay(TOOLTIP_INITIAL_DELAY);
		ToolTipManager.sharedInstance().setDismissDelay(TOOLTIP_DISMISS_DELAY);
		
		// Start the GUI
		FrmSimulator frmSim;
		if(args.length == 1)
			frmSim = new FrmSimulator(args[0]);
		else
			frmSim = new FrmSimulator();
		frmSim.setVisible(true);
		dlgLoading.dispose();
	}
}
