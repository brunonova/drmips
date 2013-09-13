/*
    DrMIPS - Educational MIPS simulator
    Copyright (C) 2013 Bruno Nova <ei08109@fe.up.pt>

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

package org.feup.brunonova.drmips.mips.components;

import java.util.List;
import org.feup.brunonova.drmips.exceptions.InvalidCPUException;
import org.feup.brunonova.drmips.mips.Component;
import org.feup.brunonova.drmips.mips.Data;
import org.feup.brunonova.drmips.mips.Input;
import org.feup.brunonova.drmips.mips.Output;
import org.feup.brunonova.drmips.util.Dimension;
import org.feup.brunonova.drmips.util.Point;

/**
 * This component splits its input in several outputs (with the same value and size).
 * 
 * @author Bruno Nova
 */
public class Fork extends Component {
	/** The identifier of the input. */
	private String inId;
	
	/**
	 * Fork constructor.
	 * @param id Fork's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param size The size of the inputs and outputs
	 * @param inId The identifier of the input.
	 * @param outIds The identifiers of the outputs.
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public Fork(String id, int latency, Point position, int size, String inId, List<String> outIds) throws InvalidCPUException {
		super(id, latency, "", "fork", "fork_description", new Point(position.x - 2, position.y - 2), new Dimension(5, 5));
		this.inId = inId;
		
		addInput(inId, new Data(size));
		getInput().setPosition(position);
		
		for(String s: outIds) 
			addOutput(s, new Data(size));
		
		for(Output o: getOutputs())
			o.setPosition(position);
	}

	@Override
	public void execute() {
		for(Output o: getOutputs())
			o.setValue(getInput().getValue());
	}
	
	/**
	 * Returns the identifier of the input.
	 * @return The identifier of the input.
	 */
	public String getInputId() {
		return inId;
	}
	
	/**
	 * Returns the splits's input.
	 * @return Fork input;
	 */
	public final Input getInput() {
		return getInput(inId);
	}
}
