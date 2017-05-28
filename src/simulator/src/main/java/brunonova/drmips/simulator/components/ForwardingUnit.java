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
 * Class that represents the pipeline forwarding unit.
 *
 * @author Bruno Nova
 */
public class ForwardingUnit extends Component {
	private RegBank regbank = null; // CPU's register bank
	private final Input exMemRegWrite, memWbRegWrite;
	private final Output forwardA, forwardB;
	private Input exMemRd, memWbRd, idExRs, idExRt;
	private String exMemRdId, memWbRdId, idExRsId, idExRtId; // temporary

	/**
	 * Component constructor.
	 * @param id The component's identifier.
	 * @param json The JSON object representing the component that should be parsed.
	 * @throws InvalidCPUException If the component has invalid parameters.
	 * @throws JSONException If the JSON object is invalid or incomplete.
	 */
	public ForwardingUnit(String id, JSONObject json) throws InvalidCPUException, JSONException {
		super(id, json, "Forwarding\nunit", "forwarding_unit", "forwarding_unit_description", new Dimension(70, 50));
		exMemRdId = json.getString("ex_mem_rd");
		memWbRdId = json.getString("mem_wb_rd");
		idExRsId = json.getString("id_ex_rs");
		idExRtId = json.getString("id_ex_rt");

		exMemRegWrite = addInput(json.getString("ex_mem_reg_write"), new Data(1), IOPort.Direction.EAST);
		memWbRegWrite = addInput(json.getString("mem_wb_reg_write"), new Data(1), IOPort.Direction.EAST);
		forwardA = addOutput(json.getString("fwd_a"), new Data(2), IOPort.Direction.NORTH);
		forwardB = addOutput(json.getString("fwd_b"), new Data(2), IOPort.Direction.NORTH);
	}

	@Override
	public void execute() {
		if(regbank != null) {
			if(getExMemRegWrite().getValue() == 1 && // EX hazard
				!regbank.isRegisterConstant(getExMemRd().getValue()) &&
				getExMemRd().getValue() == getIdExRs().getValue())
				getForwardA().setValue(2);
			else if(getMemWbRegWrite().getValue() == 1 && // MEM hazard
				!regbank.isRegisterConstant(getMemWbRd().getValue()) &&
				getExMemRd().getValue() != getIdExRs().getValue() &&
				getMemWbRd().getValue() == getIdExRs().getValue())
				getForwardA().setValue(1);
			else
				getForwardA().setValue(0);

			if(getExMemRegWrite().getValue() == 1 && // EX hazard
				!regbank.isRegisterConstant(getExMemRd().getValue()) &&
				getExMemRd().getValue() == getIdExRt().getValue())
				getForwardB().setValue(2);
			else if(getMemWbRegWrite().getValue() == 1 && // MEM hazard
				!regbank.isRegisterConstant(getMemWbRd().getValue()) &&
				getExMemRd().getValue() != getIdExRt().getValue() &&
				getMemWbRd().getValue() == getIdExRt().getValue())
				getForwardB().setValue(1);
			else
				getForwardB().setValue(0);
		}

		// Set outputs relevant if forwards are being made
		getForwardA().setRelevant(getForwardA().getValue() != 0);
		getForwardB().setRelevant(getForwardB().getValue() != 0);
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
		exMemRd = addInput(exMemRdId, new Data(size), IOPort.Direction.EAST, true, true);
		memWbRd = addInput(memWbRdId, new Data(size), IOPort.Direction.EAST, true, true);
		idExRs = addInput(idExRsId, new Data(size), IOPort.Direction.WEST, true, true);
		idExRt = addInput(idExRtId, new Data(size), IOPort.Direction.WEST, true, true);
		exMemRdId = memWbRdId = idExRsId = idExRtId = null;
	}

	/**
	 * Returns the EX/MEM.Rd input.
	 * @return The EX/MEM.Rd input.
	 */
	public final Input getExMemRd() {
		return exMemRd;
	}

	/**
	 * Returns the EX/MEM.RegWrite input.
	 * @return The EX/MEM.RegWrite input.
	 */
	public final Input getExMemRegWrite() {
		return exMemRegWrite;
	}

	/**
	 * Returns the MEM/WB.RegWrite input.
	 * @return The MEM/WB.RegWrite input.
	 */
	public final Input getMemWbRegWrite() {
		return memWbRegWrite;
	}

	/**
	 * Returns the ForwardA output.
	 * @return The ForwardA output.
	 */
	public final Output getForwardA() {
		return forwardA;
	}

	/**
	 * Returns the ForwardB output.
	 * @return The ForwardB output.
	 */
	public final Output getForwardB() {
		return forwardB;
	}

	/**
	 * Returns the ID/EX.Rs input.
	 * @return The ID/EX.Rs input.
	 */
	public final Input getIdExRs() {
		return idExRs;
	}

	/**
	 * Returns the ID/EX.Rt input.
	 * @return The ID/EX.Rt input.
	 */
	public final Input getIdExRt() {
		return idExRt;
	}

	/**
	 * Returns the MEM/WB.Rd input.
	 * @return The MEM/WB.Rd input.
	 */
	public final Input getMemWbRd() {
		return memWbRd;
	}
}
