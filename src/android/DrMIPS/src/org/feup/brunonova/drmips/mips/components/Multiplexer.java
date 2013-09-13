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

import java.util.List;
import org.feup.brunonova.drmips.exceptions.InvalidCPUException;
import org.feup.brunonova.drmips.mips.Component;
import org.feup.brunonova.drmips.mips.Data;
import org.feup.brunonova.drmips.mips.IOPort;
import org.feup.brunonova.drmips.mips.Input;
import org.feup.brunonova.drmips.mips.Output;
import org.feup.brunonova.drmips.util.Dimension;
import org.feup.brunonova.drmips.util.Point;

/**
 * Class that represents a multiplexer.
 * 
 * @author bruno
 */
public class Multiplexer extends Component {
	/** The identifier of the selector. */
	private String selId;
	/** The identifier of the output. */
	private String outId;
	/** The identifiers of the inputs. */
	private List<String> inIds;
	
	/**
	 * Multiplexer constructor.
	 * @param id Multiplexer's identifier.
	 * @param latency The latency of the component.
	 * @param position The component's position on the GUI.
	 * @param size The size of the inputs and outputs.
	 * @param inIds The identifiers of the inputs.
	 * @param selId The identifier of the selector.
	 * @param outId The identifier of the output.
	 * @throws InvalidCPUException If <tt>id</tt> is empty or duplicated.
	 */
	public Multiplexer(String id, int latency, Point position, int size, List<String> inIds, String selId, String outId) throws InvalidCPUException {
		super(id, latency, "M\nU\nX", "multiplexer", "multiplexer_description", position, new Dimension(15, 35));
		this.selId = selId;
		this.outId = outId;
		this.inIds = inIds;
		
		addInput(selId, new Data((inIds.size() > 0) ? Data.requiredNumberOfBits(inIds.size() - 1) : 1), IOPort.Direction.NORTH);
		addOutput(outId, new Data(size));
		for(String inId: inIds)
			addInput(inId, new Data(size));
	}

	@Override
	public void execute() {
		int sel = getSelector().getValue();
		Input input;
		for(int i = 0; i < inIds.size(); i++) { // put selected input value in output and mark other inputs as irrelevant
			input = getInput(i);
			if(i == sel) getOutput().setValue(input.getValue());
			input.setRelevant(i == sel);
		}
	}
	
	/**
	 * Returns the identifier of the output.
	 * @return The identifier of the output.
	 */
	public String getOutputId() {
		return outId;
	}
	
	/**
	 * Returns the multiplexer's output.
	 * @return Multiplexer output;
	 */
	public Output getOutput() {
		return getOutput(outId);
	}
	
	/**
	 * Returns the identifier of the selector.
	 * @return The identifier of the selector.
	 */
	public String getSelectorId() {
		return selId;
	}
	
	/**
	 * Returns the multiplexer's selector.
	 * @return Muoltiplexer selector;
	 */
	public Input getSelector() {
		return getInput(selId);
	}
	
	/**
	 * Returns the input with the specified index.
	 * @param index Index of the input.
	 * @return The input, or <tt>null</tt> if it doesn't exist.
	 */
	public Input getInput(int index) {
		if(index >= 0 && index < inIds.size())
			return getInput(inIds.get(index));
		else
			return null;
	}
}