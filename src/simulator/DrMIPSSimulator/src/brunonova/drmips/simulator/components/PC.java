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
import java.util.Stack;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that represents the synchronous Program Counter.
 *
 * @author Bruno Nova
 */
public class PC extends Component implements Synchronous {
	private final Input input, write;
	private final Output output;
	private final Data address;
	private final Stack<Integer> states = new Stack<>(); // previous adresses
	private int currentInstructionIndex = -1;
	private final Stack<Integer> instructions = new Stack<>(); // previous instructions

	/**
	 * Component constructor.
	 * @param id The component's identifier.
	 * @param json The JSON object representing the component that should be parsed.
	 * @throws InvalidCPUException If the component has invalid parameters.
	 * @throws JSONException If the JSON object is invalid or incomplete.
	 */
	public PC(String id, JSONObject json) throws InvalidCPUException, JSONException {
		super(id, json, "PC", "pc", "pc_description", new Dimension(30, 30));
		address = new Data();
		input = addInput(json.getString("in"), new Data(), IOPort.Direction.WEST, false, true);
		output = addOutput(json.getString("out"), new Data(), IOPort.Direction.EAST, true);
		write = addInput(json.optString("write", "Write"), new Data(1, 1), IOPort.Direction.NORTH, false);
	}

	@Override
	public void execute() {
		getOutput().setValue(getAddress().getValue());
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

	@Override
	public boolean isWritingState() {
		return getWrite().getValue() == 1;
	}

	/**
	 * Returns the current address of the Program Counter (the <tt>$pc</tt> register).
	 * @return Current address.
	 */
	public final Data getAddress() {
		return address;
	}

	/**
	 * Updates the addres of the Program Counter (the <tt>$pc</tt> register).
	 * <p>The new address is propagated to the rest of the circuit.</p>
	 * @param address New address.
	 */
	public final void setAddress(int address) {
		setAddress(address, true);
	}

	/**
	 * Updates the addres of the Program Counter (the <tt>$pc</tt> register).
	 * @param address New address.
	 * @param propagate Whether the new address is propagated to the rest of the circuit.
	 */
	public final void setAddress(int address, boolean propagate) {
		this.address.setValue(address);
		if(propagate) execute();
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
	 * Returns the Program Counter's input.
	 * @return PC input;
	 */
	public final Input getInput() {
		return input;
	}

	/**
	 * Returns the the write input.
	 * @return Write input.
	 */
	public final Input getWrite() {
		return write;
	}

	/**
	 * Returns the Program Counter's output.
	 * @return PC output;
	 */
	public final Output getOutput() {
		return output;
	}
}
