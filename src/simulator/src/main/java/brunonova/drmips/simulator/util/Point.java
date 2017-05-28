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
 * Simple abstraction that saves some coordinates in a 2D space.
 * 
 * @author Bruno Nova
 */
public final class Point {
	/** X coordinate. */
	public int x = 0;
	/** Y coordinate. */
	public int y = 0;

	/**
	 * Creates a point with the coordinates (0,0).
	 */
	public Point() {
	}
	
	/**
	 * Creates a copy of the given point.
	 * @param p The point to copy from.
	 */
	public Point(Point p) {
		this(p.x, p.y);
	}
	
	/**
	 * Creates a point with the given coordinates.
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 */
	public Point(int x, int y) {
		setX(x);
		setY(y);
	}

	/**
	 * Updates the X coordinate.
	 * @param x New value.
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Updates the y coordinate.
	 * @param y New value.
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Returns the X coordinate.
	 * @return X coordinate.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Returns the Y coordinate.
	 * @return Y coordinate.
	 */
	public int getY() {
		return y;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Point){
			Point p = (Point)obj;
			return x == p.x && y == p.y;
		}
		else
			return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 97 * hash + this.x;
		hash = 97 * hash + this.y;
		return hash;
	}
}
