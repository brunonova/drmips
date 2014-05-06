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
	private RegBank regbank = null; // CPU's register bank
	private final Input idExMemRead;
	private final Output stall;
	private Input idExRt, ifIdRs, ifIdRt;
	private String idExRtId, ifIdRsId, ifIdRtId; // temporary

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
		this.idExRtId = idExRtId;
		this.ifIdRsId = ifIdRsId;
		this.ifIdRtId = ifIdRtId;
		
		idExMemRead = addInput(idExMemReadId, new Data(1), IOPort.Direction.EAST);
		stall = addOutput(stallId, new Data(1), IOPort.Direction.NORTH);
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
	public final void setRegbank(RegBank regbank) throws InvalidCPUException {
		this.regbank = regbank;
		int size = regbank.getRequiredBitsToIdentifyRegister();
		idExRt = addInput(idExRtId, new Data(size), IOPort.Direction.EAST, true, true);
		ifIdRs = addInput(ifIdRsId, new Data(size), IOPort.Direction.WEST, true, true);
		ifIdRt = addInput(ifIdRtId, new Data(size), IOPort.Direction.WEST, true, true);
		idExRtId = ifIdRsId = ifIdRtId = null;
	}
	
	/**
	 * Returns the ID/EX.MemRead input.
	 * @return The ID/EX.MemRead input.
	 */
	public final Input getIdExMemRead() {
		return idExMemRead;
	}

	/**
	 * Returns the ID/EX.Rt input.
	 * @return The ID/EX.Rt input.
	 */
	public final Input getIdExRt() {
		return idExRt;
	}

	/**
	 * Returns the IF/ID.Rs input.
	 * @return The IF/ID.Rs input.
	 */
	public final Input getIfIdRs() {
		return ifIdRs;
	}

	/**
	 * Returns the IF/ID.Rt input.
	 * @return The IF/ID.Rt input.
	 */
	public final Input getIfIdRt() {
		return ifIdRt;
	}
	
	/**
	 * Returns the stall output.
	 * @return The stall output.
	 */
	public final Output getStall() {
		return stall;
	}
}
