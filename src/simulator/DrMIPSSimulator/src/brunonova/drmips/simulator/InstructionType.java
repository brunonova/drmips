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
 * Class that represents a MIPS instruction type, with several fields.
 * <p>Fields should be added with <tt>addField()</tt> and then the validity o
 * the type should be checked with <tt>isValid()</tt>.<br>
 * The first field is always considered the opcode field.</p>
 * 
 * @author Bruno Nova
 */
public class InstructionType {
	/** The identifier of the instruction type. */
	private String id;
	/** The fields that compose the instruction type. */
	private final List<Field> fields;
	/** The total size of the instruction type (should be equal to <tt>Data.DATA_SIZE</tt> after all fields are added. */
	private int totalSize = 0;
	
	/**
	 * Creates an instruction type.
	 * @param id The identifier of the instruction type.
	 * @throws InvalidInstructionSetException If <tt>id</tt> is empty.
	 */
	public InstructionType(String id) throws InvalidInstructionSetException {
		setId(id);
		fields = new ArrayList<>();
	}
	
	/**
	 * Updates the identifier of the instruction type.
	 * @param id The new identifier.
	 * @throws InvalidInstructionSetException If <tt>id</tt> is empty.
	 */
	private void setId(String id) throws InvalidInstructionSetException {
		if(id.isEmpty()) throw new InvalidInstructionSetException("Invalid ID " + id + "!");
		this.id = id;
	}
	
	/**
	 * Returns the identifier of the instruction type.
	 * @return Instruction type's identifier.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Adds a field to the instruction type.
	 * @param id The identifier of the field.
	 * @param size The size of the field.
	 * @throws InvalidInstructionSetException If <tt>id</tt> is empty or duplicated.
	 */
	public void addField(String id, int size) throws InvalidInstructionSetException {
		if(hasField(id))
			throw new InvalidInstructionSetException("Duplicated ID " + id + "!");
		else {
			Field f = new Field(id, size, Data.MSB - totalSize);
			fields.add(f);
			totalSize += f.getSize();
		}
	}
	
	/**
	 * Returns the field with the specified index.
	 * @param index Index of the field.
	 * @return The desired field, or <tt>null</tt> if the index is out of bounds.
	 */
	public Field getField(int index) {
		if(index < 0 || index >= fields.size())
			return null;
		else
			return fields.get(index);
	}
	
	/**
	 * Returns the field with the indicated identifier.
	 * @param id Identifier of the field.
	 * @return Desired field, or <tt>null</tt> if it doesn't exist.
	 */
	public Field getField(String id) {
		for(Field f: fields) {
			if(f.getId().equals(id))
				return f;
		}
		return null;
	}
	
	/**
	 * Returns the opcode field (the first one).
	 * @return Opcode field.
	 */
	public Field getOpCodeField() {
		return fields.get(0);
	}
	
	/**
	 * Returns whether a field with the given identifier exists.
	 * @param id Identifier to check.
	 * @return <tt>True</tt> if the field exists.
	 */
	public boolean hasField(String id) {
		return getField(id) != null;
	}
	
	/**
	 * Returns all the fields.
	 * @return List with all the fields.
	 */
	public List<Field> getFields() {
		return fields;
	}
	
	/**
	 * Returns whether the instruction is valid.
	 * @return <tt>True</tt> if the instruction type is valid.
	 */
	public boolean isValid() {
		return totalSize == Data.DATA_SIZE && !fields.isEmpty();
	}

	@Override
	public String toString() {
		String res = getId() + "-type: [";
		for(int i = 0; i < fields.size(); i++) {
			if(i > 0) res += " | ";
			res += fields.get(i).toString();
		}
		return res + "]";
	}
	
	/**
	 * A field of an instruction type.
	 */
	public final class Field {
		/** The identifier of the field. */
		private String id;
		/** The totalSize of the field. */
		private int size;
		/** The most significant bit position of the field on the instruction type. */
		private final int msb;
		/** The less significant bit position of the field on the instruction type. */
		private int lsb;
		/** The mask for the bits MSB-LSB. */
		private final int mask;

		/**
		 * Creates a new instruction type field.
		 * @param id The identifier of the field.
		 * @param size The size of the field.
		 * @param msb The most significant bit of the field on the instruction type.
		 * @throws InvalidInstructionSetException If <tt>id</tt> is empty.
		 */
		public Field(String id, int size, int msb) throws InvalidInstructionSetException {
			setId(id);
			setSize(size);
			
			if(msb > Data.MSB) msb = Data.MSB;
			else if(msb < 0) msb = 0;
			this.msb = msb;
			
			lsb = msb - getSize() + 1;
			if(lsb < 0) lsb = 0;
			
			mask = Data.createMask(msb, lsb);
		}
		
		/**
		 * Updates the identifier of the field.
		 * @param id The new identifier.
		 * @throws InvalidInstructionSetException If <tt>id</tt> is empty.
		 */
		private void setId(String id) throws InvalidInstructionSetException {
			if(id.isEmpty()) throw new InvalidInstructionSetException("Invalid ID " + id + "!");
			this.id = id;
		}
		
		/**
		 * Returns the identifier of the field.
		 * @return The field's identifier.
		 */
		public String getId() {
			return id;
		}
		
		/**
		 * Updates the totalSize of the field.
		 * @param totalSize The new totalSize.
		 */
		private void setSize(int size) {
			if(size > (Data.MSB + 1)) size = Data.MSB + 1;
			else if(size <= 0) size = 1;
			this.size = size;
		}
		
		/**
		 * Returns the totalSize of the field.
		 * @return The field's totalSize.
		 */
		public int getSize() {
			return size;
		}
		
		/**
		 * Returns the most significant bit position of the field on the instruction type.
		 * @return Field's most significant bit position.
		 */
		public int getMSB() {
			return msb;
		}
		
		/**
		 * Returns the less significant bit position of the field on the instruction type.
		 * @return Field's less significant bit position.
		 */
		public int getLSB() {
			return lsb;
		}
		
		/**
		 * Returns the given value shifted and masked to the field's position.
		 * @param value The value.
		 * @return The value left shifted by <tt>lsb</tt> and with the bits out of the interval <tt>[msb,lsb]</tt> masked.
		 */
		public int getValueInField(int value) {
			return (value << lsb) & mask;
		}
		
		/**
		 * Returns this field's value from the given instruction's data.
		 * @param data The data of the instruction.
		 * @return This field's value.
		 */
		public int getValueFromField(int data) {
			return (data & mask) >>> lsb;
		}

		@Override
		public String toString() {
			return getId() + "(" + getSize() + ")";
		}
	}
}
