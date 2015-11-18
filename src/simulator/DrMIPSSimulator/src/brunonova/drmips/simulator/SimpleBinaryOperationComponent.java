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

package brunonova.drmips.simulator;

import brunonova.drmips.simulator.exceptions.InvalidCPUException;
import brunonova.drmips.simulator.util.Dimension;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that represents a simple component that has 2 inputs and produces 1 output using a simple operation.
 *
 * @author Bruno Nova
 */
public abstract class SimpleBinaryOperationComponent extends Component {
	private final Input input1, input2;
	private final Output output;

	/**
	 * Component constructor.
	 * @param id The component's identifier.
	 * @param json The JSON object representing the component that should be parsed.
	 * @param displayName The name displayed on the GUI.
	 * @param nameKey The key of the component's name on the language file, shown on the component's tooltip.
	 * @param descriptionKey The key of the component's description on the language file.
	 * @param size The size of the component on the GUI.
	 * @param dataSize The size of the inputs and outputs (if greater than 1, a balloon tip is displayed).
	 * @throws InvalidCPUException If the component has invalid parameters.
	 * @throws JSONException If the JSON object is invalid or incomplete.
	 */
	public SimpleBinaryOperationComponent(String id, JSONObject json, String displayName, String nameKey, String descriptionKey, Dimension size, int dataSize) throws InvalidCPUException, JSONException {
		super(id, json, displayName, nameKey, descriptionKey, size);
		boolean showTip = dataSize > 1;
		input1 = addInput(json.getString("in1"), new Data(dataSize), IOPort.Direction.WEST, true, showTip);
		input2 = addInput(json.getString("in2"), new Data(dataSize), IOPort.Direction.WEST, true, showTip);
		output = addOutput(json.getString("out"), new Data(dataSize), IOPort.Direction.EAST, showTip);
	}

	@Override
	public void execute() {
		getOutput().setValue(operation(getInput1().getValue(), getInput2().getValue()));
	}

	/**
	 * Calculates the result of the binary operation between the two inputs.
	 * <p>Subclasses must implement this method.</p>
	 * @param in1 The first input's value.
	 * @param in2 The second input's value.
	 * @return The result.
	 */
	protected abstract int operation(int in1, int in2);

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