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

/**
 * Class that can store and manage a value with a given size (in bits).
 * <p>The maximum size of the value is <tt>MSB + 1</tt>.</p>
 * @author Bruno Nova
 */
public final class Data implements Cloneable {
	/** 
	 * The size, in bits, of memory addresses, registers, etc (32 or 64 bit).
	 * <p>Changing this value to 64 is not enough for 64 bit, as several <tt>int</tt>
	 * variables and paramenters would have to be changed to <tt>long</tt>, among 
	 * other things.</p>
	 */
	public static final int DATA_SIZE = 32;
	/** The position of the most significant bit. */
	public static final int MSB = DATA_SIZE - 1;
	
	/** The size of the value (number of bits). */
	private int size;
	/** The value cut to the given size. */
	private int value = 0;
	/** The mask of the value for the given size. */
	private int mask;
	
	/**
	 * Default constructor that creates a Data object with the maximum size (<tt>MSB + 1</tt>).
	 */
	public Data() {
		this(MSB + 1);
	}
	
	/**
	 * Constructor that creates a Data object with the specified size.
	 * @param size Size of the value (number of bits).
	 */
	public Data(int size) {
		setSize(size);
	}
	
	/**
	 * Constructor that creates a Data object with the specified size and value.
	 * @param size Size of the value (number of bits).
	 * @param value The value.
	 */
	public Data(int size, int value) {
		this(size);
		setValue(value);
	}
	
	/**
	 * Creates a bit mask with 1's in the specified interval.
	 * <p>The mask will have 1's in the bits between <tt>minBit</tt> and
	 * <tt>maxBit</tt>, both parameters included in the range, and 0's in the
	 * other bits.<br>
	 * The less significant bit is 0 and the most significant bit is <tt>MSB</tt>.</p>
	 * @param maxBit The highest (leftmost) bit of the mask.
	 * @param minBit The lowest  (rightmost) bit of the mask.
	 * @return The desired mask.
	 */
	public static int createMask(int maxBit, int minBit) {
		// Check maxBit and minBit
		if(maxBit > MSB) maxBit = MSB; 
		else if(maxBit < 0) maxBit = 0;
		if(minBit > MSB) minBit = MSB;
		else if(minBit < 0) minBit = 0;
		if(maxBit < minBit) {
			int tmp = maxBit;
			maxBit = minBit;
			minBit = tmp;
		}
		
		return (-1 >>> (MSB - (maxBit - minBit))) << minBit;
	}
	
	/**
	 * Returns whether the specified positive number is a power of 2.
	 * @param value Positive number to check.
	 * @return <tt>true</tt> if <tt>value</tt> is a power of 2.
	 */
	public static boolean isPowerOf2(int value) {
		return (value > 0) && ((value & (value - 1)) == 0);
	}
	
	/**
	 * Returns the number of bits required to represent the specified number.
	 * @param value Number to check.
	 * @return The number of bits required to represent the <tt>value</tt>.
	 */
	public static int requiredNumberOfBits(int value) {
		int n = 0;
		while(value != 0) {
			value = value >>> 1;
			n++;
		}
		return n;
	}
	
	/**
	 * Returns the size of the value.
	 * @return Size of the value (number of bits).
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * Sets the size of the value and creates a suitable mask.
	 * @param size Size of the value (number of bits).
	 */
	private void setSize(int size) {
		if(size > (MSB + 1)) size = MSB + 1;
		else if(size <= 0) size = 1;
		this.size = size;
		this.mask = createMask(size - 1, 0);
	}
	
	/**
	 * Returns the value.
	 * @return The value cut to the given size.
	 */
	public int getValue() {
		return value;
	}
	
	/**
	 * Updates the value, that will be cut to the given size.
	 * @param value The new value.
	 */
	public void setValue(int value) {
		this.value = value & mask;
	}
	
	/**
	 * Returns the mask for the given size.
	 * @return Mask of the value for the given size.
	 */
	public int getMask() {
		return mask;
	}
	
	/**
	 * Returns a new Data object with the value extended with zeros to the given size.
	 * @param size The new size (number of bits).
	 * @return Data object with extended size.
	 */
	public Data zeroExtend(int size) {
		if(size < this.size) size = this.size;
		return new Data(size, value);
	}
	
	/**
	 * Returns a new Data object with the value sign extended to the given size.
	 * @param size The new size (number of bits).
	 * @return Data object with extended size.
	 */
	public Data signExtend(int size) {
		if(size < this.size) size = this.size;
		int sa = MSB - getSize() + 1;
		return new Data(size, (value << sa) >> sa);
	}
	
	/**
	 * Returns a new Data object with the value shrunk to the given size.
	 * @param size The new size (number of bits).
	 * @return Data object with shrunken size.
	 */
	public Data shrink(int size) {
		if(size > this.size) size = this.size;
		return new Data(size, value);
	}

	/**
	 * Returns the binary representation of the value.
	 * @return Value in binary, including leading zeros.
	 */
	public String toBinary() {
		String str = Integer.toBinaryString(value);
		String res = "";
		int len = str.length();
		int c = 0;
		for(int i = len - 1; i >= 0; i--) {
			if(++c > 4) {
				c = 1;
				res = " " + res;
			}
			res = str.charAt(i) + res;
		}
		for(int i = len; i < size; i++) {
			if(++c > 4) {
				c = 1;
				res = " " + res;
			}
			res = "0" + res;
		}
		return res;
	}
	
	/**
	 * Returns the octal representation of the value.
	 * @return Value in octal, including leading zeros.
	 */
	public String toOctal() {
		String str = Integer.toOctalString(value);
		for(int i = str.length() * 3; i < size; i += 3)
			str = "0" + str;
		return str;
	}
	
	/**
	 * Returns the hexadecimal representation of the value.
	 * @return Value in hexadecimal, including leading zeros.
	 */
	public String toHexadecimal() {
		String str = Integer.toHexString(value);
		for(int i = str.length() * 4; i < size; i += 4)
			str = "0" + str;
		return str;
	}
	
	@Override
	public String toString() {
		return "" + value;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Data) {
			Data d = (Data)obj;
			return d.getSize() == getSize() && d.getValue() == getValue();
		}
		else
			return false;
	}

	@Override
	public int hashCode() {
		return getValue();
	}

	@Override
	public Data clone() {
		try {
			Data c = (Data)super.clone();
			c.setSize(getSize());
			c.setValue(getValue());
			return c;
		} catch (CloneNotSupportedException ex) {
			throw new Error("Error cloning a Data object!", ex);
		}
	}
}
