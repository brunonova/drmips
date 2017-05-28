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
 * The table with the data memory positions.
 * 
 * @author Bruno Nova
 */
public class DataMemoryTable extends JTable implements MouseListener {
	/** The index of the address column. */
	private static final int ADDRESS_COLUMN_INDEX = 0;
	/** The index of the value column. */
	private static final int VALUE_COLUMN_INDEX = 1;
	
	/** The model of the table. */
	private DefaultTableModel model = null;
	/** The renderer of the table cells. */
	private DataMemoryTableCellRenderer cellRenderer = null;
	/** The CPU with the memory to be displayed. */
	private CPU cpu = null;
	/** The datapath panel. */
	private DatapathPanel datapath = null;
	/** The number of memory positions. */
	private int memorySize = 0;
	/** The format of the data (<tt>Util.BINARYL_FORMAT_INDEX/Util.DECIMAL_FORMAT_INDEX/Util.HEXADECIMAL_FORMAT_INDEX</tt>). */
	private int dataFormat = DrMIPS.DEFAULT_DATA_MEMORY_FORMAT;

	/**
	 * Creates the data memory table.
	 */
	public DataMemoryTable() {
		super();
		model = new DefaultTableModel(0, 2);
		cellRenderer = new DataMemoryTableCellRenderer();
		setDefaultRenderer(Object.class, cellRenderer);
		setModel(model);
		setFont(new Font("Courier New", Font.BOLD, 12));
		getTableHeader().setReorderingAllowed(false);
		
		addMouseListener(this);
	}
	
	/**
	 * Defines the CPU that has the data memory to be displayed.
	 * @param cpu The CPU.
	 * @param datapath The datapath panel.
	 * @param format The data format (<tt>Util.BINARYL_FORMAT_INDEX/v.DECIMAL_FORMAT_INDEX/Util.HEXADECIMAL_FORMAT_INDEX</tt>).
	 */
	public void setCPU(CPU cpu, DatapathPanel datapath, int format) {
		if(model == null) return;
		this.cpu = cpu;
		this.dataFormat = format;
		this.datapath = datapath;
		
		// Initialize registers table
		model.setRowCount(0);
		if(cpu.hasDataMemory()) {
			memorySize = cpu.getDataMemory().getMemorySize();
			for(int i = 0; i < memorySize; i++) {
				Object[] data = new Object[2];
				data[0] = Util.formatDataAccordingToFormat(new Data(Data.DATA_SIZE, i * (Data.DATA_SIZE / 8)), format);
				data[1] = "";
				model.addRow(data);
			}

			refreshValues(format);
		}
	}
	
	/**
	 * Refreshes the values in the table.
	 * @param format The data format (<tt>Util.BINARYL_FORMAT_INDEX/Util.DECIMAL_FORMAT_INDEX/Util.HEXADECIMAL_FORMAT_INDEX</tt>).
	 */
	public void refreshValues(int format) {
		if(model == null || cpu == null || !cpu.hasDataMemory()) return;
		this.dataFormat = format;
		
		String data;
		for(int i = 0; i < memorySize; i++) {
			model.setValueAt(Util.formatDataAccordingToFormat(new Data(Data.DATA_SIZE, i * (Data.DATA_SIZE / 8)), format), i, ADDRESS_COLUMN_INDEX);
			model.setValueAt(Util.formatDataAccordingToFormat(new Data(Data.DATA_SIZE, cpu.getDataMemory().getDataInIndex(i)), format), i, VALUE_COLUMN_INDEX);
		}
		repaint();
	}
	
	/**
	 * Translates the table.
	 */
	public void translate() {
		getTableHeader().getColumnModel().getColumn(ADDRESS_COLUMN_INDEX).setHeaderValue(Lang.t("address"));
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
			String res = (String)JOptionPane.showInputDialog(this.getParent(), Lang.t("edit_value", row * (Data.DATA_SIZE / 8)) + ":", AppInfo.NAME, JOptionPane.QUESTION_MESSAGE, null, null, cpu.getDataMemory().getDataInIndex(row));
			if(res != null) {
				try {
					cpu.getDataMemory().setDataInIndex(row, Integer.parseInt(res));
					refreshValues(dataFormat);
					if(datapath != null)
						datapath.refresh(); // update datapath
				} catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(this.getParent(), Lang.t("invalid_value"), AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
				}
			}
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
	
	private class DataMemoryTableCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			Color background = javax.swing.UIManager.getDefaults().getColor("Table.background"); // get background color from look and feel
			
			setHorizontalAlignment(column == 1 ? SwingConstants.RIGHT : SwingConstants.LEFT); // align 2nd column to the right
			
			if(cpu.hasDataMemory()) { // Highlight memory positions being accessed
				int index = cpu.getDataMemory().getAddress().getValue() / (Data.DATA_SIZE / 8);
				boolean read = cpu.getDataMemory().getMemRead().getValue() == 1;
				boolean write = cpu.getDataMemory().getMemWrite().getValue() == 1;

				if(write && row == index) {
					if(read) {
						setBackground(Util.rwColor);
						setToolTipText(Lang.t("reading_and_writing_to_mem"));
					}
					else {
						setBackground(Util.writeColor);
						setToolTipText(Lang.t("writing_to_mem"));
					}
				}
				else if(read && row == index) {
					setBackground(Util.readColor);
					setToolTipText(Lang.t("reading_from_mem"));
				}
				else {
					setBackground(background);
					setToolTipText(null);
				}
			}
			
			return c;
		}
	}
}
