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

import brunonova.drmips.simulator.components.ALU;
import brunonova.drmips.simulator.components.ExtendedALU;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class with the information of how the ALU Control and ALU should work for the instruction set.
 * 
 * @author Bruno Nova
 */
public class ControlALU {
	/** The possible ALU operations. */
	public enum Operation {ADD, SUB, AND, OR, SLT, XOR, SLL, SRL, SRA, NOR, MULT, 
		DIV, MFHI, MFLO}
	
	/** The size of the <tt>ALUOp</tt> control signal. */
	private int aluOpSize;
	/** The size of the <tt>func</tt> field. */
	private int funcSize;
	/** The size of the ALU control output. */
	private int controlSize;
	/** Mapping of Inputs (ALUOp and func) options to output values. */
	private Map<Inputs, Map<String, Integer>> control;
	/** The sizes of each output. */
	private Map<String, Integer> out;
	/** Mapping of ALU control input options and their respective operations. */
	private Map<Integer, Operation> operations;
	/** Class logger. */
	private static final Logger LOG = Logger.getLogger(ControlALU.class.getName());
	
	/**
	 * Creates a new control object.
	 * @param aluOpSize The size of the <tt>ALUOp</tt> control signal.
	 * @param funcSize The size of the <tt>func</tt> field.
	 * @param controlSize The size of the ALU control output.
	 */
	public ControlALU(int aluOpSize, int funcSize, int controlSize) {
		this.aluOpSize = aluOpSize;
		this.funcSize = funcSize;
		this.controlSize = controlSize;
		control = new TreeMap<>();
		out = new TreeMap<>();
		operations = new TreeMap<>();
	}
	
	/**
	 * Maps an ALUOp signal to the specified control output.
	 * @param aluOp The value of the ALUOp signal.
	 * @param outId The identifier of the output.
	 * @param outValue The corresponding ALU control signal value.
	 */
	public void addALUOpControl(int aluOp, String outId, int outValue) {
		Inputs i = new Inputs(aluOp);
		Map<String, Integer> o;
		if(control.containsKey(i)) {
			o = control.get(i);
			o.put(outId, outValue);
		}
		else {
			o = new TreeMap<>();
			o.put(outId, outValue);
			control.put(i, o);
		}
		updateOutSize(outId, Data.requiredNumberOfBits(outValue));
	}
	
	/**
	 * Maps an ALUOp signal and instruction func field to the specified control output.
	 * @param aluOp The value of the ALUOp signal.
	 * @param func The value of the instruction func field.
	 * @param outId The identifier of the output.
	 * @param outValue The corresponding ALU control signal value.
	 */
	public void addFuncControl(int aluOp, int func, String outId, int outValue) {
		Inputs i = new Inputs(aluOp, func);
		Map<String, Integer> o;
		if(control.containsKey(i)) {
			o = control.get(i);
			o.put(outId, outValue);
		}
		else {
			o = new TreeMap<>();
			o.put(outId, outValue);
			control.put(i, o);
		}
		updateOutSize(outId, Data.requiredNumberOfBits(outValue));
	}
	
	/**
	 * Maps a control from the ALU Control to an operation.
	 * @param control The control signal.
	 * @param operation The corresponding operation.
	 */
	public void addOperation(int control, Operation operation) {
		operations.put(control, operation);
	}
	
	/**
	 * Maps a control from the ALU Control to an operation.
	 * @param control The control signal.
	 * @param operation The corresponding operation, as a string.
	 */
	public void addOperation(int control, String operation) {
		try {
			addOperation(control, Operation.valueOf(operation.toUpperCase()));
		}
		catch(IllegalArgumentException e) { // unknonw operation?
			addOperation(control, Operation.ADD);
			LOG.log(Level.WARNING, "error adding operation \"" + operation + "\"", e);
		}
	}
	
