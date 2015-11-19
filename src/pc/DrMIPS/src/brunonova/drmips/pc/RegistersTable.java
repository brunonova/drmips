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
import brunonova.drmips.simulator.Data;
import brunonova.drmips.simulator.components.PC;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * The table of registers in the registers tab.
 *
 * @author Bruno Nova
 */
public class RegistersTable extends JTable implements MouseListener {
	/** The index of the register column. */
	private static final int REGISTER_COLUMN_INDEX = 0;
	/** The index of the value column. */
	private static final int VALUE_COLUMN_INDEX = 1;

	/** The model of the table. */
	private DefaultTableModel model = null;
	/** The renderer of the table cells. */
	private RegistersTableCellRenderer cellRenderer = null;
	/** The CPU with the registers to be displayed. */
	private CPU cpu = null;
	/** The datapath panel. */
	private DatapathPanel datapath = null;
	/** The table with the instructions being executed. */
	private ExecTable tblExec = null;
	/** The number of registers. */
	private int numRegs = 0;
	/** The row index of the Program Counter table. */
	private int pcIndex = 0;
	/** The format of the data (<tt>Util.BINARYL_FORMAT_INDEX/Util.DECIMAL_FORMAT_INDEX/Util.HEXADECIMAL_FORMAT_INDEX</tt>). */
	private int dataFormat = DrMIPS.DEFAULT_DATAPATH_DATA_FORMAT;

	/**
	 * Creates the registers table.
	 */
	public RegistersTable() {
		super();
		model = new DefaultTableModel(0, 2);
		cellRenderer = new RegistersTableCellRenderer();
		setDefaultRenderer(Object.class, cellRenderer);
		setModel(model);
		setFont(new Font("Courier New", Font.BOLD, 12));
		getTableHeader().setReorderingAllowed(false);

		addMouseListener(this);
	}

	/**
	 * Defines the CPU that has the registers to be displayed.
	 * @param cpu The CPU.
	 * @param datapath The datapath panel.
	 * @param tblExec The table with the instructions being executed (above the datapath).
	 * @param format The data format (<tt>Util.BINARY_FORMAT_INDEX/Util.DECIMAL_FORMAT_INDEX/Util.HEXADECIMAL_FORMAT_INDEX</tt>).
	 */
	public void setCPU(CPU cpu, DatapathPanel datapath, ExecTable tblExec, int format) {
		if(model == null) return;
		this.cpu = cpu;
		this.dataFormat = format;
		this.datapath = datapath;
		this.tblExec = tblExec;

		// Initialize registers table
		model.setRowCount(0);
		numRegs = cpu.getRegBank().getNumberOfRegisters();
		for(int i = 0; i < numRegs; i++) {
			Object[] data = new Object[2];
			data[0] = i + ": " + cpu.getRegisterName(i);
			if(i < 10) data[0] = " " + data[0];
			data[1] = "";
			model.addRow(data);
		}

		// Add special "registers" (PC,...)
		pcIndex = numRegs;
		model.addRow(new Object[] {"PC", ""});
		refreshValues(format);
	}

	/**
	 * Refreshes the values in the table.
	 * @param format The data format (<tt>Util.BINARYL_FORMAT_INDEX/Util.DECIMAL_FORMAT_INDEX/Util.HEXADECIMAL_FORMAT_INDEX</tt>).
	 */
	public void refreshValues(int format) {
		if(model == null || cpu == null) return;
		this.dataFormat = format;

		String data;
		for(int i = 0; i < numRegs; i++) // registers
			model.setValueAt(Util.formatDataAccordingToFormat(cpu.getRegBank().getRegister(i), format), i, VALUE_COLUMN_INDEX);

		// Special "registers"
		model.setValueAt(Util.formatDataAccordingToFormat(cpu.getPC().getAddress(), format), pcIndex, VALUE_COLUMN_INDEX);
		repaint();
	}

