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

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import brunonova.drmips.android.dialogs.*;
import brunonova.drmips.simulator.AssembledInstruction;
import brunonova.drmips.simulator.CPU;
import brunonova.drmips.simulator.Data;
import brunonova.drmips.simulator.exceptions.*;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;

public class DrMIPSActivity extends Activity {
	/** The file currently open (if <tt>null</tt> no file is open). */
	private File openFile = null;
	/** The filter to select only .cpu files. */
	private CPUFileFilter cpuFileFilter = new CPUFileFilter();
	/** On click listener handler for the assembled code table rows. */
	private AssembledCodeRowOnClickListener assembledCodeRowOnClickListener = new AssembledCodeRowOnClickListener();
	/** On long click listener handler for the registers table rows. */
	private RegistersRowOnLongClickListener registersRowOnLongClickListener = new RegistersRowOnLongClickListener();
	/** On long click listener handler for the data memory table rows. */
	private DataMemoryRowOnLongClickListener dataMemoryRowOnLongClickListener = new DataMemoryRowOnLongClickListener();
	/** Listener that handles changes on the spinners. */
	private SpinnersListener spinnersListener = new SpinnersListener();
	/** The datapath being shown. */
	private Datapath datapath = null;
	
	private TabHost tabHost;
	private EditText txtCode;
	private TextView lblFilename, lblCPUFilename, lblDatapathFormat, lblDatapathPerformance;
	private MenuItem mnuDelete = null, mnuStep = null, mnuBackStep = null, mnuControlPath = null,
	                 mnuArrowsInWires = null, mnuPerformanceMode = null, mnuOverlayedData = null,
	                 mnuOverlayedShowNames = null, mnuOverlayedShowForAll = null, mnuRestart = null,
	                 mnuRun = null, mnuRestoreLatencies = null, mnuRemoveLatencies = null;
	private ImageButton cmdStep;
	private TableLayout tblAssembledCode, tblRegisters, tblDataMemory, tblExec;
	private Spinner cmbAssembledCodeFormat, cmbRegistersFormat, cmbDataMemoryFormat, cmbDatapathFormat, cmbDatapathPerformance;
	private HorizontalScrollView datapathScroll;
	private TableRow tblExecRow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(DrMIPS.getApplication().getCurrentTheme());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dr_mips);

		createTabs();
		
		// Open last or default CPU (but not when restoring from a screen rotation, etc.)
		if(savedInstanceState == null || getCPU() == null)
			loadFirstCPU();
		
		
		// Open last code file, if any
		String name = DrMIPS.getApplication().getPreferences().getString(DrMIPS.LAST_FILE_PREF, null);
		if(name != null) {
			File file = new File(DrMIPS.getApplication().getCodeDir() + File.separator + name);
			if(file.exists())
				openFile(file);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dr_mips, menu);
		
		mnuDelete = menu.findItem(R.id.mnuDelete);
		mnuStep = menu.findItem(R.id.mnuStep);
		mnuBackStep = menu.findItem(R.id.mnuBackStep);
		mnuRestart = menu.findItem(R.id.mnuRestart);
		mnuRun = menu.findItem(R.id.mnuRun);
		MenuItem mnuSwitchTheme = menu.findItem(R.id.mnuSwitchTheme);
		mnuSwitchTheme.setChecked(DrMIPS.getApplication().getCurrentTheme() == R.style.DarkTheme);
		mnuControlPath = menu.findItem(R.id.mnuControlPath);
		mnuControlPath.setChecked(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.SHOW_CONTROL_PATH_PREF, DrMIPS.DEFAULT_SHOW_CONTROL_PATH));
		mnuArrowsInWires = menu.findItem(R.id.mnuArrowsInWires);
		mnuArrowsInWires.setChecked(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.SHOW_ARROWS_PREF, DrMIPS.DEFAULT_SHOW_ARROWS));
		mnuPerformanceMode = menu.findItem(R.id.mnuPerformanceMode);
		mnuPerformanceMode.setChecked(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.PERFORMANCE_MODE_PREF, DrMIPS.DEFAULT_PERFORMANCE_MODE));
		mnuOverlayedData = menu.findItem(R.id.mnuOverlayedData);
		mnuOverlayedData.setChecked(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.OVERLAYED_DATA_PREF, DrMIPS.DEFAULT_OVERLAYED_DATA));
		mnuOverlayedShowNames = menu.findItem(R.id.mnuOverlayedShowNames);
		mnuOverlayedShowNames.setChecked(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.OVERLAYED_SHOW_NAMES_PREF, DrMIPS.DEFAULT_OVERLAYED_SHOW_NAMES));
		mnuOverlayedShowNames.setVisible(mnuOverlayedData.isChecked());
		mnuOverlayedShowForAll = menu.findItem(R.id.mnuOverlayedShowForAll);
		mnuOverlayedShowForAll.setChecked(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.OVERLAYED_SHOW_FOR_ALL_PREF, DrMIPS.DEFAULT_OVERLAYED_SHOW_FOR_ALL));
		mnuOverlayedShowForAll.setVisible(mnuOverlayedData.isChecked());
		mnuRemoveLatencies = menu.findItem(R.id.mnuRemoveLatencies);
		mnuRemoveLatencies.setVisible(mnuPerformanceMode.isChecked());
		mnuRestoreLatencies = menu.findItem(R.id.mnuRestoreLatencies);
		mnuRestoreLatencies.setVisible(mnuPerformanceMode.isChecked());
		
		mnuDelete.setVisible(openFile != null);
		updateStepBackEnabled();
		updateStepEnabled();
		
		return true;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(outState != null) {
			outState.putBoolean("step_enabled", cmdStep.getVisibility() == View.VISIBLE); // save simulation controls state
			outState.putInt("tab", tabHost.getCurrentTab()); // save current tab
		}
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onBackPressed() {
		DlgConfirmExit.newInstance().show(getFragmentManager(), "confirm-exit-dialog");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if(savedInstanceState != null && DrMIPS.getApplication().hasCPU()) {
			if(openFile != null) lblFilename.setText(openFile.getName());
			lblCPUFilename.setText(getCPU().getFile().getName());
			setSimulationControlsEnabled(savedInstanceState.getBoolean("step_enabled", false)); // restore simulation controls state
			tabHost.setCurrentTab(savedInstanceState.getInt("tab", 0)); // restore current tab
			
			refreshDatapath(); // recreate datapath
			datapath.setControlPathVisible(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.SHOW_CONTROL_PATH_PREF, DrMIPS.DEFAULT_SHOW_CONTROL_PATH));
			datapath.setShowArrows(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.SHOW_ARROWS_PREF, DrMIPS.DEFAULT_SHOW_ARROWS));
			datapath.setPerformanceMode(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.PERFORMANCE_MODE_PREF, DrMIPS.DEFAULT_PERFORMANCE_MODE));
			datapath.setShowTips(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.OVERLAYED_DATA_PREF, DrMIPS.DEFAULT_OVERLAYED_DATA));
			datapath.setShowTipsNames(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.OVERLAYED_SHOW_NAMES_PREF, DrMIPS.DEFAULT_OVERLAYED_SHOW_NAMES));
			datapath.setShowTipsForAllComps(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.OVERLAYED_SHOW_FOR_ALL_PREF, DrMIPS.DEFAULT_OVERLAYED_SHOW_FOR_ALL));
			refreshExecTable();
			refreshAssembledCodeTable();
			refreshRegistersTable();
			refreshDataMemoryTable();
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	public void mnuAboutOnClick(MenuItem menu) {
		DlgAbout.newInstance().show(getFragmentManager(), "about-dialog");
	}

	public void mnuNewOnClick(MenuItem menu) {
		newFile();
	}

	public void mnuOpenOnClick(MenuItem menu) {
		openFile();
	}

	public void mnuSaveOnClick(MenuItem menu) {
		saveFile();
	}

	public void mnuSaveAsOnClick(MenuItem menu) {
		saveFileAs();
	}
	
	public void mnuDeleteOnClick(MenuItem menu) {
		deleteFile();
	}
	
	public void mnuLoadCPUOnClick(MenuItem menu) {
		loadCPU();
	}
	
	public void mnuAssembleOnClick(MenuItem menu) {
		assemble();
	}
	
	public void mnuStepOnClick(MenuItem menu) {
		step();
	}
	
	public void mnuBackStepOnClick(MenuItem menu) {
		backStep();
	}
	
	public void mnuSwitchThemeOnClick(MenuItem menu) {
		int newTheme;
		if(DrMIPS.getApplication().getCurrentTheme() == R.style.LightTheme)
			newTheme = R.style.DarkTheme;
		else
			newTheme = R.style.LightTheme;
		
		// Save the new theme in the preferences
		SharedPreferences.Editor editor = DrMIPS.getApplication().getPreferences().edit();
		editor.putInt(DrMIPS.THEME_PREF, newTheme);
		editor.apply();
		
		// Restart the activity
		recreate();
	}

	public void cmdNewOnClick(View view) {
		newFile();
	}

	public void cmdOpenOnClick(View view) {
		openFile();
	}

	public void cmdSaveOnClick(View view) {
		saveFile();
	}
	
	public void cmdStepOnClick(View view) {
		step();
	}
	
	public void mnuControlPathOnClick(MenuItem menu) {
		mnuControlPath.setChecked(!mnuControlPath.isChecked());
		DrMIPS.getApplication().getPreferences().edit().putBoolean(DrMIPS.SHOW_CONTROL_PATH_PREF, mnuControlPath.isChecked()).apply();
		datapath.setControlPathVisible(mnuControlPath.isChecked());
	}
	
	public void mnuArrowsInWiresOnClick(MenuItem menu) {
		mnuArrowsInWires.setChecked(!mnuArrowsInWires.isChecked());
		DrMIPS.getApplication().getPreferences().edit().putBoolean(DrMIPS.SHOW_ARROWS_PREF, mnuArrowsInWires.isChecked()).apply();
		datapath.setShowArrows(mnuArrowsInWires.isChecked());
	}
	
	public void mnuPerformanceModeOnClick(MenuItem menu) {
		mnuPerformanceMode.setChecked(!mnuPerformanceMode.isChecked());
		DrMIPS.getApplication().getPreferences().edit().putBoolean(DrMIPS.PERFORMANCE_MODE_PREF, mnuPerformanceMode.isChecked()).apply();
		datapath.setPerformanceMode(mnuPerformanceMode.isChecked());
		lblDatapathFormat.setVisibility(mnuPerformanceMode.isChecked() ? View.GONE : View.VISIBLE);
		cmbDatapathFormat.setVisibility(mnuPerformanceMode.isChecked() ? View.GONE : View.VISIBLE);
		lblDatapathPerformance.setVisibility(!mnuPerformanceMode.isChecked() ? View.GONE : View.VISIBLE);
		cmbDatapathPerformance.setVisibility(!mnuPerformanceMode.isChecked() ? View.GONE : View.VISIBLE);
		if(mnuRemoveLatencies != null) mnuRemoveLatencies.setVisible(mnuPerformanceMode.isChecked());
		if(mnuRestoreLatencies != null) mnuRestoreLatencies.setVisible(mnuPerformanceMode.isChecked());
	}
	
	public void mnuOverlayedDataOnClick(MenuItem menu) {
		mnuOverlayedData.setChecked(!mnuOverlayedData.isChecked());
		mnuOverlayedShowNames.setVisible(mnuOverlayedData.isChecked());
		DrMIPS.getApplication().getPreferences().edit().putBoolean(DrMIPS.OVERLAYED_DATA_PREF, mnuOverlayedData.isChecked()).apply();
		datapath.setShowTips(mnuOverlayedData.isChecked());
	}

	public void mnuOverlayedShowNamesOnClick(MenuItem menu) {
		mnuOverlayedShowNames.setChecked(!mnuOverlayedShowNames.isChecked());
		DrMIPS.getApplication().getPreferences().edit().putBoolean(DrMIPS.OVERLAYED_SHOW_NAMES_PREF, mnuOverlayedShowNames.isChecked()).apply();
		datapath.setShowTipsNames(mnuOverlayedShowNames.isChecked());
	}

	public void mnuOverlayedShowForAllOnClick(MenuItem menu) {
		mnuOverlayedShowForAll.setChecked(!mnuOverlayedShowForAll.isChecked());
		DrMIPS.getApplication().getPreferences().edit().putBoolean(DrMIPS.OVERLAYED_SHOW_FOR_ALL_PREF, mnuOverlayedShowForAll.isChecked()).apply();
		datapath.setShowTipsForAllComps(mnuOverlayedShowForAll.isChecked());
	}

	public void mnuRestoreLatenciesOnClick(MenuItem menu) {
		getCPU().resetLatencies();
		datapath.refresh();
	}
	
	public void mnuRemoveLatenciesOnClick(MenuItem menu) {
		getCPU().removeLatencies();
		datapath.refresh();
	}
	
	public void mnuStatisticsOnClick(MenuItem menu) {
		DlgStatistics.newInstance().show(getFragmentManager(), "statistics-dialog");
	}
	
	public void mnuRestartOnClick(MenuItem menu) {
		restart();
	}
	
	public void mnuRunOnClick(MenuItem menu) {
		run();
	}
	
	public void lblFilenameOnClick(View view) {
		if(openFile != null)
			Toast.makeText(this, openFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
	}
	
	public void lblCPUFilenameOnClick(View view) {
		Toast.makeText(this, getCPU().getFile().getAbsolutePath(), Toast.LENGTH_LONG).show();
	}
	
	public void cmdCodeHelpOnClick(View view) {
		DlgCodeHelp.newInstance().show(getFragmentManager(), "code-help-dialog");
	}
	
	public void cmdDatapathHelpOnClick(View view) {
		DlgDatapathHelp.newInstance(getDatapath().isInPerformanceMode()).show(getFragmentManager(), "datapath-help-dialog");
	}

	/**
	 * Creates/places the tabs in the Tabhost.
	 */
	private void createTabs() {
		tabHost = (TabHost)findViewById(android.R.id.tabhost);
		tabHost.setup();

		TabSpec tabCode = tabHost.newTabSpec("tabCode");
		tabCode.setIndicator(getString(R.string.code));
		tabCode.setContent(R.id.tabCode);
		tabHost.addTab(tabCode);
		txtCode = (EditText)findViewById(R.id.txtCode);
		lblFilename = (TextView)findViewById(R.id.lblFilename);
		txtCode.addTextChangedListener(new CodeEditorTextWatcher());
		
		TabSpec tabAssembledCode = tabHost.newTabSpec("tabAssembledCode");
		tabAssembledCode.setIndicator(getString(R.string.assembled));
		tabAssembledCode.setContent(R.id.tabAssembledCode);
		tabHost.addTab(tabAssembledCode);
		tblAssembledCode = (TableLayout)findViewById(R.id.tblAssembledCode);
		cmbAssembledCodeFormat = (Spinner)findViewById(R.id.cmbAssembledCodeFormat);
		cmbAssembledCodeFormat.setOnItemSelectedListener(spinnersListener);
		cmbAssembledCodeFormat.setSelection(DrMIPS.getApplication().getPreferences().getInt(DrMIPS.ASSEMBLED_CODE_FORMAT_PREF, DrMIPS.DEFAULT_ASSEMBLED_CODE_FORMAT));
		
		TabSpec tabDatapath = tabHost.newTabSpec("tabDatapath");
		tabDatapath.setIndicator(getString(R.string.datapath));
		tabDatapath.setContent(R.id.tabDatapath);
		tabHost.addTab(tabDatapath);
		lblCPUFilename = (TextView)findViewById(R.id.lblCPUFilename);
		cmdStep = (ImageButton)findViewById(R.id.cmdStep);
		datapathScroll = (HorizontalScrollView)findViewById(R.id.datapathScroll);
		boolean performanceMode = DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.PERFORMANCE_MODE_PREF, DrMIPS.DEFAULT_PERFORMANCE_MODE);
		lblDatapathFormat = (TextView)findViewById(R.id.lblDatapathFormat);
		cmbDatapathFormat = (Spinner)findViewById(R.id.cmbDatapathFormat);
		cmbDatapathFormat.setOnItemSelectedListener(spinnersListener);
		cmbDatapathFormat.setSelection(DrMIPS.getApplication().getPreferences().getInt(DrMIPS.DATAPATH_DATA_FORMAT_PREF, DrMIPS.DEFAULT_DATAPATH_DATA_FORMAT));
		lblDatapathFormat.setVisibility(performanceMode ? View.GONE : View.VISIBLE);
		cmbDatapathFormat.setVisibility(performanceMode ? View.GONE : View.VISIBLE);
		lblDatapathPerformance = (TextView)findViewById(R.id.lblDatapathPerformance);
		cmbDatapathPerformance = (Spinner)findViewById(R.id.cmbDatapathPerformance);
		cmbDatapathPerformance.setOnItemSelectedListener(spinnersListener);
		cmbDatapathPerformance.setSelection(DrMIPS.getApplication().getPreferences().getInt(DrMIPS.PERFORMANCE_TYPE_PREF, DrMIPS.DEFAULT_PERFORMANCE_TYPE));
		lblDatapathPerformance.setVisibility(!performanceMode ? View.GONE : View.VISIBLE);
		cmbDatapathPerformance.setVisibility(!performanceMode ? View.GONE : View.VISIBLE);
		tblExec = (TableLayout)findViewById(R.id.tblExec);
		tblExecRow = (TableRow)findViewById(R.id.tblExecRow);
		
		TabSpec tabRegisters = tabHost.newTabSpec("tabRegisters");
		tabRegisters.setIndicator(getString(R.string.registers));
		tabRegisters.setContent(R.id.tabRegisters);
		tabHost.addTab(tabRegisters);
		tblRegisters = (TableLayout)findViewById(R.id.tblRegisters);
		cmbRegistersFormat = (Spinner)findViewById(R.id.cmbRegistersFormat);
		cmbRegistersFormat.setOnItemSelectedListener(spinnersListener);
		cmbRegistersFormat.setSelection(DrMIPS.getApplication().getPreferences().getInt(DrMIPS.REGISTER_FORMAT_PREF, DrMIPS.DEFAULT_REGISTER_FORMAT));
		
		TabSpec tabDataMemory = tabHost.newTabSpec("tabDataMemory");
		tabDataMemory.setIndicator(getString(R.string.data_memory));
		tabDataMemory.setContent(R.id.tabDataMemory);
		tabHost.addTab(tabDataMemory);
		tblDataMemory = (TableLayout)findViewById(R.id.tblDataMemory);
		cmbDataMemoryFormat = (Spinner)findViewById(R.id.cmbDataMemoryFormat);
		cmbDataMemoryFormat.setOnItemSelectedListener(spinnersListener);
		cmbDataMemoryFormat.setSelection(DrMIPS.getApplication().getPreferences().getInt(DrMIPS.DATA_MEMORY_FORMAT_PREF, DrMIPS.DEFAULT_DATA_MEMORY_FORMAT));
	}
	
	/**
	 * Returns the currently loaded CPU.
	 * @return Current CPU.
	 */
	public CPU getCPU() {
		return DrMIPS.getApplication().getCPU();
	}
	
	/**
	 * Returns the selected datapath data format.
	 * @return Datapath data format.
	 */
	public int getDatapathFormat() {
		return cmbDatapathFormat.getSelectedItemPosition();
	}

	/**
	 * Sets the path of the opened file and updates the title bar and recent files.
	 * @param file The opened file.
	 */
	private void setOpenedFile(File file) {
		openFile = file;
		SharedPreferences.Editor editor = DrMIPS.getApplication().getPreferences().edit();
		if(file != null) {
			lblFilename.setText(openFile.getName());
			if(mnuDelete != null) mnuDelete.setVisible(true);
			editor.putString(DrMIPS.LAST_FILE_PREF, file.getName());
		}
		else {
			lblFilename.setText("");
			if(mnuDelete != null) mnuDelete.setVisible(false);
			editor.remove(DrMIPS.LAST_FILE_PREF);
		}
		editor.apply();
	}

	/**
	 * Clears the code editor.
	 */
	public void newFile() {
		txtCode.setText("");
		setOpenedFile(null);
	}

	/**
	 * Shows the file chooser to open a file.
	 */
	private void openFile() {
		File[] files = DrMIPS.getApplication().getCodeDir().listFiles();
		if(files == null || files.length == 0)
			Toast.makeText(this, R.string.no_files_to_open, Toast.LENGTH_SHORT).show();
		else {
			String[] codeFiles = new String[files.length];
			for(int i = 0; i < files.length; i++)
				codeFiles[i] = files[i].getName();
			Arrays.sort(codeFiles);
			DlgOpen.newInstance(codeFiles).show(getFragmentManager(), "open-dialog");
		}
	}

	/**
	 * Saves the file to <tt>filename</tt> (if <tt>null</tt> asks the user for a file name).
	 */
	private void saveFile() {
		if(openFile != null)
			saveFile(openFile);
		else
			saveFileAs();
	}
	
	/**
	 * Shows the file chooser to save the code to a file.
	 */
	private void saveFileAs() {
		DlgSave.newInstance(openFile != null ? openFile.getName() : "").show(getFragmentManager(), "save-dialog");
	}
	
	/**
	 * Deletes the currently open file, after confirmation.
	 */
	private void deleteFile() {
		if(openFile != null) {
			DlgConfirmDelete.newInstance(openFile.getPath()).show(getFragmentManager(), "confirm-delete-dialog");
		}
	}
	
	/**
	 * Shows the file chooser to load a CPU.
	 */
	private void loadCPU() {
		File[] files = DrMIPS.getApplication().getCPUDir().listFiles(cpuFileFilter);
		if(files == null || files.length == 0)
			Toast.makeText(this, R.string.no_files_to_open, Toast.LENGTH_SHORT).show();
		else {
			String[] cpuFiles = new String[files.length];
			for(int i = 0; i < files.length; i++)
				cpuFiles[i] = files[i].getName();
			Arrays.sort(cpuFiles);
			DlgOpenCPU.newInstance(cpuFiles).show(getFragmentManager(), "open-cpu-dialog");
		}
	}
	
	/**
	 * Opens and loads the code from the given file.
	 * @param file The file to open.
	 */
	public void openFile(File file) {
		try {
			String code = "", line;
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			while((line = reader.readLine()) != null)
				code += line + "\n";
			reader.close();
			
			txtCode.setText(code);
			setOpenedFile(file);
			tabHost.setCurrentTabByTag("tabCode");
		}
		catch(Exception ex) {
			String msg = getString(R.string.error_opening_file).replace("#1", file.getName());
			msg += "\n" + ex.getMessage();
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			Log.e(getClass().getName(), "error opening file \"" + file.getName() + "\"", ex);
		}
	}
	
	/**
	 * Saves the code to the specified file.
	 * @param file File to save to.
	 */
	public void saveFile(File file) {
		try {
			BufferedWriter writer;
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
			writer.write(txtCode.getText().toString());
			writer.close();
			setOpenedFile(file);
			Toast.makeText(this, R.string.file_saved, Toast.LENGTH_SHORT).show();
		} catch (IOException ex) {
			String msg = getString(R.string.error_saving_file).replace("#1", file.getName());
			msg += "\n" + ex.getMessage();
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			Log.e(getClass().getName(), "error saving file \"" + file.getName() + "\"", ex);
		}
	}
	
	/**
	 * Loads the CPU from the specified file.
	 * @param file File to load the CPU from.
	 */
	public void loadCPU(File file) throws ArrayIndexOutOfBoundsException, NumberFormatException, IOException, JSONException, InvalidCPUException, InvalidInstructionSetException {
		setSimulationControlsEnabled(false);
		CPU cpu = CPU.createFromJSONFile(file.getAbsolutePath()); // load CPU from file
		cpu.setPerformanceInstructionDependent(cmbDatapathPerformance.getSelectedItemPosition() == Util.INSTRUCTION_PERFORMANCE_TYPE_INDEX);
		DrMIPS.getApplication().setCPU(cpu);
		
		refreshRegistersTable(); // display the CPU's register table
		refreshDatapath(); // display datapath in the respective tab
		refreshAssembledCodeTable(); // display assembled code in the respective tab
		refreshDataMemoryTable(); // display data memory in the respective tab
		refreshExecTable();
		datapath.setControlPathVisible(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.SHOW_CONTROL_PATH_PREF, DrMIPS.DEFAULT_SHOW_CONTROL_PATH));
		datapath.setShowArrows(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.SHOW_ARROWS_PREF, DrMIPS.DEFAULT_SHOW_ARROWS));
		datapath.setPerformanceMode(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.PERFORMANCE_MODE_PREF, DrMIPS.DEFAULT_PERFORMANCE_MODE));
		datapath.setShowTips(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.OVERLAYED_DATA_PREF, DrMIPS.DEFAULT_OVERLAYED_DATA));
		datapath.setShowTipsNames(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.OVERLAYED_SHOW_NAMES_PREF, DrMIPS.DEFAULT_OVERLAYED_SHOW_NAMES));
		datapath.setShowTipsForAllComps(DrMIPS.getApplication().getPreferences().getBoolean(DrMIPS.OVERLAYED_SHOW_FOR_ALL_PREF, DrMIPS.DEFAULT_OVERLAYED_SHOW_FOR_ALL));
		
		lblCPUFilename.setText(cpu.getFile().getName());
		SharedPreferences.Editor editor = DrMIPS.getApplication().getPreferences().edit();
		editor.putString(DrMIPS.LAST_CPU_PREF, file.getName());
		editor.apply();
	}
	
	/**
	 * Loads the last used CPU file or the default one.
	 */
	private void loadFirstCPU() {
		try { // try to load the CPU in the preferences
			loadCPU(new File(DrMIPS.getApplication().getCPUDir() + File.separator + DrMIPS.getApplication().getPreferences().getString(DrMIPS.LAST_CPU_PREF, DrMIPS.DEFAULT_CPU)));
		} catch (Throwable ex) {
			try { // fallback to the default CPU on error
				loadCPU(new File(DrMIPS.getApplication().getCPUDir() + File.separator + DrMIPS.DEFAULT_CPU));
			} catch (Throwable e) { // error on the default CPU too
				Toast.makeText(DrMIPSActivity.this, getString(R.string.invalid_file) + "\n" + ex.getClass().getName() + " (" + ex.getMessage() + ")", Toast.LENGTH_LONG).show();
				Log.e(getClass().getName(), "error loading CPU", e);
				finish();
			}
		}
	}

	/**
	 * Sets the current tab to the specified one.
	 * @param tag Tag of the desired tab.
	 */
	public void setCurrentTab(String tag) {
		tabHost.setCurrentTabByTag(tag);
	}
	
	/**
	 * Enables of disables the simulation controls.
	 * @param enabled Whether to enable or disable the controls.
	 */
	private void setSimulationControlsEnabled(boolean enabled) {
		if(!enabled) {
			if(mnuBackStep != null) mnuBackStep.setVisible(false);
			if(mnuRestart != null) mnuRestart.setVisible(false);
			if(mnuStep != null) mnuStep.setVisible(false);
			if(mnuRun != null) mnuRun.setVisible(false);
			cmdStep.setVisibility(View.GONE);
		}
		else {
			updateStepEnabled();
			updateStepBackEnabled();
		}
	}
	
	/**
	 * Sets the "step" controls enabled or disabled according to <tt>cpu.isProgramFinished()</tt>.
	 */
	private void updateStepEnabled() {
		boolean enable = !getCPU().isProgramFinished();
		cmdStep.setVisibility(enable ? View.VISIBLE : View.GONE);
		if(mnuStep != null) mnuStep.setVisible(enable);
		if(mnuRun != null) mnuRun.setVisible(enable);
	}
	
	/**
	 * Sets the "step back" controls enabled or disabled according to <tt>cpu.hasPreviousCycle()</tt>.
	 */
	private void updateStepBackEnabled() {
		boolean enable = getCPU().hasPreviousCycle();
		if(mnuBackStep != null) mnuBackStep.setVisible(enable);
		if(mnuRestart != null) mnuRestart.setVisible(enable);
	}
	
	/**
	 * Assembles and loads the code from the Code tab.
	 */
	private void assemble() {
		getCPU().resetData();
		try {
			getCPU().assembleCode(txtCode.getText().toString());
			
			if(datapath != null) datapath.refresh();
			setSimulationControlsEnabled(true);
			refreshAssembledCodeTable();
			refreshValues();
			tabHost.setCurrentTabByTag("tabAssembledCode");
		}
		catch(SyntaxErrorException ex) {
			String message = getString(R.string.line).replace("#1", "" + ex.getLine()) + ": ";
			switch(ex.getType()) {
				case DUPLICATED_LABEL: message += getString(R.string.duplicated_label).replace("#1", ex.getExtra()); break;
				case INVALID_DATA_ARG: message += getString(R.string.invalid_arg_data).replace("#1", ex.getExtra()); break;
				case INVALID_INT_ARG: message += getString(R.string.invalid_arg_int).replace("#1", ex.getExtra()); break;
				case INVALID_LABEL: message += getString(R.string.invalid_label).replace("#1", ex.getExtra()); break;
				case INVALID_REG_ARG: message += getString(R.string.invalid_arg_reg).replace("#1", ex.getExtra()); break;
				case UNKNOWN_DATA_DIRECTIVE: message += getString(R.string.unknown_data_directive).replace("#1", ex.getExtra()); break;
				case UNKNOWN_INSTRUCTION: message += getString(R.string.unknown_instruction).replace("#1", ex.getExtra()); break;
				case UNKNOWN_LABEL: message += getString(R.string.unknown_label).replace("#1", ex.getExtra()); break;
				case WRONG_NUMBER_OF_ARGUMENTS: message += getString(R.string.wrong_no_args).replace("#1", ex.getExtra()).replace("#2", ex.getExtra2()); break;
				case INVALID_POSITIVE_INT_ARG: message += getString(R.string.invalid_arg_positive_int).replace("#1", ex.getExtra()); break;
				case DATA_SEGMENT_WITHOUT_DATA_MEMORY: message += getString(R.string.data_segment_without_data_memory); break;
				default: message = ex.getMessage();
			}
			
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Updates the state of the simulation controls and the values displayed.
	 * Updates the enabled/disabled states of the simulation controls.
	 * It also refreshes the values displayed in the tables and the datapath,
	 * and scrolls the assembled code table to make the current instruction visible.
	 */
	private void refreshValues() {
		updateStepBackEnabled();
		updateStepEnabled();

		refreshRegistersTableValues();
		refreshDataMemoryTableValues();
		refreshAssembledCodeTableValues();
		refreshExecTableValues();
		if(datapath != null) datapath.refresh();

		// Scroll the assembled code table to the current instruction
		int index = getCPU().getPC().getCurrentInstructionIndex();
		if(index >= 0) {
			final ScrollView scroll = (ScrollView)findViewById(R.id.tblAssembledCodeScroll);
			final View row = tblAssembledCode.getChildAt(index + 1);

			// Scroll only if the row is out of view
			if(row != null && (row.getTop() < scroll.getScrollY() ||
			                   row.getBottom() > (scroll.getScrollY() + scroll.getHeight()))) {
				scroll.post(new Runnable() {
					@Override
					public void run() {
						if(row.getTop() < scroll.getScrollY()) {
							// Row is above the visible area
							// > scroll up until the row is visible at the top of the ScrollView
							scroll.smoothScrollTo(0, row.getTop());
						}
						else {
							// Row is below the visible area
							// > scroll down until the row is visible at the bottom of the ScrollView
							scroll.smoothScrollTo(0, row.getBottom() - scroll.getHeight());
						}
					}
				});
			}
		}
	}

	/**
	 * Executes a clock cycle in the CPU and displays the results in the GUI.
	 */
	private void step() {
		getCPU().executeCycle();
		refreshValues();
	}
	
	/**
	 * Reverts the execution to the previous clock cycle.
	 */
	private void backStep() {
		getCPU().restorePreviousCycle();
		refreshValues();
	}
	
	/**
	 * Reverts the execution to the first clock cycle.
	 */
	private void restart() {
		getCPU().resetToFirstCycle();
		refreshValues();
	}
	
	/**
	 * Executes all the instructions at once.
	 */
	private void run() {
		try {
			getCPU().executeAll();
		}
		catch(InfiniteLoopException e) {
			Toast.makeText(this, getString(R.string.possible_infinite_loop).replace("#1", "" + CPU.EXECUTE_ALL_LIMIT_CYCLES), Toast.LENGTH_SHORT).show();
		}
		refreshValues();
	}
	
	/**
	 * Refreshes the contents of the code table.
	 */
	private void refreshAssembledCodeTable() {
		while(tblAssembledCode.getChildCount() > 1) // remove all rows except header
			tblAssembledCode.removeViewAt(1);
		
		AssembledInstruction instruction;
		TableRow row;
		TextView address, assembled, code;
		String codeLine;
		CPU cpu = getCPU();
		for(int i = 0; i < cpu.getInstructionMemory().getNumberOfInstructions(); i++) {
			instruction = cpu.getInstructionMemory().getInstruction(i);
			row = new TableRow(this);
			row.setOnClickListener(assembledCodeRowOnClickListener);
			address = new TextView(this);
			address.setText(Util.formatDataAccordingToFormat(new Data(Data.DATA_SIZE, i * (Data.DATA_SIZE / 8)), cmbAssembledCodeFormat.getSelectedItemPosition()) + " ");
			address.setTextAppearance(this, android.R.style.TextAppearance_Medium);
			address.setTypeface(Typeface.MONOSPACE);
			assembled = new TextView(this);
			assembled.setText(Util.formatDataAccordingToFormat(new Data(Data.DATA_SIZE, instruction.getData().getValue()), cmbAssembledCodeFormat.getSelectedItemPosition()) + " ");
			assembled.setTextAppearance(this, android.R.style.TextAppearance_Medium);
			assembled.setTypeface(Typeface.MONOSPACE);
			code = new TextView(this);
			codeLine = instruction.getLineNumber() + ": ";
			for(String label: instruction.getLabels())
				codeLine += label + ": ";
			codeLine += instruction.getCodeLine();
			code.setText(codeLine);
			code.setTextAppearance(this, android.R.style.TextAppearance_Medium);
			code.setTypeface(Typeface.MONOSPACE);
			row.addView(address);
			row.addView(assembled);
			row.addView(code);
			tblAssembledCode.addView(row);
		}
		
		refreshAssembledCodeTableValues();
	}
	
	/**
	 * Refreshes the assembled code table highlights.
	 */
	private void refreshAssembledCodeTableValues() {
		TableRow row;
		CPU cpu = getCPU();
		
		for(int i = 0; i < cpu.getInstructionMemory().getNumberOfInstructions(); i++) {
			row = (TableRow)tblAssembledCode.getChildAt(i + 1);

			// Highlight instructions being executed
			if(i == cpu.getPC().getCurrentInstructionIndex())
				row.setBackgroundColor(Util.getThemeColor(this, getCPU().isPipeline() ? R.attr.ifColor : R.attr.instColor));
			else if(cpu.isPipeline()) {
				if(i == cpu.getIfIdReg().getCurrentInstructionIndex())
					row.setBackgroundColor(Util.getThemeColor(this, R.attr.idColor));
				else if(i == cpu.getIdExReg().getCurrentInstructionIndex())
					row.setBackgroundColor(Util.getThemeColor(this, R.attr.exColor));
				else if(i == cpu.getExMemReg().getCurrentInstructionIndex())
					row.setBackgroundColor(Util.getThemeColor(this, R.attr.memColor));
				else if(i == cpu.getMemWbReg().getCurrentInstructionIndex())
					row.setBackgroundColor(Util.getThemeColor(this, R.attr.wbColor));
				else
					row.setBackgroundResource(0); // remove background color
			}
			else
				row.setBackgroundResource(0); // remove background color
		}
		
		tblAssembledCode.requestLayout();
	}
	
	/**
	 * Refreshes both the rows and values of the registers table.
	 */
	private void refreshRegistersTable() {
		while(tblRegisters.getChildCount() > 1) // remove all rows except header
			tblRegisters.removeViewAt(1);
		
		// Add registers
		CPU cpu = getCPU();
		int numRegs = cpu.getRegBank().getNumberOfRegisters();
		TableRow row;
		TextView register, value;
		String reg;
		for(int i = 0; i < numRegs; i++) {
			row = new TableRow(this);
			row.setOnLongClickListener(registersRowOnLongClickListener);
			register = new TextView(this);
			reg = i + ": " + cpu.getRegisterName(i);
			if(i < 10) reg = " " + reg;
			register.setText(reg);
			register.setTextAppearance(this, android.R.style.TextAppearance_Medium);
			register.setTypeface(Typeface.MONOSPACE);
			value = new TextView(this);
			value.setTextAppearance(this, android.R.style.TextAppearance_Medium);
			value.setTypeface(Typeface.MONOSPACE);
			value.setGravity(Gravity.RIGHT);
			row.addView(register);
			row.addView(value);
			tblRegisters.addView(row);
		}
		
		// Add special "registers" (PC,...)
		row = new TableRow(this);
		row.setOnLongClickListener(registersRowOnLongClickListener);
		register = new TextView(this);
		register.setText("PC");
		register.setTextAppearance(this, android.R.style.TextAppearance_Medium);
		register.setTypeface(Typeface.MONOSPACE);
		value = new TextView(this);
		value.setTextAppearance(this, android.R.style.TextAppearance_Medium);
		value.setTypeface(Typeface.MONOSPACE);
		value.setGravity(Gravity.RIGHT);
		row.addView(register);
		row.addView(value);
		tblRegisters.addView(row);
		
		refreshRegistersTableValues(); // refresh values
	}
	
	/**
	 * Refreshes the values of the registers table.
	 */
	public void refreshRegistersTableValues() {
		CPU cpu = getCPU();
		int numRegs = cpu.getRegBank().getNumberOfRegisters();
		TextView value;
		TableRow row;

		int reg1 = cpu.getRegBank().getReadReg1().getValue();
		int reg2 = cpu.getRegBank().getReadReg2().getValue();
		int regW = cpu.getRegBank().getWriteReg().getValue();
		boolean write = cpu.getRegBank().getRegWrite().getValue() == 1;

		for(int i = 0; i < numRegs; i++) { // registers
			row = (TableRow)tblRegisters.getChildAt(i + 1);
			value = (TextView)row.getChildAt(1);
			value.setText(Util.formatDataAccordingToFormat(cpu.getRegBank().getRegister(i), cmbRegistersFormat.getSelectedItemPosition()));
			
			// Highlight registers being accessed
			if(write && i == regW && !cpu.getRegBank().isRegisterConstant(regW)) {
				if(i == reg1 || i == reg2)
					row.setBackgroundColor(Util.getThemeColor(this, R.attr.rwColor));
				else
					row.setBackgroundColor(Util.getThemeColor(this, R.attr.writeColor));
			}
			else if(i == reg1 || i == reg2)
				row.setBackgroundColor(Util.getThemeColor(this, R.attr.readColor));
			else
				row.setBackgroundResource(0); // remove background color
		}
		
		// Special "registers"
		value = (TextView)((TableRow)tblRegisters.getChildAt(numRegs + 1)).getChildAt(1);
		value.setText(Util.formatDataAccordingToFormat(cpu.getPC().getAddress(), cmbRegistersFormat.getSelectedItemPosition()));
		
		tblRegisters.requestLayout();
	}
	
	/**
	 * Refreshes both the rows and values of the data memory table.
	 */
	private void refreshDataMemoryTable() {
		while(tblDataMemory.getChildCount() > 1) // remove all rows except header
			tblDataMemory.removeViewAt(1);
		
		CPU cpu = getCPU();
		if(cpu.hasDataMemory()) {
			TableRow row;
			TextView address, value;
			for(int i = 0; i < cpu.getDataMemory().getMemorySize(); i++) {
				row = new TableRow(this);
				row.setOnLongClickListener(dataMemoryRowOnLongClickListener);
				address = new TextView(this);
				address.setTextAppearance(this, android.R.style.TextAppearance_Medium);
				address.setTypeface(Typeface.MONOSPACE);
				value = new TextView(this);
				value.setTextAppearance(this, android.R.style.TextAppearance_Medium);
				value.setTypeface(Typeface.MONOSPACE);
				value.setGravity(Gravity.RIGHT);
				row.addView(address);
				row.addView(value);
				tblDataMemory.addView(row);
			}
			
			refreshDataMemoryTableValues(); // refresh values
		}
	}
	
	/**
	 * Refreshes the values of the registers table.
	 */
	public void refreshDataMemoryTableValues() {
		CPU cpu = getCPU();
		if(cpu.hasDataMemory()) {
			TextView address, value;
			TableRow row;

			for(int i = 0; i < cpu.getDataMemory().getMemorySize(); i++) {
				row = (TableRow)tblDataMemory.getChildAt(i + 1);
				address = (TextView)row.getChildAt(0);
				value = (TextView)row.getChildAt(1);
				address.setText(Util.formatDataAccordingToFormat(new Data(Data.DATA_SIZE, i * (Data.DATA_SIZE / 8)), cmbDataMemoryFormat.getSelectedItemPosition()) + " ");
				value.setText(Util.formatDataAccordingToFormat(new Data(Data.DATA_SIZE, cpu.getDataMemory().getDataInIndex(i)), cmbDataMemoryFormat.getSelectedItemPosition()));
				
				// Highlight memory positions being accessed
				int index = cpu.getDataMemory().getAddress().getValue() / (Data.DATA_SIZE / 8);
				boolean read = cpu.getDataMemory().getMemRead().getValue() == 1;
				boolean write = cpu.getDataMemory().getMemWrite().getValue() == 1;

				if(write && i == index) {
					if(read)
						row.setBackgroundColor(Util.getThemeColor(this, R.attr.rwColor));
					else
						row.setBackgroundColor(Util.getThemeColor(this, R.attr.writeColor));
				}
				else if(read && i == index)
					row.setBackgroundColor(Util.getThemeColor(this, R.attr.readColor));
				else
					row.setBackgroundResource(0); // remove background color
			}
			tblDataMemory.requestLayout();
		}
	}
	
	/**
	 * Returns the name of the register in the indicated row.
	 * @param row Row of the register in the table.
	 * @return Register's name, or <tt>null</tt> if non-existant.
	 */
	private String getRegisterName(int row) {
		CPU cpu = getCPU();
		if(row == cpu.getRegBank().getNumberOfRegisters()) // PC
			return getString(getResources().getIdentifier(cpu.getPC().getNameKey(), "string", getPackageName()));
		else if(row >= 0 && row < cpu.getRegBank().getNumberOfRegisters()) // register
			return cpu.getRegisterName(row);
		else
			return null;
	}
	
	/**
	 * Returns the data of the register in the indicated row.
	 * @param row Row of the register in the table.
	 * @return Register's data, or <tt>null</tt> if non-existant.
	 */
	private Data getRegisterData(int row) {
		CPU cpu = getCPU();
		if(row == cpu.getRegBank().getNumberOfRegisters()) // PC
			return cpu.getPC().getAddress();
		else if(row >= 0 && row < cpu.getRegBank().getNumberOfRegisters()) // register
			return cpu.getRegBank().getRegister(row);
		else
			return null;
	}
	
	/**
	 * Updates the value of the register in the indicated row, if editable.
	 * @param row Row of the register in the table.
	 * @param value New value of the register.
	 */
	public void setRegisterValue(int row, int value) {
		CPU cpu = getCPU();
		if(isRegisterEditable(row)) {
			if(row == cpu.getRegBank().getNumberOfRegisters()) { // PC
				if(value % (Data.DATA_SIZE / 8) == 0) {
					cpu.setPCAddress(value);
					refreshAssembledCodeTable();
					refreshDataMemoryTable();
				}
				else
					Toast.makeText(DrMIPSActivity.this, R.string.invalid_value, Toast.LENGTH_SHORT).show();
			}
			else if(row >= 0 && row < cpu.getRegBank().getNumberOfRegisters()) // register
				cpu.getRegBank().setRegister(row, value);
			
			if(datapath != null) datapath.refresh(); // update datapath
			refreshExecTableValues();
		}
	}
	
	/**
	 * Returns whether register in the indicated row is editable.
	 * @param row Row of the register in the table.
	 * @return <tt>True</tt> if the register is editable.
	 */
	private boolean isRegisterEditable(int row) {
		CPU cpu = getCPU();
		if(row == cpu.getRegBank().getNumberOfRegisters()) // PC
			return true;
		else if(row >= 0 && row < cpu.getRegBank().getNumberOfRegisters()) // register
			return !cpu.getRegBank().isRegisterConstant(row);
		else
			return false;
	}
	
	/**
	 * Recreates the datapath.
	 */
	private void refreshDatapath() {
		datapathScroll.removeAllViews();
		datapathScroll.addView(datapath = new Datapath(this));
	}
	
	/**
	 * Returns the datapath.
	 * @return The datapath.
	 */
	public Datapath getDatapath() {
		return datapath;
	}
	
	/**
	 * Recreates the exec table.
	 */
	private void refreshExecTable() {
		tblExecRow.removeAllViews();
		TextView lbl;
		
		if(getCPU().isPipeline()) {
			for(int i = 0; i < 5; i++) {
				lbl = new TextView(this);
				lbl.setGravity(Gravity.CENTER);
				lbl.setMaxLines(1);
				lbl.setWidth(0);
				lbl.setEllipsize(TruncateAt.END);
				switch (i) {
					case 0: lbl.setBackgroundColor(Util.getThemeColor(this, R.attr.ifColor)); break;
					case 1: lbl.setBackgroundColor(Util.getThemeColor(this, R.attr.idColor)); break;
					case 2: lbl.setBackgroundColor(Util.getThemeColor(this, R.attr.exColor)); break;
					case 3: lbl.setBackgroundColor(Util.getThemeColor(this, R.attr.memColor)); break;
					case 4: lbl.setBackgroundColor(Util.getThemeColor(this, R.attr.wbColor)); break;
				}
				lbl.setOnClickListener(new ExecTableOnClickListener());
				tblExecRow.addView(lbl);
			}
			tblExec.setStretchAllColumns(true);
		}
		else {
			lbl = new TextView(this);
			lbl.setGravity(Gravity.CENTER);
			lbl.setBackgroundColor(Util.getThemeColor(this, R.attr.instColor));
			lbl.setWidth(0);
			lbl.setMaxLines(1);
			lbl.setEllipsize(TruncateAt.END);
			lbl.setOnClickListener(new ExecTableOnClickListener());
			tblExecRow.addView(lbl);
			tblExec.setStretchAllColumns(true);
		}
		
		refreshExecTableValues();
	}
	
	/**
	 * Refreshes the values of the exec table.
	 */
	private void refreshExecTableValues() {
		CPU cpu = getCPU();
		((TextView)tblExecRow.getChildAt(0)).setText(getInstructionInIndex(cpu.getPC().getCurrentInstructionIndex()));
		if(cpu.isPipeline()) {
			((TextView)tblExecRow.getChildAt(1)).setText(getInstructionInIndex(cpu.getIfIdReg().getCurrentInstructionIndex()));
			((TextView)tblExecRow.getChildAt(2)).setText(getInstructionInIndex(cpu.getIdExReg().getCurrentInstructionIndex()));
			((TextView)tblExecRow.getChildAt(3)).setText(getInstructionInIndex(cpu.getExMemReg().getCurrentInstructionIndex()));
			((TextView)tblExecRow.getChildAt(4)).setText(getInstructionInIndex(cpu.getMemWbReg().getCurrentInstructionIndex()));
		}
	}
	
	/**
	 * Returns the code line of the instruction in the specified index.
	 * @param index Index of the instruction.
	 * @return Code line of the instruction, or an empty string if it doesn't exist.
	 */
	private String getInstructionInIndex(int index) {
		AssembledInstruction i = getCPU().getInstructionMemory().getInstruction(index);
		return (i != null) ? i.getCodeLine() : "";
	}
	
	private class CPUFileFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String filename) {
			return filename.endsWith(".cpu");
		}
	}
	
	private class AssembledCodeRowOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			int index = tblAssembledCode.indexOfChild(v) - 1;
			if(index >= 0 && index < getCPU().getInstructionMemory().getNumberOfInstructions()) {
				AssembledInstruction instruction = getCPU().getInstructionMemory().getInstruction(index);
				String msg = getString(R.string.type_x).replace("#1", instruction.getInstruction().getType().getId());
				msg += ": " + instruction.getInstruction().getMnemonic() + " (";
				switch(cmbAssembledCodeFormat.getSelectedItemPosition()) {
					case Util.BINARY_FORMAT_INDEX: msg += instruction.toBinaryString() + ")"; break;
					case Util.HEXADECIMAL_FORMAT_INDEX: msg += instruction.toHexadecimalString() + ")"; break;
					default: msg += instruction.toString() + ")"; break;
				}
				Toast.makeText(DrMIPSActivity.this, msg, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private class RegistersRowOnLongClickListener implements OnLongClickListener {
		@Override
		public boolean onLongClick(View v) {
			int index = tblRegisters.indexOfChild(v) - 1;
			if(index >= 0 && index <= getCPU().getRegBank().getNumberOfRegisters()) {
				String name = getRegisterName(index);
				if(isRegisterEditable(index)) {
					Data data = getRegisterData(index);
					int value = data != null ? data.getValue() : 0;
					DlgEditRegister.newInstance(index, name, value).show(getFragmentManager(), "edit-register-dialog");
				}
				else
					Toast.makeText(DrMIPSActivity.this, getString(R.string.register_not_editable).replace("#1", name), Toast.LENGTH_SHORT).show();
			}
			return true;
		}
	}
	
	private class DataMemoryRowOnLongClickListener implements OnLongClickListener {
		@Override
		public boolean onLongClick(View v) {
			int index = tblDataMemory.indexOfChild(v) - 1;
			if(index >= 0 && index < getCPU().getDataMemory().getMemorySize()) {
				int value = getCPU().getDataMemory().getDataInIndex(index);
				DlgEditDataMemory.newInstance(index, value).show(getFragmentManager(), "edit-data-memory-dialog");
			}
			return true;
		}
	}
	
	private class SpinnersListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			SharedPreferences.Editor editor = DrMIPS.getApplication().getPreferences().edit();
			if(parent == cmbAssembledCodeFormat) { 
				refreshAssembledCodeTable();
				editor.putInt(DrMIPS.ASSEMBLED_CODE_FORMAT_PREF, cmbAssembledCodeFormat.getSelectedItemPosition());
				editor.apply();
			}
			else if(parent == cmbRegistersFormat) {
				refreshRegistersTableValues();
				editor.putInt(DrMIPS.REGISTER_FORMAT_PREF, cmbRegistersFormat.getSelectedItemPosition());
				editor.apply();
			}
			else if(parent == cmbDataMemoryFormat) {
				refreshDataMemoryTableValues();
				editor.putInt(DrMIPS.DATA_MEMORY_FORMAT_PREF, cmbDataMemoryFormat.getSelectedItemPosition());
				editor.apply();
			}
			else if(parent == cmbDatapathFormat) {
				if(datapath != null) datapath.refresh();
				editor.putInt(DrMIPS.DATAPATH_DATA_FORMAT_PREF, cmbDatapathFormat.getSelectedItemPosition());
				editor.apply();
			}
			else if(parent == cmbDatapathPerformance) {
				getCPU().setPerformanceInstructionDependent(cmbDatapathPerformance.getSelectedItemPosition() == Util.INSTRUCTION_PERFORMANCE_TYPE_INDEX);
				if(datapath != null) datapath.refresh();
				editor.putInt(DrMIPS.PERFORMANCE_TYPE_PREF, cmbDatapathPerformance.getSelectedItemPosition());
				editor.apply();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) { }
	}
	
	private class ExecTableOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			int index = tblExecRow.indexOfChild(v);
			AssembledInstruction i = null;
			CPU cpu = getCPU();
			
			switch(index) {
				case 0: i = cpu.getInstructionMemory().getInstruction(cpu.getPC().getCurrentInstructionIndex()); break;
				case 1: if(cpu.isPipeline()) i = cpu.getInstructionMemory().getInstruction(cpu.getIfIdReg().getCurrentInstructionIndex()); break;
				case 2: if(cpu.isPipeline()) i = cpu.getInstructionMemory().getInstruction(cpu.getIdExReg().getCurrentInstructionIndex()); break;
				case 3: if(cpu.isPipeline()) i = cpu.getInstructionMemory().getInstruction(cpu.getExMemReg().getCurrentInstructionIndex()); break;
				case 4: if(cpu.isPipeline()) i = cpu.getInstructionMemory().getInstruction(cpu.getMemWbReg().getCurrentInstructionIndex()); break;
			}
			
			if(i != null) {
				String msg = getString(R.string.type_x).replace("#1", i.getInstruction().getType().getId());
				msg += ": " + i.getInstruction().getMnemonic() + " (";
				switch(cmbDatapathFormat.getSelectedItemPosition()) {
					case Util.BINARY_FORMAT_INDEX: msg += i.toBinaryString() + ")"; break;
					case Util.HEXADECIMAL_FORMAT_INDEX: msg += i.toHexadecimalString() + ")"; break;
					default: msg += i.toString() + ")"; break;
				}
				Toast.makeText(DrMIPSActivity.this, msg, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	/**
	 * Watcher that disables the simulation controls when the code is changed.
	 */
	private class CodeEditorTextWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			setSimulationControlsEnabled(false);
		}
	}
}
