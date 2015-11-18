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
 * Class that represents the CPU control unit.
 *
 * @author Bruno Nova
 */
public class ControlUnit extends Component {
	private Input input;
	private String inId; // temporary
	private Control control = null;

	/**
	 * Component constructor.
	 * @param id The component's identifier.
	 * @param json The JSON object representing the component that should be parsed.
	 * @throws InvalidCPUException If the component has invalid parameters.
	 * @throws JSONException If the JSON object is invalid or incomplete.
	 */
	public ControlUnit(String id, JSONObject json) throws InvalidCPUException, JSONException {
		super(id, json, "Control", "control_unit", "control_unit_description", new Dimension(60, 100));
		inId = json.getString("in");
	}

	@Override
	public void execute() {
		int opcode = getInput().getValue();

		for(String o: control.getOutputsIds())
			getOutput(o).setValue(control.getOutOfOpcode(opcode, o));
	}

	/**
	 * Sets the control information for the control unit.
	 * <p>This method should be called after the instruction set has been loaded.</p>
	 * @param control Control information.
	 * @param opcodeSize The size of the opcode field.
	 * @throws InvalidCPUException If an output is duplicated.
	 */
	public final void setControl(Control control, int opcodeSize) throws InvalidCPUException {
		this.control = control;

		// Add input
		input = addInput(inId, new Data(opcodeSize));
		inId = null;

		// Add outputs
		for(String o: control.getOutputsIds())
			addOutput(o, new Data(control.getOutSize(o)));
	}

	/**
	 * Returns the control unit's input.
	 * @return Control unit input;
	 */
	public final Input getInput() {
		return input;
	}
}
