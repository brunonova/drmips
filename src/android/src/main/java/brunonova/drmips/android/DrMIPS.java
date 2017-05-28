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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

import brunonova.drmips.simulator.CPU;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

/**
 * Global object that represents the application.
 * <p>It also contains some global constants and parameters.</p>
 * 
 * @author Bruno Nova
 */
public class DrMIPS extends Application {
	/** The key of the preference with the version of the last app launch. */
	public static final String LAST_VERSION_PREF = "last_version";
	/** The key of the preference with the name of the last code file opened. */
	public static final String LAST_FILE_PREF = "last_file";
	/** The key of the preference with the name of the last CPU opened. */
	public static final String LAST_CPU_PREF = "last_cpu";
	/** The name of the default CPU to load at startup. */
	public static final String DEFAULT_CPU = "unicycle.cpu";
	/** The key of the registers display format preference. */
	public static final String REGISTER_FORMAT_PREF = "reg_format";
	/** The key of the datapath display data format preference. */
	public static final String DATAPATH_DATA_FORMAT_PREF = "datapath_format";
	/** The key of the assembled code display data format preference. */
	public static final String ASSEMBLED_CODE_FORMAT_PREF = "assembled_code_format";
	/** The key of the data memory display data format preference. */
	public static final String DATA_MEMORY_FORMAT_PREF = "data_memory_format";
	/** The default performance mode type. */
	public static final int DEFAULT_PERFORMANCE_TYPE = Util.CPU_PERFORMANCE_TYPE_INDEX;
	/** The key of the show control path preference. */
	public static final String THEME_PREF = "theme";
	/** The key of the show arrows preference. */
	public static final String SHOW_CONTROL_PATH_PREF = "show_control_path";
	/** The key of the theme preference. */
	public static final String SHOW_ARROWS_PREF = "show_arrows";
	/** The key of the performance mode preference. */
	public static final String PERFORMANCE_MODE_PREF = "performance_mode";
	/** The key of the performance mode type preference. */
	public static final String PERFORMANCE_TYPE_PREF = "performance_type";
	/** The key of the overlayed data preference. */
	public static final String OVERLAYED_DATA_PREF = "overlayed_data";
	/** The key of the overlayed show names preference. */
	public static final String OVERLAYED_SHOW_NAMES_PREF = "overlayed_show_names";
	/** The key of the overlayed show for all components preference. */
	public static final String OVERLAYED_SHOW_FOR_ALL_PREF = "overlayed_show_for_all";
	/** The default format with which the registers are displayed. */
	public static final int DEFAULT_REGISTER_FORMAT = Util.DECIMAL_FORMAT_INDEX;
	/** The default format with which the datapath data is displayed. */
	public static final int DEFAULT_DATAPATH_DATA_FORMAT = Util.DECIMAL_FORMAT_INDEX;
	/** The default format with which the assembled code data is displayed. */
	public static final int DEFAULT_ASSEMBLED_CODE_FORMAT = Util.DECIMAL_FORMAT_INDEX;
	/** The default format with which the data memory values are displayed. */
	public static final int DEFAULT_DATA_MEMORY_FORMAT = Util.DECIMAL_FORMAT_INDEX;
	/** Whether the control path is shown by default. */
	public static final boolean DEFAULT_SHOW_CONTROL_PATH = true;
	/** Whether arrows are shown on the wires by default. */
	public static final boolean DEFAULT_SHOW_ARROWS = true;
	/** Whether the performance mode is enabled by default. */
	public static final boolean DEFAULT_PERFORMANCE_MODE = false;
	/** Whether the in/out tips are shown by default. */
	public static final boolean DEFAULT_OVERLAYED_DATA = true;
	/** Whether the names in the in/out tips are shown by default. */
	public static final boolean DEFAULT_OVERLAYED_SHOW_NAMES = false;
	/** Whether the in/out tips should be displayed for (almost) all components by default. */
	public static final boolean DEFAULT_OVERLAYED_SHOW_FOR_ALL = false;
	
	/** The current application. */
	private static DrMIPS app = null;
	/** The context of the application. */
	private Context context = null;
	/** File representation that points to the app's files directory (preferably on the external memory). */
	private File filesDir = null;
	/** File representation that points to the CPU directory. */
	private File cpuDir = null;
	/** File representation that points to the user created assembly code files directory. */
	private File codeDir = null;
	/** The currently loaded CPU. */
	private CPU cpu = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		app = this;
		context = getApplicationContext();

