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

import java.util.HashSet;
import java.util.Set;
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
 * Class that represents the synchronous register bank.
 * 
 * @author Bruno Nova
 */
public class RegBank extends Component implements IsSynchronous {
	/** The ID of the first register address input. */
	private String readReg1Id;
	/** The ID of the second register address input. */
	private String readReg2Id;
	/** The ID of the first register data output. */
	private String readData1Id;
	/** The ID of the second register data output. */
	private String readData2Id;
	/** The ID of the write address input. */
	private String writeRegId;
	/** The ID of the write data input. */
	private String writeDataId;
	/** The ID of the RegWrite control input (that controls whether to write the register). */
	private String regWriteId;
	/** The array with the values of the registers. */
	private Data[] registers;
	/** The indexes of the registers that are constant. */
	private Set<Integer> constantRegisters;
	/** Whether the data in the WriteData input should be forwarded to and output if reading and writing to the same register. */
	private boolean forwarding;
	/** The previous values of the registers. */
	private Stack<int[]> states = new Stack<int[]>();

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
		this.readReg1Id = readReg1Id;
		this.readReg2Id = readReg2Id;
		this.readData1Id = readData1Id;
		this.readData2Id = readData2Id;
		this.writeRegId = writeRegId;
		this.writeDataId = writeDataId;
		this.regWriteId = regWriteId;
		constantRegisters = new HashSet<Integer>();
		
		// Initialize registers
		int requiredBits = Data.requiredNumberOfBits(numRegisters - 1);
		registers = new Data[numRegisters];
		for(int i = 0; i < numRegisters; i++)
			registers[i] = new Data();
		
		// Add inputs/outputs
		addInput(readReg1Id, new Data(requiredBits), IOPort.Direction.WEST, true, true);
		addInput(readReg2Id, new Data(requiredBits), IOPort.Direction.WEST, true, true);
		addOutput(readData1Id, new Data(), IOPort.Direction.EAST, true);
		addOutput(readData2Id, new Data(), IOPort.Direction.EAST, true);
		addInput(writeRegId, new Data(requiredBits), IOPort.Direction.WEST, true, true);
		addInput(writeDataId, new Data(), IOPort.Direction.WEST, false, true);
		addInput(regWriteId, new Data(1), Input.Direction.NORTH);
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
	
	/**
	 * Returns whether the data in the WriteData input should be forwarded to and output if reading and writing to the same register.
	 * @return <tt>True</tt> if internal forwarding is enabled.
	 */
	public boolean isForwarding() {
		return forwarding;
	}
	
	/**
	 * Resets the register bank to zeros.
	 */
	public void reset() {
		for(int i = 0; i < registers.length; i++)
			registers[i].setValue(0);
		execute();
	}
	
	/**
	 * Returns the number of registers.
	 * @return The number of registers.
	 */
	public int getNumberOfRegisters() {
		return registers.length;
	}
	
	/**
	 * Returns how many bits are required to identify a register.
	 * @return Number of bits required to identify a register.
	 */
	public int getRequiredBitsToIdentifyRegister() {
		return Data.requiredNumberOfBits(getNumberOfRegisters() - 1);
	}
	
	/**
	 * Returns a copy of the indicated register.
	 * @param index Index/address of the register.
	 * @return Copy of the indicated register.
	 * @throws ArrayIndexOutOfBoundsException If the index is invalid.
	 */
	public Data getRegister(int index) throws ArrayIndexOutOfBoundsException {
		return registers[index].clone();
	}
	
	/**
	 * Updates the value of the indicated register.
	 * <p>The new register is propagated to the rest of the circuit if it is being read.</p>
	 * @param index Index/address of the register.
	 * @param newValue New value.
	 * @throws ArrayIndexOutOfBoundsException If the index is invalid.
	 */
	public void setRegister(int index, int newValue) throws ArrayIndexOutOfBoundsException {
		setRegister(index, newValue, true);
	}
	
	/**
	 * Updates the value of the indicated register.
	 * @param index Index/address of the register.
	 * @param newValue New value.
	 * @param propagate Whether the new register is propagated to the rest of the circuit if it is being read.
	 * @throws ArrayIndexOutOfBoundsException If the index is invalid.
	 */
	public void setRegister(int index, int newValue, boolean propagate) throws ArrayIndexOutOfBoundsException {
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
	public void setRegisterConstant(int index, int value) throws ArrayIndexOutOfBoundsException {
		setRegister(index, value);
		constantRegisters.add(index);
	}
	
	/**
	 * Specifies that the indicated register is constant with 0 as value.
	 * @param index Index/address of the register.
	 * @throws ArrayIndexOutOfBoundsException If the index is invalid.
	 */
	public void setRegisterConstant(int index) throws ArrayIndexOutOfBoundsException {
		setRegisterConstant(index, 0);
	}
	
	/**
	 * Returns whether the indicated register is constant;
	 * @param index Index/address of the register.
	 * @return <tt>true</tt> if the register is constant.
	 */
	public boolean isRegisterConstant(int index) {
		return constantRegisters.contains(index);
	}
	
	/**
	 * Returns the ID of the first register address input.
	 * @return The ID of the first register address input.
	 */
	public String getReadReg1Id() {
		return readReg1Id;
	}
	
	/**
	 * Returns the ID of the second register address input.
	 * @return The ID of the second register address input.
	 */
	public String getReadReg2Id() {
		return readReg2Id;
	}
	
	/**
	 * Returns the ID of the first register data output.
	 * @return The ID of the first register data output.
	 */
	public String getReadData1Id() {
		return readData1Id;
	}
	
	/**
	 * Returns the ID of the second register data output.
	 * @return The ID of the second register data output.
	 */
	public String getReadData2Id() {
		return readData2Id;
	}
	
	/**
	 * Returns the ID of the write address input.
	 * @return The ID of the write address input.
	 */
	public String getWriteRegId() {
		return writeRegId;
	}
	
	/**
	 * Returns the ID of the write data input.
	 * @return The ID of the write data input.
	 */
	public String getWriteDataId() {
		return writeDataId;
	}
	
	/**
	 * Returns the ID of the RegWrite control input (that controls whether to write the register).
	 * @return The ID of the RegWrite control input.
	 */
	public String getRegWriteId() {
		return regWriteId;
	}
	
	/**
	 * Returns the first register address input.
	 * @return The first register address input.
	 */
	public Input getReadReg1() {
		return getInput(readReg1Id);
	}
	
	/**
	 * Returns the second register address input.
	 * @return The second register address input.
	 */
	public Input getReadReg2() {
		return getInput(readReg2Id);
	}
	
	/**
	 * Returns the first register data output.
	 * @return The first register data output.
	 */
	public Output getReadData1() {
		return getOutput(readData1Id);
	}
	
	/**
	 * Returns the second register data output.
	 * @return The second register data output.
	 */
	public Output getReadData2() {
		return getOutput(readData2Id);
	}
	
	/**
	 * Returns the write address input.
	 * @return The write address input.
	 */
	public Input getWriteReg() {
		return getInput(writeRegId);
	}
	
	/**
	 * Returns the write data input.
	 * @return The write data input.
	 */
	public Input getWriteData() {
		return getInput(writeDataId);
	}
	
	/**
	 * Returns the RegWrite control input (that controls whether to write the register).
	 * @return The RegWrite control input.
	 */
	public Input getRegWrite() {
		return getInput(regWriteId);
	}
}
