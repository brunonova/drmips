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

/**
 * Class that represents an input of a component.
 * 
 * <p>Each input belongs to a component, has an identifier and has some
 * data with a size (number of bits) and value.</p>
 * 
 * @author Bruno Nova
 */
public final class Input extends IOPort {
	/** The default direction of inputs. */
	public static final Direction DEFAULT_DIRECTION = Direction.WEST;
	
	/** The output this intput is connected to. */
	protected Output connectedTo = null;
	/** The acumulated latency from the first component up to this input. */
	private int accumulatedLatency = 0;
	/** Whether this input changes the respective component's accumulated latency. */
	private boolean changesComponentAccumulatedLatency = true;
	
	/**
	 * Creates an input with the given parameters.
	 * @param component The component that this input belongs to.
	 * @param id The identifier of the input.
	 * @param data Data of the input (size and initial value).
	 * @throws InvalidCPUException If <tt>id</tt> is empty.
	 */
	public Input(Component component, String id, Data data) throws InvalidCPUException {
		this(component, id, data, DEFAULT_DIRECTION);
	}
	
	/**
	 * Creates an input with the given parameters.
	 * @param component The component that this input belongs to.
	 * @param id The identifier of the input.
	 * @param data Data of the input (size and initial value).
	 * @param direction The direction/side of the input on the component.
	 * @throws InvalidCPUException If <tt>id</tt> is empty.
	 */
	public Input(Component component, String id, Data data, Direction direction) throws InvalidCPUException {
		this(component, id, data, direction, true, false);
	}
	
	/**
	 * Creates an input with the given parameters.
	 * @param component The component that this input belongs to.
	 * @param id The identifier of the input.
	 * @param data Data of the input (size and initial value).
	 * @param direction The direction/side of the input on the component.
	 * @param changesComponentAccumulatedLatency Whether this input changes the respective component's accumulated latency.
	 * @throws InvalidCPUException If <tt>id</tt> is empty.
	 */
	public Input(Component component, String id, Data data, Direction direction, boolean changesComponentAccumulatedLatency) throws InvalidCPUException {
		this(component, id, data, direction, changesComponentAccumulatedLatency, false);
	}
	
	/**
	 * Creates an input with the given parameters.
	 * @param component The component that this input belongs to.
	 * @param id The identifier of the input.
	 * @param data Data of the input (size and initial value).
	 * @param direction The direction/side of the input on the component.
	 * @param changesComponentAccumulatedLatency Whether this input changes the respective component's accumulated latency.
	 * @param showTip Whether a balloon tip with the value of the input/output should be displayed.
	 * @throws InvalidCPUException If <tt>id</tt> is empty.
	 */
	public Input(Component component, String id, Data data, Direction direction, boolean changesComponentAccumulatedLatency, boolean showTip) throws InvalidCPUException {
		super(component, id, data, direction, showTip);
		this.changesComponentAccumulatedLatency = changesComponentAccumulatedLatency;
	}
	
	/**
	 * Connect this input to another component's output.
	 * @param output The output to connect to.
	 * @throws InvalidCPUException If this or the output are already connected or have different sizes.
	 */
	protected void connectTo(Output output) throws InvalidCPUException {
		output.connectTo(this);
	}
	
	/**
	 * Returns the output this input is connected to.
	 * @return The output this input is connected to.
	 */
	public Output getConnectedOutput() {
		return connectedTo;
	}

	@Override
	public boolean isConnected() {
		return connectedTo != null;
	}

	/**
	 * Updates the value of this inouts's data.
	 * <p>It also executes the component's normal action, so call this method
	 * instead of <tt>getData().setValue()</tt> directly!</p>
	 * @param value New value.
	 */
	@Override
	public void setValue(int value) {
		int oldValue = getValue();
		super.setValue(value);
		if(getValue() != oldValue)
			getComponent().execute(); // input changed, so execute the component's normal action
	}
	
	/**
	 * Returns the acumulated latency from the first component up to this input.
	 * @return Input's accumulated latency.
	 */
	public int getAccumulatedLatency() {
		return accumulatedLatency;
	}
	
	/**
	 * Updates the input's accumulated latency.
	 * @param latency New accumulated latency.
	 * @param instructionDependent Whether the performance should depend on the current instruction or not.
	 */
	protected void setAccumulatedLatency(int latency, boolean instructionDependent) {
		this.accumulatedLatency = (latency >= 0) ? latency : 0;
		if(changesComponentAccumulatedLatency)
			getComponent().updateAccumulatedLatency(instructionDependent);
	}
	
	/**
	 * Updates the input's accumulated latency.
	 * @param latency New accumulated latency.
	 */
	protected void setAccumulatedLatency(int latency) {
		setAccumulatedLatency(latency, true);
	}
	
	/**
	 * Returns whether this input changes the respective component's accumulated latency.
	 * @return <tt>True</tt> if the input can change the component's accumulated latency.
	 */
	public boolean canChangeComponentAccumulatedLatency() {
		return changesComponentAccumulatedLatency;
	}
	
	/**
	 * Resets the accumulated latency to 0.
	 */
	public void resetAccumulatedLatency() {
		accumulatedLatency = 0;
	}

	@Override
	public void setInControlPath() {
		boolean old = isInControlPath();
		if(!old) {
			super.setInControlPath();
			if(isConnected() && !getConnectedOutput().isInControlPath())
				getConnectedOutput().setInControlPath();

			Component c = getComponent();
			if(c instanceof PipelineRegister) {
				PipelineRegister p = (PipelineRegister)c;
				if(p.hasOutput(getId()))
				p.getOutput(getId()).setInControlPath();
			}
			else if(c instanceof Fork || c instanceof Distributor || c instanceof Not || c instanceof And
				|| c instanceof Or || c instanceof Xor || c instanceof SignExtend || c instanceof ZeroExtend
				|| c instanceof ShiftLeft)
				c.setInControlPath();
		}
	}

	@Override
	public boolean isRelevant() {
		return isConnected() ? getConnectedOutput().isRelevant() : true;
	}

	@Override
	public void setRelevant(boolean relevant) {
		if(isConnected()) getConnectedOutput().setRelevant(relevant);
	}

	@Override
	public boolean isInCriticalPath() {
		return isConnected() ? getConnectedOutput().isInCriticalPath() : false;
	}

	@Override
	public void setInCriticalPath(boolean critical) {
		if(isConnected()) getConnectedOutput().setInCriticalPath(critical);
	}
}
