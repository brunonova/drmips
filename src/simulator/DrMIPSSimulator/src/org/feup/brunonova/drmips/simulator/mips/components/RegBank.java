/*
    DrMIPS - Educational MIPS simulator
    Copyright (C) 2013-2014 Bruno Nova <ei08109@fe.up.pt>

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

package org.feup.brunonova.drmips.simulator.mips.components;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import org.feup.brunonova.drmips.simulator.exceptions.InvalidCPUException;
import org.feup.brunonova.drmips.simulator.mips.Component;
import org.feup.brunonova.drmips.simulator.mips.Data;
import org.feup.brunonova.drmips.simulator.mips.IOPort;
import org.feup.brunonova.drmips.simulator.mips.Input;
import org.feup.brunonova.drmips.simulator.mips.IsSynchronous;
import org.feup.brunonova.drmips.simulator.mips.Output;
import org.feup.brunonova.drmips.simulator.util.Dimension;
import org.feup.brunonova.drmips.simulator.util.Point;

/**
 * Class that represents the synchronous register bank.
 * 
 * @author Bruno Nova
 */
public class RegBank extends Component implements IsSynchronous {
	private final Input readReg1, readReg2, writeReg, writeData, regWrite;
	private final Output readData1, readData2;
	private final Data[] registers;
	private final Set<Integer> constantRegisters; // indexes of the constant registers
	private final boolean forwarding; // use internal forwarding?
	private final Stack<int[]> states = new Stack<int[]>(); // previous values

	/**
	 * Register bank constructor.
	 * <p>Internal forwarding is disabled.</p>
	 * @param id Component's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param numRegisters Number of registers (must be <tt>&gt;1</tt> and power of 2).
	 * @param readReg1Id The ID of the first register address input.
	 * @param readReg2Id The ID of the second register address input.
	 * @param readData1Id The ID of the first register data output.
	 * @param readData2Id The ID of the second register data output.
	 * @param writeRegId The ID of the write address input.
	 * @param writeDataId The ID of the write data input.
	 * @param regWriteId The ID of the RegWrite control input (that controls whether to write the register).
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated or if the number of registers is invalid.
	 */
	public RegBank(String id, int latency, Point position, int numRegisters, String readReg1Id, String readReg2Id, String readData1Id, String readData2Id, String writeRegId, String writeDataId, String regWriteId) throws InvalidCPUException {
		this(id, latency, position, numRegisters, readReg1Id, readReg2Id, readData1Id, readData2Id, writeRegId, writeDataId, regWriteId, false);
	}
	
	/**
	 * Register bank constructor.
	 * @param id Component's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param numRegisters Number of registers (must be <tt>&gt;1</tt> and power of 2).
	 * @param readReg1Id The ID of the first register address input.
	 * @param readReg2Id The ID of the second register address input.
	 * @param readData1Id The ID of the first register data output.
	 * @param readData2Id The ID of the second register data output.
	 * @param writeRegId The ID of the write address input.
	 * @param writeDataId The ID of the write data input.
	 * @param regWriteId The ID of the RegWrite control input (that controls whether to write the register).
	 * @param forwarding Whether the data in the WriteData input should be forwarded to and output if reading and writing to the same register.
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated or if the number of registers is invalid.
	 */
	public RegBank(String id, int latency, Point position, int numRegisters, String readReg1Id, String readReg2Id, String readData1Id, String readData2Id, String writeRegId, String writeDataId, String regWriteId, boolean forwarding) throws InvalidCPUException {
		super(id, latency, "Registers", "regbank", "regbank_description", position, new Dimension(80, 100));
		
		if(numRegisters <= 1 || !Data.isPowerOf2(numRegisters)) 
			throw new InvalidCPUException("Invalid number of registers (must be a power of 2)!");
		
		this.forwarding = forwarding;
		constantRegisters = new HashSet<Integer>();
		
		// Initialize registers
		int requiredBits = Data.requiredNumberOfBits(numRegisters - 1);
		registers = new Data[numRegisters];
		for(int i = 0; i < numRegisters; i++)
			registers[i] = new Data();
		
		// Add inputs/outputs
		readReg1 = addInput(readReg1Id, new Data(requiredBits), IOPort.Direction.WEST, true, true);
		readReg2 = addInput(readReg2Id, new Data(requiredBits), IOPort.Direction.WEST, true, true);
		readData1 = addOutput(readData1Id, new Data(), IOPort.Direction.EAST, true);
		readData2 = addOutput(readData2Id, new Data(), IOPort.Direction.EAST, true);
		writeReg = addInput(writeRegId, new Data(requiredBits), IOPort.Direction.WEST, false, true);
		writeData = addInput(writeDataId, new Data(), IOPort.Direction.WEST, false, true);
		regWrite = addInput(regWriteId, new Data(1), Input.Direction.NORTH, false);
	}

	@Override
	public void execute() {
		int index1 = getReadReg1().getValue();
		int index2 = getReadReg2().getValue();
		
		if(isForwarding() && getWriteReg().getValue() == index1 && !isRegisterConstant(index1))
			getReadData1().setValue(getWriteData().getValue());
		else
			getReadData1().setValue(getRegister(index1).getValue());
		
		if(isForwarding() && getWriteReg().getValue() == index2 && !isRegisterConstant(index2))
			getReadData2().setValue(getWriteData().getValue());
		else
			getReadData2().setValue(getRegister(index2).getValue());
		
		boolean write = getRegWrite().getValue() == 1;
		getWriteReg().setRelevant(write);
		getWriteData().setRelevant(write);
	}

