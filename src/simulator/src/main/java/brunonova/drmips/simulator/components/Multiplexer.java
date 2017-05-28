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
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that represents a multiplexer.
 *
 * @author bruno
 */
public class Multiplexer extends Component {
	private final Input selector;
	private final Output output;
	private final List<Input> inputs; // inputs (excluding the selector)

	/**
	 * Component constructor.
	 * @param id The component's identifier.
	 * @param json The JSON object representing the component that should be parsed.
	 * @throws InvalidCPUException If the component has invalid parameters.
	 * @throws JSONException If the JSON object is invalid or incomplete.
	 */
	public Multiplexer(String id, JSONObject json) throws InvalidCPUException, JSONException {
		super(id, json, "M\nU\nX", "multiplexer", "multiplexer_description", new Dimension(15, 35));

		// Add the inputs
		JSONArray ins = json.getJSONArray("in");
		inputs = new ArrayList<>(ins.length());
		int size = json.getInt("size");
		for(int x = 0; x < ins.length(); x++) {
			inputs.add(addInput(ins.getString(x), new Data(size)));
		}

		selector = addInput(json.getString("sel"), new Data((ins.length() > 0) ? Data.requiredNumberOfBits(ins.length() - 1) : 1), IOPort.Direction.NORTH);
		output = addOutput(json.getString("out"), new Data(size));
	}

	@Override
	public void execute() {
		int sel = getSelector().getValue();
		Input input;
		for(int i = 0; i < inputs.size(); i++) { // put selected input value in output and mark other inputs as irrelevant
			input = getInput(i);
			if(i == sel) getOutput().setValue(input.getValue());
			input.setRelevant(i == sel);
		}
	}

	@Override
	protected List<Input> getLatencyInputs() {
		ArrayList<Input> inList = new ArrayList<>();
		// always add control input
		inList.add(getSelector());
		// add the selected input to the list of inputs that
		// influence latency
		Input selInput = getInput(getSelector().getValue());
		if (selInput != null)
			inList.add(selInput);
		return inList;
	}

	/**
	 * Returns the multiplexer's output.
	 * @return Multiplexer output;
	 */
	public final Output getOutput() {
		return output;
	}

	/**
	 * Returns the multiplexer's selector.
	 * @return Muoltiplexer selector;
	 */
	public final Input getSelector() {
		return selector;
	}

	/**
	 * Returns the input with the specified index.
	 * @param index Index of the input.
	 * @return The input, or <tt>null</tt> if it doesn't exist.
	 */
	public final Input getInput(int index) {
		if(index >= 0 && index < inputs.size())
			return inputs.get(index);
		else
			return null;
	}
}
