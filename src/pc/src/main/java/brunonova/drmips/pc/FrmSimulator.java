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
import brunonova.drmips.simulator.CPU;
import brunonova.drmips.simulator.exceptions.*;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.jscroll.widgets.JScrollInternalFrame;
import org.json.JSONException;

/**
 * Main simulator window.
 *
 * @author Bruno Nova
 */
public class FrmSimulator extends javax.swing.JFrame {
	/** The currently loaded CPU. */
	public CPU cpu = null;
	/** The file chooser to choose a CPU file. */
	private JFileChooser cpuFileChooser = null;
	/** The file chooser to open/save a code file. */
	private JFileChooser codeFileChooser = null;
	/** The file filter of the open/save file chooser. */
	private FileNameExtensionFilter codeFileFilter = null;
	/** The file currently open (if <tt>null</tt> no file is open). */
	private File openFile = null;
	/** The window icon (in different sizes). */
	private List<Image> icons = null;
	/** The code editor component. */
	private CodeEditor txtCode = null;
	/** The find/replace dialog. */
	private DlgFindReplace dlgFindReplace = null;
	/** The supported instructions dialog. */
	private DlgSupportedInstructions dlgSupportedInstructions = null;
	/** The statistics dialog. */
	private DlgStatistics dlgStatistics = null; // statistics refreshed in DatapathPanel.refresh()
	/** The selected tab when it was right-clicked. */
	private Tab selectedTab = null;

	/** Information of the code tab. */
	private Tab tabCode;
	/** Information of the datapath tab. */
	private Tab tabDatapath;
	/** Information of the registers tab. */
	private Tab tabRegisters;
	/** Information of the assembled code tab. */
	private Tab tabAssembledCode;
	/** Information of the data memory tab. */
	private Tab tabDataMemory;

	/** Internal frame for the code editor. */
	private JScrollInternalFrame frmCode = null;
	/** Internal frame for the assembled code table. */
	private JScrollInternalFrame frmAssembledCode = null;
	/** Internal frame for the datapath. */
	private JScrollInternalFrame frmDatapath = null;
	/** Internal frame for the registers table. */
	private JScrollInternalFrame frmRegisters = null;
	/** Internal frame for the data memory table. */
	private JScrollInternalFrame frmDataMemory = null;

	/** Class logger. */
	private static final Logger LOG = Logger.getLogger(FrmSimulator.class.getName());

	/**
	 * Creates new form FrmSimulator.
	 */
	public FrmSimulator() {
		obtainIcons();
		initComponents();
		setSize(DrMIPS.prefs.getInt(DrMIPS.WIDTH_PREF, DrMIPS.DEFAULT_WIDTH),
		        DrMIPS.prefs.getInt(DrMIPS.HEIGHT_PREF, DrMIPS.DEFAULT_HEIGHT));
		if(DrMIPS.prefs.getBoolean("maximized", DrMIPS.DEFAULT_MAXIMIZED))
			setExtendedState(MAXIMIZED_BOTH);
		datapath.setParent(this);
		if(DrMIPS.prefs.getInt(DrMIPS.DIVIDER_LOCATION_PREF, -1) != -1)
			pnlSplit.setDividerLocation(DrMIPS.prefs.getInt(DrMIPS.DIVIDER_LOCATION_PREF, -1));
		pnlCode.add((txtCode = new CodeEditor(mnuEditP)).getScrollPane());
		dlgFindReplace = new DlgFindReplace(this);
		dlgSupportedInstructions = new DlgSupportedInstructions(this);
		dlgStatistics = new DlgStatistics(this);
		refreshTabSides();
		updateRecentFiles();
		loadFirstCPU();
		translate();
		fillLanguages();
		txtCode.requestFocus();
		txtCode.getDocument().addDocumentListener(new CodeEditorDocumentListener());
		txtCode.addCaretListener(new CodeEditorCaretListener());
		desktop.registerDefaultFrameIcon(new ImageIcon(getClass().getResource("/res/icons/x16/drmips.png")));

		mnuOpenLastFileAtStartup.setSelected(DrMIPS.prefs.getBoolean(DrMIPS.OPEN_LAST_FILE_AT_STARTUP_PREF, DrMIPS.DEFAULT_OPEN_LAST_FILE_AT_STARTUP));
		mnuResetDataBeforeAssembling.setSelected(DrMIPS.prefs.getBoolean(DrMIPS.ASSEMBLE_RESET_PREF, DrMIPS.DEFAULT_ASSEMBLE_RESET));
		mnuSwitchTheme.setSelected(DrMIPS.prefs.getBoolean(DrMIPS.DARK_THEME_PREF, DrMIPS.DEFAULT_DARK_THEME));
		mnuInternalWindows.setSelected(DrMIPS.prefs.getBoolean(DrMIPS.INTERNAL_WINDOWS_PREF, DrMIPS.DEFAULT_INTERNAL_WINDOWS));
		if(mnuInternalWindows.isSelected()) switchToInternalWindows();
		mnuMarginLine.setSelected(DrMIPS.prefs.getBoolean(DrMIPS.MARGIN_LINE_PREF, DrMIPS.DEFAULT_MARGIN_LINE));
		mnuOpenGL.setSelected(DrMIPS.prefs.getBoolean(DrMIPS.OPENGL_PREF, DrMIPS.DEFAULT_OPENGL));
		txtCode.setMarginLineEnabled(mnuMarginLine.isSelected());
		mnuControlPath.setSelected(DrMIPS.prefs.getBoolean(DrMIPS.SHOW_CONTROL_PATH_PREF, DrMIPS.DEFAULT_SHOW_CONTROL_PATH));
		datapath.setControlPathVisible(mnuControlPath.isSelected());
		mnuArrowsInWires.setSelected(DrMIPS.prefs.getBoolean(DrMIPS.SHOW_ARROWS_PREF, DrMIPS.DEFAULT_SHOW_ARROWS));
		datapath.setShowArrows(mnuArrowsInWires.isSelected());
		mnuPerformanceMode.setSelected(DrMIPS.prefs.getBoolean(DrMIPS.PERFORMANCE_MODE_PREF, DrMIPS.DEFAULT_PERFORMANCE_MODE));
		datapath.setPerformanceMode(mnuPerformanceMode.isSelected());
		lblDatapathDataFormat.setVisible(!mnuPerformanceMode.isSelected());
		cmbDatapathDataFormat.setVisible(!mnuPerformanceMode.isSelected());
		lblDatapathPerformance.setVisible(mnuPerformanceMode.isSelected());
		cmbDatapathPerformance.setVisible(mnuPerformanceMode.isSelected());
		mnuOverlayedData.setSelected(DrMIPS.prefs.getBoolean(DrMIPS.OVERLAYED_DATA_PREF, DrMIPS.DEFAULT_OVERLAYED_DATA));
		datapath.setShowTips(mnuOverlayedData.isSelected());
		mnuOverlayedShowNames.setEnabled(mnuOverlayedData.isSelected());
		mnuOverlayedShowNames.setSelected(DrMIPS.prefs.getBoolean(DrMIPS.OVERLAYED_SHOW_NAMES_PREF, DrMIPS.DEFAULT_OVERLAYED_SHOW_NAMES));
		datapath.setShowTipsNames(mnuOverlayedShowNames.isSelected());
		mnuOverlayedShowForAll.setEnabled(mnuOverlayedData.isSelected());
		mnuOverlayedShowForAll.setSelected(DrMIPS.prefs.getBoolean(DrMIPS.OVERLAYED_SHOW_FOR_ALL_PREF, DrMIPS.DEFAULT_OVERLAYED_SHOW_FOR_ALL));
		datapath.setShowTipsForAllComps(mnuOverlayedShowForAll.isSelected());
		mnuRemoveLatencies.setEnabled(mnuPerformanceMode.isSelected());
		mnuRestoreLatencies.setEnabled(mnuPerformanceMode.isSelected());
		refreshDatapathHelp();
		switchZoomAuto(DrMIPS.prefs.getBoolean(DrMIPS.AUTO_SCALE_PREF, DrMIPS.DEFAULT_AUTO_SCALE));
		updateZoomStatus();
	}

	/**
	 * Creates new form FrmSimulator and loads the code from the given file.
	 * @param filename The path to the file.
	 */
	public FrmSimulator(String filename) {
		this();
		openFile(filename);
	}

