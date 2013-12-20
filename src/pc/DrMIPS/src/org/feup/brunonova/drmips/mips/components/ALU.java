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
import org.feup.brunonova.drmips.mips.ControlALU;
import org.feup.brunonova.drmips.mips.Data;
import org.feup.brunonova.drmips.mips.IOPort;
import org.feup.brunonova.drmips.mips.Input;
import org.feup.brunonova.drmips.mips.Output;
import org.feup.brunonova.drmips.util.Dimension;
import org.feup.brunonova.drmips.util.Point;

/**
 * Class that represents the MIPS ALU.
 * 
 * @author Bruno Nova
 */
public class ALU extends Component {
	/** The identifier of the first input. */
	private final String in1Id;
	/** The identifier of the second input. */
	private final String in2Id;
	/** The identifier of the control input. */
	private final String controlId;
	/** The identifier of the result output. */
	private final String outId;
	/** The identifier of the zero output. */
	private final String zeroId;
	/** How the ALU should work. */
	protected ControlALU control = null;
	
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
		this.in1Id = in1Id;
		this.in2Id = in2Id;
		this.controlId = controlId;
		this.outId = outId;
		this.zeroId = zeroId;
		
		addInput(in1Id, new Data(), IOPort.Direction.WEST, true, true);
		addInput(in2Id, new Data(), IOPort.Direction.WEST, true, true);
		addOutput(outId, new Data(), IOPort.Direction.EAST, true);
		addOutput(zeroId, new Data(1));
	}
	
	/**
	 * Sets the control information for the ALU.
	 * <p>This method should be called after the instruction set has been loaded.</p>
	 * @param control Control information.
	 * @throws InvalidCPUException If an output is duplicated.
	 */
	public void setControl(ControlALU control) throws InvalidCPUException {
		this.control = control;
		addInput(controlId, new Data(control.getControlSize()), IOPort.Direction.SOUTH);
	}

	@Override
	public void execute() {
		int res = control.doOperation(getInput1().getValue(), getInput2().getValue(), this, getControl().getValue());
		getOutput().setValue(res);
		getZero().setValue(res == 0 ? 1 : 0);
	}
	
	/**
	 * Returns the operation that the ALU is permorming.
	 * @return Current operation of the ALU.
	 */
	public ControlALU.Operation getOperation() {
		return control.getOperation(getControl().getValue());
	}
	
	/**
	 * Returns the name of the operation that the ALU is performing.
	 * @return Name of the current operation of the ALU.
	 */
	public String getOperationName() {
		return getOperation().toString();
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
	 * Returns the identifier of the control input.
	 * @return The identifier of the control input.
	 */
	public String getControlId() {
		return controlId;
	}

	/**
	 * Returns the identifier of the zero output.
	 * @return The identifier of the zero output.
	 */
	public String getZeroId() {
		return zeroId;
	}
	
	/**
	 * Returns the first input.
	 * @return First input.
	 */
	public Input getInput1() {
		return getInput(in1Id);
	}
	
	/**
	 * Returns the second input.
	 * @return Second input.
	 */
	public Input getInput2() {
		return getInput(in2Id);
	}
	
	/**
	 * Returns the control input.
	 * @return Control input.
	 */
	public Input getControl() {
		return getInput(controlId);
	}
	
	/**
	 * Returns the output.
	 * @return The result output.
	 */
	public Output getOutput() {
		return getOutput(outId);
	}
	
	/**
	 * Returns the zero output.
	 * @return Zero output.
	 */
	public Output getZero() {
		return getOutput(zeroId);
	}
}
