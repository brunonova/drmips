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

import brunonova.drmips.simulator.Component;
import brunonova.drmips.simulator.Data;
import brunonova.drmips.simulator.Input;
import brunonova.drmips.simulator.exceptions.InvalidCPUException;
import brunonova.drmips.simulator.util.Dimension;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This component splits the input's value in parts and sends each part into each output.
 *
 * @author Bruno Nova
 */
public class Distributor extends Component {
	private final Input input;
	private final List<OutputParameters> outParameters;

	/**
	 * Component constructor.
	 * @param id The component's identifier.
	 * @param json The JSON object representing the component that should be parsed.
	 * @throws InvalidCPUException If the component has invalid parameters.
	 * @throws JSONException If the JSON object is invalid or incomplete.
	 */
	public Distributor(String id, JSONObject json) throws InvalidCPUException, JSONException {
		super(id, json, "", "distributor", "distributor_description", new Dimension(5, 30));

		JSONObject i = json.getJSONObject("in");
		input = addInput(i.getString("id"), new Data(i.getInt("size")));

		// Add the outputs
		JSONObject o;
		JSONArray outs = json.getJSONArray("out");
		outParameters = new LinkedList<>();
		int msb, lsb;
		for(int x = 0; x < outs.length(); x++) {
			o = outs.getJSONObject(x);
			msb = o.getInt("msb");
			lsb = o.getInt("lsb");
			addOutput(o.optString("id", msb + "-" + lsb), msb, lsb);
		}
	}

	/**
	 * Adds an output.
	 * @param id The identifier of the output.
	 * @param msb The most significant bit of the value to put.
	 * @param lsb The less significant bit of the value to put.
	 * @throws InvalidCPUException If <tt>id</tt> is empty.
	 */
	private void addOutput(String id, int msb, int lsb) throws InvalidCPUException {
		OutputParameters param = new OutputParameters(id, msb, lsb, getInput().getSize());
		outParameters.add(param);
		addOutput(id, new Data(param.msb - param.lsb + 1));
	}

	@Override
	public void execute() {
		int value = getInput().getValue();
		for(OutputParameters o: outParameters) {
			getOutput(o.id).setValue(o.getValueForOutput(value));
		}
	}

	/**
	 * Returns the distributor's input.
	 * @return Distributor input;
	 */
	public final Input getInput() {
		return input;
	}

	/**
	 * Contains the parameters (MSB, LSB, id) for an output of a distributor.
	 */
	private class OutputParameters {
		private String id; // output identifier
		private int msb, lsb, mask; // most/less significant bits for the value and corresponding mask

		/**
		 * Creates the parameters for an output.
		 * @param id The identifier of the output.
		 * @param msb The most significant bit of the value to put.
		 * @param lsb The less significant bit of the value to put.
		 * @param maxSize The size of the distributor's input.
		 * @throws InvalidCPUException If <tt>id</tt> is empty.
		 */
		public OutputParameters(String id, int msb, int lsb, int inSize) throws InvalidCPUException {
			if(id.isEmpty()) throw new InvalidCPUException("Invalid ID " + id + "!");
			this.id = id;

			if(msb > (inSize - 1)) msb = inSize - 1;
			else if(msb < 0) msb = 0;
			if(lsb > (inSize - 1)) lsb = inSize - 1;
			else if(lsb < 0) lsb = 0;
			if(lsb > msb) {
				int aux = msb;
				msb = lsb;
				lsb = aux;
			}

			this.msb = msb;
			this.lsb = lsb;

			mask = Data.createMask(msb, lsb);
		}

		/**
		 * Returns the value masked and shifted for this output's parameters.
		 * @param value The original value.
		 * @return The value formatted for the output.
		 */
		public final int getValueForOutput(int value) {
			return (value & mask) >>> lsb;
		}
	}
}
