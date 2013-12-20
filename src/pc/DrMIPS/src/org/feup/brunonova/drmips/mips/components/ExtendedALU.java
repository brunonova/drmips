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

import java.util.Stack;
import org.feup.brunonova.drmips.exceptions.InvalidCPUException;
import org.feup.brunonova.drmips.mips.Data;
import org.feup.brunonova.drmips.mips.IsSynchronous;
import org.feup.brunonova.drmips.util.Point;

/**
 * An ALU that supports multiplications and divisions, and contains the <tt>HI</tt> and <tt>LO</tt> "registers".
 * 
 * @author Bruno Nova
 */
public class ExtendedALU extends ALU implements IsSynchronous {
	/** The <tt>HI</tt> "register". */
	private final Data hi;
	/** The <tt>LO</tt> "register". */
	private final Data lo;
	/** The previous values of the hi and lo registers. */
	private final Stack<int[]> states = new Stack<int[]>();
	
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
	public ExtendedALU(String id, int latency, Point position, String in1Id, String in2Id, String controlId, String outId, String zeroId) throws InvalidCPUException {
		super(id, latency, position, in1Id, in2Id, controlId, outId, zeroId);
		setNameKey("extended_alu");
		setDescriptionKey("extended_alu_description");
		
		hi = new Data();
		lo = new Data();
	}
	
	@Override
	public void executeSynchronous() {
		control.doSynchronousOperation(getInput1().getValue(), getInput2().getValue(), this, getControl().getValue());
	}

	@Override
	public void pushState() {
		states.push(new int[] {hi.getValue(), lo.getValue()});
	}

	@Override
	public void popState() {
		if(hasSavedStates()) {
			int[] val = states.pop();
			hi.setValue(val[0]);
			lo.setValue(val[1]);
		}
	}

	@Override
	public boolean hasSavedStates() {
		return !states.empty();
	}

	@Override
	public void clearSavedStates() {
		states.clear();
	}

	@Override
	public void resetFirstState() {
		while(hasSavedStates())
			popState();
	}
	
	/**
	 * Returns the <tt>HI</tt> "register".
	 * @return The <tt>HI</tt> "register".
	 */
	public Data getHI() {
		return hi;
	}
	
	/**
	 * Returns the <tt>HI</tt> "register".
	 * @return The <tt>HI</tt> "register".
	 */
	public Data getLO() {
		return lo;
	}
	
	/**
	 * Resets the <tt>HI</tt> and <tt>LO</tt> registers to 0.
	 */
	public void reset() {
		hi.setValue(0);
		lo.setValue(0);
	}
}
