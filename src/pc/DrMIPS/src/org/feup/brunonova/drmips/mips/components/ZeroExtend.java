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
import org.feup.brunonova.drmips.mips.IOPort;
import org.feup.brunonova.drmips.mips.Input;
import org.feup.brunonova.drmips.mips.Output;
import org.feup.brunonova.drmips.util.Dimension;
import org.feup.brunonova.drmips.util.Point;

/**
 * Class that represents a zero extender.
 * 
 * @author Bruno nova
 */
public class ZeroExtend extends Component {
	/** The identifier of the input. */
	private String inId;
	/** The identifier of the output. */
	private String outId;
	
	/**
	 * Zero extend constructor.
	 * @param id Zero extend's constructor.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param inId The identifier of the input.
	 * @param inSize The size of the input.
	 * @param outId The identifier of the output.
	 * @param outSize The size of the output.
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public ZeroExtend(String id, int latency, Point position, String inId, int inSize, String outId, int outSize) throws InvalidCPUException {
		super(id, latency, "Zero\nextend", "zero_extend", "zero_extend_description", position, new Dimension(40, 40));
		this.inId = inId;
		this.outId = outId;
		if(inSize > outSize) {
			int aux = inSize;
			inSize = outSize;
			outSize = aux;
		}
		
		addInput(inId, new Data(inSize), IOPort.Direction.WEST, true, true);
		addOutput(outId, new Data(outSize), IOPort.Direction.EAST, true);
	}

	@Override
	public void execute() {
		getOutput().setValue(getInput().getData().zeroExtend(getOutput().getSize()).getValue());
	}
	
	/**
	 * Returns the identifier of the input.
	 * @return The identifier of the input.
	 */
	public String getInputId() {
		return inId;
	}
	
	/**
	 * Returns the identifier of the output.
	 * @return The identifier of the output.
	 */
	public String getOutputId() {
		return outId;
	}
	
	/**
	 * Returns the input.
	 * @return The input;
	 */
	public Input getInput() {
		return getInput(inId);
	}
	
	/**
	 * Returns the output.
	 * @return The output;
	 */
	public Output getOutput() {
		return getOutput(outId);
	}
}