		createDefaultFiles();
	}
	
	/**
	 * Returns a reference to the application.
	 * @return The application.
	 */
	public static DrMIPS getApplication() {
		return app;
	}
	
	/**
	 * Returns the default theme of the application
	 * <p>For Android 3.0 and higher the default theme is LightTheme.<br />
	 * For versions below 3.0 the default theme is DarkTheme.</p>
	 * @return Application's default theme.
	 */
	public static int getDefaultTheme() {
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) ? R.style.LightTheme : R.style.DarkTheme;
	}
	
	/**
	 * Returns the currently loaded CPU.
	 * @return The currently loaded CPU.
	 */
	public CPU getCPU() {
		return cpu;
	}
	
	/**
	 * Updates the currently loaded CPU.
	 * @param cpu New CPU.
	 */
	public void setCPU(CPU cpu) {
		this.cpu = cpu;
	}
	
	/**
	 * Returns whether there is a CPU loaded.
	 * @return <tt>True</tt> if the is a CPU loaded.
	 */
	public boolean hasCPU() {
		return cpu != null;
	}
	
	/**
	 * Returns the context of the application.
	 * @return Context of the application.
	 */
	public Context getContext() {
		return context;
	}
	
	/**
	 * Returns the ID of the current theme (from the preferences).
	 * @return ID of the current theme.
	 */
	public int getCurrentTheme() {
		return DrMIPS.getApplication().getPreferences().getInt(DrMIPS.THEME_PREF, getDefaultTheme());
	}
	
	/**
	 * Converts the given size in dips to pixels.
	 * @param dip Size in dips.
	 * @return Size in pixels
	 */
	public int dipToPx(float dip) {
		// getResources().getDisplayMetrics().density
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics());
	}
	
	/**
	 * Returns the file representation that points to the app's files directory (preferably on the external memory).
	 * @return File representation that points to the app's files directory (preferably on the external memory).
	 */
	public File getFilesDir() {
		return filesDir;
	}
	
	/**
	 * Returns the file representation that points to the CPU directory.
	 * @return File representation that points to the CPU directory.
	 */
	public File getCPUDir() {
		return cpuDir;
	}
	
	/**
	 * Returns the file representation that points to the user created assembly code files directory.
	 * @return File representation that points to the user created assembly code files directory.
	 */
	public File getCodeDir() {
		return codeDir;
	}

	/**
	 * Returns the application's preferences.
	 * @return App's preferences.
	 */
	public SharedPreferences getPreferences() {
		return getSharedPreferences("prefs", MODE_PRIVATE);
	}
	
	/**
	 * Creates the default .cpu and .set files on the sdcard if they don't exist yet.
	 * <p>Also sets the path to the application files directory.</p>
	 */
	private void createDefaultFiles() { 
		// Find if the (preferred) external memory is available
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) // external memory available?
			filesDir = getExternalFilesDir(null);
		else { // external memory not available! (use internal memory)
			filesDir = context.getFilesDir(); // Apparently can return null, at least in the emulator
			Toast.makeText(getContext(), R.string.sdcard_not_available, Toast.LENGTH_SHORT).show();
		}
		if(filesDir == null)
			filesDir = context.getCacheDir(); // if it's null for some reason, use the cache directory

		// Create internal directories
		cpuDir = new File(filesDir.getAbsolutePath() + File.separator + "cpu");
		codeDir = new File(filesDir.getAbsolutePath() + File.separator + "code");
		if(!cpuDir.exists() && !cpuDir.mkdir()) Log.e(getClass().getName(), "Failed to create CPUs directory!");
		if(!codeDir.exists() && !codeDir.mkdir()) Log.e(getClass().getName(), "Failed to create code directory!");

		// Find application's version
		int versionCode = 0;
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionCode = info.versionCode;
		} catch (NameNotFoundException e) {
			Log.e(getClass().getName(), "Failed to get app version!", e);
		}

		// Find if the app was upgraded (because the default files could have been upgraded too)
		boolean upgraded = versionCode > getPreferences().getInt(LAST_VERSION_PREF, -1);
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putInt(LAST_VERSION_PREF, versionCode); // save new "last version"
		editor.apply();
		
		// Copy default CPU files to the memory
		File unicycleCPU = new File(cpuDir.getAbsoluteFile() + File.separator + "unicycle.cpu");
		File unicycleNoJumpCPU = new File(cpuDir.getAbsoluteFile() + File.separator + "unicycle-no-jump.cpu");
		File unicycleNoJumpBranchCPU = new File(cpuDir.getAbsoluteFile() + File.separator + "unicycle-no-jump-branch.cpu");
		File unicycleExtendedCPU = new File(cpuDir.getAbsoluteFile() + File.separator + "unicycle-extended.cpu");
		File pipelineCPU = new File(cpuDir.getAbsoluteFile() + File.separator + "pipeline.cpu");
		File pipelineNoHazardDetectionCPU = new File(cpuDir.getAbsoluteFile() + File.separator + "pipeline-no-hazard-detection.cpu");
		File pipelineOnlyForwardingCPU = new File(cpuDir.getAbsoluteFile() + File.separator + "pipeline-only-forwarding.cpu");
		File pipelineExtendedCPU = new File(cpuDir.getAbsoluteFile() + File.separator + "pipeline-extended.cpu");
		File defaultSet = new File(cpuDir.getAbsoluteFile() + File.separator + "default.set");
		File defaultNoJumpSet = new File(cpuDir.getAbsoluteFile() + File.separator + "default-no-jump.set");
		File defaultNoJumpBranchSet = new File(cpuDir.getAbsoluteFile() + File.separator + "default-no-jump-branch.set");
		File defaultExtendedSet = new File(cpuDir.getAbsoluteFile() + File.separator + "default-extended.set");
		File defaultExtendedNoJumpSet = new File(cpuDir.getAbsoluteFile() + File.separator + "default-extended-no-jump.set");
		if(upgraded || !unicycleCPU.exists()) copyResourceFile(R.raw.unicycle_cpu, unicycleCPU);
		if(upgraded || !unicycleNoJumpCPU.exists()) copyResourceFile(R.raw.unicycle_no_jump_cpu, unicycleNoJumpCPU);
		if(upgraded || !unicycleNoJumpBranchCPU.exists()) copyResourceFile(R.raw.unicycle_no_jump_branch_cpu, unicycleNoJumpBranchCPU);
		if(upgraded || !unicycleExtendedCPU.exists()) copyResourceFile(R.raw.unicycle_extended_cpu, unicycleExtendedCPU);
		if(upgraded || !pipelineCPU.exists()) copyResourceFile(R.raw.pipeline_cpu, pipelineCPU);
		if(upgraded || !pipelineOnlyForwardingCPU.exists()) copyResourceFile(R.raw.pipeline_only_forwarding_cpu, pipelineOnlyForwardingCPU);
		if(upgraded || !pipelineNoHazardDetectionCPU.exists()) copyResourceFile(R.raw.pipeline_no_hazard_detection_cpu, pipelineNoHazardDetectionCPU);
		if(upgraded || !pipelineExtendedCPU.exists()) copyResourceFile(R.raw.pipeline_extended_cpu, pipelineExtendedCPU);
		if(upgraded || !defaultSet.exists()) copyResourceFile(R.raw.default_set, defaultSet);
		if(upgraded || !defaultNoJumpSet.exists()) copyResourceFile(R.raw.default_no_jump_set, defaultNoJumpSet);
		if(upgraded || !defaultNoJumpBranchSet.exists()) copyResourceFile(R.raw.default_no_jump_branch_set, defaultNoJumpBranchSet);
		if(upgraded || !defaultExtendedSet.exists()) copyResourceFile(R.raw.default_extended_set, defaultExtendedSet);
		if(upgraded || !defaultExtendedNoJumpSet.exists()) copyResourceFile(R.raw.default_extended_no_jump_set, defaultExtendedNoJumpSet);
	}
	
	/**
	 * Copies the raw file with the given id to the specified path.
	 * @param resource The resource identifier of the file to copy.
	 * @param dest The File representation with the full path to the destination (including the file name).
	 */
	private void copyResourceFile(int resource, File dest) {
		BufferedReader in = null;
		BufferedWriter out = null;
		String line;
		
		try {
			in = new BufferedReader(new InputStreamReader(getContext().getResources().openRawResource(resource)));
			out = new BufferedWriter(new FileWriter(dest));
			while((line = in.readLine()) != null)
				out.write(line + "\n");
		}
		catch(Exception e) {
			Log.e(getClass().getName(), "Failed to copy default file to " + dest.getAbsolutePath() + "!", e);
		}
		finally {
			try {
				if(in != null) in.close();
				if(out != null) out.close();
			} catch(Exception ignored) { }
		}
	}
}
