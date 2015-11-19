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

package brunonova.drmips.simulator.components;

import brunonova.drmips.simulator.*;
import brunonova.drmips.simulator.exceptions.InvalidCPUException;
import brunonova.drmips.simulator.util.Dimension;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that represents a synchronous register that divides two stages of the pipeline.
 *
 * @author Bruno Nova
 */
public class PipelineRegister extends Component implements Synchronous {
	private final Input write, flush;
	private Map<String, Data> registers; // stored values
	private final Stack<Map<String, Data>> states = new Stack<>(); // previous values
	private int currentInstructionIndex = -1;
	private final Stack<Integer> instructions = new Stack<>(); // previous instructions

	/**
	 * Component constructor.
	 * @param id The component's identifier.
	 * @param json The JSON object representing the component that should be parsed.
	 * @throws InvalidCPUException If the component has invalid parameters.
	 * @throws JSONException If the JSON object is invalid or incomplete.
	 */
	public PipelineRegister(String id, JSONObject json) throws InvalidCPUException, JSONException {
		super(id, json, "", "pipeline_register", "pipeline_register_description", new Dimension(15, 300));
		setDisplayName();

		write = addInput(json.optString("write", "Write"), new Data(1, 1), IOPort.Direction.NORTH, false);
		flush = addInput(json.optString("flush", "Flush"), new Data(1, 0), IOPort.Direction.NORTH, false);

		// Add the pipeline "registers", plus their inputs and outputs
		String name;
		JSONObject regs = json.getJSONObject("regs");
		registers = new HashMap<>(32);
		Iterator<String> i = regs.keys();
		while(i.hasNext()) {
			name = i.next();
			addInput(name, new Data(regs.getInt(name)), IOPort.Direction.WEST, false);
			addOutput(name, new Data(regs.getInt(name)));
			this.registers.put(name, new Data(regs.getInt(name)));
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
		boolean f = getFlush().getValue() == 1; // flush?
		if(getWrite().getValue() == 1 || f) {
			for(Map.Entry<String, Data> e: registers.entrySet())
				e.getValue().setValue(f ? 0 : getInput(e.getKey()).getValue());
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
		Map<String, Data> map = new HashMap<>();
		for(Map.Entry<String, Data> e: registers.entrySet())
			map.put(e.getKey(), e.getValue().clone());
		return map;
	}

	/**
	 * Returns the index of the current instruction being executed.
	 * @return Index of the current instruction being executed (-1 if none).
	 */
	public final int getCurrentInstructionIndex() {
		return currentInstructionIndex;
	}

	/**
	 * Updates the index of the current instruction being executed.
	 * @param currentInstructionIndex The index of the instruction (-1 if none).
	 */
	public final void setCurrentInstructionIndex(int currentInstructionIndex) {
		this.currentInstructionIndex = currentInstructionIndex;
	}

	/**
	 * Returns the write input.
	 * @return Write input.
	 */
	public final Input getWrite() {
		return write;
	}

	/**
	 * Returns the flush input.
	 * @return Flush input.
	 */
	public final Input getFlush() {
		return flush;
	}
}
