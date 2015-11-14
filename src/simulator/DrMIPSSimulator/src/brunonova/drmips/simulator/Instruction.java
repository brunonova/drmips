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
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an available instruction.
 * 
 * @author Bruno Nova
 */
public class Instruction extends AbstractInstruction {
	/** The instruction's type. */
	private InstructionType type;
	/** The values each field should have. */
	private final Map<InstructionType.Field, FieldValue> fields;
	
	/**
	 * Creates a new instruction.
	 * @param mnemonic The instruction's mnemonic.
	 * @param type The instruction's type.
	 * @throws InvalidInstructionSetException If <tt>mnemonic</tt> is empty.
	 */
	protected Instruction(String mnemonic, InstructionType type) throws InvalidInstructionSetException {
		super(mnemonic);
		fields = new HashMap<>();
		setType(type);
	}
	
	/**
	 * Updates the instruction's type.
	 * @param type The instruction's type.
	 */
	private void setType(InstructionType type) {
		this.type = type;
		
		// Predefine all fields as constant values (=0)
		for(InstructionType.Field f: type.getFields())
			fields.put(f, new FieldConstant(0));
	}
	
	/**
	 * Returns the type of the instruction.
	 * @return The instruction's type.
	 */
	public InstructionType getType() {
		return type;
	}
	
	/**
	 * Indicates that the specified field has a constant value.
	 * <p>The arguments should have already been added.</p>
	 * @param field Identifier of the field.
	 * @param value Constant value.
	 * @throws InvalidInstructionSetException If the field doesn't exist.
	 */
	public void setFieldConstant(String field, int value) throws InvalidInstructionSetException {
		if(!type.hasField(field)) throw new InvalidInstructionSetException("Unknown field " + field + "!");
		fields.put(type.getField(field), new FieldConstant(value));
	}
	
	/**
	 * Indicates that the specified field receives its value from an argument.
	 * <p>The arguments should have already been added.</p>
	 * @param field Identifier of the field.
	 * @param argIndex Index of the argument.
	 * @throws InvalidInstructionSetException If the field doesn't exist.
	 * @throws ArrayIndexOutOfBoundsException If the argument doesn't exist.
	 */
	public void setFieldFromArgument(String field, int argIndex) throws InvalidInstructionSetException, ArrayIndexOutOfBoundsException {
		if(!type.hasField(field)) throw new InvalidInstructionSetException("Unknown field " + field + "!");
		fields.put(type.getField(field), new FieldFromArgument(argIndex, getArgument(argIndex)));
	}
	
	/**
	 * Indicates that the specified field is in the form <tt>address</tt> or <tt>address($offset)</tt> and receives its value from an argument.
	 * @param field Identifier of the field.
	 * @param argIndex Index of the argument.
	 * @param t The type of the component (address or offset par).
	 * @throws InvalidInstructionSetException If the field doesn't exist.
	 * @throws ArrayIndexOutOfBoundsException If the argument doesn't exist.
	 */
	public void setFieldDataFromArgument(String field, int argIndex, FieldDataFromArgument.Type t) throws InvalidInstructionSetException, ArrayIndexOutOfBoundsException {
		if(!type.hasField(field)) throw new InvalidInstructionSetException("Unknown field " + field + "!");
		fields.put(type.getField(field), new FieldDataFromArgument(argIndex, t));
	}
	
	/**
	 * Calls <tt>setFieldConstant()</tt>, <tt>setFieldFromConstant()</tt>, etc. according to <tt>arg</tt>.
	 * @param field Identifier of the field.
	 * @param arg Argument from the JSON file.
	 * @throws InvalidInstructionSetException If the argument is invalid for the field.
	 */
	public void setField(String field, String arg) throws InvalidInstructionSetException {
		try {
			setFieldConstant(field, Integer.parseInt(arg)); // constant int value?
		}
		catch(NumberFormatException e) {
			// not a constant
			if(arg.length() >= 2 && arg.startsWith("" + InstructionSet.ARGUMENT_CHAR)) { // value from argument?
				if(arg.endsWith(".base") || arg.endsWith(".offset")) { // argument in the form address or address($offset) for lw/sw
					FieldDataFromArgument.Type t;
					int index = arg.lastIndexOf(".");
					if(arg.endsWith(".base")) t = FieldDataFromArgument.Type.BASE;
					else t = FieldDataFromArgument.Type.OFFSET;
					try {
						setFieldDataFromArgument(field, Integer.parseInt(arg.substring(1, index)) - 1, t);
					}
					catch(NumberFormatException ex) {
						throw new InvalidInstructionSetException("Invalid parameter for field " + field + "!");
					}
				}
				else { // direct value from argument
					try {
						setFieldFromArgument(field, Integer.parseInt(arg.substring(1)) - 1);
					}
					catch(NumberFormatException ex) {
						throw new InvalidInstructionSetException("Invalid parameter for field " + field + "!");
					}
				}
			}
			else
				throw new InvalidInstructionSetException("Invalid parameter for field " + field + "!");
		}
	}
	
	/**
	 * Returns the value of the specified field.
	 * @param field The field.
	 * @return Desired field, or <tt>null</tt> if it doesn't exist;
	 */
	public FieldValue getField(InstructionType.Field field) {
		return fields.get(field);
	}
	
	
	/** Base class of the field values. */
	public static abstract class FieldValue {}
	
	/** Class that indicates a field as having a constant value. */
	public static class FieldConstant extends FieldValue {
		/** Constant value. */
		private final int value;

		/**
		 * Creates the value of a constant field.
		 * @param value Constant value.
		 */
		public FieldConstant(int value) {
			this.value = value;
		}
		
		/**
		 * Returns the constant value of the field.
		 * @return Constant value.
		 */
		public int getValue() {
			return value;
		}
	}
	
	/** Class that indicates a field as having its value from an instruction argument. */
	public static class FieldFromArgument extends FieldValue {
		/** Index of the argument. */
		private final int argIndex;
		/** Type of the argument. */
		private final ArgumentType type;

		/**
		 * Creates the value of a field from an argument.
		 * @param argIndex Index of the argument.
		 * @param type Type of the argument.
		 */
		public FieldFromArgument(int argIndex, ArgumentType type) {
			this.argIndex = argIndex;
			this.type = type;
		}
		
		/**
		 * Returns the index of the argument.
		 * @return Index of the argument.
		 */
		public int getArgIndex() { 
			return argIndex;
		}
		
		/**
		 * Returns the type of the argument.
		 * @return Type of the argument.
		 */
		public ArgumentType getArgumentType() {
			return type;
		}
	}
	
	/** Class that indicates a field as being in the form <tt>address</tt> or <tt>base($offset)</tt> and having its value from an instruction argument. */
	public static class FieldDataFromArgument extends FieldValue {
		/** Possible types to specify either the base address or the offset component. */
		public enum Type {BASE, OFFSET}
		
		/** Index of the argument. */
		private final int argIndex;
		/** Type of the argument component. */
		private final Type type;
		
		/**
		 * Creates the value of a field in the form <tt>address</tt> or <tt>base($offset)</tt> and from an argument.
		 * @param argIndex Index of the argument.
		 * @param type Type of the argument component.
		 */
		public FieldDataFromArgument(int argIndex, Type type) {
			this.argIndex = argIndex;
			this.type = type;
		}
		
		/**
		 * Returns the index of the argument.
		 * @return Index of the argument.
		 */
		public int getArgIndex() { 
			return argIndex;
		}
		
		/**
		 * Returns the type of the argument component.
		 * @return Type of the argument component.
		 */
		public Type getType() {
			return type;
		}
	}
}
