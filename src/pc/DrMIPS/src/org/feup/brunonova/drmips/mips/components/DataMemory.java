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
 * Class that represents the data memory.
 * 
 * @author Bruno Nova
 */
public class DataMemory extends Component implements IsSynchronous {
	/** The minimum size of the memory (in ints). */
	public static final int MINIMUM_SIZE = 20;
	/** The maximum size of the memory (in ints). */
	public static final int MAXIMUM_SIZE = 500;
	
	/** The saved data. */
	private int[] memory;
	/** The identifier of the address input. */
	private final String addressId;
	/** The identifier of the write data input. */
	private final String writeDataId;
	/** The identifier of the output. */
	private final String outId;
	/** The identifier of the MemRead input. */
	private final String memReadId
		/** The identifier of the MemWrite input. */;
	private final String memWriteId;
	/** The previous values of the memory. */
	private final Stack<int[]> states = new Stack<int[]>();
	
	/**
	 * Data memory contructor.
	 * @param id Data memory's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param size The size of the memory (number of 32 bits positions).
	 * @param addressId The identifier of the address input.
	 * @param writeDataId The identifier of the write data input.
	 * @param outId The identifier of the output.
	 * @param memReadId The identifier of the MemRead input.
	 * @param memWriteId The identifier of the MemWrite input.
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public DataMemory(String id, int latency, Point position, int size, String addressId, String writeDataId, String outId, String memReadId, String memWriteId) throws InvalidCPUException {
		super(id, latency, "Data\nmemory", "data_memory", "data_memory_description", position, new Dimension(80, 100));
		this.addressId = addressId;
		this.writeDataId = writeDataId;
		this.outId = outId;
		this.memReadId = memReadId;
		this.memWriteId = memWriteId;
		
		if(size < MINIMUM_SIZE || size > MAXIMUM_SIZE)
			throw new InvalidCPUException("Invalid data memory size! Must be between " + MINIMUM_SIZE + " and " + MAXIMUM_SIZE + " positions (each position has 32 bits).");
		
		memory = new int[size];
		addInput(addressId, new Data(), IOPort.Direction.WEST, true, true);
		addInput(writeDataId, new Data(), IOPort.Direction.WEST, false, true);
		addInput(memReadId, new Data(1), IOPort.Direction.NORTH);
		addInput(memWriteId, new Data(1), IOPort.Direction.NORTH, false);
		addOutput(outId, new Data(), IOPort.Direction.EAST, true);
	}

	@Override
	public void execute() {
		boolean read = getMemRead().getValue() == 1;
		boolean write = getMemWrite().getValue() == 1;
		
		if(getMemRead().getValue() == 1)
			getOutput().setValue(getData(getAddress().getValue()));
		else
			getOutput().setValue(0);
		
		// Set inputs/outputs relevant if reading/writing
		getWriteData().setRelevant(write);
		getAddress().setRelevant(read || write);
		getOutput().setRelevant(read);
	}

	@Override
	public void executeSynchronous() {
		if(getMemWrite().getValue() == 1)
			setData(getAddress().getValue(), getWriteData().getValue(), true);
	}

	@Override
	public void pushState() {
		states.push(memory.clone());
	}

	@Override
	public void popState() {
		memory = states.pop();
	}

	@Override
	public boolean hasSavedStates() {
		return !states.empty();
	}

	@Override
	public void clearSavedStates() {
		states.clear();
	}

	@Override
	public void resetFirstState() {
		while(hasSavedStates())
			popState();
	}
	
	@Override
	public boolean isWritingState() {
		return getMemWrite().getValue() == 1;
	}
	
	/**
	 * Resets the memory to zeros.
	 */
	public void reset() {
		for(int i = 0; i < memory.length; i++)
			memory[i] = 0;
		execute();
	}
	
	/**
	 * Returns the value in the specified address.
	 * @param address The address of the memory position.
	 * @return The desired value.
	 */
	public int getData(int address) {
		return getDataInIndex(getIndexOfAddress(address));
	}
	
