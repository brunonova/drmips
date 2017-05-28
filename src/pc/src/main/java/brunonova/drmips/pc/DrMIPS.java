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

import brunonova.drmips.simulator.AppInfo;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;


/**
 * Entry point of the program.
 * <p>It also contains some global constants and parameters.</p>
 *
 * @author Bruno Nova
 */
public class DrMIPS {
	/** The number of milliseconds before tooltips are displayed. */
	public static final int TOOLTIP_INITIAL_DELAY = 200;
	/** The number of milliseconds tooltips are shown before disappearing. */
	public static final int TOOLTIP_DISMISS_DELAY = 30000;
	/** Reference to the user preferences. */
	public static Preferences prefs = Preferences.userNodeForPackage(DrMIPS.class);
	/** The full path to the program's jar folder, or '.' if not running from a jar (when running from the IDE, for example). */
	public static String path = ".";
	/** The CPU file loaded by default. */
	public static final String DEFAULT_CPU = "cpu" + File.separator + "unicycle.cpu";
	/** Relative path to the documentation directory. */
	public static final String DOC_DIR = "doc";
	/** Absolute path to the fallback documentation directory (when installed in a separate directory). */
	public static final String DOC_DIR2 = "/usr/share/doc/drmips/manuals";

	// Names of the preferences
	public static final String LANG_PREF = "lang";
	public static final String LAST_CPU_PREF = "last_cpu";
	public static final String RECENT_FILES_PREF = "recent";
	public static final int MAX_RECENT_FILES = 10;
	public static final String RECENT_CPUS_PREF = "recent_cpu";
	public static final int MAX_RECENT_CPUS = 10;
	public static final String REGISTER_FORMAT_PREF = "reg_format";
	public static final String DATAPATH_DATA_FORMAT_PREF = "datapath_format";
	public static final String ASSEMBLED_CODE_FORMAT_PREF = "assembled_code_format";
	public static final String DATA_MEMORY_FORMAT_PREF = "data_memory_format";
	public static final String CODE_TAB_SIDE_PREF = "code_tab_side";
	public static final String DATAPATH_TAB_SIDE_PREF = "datapath_tab_side";
	public static final String REGISTERS_TAB_SIDE_PREF = "reg_tab_side";
	public static final String ASSEMBLED_CODE_TAB_SIDE_PREF = "assembled_code_tab_side";
	public static final String DATA_MEMORY_TAB_SIDE_PREF = "data_mem_tab_side";
	public static final String DIVIDER_LOCATION_PREF = "div_location";
	public static final String ASSEMBLE_RESET_PREF = "assemble_reset";
	public static final String DARK_THEME_PREF = "dark_theme";
	public static final String INTERNAL_WINDOWS_PREF = "internal_windows";
	public static final String SHOW_CONTROL_PATH_PREF = "show_control_path";
	public static final String SHOW_ARROWS_PREF = "show_arrows";
	public static final String PERFORMANCE_MODE_PREF = "performance_mode";
	public static final String PERFORMANCE_TYPE_PREF = "performance_type";
	public static final String OVERLAYED_DATA_PREF = "overlayed_data";
	public static final String OVERLAYED_SHOW_NAMES_PREF = "overlayed_show_names";
	public static final String OVERLAYED_SHOW_FOR_ALL_PREF = "overlayed_show_for_all";
	public static final String MAXIMIZED_PREF = "maximized";
	public static final String WIDTH_PREF = "width";
	public static final String HEIGHT_PREF = "height";
	public static final String MARGIN_LINE_PREF = "margin_line";
	public static final String LAST_FILE_PREF = "last_file";
	public static final String OPEN_LAST_FILE_AT_STARTUP_PREF = "open_last_file_at_startup";
	public static final String SCALE_PREF = "scale";
	public static final String AUTO_SCALE_PREF = "auto_scale";
	public static final String OPENGL_PREF = "use_opengl";

