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
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a pseudo-instruction.
 * 
 * @author Bruno Nova
 */
public class PseudoInstruction extends AbstractInstruction {
	/** The real instructions this pseudo-instruction should be converted to. */
	private final List<String> instructions;
	
	/**
	 * Creates a new pseudo-instruction.
	 * @param mnemonic The pseudo-instruction's mnemonic.
	 * @throws InvalidInstructionSetException If <tt>mnemonic</tt> is empty.
	 */
	public PseudoInstruction(String mnemonic) throws InvalidInstructionSetException {
		super(mnemonic);
		instructions = new LinkedList<>();
	}
	
	/**
	 * Adds the specified instruction to the list of instructions this pseudo-instruction should be converted to.
	 * @param instruction The instruction to add.
	 * @throws InvalidInstructionSetException If the instruction set is invalid.
	 */
	public void addInstruction(String instruction) throws InvalidInstructionSetException {
		instruction = instruction.trim();
		
		// Validate instruction
		if(instruction.isEmpty()) throw new InvalidInstructionSetException("Invalid pseudo-instruction " + getMnemonic() + "!");
		String mnem = instruction.split("\\s+", 2)[0].toLowerCase();
		if(mnem.equals(getMnemonic().toLowerCase()))
			throw new InvalidInstructionSetException("Invalid pseudo-instruction " + mnem + "!");
		
		// Add instruction
		instructions.add(instruction);
	}
	
	/**
	 * Returns the real instructions this pseudo-instruction should be converted to. 
	 * @return The real instructions this pseudo-instruction should be converted to.
	 */
	public List<String> getInstructions() {
		return instructions;
	}
	
	/**
	 * Returns whether the number of arguments is valid.
	 * <p>That is, if the number of arguments defined in the "args" array in the JSON file
	 * is equal or greater than the indexes of the arguments referenced by #1, #2, etc. in
	 * the "to" array.<br>
	 * This validity should be checked after loading the pseudo-instruction from the JSON
	 * file.</p>
	 * @return <tt>True</tt> if valid, <tt>false</tt> otherwise.
	 */
	public boolean isNumberOfArgumentsValid() {
		int i, j, args = 0;
		
		for(String instruction: instructions) {
			i = 0;
			while((i = instruction.indexOf(InstructionSet.ARGUMENT_CHAR, i)) >= 0) { // check each # char
				j = ++i;
				while(j < instruction.length() && instruction.charAt(j) >= '0' && instruction.charAt(j) <= '9') // find the index of the last digit
					j++;
				if(i == j) 
					return false;
				j = Integer.parseInt(instruction.substring(i, j));
				if(j > args)
						args = j;
			}
		}
		
		return args <= getNumberOfArguments();
	}
}
