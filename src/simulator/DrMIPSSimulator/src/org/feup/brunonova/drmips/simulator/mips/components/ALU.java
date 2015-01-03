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
import org.feup.brunonova.drmips.simulator.mips.ControlALU;
import org.feup.brunonova.drmips.simulator.mips.Data;
import org.feup.brunonova.drmips.simulator.mips.IOPort;
import org.feup.brunonova.drmips.simulator.mips.Input;
import org.feup.brunonova.drmips.simulator.mips.Output;
import org.feup.brunonova.drmips.simulator.util.Dimension;
import org.feup.brunonova.drmips.simulator.util.Point;

/**
 * Class that represents the MIPS ALU.
 * 
 * @author Bruno Nova
 */
public class ALU extends Component {
	private final Input input1, input2;
	private final Output output, zero;
	private Input control;
	private String controlId; // temporary
	protected ControlALU controlALU = null;
	
	/**
	 * ALU constructor.
	 * @param id ALU's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param in1Id The identifier of the first input.
	 * @param in2Id The identifier of the second input.
	 * @param controlId The identifier of the control input.
	 * @param outId The identifier of the output
	 * @param zeroId The identifier of the zero output.
	 * @throws InvalidCPUException InvalidCPUException InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public ALU(String id, int latency, Point position, String in1Id, String in2Id, String controlId, String outId, String zeroId) throws InvalidCPUException {
		super(id, latency, "ALU", "alu", "alu_description", position, new Dimension(60, 60));
		this.controlId = controlId;
		
		input1 = addInput(in1Id, new Data(), IOPort.Direction.WEST, true, true);
		input2 = addInput(in2Id, new Data(), IOPort.Direction.WEST, true, true);
		output = addOutput(outId, new Data(), IOPort.Direction.EAST, true);
		zero = addOutput(zeroId, new Data(1));
	}
	
	/**
	 * Sets the control information for the ALU.
	 * <p>This method should be called after the instruction set has been loaded.</p>
	 * @param controlALU Control information.
	 * @throws InvalidCPUException If an output is duplicated.
	 */
	public void setControlALU(ControlALU controlALU) throws InvalidCPUException {
		this.controlALU = controlALU;
		control = addInput(controlId, new Data(controlALU.getControlSize()), IOPort.Direction.SOUTH);
		controlId = null;
	}

	@Override
	public void execute() {
		int res = controlALU.doOperation(getInput1().getValue(), getInput2().getValue(), this, getControl().getValue());
		getOutput().setValue(res);
		getZero().setValue(res == 0 ? 1 : 0);
	}
	
	/**
	 * Returns the operation that the ALU is permorming.
	 * @return Current operation of the ALU.
	 */
	public ControlALU.Operation getOperation() {
		return controlALU.getOperation(getControl().getValue());
	}
	
	/**
	 * Returns the name of the operation that the ALU is performing.
	 * @return Name of the current operation of the ALU.
	 */
	public String getOperationName() {
		return getOperation().toString();
	}

	/**
	 * Returns the first input.
	 * @return First input.
	 */
	public final Input getInput1() {
		return input1;
	}
	
	/**
	 * Returns the second input.
	 * @return Second input.
	 */
	public final Input getInput2() {
		return input2;
	}
	
	/**
	 * Returns the control input.
	 * @return Control input.
	 */
	public final Input getControl() {
		return control;
	}
	
	/**
	 * Returns the output.
	 * @return The result output.
	 */
	public final Output getOutput() {
		return output;
	}
	
	/**
	 * Returns the zero output.
	 * @return Zero output.
	 */
	public final Output getZero() {
		return zero;
	}
}
