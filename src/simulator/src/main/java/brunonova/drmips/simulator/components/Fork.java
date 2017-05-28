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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This component splits its input in several outputs (with the same value and size).
 *
 * @author Bruno Nova
 */
public class Fork extends Component {
	private final Input input;

	/**
	 * Component constructor.
	 * @param id The component's identifier.
	 * @param json The JSON object representing the component that should be parsed.
	 * @throws InvalidCPUException If the component has invalid parameters.
	 * @throws JSONException If the JSON object is invalid or incomplete.
	 */
	public Fork(String id, JSONObject json) throws InvalidCPUException, JSONException {
		super(id, json, "", "fork", "fork_description", new Dimension(5, 5));

		int size = json.getInt("size");
		input = addInput(json.getString("in"), new Data(size));
		input.setPosition(getPosition());

		// Add the outputs
		Output o;
		JSONArray outs = json.getJSONArray("out");
		for(int x = 0; x < outs.length(); x++) {
			o = addOutput(outs.getString(x), new Data(size));
			o.setPosition(getPosition());
		}

		// Adjust the position
		setPosition(new Point(getPosition().x - 2, getPosition().y - 2));
	}

	@Override
	public void execute() {
		for(Output o: getOutputs())
			o.setValue(getInput().getValue());
	}

	/**
	 * Returns the splits's input.
	 * @return Fork input;
	 */
	public final Input getInput() {
		return input;
	}
}
