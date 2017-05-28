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

import brunonova.drmips.simulator.components.*;
import brunonova.drmips.simulator.exceptions.InvalidCPUException;
import brunonova.drmips.simulator.util.Point;
import java.util.LinkedList;
import java.util.List;

/**
 * Class that represents an output of a component.
 * 
 * <p>Each output belongs to a component, has an identifier and has some
 * data with a size (number of bits) and value.<br>
 * The output can be connected to the input of another component.<br>
 * <b>To change the Data value, call this class's <tt>setValue()</tt> instead of
 * <tt>getData().setValue()</tt>, or else the connected input value won't be
 * updated!</b></p>
 * 
 * @author Bruno Nova
 */
public final class Output extends IOPort {
	/** The default direction of outputs. */
	public static final Direction DEFAULT_DIRECTION = Direction.EAST;
	
	/** The input this output is connected to. */
	private Input connectedTo = null;
	/** The intermediate points of the graphical wire. */
	private List<Point> points = null;
	/** Whether this output and its wire is in the critical path. */
	private boolean inCriticalPath = false;
	/** Whether the output and its wire is relevant. */
	private boolean relevant = true;
	
	/**
	 * Creates an output with the given parameters.
	 * @param component The component that this output belongs to.
	 * @param id The identifier of the output.
	 * @param data Data of the output (size and initial value).
	 * @throws InvalidCPUException If <tt>id</tt> is empty.
	 */
	public Output(Component component, String id, Data data) throws InvalidCPUException {
		this(component, id, data, DEFAULT_DIRECTION, false);
	}
	
	/**
	 * Creates an output with the given parameters.
	 * @param component The component that this output belongs to.
	 * @param id The identifier of the output.
	 * @param data Data of the output (size and initial value).
	 * @param direction The direction/side of the output on the component.
	 * @throws InvalidCPUException If <tt>id</tt> is empty.
	 */
	public Output(Component component, String id, Data data, Direction direction) throws InvalidCPUException {
		this(component, id, data, direction, false);
	}
	
	/**
	 * Creates an output with the given parameters.
	 * @param component The component that this output belongs to.
	 * @param id The identifier of the output.
	 * @param data Data of the output (size and initial value).
	 * @param direction The direction/side of the output on the component.
	 * @param showTip Whether a balloon tip with the value of the input/output should be displayed.
	 * @throws InvalidCPUException If <tt>id</tt> is empty.
	 */
	public Output(Component component, String id, Data data, Direction direction, boolean showTip) throws InvalidCPUException {
		super(component, id, data, direction, showTip);
		points = new LinkedList<>();
	}
	
	/**
	 * Connect this output to another component's input.
	 * @param input The input to connect to.
	 * @throws InvalidCPUException If this or the input are already connected or have different sizes.
	 */
	protected void connectTo(Input input) throws InvalidCPUException {
		if(this.isConnected()) 
			throw new InvalidCPUException(getComponent().getId() + ":" + getId() + " is already connected!");
		if(input.isConnected())
			throw new InvalidCPUException(input.getComponent().getId() + ":" + input.getId() + " is already connected!");
		if(this.getSize() != input.getSize())
			throw new InvalidCPUException(getComponent().getId() + ":" + getId() + " and " + input.getComponent().getId() + ":" + input.getId() + " have different sizes!");
		
		connectedTo = input;
		input.connectedTo = this;
		connectedTo.setValue(getValue()); // update the input's value
	}
	
	/**
	 * Returns the input this output is connected to.
	 * @return The input this output is connected to.
	 */
	public Input getConnectedInput() {
		return connectedTo;
	}

	@Override
	public boolean isConnected() {
		return connectedTo != null;
	}

	/**
	 * Updates the value of this output's data.
	 * <p>It also updates the value of the connected input, so call this method
	 * instead of <tt>getData().setValue()</tt> directly!</p>
	 * @param value New value.
	 */
	@Override
	public void setValue(int value) {
		setValue(value, true);
	}
	
	/**
	 * Updates the value of this output's data.
	 * <p>It can also update the value of the connected input if <tt>propagate == true</tt>, 
	 * so call this method instead of <tt>getData().setValue()</tt> directly!</p>
	 * @param value New value.
	 * @param propagate Whether to propagate the value to the connected input (only if the value changes!).
	 */
	public void setValue(int value, boolean propagate) {
		int oldValue = getValue();
		super.setValue(value);
		if(getSize() == 1) setRelevant(getValue() == 1); // set whether relevant or not automatically, if it is a single bit
		if(isConnected() && propagate && getValue() != oldValue)
			connectedTo.setValue(value); // update value of connected input
	}
	
	/**
	 * Returns the intermediate points of the wire.
	 * @return The intermediate points of the graphical wire.
	 */
	public List<Point> getIntermediatePoints() {
		return points;
	}
	
	/**
	 * Returns whether the output's wire has intermediate points.
	 * @return <tt>True</tt> if the wire has intermediate points.
	 */
	public boolean hasIntermediatePoints() {
		return points != null && !points.isEmpty();
	}
	
	/**
	 * Adds an intermediate point to the output's wire.
	 * @param point Point to add.
	 */
	public void addIntermediatePoint(Point point) {
		points.add(point);
	}

	@Override
	public void setInControlPath() {
		boolean old = isInControlPath();
		if(!old) {
			super.setInControlPath();
			if(isConnected() && !getConnectedInput().isInControlPath())
				getConnectedInput().setInControlPath();
			
			Component c = getComponent();
			if(c instanceof Constant || c instanceof Not || c instanceof SignExtend || c instanceof ZeroExtend
				|| c instanceof ShiftLeft)
				c.setInControlPath();
		}
	}

	@Override
	public boolean isInCriticalPath() {
		return inCriticalPath;
	}

	@Override
	public void setInCriticalPath(boolean critical) {
		inCriticalPath = critical;
	}

	@Override
	public boolean isRelevant() {
		return relevant;
	}

	@Override
	public void setRelevant(boolean relevant) {
		this.relevant = relevant;
	}
}
