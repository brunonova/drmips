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

package brunonova.drmips.simulator;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains the information of an assembled MIPS instruction.
 * 
 * @author Bruno Nova
 */
public class AssembledInstruction {
	/** The respective MIPS instruction. */
	private final Instruction instruction;
	/** The instruction in machine code. */
	private final Data data;
	/** The respective line in the code. */
	private String codeLine;
	/** The number of the line in the code. */
	private final int line;
	/** The labels for the instruction */
	private final List<String> labels;
	
	/**
	 * Creates an assembled instruction.
	 * @param instruction The respective MIPS instruction.
	 * @param data The instruction in machine code.
	 * @param codeLine The respective line in the code.
	 * @param lineNumber The number of the line in the code.
	 */
	public AssembledInstruction(Instruction instruction, Data data, String codeLine, int lineNumber) {
		this.instruction = instruction;
		this.data = data;
		this.codeLine = codeLine;
		this.line = lineNumber;
		labels = new LinkedList<>();
	}
	
	/**
	 * Returns the respective MIPS instruction.
	 * @return Respective MIPS instruction.
	 */
	public Instruction getInstruction() {
		return instruction;
	}
	
	/**
	 * Returns the instruction in machine code.
	 * @return Instruction in machine code.
	 */
	public Data getData() {
		return data;
	}
	
	/**
	 * Returns the respective line in the code.
	 * @return The line of code.
	 */
	public String getCodeLine() {
		return codeLine;
	}
	
	/**
	 * Updates the instruction's line of code.
	 * @param codeLine The new line of code.
	 */
	protected void setCodeLine(String codeLine) {
		this.codeLine = codeLine;
	}

	/**
	 * Returns the number of the line in the code.
	 * @return Number of the line in the code.
	 */
	public int getLineNumber() {
		return line;
	}
	
	/**
	 * Adds a label to this instruction.
	 * @param label Label to add.
	 */
	public void addLabel(String label) {
		labels.add(label);
	}
	
	/**
	 * Returns the labels for this instruction.
	 * @return The labels for this instruction.
	 */
	public List<String> getLabels() {
		return labels;
	}
	
	/**
	 * Returns a string that shows the instruction field values in binary.
	 * @return Field values in binary.
	 */
	public String toBinaryString() {
		String str = "";
		for(InstructionType.Field field: instruction.getType().getFields()) {
			if(!str.isEmpty()) str += ", ";
			str += field.getId() + "=" + new Data(field.getSize(), field.getValueFromField(data.getValue())).toBinary();
		}
		return str;
	}
	
	/**
	 * Returns a string that shows the instruction field values in decimal.
	 * @return Field values in decimal.
	 */
	public String toDecimalString() {
		String str = "";
		for(InstructionType.Field field: instruction.getType().getFields()) {
			if(!str.isEmpty()) str += ", ";
			str += field.getId() + "=" + new Data(field.getSize(), field.getValueFromField(data.getValue())).toString();
		}
		return str;
	}
	
	/**
	 * Returns a string that shows the instruction field values in hexadecimal.
	 * @return Field values in hexadecimal.
	 */
	public String toHexadecimalString() {
		String str = "";
		for(InstructionType.Field field: instruction.getType().getFields()) {
			if(!str.isEmpty()) str += ", ";
			str += field.getId() + "=" + new Data(field.getSize(), field.getValueFromField(data.getValue())).toHexadecimal();
		}
		return str;
	}

	@Override
	public String toString() {
		return toDecimalString();
	}
}