	/**
	 * Returns the value of the ALU Control output signal for the specified ALUOp and func.
	 * @param aluOp The value of the ALUOp signal.
	 * @param func The value of the instruction func field.
	 * @param outId The identifier of the output.
	 * @return Value of the control signal for the ALU.
	 */
	public int getControlValue(int aluOp, int func, String outId) {
		Inputs i = new Inputs(aluOp, func);
		if(control.containsKey(i) && control.get(i).containsKey(outId))
			return control.get(i).get(outId);
		else
			return 0;
	}
	
	/**
	 * Returns the operation that corresponds to the specifield ALU control signal.
	 * @param control The control signal.
	 * @return The corresponding operation.
	 */
	public Operation getOperation(int control) {
		if(operations.containsKey(control))
			return operations.get(control);
		else
			return Operation.ADD;
	}
	
	/**
	 * Executes the operations for the given values according to the specified operation.
	 * @param val1 The first value.
	 * @param val2 The second value.
	 * @param alu The ALU.
	 * @param operation The operation to execute, as the value of the ALU control signal.
	 * @return The result.
	 */
	public int doOperation(int val1, int val2, ALU alu, int operation) {
		return doOperation(val1, val2, alu, getOperation(operation));
	}
	
	/**
	 * Executes the operations for the given values according to the specified operation.
	 * @param val1 The first value.
	 * @param val2 The second value.
	 * @param alu The ALU.
	 * @param operation The operation to execute.
	 * @return The result.
	 */
	public int doOperation(int val1, int val2, ALU alu, Operation operation) {
		switch(operation) {
			case ADD: return val1 + val2;
			case SUB: return val1 - val2;
			case AND: return val1 & val2;
			case OR:  return val1 | val2;
			case SLT: return (val1 < val2) ? 1 : 0;
			case XOR: return val1 ^ val2;
			case SLL: return val1 << val2;
			case SRL: return val1 >>> val2;
			case SRA: return val1 >> val2;
			case NOR: return ~(val1 | val2);
			case MULT:
			case DIV: return 0;
			case MFHI: return (alu instanceof ExtendedALU) ? ((ExtendedALU)alu).getHI().getValue() : 0;
			case MFLO: return (alu instanceof ExtendedALU) ? ((ExtendedALU)alu).getLO().getValue() : 0;
			default:  return val1 + val2;
		}
	}
	
	/**
	 * Executes the synchronous part of the operations for the given values according to the specified operation.
	 * <p>Used by the extended ALU.</p>
	 * @param val1 The first value.
	 * @param val2 The second value.
	 * @param alu The extended ALU.
	 * @param operation The operation to execute, as the value of the ALU control signal.
	 */
	public void doSynchronousOperation(int val1, int val2, ExtendedALU alu, int operation) {
		doSynchronousOperation(val1, val2, alu, getOperation(operation));
	}
	
	/**
	 * Executes the synchronous part of the operations for the given values according to the specified operation.
	 * <p>Used by the extended ALU.</p>
	 * @param val1 The first value.
	 * @param val2 The second value.
	 * @param alu The extended ALU.
	 * @param operation The operation to execute.
	 */
	public void doSynchronousOperation(int val1, int val2, ExtendedALU alu, Operation operation) {
		switch(operation) {
			case MULT:
				long res = (long)val1 * (long)val2;
				alu.getLO().setValue((int)res);
				alu.getHI().setValue((int)(res >>> 32));
				break;
			case DIV:
				if(val2 != 0) {
					alu.getLO().setValue(val1 / val2);
					alu.getHI().setValue(val1 % val2);
				}
				else { // should throw an exception
					alu.getLO().setValue(Integer.MIN_VALUE);
					alu.getHI().setValue(Integer.MIN_VALUE);
				}
				break;
		}
	}
	
