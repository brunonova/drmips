/*
    DrMIPS - Educational MIPS simulator
    Copyright (C) 2013-2015 Bruno Nova

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

import brunonova.drmips.simulator.Component;
import brunonova.drmips.simulator.Data;
import brunonova.drmips.simulator.Output;
import brunonova.drmips.simulator.exceptions.InvalidCPUException;
import brunonova.drmips.simulator.util.Dimension;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Component that simply outputs a constant number.
 *
 * @author Bruno Nova
 */
public class Constant extends Component {
	private final Output output;
	private final int value;

	/**
	 * Component constructor.
	 * @param id The component's identifier.
	 * @param json The JSON object representing the component that should be parsed.
	 * @throws InvalidCPUException If the component has invalid parameters.
	 * @throws JSONException If the JSON object is invalid or incomplete.
	 */
	public Constant(String id, JSONObject json) throws InvalidCPUException, JSONException {
		super(id, json, "" + json.getInt("val"), "constant", "constant_description", new Dimension(20, 15));
		value = json.getInt("val");
		output = addOutput(json.getString("out"), new Data(json.getInt("size")));
	}

	@Override
	public void execute() {
		getOutput().setValue(value);
	}

	/**
	 * Returns the output.
	 * @return The output;
	 */
	public final Output getOutput() {
		return output;
	}
}
