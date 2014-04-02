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

package org.feup.brunonova.drmips.simulator.mips.components;

import org.feup.brunonova.drmips.simulator.exceptions.InvalidCPUException;
import org.feup.brunonova.drmips.simulator.mips.Component;
import org.feup.brunonova.drmips.simulator.mips.Data;
import org.feup.brunonova.drmips.simulator.mips.IOPort;
import org.feup.brunonova.drmips.simulator.mips.Input;
import org.feup.brunonova.drmips.simulator.mips.Output;
import org.feup.brunonova.drmips.simulator.util.Dimension;
import org.feup.brunonova.drmips.simulator.util.Point;

/**
 * Class that represents a simple component that has 2 inputs and produces 1 output using a simple operation.
 * 
 * @author Bruno Nova
 */
public abstract class SimpleBinaryOperationComponent extends Component {
	/** The identifier of first the input. */
	private final String in1Id;
	/** The identifier of second the input. */
	private final String in2Id;
	/** The identifier of the output. */
	private final String outId;

	/**
	 * Component constructor.
	 * @param id Components's identifier.
	 * @param latency The latency of the component.
	 * @param displayName The name displayed on the GUI.
	 * @param nameKey The key of the component's name on the language file, shown on the component's tooltip.
	 * @param descriptionKey The key of the component's description on the language file.
	 * @param position The component's position on the GUI.
	 * @param size The size of the component on the GUI.
	 * @param in1Id The identifier of first the input.
	 * @param in2Id The identifier of second the input.
	 * @param outId The identifier of the output.
	 * @param dataSize The size of the inputs and outputs (if greater than 1, a balloon tip is displayed).
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public SimpleBinaryOperationComponent(String id, int latency, String displayName, String nameKey, String descriptionKey, Point position, Dimension size, String in1Id, String in2Id, String outId, int dataSize) throws InvalidCPUException {
		super(id, latency, displayName, nameKey, descriptionKey, position, size);
		this.in1Id = in1Id;
		this.in2Id = in2Id;
		this.outId = outId;
		boolean showTip = dataSize > 1;
		addInput(in1Id, new Data(dataSize), IOPort.Direction.WEST, true, showTip);
		addInput(in2Id, new Data(dataSize), IOPort.Direction.WEST, true, showTip);
		addOutput(outId, new Data(dataSize), IOPort.Direction.EAST, showTip);
	}

	@Override
	public void execute() {
		getOutput().setValue(operation(getInput1().getValue(), getInput2().getValue()));
	}
	
	/**
	 * Calculates the result of the binary operation between the two inputs.
	 * <p>Derived classes must implement this method.</p>
	 * @param in1 The first input's value.
	 * @param in2 The second input's value.
	 * @return The result.
	 */
	protected abstract int operation(int in1, int in2);
	
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
	public Input getInput1() {
		return getInput(in1Id);
	}
	
	/**
	 * Returns the component's second input.
	 * @return Component's second input;
	 */
	public Input getInput2() {
		return getInput(in2Id);
	}
	
	/**
	 * Returns the component's output.
	 * @return Component's output;
	 */
	public Output getOutput() {
		return getOutput(outId);
	}
}