	// Default values of the preferences
	public static final int DEFAULT_REGISTER_FORMAT = Util.DECIMAL_FORMAT_INDEX;
	public static final int DEFAULT_DATAPATH_DATA_FORMAT = Util.DECIMAL_FORMAT_INDEX;
	public static final int DEFAULT_ASSEMBLED_CODE_FORMAT = Util.DECIMAL_FORMAT_INDEX;
	public static final int DEFAULT_DATA_MEMORY_FORMAT = Util.DECIMAL_FORMAT_INDEX;
	public static final int DEFAULT_PERFORMANCE_TYPE = Util.CPU_PERFORMANCE_TYPE_INDEX;
	public static final int DEFAULT_CODE_TAB_SIDE = Util.LEFT;
	public static final int DEFAULT_DATAPATH_TAB_SIDE = Util.LEFT;
	public static final int DEFAULT_REGISTERS_TAB_SIDE = Util.RIGHT;
	public static final int DEFAULT_ASSEMBLED_CODE_TAB_SIDE = Util.LEFT;
	public static final int DEFAULT_DATA_MEMORY_TAB_SIDE = Util.LEFT;
	public static final boolean DEFAULT_ASSEMBLE_RESET = true;
	public static final boolean DEFAULT_DARK_THEME = false;
	public static final boolean DEFAULT_INTERNAL_WINDOWS = false;
	public static final boolean DEFAULT_SHOW_CONTROL_PATH = true;
	public static final boolean DEFAULT_SHOW_ARROWS = true;
	public static final boolean DEFAULT_PERFORMANCE_MODE = false;
	public static final boolean DEFAULT_OVERLAYED_DATA = true;
	public static final boolean DEFAULT_OVERLAYED_SHOW_NAMES = false;
	public static final boolean DEFAULT_OVERLAYED_SHOW_FOR_ALL = false;
	public static final boolean DEFAULT_MAXIMIZED = true;
	public static final int DEFAULT_WIDTH = 800;
	public static final int DEFAULT_HEIGHT = 600;
	public static final boolean DEFAULT_MARGIN_LINE = false;
	public static final boolean DEFAULT_OPEN_LAST_FILE_AT_STARTUP = false;
	public static final double DEFAULT_SCALE = 1.0;
	public static final boolean DEFAULT_AUTO_SCALE = false;
	public static final boolean DEFAULT_OPENGL = false;

	/** "Loading" dialog. */
	private static DlgLoading dlgLoading = null;
	/** Main window. */
	private static FrmSimulator frmSim = null;
	/** Optional filename to open. */
	private static String filename = null;
	/** Class logger. */
	private static final Logger LOG = Logger.getLogger(DrMIPS.class.getName());

