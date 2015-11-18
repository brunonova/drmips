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
 * Interface that declares that a component is synchronous.
 *
 * <p>Synchronous components must implement <tt>executeSynchronous()</tt>, besides
 * what is required by the Component base class.<br><br>
 * Synchronous components also have an internal state, and must implement
 * <tt>pushState()</tt>, <tt>popState()</tt>, <tt>hasSavedStates()</tt>,
 * <tt>clearSavedStates()</tt> and <tt>resetFirstState()</tt>.<br>
 * These methods are called automatically to save the internal state of the component
 * (to a stack, for example) or to restore the previous state.</p>
 *
 * @author Bruno Nova
 */
public interface Synchronous {
	/**
	 * "Executes" the synchronous action of the component on a clock transition, like updating the component's state.
	 * <p>This method is executed automatically at the start of each clock cycle.</p>
	 */
	public abstract void executeSynchronous();

	/**
	 * Saves the state of the component in the stack of states.
	 * <p>Subclasses that have an internal state must implement this method to
	 * allow the "back step" function to work.</p>
	 */
	public void pushState();

	/**
	 * Loads the state of the component in the last clock cycle, from the stack of states.
	 * <p>Subclasses that have an internal state must implement this method to
	 * allow the "back step" function to work.</p>
	 */
	public void popState();

	/**
	 * Returns whether the component has saved states (if a "back step" is possible).
	 * <p>Subclasses that have an internal state must implement this method to
	 * allow the "back step" function to work.</p>
	 * @return <tt>True</tt> if the component has saved states.
	 */
	public boolean hasSavedStates();

	/**
	 * Removes all saved states.
	 * <p>Subclasses that have an internal state must implement this method to
	 * allow the "back step" function to work.</p>
	 */
	public void clearSavedStates();

	/**
	 * Resets the component to the first saved state.
	 * <p>Subclasses that have an internal state must implement this method to
	 * allow the "restart" function to work.</p>
	 */
	public void resetFirstState();

	/**
	 * Returns whether the component's internal state will be changed in the next
	 * clock transition.
	 * <p>Subclasses that have an internal state must implement this method to
	 * allow the critical path of the instruction to be determined.</p>
	 * @return <tt>true</tt> if the internal state is to be changed in this clock cycle.
	 */
	public boolean isWritingState();
}