	/**
	 * Obtains the icon (in different sizes) for the window.
	 */
	private void obtainIcons() {
		icons = new LinkedList<>();
		int[] sizes = {16, 24, 32, 48, 64, 96, 128, 256, 512};
		for(int size: sizes)
			icons.add((new ImageIcon(getClass().getResource("/res/icons/x" + size + "/drmips.png"))).getImage());
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpLanguages = new javax.swing.ButtonGroup();
        mnuEditP = new javax.swing.JPopupMenu();
        mnuUndoP = new javax.swing.JMenuItem();
        mnuRedoP = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        mnuCutP = new javax.swing.JMenuItem();
        mnuCopyP = new javax.swing.JMenuItem();
        mnuPasteP = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        mnuSelectAllP = new javax.swing.JMenuItem();
        mnuFindReplaceP = new javax.swing.JMenuItem();
        mnuTabSide = new javax.swing.JPopupMenu();
        mnuSwitchSide = new javax.swing.JMenuItem();
        txtPrint = new javax.swing.JTextArea();
        desktop = new org.jscroll.JScrollDesktopPane();
        pnlToolBar = new javax.swing.JToolBar();
        cmdNew = new javax.swing.JButton();
        cmdOpen = new javax.swing.JButton();
        cmdSave = new javax.swing.JButton();
        cmdSaveAs = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        cmdAssemble = new javax.swing.JButton();
        cmdRestart = new javax.swing.JButton();
        cmdBackStep = new javax.swing.JButton();
        cmdStep = new javax.swing.JButton();
        cmdRun = new javax.swing.JButton();
        jSeparator12 = new javax.swing.JToolBar.Separator();
        cmdStatistics = new javax.swing.JButton();
        cmdSupportedInstructions = new javax.swing.JButton();
        cmdHelp = new javax.swing.JButton();
        jSeparator18 = new javax.swing.JToolBar.Separator();
        cmdZoomIn = new javax.swing.JButton();
        cmdZoomOut = new javax.swing.JButton();
        cmdZoomNormal = new javax.swing.JButton();
        chkZoomAutoAdjust = new javax.swing.JToggleButton();
        jSeparator19 = new javax.swing.JToolBar.Separator();
        lblZoom = new javax.swing.JLabel();
        pnlSplit = new javax.swing.JSplitPane();
        pnlLeft = new javax.swing.JTabbedPane();
        pnlCode = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        lblCaretPosition = new javax.swing.JLabel();
        pnlAssembledCode = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblAssembledCode = new brunonova.drmips.pc.AssembledCodeTable();
        jPanel3 = new javax.swing.JPanel();
        lblAssembledCodeFormat = new javax.swing.JLabel();
        cmbAssembledCodeFormat = new javax.swing.JComboBox();
        pnlDatapath = new javax.swing.JPanel();
        tblExec = new brunonova.drmips.pc.ExecTable();
        datapathScroll = new javax.swing.JScrollPane();
        datapath = new brunonova.drmips.pc.DatapathPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        lblDatapathDataFormat = new javax.swing.JLabel();
        cmbDatapathDataFormat = new javax.swing.JComboBox();
        lblDatapathPerformance = new javax.swing.JLabel();
        cmbDatapathPerformance = new javax.swing.JComboBox();
        lblFile = new javax.swing.JLabel();
        lblFileName = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        lblDatapathHelp = new javax.swing.JLabel();
        pnlDataMemory = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblDataMemory = new brunonova.drmips.pc.DataMemoryTable();
        jPanel4 = new javax.swing.JPanel();
        lblDataMemoryFormat = new javax.swing.JLabel();
        cmbDataMemoryFormat = new javax.swing.JComboBox();
        pnlRight = new javax.swing.JTabbedPane();
        pnlRegisters = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        lblRegFormat = new javax.swing.JLabel();
        cmbRegFormat = new javax.swing.JComboBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblRegisters = new brunonova.drmips.pc.RegistersTable();
        mnuBar = new javax.swing.JMenuBar();
        lbl = new javax.swing.JMenu();
        mnuNew = new javax.swing.JMenuItem();
        mnuOpen = new javax.swing.JMenuItem();
        mnuOpenRecent = new javax.swing.JMenu();
        mnuOpenLastFileAtStartup = new javax.swing.JCheckBoxMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mnuSave = new javax.swing.JMenuItem();
        mnuSaveAs = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        mnuPrint = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        mnuExit = new javax.swing.JMenuItem();
        mnuView = new javax.swing.JMenu();
        mnuSwitchTheme = new javax.swing.JCheckBoxMenuItem();
        mnuInternalWindows = new javax.swing.JCheckBoxMenuItem();
        mnuWindows = new javax.swing.JMenu();
        mnuTileWindows = new javax.swing.JMenuItem();
        mnuCascadeWindows = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        mnuMarginLine = new javax.swing.JCheckBoxMenuItem();
        jSeparator16 = new javax.swing.JPopupMenu.Separator();
        mnuLanguage = new javax.swing.JMenu();
        mnuOpenGL = new javax.swing.JCheckBoxMenuItem();
        mnuEdit = new javax.swing.JMenu();
        mnuUndo = new javax.swing.JMenuItem();
        mnuRedo = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        mnuCut = new javax.swing.JMenuItem();
        mnuCopy = new javax.swing.JMenuItem();
        mnuPaste = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        mnuSelectAll = new javax.swing.JMenuItem();
        mnuFindReplace = new javax.swing.JMenuItem();
        mnuDatapath = new javax.swing.JMenu();
        mnuPerformanceMode = new javax.swing.JCheckBoxMenuItem();
        mnuControlPath = new javax.swing.JCheckBoxMenuItem();
        mnuArrowsInWires = new javax.swing.JCheckBoxMenuItem();
        mnuOverlayed = new javax.swing.JMenu();
        mnuOverlayedData = new javax.swing.JCheckBoxMenuItem();
        mnuOverlayedShowNames = new javax.swing.JCheckBoxMenuItem();
        mnuOverlayedShowForAll = new javax.swing.JCheckBoxMenuItem();
        jSeparator14 = new javax.swing.JPopupMenu.Separator();
        mnuZoomIn = new javax.swing.JMenuItem();
        mnuZoomOut = new javax.swing.JMenuItem();
        mnuZoomNormal = new javax.swing.JMenuItem();
        mnuZoomAutoAdjust = new javax.swing.JCheckBoxMenuItem();
        jSeparator17 = new javax.swing.JPopupMenu.Separator();
        mnuRestoreLatencies = new javax.swing.JMenuItem();
        mnuRemoveLatencies = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JPopupMenu.Separator();
        mnuStatistics = new javax.swing.JMenuItem();
        mnuExecute = new javax.swing.JMenu();
        mnuAssemble = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        mnuRestart = new javax.swing.JMenuItem();
        mnuBackStep = new javax.swing.JMenuItem();
        mnuStep = new javax.swing.JMenuItem();
        mnuRun = new javax.swing.JMenuItem();
        mnuBreak = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        mnuResetDataBeforeAssembling = new javax.swing.JCheckBoxMenuItem();
        mnuCPU = new javax.swing.JMenu();
        mnuLoadCPU = new javax.swing.JMenuItem();
        mnuLoadRecentCPU = new javax.swing.JMenu();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        mnuSupportedInstructions = new javax.swing.JMenuItem();
        mnuHelp = new javax.swing.JMenu();
        mnuDocs = new javax.swing.JMenuItem();
        mnuAbout = new javax.swing.JMenuItem();

        mnuEditP.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                mnuEditPPopupMenuWillBecomeVisible(evt);
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        mnuUndoP.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        mnuUndoP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/undo.png"))); // NOI18N
        mnuUndoP.setText("undo");
        mnuUndoP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuUndoPActionPerformed(evt);
            }
        });
        mnuEditP.add(mnuUndoP);

        mnuRedoP.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        mnuRedoP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/redo.png"))); // NOI18N
        mnuRedoP.setText("redo");
        mnuRedoP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRedoPActionPerformed(evt);
            }
        });
        mnuEditP.add(mnuRedoP);
        mnuEditP.add(jSeparator7);

        mnuCutP.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        mnuCutP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/cut.png"))); // NOI18N
        mnuCutP.setText("cut");
        mnuCutP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCutPActionPerformed(evt);
            }
        });
        mnuEditP.add(mnuCutP);

        mnuCopyP.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        mnuCopyP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/copy.png"))); // NOI18N
        mnuCopyP.setText("copy");
        mnuCopyP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCopyPActionPerformed(evt);
            }
        });
        mnuEditP.add(mnuCopyP);

        mnuPasteP.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        mnuPasteP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/paste.png"))); // NOI18N
        mnuPasteP.setText("paste");
        mnuPasteP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuPastePActionPerformed(evt);
            }
        });
        mnuEditP.add(mnuPasteP);
        mnuEditP.add(jSeparator8);

        mnuSelectAllP.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        mnuSelectAllP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/select_all.png"))); // NOI18N
        mnuSelectAllP.setText("select_all");
        mnuSelectAllP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSelectAllPActionPerformed(evt);
            }
        });
        mnuEditP.add(mnuSelectAllP);

        mnuFindReplaceP.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        mnuFindReplaceP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/search.png"))); // NOI18N
        mnuFindReplaceP.setText("find_replace");
        mnuFindReplaceP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuFindReplacePActionPerformed(evt);
            }
        });
        mnuEditP.add(mnuFindReplaceP);

        mnuSwitchSide.setText("jMenuItem1");
        mnuSwitchSide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSwitchSideActionPerformed(evt);
            }
        });
        mnuTabSide.add(mnuSwitchSide);

        txtPrint.setBackground(java.awt.Color.white);
        txtPrint.setColumns(80);
        txtPrint.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        txtPrint.setForeground(java.awt.Color.black);
        txtPrint.setLineWrap(true);
        txtPrint.setRows(30);
        txtPrint.setTabSize(4);
        txtPrint.setWrapStyleWord(true);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(AppInfo.NAME);
        setIconImages(icons);
        setMinimumSize(new java.awt.Dimension(500, 400));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        pnlToolBar.setRollover(true);

        cmdNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/new.png"))); // NOI18N
        cmdNew.setFocusable(false);
        cmdNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdNewActionPerformed(evt);
            }
        });
        pnlToolBar.add(cmdNew);

        cmdOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/open.png"))); // NOI18N
        cmdOpen.setFocusable(false);
        cmdOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdOpenActionPerformed(evt);
            }
        });
        pnlToolBar.add(cmdOpen);

        cmdSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/save.png"))); // NOI18N
        cmdSave.setToolTipText("");
        cmdSave.setFocusable(false);
        cmdSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSaveActionPerformed(evt);
            }
        });
        pnlToolBar.add(cmdSave);

        cmdSaveAs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/save_as.png"))); // NOI18N
        cmdSaveAs.setFocusable(false);
        cmdSaveAs.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSaveAs.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSaveAsActionPerformed(evt);
            }
        });
        pnlToolBar.add(cmdSaveAs);
        pnlToolBar.add(jSeparator4);

        cmdAssemble.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/assemble.png"))); // NOI18N
        cmdAssemble.setFocusable(false);
        cmdAssemble.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdAssemble.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdAssemble.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdAssembleActionPerformed(evt);
            }
        });
        pnlToolBar.add(cmdAssemble);

        cmdRestart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/restart.png"))); // NOI18N
        cmdRestart.setEnabled(false);
        cmdRestart.setFocusable(false);
        cmdRestart.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdRestart.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdRestart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRestartActionPerformed(evt);
            }
        });
        pnlToolBar.add(cmdRestart);

        cmdBackStep.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/back_step.png"))); // NOI18N
        cmdBackStep.setEnabled(false);
        cmdBackStep.setFocusable(false);
        cmdBackStep.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdBackStep.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdBackStep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdBackStepActionPerformed(evt);
            }
        });
        pnlToolBar.add(cmdBackStep);

        cmdStep.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/step.png"))); // NOI18N
        cmdStep.setEnabled(false);
        cmdStep.setFocusable(false);
        cmdStep.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdStep.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdStep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdStepActionPerformed(evt);
            }
        });
        pnlToolBar.add(cmdStep);

        cmdRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/run.png"))); // NOI18N
        cmdRun.setEnabled(false);
        cmdRun.setFocusable(false);
        cmdRun.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdRun.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRunActionPerformed(evt);
            }
        });
        pnlToolBar.add(cmdRun);
        pnlToolBar.add(jSeparator12);

        cmdStatistics.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/statistics.png"))); // NOI18N
        cmdStatistics.setFocusable(false);
        cmdStatistics.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdStatistics.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdStatistics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdStatisticsActionPerformed(evt);
            }
        });
        pnlToolBar.add(cmdStatistics);

        cmdSupportedInstructions.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/info.png"))); // NOI18N
        cmdSupportedInstructions.setFocusable(false);
        cmdSupportedInstructions.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSupportedInstructions.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdSupportedInstructions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSupportedInstructionsActionPerformed(evt);
            }
        });
        pnlToolBar.add(cmdSupportedInstructions);

        cmdHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/help.png"))); // NOI18N
        cmdHelp.setFocusable(false);
        cmdHelp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdHelp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdHelpActionPerformed(evt);
            }
        });
        pnlToolBar.add(cmdHelp);
        pnlToolBar.add(jSeparator18);

        cmdZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/zoom-in.png"))); // NOI18N
        cmdZoomIn.setFocusable(false);
        cmdZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdZoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdZoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdZoomInActionPerformed(evt);
            }
        });
        pnlToolBar.add(cmdZoomIn);

        cmdZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/zoom-out.png"))); // NOI18N
        cmdZoomOut.setEnabled(false);
        cmdZoomOut.setFocusable(false);
        cmdZoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdZoomOut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdZoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdZoomOutActionPerformed(evt);
            }
        });
        pnlToolBar.add(cmdZoomOut);

        cmdZoomNormal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/zoom-default.png"))); // NOI18N
        cmdZoomNormal.setFocusable(false);
        cmdZoomNormal.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdZoomNormal.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdZoomNormal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdZoomNormalActionPerformed(evt);
            }
        });
        pnlToolBar.add(cmdZoomNormal);

        chkZoomAutoAdjust.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x24/zoom-auto-adjust.png"))); // NOI18N
        chkZoomAutoAdjust.setFocusable(false);
        chkZoomAutoAdjust.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        chkZoomAutoAdjust.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        chkZoomAutoAdjust.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkZoomAutoAdjustActionPerformed(evt);
            }
        });
        pnlToolBar.add(chkZoomAutoAdjust);
        pnlToolBar.add(jSeparator19);

        lblZoom.setText("zoom: 100%");
        lblZoom.setFocusable(false);
        pnlToolBar.add(lblZoom);

        getContentPane().add(pnlToolBar, java.awt.BorderLayout.NORTH);

        pnlSplit.setResizeWeight(1.0);
        pnlSplit.setOneTouchExpandable(true);

        pnlLeft.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        pnlLeft.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlLeftMouseClicked(evt);
            }
        });

        pnlCode.setLayout(new java.awt.BorderLayout());

        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        lblCaretPosition.setText("line_col");
        jPanel7.add(lblCaretPosition);

        pnlCode.add(jPanel7, java.awt.BorderLayout.SOUTH);

        pnlLeft.addTab("code", pnlCode);

        pnlAssembledCode.setLayout(new java.awt.BorderLayout());

        jScrollPane4.setViewportView(tblAssembledCode);

        pnlAssembledCode.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        lblAssembledCodeFormat.setLabelFor(cmbAssembledCodeFormat);
        lblAssembledCodeFormat.setText("format:");
        jPanel3.add(lblAssembledCodeFormat);

        cmbAssembledCodeFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbAssembledCodeFormatActionPerformed(evt);
            }
        });
        jPanel3.add(cmbAssembledCodeFormat);

        pnlAssembledCode.add(jPanel3, java.awt.BorderLayout.SOUTH);

        pnlLeft.addTab("assembled_code", pnlAssembledCode);

        pnlDatapath.setLayout(new java.awt.BorderLayout());
        pnlDatapath.add(tblExec, java.awt.BorderLayout.NORTH);

        datapathScroll.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                datapathScrollComponentResized(evt);
            }
        });

        datapath.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                datapathMouseWheelMoved(evt);
            }
        });
        datapathScroll.setViewportView(datapath);

        pnlDatapath.add(datapathScroll, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        lblDatapathDataFormat.setLabelFor(cmbDatapathDataFormat);
        lblDatapathDataFormat.setText("format:");
        jPanel5.add(lblDatapathDataFormat);

        cmbDatapathDataFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbDatapathDataFormatActionPerformed(evt);
            }
        });
        jPanel5.add(cmbDatapathDataFormat);

        lblDatapathPerformance.setLabelFor(cmbDatapathDataFormat);
        lblDatapathPerformance.setText("performance:");
        jPanel5.add(lblDatapathPerformance);

        cmbDatapathPerformance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbDatapathPerformanceActionPerformed(evt);
            }
        });
        jPanel5.add(cmbDatapathPerformance);

        lblFile.setText("file:");
        jPanel5.add(lblFile);

        lblFileName.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblFileName.setText("filename");
        jPanel5.add(lblFileName);

        jPanel2.add(jPanel5, java.awt.BorderLayout.WEST);

        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        lblDatapathHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/help.png"))); // NOI18N
        jPanel6.add(lblDatapathHelp);

        jPanel2.add(jPanel6, java.awt.BorderLayout.EAST);

        pnlDatapath.add(jPanel2, java.awt.BorderLayout.SOUTH);

        pnlLeft.addTab("datapath", pnlDatapath);

        pnlDataMemory.setLayout(new java.awt.BorderLayout());

        jScrollPane5.setViewportView(tblDataMemory);

        pnlDataMemory.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        lblDataMemoryFormat.setLabelFor(cmbDataMemoryFormat);
        lblDataMemoryFormat.setText("format:");
        jPanel4.add(lblDataMemoryFormat);

        cmbDataMemoryFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbDataMemoryFormatActionPerformed(evt);
            }
        });
        jPanel4.add(cmbDataMemoryFormat);

        pnlDataMemory.add(jPanel4, java.awt.BorderLayout.SOUTH);

        pnlLeft.addTab("data_memory", pnlDataMemory);

        pnlSplit.setLeftComponent(pnlLeft);

        pnlRight.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        pnlRight.setMinimumSize(new java.awt.Dimension(200, 71));
        pnlRight.setPreferredSize(new java.awt.Dimension(200, 452));
        pnlRight.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlRightMouseClicked(evt);
            }
        });

        pnlRegisters.setMinimumSize(new java.awt.Dimension(200, 46));
        pnlRegisters.setPreferredSize(new java.awt.Dimension(200, 427));
        pnlRegisters.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        lblRegFormat.setLabelFor(cmbRegFormat);
        lblRegFormat.setText("format:");
        jPanel1.add(lblRegFormat);

        cmbRegFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbRegFormatActionPerformed(evt);
            }
        });
        jPanel1.add(cmbRegFormat);

        pnlRegisters.add(jPanel1, java.awt.BorderLayout.SOUTH);

        jScrollPane3.setViewportView(tblRegisters);

        pnlRegisters.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        pnlRight.addTab("registers", pnlRegisters);

        pnlSplit.setRightComponent(pnlRight);

        getContentPane().add(pnlSplit, java.awt.BorderLayout.CENTER);

        lbl.setText("file");
        lbl.setName(""); // NOI18N

        mnuNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        mnuNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/new.png"))); // NOI18N
        mnuNew.setText("new");
        mnuNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuNewActionPerformed(evt);
            }
        });
        lbl.add(mnuNew);

        mnuOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        mnuOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/open.png"))); // NOI18N
        mnuOpen.setText("open");
        mnuOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOpenActionPerformed(evt);
            }
        });
        lbl.add(mnuOpen);

        mnuOpenRecent.setText("open_recent");
        lbl.add(mnuOpenRecent);

        mnuOpenLastFileAtStartup.setText("open_last_file_at_startup");
        mnuOpenLastFileAtStartup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOpenLastFileAtStartupActionPerformed(evt);
            }
        });
        lbl.add(mnuOpenLastFileAtStartup);
        lbl.add(jSeparator1);

        mnuSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        mnuSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/save.png"))); // NOI18N
        mnuSave.setText("save");
        mnuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveActionPerformed(evt);
            }
        });
        lbl.add(mnuSave);

        mnuSaveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        mnuSaveAs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/save_as.png"))); // NOI18N
        mnuSaveAs.setText("save_as");
        mnuSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveAsActionPerformed(evt);
            }
        });
        lbl.add(mnuSaveAs);
        lbl.add(jSeparator2);

        mnuPrint.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        mnuPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/print.png"))); // NOI18N
        mnuPrint.setText("print");
        mnuPrint.setEnabled(false);
        mnuPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuPrintActionPerformed(evt);
            }
        });
        lbl.add(mnuPrint);
        lbl.add(jSeparator9);

        mnuExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        mnuExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/exit.png"))); // NOI18N
        mnuExit.setText("exit");
        mnuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExitActionPerformed(evt);
            }
        });
        lbl.add(mnuExit);

        mnuBar.add(lbl);

        mnuView.setText("view");

        mnuSwitchTheme.setText("dark_theme");
        mnuSwitchTheme.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/dark_theme.png"))); // NOI18N
        mnuSwitchTheme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSwitchThemeActionPerformed(evt);
            }
        });
        mnuView.add(mnuSwitchTheme);

        mnuInternalWindows.setText("internal_windows");
        mnuInternalWindows.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/windows.png"))); // NOI18N
        mnuInternalWindows.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuInternalWindowsActionPerformed(evt);
            }
        });
        mnuView.add(mnuInternalWindows);

        mnuWindows.setText("windows");
        mnuWindows.setEnabled(false);

        mnuTileWindows.setText("tile");
        mnuTileWindows.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuTileWindowsActionPerformed(evt);
            }
        });
        mnuWindows.add(mnuTileWindows);

        mnuCascadeWindows.setText("cascade");
        mnuCascadeWindows.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCascadeWindowsActionPerformed(evt);
            }
        });
        mnuWindows.add(mnuCascadeWindows);

        mnuView.add(mnuWindows);
        mnuView.add(jSeparator11);

        mnuMarginLine.setText("show_margin_line");
        mnuMarginLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuMarginLineActionPerformed(evt);
            }
        });
        mnuView.add(mnuMarginLine);
        mnuView.add(jSeparator16);

        mnuLanguage.setText("language");
        mnuView.add(mnuLanguage);

        mnuOpenGL.setText("opengl_acceleration");
        mnuOpenGL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOpenGLActionPerformed(evt);
            }
        });
        mnuView.add(mnuOpenGL);

        mnuBar.add(mnuView);

        mnuEdit.setText("edit");
        mnuEdit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mnuEditMousePressed(evt);
            }
        });

        mnuUndo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        mnuUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/undo.png"))); // NOI18N
        mnuUndo.setText("undo");
        mnuUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuUndoActionPerformed(evt);
            }
        });
        mnuEdit.add(mnuUndo);

        mnuRedo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        mnuRedo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/redo.png"))); // NOI18N
        mnuRedo.setText("redo");
        mnuRedo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRedoActionPerformed(evt);
            }
        });
        mnuEdit.add(mnuRedo);
        mnuEdit.add(jSeparator5);

        mnuCut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        mnuCut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/cut.png"))); // NOI18N
        mnuCut.setText("cut");
        mnuCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCutActionPerformed(evt);
            }
        });
        mnuEdit.add(mnuCut);

        mnuCopy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        mnuCopy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/copy.png"))); // NOI18N
        mnuCopy.setText("copy");
        mnuCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCopyActionPerformed(evt);
            }
        });
        mnuEdit.add(mnuCopy);

        mnuPaste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        mnuPaste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/paste.png"))); // NOI18N
        mnuPaste.setText("paste");
        mnuPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuPasteActionPerformed(evt);
            }
        });
        mnuEdit.add(mnuPaste);
        mnuEdit.add(jSeparator6);

        mnuSelectAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        mnuSelectAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/select_all.png"))); // NOI18N
        mnuSelectAll.setText("select_all");
        mnuSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSelectAllActionPerformed(evt);
            }
        });
        mnuEdit.add(mnuSelectAll);

        mnuFindReplace.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        mnuFindReplace.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/search.png"))); // NOI18N
        mnuFindReplace.setText("find_replace");
        mnuFindReplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuFindReplaceActionPerformed(evt);
            }
        });
        mnuEdit.add(mnuFindReplace);

        mnuBar.add(mnuEdit);

        mnuDatapath.setText("datapath");

        mnuPerformanceMode.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        mnuPerformanceMode.setText("performance_mode");
        mnuPerformanceMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuPerformanceModeActionPerformed(evt);
            }
        });
        mnuDatapath.add(mnuPerformanceMode);

        mnuControlPath.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        mnuControlPath.setSelected(true);
        mnuControlPath.setText("control_path");
        mnuControlPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuControlPathActionPerformed(evt);
            }
        });
        mnuDatapath.add(mnuControlPath);

        mnuArrowsInWires.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        mnuArrowsInWires.setSelected(true);
        mnuArrowsInWires.setText("arrows_in_wires");
        mnuArrowsInWires.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuArrowsInWiresActionPerformed(evt);
            }
        });
        mnuDatapath.add(mnuArrowsInWires);

        mnuOverlayed.setText("overlayed_data");

        mnuOverlayedData.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        mnuOverlayedData.setSelected(true);
        mnuOverlayedData.setText("display");
        mnuOverlayedData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOverlayedDataActionPerformed(evt);
            }
        });
        mnuOverlayed.add(mnuOverlayedData);

        mnuOverlayedShowNames.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        mnuOverlayedShowNames.setText("show_names");
        mnuOverlayedShowNames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOverlayedShowNamesActionPerformed(evt);
            }
        });
        mnuOverlayed.add(mnuOverlayedShowNames);

        mnuOverlayedShowForAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        mnuOverlayedShowForAll.setText("show_for_all_components");
        mnuOverlayedShowForAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOverlayedShowForAllActionPerformed(evt);
            }
        });
        mnuOverlayed.add(mnuOverlayedShowForAll);

        mnuDatapath.add(mnuOverlayed);
        mnuDatapath.add(jSeparator14);

        mnuZoomIn.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PLUS, java.awt.event.InputEvent.CTRL_MASK));
        mnuZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/zoom-in.png"))); // NOI18N
        mnuZoomIn.setText("zoom_in");
        mnuZoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuZoomInActionPerformed(evt);
            }
        });
        mnuDatapath.add(mnuZoomIn);

        mnuZoomOut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_MINUS, java.awt.event.InputEvent.CTRL_MASK));
        mnuZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/zoom-out.png"))); // NOI18N
        mnuZoomOut.setText("zoom_out");
        mnuZoomOut.setEnabled(false);
        mnuZoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuZoomOutActionPerformed(evt);
            }
        });
        mnuDatapath.add(mnuZoomOut);

        mnuZoomNormal.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0, java.awt.event.InputEvent.CTRL_MASK));
        mnuZoomNormal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/zoom-default.png"))); // NOI18N
        mnuZoomNormal.setText("normal");
        mnuZoomNormal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuZoomNormalActionPerformed(evt);
            }
        });
        mnuDatapath.add(mnuZoomNormal);

        mnuZoomAutoAdjust.setText("adjust_automatically");
        mnuZoomAutoAdjust.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/zoom-auto-adjust.png"))); // NOI18N
        mnuZoomAutoAdjust.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuZoomAutoAdjustActionPerformed(evt);
            }
        });
        mnuDatapath.add(mnuZoomAutoAdjust);
        mnuDatapath.add(jSeparator17);

        mnuRestoreLatencies.setText("restore_latencies");
        mnuRestoreLatencies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRestoreLatenciesActionPerformed(evt);
            }
        });
        mnuDatapath.add(mnuRestoreLatencies);

        mnuRemoveLatencies.setText("remove_latencies");
        mnuRemoveLatencies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRemoveLatenciesActionPerformed(evt);
            }
        });
        mnuDatapath.add(mnuRemoveLatencies);
        mnuDatapath.add(jSeparator15);

        mnuStatistics.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, 0));
        mnuStatistics.setText("statistics");
        mnuStatistics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuStatisticsActionPerformed(evt);
            }
        });
        mnuDatapath.add(mnuStatistics);

        mnuBar.add(mnuDatapath);

        mnuExecute.setText("execute");

        mnuAssemble.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        mnuAssemble.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/assemble.png"))); // NOI18N
        mnuAssemble.setText("assemble");
        mnuAssemble.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAssembleActionPerformed(evt);
            }
        });
        mnuExecute.add(mnuAssemble);
        mnuExecute.add(jSeparator3);

        mnuRestart.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0));
        mnuRestart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/restart.png"))); // NOI18N
        mnuRestart.setText("restart");
        mnuRestart.setEnabled(false);
        mnuRestart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRestartActionPerformed(evt);
            }
        });
        mnuExecute.add(mnuRestart);

        mnuBackStep.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, 0));
        mnuBackStep.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/back_step.png"))); // NOI18N
        mnuBackStep.setText("back_step");
        mnuBackStep.setEnabled(false);
        mnuBackStep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuBackStepActionPerformed(evt);
            }
        });
        mnuExecute.add(mnuBackStep);

        mnuStep.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F7, 0));
        mnuStep.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/step.png"))); // NOI18N
        mnuStep.setText("step");
        mnuStep.setEnabled(false);
        mnuStep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuStepActionPerformed(evt);
            }
        });
        mnuExecute.add(mnuStep);

        mnuRun.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        mnuRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/run.png"))); // NOI18N
        mnuRun.setText("run");
        mnuRun.setEnabled(false);
        mnuRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuRunActionPerformed(evt);
            }
        });
        mnuExecute.add(mnuRun);

        mnuBreak.setText("add breakpoint");
        mnuBreak.setEnabled(false);
        mnuBreak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuBreakActionPerformed(evt);
            }
        });
        mnuExecute.add(mnuBreak);
        mnuExecute.add(jSeparator10);

        mnuResetDataBeforeAssembling.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        mnuResetDataBeforeAssembling.setSelected(true);
        mnuResetDataBeforeAssembling.setText("reset_data_before_assembling");
        mnuResetDataBeforeAssembling.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/reset_data.png"))); // NOI18N
        mnuExecute.add(mnuResetDataBeforeAssembling);

        mnuBar.add(mnuExecute);

        mnuCPU.setText("cpu");

        mnuLoadCPU.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        mnuLoadCPU.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/load_cpu.png"))); // NOI18N
        mnuLoadCPU.setText("load");
        mnuLoadCPU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLoadCPUActionPerformed(evt);
            }
        });
        mnuCPU.add(mnuLoadCPU);

        mnuLoadRecentCPU.setText("load_recent");
        mnuCPU.add(mnuLoadRecentCPU);
        mnuCPU.add(jSeparator13);

        mnuSupportedInstructions.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, java.awt.event.InputEvent.SHIFT_MASK));
        mnuSupportedInstructions.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/info.png"))); // NOI18N
        mnuSupportedInstructions.setText("supported_instructions");
        mnuSupportedInstructions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSupportedInstructionsActionPerformed(evt);
            }
        });
        mnuCPU.add(mnuSupportedInstructions);

        mnuBar.add(mnuCPU);

        mnuHelp.setText("help");

        mnuDocs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        mnuDocs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/help.png"))); // NOI18N
        mnuDocs.setText("documentation");
        mnuDocs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuDocsActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuDocs);

        mnuAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/x16/about.png"))); // NOI18N
        mnuAbout.setText("about");
        mnuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAboutActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuAbout);

        mnuBar.add(mnuHelp);

        setJMenuBar(mnuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
		exit();
    }//GEN-LAST:event_formWindowClosing

    private void mnuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExitActionPerformed
		exit();
    }//GEN-LAST:event_mnuExitActionPerformed

    private void mnuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAboutActionPerformed
		(new DlgAbout(this)).setVisible(true);
    }//GEN-LAST:event_mnuAboutActionPerformed

	@SuppressWarnings("UseSpecificCatch")
    private void mnuLoadCPUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLoadCPUActionPerformed
		try {
			if(cpuFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				loadCPU(cpuFileChooser.getSelectedFile().getPath());
				if(!mnuInternalWindows.isSelected())
					tabDatapath.select();
			}
		}
		catch(Throwable ex) {
			JOptionPane.showMessageDialog(this, Lang.t("invalid_file") + "\n" + ex.getClass().getName() + " (" + ex.getMessage() + ")", AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
			LOG.log(Level.WARNING, "error loading CPU", ex);
		}
    }//GEN-LAST:event_mnuLoadCPUActionPerformed

    private void cmbRegFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbRegFormatActionPerformed
		tblRegisters.refreshValues(cmbRegFormat.getSelectedIndex());
    }//GEN-LAST:event_cmbRegFormatActionPerformed

    private void mnuStepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuStepActionPerformed
		step();
    }//GEN-LAST:event_mnuStepActionPerformed

    private void cmdStepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdStepActionPerformed
		step();
    }//GEN-LAST:event_cmdStepActionPerformed

    private void cmbDatapathDataFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbDatapathDataFormatActionPerformed
		datapath.translate(cmbDatapathDataFormat.getSelectedIndex());
		tblExec.refresh(cmbDatapathDataFormat.getSelectedIndex());
    }//GEN-LAST:event_cmbDatapathDataFormatActionPerformed

    private void mnuAssembleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAssembleActionPerformed
		assemble();
    }//GEN-LAST:event_mnuAssembleActionPerformed

    private void cmdAssembleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAssembleActionPerformed
		assemble();
    }//GEN-LAST:event_cmdAssembleActionPerformed

    private void cmbAssembledCodeFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAssembledCodeFormatActionPerformed
		tblAssembledCode.refresh(cmbAssembledCodeFormat.getSelectedIndex());
    }//GEN-LAST:event_cmbAssembledCodeFormatActionPerformed

    private void mnuNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuNewActionPerformed
		newFile();
    }//GEN-LAST:event_mnuNewActionPerformed

    private void mnuOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOpenActionPerformed
		openFile();
    }//GEN-LAST:event_mnuOpenActionPerformed

    private void mnuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveAsActionPerformed
		saveFileAs();
    }//GEN-LAST:event_mnuSaveAsActionPerformed

    private void mnuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveActionPerformed
		saveFile();
    }//GEN-LAST:event_mnuSaveActionPerformed

    private void cmdBackStepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdBackStepActionPerformed
		backStep();
    }//GEN-LAST:event_cmdBackStepActionPerformed

    private void mnuBackStepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuBackStepActionPerformed
		backStep();
    }//GEN-LAST:event_mnuBackStepActionPerformed

    private void cmdNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdNewActionPerformed
		newFile();
    }//GEN-LAST:event_cmdNewActionPerformed

    private void cmdOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdOpenActionPerformed
        openFile();
    }//GEN-LAST:event_cmdOpenActionPerformed

    private void cmdSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSaveActionPerformed
        saveFile();
    }//GEN-LAST:event_cmdSaveActionPerformed

    private void cmdSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSaveAsActionPerformed
        saveFileAs();
    }//GEN-LAST:event_cmdSaveAsActionPerformed

    private void cmbDataMemoryFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbDataMemoryFormatActionPerformed
		tblDataMemory.refreshValues(cmbDataMemoryFormat.getSelectedIndex());
    }//GEN-LAST:event_cmbDataMemoryFormatActionPerformed

    private void mnuEditMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mnuEditMousePressed
		mnuUndo.setEnabled(txtCode.canUndo());
		mnuRedo.setEnabled(txtCode.canRedo());
    }//GEN-LAST:event_mnuEditMousePressed

    private void mnuUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuUndoActionPerformed
		txtCode.undoLastAction();
    }//GEN-LAST:event_mnuUndoActionPerformed

    private void mnuRedoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRedoActionPerformed
		txtCode.redoLastAction();
    }//GEN-LAST:event_mnuRedoActionPerformed

    private void mnuPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPasteActionPerformed
		txtCode.paste();
    }//GEN-LAST:event_mnuPasteActionPerformed

    private void mnuSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSelectAllActionPerformed
		txtCode.selectAll();
    }//GEN-LAST:event_mnuSelectAllActionPerformed

    private void mnuCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCopyActionPerformed
		txtCode.copy();
    }//GEN-LAST:event_mnuCopyActionPerformed

    private void mnuCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCutActionPerformed
		txtCode.cut();
    }//GEN-LAST:event_mnuCutActionPerformed

    private void mnuUndoPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuUndoPActionPerformed
		txtCode.undoLastAction();
    }//GEN-LAST:event_mnuUndoPActionPerformed

    private void mnuRedoPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRedoPActionPerformed
		txtCode.redoLastAction();
    }//GEN-LAST:event_mnuRedoPActionPerformed

    private void mnuCutPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCutPActionPerformed
		txtCode.cut();
    }//GEN-LAST:event_mnuCutPActionPerformed

    private void mnuCopyPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCopyPActionPerformed
		txtCode.copy();
    }//GEN-LAST:event_mnuCopyPActionPerformed

    private void mnuPastePActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPastePActionPerformed
		txtCode.paste();
    }//GEN-LAST:event_mnuPastePActionPerformed

    private void mnuSelectAllPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSelectAllPActionPerformed
		txtCode.selectAll();
    }//GEN-LAST:event_mnuSelectAllPActionPerformed

    private void mnuEditPPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_mnuEditPPopupMenuWillBecomeVisible
		mnuUndoP.setEnabled(txtCode.canUndo());
		mnuRedoP.setEnabled(txtCode.canRedo());
    }//GEN-LAST:event_mnuEditPPopupMenuWillBecomeVisible

    private void mnuFindReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuFindReplaceActionPerformed
		dlgFindReplace.setVisible(true);
    }//GEN-LAST:event_mnuFindReplaceActionPerformed

    private void mnuFindReplacePActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuFindReplacePActionPerformed
		dlgFindReplace.setVisible(true);
    }//GEN-LAST:event_mnuFindReplacePActionPerformed

    private void mnuPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPrintActionPerformed
		printFile();
    }//GEN-LAST:event_mnuPrintActionPerformed

    private void pnlLeftMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlLeftMouseClicked
		if(evt.getButton() == MouseEvent.BUTTON3) {
			selectedTab = getSelectedTab(Util.LEFT);
			mnuTabSide.show(pnlLeft, evt.getX(), evt.getY());
		}
    }//GEN-LAST:event_pnlLeftMouseClicked

    private void pnlRightMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlRightMouseClicked
		if(evt.getButton() == MouseEvent.BUTTON3) {
			selectedTab = getSelectedTab(Util.RIGHT);
			mnuTabSide.show(pnlRight, evt.getX(), evt.getY());
		}
    }//GEN-LAST:event_pnlRightMouseClicked

    private void mnuSwitchSideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSwitchSideActionPerformed
		// Save side on preferences
		int newSide = (selectedTab.getSide() == Util.LEFT) ? Util.RIGHT : Util.LEFT;
		if(selectedTab == tabCode)
			DrMIPS.prefs.putInt(DrMIPS.CODE_TAB_SIDE_PREF, newSide);
		else if(selectedTab == tabDatapath)
			DrMIPS.prefs.putInt(DrMIPS.DATAPATH_TAB_SIDE_PREF, newSide);
		else if(selectedTab == tabRegisters)
			DrMIPS.prefs.putInt(DrMIPS.REGISTERS_TAB_SIDE_PREF, newSide);
		else if(selectedTab == tabAssembledCode)
			DrMIPS.prefs.putInt(DrMIPS.ASSEMBLED_CODE_TAB_SIDE_PREF, newSide);
		else if(selectedTab == tabDataMemory)
			DrMIPS.prefs.putInt(DrMIPS.DATA_MEMORY_TAB_SIDE_PREF, newSide);

		refreshTabSides(); // refresh tabs
    }//GEN-LAST:event_mnuSwitchSideActionPerformed

    private void mnuSwitchThemeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSwitchThemeActionPerformed
		switchTheme();
    }//GEN-LAST:event_mnuSwitchThemeActionPerformed

    private void mnuInternalWindowsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuInternalWindowsActionPerformed
		switchUseInternalWindows();
    }//GEN-LAST:event_mnuInternalWindowsActionPerformed

    private void mnuControlPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuControlPathActionPerformed
		datapath.setControlPathVisible(mnuControlPath.isSelected());
		DrMIPS.prefs.putBoolean(DrMIPS.SHOW_CONTROL_PATH_PREF, mnuControlPath.isSelected());
    }//GEN-LAST:event_mnuControlPathActionPerformed

    private void mnuArrowsInWiresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuArrowsInWiresActionPerformed
		datapath.setShowArrows(mnuArrowsInWires.isSelected());
		DrMIPS.prefs.putBoolean(DrMIPS.SHOW_ARROWS_PREF, mnuArrowsInWires.isSelected());
    }//GEN-LAST:event_mnuArrowsInWiresActionPerformed

    private void mnuPerformanceModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPerformanceModeActionPerformed
		datapath.setPerformanceMode(mnuPerformanceMode.isSelected());
		lblDatapathDataFormat.setVisible(!mnuPerformanceMode.isSelected());
		cmbDatapathDataFormat.setVisible(!mnuPerformanceMode.isSelected());
		lblDatapathPerformance.setVisible(mnuPerformanceMode.isSelected());
		cmbDatapathPerformance.setVisible(mnuPerformanceMode.isSelected());
		mnuRemoveLatencies.setEnabled(mnuPerformanceMode.isSelected());
		mnuRestoreLatencies.setEnabled(mnuPerformanceMode.isSelected());
		DrMIPS.prefs.putBoolean(DrMIPS.PERFORMANCE_MODE_PREF, mnuPerformanceMode.isSelected());
		refreshDatapathHelp();
    }//GEN-LAST:event_mnuPerformanceModeActionPerformed

    private void mnuRestoreLatenciesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRestoreLatenciesActionPerformed
		cpu.resetLatencies();
		datapath.refresh();
    }//GEN-LAST:event_mnuRestoreLatenciesActionPerformed

    private void mnuOverlayedDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOverlayedDataActionPerformed
		datapath.setShowTips(mnuOverlayedData.isSelected());
		mnuOverlayedShowNames.setEnabled(mnuOverlayedData.isSelected());
		mnuOverlayedShowForAll.setEnabled(mnuOverlayedData.isSelected());
		DrMIPS.prefs.putBoolean(DrMIPS.OVERLAYED_DATA_PREF, mnuOverlayedData.isSelected());
    }//GEN-LAST:event_mnuOverlayedDataActionPerformed

    private void cmdRestartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRestartActionPerformed
		restart();
    }//GEN-LAST:event_cmdRestartActionPerformed

    private void mnuRestartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRestartActionPerformed
		restart();
    }//GEN-LAST:event_mnuRestartActionPerformed

    private void cmdRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRunActionPerformed
		run();
    }//GEN-LAST:event_cmdRunActionPerformed

    private void mnuRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRunActionPerformed
		run();
    }//GEN-LAST:event_mnuRunActionPerformed

    private void mnuBreakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuBreakActionPerformed
        addBreakpoint();
    }//GEN-LAST:event_mnuBreakActionPerformed

    private void mnuDocsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuDocsActionPerformed
		openDocDir();
    }//GEN-LAST:event_mnuDocsActionPerformed

    private void mnuRemoveLatenciesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRemoveLatenciesActionPerformed
		cpu.removeLatencies();
		datapath.refresh();
    }//GEN-LAST:event_mnuRemoveLatenciesActionPerformed

    private void cmdHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdHelpActionPerformed
		openDocDir();
    }//GEN-LAST:event_cmdHelpActionPerformed

    private void mnuSupportedInstructionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSupportedInstructionsActionPerformed
		dlgSupportedInstructions.setVisible(true);
    }//GEN-LAST:event_mnuSupportedInstructionsActionPerformed

    private void cmdSupportedInstructionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSupportedInstructionsActionPerformed
		dlgSupportedInstructions.setVisible(true);
    }//GEN-LAST:event_cmdSupportedInstructionsActionPerformed

    private void mnuStatisticsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuStatisticsActionPerformed
		dlgStatistics.setVisible(true);
    }//GEN-LAST:event_mnuStatisticsActionPerformed

    private void cmdStatisticsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdStatisticsActionPerformed
		dlgStatistics.setVisible(true);
    }//GEN-LAST:event_cmdStatisticsActionPerformed

    private void cmbDatapathPerformanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbDatapathPerformanceActionPerformed
		cpu.setPerformanceInstructionDependent(cmbDatapathPerformance.getSelectedIndex() == Util.INSTRUCTION_PERFORMANCE_TYPE_INDEX);
		datapath.refresh();
    }//GEN-LAST:event_cmbDatapathPerformanceActionPerformed

    private void mnuMarginLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuMarginLineActionPerformed
		txtCode.setMarginLineEnabled(mnuMarginLine.isSelected());
		txtCode.repaint();
		DrMIPS.prefs.putBoolean(DrMIPS.MARGIN_LINE_PREF, mnuMarginLine.isSelected());
    }//GEN-LAST:event_mnuMarginLineActionPerformed

    private void mnuTileWindowsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuTileWindowsActionPerformed
		if(mnuInternalWindows.isSelected())
			desktop.tileInternalFrames();
    }//GEN-LAST:event_mnuTileWindowsActionPerformed

    private void mnuCascadeWindowsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCascadeWindowsActionPerformed
		if(mnuInternalWindows.isSelected())
			desktop.cascadeInternalFrames();
    }//GEN-LAST:event_mnuCascadeWindowsActionPerformed

    private void mnuOpenLastFileAtStartupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOpenLastFileAtStartupActionPerformed
		DrMIPS.prefs.putBoolean(DrMIPS.OPEN_LAST_FILE_AT_STARTUP_PREF, mnuOpenLastFileAtStartup.isSelected());
    }//GEN-LAST:event_mnuOpenLastFileAtStartupActionPerformed

    private void mnuZoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuZoomInActionPerformed
		zoomIn();
    }//GEN-LAST:event_mnuZoomInActionPerformed

    private void mnuZoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuZoomOutActionPerformed
		zoomOut();
    }//GEN-LAST:event_mnuZoomOutActionPerformed

    private void mnuZoomNormalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuZoomNormalActionPerformed
		zoomNormal();
    }//GEN-LAST:event_mnuZoomNormalActionPerformed

    private void cmdZoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdZoomInActionPerformed
		zoomIn();
    }//GEN-LAST:event_cmdZoomInActionPerformed

    private void cmdZoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdZoomOutActionPerformed
		zoomOut();
    }//GEN-LAST:event_cmdZoomOutActionPerformed

    private void cmdZoomNormalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdZoomNormalActionPerformed
		zoomNormal();
    }//GEN-LAST:event_cmdZoomNormalActionPerformed

    private void datapathScrollComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_datapathScrollComponentResized
		if(mnuZoomAutoAdjust.isSelected()) {
			datapath.scaleToFitPanel(datapathScroll.getSize());
			updateZoomStatus();
		}
    }//GEN-LAST:event_datapathScrollComponentResized

    private void mnuZoomAutoAdjustActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuZoomAutoAdjustActionPerformed
		switchZoomAuto();
    }//GEN-LAST:event_mnuZoomAutoAdjustActionPerformed

    private void chkZoomAutoAdjustActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkZoomAutoAdjustActionPerformed
		switchZoomAuto();
    }//GEN-LAST:event_chkZoomAutoAdjustActionPerformed

    private void datapathMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_datapathMouseWheelMoved
		if(evt.isControlDown()) {
			if(!mnuZoomAutoAdjust.isSelected()) {
				// Zoom in/out
				if(evt.getWheelRotation() < 0)
					zoomIn();
				else
					zoomOut();
			}
		}
		else
			evt.getComponent().getParent().dispatchEvent(evt); // scroll up/down
    }//GEN-LAST:event_datapathMouseWheelMoved

    private void mnuOverlayedShowNamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOverlayedShowNamesActionPerformed
		datapath.setShowTipsNames(mnuOverlayedShowNames.isSelected());
		DrMIPS.prefs.putBoolean(DrMIPS.OVERLAYED_SHOW_NAMES_PREF, mnuOverlayedShowNames.isSelected());
    }//GEN-LAST:event_mnuOverlayedShowNamesActionPerformed

    private void mnuOverlayedShowForAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOverlayedShowForAllActionPerformed
		datapath.setShowTipsForAllComps(mnuOverlayedShowForAll.isSelected());
		DrMIPS.prefs.putBoolean(DrMIPS.OVERLAYED_SHOW_FOR_ALL_PREF, mnuOverlayedShowForAll.isSelected());
    }//GEN-LAST:event_mnuOverlayedShowForAllActionPerformed

    private void mnuOpenGLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOpenGLActionPerformed
		boolean useOpenGl = DrMIPS.prefs.getBoolean(DrMIPS.OPENGL_PREF, DrMIPS.DEFAULT_OPENGL);
		int res;

		if(!useOpenGl) {
			res = JOptionPane.showConfirmDialog(this, Lang.t("enable_opengl_msg"), AppInfo.NAME, JOptionPane.OK_CANCEL_OPTION);
			if(res == JOptionPane.OK_OPTION) {
				DrMIPS.prefs.putBoolean(DrMIPS.OPENGL_PREF, true);
				mnuOpenGL.setEnabled(false);
			} else {
				mnuOpenGL.setSelected(useOpenGl);
			}
		} else {
			res = JOptionPane.showConfirmDialog(this, Lang.t("disable_opengl_msg"), AppInfo.NAME, JOptionPane.OK_CANCEL_OPTION);
			if(res == JOptionPane.OK_OPTION) {
				DrMIPS.prefs.putBoolean(DrMIPS.OPENGL_PREF, false);
				mnuOpenGL.setEnabled(false);
			} else {
				mnuOpenGL.setSelected(useOpenGl);
			}
		}
    }//GEN-LAST:event_mnuOpenGLActionPerformed

	/**
	 * Sets the path of the opened file and updates the title bar and recent files.
	 * @param path Path to the opened file.
	 */
	private void setOpenedFile(File file) {
		openFile = file;
		String title = AppInfo.NAME;
		if(openFile != null) {
			title = openFile.getName() + " (" + openFile.getAbsolutePath() + ") - " + title;
			addRecentFile(file);
			DrMIPS.prefs.put(DrMIPS.LAST_FILE_PREF, file.getAbsolutePath());
		}
		else
			DrMIPS.prefs.remove(DrMIPS.LAST_FILE_PREF);
		setTitle(title);
	}

	/**
	 * Adds a file to the "Open recent" menu and saves the recent files paths.
	 * @param file The file to add.
	 */
	private void addRecentFile(File file) {
		addRecentFileToPrefs(file, DrMIPS.RECENT_FILES_PREF, DrMIPS.MAX_RECENT_FILES);
		updateRecentFiles();
	}

	/**
	 * Adds a file to the CPU's "Load recent" menu and saves the recent CPUs paths.
	 * @param file The file to add.
	 */
	private void addRecentCPU(File file) {
		addRecentFileToPrefs(file, DrMIPS.RECENT_CPUS_PREF, DrMIPS.MAX_RECENT_CPUS);
		updateRecentCPUs();
	}

	/**
	 * Adds a file to the saved recent files.
	 * @param file The file to add.
	 * @param pref The prefix of the preference.
	 * @param maxFiles The maximum number of files to save.
	 */
	private void addRecentFileToPrefs(File file, String pref, int maxFiles) {
		List<File> files = new LinkedList<>();
		files.add(file);
		File f;
		String filename;

		// Read recent file names
		for(int i = 0; i < maxFiles && files.size() < maxFiles; i++) {
			filename = DrMIPS.prefs.get(pref + i, null);
			if(filename != null) {
				f = new File(filename);
				if(f.exists() && !file.getAbsolutePath().equals(f.getAbsolutePath()))
					files.add(f);
				DrMIPS.prefs.remove(pref + i);
			}
		}

		// Save new and old filenames
		for(int i = 0; i < files.size(); i++)
			DrMIPS.prefs.put(pref + i, files.get(i).getAbsolutePath());
	}

	/**
	 * Updates the "Open recent" menu with the recent files.
	 */
	private void updateRecentFiles() {
		mnuOpenRecent.removeAll();
		File file;
		String filename;
		JMenuItem menuItem;

		for(int i = 0; i < DrMIPS.MAX_RECENT_FILES; i++) {
			filename = DrMIPS.prefs.get(DrMIPS.RECENT_FILES_PREF + i, null);
			if(filename != null) {
				file = new File(filename);
				if(file.exists()) {
					menuItem = new JMenuItem(file.getAbsolutePath());
					menuItem.addActionListener(new RecentFileActionListener(file));
					mnuOpenRecent.add(menuItem);
				}
			}
		}
	}

	/**
	 * Updates the CPU's "Load recent" menu with the recent CPUs.
	 */
	private void updateRecentCPUs() {
		mnuLoadRecentCPU.removeAll();
		File file;
		String filename;
		JMenuItem menuItem;

		for(int i = 0; i < DrMIPS.MAX_RECENT_CPUS; i++) {
			filename = DrMIPS.prefs.get(DrMIPS.RECENT_CPUS_PREF + i, null);
			if(filename != null) {
				file = new File(filename);
				if(file.exists()) {
					menuItem = new JMenuItem(file.getAbsolutePath());
					menuItem.addActionListener(new RecentCPUActionListener(file));
					mnuLoadRecentCPU.add(menuItem);
				}
			}
		}
	}

	/**
	 * Clears the code editor.
	 */
	private void newFile() {
		boolean create = true;
		if(txtCode != null && txtCode.isDirty()) { // file changed?
			int opt = JOptionPane.showConfirmDialog(this, Lang.t("code_changed"), AppInfo.NAME, JOptionPane.YES_NO_CANCEL_OPTION);
			switch(opt) {
				case JOptionPane.YES_OPTION:
					create = true;
					saveFile();
					break;
				case JOptionPane.NO_OPTION: create = true; break;
				case JOptionPane.CANCEL_OPTION: create = false; break;
			}
		}

		if(create) {
			txtCode.setText("");
			txtCode.setDirty(false);
			txtCode.clearErrorIcons();
			setOpenedFile(null);
		}
	}

	/**
	 * Shows the file chooser to open a file.
	 */
	private void openFile() {
		boolean open = true;
		if(txtCode != null && txtCode.isDirty()) { // file changed?
			int opt = JOptionPane.showConfirmDialog(this, Lang.t("code_changed"), AppInfo.NAME, JOptionPane.YES_NO_CANCEL_OPTION);
			switch(opt) {
				case JOptionPane.YES_OPTION:
					open = true;
					saveFile();
					break;
				case JOptionPane.NO_OPTION: open = true; break;
				case JOptionPane.CANCEL_OPTION: open = false; break;
			}
		}

		if(open) {
			codeFileChooser.setDialogTitle(Lang.t("open"));
			if(codeFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
				openFile(codeFileChooser.getSelectedFile());
		}
	}

	/**
	 * Opens and loads the code from the given file.
	 * @param path Path to the file.
	 */
	private void openFile(String path) {
		openFile(new File(path));
	}

	/**
	 * Opens and loads the code from the given file.
	 * @param file The file to open.
	 */
	private void openFile(File file) {
		try {
			String code = "", line;

			try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"))) {
				while((line = reader.readLine()) != null)
					code += line + "\n";
			}

			txtCode.setText(code);
			txtCode.discardAllEdits();
			txtCode.setDirty(false);
			txtCode.clearErrorIcons();
			setOpenedFile(file);
			if(!mnuInternalWindows.isSelected()) tabCode.select();
			txtCode.requestFocus();
		}
		catch(Exception ex) {
			JOptionPane.showMessageDialog(this, Lang.t("error_opening_file", file.getName()) + "\n" + ex.getMessage(), AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
			LOG.log(Level.WARNING, "error opening file \"" + file.getName() + "\"", ex);
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
		codeFileChooser.setDialogTitle(Lang.t("save"));
		if(codeFileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File f = codeFileChooser.getSelectedFile();
			if(codeFileChooser.getFileFilter() == codeFileFilter && f.getName().lastIndexOf(".") == -1)
				f = new File(f.getPath() + ".asm"); // append extension if missing
			if(!f.exists() || JOptionPane.showConfirmDialog(this, Lang.t("confirm_replace", f.getName()), AppInfo.NAME, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)
				saveFile(f);
		}
	}

	/**
	 * Saves the code to the specified file.
	 * @param path Path to the file to save.
	 */
	private void saveFile(String path) {
		saveFile(new File(path));
	}

	/**
	 * Saves the code to the specified file.
	 * @param file File to save to.
	 */
	private void saveFile(File file) {
		try {
			BufferedWriter writer;
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
			writer.write(txtCode.getText());
			writer.close();
			setOpenedFile(file);
			txtCode.setDirty(false);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, Lang.t("error_saving_file", file.getName()) + "\n" + ex.getMessage(), AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
			LOG.log(Level.WARNING, "error saving file \"" + file.getName() + "\"", ex);
		}

	}

	/**
	 * Shows the print dialog to send the current file to the printer.
	 */
	private void printFile() {
		if(!txtCode.getText().isEmpty()) {
			try {
				// Copy the code to a hidden JTextArea (with white background,
				// black foreground) and print it
				txtPrint.setText(txtCode.getText());
				txtPrint.print();
			}
			catch(Exception ex) {
				JOptionPane.showMessageDialog(this, Lang.t("error_printing_file") + ": " + ex.getMessage(), AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
				LOG.log(Level.WARNING, "error printing file", ex);
			}
		}
	}

	/**
	 * Translates the form's strings.
	 */
	protected final void translate() {
		Lang.tButton(lbl, "file");
		Lang.tButton(mnuNew, "new");
		Lang.tButton(mnuOpen, "open");
		Lang.tButton(mnuOpenRecent, "open_recent");
		Lang.tButton(mnuOpenLastFileAtStartup, "open_last_file_at_startup");
		Lang.tButton(mnuSave, "save");
		Lang.tButton(mnuSaveAs, "save_as");
		Lang.tButton(mnuPrint, "print");
		Lang.tButton(mnuExit, "exit");
		Lang.tButton(mnuEdit, "edit");
		Lang.tButton(mnuUndo, "undo");
		Lang.tButton(mnuRedo, "redo");
		Lang.tButton(mnuCut, "cut");
		Lang.tButton(mnuCopy, "copy");
		Lang.tButton(mnuPaste, "paste");
		Lang.tButton(mnuSelectAll, "select_all");
		Lang.tButton(mnuFindReplace, "find_replace");
		Lang.tButton(mnuView, "view");
		Lang.tButton(mnuDatapath, "datapath");
		Lang.tButton(mnuPerformanceMode, "performance_mode");
		Lang.tButton(mnuControlPath, "control_path");
		Lang.tButton(mnuArrowsInWires, "arrows_in_wires");
		Lang.tButton(mnuOverlayed, "overlayed_data");
		Lang.tButton(mnuOverlayedData, "enable");
		Lang.tButton(mnuOverlayedShowNames, "show_names");
		Lang.tButton(mnuOverlayedShowForAll, "show_for_all_components");
		Lang.tButton(mnuInternalWindows, "internal_windows");
		Lang.tButton(mnuSwitchTheme, "dark_theme");
		Lang.tButton(mnuMarginLine, "show_margin_line");
		Lang.tButton(mnuWindows, "windows");
		Lang.tButton(mnuTileWindows, "tile");
		Lang.tButton(mnuCascadeWindows, "cascade");
		Lang.tButton(mnuOpenGL, "opengl_acceleration");
		Lang.tButton(mnuExecute, "execute");
		Lang.tButton(mnuAssemble, "assemble");
		Lang.tButton(mnuRestart, "restart");
		Lang.tButton(mnuBackStep, "back_step");
		Lang.tButton(mnuStep, "step");
		Lang.tButton(mnuRun, "run");
		Lang.tButton(mnuBreak, "add breakpoint");
		Lang.tButton(mnuZoomIn, "zoom_in");
		Lang.tButton(mnuZoomOut, "zoom_out");
		Lang.tButton(mnuZoomNormal, "normal");
		Lang.tButton(mnuZoomAutoAdjust, "adjust_automatically");
		Lang.tButton(mnuResetDataBeforeAssembling, "reset_data_before_assembling");
		Lang.tButton(mnuCPU, "cpu");
		Lang.tButton(mnuLoadCPU, "load");
		Lang.tButton(mnuLoadRecentCPU, "load_recent");
		Lang.tButton(mnuSupportedInstructions, "supported_instructions");
		Lang.tButton(mnuRestoreLatencies, "restore_latencies");
		Lang.tButton(mnuRemoveLatencies, "remove_latencies");
		Lang.tButton(mnuStatistics, "statistics");
		Lang.tButton(mnuLanguage, "language");
		Lang.tButton(mnuHelp, "help");
		Lang.tButton(mnuDocs, "documentation");
		Lang.tButton(mnuAbout, "about");

		Lang.tButton(mnuUndoP, "undo");
		Lang.tButton(mnuRedoP, "redo");
		Lang.tButton(mnuCutP, "cut");
		Lang.tButton(mnuCopyP, "copy");
		Lang.tButton(mnuPasteP, "paste");
		Lang.tButton(mnuSelectAllP, "select_all");
		Lang.tButton(mnuFindReplaceP, "find_replace");
		Lang.tButton(mnuSwitchSide, "switch_side");

		if(mnuInternalWindows.isSelected()) {
			frmCode.setTitle(Lang.t("code"));
			frmDatapath.setTitle(Lang.t("datapath"));
			frmRegisters.setTitle(Lang.t("registers"));
			frmAssembledCode.setTitle(Lang.t("assembled"));
			frmDataMemory.setTitle(Lang.t("data_memory"));
		}
		else {
			tabCode.setTitle(Lang.t("code"));
			tabDatapath.setTitle(Lang.t("datapath"));
			tabRegisters.setTitle(Lang.t("registers"));
			tabAssembledCode.setTitle(Lang.t("assembled"));
			tabDataMemory.setTitle(Lang.t("data_memory"));
		}
		cpuFileChooser = new JFileChooser(DrMIPS.path + File.separator + CPU.FILENAME_PATH);
		cpuFileChooser.setDialogTitle(Lang.t("load_cpu_from_file"));
		cpuFileChooser.setFileFilter(new FileNameExtensionFilter(Lang.t("cpu_files"), CPU.FILENAME_EXTENSION));
		codeFileChooser = new JFileChooser();
		codeFileChooser.setFileFilter(codeFileFilter = new FileNameExtensionFilter(Lang.t("assembly_files"), "asm", "s"));
		dlgFindReplace.translate();
		dlgSupportedInstructions.translate();
		dlgStatistics.translate();

		cmdNew.setToolTipText(Lang.t("new"));
		cmdOpen.setToolTipText(Lang.t("open"));
		cmdSave.setToolTipText(Lang.t("save"));
		cmdSaveAs.setToolTipText(Lang.t("save_as"));
		cmdAssemble.setToolTipText(Lang.t("assemble"));
		cmdRestart.setToolTipText(Lang.t("restart"));
		cmdBackStep.setToolTipText(Lang.t("back_step"));
		cmdStep.setToolTipText(Lang.t("step"));
		cmdRun.setToolTipText(Lang.t("run"));
		cmdStatistics.setToolTipText(Lang.t("statistics"));
		cmdSupportedInstructions.setToolTipText(Lang.t("supported_instructions"));
		cmdHelp.setToolTipText(Lang.t("documentation"));
		cmdZoomIn.setToolTipText(Lang.t("zoom_in"));
		cmdZoomOut.setToolTipText(Lang.t("zoom_out"));
		cmdZoomNormal.setToolTipText(Lang.t("normal"));
		chkZoomAutoAdjust.setToolTipText(Lang.t("adjust_automatically"));
		lblZoom.setText(Lang.t("zoom", Math.round(datapath.getScale() * 100) + "%"));

		updateCaretPosition();
		lblRegFormat.setText(Lang.t("format") + ":");
		lblDatapathDataFormat.setText(Lang.t("format") + ":");
		lblDatapathPerformance.setText(Lang.t("performance") + ":");
		lblAssembledCodeFormat.setText(Lang.t("format") + ":");
		lblDataMemoryFormat.setText(Lang.t("format") + ":");
		lblFile.setText(Lang.t("file") + ":");

		initFormatComboBox(cmbRegFormat, DrMIPS.REGISTER_FORMAT_PREF, DrMIPS.DEFAULT_REGISTER_FORMAT);
		initFormatComboBox(cmbDatapathDataFormat, DrMIPS.DATAPATH_DATA_FORMAT_PREF, DrMIPS.DEFAULT_DATAPATH_DATA_FORMAT);
		initFormatComboBox(cmbAssembledCodeFormat, DrMIPS.ASSEMBLED_CODE_FORMAT_PREF, DrMIPS.DEFAULT_ASSEMBLED_CODE_FORMAT);
		initFormatComboBox(cmbDataMemoryFormat, DrMIPS.DATA_MEMORY_FORMAT_PREF, DrMIPS.DEFAULT_DATA_MEMORY_FORMAT);
		initPerformanceComboBox();

		datapath.translate(cmbDatapathDataFormat.getSelectedIndex());
		tblAssembledCode.translate();
		tblRegisters.translate();
		tblDataMemory.translate();
		txtCode.translate();
		refreshDatapathHelp();
		repaint();

		JMenuItem mnu;
		for(int i = 0; i < mnuLanguage.getItemCount(); i++) {
			mnu = mnuLanguage.getItem(i);
			mnu.setText(Lang.getDisplayName(mnu.getToolTipText()));
		}
	}

	/**
	 * Fills language menu with the available languages.
	 */
	private void fillLanguages() {
		JRadioButtonMenuItem mnu;

		for(String lang: Lang.getAvailableLanguages()) {
			mnu = new JRadioButtonMenuItem(Lang.getDisplayName(lang), lang.equals(Lang.getLanguage()));
			mnu.addActionListener(new LanguageSelectedListener(lang));
			mnu.setToolTipText(lang); // used to identify the language in translate()
			grpLanguages.add(mnu);
			mnuLanguage.add(mnu);
		}
	}

	/**
	 * Initializes/translates the specified data format selection combo box.
	 * @param cmb The combo box.
	 * @param formatPref The name of the preference for the saved previous format.
	 * @param defaultFormat The default data format.
	 */
	private void initFormatComboBox(JComboBox cmb, String formatPref, int defaultFormat) {
		if(cmb.getSelectedIndex() >= 0)
			DrMIPS.prefs.putInt(formatPref, cmb.getSelectedIndex());
		cmb.removeAllItems();
		cmb.addItem(Lang.t("binary"));
		cmb.addItem(Lang.t("decimal"));
		cmb.addItem(Lang.t("hexadecimal"));
		cmb.setSelectedIndex(DrMIPS.prefs.getInt(formatPref, defaultFormat));
	}

	/**
	 * Initializes/translates the performance mode type selection combo box.
	 */
	private void initPerformanceComboBox() {
		if(cmbDatapathPerformance.getSelectedIndex() >= 0)
			DrMIPS.prefs.putInt(DrMIPS.PERFORMANCE_TYPE_PREF, cmbDatapathPerformance.getSelectedIndex());
		cmbDatapathPerformance.removeAllItems();
		cmbDatapathPerformance.addItem(Lang.t("instruction"));
		cmbDatapathPerformance.addItem(Lang.t("cpu"));
		cmbDatapathPerformance.setSelectedIndex(DrMIPS.prefs.getInt(DrMIPS.PERFORMANCE_TYPE_PREF, DrMIPS.DEFAULT_PERFORMANCE_TYPE));
	}

	/**
	 * Terminates the program.
	 */
	private void exit() {
		boolean exit = true;
		if(txtCode != null && txtCode.isDirty()) { // file changed?
			int opt = JOptionPane.showConfirmDialog(this, Lang.t("code_changed"), AppInfo.NAME, JOptionPane.YES_NO_CANCEL_OPTION);
			switch(opt) {
				case JOptionPane.YES_OPTION:
					exit = true;
					saveFile();
					break;
				case JOptionPane.NO_OPTION: exit = true; break;
				case JOptionPane.CANCEL_OPTION: exit = false; break;
			}
		}

		if(exit) {
			// Save some preferences
			DrMIPS.prefs.putInt(DrMIPS.REGISTER_FORMAT_PREF, cmbRegFormat.getSelectedIndex());
			DrMIPS.prefs.putInt(DrMIPS.DATAPATH_DATA_FORMAT_PREF, cmbDatapathDataFormat.getSelectedIndex());
			DrMIPS.prefs.putInt(DrMIPS.ASSEMBLED_CODE_FORMAT_PREF, cmbAssembledCodeFormat.getSelectedIndex());
			DrMIPS.prefs.putInt(DrMIPS.DATA_MEMORY_FORMAT_PREF, cmbDataMemoryFormat.getSelectedIndex());
			DrMIPS.prefs.putInt(DrMIPS.PERFORMANCE_TYPE_PREF, cmbDatapathPerformance.getSelectedIndex());
			DrMIPS.prefs.putBoolean(DrMIPS.ASSEMBLE_RESET_PREF, mnuResetDataBeforeAssembling.isSelected());
			boolean maximized = getExtendedState() == MAXIMIZED_BOTH;
			DrMIPS.prefs.putBoolean(DrMIPS.MAXIMIZED_PREF, maximized);
			if(!maximized) {
				DrMIPS.prefs.putInt(DrMIPS.WIDTH_PREF, getWidth());
				DrMIPS.prefs.putInt(DrMIPS.HEIGHT_PREF, getHeight());
			}

			if(mnuInternalWindows.isSelected()) {
				saveFrameBounds("code", frmCode);
				saveFrameBounds("datapath", frmDatapath);
				saveFrameBounds("registers", frmRegisters);
				saveFrameBounds("assembled_code", frmAssembledCode);
				saveFrameBounds("data_memory", frmDataMemory);
			}
			else
				DrMIPS.prefs.putInt(DrMIPS.DIVIDER_LOCATION_PREF, pnlSplit.getDividerLocation());

			System.exit(0);
		}
	}

	/**
	 * Enables of disables the simulation controls.
	 * @param enabled Whether to enable or disable the controls.
	 */
	private void setSimulationControlsEnabled(boolean enabled) {
		if(!enabled) {
			mnuBackStep.setEnabled(false);
			mnuRestart.setEnabled(false);
			mnuStep.setEnabled(false);
			mnuRun.setEnabled(false);
			mnuBreak.setEnabled(false);
			cmdBackStep.setEnabled(false);
			cmdRestart.setEnabled(false);
			cmdStep.setEnabled(false);
			cmdRun.setEnabled(false);
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
		boolean enable = !cpu.isProgramFinished();
		mnuStep.setEnabled(enable);
		mnuRun.setEnabled(enable);
		mnuBreak.setEnabled(enable);
		cmdStep.setEnabled(enable);
		cmdRun.setEnabled(enable);
	}

	/**
	 * Sets the "step back" controls enabled or disabled according to <tt>cpu.hasPreviousCycle()</tt>.
	 */
	private void updateStepBackEnabled() {
		boolean enable = cpu.hasPreviousCycle();
		mnuBackStep.setEnabled(enable);
		mnuRestart.setEnabled(enable);
		cmdBackStep.setEnabled(enable);
		cmdRestart.setEnabled(enable);
	}

	/**
	 * Loads the CPU from the specified file.
	 * @param path Path to the CPU file.
	 */
	private void loadCPU(String path) throws IOException, JSONException, InvalidCPUException, ArrayIndexOutOfBoundsException, InvalidInstructionSetException, NumberFormatException {
		setSimulationControlsEnabled(false);
		cpu = CPU.createFromJSONFile(path); // load CPU from file
		cpu.setPerformanceInstructionDependent(cmbDatapathPerformance.getSelectedIndex() == Util.INSTRUCTION_PERFORMANCE_TYPE_INDEX);
		DrMIPS.prefs.put(DrMIPS.LAST_CPU_PREF, path); // save CPU path in preferences
		tblRegisters.setCPU(cpu, datapath, tblExec, cmbRegFormat.getSelectedIndex()); // display the CPU's register table
		datapath.setCPU(cpu); // display datapath in the respective tab
		tblAssembledCode.setCPU(cpu, cmbAssembledCodeFormat.getSelectedIndex()); // display assembled code in the respective tab
		tblDataMemory.setCPU(cpu, datapath, cmbDataMemoryFormat.getSelectedIndex()); // display data memory in the respective tab
		tblExec.setCPU(cpu, cmbDatapathDataFormat.getSelectedIndex());
		lblFileName.setText(cpu.getFile().getName());
		lblFileName.setToolTipText(cpu.getFile().getAbsolutePath());
		dlgSupportedInstructions.setCPU(cpu);
		addRecentCPU(new File(path));
		txtCode.setCPU(cpu);
		datapath.setControlPathVisible(mnuControlPath.isSelected());
		datapath.setShowArrows(mnuArrowsInWires.isSelected());
		datapath.setPerformanceMode(mnuPerformanceMode.isSelected());
		repaint();
		if(mnuZoomAutoAdjust.isSelected()) {
			datapath.scaleToFitPanel(datapathScroll.getSize());
			updateZoomStatus();
		}
	}

	/**
	 * Loads the last used CPU file or the default one.
	 */
	@SuppressWarnings("UseSpecificCatch")
	private void loadFirstCPU() {
		try { // try to load the CPU in the preferences
			loadCPU(DrMIPS.prefs.get(DrMIPS.LAST_CPU_PREF, DrMIPS.path + File.separator + DrMIPS.DEFAULT_CPU));
		} catch (Throwable ex) {
			try { // fallback to the default CPU on error
				loadCPU(DrMIPS.path + File.separator + DrMIPS.DEFAULT_CPU);
				DrMIPS.prefs.put(DrMIPS.LAST_CPU_PREF, DrMIPS.path + File.separator + DrMIPS.DEFAULT_CPU);
			} catch (Throwable e) { // error on the default CPU too
				JOptionPane.showMessageDialog(this, Lang.t("invalid_file") + "\n" + ex.getClass().getName() + " (" + ex.getMessage() + ")", AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
				LOG.log(Level.SEVERE, "error loading CPU", e);
				System.exit(2);
			}
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

		tblRegisters.refreshValues(cmbRegFormat.getSelectedIndex());
		tblDataMemory.refreshValues(cmbDataMemoryFormat.getSelectedIndex());
		tblAssembledCode.refreshValues();
		tblExec.refresh(cmbDatapathDataFormat.getSelectedIndex());
		datapath.refresh();

		// Scroll the assembled code table to the current instruction
		int index = cpu.getPC().getCurrentInstructionIndex();
		if(index >= 0)
			tblAssembledCode.scrollRectToVisible(tblAssembledCode.getCellRect(index, 0, true));
	}

	/**
	 * Executes a clock cycle in the CPU and displays the results in the GUI.
	 */
	private void step() {
		cpu.executeCycle();
		refreshValues();
	}

	/**
	 * Reverts the execution to the previous clock cycle.
	 */
	private void backStep() {
		cpu.restorePreviousCycle();
		refreshValues();
	}

	/**
	 * Reverts the execution to the first clock cycle.
	 */
	private void restart() {
		cpu.resetToFirstCycle();
		refreshValues();
	}

	/**
	 * Executes all the instructions at once.
	 */
	private void run() {
		try {
			cpu.executeAll();
		}
		catch(InfiniteLoopException e) {
			JOptionPane.showMessageDialog(this, Lang.t("possible_infinite_loop", CPU.EXECUTE_ALL_LIMIT_CYCLES), AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
		}
		refreshValues();
	}

	/**
	 * Add a breakpoint.
	 */
	private void addBreakpoint() {

		String inputValue = JOptionPane.showInputDialog(this, "Set breakpoint address (empty for disable)", AppInfo.NAME, JOptionPane.QUESTION_MESSAGE);
		if (inputValue == "") {
			cpu.setBreakpointAddr(-1);
			return;
		}

		boolean err = false;

		try {
			int addr = Integer.parseInt(inputValue);
			if ((addr < 0) || ((addr % 4) != 0)) {
				err = true;
			}
			else {
				cpu.setBreakpointAddr(addr);
			}
		}
		catch(NumberFormatException ex) {
			err = true;
		}

		if (err)
			JOptionPane.showMessageDialog(this, "Breakpoint address must positive and a multiple of 4", AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Assembles and loads the code from the Code tab.
	 */
	private void assemble() {
		txtCode.clearErrorIcons();
		if(mnuResetDataBeforeAssembling.isSelected()) cpu.resetData();
		try {
			cpu.assembleCode(txtCode.getText());
			setSimulationControlsEnabled(true);
			tblAssembledCode.refresh(cmbAssembledCodeFormat.getSelectedIndex());
			refreshValues();
			if(!mnuInternalWindows.isSelected())
				tabAssembledCode.select();
		}
		catch(SyntaxErrorException ex) {
			String message = getTranslatedSyntaxErrorMessage(ex);

			if(!ex.hasOtherErrors())
				txtCode.addErrorIcon(ex.getLine(), message);
			else {
				for(SyntaxErrorException e: ex.getOtherErrors())
					txtCode.addErrorIcon(e.getLine(), getTranslatedSyntaxErrorMessage(e));
			}

			JOptionPane.showMessageDialog(this, message, AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Returns the translated message for the given syntax error exception.
	 * @param ex The exception.
	 * @return Translated error message.
	 */
	private String getTranslatedSyntaxErrorMessage(SyntaxErrorException ex) {
		String message = Lang.t("line", ex.getLine()) + ": ";
		switch(ex.getType()) {
			case DUPLICATED_LABEL: message += Lang.t("duplicated_label", ex.getExtra()); break;
			case INVALID_DATA_ARG: message += Lang.t("invalid_arg_data", ex.getExtra()); break;
			case INVALID_INT_ARG: message += Lang.t("invalid_arg_int", ex.getExtra()); break;
			case INVALID_LABEL: message += Lang.t("invalid_label", ex.getExtra()); break;
			case INVALID_REG_ARG: message += Lang.t("invalid_arg_reg", ex.getExtra()); break;
			case UNKNOWN_DATA_DIRECTIVE: message += Lang.t("unknown_data_directive", ex.getExtra()); break;
			case UNKNOWN_INSTRUCTION: message += Lang.t("unknown_instruction", ex.getExtra()); break;
			case UNKNOWN_LABEL: message += Lang.t("unknown_label", ex.getExtra()); break;
			case WRONG_NUMBER_OF_ARGUMENTS: message += Lang.t("wrong_no_args", ex.getExtra(), ex.getExtra2()); break;
			case INVALID_POSITIVE_INT_ARG: message += Lang.t("invalid_arg_positive_int", ex.getExtra()); break;
			case DATA_SEGMENT_WITHOUT_DATA_MEMORY: message += Lang.t("data_segment_without_data_memory"); break;
			default: message = ex.getMessage();
		}
		return message;
	}

	/**
	 * Finds the specified string in the code editor.
	 * @param str The string to find.
	 * @param matchCase If the search is case-sensitive.
	 * @param forward Whether to search forwards or backwards.
	 */
	protected void find(String str, boolean matchCase, boolean forward) {
		if(!str.isEmpty()) {
			clearFind();

			SearchContext context = new SearchContext();
			context.setSearchFor(str);
			context.setMatchCase(matchCase);
			context.setRegularExpression(false);
			context.setSearchForward(forward);
			context.setWholeWord(false);

			SearchEngine.find(txtCode, context);
			txtCode.markAll(str, matchCase, false, false);
		}
	}

	/**
	 * Replaces the next occurrence of the given string by another string.
	 * @param str The string to find.
	 * @param by The string to replace with.
	 * @param matchCase If the search is case-sensitive.
	 * @param forward Whether to search forwards or backwards.
	 */
	protected void replace(String str, String by, boolean matchCase, boolean forward) {
		if(!str.isEmpty()) {
			clearFind();

			SearchContext context = new SearchContext();
			context.setSearchFor(str);
			context.setReplaceWith(by);
			context.setMatchCase(matchCase);
			context.setRegularExpression(false);
			context.setSearchForward(forward);
			context.setWholeWord(false);

			SearchEngine.replace(txtCode, context);
			txtCode.markAll(str, matchCase, false, false);
		}
	}

	/**
	 * Replaces all occurrence of the given string by another string.
	 * @param str The string to find.
	 * @param by The string to replace with.
	 * @param matchCase If the search is case-sensitive.
	 * @param forward Whether to search forwards or backwards.
	 */
	protected void replaceAll(String str, String by, boolean matchCase, boolean forward) {
		if(!str.isEmpty()) {
			clearFind();

			SearchContext context = new SearchContext();
			context.setSearchFor(str);
			context.setReplaceWith(by);
			context.setMatchCase(matchCase);
			context.setRegularExpression(false);
			context.setSearchForward(forward);
			context.setWholeWord(false);

			SearchEngine.replaceAll(txtCode, context);
		}
	}

	/**
	 * Clears all highlights created by find.
	 */
	protected void clearFind() {
		txtCode.clearMarkAllHighlights();
	}

	/**
	 * Puts each tab in the right side, based on the preferences.
	 */
	private void refreshTabSides() {
		pnlLeft.removeAll();
		pnlRight.removeAll();

		tabCode = new Tab(pnlCode, Lang.t("code"), DrMIPS.prefs.getInt(DrMIPS.CODE_TAB_SIDE_PREF, DrMIPS.DEFAULT_CODE_TAB_SIDE));
		tabAssembledCode = new Tab(pnlAssembledCode, Lang.t("assembled"), DrMIPS.prefs.getInt(DrMIPS.ASSEMBLED_CODE_TAB_SIDE_PREF, DrMIPS.DEFAULT_ASSEMBLED_CODE_TAB_SIDE));
		tabDatapath = new Tab(pnlDatapath, Lang.t("datapath"), DrMIPS.prefs.getInt(DrMIPS.DATAPATH_TAB_SIDE_PREF, DrMIPS.DEFAULT_DATAPATH_TAB_SIDE));
		tabRegisters = new Tab(pnlRegisters, Lang.t("registers"), DrMIPS.prefs.getInt(DrMIPS.REGISTERS_TAB_SIDE_PREF, DrMIPS.DEFAULT_REGISTERS_TAB_SIDE));
		tabDataMemory = new Tab(pnlDataMemory, Lang.t("data_memory"), DrMIPS.prefs.getInt(DrMIPS.DATA_MEMORY_TAB_SIDE_PREF, DrMIPS.DEFAULT_DATA_MEMORY_TAB_SIDE));
		repaint();
	}

	/**
	 * Returns the selected tab in the specified side.
	 * @param side The side of the tab.
	 * @return The selected tab.
	 */
	private Tab getSelectedTab(int side) {
		int index = (side == Util.LEFT) ? pnlLeft.getSelectedIndex() : pnlRight.getSelectedIndex();
		return getTab(side, index);
	}

	/**
	 * Returns the tab in the given side and index.
	 * @param side Side of the tab.
	 * @param index Index of the tab.
	 * @return The desired tab, or <tt>null</tt> if non-existant.
	 */
	private Tab getTab(int side, int index) {
		Tab[] tabs = new Tab[] {tabCode, tabAssembledCode, tabDatapath, tabRegisters, tabDataMemory};
		for(Tab t: tabs) {
			if(t.getSide() == side && t.getIndex() == index)
				return t;
		}
		return null;
	}

	/**
	 * Switches the current theme.
	 */
	private void switchTheme() {
		boolean currentDark = DrMIPS.prefs.getBoolean(DrMIPS.DARK_THEME_PREF, DrMIPS.DEFAULT_DARK_THEME);
		boolean dark = mnuSwitchTheme.isSelected();
		if(dark && !currentDark)
			Util.setDarkLookAndFeel();
		else if(!dark && currentDark)
			Util.setLightLookAndFeel();

		DrMIPS.prefs.putBoolean(DrMIPS.DARK_THEME_PREF, dark);
		SwingUtilities.updateComponentTreeUI(this);
		if(dlgFindReplace != null) SwingUtilities.updateComponentTreeUI(dlgFindReplace);
		if(dlgSupportedInstructions != null) SwingUtilities.updateComponentTreeUI(dlgSupportedInstructions);
		if(dlgStatistics != null) SwingUtilities.updateComponentTreeUI(dlgStatistics);
		if(cpuFileChooser != null) cpuFileChooser.updateUI();
		if(codeFileChooser != null) codeFileChooser.updateUI();
		datapath.setCPU(cpu);
		datapath.setControlPathVisible(mnuControlPath.isSelected());
		datapath.setShowArrows(mnuArrowsInWires.isSelected());
		datapath.setPerformanceMode(mnuPerformanceMode.isSelected());
		refreshDatapathHelp();
		txtCode.setColors();
	}

	/**
	 * If using tabs switch to use internal frames and vice-versa.
	 */
	private void switchUseInternalWindows() {
		boolean currentWindows = DrMIPS.prefs.getBoolean(DrMIPS.INTERNAL_WINDOWS_PREF, DrMIPS.DEFAULT_INTERNAL_WINDOWS);
		boolean windows = mnuInternalWindows.isSelected();
		DrMIPS.prefs.putBoolean(DrMIPS.INTERNAL_WINDOWS_PREF, windows);

		if(windows && !currentWindows) // use internal frames
			switchToInternalWindows();
		else if(!windows && currentWindows) // use tabs
			switchToTabs();
	}

	/**
	 * Switches to use internal windows.
	 */
	private void switchToInternalWindows() {
		DrMIPS.prefs.putInt(DrMIPS.DIVIDER_LOCATION_PREF, pnlSplit.getDividerLocation());

		remove(pnlSplit);
		add(desktop, BorderLayout.CENTER);
		frmCode = desktop.add(Lang.t("code"), pnlCode, false);
		frmAssembledCode = desktop.add(Lang.t("assembled"), pnlAssembledCode, false);
		frmDatapath = desktop.add(Lang.t("datapath"), pnlDatapath, false);
		frmRegisters = desktop.add(Lang.t("registers"), pnlRegisters, false);
		frmDataMemory = desktop.add(Lang.t("data_memory"), pnlDataMemory, false);

		restoreFrameBounds("code", frmCode);
		restoreFrameBounds("assembled_code", frmAssembledCode);
		restoreFrameBounds("datapath", frmDatapath);
		restoreFrameBounds("registers", frmRegisters);
		restoreFrameBounds("data_memory", frmDataMemory);

		mnuWindows.setEnabled(true);
		SwingUtilities.updateComponentTreeUI(this);
		txtCode.requestFocus(); // make sure a component has focus so shortcut keys work
	}

	/**
	 * Switches to use tabs.
	 */
	private void switchToTabs() {
		saveFrameBounds("code", frmCode);
		saveFrameBounds("assembled_code", frmAssembledCode);
		saveFrameBounds("datapath", frmDatapath);
		saveFrameBounds("registers", frmRegisters);
		saveFrameBounds("data_memory", frmDataMemory);

		remove(desktop);
		add(pnlSplit, BorderLayout.CENTER);
		refreshTabSides();

		desktop.remove(frmCode);
		desktop.remove(frmAssembledCode);
		desktop.remove(frmDatapath);
		desktop.remove(frmRegisters);
		desktop.remove(frmDataMemory);

		mnuWindows.setEnabled(false);
		SwingUtilities.updateComponentTreeUI(this);
		txtCode.requestFocus(); // make sure a component has focus so shortcut keys work
	}

	/**
	 * Saves the specified frame's bounds to the preferences.
	 * @param prefPrefix The prefix of the preference.
	 * @param frame The frame.
	 */
	private void saveFrameBounds(String prefPrefix, JInternalFrame frame) {
		DrMIPS.prefs.putInt(prefPrefix + "_x", frame.getNormalBounds().x);
		DrMIPS.prefs.putInt(prefPrefix + "_y", frame.getNormalBounds().y);
		DrMIPS.prefs.putInt(prefPrefix + "_w", frame.getNormalBounds().width);
		DrMIPS.prefs.putInt(prefPrefix + "_h", frame.getNormalBounds().height);
		DrMIPS.prefs.putBoolean(prefPrefix + "_max", frame.isMaximum());
		DrMIPS.prefs.putBoolean(prefPrefix + "_min", frame.isIcon());
	}

	/**
	 * Restores the specified frame's bounds from the preferences.
	 * @param prefPrefix The prefix of the preference.
	 * @param frame The frame.
	 */
	private void restoreFrameBounds(String prefPrefix, JInternalFrame frame) {
		frame.setLocation(DrMIPS.prefs.getInt(prefPrefix + "_x", 0), DrMIPS.prefs.getInt(prefPrefix + "_y", 0));
		int w = DrMIPS.prefs.getInt(prefPrefix + "_w", -1);
		int h = DrMIPS.prefs.getInt(prefPrefix + "_h", -1);
		if(w > 0 && h > 0)
			frame.setSize(w, h);
		else
			frame.pack();
		try {
			if(DrMIPS.prefs.getBoolean(prefPrefix + "_max", false))
				frame.setMaximum(true);
			if(DrMIPS.prefs.getBoolean(prefPrefix + "_min", false))
				frame.setIcon(true);
		}
		catch(PropertyVetoException ex) {
			LOG.log(Level.WARNING, "failed to maximize/minimize an internal frame");
		}
	}

	/**
	 * Refreshes the datapath help tooltip.
	 */
	private void refreshDatapathHelp() {
		String tip = "<html><b><u>";

		if(datapath.isInPerformanceMode()) {
			tip += Lang.t("performance_mode") + "</u></b><br /><br />";
			tip += "<span style='color: " + Util.colorToRGBString(Util.wireColor) + "'>- " + Lang.t("normal_wire") + "</span><br />";
			tip += "<span style='color: " + Util.colorToRGBString(Util.controlPathColor) + "'>- " + Lang.t("control_path_wire") + "</span><br />";
			tip += "<span style='color: " + Util.colorToRGBString(Util.criticalPathColor) + "'>- " + Lang.t("wire_in_critical_path") + "</span><br />";
			tip += "<span style='color: " + Util.colorToRGBString(Util.irrelevantColor) + "'>- " + Lang.t("irrelevant_wire") + "</span><br /><br />";
			tip += Lang.t("advised_to_display_control_path");
		}
		else {
			tip += Lang.t("data_mode") + "</u></b><br /><br />";
			tip += "<span style='color: " + Util.colorToRGBString(Util.wireColor) + "'>- " + Lang.t("normal_wire") + "</span><br />";
			tip += "<span style='color: " + Util.colorToRGBString(Util.controlPathColor) + "'>- " + Lang.t("control_path_wire") + "</span><br />";
			tip += "<span style='color: " + Util.colorToRGBString(Util.irrelevantColor) + "'>- " + Lang.t("irrelevant_wire") + "</span>";
		}

		tip += "</html>";
		lblDatapathHelp.setToolTipText(tip);
	}

	/**
	 * Opens the documentation directory.
	 */
	public void openDocDir() {
		File docDir = new File(DrMIPS.path + File.separator + DrMIPS.DOC_DIR);
		File docDir2 = new File(DrMIPS.DOC_DIR2);
		File dir;

		if(docDir.isDirectory())
			dir = docDir;
		else if(docDir2.isDirectory())
			dir = docDir2;
		else {
			JOptionPane.showMessageDialog(this, Lang.t("doc_dir_not_found"), AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
			return;
		}

		if(!Desktop.isDesktopSupported()) {
			LOG.log(Level.WARNING, "Desktop class is not supported in this system");
			JOptionPane.showMessageDialog(this, Lang.t("failed_to_open_doc_folder", dir.getAbsolutePath()), AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
			return;
		}

		if(!openDocIndex(dir)) { // try to open index.html
			// if failed, try to open the directory
			try {
				Desktop.getDesktop().open(dir);
			}
			catch(Exception ex) {
				LOG.log(Level.WARNING, "error opening doc folder", ex);
				JOptionPane.showMessageDialog(this, Lang.t("failed_to_open_doc_folder", dir.getAbsolutePath()), AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Tries to open the manual's index.html in a browser.
	 * @param docDir Documentation directory.
	 * @return Whether the operation was successfully.
	 */
	private boolean openDocIndex(File docDir) {
		try {
			File index = new File(docDir.getPath() + File.separator + "index.html");
			if(index.isFile()) {
				Desktop.getDesktop().browse(index.toURI());
				return true;
			} else {
				return false;
			}
		} catch(Exception ex) {
			return false;
		}
	}

	/**
	 * Refreshes the statistics dialog.
	 */
	public void refreshStatistics() {
		dlgStatistics.refresh(cpu);
	}

	/**
	 * Updates the caret position displayed in the status bar of the code editor.
	 */
	public void updateCaretPosition() {
		int line = txtCode.getCaretLineNumber() + 1;
		int col = txtCode.getCaretOffsetFromLineStart() + 1;
		lblCaretPosition.setText(Lang.t("line", line) + ", " + Lang.t("column", col));
	}

	/**
	 * Zooms the datapath in.
	 */
	private void zoomIn() {
		datapath.increaseScale();
		updateZoomStatus();
		DrMIPS.prefs.putDouble(DrMIPS.SCALE_PREF, datapath.getScale());
		if(!mnuInternalWindows.isSelected())
			tabDatapath.select();
	}

	/**
	 * Zooms the datapath out.
	 */
	private void zoomOut() {
		datapath.decreaseScale();
		updateZoomStatus();
		DrMIPS.prefs.putDouble(DrMIPS.SCALE_PREF, datapath.getScale());
		if(!mnuInternalWindows.isSelected())
			tabDatapath.select();
	}

	/**
	 * Sets the datapath zoom level to normal.
	 */
	private void zoomNormal() {
		datapath.restoreDefaultScale();
		updateZoomStatus();
		DrMIPS.prefs.putDouble(DrMIPS.SCALE_PREF, datapath.getScale());
		if(!mnuInternalWindows.isSelected())
			tabDatapath.select();
	}

	/**
	 * Switches the "auto zoom" state.
	 */
	private void switchZoomAuto() {
		boolean auto = !DrMIPS.prefs.getBoolean(DrMIPS.AUTO_SCALE_PREF, DrMIPS.DEFAULT_AUTO_SCALE);
		switchZoomAuto(auto);
		if(!mnuInternalWindows.isSelected())
			tabDatapath.select();
	}

	/**
	 * Enables or disables auto zoom.
	 * @param auto Whether to enable or disable auto zoom.
	 */
	private void switchZoomAuto(boolean auto) {
		DrMIPS.prefs.putBoolean(DrMIPS.AUTO_SCALE_PREF, auto);
		if(auto)
			datapath.scaleToFitPanel(datapathScroll.getSize());
		else
			datapath.setScale(DrMIPS.prefs.getDouble(DrMIPS.SCALE_PREF, DrMIPS.DEFAULT_SCALE));
		updateZoomStatus();
	}

	/**
	 * Updates the enabled/selected state of zoom controls and the zoom level label.
	 */
	private void updateZoomStatus() {
		boolean auto = DrMIPS.prefs.getBoolean(DrMIPS.AUTO_SCALE_PREF, DrMIPS.DEFAULT_AUTO_SCALE);
		mnuZoomAutoAdjust.setSelected(auto);
		chkZoomAutoAdjust.setSelected(auto);
		mnuZoomIn.setEnabled(!auto && datapath.canIncreaseScale());
		mnuZoomOut.setEnabled(!auto && datapath.canDecreaseScale());
		mnuZoomNormal.setEnabled(!auto && !datapath.isDefaultScale());
		cmdZoomIn.setEnabled(!auto && mnuZoomIn.isEnabled());
		cmdZoomOut.setEnabled(!auto && mnuZoomOut.isEnabled());
		cmdZoomNormal.setEnabled(!auto && mnuZoomNormal.isEnabled());
		lblZoom.setText(Lang.t("zoom", Math.round(datapath.getScale() * 100) + "%"));
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton chkZoomAutoAdjust;
    private javax.swing.JComboBox cmbAssembledCodeFormat;
    private javax.swing.JComboBox cmbDataMemoryFormat;
    private javax.swing.JComboBox cmbDatapathDataFormat;
    private javax.swing.JComboBox cmbDatapathPerformance;
    private javax.swing.JComboBox cmbRegFormat;
    private javax.swing.JButton cmdAssemble;
    private javax.swing.JButton cmdBackStep;
    private javax.swing.JButton cmdHelp;
    private javax.swing.JButton cmdNew;
    private javax.swing.JButton cmdOpen;
    private javax.swing.JButton cmdRestart;
    private javax.swing.JButton cmdRun;
    private javax.swing.JButton cmdSave;
    private javax.swing.JButton cmdSaveAs;
    private javax.swing.JButton cmdStatistics;
    private javax.swing.JButton cmdStep;
    private javax.swing.JButton cmdSupportedInstructions;
    private javax.swing.JButton cmdZoomIn;
    private javax.swing.JButton cmdZoomNormal;
    private javax.swing.JButton cmdZoomOut;
    private brunonova.drmips.pc.DatapathPanel datapath;
    private javax.swing.JScrollPane datapathScroll;
    private org.jscroll.JScrollDesktopPane desktop;
    private javax.swing.ButtonGroup grpLanguages;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JToolBar.Separator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JPopupMenu.Separator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator16;
    private javax.swing.JPopupMenu.Separator jSeparator17;
    private javax.swing.JToolBar.Separator jSeparator18;
    private javax.swing.JToolBar.Separator jSeparator19;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JMenu lbl;
    private javax.swing.JLabel lblAssembledCodeFormat;
    private javax.swing.JLabel lblCaretPosition;
    private javax.swing.JLabel lblDataMemoryFormat;
    private javax.swing.JLabel lblDatapathDataFormat;
    private javax.swing.JLabel lblDatapathHelp;
    private javax.swing.JLabel lblDatapathPerformance;
    private javax.swing.JLabel lblFile;
    private javax.swing.JLabel lblFileName;
    private javax.swing.JLabel lblRegFormat;
    private javax.swing.JLabel lblZoom;
    private javax.swing.JMenuItem mnuAbout;
    private javax.swing.JCheckBoxMenuItem mnuArrowsInWires;
    private javax.swing.JMenuItem mnuAssemble;
    private javax.swing.JMenuItem mnuBackStep;
    private javax.swing.JMenuBar mnuBar;
    private javax.swing.JMenu mnuCPU;
    private javax.swing.JMenuItem mnuCascadeWindows;
    private javax.swing.JCheckBoxMenuItem mnuControlPath;
    private javax.swing.JMenuItem mnuCopy;
    private javax.swing.JMenuItem mnuCopyP;
    private javax.swing.JMenuItem mnuCut;
    private javax.swing.JMenuItem mnuCutP;
    private javax.swing.JMenu mnuDatapath;
    private javax.swing.JMenuItem mnuDocs;
    private javax.swing.JMenu mnuEdit;
    private javax.swing.JPopupMenu mnuEditP;
    private javax.swing.JMenu mnuExecute;
    private javax.swing.JMenuItem mnuExit;
    private javax.swing.JMenuItem mnuFindReplace;
    private javax.swing.JMenuItem mnuFindReplaceP;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JCheckBoxMenuItem mnuInternalWindows;
    private javax.swing.JMenu mnuLanguage;
    private javax.swing.JMenuItem mnuLoadCPU;
    private javax.swing.JMenu mnuLoadRecentCPU;
    private javax.swing.JCheckBoxMenuItem mnuMarginLine;
    private javax.swing.JMenuItem mnuNew;
    private javax.swing.JMenuItem mnuOpen;
    private javax.swing.JCheckBoxMenuItem mnuOpenGL;
    private javax.swing.JCheckBoxMenuItem mnuOpenLastFileAtStartup;
    private javax.swing.JMenu mnuOpenRecent;
    private javax.swing.JMenu mnuOverlayed;
    private javax.swing.JCheckBoxMenuItem mnuOverlayedData;
    private javax.swing.JCheckBoxMenuItem mnuOverlayedShowForAll;
    private javax.swing.JCheckBoxMenuItem mnuOverlayedShowNames;
    private javax.swing.JMenuItem mnuPaste;
    private javax.swing.JMenuItem mnuPasteP;
    private javax.swing.JCheckBoxMenuItem mnuPerformanceMode;
    private javax.swing.JMenuItem mnuPrint;
    private javax.swing.JMenuItem mnuRedo;
    private javax.swing.JMenuItem mnuRedoP;
    private javax.swing.JMenuItem mnuRemoveLatencies;
    private javax.swing.JCheckBoxMenuItem mnuResetDataBeforeAssembling;
    private javax.swing.JMenuItem mnuRestart;
    private javax.swing.JMenuItem mnuRestoreLatencies;
    private javax.swing.JMenuItem mnuRun;
    private javax.swing.JMenuItem mnuBreak;
    private javax.swing.JMenuItem mnuSave;
    private javax.swing.JMenuItem mnuSaveAs;
    private javax.swing.JMenuItem mnuSelectAll;
    private javax.swing.JMenuItem mnuSelectAllP;
    private javax.swing.JMenuItem mnuStatistics;
    private javax.swing.JMenuItem mnuStep;
    private javax.swing.JMenuItem mnuSupportedInstructions;
    private javax.swing.JMenuItem mnuSwitchSide;
    private javax.swing.JCheckBoxMenuItem mnuSwitchTheme;
    private javax.swing.JPopupMenu mnuTabSide;
    private javax.swing.JMenuItem mnuTileWindows;
    private javax.swing.JMenuItem mnuUndo;
    private javax.swing.JMenuItem mnuUndoP;
    private javax.swing.JMenu mnuView;
    private javax.swing.JMenu mnuWindows;
    private javax.swing.JCheckBoxMenuItem mnuZoomAutoAdjust;
    private javax.swing.JMenuItem mnuZoomIn;
    private javax.swing.JMenuItem mnuZoomNormal;
    private javax.swing.JMenuItem mnuZoomOut;
    private javax.swing.JPanel pnlAssembledCode;
    private javax.swing.JPanel pnlCode;
    private javax.swing.JPanel pnlDataMemory;
    private javax.swing.JPanel pnlDatapath;
    private javax.swing.JTabbedPane pnlLeft;
    private javax.swing.JPanel pnlRegisters;
    private javax.swing.JTabbedPane pnlRight;
    private javax.swing.JSplitPane pnlSplit;
    private javax.swing.JToolBar pnlToolBar;
    private brunonova.drmips.pc.AssembledCodeTable tblAssembledCode;
    private brunonova.drmips.pc.DataMemoryTable tblDataMemory;
    private brunonova.drmips.pc.ExecTable tblExec;
    private brunonova.drmips.pc.RegistersTable tblRegisters;
    private javax.swing.JTextArea txtPrint;
    // End of variables declaration//GEN-END:variables

	/**
	 * Listener for the "Open recent" menu items.
	 */
	private class RecentFileActionListener implements ActionListener {
		/** The file of the menu item. */
		private final File file;

		/**
		 * Creates the listener.
		 * @param file The file of the menu item.
		 */
		public RecentFileActionListener(File file) {
			this.file = file;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean open = true;
			if(txtCode != null && txtCode.isDirty()) { // file changed?
				int opt = JOptionPane.showConfirmDialog(FrmSimulator.this, Lang.t("code_changed"), AppInfo.NAME, JOptionPane.YES_NO_CANCEL_OPTION);
				switch(opt) {
					case JOptionPane.YES_OPTION:
						open = true;
						saveFile();
						break;
					case JOptionPane.NO_OPTION: open = true; break;
					case JOptionPane.CANCEL_OPTION: open = false; break;
				}
			}

			if(open)
				openFile(file);
		}
	}

	/**
	 * Listener for the CPU's "Load recent" menu items.
	 */
	private class RecentCPUActionListener implements ActionListener {
		/** The file of the menu item. */
		private final File file;

		/**
		 * Creates the listener.
		 * @param file The file of the menu item.
		 */
		public RecentCPUActionListener(File file) {
			this.file = file;
		}

		@Override
		@SuppressWarnings("UseSpecificCatch")
		public void actionPerformed(ActionEvent e) {
			try {
				loadCPU(file.getAbsolutePath());
				if(!mnuInternalWindows.isSelected())
					tabDatapath.select();
			}
			catch(Exception ex) {
				JOptionPane.showMessageDialog(FrmSimulator.this, Lang.t("invalid_file") + "\n" + ex.getMessage(), AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
				LOG.log(Level.WARNING, "error loading CPU \"" + file.getName() + "\"", ex);
			}
		}
	}

	/**
	 * Event handler fired when the language is changes in the language menu.
	 */
	private class LanguageSelectedListener implements ActionListener {
		/** The associated language. */
		private final String lang;

		/**
		 * Creates the handler.
		 * @param lang The associated language.
		 */
		public LanguageSelectedListener(String lang) {
			this.lang = lang;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(!lang.equals(Lang.getLanguage())) {
				try {
					Lang.load(lang);
					DrMIPS.prefs.put(DrMIPS.LANG_PREF, lang);
					translate();
				}
				catch(Exception ex) {
					JOptionPane.showMessageDialog(null, "Error opening language file " + Lang.getLanguage() + "!\n" + ex.getMessage(), AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
					LOG.log(Level.WARNING, "error opening language file \"" + lang + "\"", ex);
				}
			}
		}
	}


	/**
	 * Contains the informations of a tab.
	 */
	private class Tab {
		/** The panel in the tab. */
		private final JPanel panel;
		/** The side of the tab (<tt>Util.LEFT</tt> or <tt>Util.RIGHT</tt>). */
		private final int side;
		/** The tabbed pane in the side of the tab. */
		private final JTabbedPane tabbedPane;
		/** The index of the tab in the corresponding tabbed pane. */
		private final int index;

		/**
		 * Creates the tab and adds it to the given side.
		 * @param panel The panel in the tab.
		 * @param title The title of the tab.
		 * @param side The side of the tab (<tt>Util.LEFT</tt> or <tt>Util.RIGHT</tt>).
		 */
		public Tab(JPanel panel, String title, int side) {
			this.panel = panel;
			this.side = (side == Util.LEFT || side == Util.RIGHT) ? side : Util.RIGHT;
			tabbedPane = (side == Util.LEFT) ? pnlLeft : pnlRight;
			this.index = tabbedPane.getTabCount();
			tabbedPane.addTab(title, panel);
		}

		/**
		 * Updates the title of the tab.
		 * @param title New title.
		 */
		public void setTitle(String title) {
			tabbedPane.setTitleAt(index, title);
		}

		/**
		 * Returns the panel in the tab.
		 * @return The panel in the tab.
		 */
		public JPanel getPanel() {
			return panel;
		}

		/**
		 * Returns the side of the tab.
		 * @return The side of the tab (<tt>Util.LEFT</tt> or <tt>Util.RIGHT</tt>).
		 */
		public int getSide() {
			return side;
		}

		/**
		 * Returns the index of the tab in the corresponding tabbed pane.
		 * @return The index of the tab in the corresponding tabbed pane.
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * Selects this tab.
		 */
		public void select() {
			if(side == Util.LEFT)
				pnlLeft.setSelectedIndex(index);
			else
				pnlRight.setSelectedIndex(index);
		}
	}

	/**
	 * Listener that disables the simulation controls when the code is changed.
	 */
	private class CodeEditorDocumentListener implements DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			codeEdited();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			codeEdited();
		}

		@Override
		public void changedUpdate(DocumentEvent e) { }

		private void codeEdited() {
			setSimulationControlsEnabled(false);
			mnuPrint.setEnabled(!txtCode.getText().isEmpty());
			if(openFile != null && !getTitle().startsWith("*")) {
				// Prepend '*' to the window title to inform that the file has
				// unsaved changes
				setTitle("*" + getTitle());
			}
		}
	}

	/**
	 * Listener that updates the caret position displayed in the status bar when
	 * the caret changes.
	 */
	private class CodeEditorCaretListener implements CaretListener {
		@Override
		public void caretUpdate(CaretEvent e) {
			updateCaretPosition();
		}
	}
}
