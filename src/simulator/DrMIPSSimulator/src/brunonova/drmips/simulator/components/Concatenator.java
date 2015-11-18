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
import brunonova.drmips.simulator.util.Point;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This component concatenates the values of the two inputs.
 * <p>The 1st input is concatenated in the most significant bits, while the 2nd is
 * concatenated to the less significant bits.<br>
 * Concatenating 111 with 0000, in that order, would give 1110000</p>
 *
 * @author Bruno Nova
 */
public class Concatenator extends Component {
	private final Input input1, input2;
	private final Output output;

	/**
	 * Component constructor.
	 * @param id The component's identifier.
	 * @param json The JSON object representing the component that should be parsed.
	 * @throws InvalidCPUException If the component has invalid parameters.
	 * @throws JSONException If the JSON object is invalid or incomplete.
	 */
	public Concatenator(String id, JSONObject json) throws InvalidCPUException, JSONException {
		super(id, json, "", "concatenator", "concatenator_description", new Dimension(5, 5));

		JSONObject in1 = json.getJSONObject("in1");
		JSONObject in2 = json.getJSONObject("in2");
		int size1 = in1.getInt("size"), size2 = in2.getInt("size");
		input1 = addInput(in1.getString("id"), new Data(size1));
		input2 = addInput(in2.getString("id"), new Data(size2));
		input1.setPosition(getPosition());
		input2.setPosition(getPosition());

		output = addOutput(json.getString("out"), new Data(size1 + size2));
		output.setPosition(getPosition());

		// Adjust the position
		setPosition(new Point(getPosition().x - 2, getPosition().y - 2));
	}

	@Override
	public void execute() {
		getOutput().setValue((getInput1().getValue() << getInput2().getSize()) | getInput2().getValue());
	}

	/**
	 * Returns the component's first input.
	 * @return Component's first input;
	 */
	public final Input getInput1() {
		return input1;
	}

	/**
	 * Returns the component's second input.
	 * @return Component's second input;
	 */
	public final Input getInput2() {
		return input2;
	}

	/**
	 * Returns the component's output.
	 * @return Component's output;
	 */
	public final Output getOutput() {
		return output;
	}
}
