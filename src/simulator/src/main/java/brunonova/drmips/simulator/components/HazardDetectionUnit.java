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

package brunonova.drmips.simulator.components;

import brunonova.drmips.simulator.*;
import brunonova.drmips.simulator.exceptions.InvalidCPUException;
import brunonova.drmips.simulator.util.Dimension;
import org.json.JSONException;
import org.json.JSONObject;

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
	 * Component constructor.
	 * @param id The component's identifier.
	 * @param json The JSON object representing the component that should be parsed.
	 * @throws InvalidCPUException If the component has invalid parameters.
	 * @throws JSONException If the JSON object is invalid or incomplete.
	 */
	public HazardDetectionUnit(String id, JSONObject json) throws InvalidCPUException, JSONException {
		super(id, json, "Hazard\ndetection\nunit", "hazard_detection_unit", "hazard_detection_unit_description", new Dimension(70, 50));
		idExRtId = json.getString("id_ex_rt");
		ifIdRsId = json.getString("if_id_rs");
		ifIdRtId = json.getString("if_id_rt");

		idExMemRead = addInput(json.getString("id_ex_mem_read"), new Data(1), IOPort.Direction.EAST);
		stall = addOutput(json.getString("stall"), new Data(1), IOPort.Direction.NORTH);
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