	/**
	 * Returns the value in the specified index.
	 * @param index The index of the memory position.
	 * @return The desired value, or 0 if the index is out of bounds.
	 */
	public int getDataInIndex(int index) {
		return (index >= 0 && index < getMemorySize()) ? memory[index] : 0;
	}
	
	/**
	 * Updates the value in the specified address.
	 * <p>The new value is propagated to the rest of the circuit if it is being read.</p>
	 * @param address The address of the memory position.
	 * @param value The new value.
	 */
	public void setData(int address, int value) {
		setData(address, value, true);
	}
	
	/**
	 * Updates the value in the specified address.
	 * @param address The address of the memory position.
	 * @param value The new value.
	 * @param propagate Whether the new value is propagated to the rest of the circuit if it is being read.
	 */
	public void setData(int address, int value, boolean propagate) {
		setDataInIndex(getIndexOfAddress(address), value, propagate);
	}
	
	/**
	 * Updates the value in the specified index.
	 * <p>The new value is propagated to the rest of the circuit if it is being read.</p>
	 * @param index The index of the memory position.
	 * @param value The new value.
	 */
	public void setDataInIndex(int index, int value) {
		setDataInIndex(index, value, true);
	}
	
	/**
	 * Updates the value in the specified index.
	 * @param index The index of the memory position.
	 * @param value The new value.
	 * @param propagate Whether the new value is propagated to the rest of the circuit if it is being read.
	 */
	public void setDataInIndex(int index, int value, boolean propagate) {
		if(index >= 0 && index < getMemorySize()) {
			memory[index] = value;
			if(propagate) execute();
		}
	}
	
	/**
	 * Returns the index of the memory position in the specified address.
	 * @param address The address of the memory position.
	 * @return The index of the position, or -1 if out of bounds.
	 */
	public int getIndexOfAddress(int address) {
		int index = address / (Data.DATA_SIZE / 8); // A lw on an address like 3 would give an error in a CPU with exceptions
		return (index >= 0 && index < getMemorySize()) ? index : -1;
	}
	
	/**
	 * Returns the size of the memory.
	 * @return The size of the memory (number of 32 bits positions).
	 */
	public int getMemorySize() {
		return memory.length;
	}

	/**
	 * Returns the identifier of the address input.
	 * @return The identifier of the address input.
	 */
	public String getAddressId() {
		return addressId;
	}

	/**
	 * Returns the identifier of the write data input.
	 * @return The identifier of the write data input.
	 */
	public String getWriteDataId() {
		return writeDataId;
	}

	/**
	 * Returns the identifier of the MemRead input.
	 * @return The identifier of the MemRead input.
	 */
	public String getMemReadId() {
		return memReadId;
	}

	/**
	 * Returns the identifier of the MemWrite input.
	 * @return The identifier of the MemWrite input.
	 */
	public String getMemWriteId() {
		return memWriteId;
	}
	
	/**
	 * Returns the identifier of the output.
	 * @return The identifier of the output.
	 */
	public String getOutputId() {
		return outId;
	}
	
	/**
	 * Returns the address input.
	 * @return Address input.
	 */
	public Input getAddress() {
		return getInput(addressId);
	}
	
	/**
	 * Returns the write data input.
	 * @return Write data input.
	 */
	public Input getWriteData() {
		return getInput(writeDataId);
	}
	
	/**
	 * Returns the MemRead input.
	 * @return MemRead input.
	 */
	public Input getMemRead() {
		return getInput(memReadId);
	}
	
	/**
	 * Returns the MemWrite input.
	 * @return MemWrite input.
	 */
	public Input getMemWrite() {
		return getInput(memWriteId);
	}
	
	/**
	 * Returns the output.
	 * @return Output.
	 */
	public Output getOutput() {
		return getOutput(outId);
	}
}
