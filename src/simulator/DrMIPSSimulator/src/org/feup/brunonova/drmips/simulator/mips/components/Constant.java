/*
    DrMIPS - Educational MIPS simulator
    Copyright (C) 2013-2015 Bruno Nova <ei08109@fe.up.pt>

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

package org.feup.brunonova.drmips.simulator.mips.components;

import org.feup.brunonova.drmips.simulator.exceptions.InvalidCPUException;
import org.feup.brunonova.drmips.simulator.mips.Component;
import org.feup.brunonova.drmips.simulator.mips.Data;
import org.feup.brunonova.drmips.simulator.mips.Output;
import org.feup.brunonova.drmips.simulator.util.Dimension;
import org.feup.brunonova.drmips.simulator.util.Point;

/**
 * Component that simply outputs a constant number.
 * 
 * @author Bruno Nova
 */
public class Constant extends Component {
	private final Output output;
	private final int value;
	
	/**
	 * Constant constructor.
	 * @param id Constant's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param outId The identifier of the output.
	 * @param value The contant value.
	 * @param size The size of the output.
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public Constant(String id, int latency, Point position, String outId, int value, int size) throws InvalidCPUException {
		super(id, latency, "" + value, "constant", "constant_description", position, new Dimension(20, 15));
		this.value = value;
		output = addOutput(outId, new Data(size));
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
