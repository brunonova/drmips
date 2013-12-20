/*
    DrMIPS - Educational MIPS simulator
    Copyright (C) 2013 Bruno Nova <ei08109@fe.up.pt>

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

package org.feup.brunonova.drmips.mips.components;

import java.util.ArrayList;
import java.util.List;
import org.feup.brunonova.drmips.exceptions.InvalidCPUException;
import org.feup.brunonova.drmips.mips.AssembledInstruction;
import org.feup.brunonova.drmips.mips.Component;
import org.feup.brunonova.drmips.mips.Data;
import org.feup.brunonova.drmips.mips.Input;
import org.feup.brunonova.drmips.mips.Output;
import org.feup.brunonova.drmips.util.Dimension;
import org.feup.brunonova.drmips.util.Point;

/**
 * Class that represents the instruction memory.
 * 
 * @author Bruno Nova
 */
public class InstructionMemory extends Component {
	/** All the assembled instructions. */
	private List<AssembledInstruction> instructions;
	/** The identifier of the input. */
	private final String inId;
	/** The identifier of the output. */
	private final String outId;

	/**
	 * Instruction memory constructor.
	 * @param id Instruction memory's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param inId The identifier of the input.
	 * @param outId The identifier of the output.
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public InstructionMemory(String id, int latency, Point position, String inId, String outId) throws InvalidCPUException {
		super(id, latency, "Instruction\nMemory", "instruction_memory", "instruction_memory_description", position, new Dimension(80, 100));
		instructions = new ArrayList<AssembledInstruction>();
		this.inId = inId;
		this.outId = outId;
		addInput(inId, new Data());
		addOutput(outId, new Data());
	}

	@Override
	public void execute() {
		int index = getInput().getValue() / (Data.DATA_SIZE / 8);
		if(index >= 0 && index < instructions.size())
			getOutput().setValue(getInstruction(index).getData().getValue());
		else
			getOutput().setValue(0);
	}
	
	/**
	 * Returns the assembled instruction with the specified index.
	 * @param index Index of the instruction.
	 * @return The desired instruction, or <tt>null</tt> if it doesn't exist.
	 */
	public AssembledInstruction getInstruction(int index) {
		if(index >= 0 && index < instructions.size())
			return instructions.get(index);
		else
			return null;
	}
	
	/**
	 * Returns the number of instructions in memory.
	 * @return The number of instructions.
	 */
	public int getNumberOfInstructions() {
		return instructions.size();
	}
	
	/**
	 * Loads the specified instructions into the memory.
	 * @param instructions Instructions to load.
	 */
	public void setInstructions(List<AssembledInstruction> instructions) {
		this.instructions = instructions;
		execute();
	}
	
	/**
	 * Returns the identifier of the input.
	 * @return The identifier of the input.
	 */
	public String getInputId() {
		return inId;
	}
	
	/**
	 * Returns the identifier of the output.
	 * @return The identifier of the output.
	 */
	public String getOutputId() {
		return outId;
	}
	
	/**
	 * Return the memory's input.
	 * @return Memory input.
	 */
	public Input getInput() {
		return getInput(inId);
	}
	
	/**
	 * Return the memory's output.
	 * @return Memory output.
	 */
	public Output getOutput() {
		return getOutput(outId);
	}
}
