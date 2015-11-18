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
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that represents a zero extender.
 *
 * @author Bruno nova
 */
public class ZeroExtend extends Component {
	private final Input input;
	private final Output output;

	/**
	 * Component constructor.
	 * @param id The component's identifier.
	 * @param json The JSON object representing the component that should be parsed.
	 * @throws InvalidCPUException If the component has invalid parameters.
	 * @throws JSONException If the JSON object is invalid or incomplete.
	 */
	public ZeroExtend(String id, JSONObject json) throws InvalidCPUException, JSONException {
		super(id, json, "Zero\nextend", "zero_extend", "zero_extend_description", new Dimension(40, 40));

		JSONObject i = json.getJSONObject("in");
		JSONObject o = json.getJSONObject("out");
		int inSize = i.getInt("size"), outSize = o.getInt("size");

		if(inSize > outSize) {
			int aux = inSize;
			inSize = outSize;
			outSize = aux;
		}

		input = addInput(i.getString("id"), new Data(inSize), IOPort.Direction.WEST, true, true);
		output = addOutput(o.getString("id"), new Data(outSize), IOPort.Direction.EAST, true);
	}

	@Override
	public void execute() {
		getOutput().setValue(getInput().getData().zeroExtend(getOutput().getSize()).getValue());
	}

	/**
	 * Returns the input.
	 * @return The input;
	 */
	public final Input getInput() {
		return input;
	}

	/**
	 * Returns the output.
	 * @return The output;
	 */
	public final Output getOutput() {
		return output;
	}
}
