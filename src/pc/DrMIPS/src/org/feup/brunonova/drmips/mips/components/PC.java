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

import java.util.Stack;
import org.feup.brunonova.drmips.exceptions.InvalidCPUException;
import org.feup.brunonova.drmips.mips.Component;
import org.feup.brunonova.drmips.mips.Data;
import org.feup.brunonova.drmips.mips.IOPort;
import org.feup.brunonova.drmips.mips.Input;
import org.feup.brunonova.drmips.mips.IsSynchronous;
import org.feup.brunonova.drmips.mips.Output;
import org.feup.brunonova.drmips.util.Dimension;
import org.feup.brunonova.drmips.util.Point;

/**
 * Class that represents the synchronous Program Counter.
 * 
 * @author Bruno Nova
 */
public class PC extends Component implements IsSynchronous {
	/** The identifier of the input. */
	private String inId;
	/** The identifier of the write input. */
	private String writeId;
	/** The identifier of the output. */
	private String outId;
	/** Current address of the Program Counter (the <tt>$pc</tt> register) */
	private Data address;
	/** The previous saved addresses. */
	private final Stack<Integer> states = new Stack<Integer>();
	/** The index of the current instruction being executed (-1 if none). */
	private int currentInstructionIndex = -1;
	/** The indexes of the previous instructions. */
	private final Stack<Integer> instructions = new Stack<Integer>();
	
	/**
	 * Program Counter constructor.
	 * @param id PC's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param inId The identifier of the input.
	 * @param outId The identifier of the output.
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public PC(String id, int latency, Point position, String inId, String outId) throws InvalidCPUException {
		this(id, latency, position, inId, outId, "Write");
	}
	
	/**
	 * Program Counter constructor.
	 * @param id PC's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param inId The identifier of the input.
	 * @param outId The identifier of the output.
	 * @param writeId The identifier of the write input.
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public PC(String id, int latency, Point position, String inId, String outId, String writeId) throws InvalidCPUException {
		super(id, latency, "PC", "pc", "pc_description", position, new Dimension(30, 30));
		this.address = new Data();
		this.inId = inId;
		this.writeId = writeId;
		this.outId = outId;
		addInput(inId, new Data(), IOPort.Direction.WEST, false, true);
		addInput(writeId, new Data(1, 1), IOPort.Direction.NORTH, false);
		addOutput(outId, new Data(), IOPort.Direction.EAST, true);
	}
	
	@Override
	public void execute() {
		getOutput().setValue(address.getValue());
		getInput().setRelevant(getWrite().getValue() == 1); // mark input as irrelevant if the Write control signal is off
	}

	@Override
	public void executeSynchronous() {
		if(getWrite().getValue() == 1)
			setAddress(getInput().getValue(), false);
	}

	@Override
	public void pushState() {
		states.push(getAddress().getValue());
		instructions.push(getCurrentInstructionIndex());
	}

	@Override
	public void popState() {
		if(hasSavedStates()) {
			setAddress(states.pop(), false);
			setCurrentInstructionIndex(instructions.pop());
		}
	}

	@Override
	public boolean hasSavedStates() {
		return !states.empty();
	}

	@Override
	public void clearSavedStates() {
		states.clear();
		instructions.clear();
	}
	
	@Override
	public void resetFirstState() {
		while(hasSavedStates())
			popState();
	}
	
	/**
	 * Returns the current address of the Program Counter (the <tt>$pc</tt> register).
	 * @return Current address.
	 */
	public Data getAddress() {
		return address;
	}
	
	/**
	 * Updates the addres of the Program Counter (the <tt>$pc</tt> register).
	 * <p>The new address is propagated to the rest of the circuit.</p>
	 * @param address New address.
	 */
	public void setAddress(int address) {
		setAddress(address, true);
	}
	
	/**
	 * Updates the addres of the Program Counter (the <tt>$pc</tt> register).
	 * @param address New address.
	 * @param propagate Whether the new address is propagated to the rest of the circuit.
	 */
	public void setAddress(int address, boolean propagate) {
		this.address.setValue(address);
		if(propagate) execute();
	}

	/**
	 * Returns the index of the current instruction being executed.
	 * @return Index of the current instruction being executed (-1 if none).
	 */
	public int getCurrentInstructionIndex() {
		return currentInstructionIndex;
	}

	/**
	 * Updates the index of the current instruction being executed.
	 * @param currentInstructionIndex The index of the instruction (-1 if none).
	 */
	public void setCurrentInstructionIndex(int currentInstructionIndex) {
		this.currentInstructionIndex = currentInstructionIndex;
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
	 * Returns the identifier of the write input.
	 * @return The identifier of the write input.
	 */
	public String getWriteId() {
		return writeId;
	}
	
	/**
	 * Returns the Program Counter's input.
	 * @return PC input;
	 */
	public Input getInput() {
		return getInput(inId);
	}
	
	/**
	 * Returns the Program Counter's output.
	 * @return PC output;
	 */
	public Output getOutput() {
		return getOutput(outId);
	}
	
	/**
	 * Returns the the write input.
	 * @return Write input.
	 */
	public Input getWrite() {
		return getInput(writeId);
	}
}
