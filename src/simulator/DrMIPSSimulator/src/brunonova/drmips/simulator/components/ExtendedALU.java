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

import brunonova.drmips.simulator.Data;
import brunonova.drmips.simulator.exceptions.InvalidCPUException;
import java.util.Stack;
import org.json.JSONException;
import org.json.JSONObject;
import brunonova.drmips.simulator.Synchronous;

/**
 * An ALU that supports multiplications and divisions, and contains the <tt>HI</tt> and <tt>LO</tt> "registers".
 *
 * @author Bruno Nova
 */
public class ExtendedALU extends ALU implements Synchronous {
	private final Data hi, lo;
	private final Stack<int[]> states = new Stack<>(); // previous values

	/**
	 * Component constructor.
	 * @param id The component's identifier.
	 * @param json The JSON object representing the component that should be parsed.
	 * @throws InvalidCPUException If the component has invalid parameters.
	 * @throws JSONException If the JSON object is invalid or incomplete.
	 */
	public ExtendedALU(String id, JSONObject json) throws InvalidCPUException, JSONException {
		super(id, json);
		setNameKey("extended_alu");
		setDescriptionKey("extended_alu_description");

		hi = new Data();
		lo = new Data();
	}

	@Override
	public void executeSynchronous() {
		controlALU.doSynchronousOperation(getInput1().getValue(), getInput2().getValue(), this, getControl().getValue());
	}

	@Override
	public void pushState() {
		states.push(new int[] {hi.getValue(), lo.getValue()});
	}

	@Override
	public void popState() {
		if(hasSavedStates()) {
			int[] val = states.pop();
			hi.setValue(val[0]);
			lo.setValue(val[1]);
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
		while(hasSavedStates())
			popState();
	}

	@Override
	public boolean isWritingState() {
		return controlALU.isWritingState(getControl().getValue());
	}

	/**
	 * Returns the <tt>HI</tt> "register".
	 * @return The <tt>HI</tt> "register".
	 */
	public final Data getHI() {
		return hi;
	}

	/**
	 * Returns the <tt>HI</tt> "register".
	 * @return The <tt>HI</tt> "register".
	 */
	public final Data getLO() {
		return lo;
	}

	/**
	 * Resets the <tt>HI</tt> and <tt>LO</tt> registers to 0.
	 */
	public final void reset() {
		hi.setValue(0);
		lo.setValue(0);
	}
}
