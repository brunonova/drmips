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

import java.util.LinkedList;
import java.util.List;
import org.feup.brunonova.drmips.exceptions.InvalidCPUException;
import org.feup.brunonova.drmips.mips.Component;
import org.feup.brunonova.drmips.mips.Data;
import org.feup.brunonova.drmips.mips.Input;
import org.feup.brunonova.drmips.util.Dimension;
import org.feup.brunonova.drmips.util.Point;

/**
 * This component splits the input's value in parts and sends each part into each output.
 * 
 * @author Bruno Nova
 */
public class Distributor extends Component {
	/** The identifier of the input. */
	private final String inId;
	/** The parameters of the outputs. */
	private final List<OutputParameters> outParameters;
	
	/**
	 * Distributor's constructor.
	 * @param id Distributor's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param inId The identifier of the input.
	 * @param inSize The size of the input.
	 * @throws InvalidCPUException If <tt>id</tt> is empty.
	 */
	public Distributor(String id, int latency, Point position, String inId, int inSize) throws InvalidCPUException {
		super(id, latency, "", "distributor", "distributor_description", position, new Dimension(5, 30));
		this.inId = inId;
		addInput(inId, new Data(inSize));
		outParameters = new LinkedList<OutputParameters>();
	}
	
	/**
	 * Adds an output.
	 * @param id The identifier of the output.
	 * @param msb The most significant bit of the value to put.
	 * @param lsb The less significant bit of the value to put.
	 * @throws InvalidCPUException If <tt>id</tt> is empty.
	 */
	public void addOutput(String id, int msb, int lsb) throws InvalidCPUException {
		OutputParameters param = new OutputParameters(id, msb, lsb, getInput().getSize());
		outParameters.add(param);
		addOutput(id, new Data(param.msb - param.lsb + 1));
	}

	@Override
	public void execute() {
		int value = getInput().getValue();
		for(OutputParameters o: outParameters) {
			getOutput(o.id).setValue(o.getValueForOutput(value));
		}
	}
	
	/**
	 * Returns the identifier of the input.
	 * @return The identifier of the input.
	 */
	public String getInputId() {
		return inId;
	}
	
	/**
	 * Returns the distributor's input.
	 * @return Distributor input;
	 */
	public Input getInput() {
		return getInput(inId);
	}
	
	/**
	 * Contains the parameters (MSB, LSB, id) for an output of a distributor.
	 */
	private class OutputParameters {
		/** The identifier of the output. */
		private String id;
		/** The most significant bit of the value to put. */
		private int msb;
		/** The less significant bit of the value to put. */
		private int lsb;
		/** The mask generated for the given msb and lsb. */
		private int mask;

		/**
		 * Creates the parameters for an output.
		 * @param id The identifier of the output.
		 * @param msb The most significant bit of the value to put.
		 * @param lsb The less significant bit of the value to put.
		 * @param maxSize The size of the distributor's input.
		 * @throws InvalidCPUException If <tt>id</tt> is empty.
		 */
		public OutputParameters(String id, int msb, int lsb, int inSize) throws InvalidCPUException {
			if(id.isEmpty()) throw new InvalidCPUException("Invalid ID " + id + "!");
			this.id = id;
			
			if(msb > (inSize - 1)) msb = inSize - 1;
			else if(msb < 0) msb = 0;
			if(lsb > (inSize - 1)) lsb = inSize - 1;
			else if(lsb < 0) lsb = 0;
			if(lsb > msb) {
				int aux = msb;
				msb = lsb;
				lsb = aux;
			}
			
			this.msb = msb;
			this.lsb = lsb;
			
			mask = Data.createMask(msb, lsb);
		}
		
		/**
		 * Returns the value masked and shifted for this output's parameters.
		 * @param value The original value.
		 * @return The value formatted for the output.
		 */
		public int getValueForOutput(int value) {
			return (value & mask) >>> lsb;
		}
	}
}
