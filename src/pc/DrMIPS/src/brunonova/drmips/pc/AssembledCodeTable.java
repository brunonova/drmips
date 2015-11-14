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

import brunonova.drmips.simulator.AssembledInstruction;
import brunonova.drmips.simulator.CPU;
import brunonova.drmips.simulator.Data;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * The table with the assembled instructions.
 * 
 * @author Bruno Nova
 */
public class AssembledCodeTable extends JTable {
	/** The index of the address column. */
	private static final int ADDRESS_COLUMN_INDEX = 0;
	/** The index of the assembled column. */
	private static final int ASSEMBLED_COLUMN_INDEX = 1;
	/** The index of the code column. */
	private static final int CODE_COLUMN_INDEX = 2;
	
	/** The model of the table. */
	private DefaultTableModel model = null;
	/** The renderer of the table cells. */
	private AssembledCodeTableCellRenderer cellRenderer = null;
	/** The CPU with the assembled code to be displayed. */
	private CPU cpu = null;
	/** The format of the data (<tt>Util.BINARYL_FORMAT_INDEX/Util.DECIMAL_FORMAT_INDEX/Util.HEXADECIMAL_FORMAT_INDEX</tt>). */
	private int dataFormat = DrMIPS.DEFAULT_DATAPATH_DATA_FORMAT;

	/**
	 * Creates the assembled code table.
	 */
	public AssembledCodeTable() {
		super();
		model = new DefaultTableModel(0, 3);
		cellRenderer = new AssembledCodeTableCellRenderer();
		setDefaultRenderer(Object.class, cellRenderer);
		setModel(model);
		setFont(new Font("Courier New", Font.BOLD, 12));
		getTableHeader().setReorderingAllowed(false);
	}
	
	/**
	 * Defines the CPU that has the assembled code to be displayed.
	 * @param cpu The CPU.
	 * @param format The data format (<tt>Util.BINARYL_FORMAT_INDEX/v.DECIMAL_FORMAT_INDEX/Util.HEXADECIMAL_FORMAT_INDEX</tt>).
	 */
	public void setCPU(CPU cpu, int format) {
		if(model == null) return;
		this.cpu = cpu;
		this.dataFormat = format;
		
		// Initialize the table
		refresh(format);
	}
	
	/**
	 * Refresh the values in the table.
	 * @param format The data format (<tt>Util.BINARYL_FORMAT_INDEX/v.DECIMAL_FORMAT_INDEX/Util.HEXADECIMAL_FORMAT_INDEX</tt>).
	 */
	public void refresh(int format) {
		AssembledInstruction instruction;
		Object[] data;
		dataFormat = format;
		
		model.setRowCount(0);
		for(int i = 0; i < cpu.getInstructionMemory().getNumberOfInstructions(); i++) {
			instruction = cpu.getInstructionMemory().getInstruction(i);
			data = new Object[3];
			data[0] = Util.formatDataAccordingToFormat(new Data(Data.DATA_SIZE, i * (Data.DATA_SIZE / 8)), format);
			data[1] = Util.formatDataAccordingToFormat(instruction.getData(), format);
			data[2] = instruction.getLineNumber() + ": ";
			for(String label: instruction.getLabels())
				data[2] += label + ": ";
			data[2] += instruction.getCodeLine();
			model.addRow(data);
		}
		
		refreshValues();
	}
	
	/**
	 * Refreshes the highlights.
	 */
	public void refreshValues() {
		repaint();
	}
	
	/**
	 * Translates the table.
	 */
	public void translate() {
		getTableHeader().getColumnModel().getColumn(ADDRESS_COLUMN_INDEX).setHeaderValue(Lang.t("address"));
		getTableHeader().getColumnModel().getColumn(ASSEMBLED_COLUMN_INDEX).setHeaderValue(Lang.t("assembled"));
		getTableHeader().getColumnModel().getColumn(CODE_COLUMN_INDEX).setHeaderValue(Lang.t("code"));
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		AssembledInstruction i = cpu.getInstructionMemory().getInstruction(rowAtPoint(event.getPoint()));
		if(i != null) {
			switch(dataFormat) {
				case Util.BINARY_FORMAT_INDEX: return "<html><tt><b>" + Lang.t("type_x", i.getInstruction().getType().getId()) + ": " + i.getInstruction().getMnemonic() + "</b> (" + i.toBinaryString() + ")</tt></html>";
				case Util.HEXADECIMAL_FORMAT_INDEX: return "<html><tt><b>" + Lang.t("type_x", i.getInstruction().getType().getId()) + ": "  + i.getInstruction().getMnemonic() + "</b> (" + i.toHexadecimalString() + ")</tt></html>";	
				default:return "<html><tt><b>" + Lang.t("type_x", i.getInstruction().getType().getId()) + ": "  + i.getInstruction().getMnemonic() + "</b> (" + i.toString() + ")</tt></html>";
			}
		}
		else
			return null;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
	
	private class AssembledCodeTableCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			Color background = javax.swing.UIManager.getDefaults().getColor("Table.background"); // get background color from look and feel
			
			// Highlight instructions being executed
			if(row == cpu.getPC().getCurrentInstructionIndex())
				setBackground(cpu.isPipeline() ? Util.ifColor : Util.instColor);
			else if(cpu.isPipeline()) {
				if(row == cpu.getIfIdReg().getCurrentInstructionIndex())
					setBackground(Util.idColor);
				else if(row == cpu.getIdExReg().getCurrentInstructionIndex())
					setBackground(Util.exColor);
				else if(row == cpu.getExMemReg().getCurrentInstructionIndex())
					setBackground(Util.memColor);
				else if(row == cpu.getMemWbReg().getCurrentInstructionIndex())
					setBackground(Util.wbColor);
				else
					setBackground(background);
			}
			else
				setBackground(background);
			
			return c;
		}
	}
}