	private static void displayHelpAndExit(OptionParser parser) {
		// The help text will contain a "Usage" line. The default value of the line is:
		//   Usage: java -jar DrMIPS.jar [options] [file]
		// If the simulator will be started from a launcher script/program, you may
		// want to change the displayed "Usage" line. To do that, you must define
		// the "program.name" property in the launcher script, like so:
		//   java -Dprogram.name=drmips -jar /path/to/DrMIPS.jar
		// This example will change the "Usage" line to:
		//   Usage: drmips [options] [file]
		String prog_name = System.getProperty("program.name", "java -jar " + AppInfo.NAME + ".jar");

		System.out.println(AppInfo.NAME + " - " + AppInfo.DESCRIPTION);
		System.out.println("Usage: " + prog_name + " [options] [file]\n");
		try {
			parser.printHelpOn(System.out);
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, "failed to print help", ex);
		}
		System.exit(0);
	}

	private static void displayVersionAndExit() {
		System.out.println(AppInfo.NAME + " " + AppInfo.VERSION + "\n"
			+ AppInfo.COPYRIGHT + "\n"
			+ "License: " + AppInfo.LICENSE_SHORT);
		System.exit(0);
	}

	private static void enableOpenGl() {
		// Hardware acceleration using OpenGL
		System.setProperty("sun.java2d.opengl", "True");
	}

	@SuppressWarnings("UseSpecificCatch")
	public static void main(String[] args) {
		boolean useOpenGl = prefs.getBoolean(OPENGL_PREF, DEFAULT_OPENGL);

		// Setup the default exception handler
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

		// Parse command-line arguments
		try {
			OptionParser parser = new OptionParser();
			OptionSpec<String> fileArg = parser.nonOptions("code file to open")
											   .ofType(String.class).describedAs("file");
			parser.acceptsAll(Arrays.asList("h", "help"), "display this help and exit").forHelp();
			parser.accepts("version", "display version information and exit");
			parser.accepts("opengl", "enable OpenGL hardware acceleration");
			parser.accepts("no-opengl", "disable OpenGL hardware acceleration");
			parser.accepts("reset", "reset all settings to their defaults");

			OptionSet options = parser.parse(args);
			List<String> otherArgs = options.valuesOf(fileArg);
			if(options.has("help"))
				displayHelpAndExit(parser);
			else if(options.has("version"))
				displayVersionAndExit();
			if(options.has("reset")) {
				prefs.clear();
				useOpenGl = DEFAULT_OPENGL;
			}
			if(options.has("no-opengl")) {
				useOpenGl = false;
				prefs.putBoolean(OPENGL_PREF, useOpenGl);
			} else if(options.has("opengl")) {
				useOpenGl = true;
				prefs.putBoolean(OPENGL_PREF, useOpenGl);
			}
			if(!otherArgs.isEmpty()) {
				if(otherArgs.size() == 1)
					filename = otherArgs.get(0);
				else {
					System.err.println("Only one file name should be supplied!");
					System.exit(1);
				}
			}
		} catch(Exception ex) {
			System.err.println("Error parsing arguments: " + ex.getMessage());
			System.exit(1);
		}

		// Try to enable OpenGL hardware acceleration, unless requested not to
		if(useOpenGl)
			enableOpenGl();

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

		// Start the GUI
		SwingUtilities.invokeLater(new StartGUIRunnable());
	}

	private static class StartGUIRunnable implements Runnable {
		@Override
		public void run() {
			// Change tooltip delays
			ToolTipManager.sharedInstance().setInitialDelay(TOOLTIP_INITIAL_DELAY);
			ToolTipManager.sharedInstance().setDismissDelay(TOOLTIP_DISMISS_DELAY);

			// Display a "loading" dialog
			dlgLoading = new DlgLoading();
			dlgLoading.setVisible(true);
			Util.centerWindow(dlgLoading);
			dlgLoading.paint(dlgLoading.getGraphics());

			// Set the theme
			if(prefs.getBoolean(DARK_THEME_PREF, DEFAULT_DARK_THEME))
				Util.setDarkLookAndFeel();
			else
				Util.setLightLookAndFeel();
			SwingUtilities.updateComponentTreeUI(dlgLoading);

			// Load the strings
			if(!Lang.loadPreferredLanguage()) {
				JOptionPane.showMessageDialog(null, "Error opening language file " + Lang.getLanguage() + "!", AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}

			// Translate the "loading" window
			dlgLoading.setLoadingText(Lang.t("loading"));
			dlgLoading.paint(dlgLoading.getGraphics());

			// Start the main window
			if(filename == null && prefs.getBoolean(OPEN_LAST_FILE_AT_STARTUP_PREF, DEFAULT_OPEN_LAST_FILE_AT_STARTUP)) {
				String f = prefs.get(LAST_FILE_PREF, null);
				if(f != null && new File(f).isFile())
					filename = f;
			}
			frmSim = (filename == null ? new FrmSimulator() : new FrmSimulator(filename));
			frmSim.setVisible(true);

			dlgLoading.dispose();
		}
	}

	private static class ExceptionHandler implements Thread.UncaughtExceptionHandler {
		@Override
		public void uncaughtException(Thread t, Throwable ex) {
			LOG.log(Level.SEVERE, "error starting simulator", ex);
			JOptionPane.showMessageDialog(null, "Fatal error!\n" + ex, AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
			if(frmSim == null || !frmSim.isVisible())
				System.exit(1);
		}
	}
}
