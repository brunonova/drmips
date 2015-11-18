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

import brunonova.drmips.simulator.SimpleBinaryOperationComponent;
import brunonova.drmips.simulator.Input;
import brunonova.drmips.simulator.exceptions.InvalidCPUException;
import brunonova.drmips.simulator.util.Dimension;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that represents an AND port with 2 inputs and 1 output with the size of 1 bit.
 *
 * @author Bruno Nova
 */
public class And extends SimpleBinaryOperationComponent {
	/**
	 * Component constructor.
	 * @param id The component's identifier.
	 * @param json The JSON object representing the component that should be parsed.
	 * @throws InvalidCPUException If the component has invalid parameters.
	 * @throws JSONException If the JSON object is invalid or incomplete.
	 */
	public And(String id, JSONObject json) throws InvalidCPUException, JSONException {
		super(id, json, "AND", "and", "and_description", new Dimension(30, 30), 1);
	}

	@Override
	protected int operation(int in1, int in2) {
		return in1 & in2;
	}

	@Override
	protected List<Input> getLatencyInputs() {
		ArrayList<Input> inList = new ArrayList<>();
		int val1 = getInput1().getValue();
		int val2 = getInput2().getValue();
		if (val1 == val2) {  // inputs have identical logic values
			if (val1 == 1) {  // both 1; use both
				inList.add(getInput1());
				inList.add(getInput2());
			} else { // both 0; use just the earliest input
				int lat1 = getInput1().getAccumulatedLatency();
				int lat2 = getInput2().getAccumulatedLatency();
				if (lat1 <= lat2) {
					inList.add(getInput1());
				} else {
					inList.add(getInput2());
				}
			}
		} else if (val1 == 1) { // only val2 == 0
			inList.add(getInput2());
		} else { // only val1 == 0
			inList.add(getInput1());
		}
		return inList;
	}
}
