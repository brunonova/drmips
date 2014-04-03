/*
    DrMIPS - Educational MIPS simulator
    Copyright (C) 2013-2014 Bruno Nova <ei08109@fe.up.pt>

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
 * Class that represents the pipeline hazard detection unit.
 * 
 * @author Bruno Nova
 */
public class HazardDetectionUnit extends Component {
	/** The CPU's register bank. */
	private RegBank regbank = null;
	/** The identifier of the ID/EX.MemRead input. */
	private final String idExMemReadId;
	/** The identifier of the ID/EX.Rs input. */
	private final String idExRtId;
	/** The identifier of the IF/ID.Rs input. */
	private final String ifIdRsId;
	/** The identifier of the IF/ID.Rt input. */
	private final String ifIdRtId;
	/** The identifier of the stall output. */
	private final String stallId;

	/**
	 * Hazard detection unit constructor.
	 * @param id Component's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param idExMemReadId
	 * @param idExRtId
	 * @param ifIdRsId
	 * @param ifIdRtId
	 * @param stallId
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public HazardDetectionUnit(String id, int latency, Point position, String idExMemReadId, String idExRtId, String ifIdRsId, String ifIdRtId, String stallId) throws InvalidCPUException {
		super(id, latency, "Hazard\ndetection\nunit", "hazard_detection_unit", "hazard_detection_unit_description", position, new Dimension(70, 50));
		this.idExMemReadId = idExMemReadId;
		this.idExRtId = idExRtId;
		this.ifIdRsId = ifIdRsId;
		this.ifIdRtId = ifIdRtId;
		this.stallId = stallId;
		
		addInput(idExMemReadId, new Data(1), IOPort.Direction.EAST);
		addOutput(stallId, new Data(1), IOPort.Direction.NORTH);
	}
	
	@Override
	public void execute() {
		if(regbank != null) {
			if(getIdExMemRead().getValue() == 1 &&
				(getIdExRt().getValue() == getIfIdRs().getValue() ||
				getIdExRt().getValue() == getIfIdRt().getValue()))
				getStall().setValue(1);
			else
				getStall().setValue(0);
		}
	}
	
	/**
	 * Sets the reference to the CPU's register bank.
	 * <p>This should be called after the register bank has been added to the CPU
	 * and before connections are made.</p>
	 * @param regbank The CPU's register bank.
	 * @throws InvalidCPUException If an id is empty or duplicated.
	 */
	public void setRegbank(RegBank regbank) throws InvalidCPUException {
		this.regbank = regbank;
		int size = regbank.getRequiredBitsToIdentifyRegister();
		addInput(idExRtId, new Data(size), IOPort.Direction.EAST, true, true);
		addInput(ifIdRsId, new Data(size), IOPort.Direction.WEST, true, true);
		addInput(ifIdRtId, new Data(size), IOPort.Direction.WEST, true, true);
	}
	
	/**
	 * Returns the identifier of the ID/EX.MemRead input.
	 * @return The identifier of the ID/EX.MemRead input.
	 */
	public String getIdExMemReadId() {
		return idExMemReadId;
	}

	/**
	 * Returns the identifier of the ID/EX.Rs input.
	 * @return The identifier of the ID/EX.Rs input.
	 */
	public String getIdExRtId() {
		return idExRtId;
	}

	/**
	 * Returns the identifier of the IF/ID.Rs input.
	 * @return The identifier of the IF/ID.Rs input.
	 */
	public String getIfIdRsId() {
		return ifIdRsId;
	}

	/**
	 * Returns the identifier of the IF/ID.Rt input.
	 * @return The identifier of the IF/ID.Rt input.
	 */
	public String getIfIdRtId() {
		return ifIdRtId;
	}

	/**
	 * Returns the identifier of the stall output.
	 * @return The identifier of the stall output.
	 */
	public String getStallId() {
		return stallId;
	}
	
	/**
	 * Returns the ID/EX.MemRead input.
	 * @return The ID/EX.MemRead input.
	 */
	public Input getIdExMemRead() {
		return getInput(idExMemReadId);
	}

	/**
	 * Returns the ID/EX.Rt input.
	 * @return The ID/EX.Rt input.
	 */
	public Input getIdExRt() {
		return getInput(idExRtId);
	}

	/**
	 * Returns the IF/ID.Rs input.
	 * @return The IF/ID.Rs input.
	 */
	public Input getIfIdRs() {
		return getInput(ifIdRsId);
	}

	/**
	 * Returns the IF/ID.Rt input.
	 * @return The IF/ID.Rt input.
	 */
	public Input getIfIdRt() {
		return getInput(ifIdRtId);
	}
	
	/**
	 * Returns the stall output.
	 * @return The stall output.
	 */
	public Output getStall() {
		return getOutput(stallId);
	}
}
