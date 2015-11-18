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
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an abstract instruction, which is either an instruction or a pseudo-instruction.
 * <p>This class was created to aggregate the methods and attributes that are used
 * on both the <tt>Instruction</tt> and <tt>PseudoInstruction</tt> classes.</p>
 *
 * @author Bruno Nova
 */
public abstract class AbstractInstruction {
	/** The available types of instruction arguments. */
	public enum ArgumentType {REG, INT, TARGET, OFFSET, DATA, LABEL}
	/** The regular expression to validate the mnemonic. */
	public static final String MNEMONIC_REGEX = "^[a-zA-Z]([a-zA-Z0-9._]*[a-zA-Z0-9])?$";

	/** The instruction's mnemonic. */
	private String mnemonic;
	/** The argument types of the instruction. */
	private final List<ArgumentType> arguments;
	/** The short description of the instruction. */
	private String description = null;

	/**
	 * Creates a new instruction.
	 * <p>Subclasses should call this constructor in their own constructors.</p>
	 * @param mnemonic The instruction's mnemonic.
	 * @throws InvalidInstructionSetException If <tt>mnemonic</tt> is empty.
	 */
	protected AbstractInstruction(String mnemonic) throws InvalidInstructionSetException {
		arguments = new ArrayList<>();
		setMnemonic(mnemonic);
	}

	/**
	 * Updates the instruction's mnemonic.
	 * @param mnemonic The instruction's mnemonic.
	 * @throws InvalidInstructionSetException If <tt>mnemonic</tt> is empty.
	 */
	private void setMnemonic(String mnemonic) throws InvalidInstructionSetException {
		if(mnemonic.isEmpty() || ! mnemonic.matches(MNEMONIC_REGEX)) throw new InvalidInstructionSetException("Invalid mnemonic " + mnemonic + "!");
		this.mnemonic = mnemonic;
	}

	/**
	 * Returns the mnemonic of the instruction.
	 * @return The instruction's mnemonic.
	 */
	public String getMnemonic() {
		return mnemonic;
	}

	/**
	 * Updates the instruction's short description.
	 * @param description The short description of the instruction.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the instruction's short description.
	 * @return The short description of the instruction.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns whether the instruction has a description.
	 * @return <tt>True</tt> if the instruction has a description.
	 */
	public boolean hasDescription() {
		return description != null;
	}

	/**
	 * Adds an argument to the instruction.
	 * @param argument The type of the argument.
	 */
	public void addArgument(ArgumentType argument) {
		arguments.add(argument);
	}

	/**
	 * Adds an argument to the instruction.
	 * @param argument The type of the argument, as a String.
	 * @throws InvalidInstructionSetException If the type is invalid.
	 */
	public void addArgument(String argument) throws InvalidInstructionSetException {
		try {
			addArgument(ArgumentType.valueOf(argument.toUpperCase()));
		}
		catch(IllegalArgumentException e) { // unknown type?
			throw new InvalidInstructionSetException("Invalid argument type " + argument + "!");
		}
	}

	/**
	 * Returns the type of the specified argument.
	 * @param index Index of the argument.
	 * @return The type of the argument.
	 * @throws ArrayIndexOutOfBoundsException If the argument doesn't exist.
	 */
	public ArgumentType getArgument(int index) throws ArrayIndexOutOfBoundsException {
		return arguments.get(index);
	}

	/**
	 * Returns the number of arguments.
	 * @return Number of arguments.
	 */
	public int getNumberOfArguments() {
		return arguments.size();
	}

	/**
	 * Returns whether the instruction has arguments.
	 * @return <tt>True</tt> if the instruction has arguments.
	 */
	public boolean hasArguments() {
		return !arguments.isEmpty();
	}

	/**
	 * Returns a line with an example usage of this instruction/pseudo-instruction.
	 * <p>The method checks the instruction's argument types to create example arguments:
	 * <ul>
	 * <li>For integers it uses 21, 22, 23, etc. depending on the argument index.</li>
	 * <li>For registers it uses $t1, $t2, $t3, etc. depending on the argument index.</li>
	 * <li>For jump targets it uses "target".</li>
	 * <li>For brach offsets it uses "offset".</li>
	 * <li>For data memory addresses it uses "base($t1)", "base($t2)", etc. depending on the argument index.</li>
	 * </ul></p>
	 * @return Example usage for the instruction or pseudo-instruction.
	 */
	public String getUsage() {
		String desc = getMnemonic() + " ";
		for(int i = 0; i < getNumberOfArguments(); i++) {
			if(i > 0) desc += ", ";
			switch(getArgument(i)) {
				case INT: desc += "" + (20 + i + 1); break;
				case REG: desc += CPU.REGISTER_PREFIX + "t" + (i + 1); break;
				case TARGET: desc += "target"; break;
				case LABEL: desc += "label"; break;
				case OFFSET: desc += "offset"; break;
				case DATA: desc += "base(" + CPU.REGISTER_PREFIX + "t" + (i + 1) + ")"; break;
			}
		}
		return desc;
	}
}
