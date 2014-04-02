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

package org.feup.brunonova.drmips.simulator.mips.components;

import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
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
 * Class that represents a synchronous register that divides two stages of the pipeline.
 * 
 * @author Bruno Nova
 */
public class PipelineRegister extends Component implements IsSynchronous {
	/** The identifier of the write input. */
	private String writeId;
	/** The identifier of the flush input. */
	private String flushId;
	/** The stored registers (mapped by their ids). */
	private Map<String, Data> registers;
	/** The previous saved registers. */
	private final Stack<Map<String, Data>> states = new Stack<Map<String, Data>>();
	/** The index of the current instruction being executed (-1 if none). */
	private int currentInstructionIndex = -1;
	/** The indexes of the previous instructions. */
	private final Stack<Integer> instructions = new Stack<Integer>();
	
	/**
	 * Pipeline register constructor.
	 * @param id Pipeline registers's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param registers The identifiers and sizes of the values to store.
	 * @throws InvalidCPUException 
	 */
	public PipelineRegister(String id, int latency, Point position, Map<String, Integer> registers) throws InvalidCPUException {
		this(id, latency, position, registers, "Write", "Flush");
	}
	
	/**
	 * Pipeline register constructor.
	 * @param id Pipeline registers's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param registers The identifiers and sizes of the values to store.
	 * @param writeId The identifier of the write input.
	 * @param flushId The identifier of the flush input.
	 * @throws InvalidCPUException 
	 */
	public PipelineRegister(String id, int latency, Point position, Map<String, Integer> registers, String writeId, String flushId) throws InvalidCPUException {
		super(id, latency, "", "pipeline_register", "pipeline_register_description", position, new Dimension(15, 300));
		this.registers = new TreeMap<String, Data>();
		this.writeId = writeId;
		this.flushId = flushId;
		setDisplayName();
		
		addInput(writeId, new Data(1, 1), IOPort.Direction.NORTH, false);
		addInput(flushId, new Data(1, 0), IOPort.Direction.NORTH, false);
		
		for(Map.Entry<String, Integer> e: registers.entrySet()) {
			addInput(e.getKey(), new Data(e.getValue()), IOPort.Direction.WEST, false);
			addOutput(e.getKey(), new Data(e.getValue()));
			this.registers.put(e.getKey(), new Data(e.getValue()));
		}
	}

	@Override
	public void execute() {
		boolean stall = getWrite().getValue() == 0 || getFlush().getValue() == 1;
		Input input;
		Output output;
		
		for(Map.Entry<String, Data> e: registers.entrySet()) {
			input = getInput(e.getKey());
			output = getOutput(e.getKey());
			output.setValue(e.getValue().getValue());
			
			if(stall) // mark input as irrelevant if stalled
				input.setRelevant(false);
			else if(input.getSize() != 1) // 1 bit inputs are set as relevant/irrelevant automatically
				input.setRelevant(true);
			
			// mark output as irrelevant if connected to a stalled pipeline register (and it they are 1 bit)
			if(output.getSize() == 1 && output.isConnected() && output.getConnectedInput().getComponent() instanceof PipelineRegister) {
				PipelineRegister p = (PipelineRegister)output.getConnectedInput().getComponent();
				if(p.getWrite().getValue() == 0 || p.getFlush().getValue() == 1)
					output.setRelevant(false);
			}
		}
	}

	@Override
	public void executeSynchronous() {
		boolean flush = getFlush().getValue() == 1;
		if(getWrite().getValue() == 1 || flush) {
			for(Map.Entry<String, Data> e: registers.entrySet())
				e.getValue().setValue(flush ? 0 : getInput(e.getKey()).getValue());
		}
	}

	@Override
	public void pushState() {
		states.push(cloneRegisters());
		instructions.push(getCurrentInstructionIndex());
	}

	@Override
	public void popState() {
		if(hasSavedStates()) {
			registers = states.pop();
			setCurrentInstructionIndex(instructions.pop());
		}
	}

	@Override
	public boolean hasSavedStates() {
		return !states.isEmpty();
	}

	@Override
	public void clearSavedStates() {
		states.clear();
		instructions.clear();
		for(Map.Entry<String, Data> e: registers.entrySet()) // also clear registers
			e.getValue().setValue(0);
		execute();
	}
	
	@Override
	public void resetFirstState() {
		while(hasSavedStates())
			popState();
	}
	
	@Override
	public boolean isWritingState() {
		return getWrite().getValue() == 1 && getFlush().getValue() == 0;
	}
	
	/**
	 * Sets the pipeline register's display name.
	 * <p>The name corresponds to the component's identifier, 1 letter per line.</p>
	 */
	private void setDisplayName() {
		String name = "";
		for(int i = 0; i < getId().length(); i++) {
			if(i > 0) name += "\n";
			name += getId().charAt(i);
		}
		setDisplayName(name);
	}
	
	/**
	 * Returns a copy of the stored registers.
	 * @return Copy of the registers.
	 */
	private Map<String, Data> cloneRegisters() {
		Map<String, Data> map = new TreeMap<String, Data>();
		for(Map.Entry<String, Data> e: registers.entrySet())
			map.put(e.getKey(), e.getValue().clone());
		return map;
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
	 * Returns the identifier of the write input.
	 * @return The identifier of the write input.
	 */
	public String getWriteId() {
		return writeId;
	}

	/**
	 * Returns the identifier of the flush input.
	 * @return The identifier of the flush input.
	 */
	public String getFlushId() {
		return flushId;
	}
	
	/**
	 * Returns the write input.
	 * @return Write input.
	 */
	public Input getWrite() {
		return getInput(writeId);
	}
	
	/**
	 * Returns the flush input.
	 * @return Flush input.
	 */
	public Input getFlush() {
		return getInput(flushId);
	}
}
