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
import org.feup.brunonova.drmips.simulator.mips.ControlALU;
import org.feup.brunonova.drmips.simulator.mips.Data;
import org.feup.brunonova.drmips.simulator.mips.IOPort;
import org.feup.brunonova.drmips.simulator.mips.Input;
import org.feup.brunonova.drmips.simulator.util.Dimension;
import org.feup.brunonova.drmips.simulator.util.Point;

/**
 * Class that represents the ALU controller.
 * 
 * @author Bruno Nova
 */
public class ALUControl extends Component {
	private Input aluOp, func;
	private String aluOpId, funcId; // temporary
	private ControlALU controlALU = null;
	
	/**
	 * ALU Control constructor.
	 * @param id ALU Control's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param aluOpId The identifier of the ALUOp input.
	 * @param funcId The identifier of the func input.
	 * @throws InvalidCPUException InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public ALUControl(String id, int latency, Point position, String aluOpId, String funcId) throws InvalidCPUException {
		super(id, latency, "ALU\ncontrol", "alu_control", "alu_control_description", position, new Dimension(40, 40));
		this.aluOpId = aluOpId;
		this.funcId = funcId;
	}

	@Override
	public void execute() {
		for(String id: controlALU.getOutputsIds())
			getOutput(id).setValue(controlALU.getControlValue(getALUOp().getValue(), getFunc().getValue(), id));
	}

	/**
	 * Sets the control information for the ALU Control.
	 * <p>This method should be called after the instruction set has been loaded.</p>
	 * @param controlALU Control information.
	 * @throws InvalidCPUException If an output is duplicated.
	 */
	public final void setControlALU(ControlALU controlALU) throws InvalidCPUException {
		this.controlALU = controlALU;
		
		// Add inputs and outputs
		aluOp = addInput(aluOpId, new Data(controlALU.getAluOpSize()), IOPort.Direction.NORTH);
		func = addInput(funcId, new Data(controlALU.getFuncSize()));
		aluOpId = funcId = null;
		for(String id: controlALU.getOutputsIds())
			addOutput(id, new Data(controlALU.getOutSize(id)));
	}
	
	/**
	 * Returns the ALUOp input.
	 * @return ALUOp input.
	 */
	public final Input getALUOp() {
		return aluOp;
	}
	
	/**
	 * Returns the func input.
	 * @return Func input.
	 */
	public final Input getFunc() {
		return func;
	}
}
