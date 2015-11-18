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

import brunonova.drmips.simulator.exceptions.InvalidCPUException;
import brunonova.drmips.simulator.util.Point;

/**
 * Abstract base class that represents an input or an output of a component.
 *
 * <p>Each input/output belongs to a component, has an identifier and has some
 * data with a size (number of bits) and value.</p>
 *
 * @author Bruno Nova
 */
public abstract class IOPort {
	/** The possible directions/sides of the input/output on the component. */
	public enum Direction { WEST, EAST, NORTH, SOUTH }

	/** The component that this input/output belongs to. */
	private Component component;
	/** The identifier of the input/output. */
	private String id;
	/** Data of the input/output (size and initial value). */
	private Data data;
	/** The direction/side of the input/output on the component. */
	private Direction direction;
	/** The graphical position of the input/output (if <tt>null</tt> it's calculated automatically). */
	private Point position;
	/** Whether this input/output is in the control path. */
	private boolean inControlPath = false;
	/** Whether a balloon tip with the value of the input/output should be displayed. */
	private boolean showTip = false;

	/**
	 * Creates an input/output with the given parameters.
	 * <p>Constructors of subclasses should call <tt>super(component, id, data)</tt>.</p>
	 * @param component The component that this input/output belongs to.
	 * @param id The identifier of the input/output.
	 * @param data Data of the input/output (size and initial value).
	 * @param direction The direction/side of the input/output on the component.
	 * @param showTip Whether a balloon tip with the value of the input/output should be displayed.
	 * @throws InvalidCPUException If <tt>id</tt> is empty.
	 */
	public IOPort(Component component, String id, Data data, Direction direction, boolean showTip) throws InvalidCPUException {
		if(id.isEmpty()) throw new InvalidCPUException("Invalid ID " + id + "!");
		this.component = component;
		this.id = id;
		this.data = data;
		this.direction = direction;
		this.showTip = showTip;
	}

	/**
	 * Returns the component this input/output belongs to.
	 * @return Component this input/output belongs to.
	 */
	public Component getComponent() {
		return component;
	}

	/**
	 * Returns whether this input/output is connected to another component's input/output.
	 * @return <tt>true</tt> if this input/output is connected to an input/output.
	 */
	public abstract boolean isConnected();

	/**
	 * Returns the identifier of this input/output.
	 * @return The id of this input/output.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the data of this input/output.
	 * @return The data of this input/output
	 */
	public Data getData() {
		return data;
	}

	/**
	 * Simply returns <tt>getData().getValue()</tt>.
	 * @return Value of the data.
	 */
	public int getValue() {
		return data.getValue();
	}

	/**
	 * Simply calls <tt>getData().setValue(value)</tt>.
	 * @param value New value.
	 */
	public void setValue(int value) {
		data.setValue(value);
	}

	/**
	 * Simply calls <tt>getData().getSize()</tt>.
	 * @return Size of the data (number of bits).
	 */
	public int getSize() {
		return data.getSize();
	}

	/**
	 * Returns the direction/side of the input/output on the component.
	 * @return Direction/side of the input/output on the component.
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * Returns the graphical position of the input/output.
	 * @return Position of the input/output (<tt>null</tt> if calculated automatically).
	 */
	public Point getPosition() {
		return position;
	}

	/**
	 * Updates the graphical position of the input/output.
	 * @param position The new position.
	 */
	public void setPosition(Point position) {
		this.position = position;
	}

	/**
	 * Returns whether the graphical position of the input/output has been defined.
	 * @return <tt>True</tt> if the position is defined.
	 */
	public boolean hasPositionDefined() {
		return position != null;
	}

	/**
	 * Indicates that this input/output is in the control path.
	 */
	public void setInControlPath() {
		inControlPath = true;
	}

	/**
	 * Returns whether this input/output is in the control path.
	 * @return <tt>True</tt> if in control path.
	 */
	public boolean isInControlPath() {
		return inControlPath;
	}

	/**
	 * Returns whether a balloon tip with the value of the input/output should be displayed.
	 * @return <tt>True</tt> if a balloon tip with the value of the input/output should be displayed.
	 */
	public boolean shouldShowTip() {
		return showTip;
	}

	/**
	 * Returns whether the input/output and its wire is relevant.
	 * @return <tt>True</tt> if relevant.
	 */
	public abstract boolean isRelevant();

	/**
	 * Sets whether the input/output and its wire is relevant.
	 * @param relevant Whether it's relevant.
	 */
	public abstract void setRelevant(boolean relevant);

	/**
	 * Returns whether this input/output and its wire is in the critical path.
	 * @return <tt>True</tt> if in the critical path.
	 */
	public abstract boolean isInCriticalPath();

	/**
	 * Defines whether this output and its wire is in the critical path.
	 * @param critical Whether it's in the critical path.
	 */
	public abstract void setInCriticalPath(boolean critical);
}
