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

import java.util.ArrayList;
import java.util.List;
import org.feup.brunonova.drmips.exceptions.InvalidCPUException;
import org.feup.brunonova.drmips.mips.Input;
import org.feup.brunonova.drmips.util.Dimension;
import org.feup.brunonova.drmips.util.Point;

/**
 * Class that represents an OR port with 2 inputs and 1 output with the size of 1 bit.
 * 
 * @author Bruno Nova
 */
public class Or extends SimpleBinaryOperationComponent {
	/**
	 * Or constructor.
	 * @param id Or's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param in1Id The identifier of first the input.
	 * @param in2Id The identifier of second the input.
	 * @param outId The identifier of the output.
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public Or(String id, int latency, Point position, String in1Id, String in2Id, String outId) throws InvalidCPUException {
		super(id, latency, "OR", "or", "or_description", position, new Dimension(30, 30), in1Id, in2Id, outId, 1);
	}

	@Override
	protected int operation(int in1, int in2) {
		return in1 | in2;
	}
	
	@Override
	protected List<Input> getLatencyInputs() {
		ArrayList<Input> inList = new ArrayList<Input>();
		int val1 = getInput1().getValue();
		int val2 = getInput2().getValue();
		if (val1 == val2) {  // inputs have identical logic values
			if (val1 == 0) {  // both 0; use both
				inList.add(getInput1());
				inList.add(getInput2());
			} else { // both 1; use just the earliest input
				int lat1 = getInput1().getAccumulatedLatency();
				int lat2 = getInput2().getAccumulatedLatency();
				if (lat1 <= lat2) {
					inList.add(getInput1());
				} else {
					inList.add(getInput2());
				}
			}
		} else if (val1 == 0) { // val2 == 1 and val1 == 0
			inList.add(getInput2());
		} else { // val1 == 1 and val2 == 0
			inList.add(getInput1());
		}
		return inList;
	}
}
