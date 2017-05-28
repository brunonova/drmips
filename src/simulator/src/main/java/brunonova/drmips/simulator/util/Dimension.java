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

package brunonova.drmips.simulator.util;

/**
 * Simple abstraction that saves the size of a 2D object.
 * 
 * @author Bruno Nova
 */
public final class Dimension {
	/** The width. */
	public int width = 0;
	/** The height. */
	public int height = 0;

	/**
	 * Creates a dimension with no size.
	 */
	public Dimension() {
	}

	/**
	 * Creates a copy of the given dimension.
	 * @param d The dimension to copy from.
	 */
	public Dimension(Dimension d) {
		this(d.width, d.height);
	}
	
	/**
	 * Creates a dimension with the given parameters.
	 * @param width The width.
	 * @param height The height.
	 */
	public Dimension(int width, int height) {
		setWidth(width);
		setHeight(height);
	}
	
	/**
	 * Updates the width.
	 * @param width New value.
	 */
	public void setWidth(int width) {
		this.width = (width >= 0) ? width : 0;
	}

	/**
	 * Updates the height.
	 * @param height  New value.
	 */
	public void setHeight(int height) {
		this.height = (height >= 0) ? height : 0;
	}

	/**
	 * Returns the width.
	 * @return The width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns the height.
	 * @return The height.
	 */
	public int getHeight() {
		return height;
	}

	@Override
	public String toString() {
		return width + "x" + height;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Dimension){
			Dimension d = (Dimension)obj;
			return width == d.width && height == d.height;
		}
		else
			return false;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 61 * hash + this.width;
		hash = 61 * hash + this.height;
		return hash;
	}
}
