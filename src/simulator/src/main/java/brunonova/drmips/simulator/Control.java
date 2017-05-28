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

package brunonova.drmips.simulator;

import brunonova.drmips.simulator.exceptions.InvalidInstructionSetException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class with the information of the values of the outputs for each opcode for the control unit.
 * 
 * @author Bruno Nova
 */
public class Control {
	/** Mapping from each opcode to their respective output values (another map). */
	private final Map<Integer, Map<String, Integer>> map;
	/** The sizes of each output. */
	private final Map<String, Integer> out;
	
	/**
	 * Creates a new control object.
	 */
	public Control() {
		map = new TreeMap<>();
		out = new TreeMap<>();
	}
	
	/**
	 * Adds the specified opcode to the map.
	 * @param opcode Opcode to add.
	 */
	public void addOpcode(int opcode) {
		map.put(opcode, new TreeMap<String, Integer>());
	}
	
	/**
	 * Returns whether the specified opcode exists in the map.
	 * @param opcode Opcode to check.
	 * @return <tt>True</tt> if the opcode is in the map.
	 */
	public boolean hasOpcode(int opcode) {
		return map.containsKey(opcode);
	}
	
	/**
	 * Sets the value of the specified output for the given opcode.
	 * @param opcode The opcode.
	 * @param id The identifier of the output.
	 * @param value The value of the output.
	 * @throws InvalidInstructionSetException If <tt>id</tt> is empty.
	 */
	public void addOutToOpcode(int opcode, String id, int value) throws InvalidInstructionSetException {
		if(id.isEmpty()) throw new InvalidInstructionSetException("Invalid ID " + id + "!");
		if(!hasOpcode(opcode)) addOpcode(opcode);
		map.get(opcode).put(id, value);
		out.put(id, 0);
	}
	
	/**
	 * Returns the data of the specified output for the given opcode.
	 * @param opcode The opcode.
	 * @param id The identifier of the output.
	 * @return Value of the output.
	 */
	public int getOutOfOpcode(int opcode, String id) {
		if(!hasOpcode(opcode) || !hasOut(id)) return 0;
		return map.get(opcode).get(id);
	}
	
	/**
	 * Returns whether the control has the specified output.
	 * @param id The identifier of the output to check.
	 * @return <tt>True</tt> the the output exists.
	 */
	public boolean hasOut(String id) {
		return out.containsKey(id);
	}
	
	/**
	 * Returns the size of the specified output.
	 * @param id The identifier of the output.
	 * @return Size of the output.
	 */
	public int getOutSize(String id) {
		if(!hasOut(id)) return 1;
		return out.get(id);
	}
	
	/**
	 * Returns the ids of all the outputs (ids and sizes).
	 * @return Ids of all outputs.
	 */
	public Set<String> getOutputsIds() {
		return out.keySet();
	}
	
	/**
	 * Finishes the creation of the control.
	 * <p>The sizes of the ouputs are calculated here.</p>
	 */
	public void finishCreation() {
		int size, s;
		
		// Add missing outputs to opcodes
		for(String id: out.keySet()) {
			for(int opcode: map.keySet()) {
				if(!map.get(opcode).containsKey(id))
					map.get(opcode).put(id, 0);
			}
		}
		
		// Find and update maximum sizes
		for(String id: out.keySet()) {
			size = 1;
			for(int opcode: map.keySet()) { // find output max size
				s = Data.requiredNumberOfBits(getOutOfOpcode(opcode, id));
				if(s > size) size = s;
			}
			
			out.put(id, size); // update output size
		}
	}
}
