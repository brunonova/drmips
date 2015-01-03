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

package org.feup.brunonova.drmips.simulator.mips.components;

import org.feup.brunonova.drmips.simulator.exceptions.InvalidCPUException;
import org.feup.brunonova.drmips.simulator.mips.Component;
import org.feup.brunonova.drmips.simulator.mips.Data;
import org.feup.brunonova.drmips.simulator.mips.Input;
import org.feup.brunonova.drmips.simulator.mips.Output;
import org.feup.brunonova.drmips.simulator.util.Dimension;
import org.feup.brunonova.drmips.simulator.util.Point;

/**
 * Class that represents a NOT port.
 * 
 * @author Bruno Nova
 */
public class Not extends Component {
	private final Input input;
	private final Output output;
	
	/**
	 * Not constructor.
	 * @param id Not's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param inId The identifier of the input.
	 * @param outId The identifier of the output.
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public Not(String id, int latency, Point position, String inId, String outId) throws InvalidCPUException {
		super(id, latency, "NOT", "not", "not_description", position, new Dimension(30, 30));
		input = addInput(inId, new Data(1));
		output = addOutput(outId, new Data(1));
	}
	
	@Override
	public void execute() {
		getOutput().setValue(~getInput().getValue());
	}
	
	/**
	 * Returns the input.
	 * @return Not's input;
	 */
	public final Input getInput() {
		return input;
	}
	
	/**
	 * Returns the output.
	 * @return Not's output;
	 */
	public final Output getOutput() {
		return output;
	}
}