	/**
	 * Returns the name of the register in the indicated row.
	 * @param row Row of the register in the table.
	 * @return Register's name, or <tt>null</tt> if non-existant.
	 */
	private String getRegisterName(int row) {
		if(row == pcIndex) { // PC
			PC pc = cpu.getPC();
			return Lang.t(pc.hasNameKey() ? pc.getNameKey(): pc.getDefaultName());
		} else if(row >= 0 && row < cpu.getRegBank().getNumberOfRegisters()) { // register
			return cpu.getRegisterName(row);
		} else {
			return null;
		}
	}

	/**
	 * Returns the data of the register in the indicated row.
	 * @param row Row of the register in the table.
	 * @return Register's data, or <tt>null</tt> if non-existant.
	 */
	private Data getRegisterData(int row) {
		if(row == pcIndex) // PC
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
	private void setRegisterValue(int row, int value) {
		if(isRegisterEditable(row)) {
			if(row == pcIndex) { // PC
				if(value % (Data.DATA_SIZE / 8) == 0) {
					cpu.setPCAddress(value);
				}
				else
					JOptionPane.showMessageDialog(this, Lang.t("invalid_value"), AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
			}
			else if(row >= 0 && row < cpu.getRegBank().getNumberOfRegisters()) // register
				cpu.getRegBank().setRegister(row, value);

			if(datapath != null) datapath.refresh(); // update datapath
			if(tblExec != null) tblExec.refresh(); // update exec table
		}
	}

	/**
	 * Returns whether register in the indicated row is editable.
	 * @param row Row of the register in the table.
	 * @return <tt>True</tt> if the register is editable.
	 */
	private boolean isRegisterEditable(int row) {
		if(row == pcIndex) // PC
			return true;
		else if(row >= 0 && row < cpu.getRegBank().getNumberOfRegisters()) // register
			return !cpu.getRegBank().isRegisterConstant(row);
		else
			return false;
	}

	/**
	 * Translates the table.
	 */
	public void translate() {
		getTableHeader().getColumnModel().getColumn(REGISTER_COLUMN_INDEX).setHeaderValue(Lang.t("register"));
		getTableHeader().getColumnModel().getColumn(VALUE_COLUMN_INDEX).setHeaderValue(Lang.t("value"));
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getClickCount() == 2) {
			int row = rowAtPoint(e.getPoint());
			if(isRegisterEditable(row)) {
				String res = (String)JOptionPane.showInputDialog(this.getParent(), Lang.t("edit_value", getRegisterName(row)) + ":", AppInfo.NAME, JOptionPane.QUESTION_MESSAGE, null, null, getRegisterData(row).getValue());
				if(res != null) {
					try {
						setRegisterValue(row, Integer.parseInt(res));
						refreshValues(dataFormat);
					} catch(NumberFormatException ex) {
						JOptionPane.showMessageDialog(this.getParent(), Lang.t("invalid_value"), AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			else
				JOptionPane.showMessageDialog(this.getParent(), Lang.t("register_not_editable", getRegisterName(row)), AppInfo.NAME, JOptionPane.INFORMATION_MESSAGE);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	private class RegistersTableCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			Color background = javax.swing.UIManager.getDefaults().getColor("Table.background"); // get foreground color from look and feel

			setHorizontalAlignment(column == 1 ? SwingConstants.RIGHT : SwingConstants.LEFT); // align 2nd column to the right

			// Highlight registers being accessed
			int reg1 = cpu.getRegBank().getReadReg1().getValue();
			int reg2 = cpu.getRegBank().getReadReg2().getValue();
			int regW = cpu.getRegBank().getWriteReg().getValue();
			boolean write = cpu.getRegBank().getRegWrite().getValue() == 1;

			if(write && row == regW && !cpu.getRegBank().isRegisterConstant(regW)) {
				if(row == reg1 || row == reg2) {
					setBackground(Util.rwColor);
					setToolTipText(Lang.t("reading_and_writing_to_reg"));
				}
				else {
					setBackground(Util.writeColor);
					setToolTipText(Lang.t("writing_to_reg"));
				}
			}
			else if(row == reg1 || row == reg2) {
				setBackground(Util.readColor);
				setToolTipText(Lang.t("reading_from_reg"));
			}
			else {
				setBackground(background);
				setToolTipText(null);
			}

			return c;
		}
	}
}
