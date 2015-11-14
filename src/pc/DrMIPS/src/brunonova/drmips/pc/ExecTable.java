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
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * The table that presents the instructions being executed in the datapath tab.
 * 
 * @author Bruno Nova
 */
public class ExecTable extends JTable {
	/** The model of the table. */
	private DefaultTableModel model = null;
	/** The renderer of the table cells. */
	private ExecTableCellRenderer cellRenderer = null;
	/** The CPU with the registers to be displayed. */
	private CPU cpu = null;
	/** The format of the data (<tt>Util.BINARYL_FORMAT_INDEX/Util.DECIMAL_FORMAT_INDEX/Util.HEXADECIMAL_FORMAT_INDEX</tt>). */
	private int dataFormat = DrMIPS.DEFAULT_DATAPATH_DATA_FORMAT;
	
	/**
	 * Creates the registers table.
	 */
	public ExecTable() {
		super();
		model = new DefaultTableModel(1, 1);
		cellRenderer = new ExecTableCellRenderer();
		setDefaultRenderer(Object.class, cellRenderer);
		setModel(model);
		setFont(new Font("Courier New", Font.BOLD, 12));
		getTableHeader().setReorderingAllowed(false);
	}
	
	/**
	 * Defines the CPU that is executing the program.
	 * @param cpu The CPU.
	 * @param format The data format (<tt>Util.BINARYL_FORMAT_INDEX/v.DECIMAL_FORMAT_INDEX/Util.HEXADECIMAL_FORMAT_INDEX</tt>).
	 */
	public void setCPU(CPU cpu, int format) {
		this.cpu = cpu;
		this.dataFormat = format;
		
		// Set the columns
		model.setRowCount(0);
		model.setColumnCount(0);
		if(cpu.isPipeline())
			for(int i = 0; i < 5; i++) model.addColumn(null);
		else
			model.addColumn(null);
		
		model.setRowCount(1); // add 1 row
		
		refresh(format);
	}
	
	/**
	 * Refreshes the values in the table.
	 */
	public void refresh() {
		refresh(dataFormat);
	}
	
	/**
	 * Refreshes the values in the table.
	 * @param format The data format (<tt>Util.BINARYL_FORMAT_INDEX/v.DECIMAL_FORMAT_INDEX/Util.HEXADECIMAL_FORMAT_INDEX</tt>).
	 */
	public void refresh(int format) {
		if(model == null || cpu == null) return;
		dataFormat = format;
		
		model.setValueAt(getInstructionInIndex(cpu.getPC().getCurrentInstructionIndex()), 0, 0);
		if(cpu.isPipeline()) {
			model.setValueAt(getInstructionInIndex(cpu.getIfIdReg().getCurrentInstructionIndex()), 0, 1);
			model.setValueAt(getInstructionInIndex(cpu.getIdExReg().getCurrentInstructionIndex()), 0, 2);
			model.setValueAt(getInstructionInIndex(cpu.getExMemReg().getCurrentInstructionIndex()), 0, 3);
			model.setValueAt(getInstructionInIndex(cpu.getMemWbReg().getCurrentInstructionIndex()), 0, 4);
		}
		
		repaint();
	}
	
	/**
	 * Returns the code line of the instruction in the specified index.
	 * @param index Index of the instruction.
	 * @return Code line of the instruction, or an empty string if it doesn't exist.
	 */
	private String getInstructionInIndex(int index) {
		AssembledInstruction i = cpu.getInstructionMemory().getInstruction(index);
		return (i != null) ? i.getCodeLine() : "";
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		if(cpu == null || model == null) return null;
		AssembledInstruction i = null;
		
		switch(columnAtPoint(event.getPoint())) {
			case 0: i = cpu.getInstructionMemory().getInstruction(cpu.getPC().getCurrentInstructionIndex()); break;
			case 1: i = cpu.getInstructionMemory().getInstruction(cpu.getIfIdReg().getCurrentInstructionIndex()); break;
			case 2: i = cpu.getInstructionMemory().getInstruction(cpu.getIdExReg().getCurrentInstructionIndex()); break;
			case 3: i = cpu.getInstructionMemory().getInstruction(cpu.getExMemReg().getCurrentInstructionIndex()); break;
			case 4: i = cpu.getInstructionMemory().getInstruction(cpu.getMemWbReg().getCurrentInstructionIndex()); break;
		}
		
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

	private class ExecTableCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(SwingConstants.CENTER); // center all values

			if(getColumnCount() == 1)
				setBackground(Util.instColor);
			else {
				switch(column) {
					case 0: setBackground(Util.ifColor); break;
					case 1: setBackground(Util.idColor); break;
					case 2: setBackground(Util.exColor); break;
					case 3: setBackground(Util.memColor); break;
					case 4: setBackground(Util.wbColor); break;
				}
			}
			
			return c;
		}
	}
}