	@Override
	public void executeSynchronous() {
		if(getRegWrite().getValue() == 1 && !isRegisterConstant(getWriteReg().getValue()))
			registers[getWriteReg().getValue()].setValue(getWriteData().getValue());
	}

	@Override
	public void pushState() {
		int[] values = new int[getNumberOfRegisters()];
		for(int i = 0; i < getNumberOfRegisters(); i++)
			values[i] = registers[i].getValue();
		states.push(values);
	}

	@Override
	public void popState() {
		if(hasSavedStates()) {
			int[] values = states.pop();
			for(int i = 0; i < getNumberOfRegisters(); i++)
				registers[i].setValue(values[i]);
		}
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
		if(hasSavedStates()) {
			int[] values = states.peek();
			while(hasSavedStates())
				values = states.pop();
			for(int i = 0; i < getNumberOfRegisters(); i++)
				registers[i].setValue(values[i]);
		}
	}
	
	@Override
	public boolean isWritingState() {
		return getRegWrite().getValue() == 1;
	}
	
	/**
	 * Returns whether the data in the WriteData input should be forwarded to and output if reading and writing to the same register.
	 * @return <tt>True</tt> if internal forwarding is enabled.
	 */
	public final boolean isForwarding() {
		return forwarding;
	}
	
	/**
	 * Resets the register bank to zeros.
	 */
	public final void reset() {
		for (Data register: registers)
			register.setValue(0);
		execute();
	}
	
	/**
	 * Returns the number of registers.
	 * @return The number of registers.
	 */
	public final int getNumberOfRegisters() {
		return registers.length;
	}
	
	/**
	 * Returns how many bits are required to identify a register.
	 * @return Number of bits required to identify a register.
	 */
	public final int getRequiredBitsToIdentifyRegister() {
		return Data.requiredNumberOfBits(getNumberOfRegisters() - 1);
	}
	
	/**
	 * Returns a copy of the indicated register.
	 * @param index Index/address of the register.
	 * @return Copy of the indicated register.
	 * @throws ArrayIndexOutOfBoundsException If the index is invalid.
	 */
	public final Data getRegister(int index) throws ArrayIndexOutOfBoundsException {
		return registers[index].clone();
	}
	
	/**
	 * Updates the value of the indicated register.
	 * <p>The new register is propagated to the rest of the circuit if it is being read.</p>
	 * @param index Index/address of the register.
	 * @param newValue New value.
	 * @throws ArrayIndexOutOfBoundsException If the index is invalid.
	 */
	public final void setRegister(int index, int newValue) throws ArrayIndexOutOfBoundsException {
		setRegister(index, newValue, true);
	}
	
	/**
	 * Updates the value of the indicated register.
	 * @param index Index/address of the register.
	 * @param newValue New value.
	 * @param propagate Whether the new register is propagated to the rest of the circuit if it is being read.
	 * @throws ArrayIndexOutOfBoundsException If the index is invalid.
	 */
	public final void setRegister(int index, int newValue, boolean propagate) throws ArrayIndexOutOfBoundsException {
		if(!isRegisterConstant(index)) { // don't update constant registers
			registers[index].setValue(newValue);
			if(propagate) execute();
		}
	}
	
	/**
	 * Specifies that the indicated register is constant with the indicated value.
	 * @param index Index/address of the register.
	 * @param value Constant value.
	 * @throws ArrayIndexOutOfBoundsException If the index is invalid.
	 */
	public final void setRegisterConstant(int index, int value) throws ArrayIndexOutOfBoundsException {
		setRegister(index, value);
		constantRegisters.add(index);
	}
	
	/**
	 * Specifies that the indicated register is constant with 0 as value.
	 * @param index Index/address of the register.
	 * @throws ArrayIndexOutOfBoundsException If the index is invalid.
	 */
	public final void setRegisterConstant(int index) throws ArrayIndexOutOfBoundsException {
		setRegisterConstant(index, 0);
	}
	
	/**
	 * Returns whether the indicated register is constant;
	 * @param index Index/address of the register.
	 * @return <tt>true</tt> if the register is constant.
	 */
	public final boolean isRegisterConstant(int index) {
		return constantRegisters.contains(index);
	}
	
	/**
	 * Returns the first register address input.
	 * @return The first register address input.
	 */
	public final Input getReadReg1() {
		return readReg1;
	}
	
	/**
	 * Returns the second register address input.
	 * @return The second register address input.
	 */
	public final Input getReadReg2() {
		return readReg2;
	}
	
	/**
	 * Returns the first register data output.
	 * @return The first register data output.
	 */
	public final Output getReadData1() {
		return readData1;
	}
	
	/**
	 * Returns the second register data output.
	 * @return The second register data output.
	 */
	public final Output getReadData2() {
		return readData2;
	}
	
	/**
	 * Returns the write address input.
	 * @return The write address input.
	 */
	public final Input getWriteReg() {
		return writeReg;
	}
	
	/**
	 * Returns the write data input.
	 * @return The write data input.
	 */
	public final Input getWriteData() {
		return writeData;
	}
	
	/**
	 * Returns the RegWrite control input (that controls whether to write the register).
	 * @return The RegWrite control input.
	 */
	public final Input getRegWrite() {
		return regWrite;
	}
}
