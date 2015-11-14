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

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Table listing the supported instructions.
 * 
 * @author Bruno Nova
 */
public class SupportedInstructionsTable extends JTable {
	/** Space between the first column and the second */
	private static final int SPACING = 8;
	
	/** The model of the table. */
	private DefaultTableModel model = null;
	
	/**
	 * Creates the table.
	 */
	public SupportedInstructionsTable() {
		super();
		model = new DefaultTableModel(0, 2);
		setModel(model);
		setFont(new java.awt.Font("Courier New", 0, 12));
		
		getTableHeader().setReorderingAllowed(false); // table header required
		getTableHeader().getColumnModel().getColumn(0).setHeaderValue("");
		getTableHeader().getColumnModel().getColumn(1).setHeaderValue("");
	}
	
	/**
	 * Clears the table.
	 */
	public void clear() {
		model.setRowCount(0);
	}
	
	/**
	 * Adds an instruction to the table.
	 * @param usage The mnemonic with an example of its usage.
	 * @param description A short description of the instruction.
	 */
	public void addInstruction(String usage, String description) {
		String[] data = new String[2];
		data[0] = usage;
		data[1] = (description != null && !description.isEmpty()) ? description : "-";
		model.addRow(data);
	}
	
	/**
	 * Resizes the first column to the minimum width.
	 */
	public void packFirstColumn() {
		int width = 0, num = getRowCount();
		TableCellRenderer renderer;
		Component comp;
		
		for(int r = 0; r < num; r++) {
			renderer = getCellRenderer(r, 0);
			comp = prepareRenderer(renderer, r, 0);
			width = Math.max(comp.getPreferredSize().width + getIntercellSpacing().width, width);
		}
		
		TableColumn col = getColumnModel().getColumn(0);
		getTableHeader().setResizingColumn(col);
		col.setWidth(width + SPACING);
	}
}
