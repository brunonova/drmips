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

import org.feup.brunonova.drmips.exceptions.InvalidCPUException;
import org.feup.brunonova.drmips.mips.Component;
import org.feup.brunonova.drmips.mips.Data;
import org.feup.brunonova.drmips.mips.Input;
import org.feup.brunonova.drmips.mips.Output;
import org.feup.brunonova.drmips.util.Dimension;
import org.feup.brunonova.drmips.util.Point;

/**
 * This component concatenates the values of the two inputs.
 * <p>The 1st input is concatenated in the most significant bits, while the 2nd is
 * concatenated to the less significant bits.<br />
 * Concatenating 111 with 0000, in that order, would give 1110000</p>
 * 
 * @author Bruno Nova
 */
public class Concatenator extends Component {
	/** The identifier of the first input (concatenated to the "left"). */
	private String in1Id;
	/** The identifier of the second input (concatenated to the "right"). */
	private String in2Id;
	/** The identifier of the output. */
	private String outId;
	
	/**
	 * Fork constructor.
	 * @param id Fork's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param in1Id The identifier of the first input (concatenated to the "left").
	 * @param in1Size The size of the first input.
	 * @param in2Id The identifier of the second input (concatenated to the "right").
	 * @param in2Size The size of the second input.
	 * @param outId The identifier of the output.
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public Concatenator(String id, int latency, Point position, String in1Id, int in1Size, String in2Id, int in2Size, String outId) throws InvalidCPUException {
		super(id, latency, "", "concatenator", "concatenator_description", new Point(position.x - 2, position.y - 2), new Dimension(5, 5));
		this.in1Id = in1Id;
		this.in2Id = in2Id;
		this.outId = outId;
		
		addInput(in1Id, new Data(in1Size));
		addInput(in2Id, new Data(in2Size));
		getInput1().setPosition(position);
		getInput2().setPosition(position);
		
		addOutput(outId, new Data(in1Size + in2Size));
		getOutput().setPosition(position);
	}

	@Override
	public void execute() {
		getOutput().setValue((getInput1().getValue() << getInput2().getSize()) | getInput2().getValue());
	}
	
	/**
	 * Returns the identifier of the first input.
	 * @return The identifier of the first input.
	 */
	public String getInput1Id() {
		return in1Id;
	}
	
	/**
	 * Returns the identifier of the second input.
	 * @return The identifier of the second input.
	 */
	public String getInput2Id() {
		return in2Id;
	}
	
	
	/**
	 * Returns the identifier of the output.
	 * @return The identifier of the output.
	 */
	public String getOutputId() {
		return outId;
	}
	
	/**
	 * Returns the component's first input.
	 * @return Component's first input;
	 */
	public final Input getInput1() {
		return getInput(in1Id);
	}
	
	/**
	 * Returns the component's second input.
	 * @return Component's second input;
	 */
	public final Input getInput2() {
		return getInput(in2Id);
	}
	
	/**
	 * Returns the component's output.
	 * @return Component's output;
	 */
	public final Output getOutput() {
		return getOutput(outId);
	}
}