	/**
	 * Returns whether the extended ALU's internal registers will be written in
	 * this clock cycle.
	 * @param operation The operation that will be executed, as the value of the ALU control signal.
	 * @return <tt>true</tt> if the internal state is to be changed in this clock cycle.
	 */
	public boolean isWritingState(int operation) {
		Operation op = getOperation(operation);
		return op == Operation.MULT || op == Operation.DIV;
	}
	
	/**
	 * Updates the size of the output with the specified identifier, if bigger.
	 * @param id Identifier of the output.
	 * @param size New size, updated if bigger than before.
	 */
	private void updateOutSize(String id, int size) {
		if(!out.containsKey(id) || size > out.get(id))
			out.put(id, size);
	}
	
	/**
	 * Returns whether the control has the specified output.
	 * @param id The identifier of the output to check.
	 * @return <tt>True</tt> the the output exists.
	 */
	public boolean hasOut(String id) {
		return out.containsKey(id);
	}
	
	/**
	 * Returns the size of the specified output.
	 * @param id The identifier of the output.
	 * @return Size of the output.
	 */
	public int getOutSize(String id) {
		if(!hasOut(id)) return 1;
		return out.get(id);
	}

	/**
	 * Returns the ids of all the outputs (ids and sizes).
	 * @return Ids of all outputs.
	 */
	public Set<String> getOutputsIds() {
		return out.keySet();
	}
	
	/**
	 * Returns the size of the <tt>func</tt> field.
	 * @return The size of the <tt>func</tt> field.
	 */
	public int getFuncSize() {
		return funcSize;
	}

	/**
	 * Returns the size of the <tt>ALUOp</tt> control signal.
	 * @return The size of the <tt>ALUOp</tt> control signal.
	 */
	public int getAluOpSize() {
		return aluOpSize;
	}

	/**
	 * Returns the size of the ALU control output.
	 * @return The size of the ALU control output.
	 */
	public int getControlSize() {
		return controlSize;
	}
	
	
	/**
	 * Represents a combination of inputs in the ALU control.
	 */
	private class Inputs implements Comparable<Inputs> {
		/** The ALUOp control input value. */
		private int aluOp = 0;
		/** The function field input value. */
		private int func = 0;
		/** Whether the function input is ignored (outputs decided only by ALUOp). */
		private final boolean funcIgnored;

		/**
		 * Constructor with only the ALUOp input (func input ignored).
		 * @param aluOp The ALUOp control input value.
		 */
		public Inputs(int aluOp) {
			this.aluOp = aluOp;
			funcIgnored = true;
		}
		
		/**
		 * Constructor with both ALUOp and func inputs.
		 * @param aluOp The ALUOp control input value.
		 * @param func The function field input value.
		 */
		public Inputs(int aluOp, int func) {
			this.aluOp = aluOp;
			this.func = func;
			funcIgnored = false;
		}

		/**
		 * Returns the ALUOp control input value.
		 * @return The ALUOp control input value.
		 */
		public int getAluOp() {
			return aluOp;
		}

		/**
		 * Returns the function field input value.
		 * @return The function field input value.
		 */
		public int getFunc() {
			return func;
		}

		/**
		 * Return whether the function input is ignored.
		 * @return Whether the function input is ignored (outputs decided only by ALUOp).
		 */
		public boolean isFuncIgnored() {
			return funcIgnored;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Inputs) {
				Inputs o = (Inputs)obj;
				return (funcIgnored || o.funcIgnored) ? (aluOp == o.aluOp) : (aluOp == o.aluOp && func == o.func);
			}
			else
				return false;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 23 * hash + this.aluOp;
			hash = 23 * hash + this.func;
			return hash;
		}

		@Override
		public int compareTo(Inputs o) {
			if(aluOp < o.aluOp)
				return -1;
			else if(aluOp > o.aluOp)
				return 1;
			else if(funcIgnored || o.funcIgnored)
				return 0;
			else {
				if(func < o.func)
					return -1;
				else if(func > o.func)
					return 1;
				else
					return 0;
			}
		}
	}
}
