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
import brunonova.drmips.simulator.exceptions.*;
import brunonova.drmips.simulator.util.Dimension;
import brunonova.drmips.simulator.util.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that represents and manipulates a simulated MIPS CPU.
 *
 * @author Bruno Nova
 */
public class CPU {
	/** The path to the CPU files, with the trailing slash. */
	public static final String FILENAME_PATH = "cpu/";
	/** The file extension of the CPU files. */
	public static final String FILENAME_EXTENSION = "cpu";
	/** The regular expression to validate register names. */
	public static final String REGNAME_REGEX = "^[a-zA-Z][a-zA-Z\\d]*$";
	/** The prefix char of the registers. */
	public static final char REGISTER_PREFIX = '$';
	/** The extra margin added to the width of the graphical CPU's size. */
	public static final int RIGHT_MARGIN = 10;
	/** The extra margin added to the height of the graphical CPU's size. */
	public static final int BOTTOM_MARGIN = 10;
	/** The unit used in latencies. */
	public static final String LATENCY_UNIT = "ps";
	/** The exponent (e), that multiplied by 10 and the latency gives the real latency in seconds (ps * 10 ^ e). */
	public static final int LATENCY_EXPONENT = -12;
	/** The number of clock cycles executed in <tt>executeAll()</tt> after which it throws an exception. */
	public static final int EXECUTE_ALL_LIMIT_CYCLES = 1000;

	/** The file of the CPU. */
	private File file = null;
	/** The components that the CPU contains. */
	private Map<String, Component> components;
	/** The components that are synchronous (convenience list). */
	private List<Component> synchronousComponents;
	/** The names of the registers (without the prefix). */
	private List<String> registerNames = null;
	/** The loaded instruction set. */
	private InstructionSet instructionSet = null;
	/** The assembler for this CPU. */
	private Assembler assembler = null;
	/** The Program Counter (set automatically in <tt>addComponent()</tt>. */
	private PC pc = null;
	/** The register bank (set automatically in <tt>addComponent()</tt>. */
	private RegBank regbank = null;
	/** The instruction memory (set automatically in <tt>addComponent()</tt>. */
	private InstructionMemory instructionMemory = null;
	/** The control unit (set automatically in <tt>addComponent()</tt>. */
	private ControlUnit controlUnit = null;
	/** The ALU controller (set automatically in <tt>addComponent()</tt>. */
	private ALUControl aluControl = null;
	/** The ALU (set automatically in <tt>addComponent()</tt>. */
	private ALU alu = null;
	/** The data memory (set automatically in <tt>addComponent()</tt>. */
	private DataMemory dataMemory = null;
	/** The forwarding unit (set automatically in <tt>addComponent()</tt>. */
	private ForwardingUnit forwardingUnit = null;
	/** The hazard detection unit (set automatically in <tt>addComponent()</tt>. */
	private HazardDetectionUnit hazardDetectionUnit = null;
	/** The IF/ID register, if the CPU is pipelined. */
	private PipelineRegister ifIdReg = null;
	/** The ID/EX register, if the CPU is pipelined. */
	private PipelineRegister idExReg = null;
	/** The EX/MEM register, if the CPU is pipelined. */
	private PipelineRegister exMemReg = null;
	/** The MEM/WB register, if the CPU is pipelined. */
	private PipelineRegister memWbReg = null;

	/** Clock period in LATENCY_UNIT unit. */
	private int clockPeriod;
	/** Clock frequency in Hz. */
	private double clockFrequency;
	/** Number of executed cycles. */
	private int executedCycles = 0;
	/** Number of executed instructions. */
	private int executedInstructions = 0;
	/** Number of forwards. */
	private int forwards = 0;
	/** Number of stalls. */
	private int stalls = 0;
	/** Whether the latencies and critical path should depend on the current instruction. */
	private boolean performanceInstructionDependent = false;
	/** Breakpoint addr . */
	private int breakpointAddr = -1;

	/**
	 * Constructor that should by called by other constructors.
	 */
	protected CPU() {
		components = new TreeMap<>();
		synchronousComponents = new LinkedList<>();
		assembler = new Assembler(this);
	}

	/**
	 * Constructor that should by called by other constructors.
	 * @param file The file of the CPU.
	 */
	protected CPU(File file) {
		this();
		setFile(file);
	}

	/**
	 * Creates a CPU from a JSON file.
	 * <p><b>Don't forget to call <tt>setPerformanceInstructionDependent()</tt> on the CPU!</b></p>.
	 * @param path Path to the JSON file.
	 * @return CPU created from the file.
	 * @throws IOException If the file doesn't exist or an I/O error occurs.
	 * @throws JSONException If the JSON file is malformed.
	 * @throws InvalidCPUException If the CPU is invalid or incomplete
	 * @throws ArrayIndexOutOfBoundsException If an array index is invalid somewhere (like an invalid register).
	 * @throws InvalidInstructionSetException If the instruction set is invalid.
	 * @throws NumberFormatException If an opcode is not a number.
	 */
	public static CPU createFromJSONFile(String path) throws IOException, JSONException, InvalidCPUException, ArrayIndexOutOfBoundsException, InvalidInstructionSetException, NumberFormatException {
		CPU cpu = new CPU(new File(path));
		BufferedReader reader = null;
		String file = "", line, parentPath = ".";

		// Read file to String
		try {
			File f = new File(path);
			parentPath = f.getParentFile().getAbsolutePath();
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"));
			while((line = reader.readLine()) != null)
				file += line + "\n";
			reader.close();
		}
		catch(IOException e) {
			throw e;
		}
		finally {
			if(reader != null) reader.close();
		}

		// Parse the JSON file
		JSONObject json = new JSONObject(file);
		parseJSONComponents(cpu, json.getJSONObject("components"), parentPath);
		cpu.checkRequiredComponents();
		if(cpu.hasForwardingUnit()) cpu.forwardingUnit.setRegbank(cpu.getRegBank());
		if(cpu.hasHazardDetectionUnit()) cpu.hazardDetectionUnit.setRegbank(cpu.getRegBank());
		if(json.has("reg_names")) parseJSONRegNames(cpu, json.getJSONArray("reg_names"));
		cpu.instructionSet = new InstructionSet(parentPath + File.separator + json.getString("instructions"));
		cpu.controlUnit.setControl(cpu.getInstructionSet().getControl(), cpu.getInstructionSet().getOpCodeSize());
		if(cpu.hasALUControl()) cpu.aluControl.setControlALU(cpu.getInstructionSet().getControlALU());
		if(cpu.hasALU()) cpu.alu.setControlALU(cpu.getInstructionSet().getControlALU());
		parseJSONWires(cpu, json.getJSONArray("wires"));
		cpu.determineControlPath();

		for(Component c: cpu.getComponents()) // "execute" all components (initialize all outputs/inputs)
			c.execute();

		cpu.calculatePerformance();

		return cpu;
	}

	private void checkRequiredComponents() throws InvalidCPUException {
		if(pc == null) throw new InvalidCPUException("The program counter is required!");
		if(regbank == null) throw new InvalidCPUException("The register bank is required!");
		if(instructionMemory == null) throw new InvalidCPUException("The instruction memory is required!");
		if(controlUnit == null) throw new InvalidCPUException("The control unit is required!");

		// Check number of pipeline registers (must be 0 or 4)
		int count = 0;
		for(Component c: getComponents())
			if(c instanceof PipelineRegister) count++;
		if(count > 0 && count != 4)
			throw new InvalidCPUException("Pipelined CPUs must have exactly 4 pipeline registers (5 stages)!");
	}

	/**
	 * Calculates the latency in each component and input and determines the critical path of the CPU and of the instruction (if instruction dependent).
	 */
	public final void calculatePerformance() {
		// CPU performance
		calculateAccumulatedLatencies(false);
		determineClockPeriodAndFrequency();
		if(isPerformanceInstructionDependent()) // instruction performance?
			calculateAccumulatedLatencies(true);
		determineCriticalPath();
	}

	/**
	 * Calculates the latency in each component and input and determines the critical path of the instruction.
	 */
	protected final void calculateInstructionPerformance() {
		if(isPerformanceInstructionDependent()) {
			calculateAccumulatedLatencies(true);
			determineCriticalPath();
		}
	}

	/**
	 * Calculates the accumulated latencies of all components.
	 * @param instructionDependent If <tt>true</tt>, the latencies will depend on the current instruction.
	 */
	protected final void calculateAccumulatedLatencies(boolean instructionDependent) {
		for(Component c: getComponents()) // reset latencies and critical path
			c.resetPerformance();

		for(Component c: synchronousComponents) // calculate latencies
			c.updateAccumulatedLatency(instructionDependent);
	}

	/**
	 * Resets the latencies of all the components to their original latencies.
	 */
	public final void resetLatencies() {
		for(Component c: getComponents())
			c.resetLatency();
		calculatePerformance();
	}

	/**
	 * Sets the latencies of all components to 0 (zero).
	 */
	public final void removeLatencies() {
		for(Component c: getComponents())
			c.setLatency(0);
		calculatePerformance();
	}

	/**
	 * Returns the highest accumulated latency.
	 * @return Highest accumulated latency.
	 */
	private int findHighestAccumulatedLatency() {
		int maxLatency = 0;
		for(Component c: getComponents()) {
			if(c.getAccumulatedLatency() > maxLatency)
				maxLatency = c.getAccumulatedLatency();
			for(Input i: c.getInputs()) {
				if(i.getAccumulatedLatency() > maxLatency)
					maxLatency = i.getAccumulatedLatency();
			}
		}
		return maxLatency;
	}

	/**
	 * Returns the input(s) with the highest accumulated latency.
	 * @param instructionDependent Whether the result should depend on the current instruction-
	 * @return Input(s) with the highest accumulated latency.
	 */
	private List<Input> findHighetsAccumulatedLatencyInputs(boolean instructionDependent) {
		List<Input> maxIns = new LinkedList<>();
		int maxLatency = -1;
		Collection<Component> comps = isPerformanceInstructionDependent() ? synchronousComponents : components.values();
		for(Component c: comps) {
			if(!isPerformanceInstructionDependent() || ((Synchronous)c).isWritingState()) {
				for(Input in: c.getInputs()) {
					if(in.isConnected()) {
						if(in.getAccumulatedLatency() > maxLatency) {
							maxIns.clear();
							maxIns.add(in);
							maxLatency = in.getAccumulatedLatency();
						}
						else if(in.getAccumulatedLatency() == maxLatency)
							maxIns.add(in);
					}
				}
			}
		}
		if(maxIns.isEmpty() && instructionDependent) // no inputs for instruction? Fallback to use all inputs
			return findHighetsAccumulatedLatencyInputs(false);
		else
			return maxIns;
	}

	/**
	 * Determines the clock period and frequency, setting the respective variables.
	 */
	private void determineClockPeriodAndFrequency() {
		clockPeriod = findHighestAccumulatedLatency();
		if(clockPeriod > 0)
			clockFrequency = 1.0 / (clockPeriod * Math.pow(10, LATENCY_EXPONENT));
		else
			clockFrequency = 0;
	}

	/**
	 * Returns the clock period in the LATENCY_UNIT unit.
	 * @return Clock period of the CPU.
	 */
	public int getClockPeriod() {
		return clockPeriod;
	}

	/**
	 * Returns the clock frequency in Hz.
	 * @return Clock frequency in Hz.
	 */
	public double getClockFrequencyInHz() {
		return clockFrequency;
	}

	/**
	 * Returns the clock frequency in MHz.
	 * @return Clock frequency in MHz.
	 */
	public double getClockFrequencyInMHz() {
		return clockFrequency / Math.pow(10, 6);
	}

	/**
	 * Returns the clock frequency in GHz.
	 * @return Clock frequency in GHz.
	 */
	public double getClockFrequencyInGHz() {
		return clockFrequency / Math.pow(10, 9);
	}

	/**
	 * Returns the clock frequency as string in an adequate unit.
	 * @return Clock frequency as string with adequate unit.
	 */
	public String getClockFrequencyInAdequateUnit() {
		double mhz, ghz;
		if((ghz = getClockFrequencyInGHz()) >= 1.0)
			return String.format("%.2f", ghz) + " GHz";
		else if((mhz = getClockFrequencyInMHz()) >= 1.0)
			return String.format("%.2f", mhz) + " MHz";
		else
			return String.format("%.2f", getClockFrequencyInHz()) + " Hz";
	}

	/**
	 * Returns the number of executed clock cycles.
	 * @return Number of executed cycles.
	 */
	public int getNumberOfExecutedCycles() {
		return executedCycles;
	}

	/**
	 * Returns the number of executed instructions.
	 * @return Number of executed instructions.
	 */
	public int getNumberOfExecutedInstructions() {
		return executedInstructions;
	}

	/**
	 * Returns the CPI.
	 * @return Cycles Per Instruction.
	 */
	public double getCPI() {
		if(getNumberOfExecutedInstructions() > 0)
			return (double)getNumberOfExecutedCycles() / (double)getNumberOfExecutedInstructions();
		else
			return 0.0;
	}

	/**
	 * Returns the CPI as a formatted string.
	 * @return CPI as string.
	 */
	public String getCPIAsString() {
		return String.format("%.2f", getCPI());
	}

	/**
	 * Returns the amount of time spent executing the program.
	 * @return Execution time (in LATENCY_UNIT unit).
	 */
	public long getExecutionTime() {
		return (long)getNumberOfExecutedCycles() * (long)getClockPeriod();
	}

	/**
	 * Returns the number of forwards.
	 * @return Number of forwards.
	 */
	public int getNumberOfForwards() {
		return forwards;
	}

	/**
	 * Returns the number of stalls.
	 * @return Number of stalls.
	 */
	public int getNumberOfStalls() {
		return stalls;
	}

	/**
	 * Returns whether the latencies and critical path depend on the current instruction.
	 * @return <tt>true</tt> if the performance depends on the current instruction.
	 */
	public boolean isPerformanceInstructionDependent() {
		return performanceInstructionDependent;
	}

	/**
	 * Resets the statistics to zero.
	 */
	protected void resetStatistics() {
		executedCycles = 0;
		executedInstructions = 0;
		forwards = 0;
		stalls = 0;
	}

	/**
	 * Determines the CPU's critical path
	 */
	private void determineCriticalPath() {
		List<Input> maxIns = findHighetsAccumulatedLatencyInputs(isPerformanceInstructionDependent());

		if(!maxIns.isEmpty()) {
			for(Input in: maxIns) {
				in.getConnectedOutput().setInCriticalPath(true);
				determineCriticalPath(in.getConnectedOutput().getComponent());
			}
		}
	}

	/**
	 * Determines the critical path up to the specified component (recursively).
	 * @param c The component.
	 */
	private void determineCriticalPath(Component component) {
		int lat = component.getAccumulatedLatency() - component.getLatency();
		for(Input i: component.getInputs()) {
			if(i.canChangeComponentAccumulatedLatency() && i.getAccumulatedLatency() == lat
				&& i.isConnected() && !i.getConnectedOutput().isInCriticalPath()) {
				i.getConnectedOutput().setInCriticalPath(true);
				determineCriticalPath(i.getConnectedOutput().getComponent());
			}
		}
	}

	/**
	 * Sets whether the latencies and critical path should depend on the current instruction.
	 * <p><b>This method should be called after loading a CPU.</b></p>
	 * @param instructionDependent Whether the performance should depend on the current instruction.
	 */
	public void setPerformanceInstructionDependent(boolean instructionDependent) {
		if(performanceInstructionDependent != instructionDependent) {
			performanceInstructionDependent = instructionDependent;
			calculateAccumulatedLatencies(performanceInstructionDependent);
			determineCriticalPath();
		}
	}

	/**
	 * Updates the list of components and wires that are in the control path.
	 */
	public final void determineControlPath() {
		controlUnit.setInControlPath();
		if(alu != null) alu.getZero().setInControlPath();
		if(aluControl != null) aluControl.setInControlPath();
		if(forwardingUnit != null) forwardingUnit.setInControlPath();
		if(hazardDetectionUnit != null) hazardDetectionUnit.setInControlPath();
	}

	/**
	 * Returns whether the CPU is pipelined.
	 * @return <tt>True</tt> if the CPU is pipelined.
	 */
	public boolean isPipeline() {
		return ifIdReg != null;
	}

	/**
	 * Sets the file of the CPU.
	 * @param file The file.
	 */
	public final void setFile(File file) {
		this.file = file;
	}

	/**
	 * Returns the file of the CPU.
	 * @return The file of the CPU.
	 */
	public final File getFile() {
		return file;
	}

	/**
	 * Returns the graphical size of the CPU.
	 * <p>The size is calculated here, so avoid calling this method repeteadly!</p>
	 * @return Size of the graphical CPU.
	 */
	public Dimension getSize() {
		int x, y, width, height;
		width = height = 0;

		for(Component c: getComponents()) { // check each component's position + size and output wires points
			// Component's position + size
			x = c.getPosition().x + c.getSize().width;
			y = c.getPosition().y + c.getSize().height;
			if(x > width) width = x;
			if(y > height) height = y;

			// Component's outputs points
			for(Output o: c.getOutputs()) {
				if(o.isConnected()) {
					o.getPosition();
					if(o.getPosition() != null) { // start point
						x = o.getPosition().x;
						y = o.getPosition().y;
						if(x > width) width = x;
						if(y > height) height = y;
					}
					if(o.getConnectedInput().getPosition() != null) { // end point
						x = o.getConnectedInput().getPosition().x;
						y = o.getConnectedInput().getPosition().y;
						if(x > width) width = x;
						if(y > height) height = y;
					}
					for(Point p: o.getIntermediatePoints()) { // intermediate points
						if(p.x > width) width = p.x;
						if(p.y > height) height = p.y;
					}
				}
			}
		}

		return new Dimension(width + RIGHT_MARGIN, height + BOTTOM_MARGIN);
	}

	/**
	 * Assembles the given code and updates the CPU's instruction and data memory-
	 * @param code The code to assemble.
	 * @throws SyntaxErrorException If the code has a syntax error.
	 */
	public void assembleCode(String code) throws SyntaxErrorException {
		getAssembler().assembleCode(code);
	}

	/**
	 * Loads the given assembled instructions into the instruction memory and
	 * starts the simulation.
	 * @param instructions Program's list of assembled instructions.
	 */
	protected void loadProgram(List<AssembledInstruction> instructions) {
		getInstructionMemory().setInstructions(instructions); // load instructions to memory
		clearPreviousCycles(); // clear all components' saved states
		setPCAddress(0); // reset PC
		if(isPipeline()) { // clears the current instruction index in the pipeline registers
			getIfIdReg().setCurrentInstructionIndex(-1);
			getIdExReg().setCurrentInstructionIndex(-1);
			getExMemReg().setCurrentInstructionIndex(-1);
			getMemWbReg().setCurrentInstructionIndex(-1);
		}
		resetStatistics();

		calculateInstructionPerformance(); // Refresh critical path
	}

	/**
	 * Returns whether the currently loaded program has finished executing.
	 * @return <tt>True</tt> it the program has finished.
	 */
	public boolean isProgramFinished() {
		if(isPipeline())
			return pc.getCurrentInstructionIndex() == -1 && ifIdReg.getCurrentInstructionIndex() == -1
				&& idExReg.getCurrentInstructionIndex() == -1 && exMemReg.getCurrentInstructionIndex() == -1
				&& memWbReg.getCurrentInstructionIndex() == -1;
		else
			return pc.getCurrentInstructionIndex() == -1;
	}

	/**
	 * Executes the currently loaded program until the end.
	 * Or until we hit the breakpoint
	 * @throws InfiniteLoopException If the <tt>EXECUTE_ALL_LIMIT_CYCLES</tt> limit has been reached (possible infinite loop).
	 */
	public void executeAll() throws InfiniteLoopException {
		int cycles = 0;
		while(!isProgramFinished()) {
			if(cycles++ > EXECUTE_ALL_LIMIT_CYCLES) // prevent possible infinite cycles
				throw new InfiniteLoopException();
			executeCycle();

			// check if we have hit the breakpoint
			if (getPC().getAddress().getValue() == breakpointAddr)
			{
				break;
			}
		}
	}

	/**
	 * Sets the breakpoint address.
	 */
	public void setBreakpointAddr(int addr) {
		breakpointAddr = addr;
	}

	/**
	 * "Executes" a clock cycle (a step).
	 */
	public void executeCycle() {
		executedCycles++;
		if(!isPipeline() || memWbReg.getCurrentInstructionIndex() >= 0)
			executedInstructions++;
		if(hasForwardingUnit()) {
			if(getForwardingUnit().getForwardA().getValue() != 0) forwards++;
			if(getForwardingUnit().getForwardB().getValue() != 0) forwards++;
		}
		if(hasHazardDetectionUnit() && getHazardDetectionUnit().getStall().getValue() != 0)
			stalls++;

		saveCycleState();
		for(Component c: synchronousComponents) // execute synchronous actions without propagating output changes
			((Synchronous)c).executeSynchronous();

		// Store index(es) of the instruction(s) being executed
		int index = getPC().getAddress().getValue() / (Data.DATA_SIZE / 8);
		if(index < 0 || index >= getInstructionMemory().getNumberOfInstructions())
			index = -1;
		if(isPipeline()) { // save other instructions in pipeline
			updatePipelineRegisterCurrentInstruction(memWbReg, exMemReg.getCurrentInstructionIndex());
			updatePipelineRegisterCurrentInstruction(exMemReg, idExReg.getCurrentInstructionIndex());
			updatePipelineRegisterCurrentInstruction(idExReg, ifIdReg.getCurrentInstructionIndex());
			updatePipelineRegisterCurrentInstruction(ifIdReg, pc.getCurrentInstructionIndex());
		}
		getPC().setCurrentInstructionIndex(index);

		for(Component c: synchronousComponents) // execute normal actions, propagating output changes
			c.execute();
		for(Component c: getComponents()) // "execute" all components, just to be safe
			c.execute();

		calculateInstructionPerformance(); // Refresh critical path
	}

	/**
	 * Updates the current instruction index stored in the specified pipeline register.
	 * @param reg The pipeline register to update.
	 * @param previousIndex The index of the instruction in the previous stage.
	 */
	private void updatePipelineRegisterCurrentInstruction(PipelineRegister reg, int previousIndex) {
		if(reg.getFlush().getValue() == 1)
				reg.setCurrentInstructionIndex(-1);
			else if(reg.getWrite().getValue() == 1)
				reg.setCurrentInstructionIndex(previousIndex);
	}

	/**
	 * Updates the program counter to a new address.
	 * <p>This method also changes the current instruction index to a correct value,
	 * so call this method instead of <tt>getPC().setAddress()</tt>!</p>
	 * @param address The new address.
	 */
	public void setPCAddress(int address) {
		getPC().setAddress(address); // reset PC
		int index = address / (Data.DATA_SIZE / 8);
		if(index >= 0 && index < getInstructionMemory().getNumberOfInstructions())
			getPC().setCurrentInstructionIndex(index);
		else
			getPC().setCurrentInstructionIndex(-1);
	}

	/**
	 * Saves the state of the current cycle.
	 */
	public void saveCycleState() {
		for(Component c: synchronousComponents)
			((Synchronous)c).pushState();
	}

	/**
	 * Performs a "step back" in the execution if possible (if <tt>hasPreviousCycle() == true</tt>).
	 */
	public void restorePreviousCycle() {
		if(hasPreviousCycle()) {
			for(Component c: synchronousComponents) // restore previous states
				((Synchronous)c).popState();
			for(Component c: synchronousComponents) // execute normal actions, propagating output changes
				c.execute();
			for(Component c: getComponents()) // "execute" all components
				c.execute();

			executedCycles--;
			if(!isPipeline() || memWbReg.getCurrentInstructionIndex() >= 0)
				executedInstructions--;
			if(hasForwardingUnit()) {
				if(getForwardingUnit().getForwardA().getValue() != 0) forwards--;
				if(getForwardingUnit().getForwardB().getValue() != 0) forwards--;
			}
			if(hasHazardDetectionUnit() && getHazardDetectionUnit().getStall().getValue() != 0)
				stalls--;

			calculateInstructionPerformance(); // Refresh critical path
		}
	}

	/**
	 * Returns whether there was a previous cycle executed.
	 * @return <tt>True</tt> if a "step back" is possible (<tt>getPc().hasSavedStates() == true</tt>).
	 */
	public boolean hasPreviousCycle() {
		if(pc != null)
			return pc.hasSavedStates();
		else
			return false;
	}

	/**
	 * Removes all the saved previous cycles.
	 */
	public void clearPreviousCycles() {
		for(Component c: synchronousComponents)
			((Synchronous)c).clearSavedStates();
	}

	/**
	 * Resets the states of the CPU's components to the first cycle.
	 */
	public void resetToFirstCycle() {
		if(hasPreviousCycle()) {
			for(Component c: synchronousComponents) // restore first state
				((Synchronous)c).resetFirstState();
			for(Component c: synchronousComponents) // execute normal actions, propagating output changes
				c.execute();
			for(Component c: getComponents()) // "execute" all components
				c.execute();
			resetStatistics();

			calculateInstructionPerformance(); // Refresh critical path
		}
	}

	/**
	 * Resets the stored data of the CPU to zeros (register bank and data memory).
	 */
	public void resetData() {
		regbank.reset();
		if(hasDataMemory()) dataMemory.reset();
		if(hasALU() && alu instanceof ExtendedALU) ((ExtendedALU)alu).reset();
	}

	/**
	 * Connects the given output to the given input.
	 * @param output Output to connect from.
	 * @param input Input to connect to.
	 * @return The resulting output.
	 * @throws InvalidCPUException If the output or the input are already connected or have different sizes.
	 */
	protected Output connectComponents(Output output, Input input) throws InvalidCPUException {
		output.connectTo(input);
		return output;
	}

	/**
	 * Connects the given output to the given input.
	 * @param outCompId The identifier of the output component.
	 * @param outId The identifier of the output of the output component.
	 * @param inCompId The identifier of the input component.
	 * @param inId The identifier of the input of the input component.
	 * @return The resulting output.
	 * @throws InvalidCPUException If the output or the input are already connected or have different sizes or don't exist.
	 */
	protected Output connectComponents(String outCompId, String outId, String inCompId, String inId) throws InvalidCPUException {
		Component out = getComponent(outCompId);
		Component in = getComponent(inCompId);
		if(out == null) throw new InvalidCPUException("Unknown ID " + outCompId + "!");
		if(in == null) throw new InvalidCPUException("Unknown ID " + inCompId + "!");

		Output o = out.getOutput(outId);
		Input i = in.getInput(inId);
		if(o == null) throw new InvalidCPUException("Unknown ID " + outId + "!");
		if(i == null) throw new InvalidCPUException("Unknown ID " + inId + "!");

		o.connectTo(i);
		return o;
	}

	/**
	 * Returns whether a component with the specified identifier exists.
	 * @param id Component identifier.
	 * @return <tt>true</tt> if the component exists.
	 */
	public final boolean hasComponent(String id) {
		return components.containsKey(id);
	}

	/**
	 * Returns the component with the specified identifier.
	 * @param id Component identifier.
	 * @return The desired component, or <tt>null</tt> if it doesn't exist.
	 */
	public final Component getComponent(String id) {
		return components.get(id);
	}

	/**
	 * Returns an array with all the coponents.
	 * @return Array with all components.
	 */
	public Component[] getComponents() {
		Component[] c = new Component[components.size()];
		return components.values().toArray(c);
	}

	/**
	 * Adds the specified component to the CPU.
	 * @param component The component to add.
	 * @throws InvalidCPUException If the new component makes the CPU invalid.
	 */
	protected final void addComponent(Component component) throws InvalidCPUException {
		if(hasComponent(component.getId())) throw new InvalidCPUException("Duplicated ID " + component.getId() + "!");
		components.put(component.getId(), component);
		if(component instanceof Synchronous)
			synchronousComponents.add(component);
		if(component instanceof PC) {
			if(pc != null) throw new InvalidCPUException("Only one program counter allowed!");
			pc = (PC)component;
		}
		else if(component instanceof RegBank) {
			if(regbank != null) throw new InvalidCPUException("Only one register bank allowed!");
			regbank = (RegBank)component;
		}
		else if(component instanceof InstructionMemory) {
			if(instructionMemory != null) throw new InvalidCPUException("Only one instruction memory allowed!");
			instructionMemory = (InstructionMemory)component;
		}
		else if(component instanceof ControlUnit) {
			if(controlUnit != null) throw new InvalidCPUException("Only one control unit allowed!");
			controlUnit = (ControlUnit)component;
		}
		else if(component instanceof ALUControl) {
			if(aluControl != null) throw new InvalidCPUException("Only one ALU control allowed!");
			aluControl = (ALUControl)component;
		}
		else if(component instanceof ALU) {
			if(alu != null) throw new InvalidCPUException("Only one ALU allowed!");
			alu = (ALU)component;
		}
		else if(component instanceof DataMemory) {
			if(dataMemory != null) throw new InvalidCPUException("Only one data memory allowed!");
			dataMemory = (DataMemory)component;
		}
		else if(component instanceof ForwardingUnit) {
			if(forwardingUnit != null) throw new InvalidCPUException("Only one forwarding unit allowed!");
			forwardingUnit = (ForwardingUnit)component;
		}
		else if(component instanceof HazardDetectionUnit) {
			if(hazardDetectionUnit != null) throw new InvalidCPUException("Only one hazard detection unit allowed!");
			hazardDetectionUnit = (HazardDetectionUnit)component;
		}
		else if(component instanceof PipelineRegister) {
			String id = component.getId().trim().toUpperCase();
			switch (id) {
				case "IF/ID":
					if(ifIdReg != null) throw new InvalidCPUException("Only one IF/ID pipeline register allowed!");
					ifIdReg = (PipelineRegister)component;
					break;
				case "ID/EX":
					if(idExReg != null) throw new InvalidCPUException("Only one ID/EX pipeline register allowed!");
					idExReg = (PipelineRegister)component;
					break;
				case "EX/MEM":
					if(exMemReg != null) throw new InvalidCPUException("Only one EX/MEM pipeline register allowed!");
					exMemReg = (PipelineRegister)component;
					break;
				case "MEM/WB":
					if(memWbReg != null) throw new InvalidCPUException("Only one MEM/WB pipeline register allowed!");
					memWbReg = (PipelineRegister)component;
					break;
				default:
					throw new InvalidCPUException("A pipeline register's identifier must be one of {IF/ID, ID/EX, EX/MEM, MEM/WB}!");
			}
		}
	}

	/**
	 * Returns the loaded instruction set.
	 * @return Loaded instruction set.
	 */
	public final InstructionSet getInstructionSet() {
		return instructionSet;
	}

	/**
	 * Returns the Program Counter.
	 * @return Program Counter.
	 */
	public final PC getPC() {
		return pc;
	}

	/**
	 * Returns the register bank.
	 * @return Register bank.
	 */
	public final RegBank getRegBank() {
		return regbank;
	}

	/**
	 * Returns the instruction memory.
	 * @return Instruction memory.
	 */
	public final InstructionMemory getInstructionMemory() {
		return instructionMemory;
	}

	/**
	 * Returns the control unit.
	 * @return Control unit.
	 */
	public final ControlUnit getControlUnit() {
		return controlUnit;
	}

	/**
	 * Returns the ALU control.
	 * @return ALU control.
	 */
	public final ALUControl getALUControl() {
		return aluControl;
	}

	/**
	 * Returns whether the CPU contains an ALU control.
	 * @return <tt>True</tt> if an ALU control exists.
	 */
	public final boolean hasALUControl() {
		return aluControl != null;
	}

	/**
	 * Returns the ALU.
	 * @return ALu.
	 */
	public final ALU getALU() {
		return alu;
	}

	/**
	 * Returns whether the CPU contains an ALU.
	 * @return <tt>True</tt> if an ALU exists.
	 */
	public final boolean hasALU() {
		return alu != null;
	}

	/**
	 * Returns the data memory.
	 * @return Data memory.
	 */
	public final DataMemory getDataMemory() {
		return dataMemory;
	}

	/**
	 * Returns whether the CPU contains data memory.
	 * @return <tt>True</tt> if a data memory exists.
	 */
	public final boolean hasDataMemory() {
		return dataMemory != null;
	}

	/**
	 * Returns the forwarding unit.
	 * @return Forwarding unit.
	 */
	public final ForwardingUnit getForwardingUnit() {
		return forwardingUnit;
	}

	/**
	 * Returns whether the CPU contains a forwarding unit.
	 * @return <tt>True</tt> if a forwarding unit exists.
	 */
	public final boolean hasForwardingUnit() {
		return forwardingUnit != null;
	}

	/**
	 * Returns the hazard detection unit.
	 * @return Hazard detection unit.
	 */
	public final HazardDetectionUnit getHazardDetectionUnit() {
		return hazardDetectionUnit;
	}

	/**
	 * Returns whether the CPU contains a hazard detection unit.
	 * @return <tt>True</tt> if a hazard detection unit exists.
	 */
	public final boolean hasHazardDetectionUnit() {
		return hazardDetectionUnit != null;
	}

	/**
	 * Returns the IF/ID pipeline register.
	 * @return IF/ID pipeline register, or <tt>null</tt> if not pipeline.
	 */
	public final PipelineRegister getIfIdReg() {
		return ifIdReg;
	}

	/**
	 * Returns the ID/EX pipeline register.
	 * @return ID/EX pipeline register, or <tt>null</tt> if not pipeline.
	 */
	public final PipelineRegister getIdExReg() {
		return idExReg;
	}

	/**
	 * Returns the EX/MEM pipeline register.
	 * @return EX/MEM pipeline register, or <tt>null</tt> if not pipeline.
	 */
	public final PipelineRegister getExMemReg() {
		return exMemReg;
	}

	/**
	 * Returns the MEM/WB pipeline register.
	 * @return MEM/WB pipeline register, or <tt>null</tt> if not pipeline.
	 */
	public final PipelineRegister getMemWbReg() {
		return memWbReg;
	}

	/**
	 * Returns the index/address of the register with the specified name.
	 * @param name Name of the register (with prefix).
	 * @return The index of the register, or -1 if it doesn't exist.
	 */
	public int getRegisterIndex(String name) {
		name = name.trim().toLowerCase();
		if(name.length() < 2 || name.charAt(0) != REGISTER_PREFIX)
			return -1;
		name = name.substring(1);
		try {
			int index = Integer.parseInt(name);
			// Numeric name (like $0)
			if(index >= 0 && index < regbank.getNumberOfRegisters())
				return index;
			else
				return -1;
		}
		catch(NumberFormatException e) {
			// Register name (like $zero)
			if(registerNames != null)
				return registerNames.indexOf(name);
			else
				return -1;
		}
	}

	/**
	 * Returns the name of the register with the specified index/address.
	 * @param index The index of the register.
	 * @return The name of the register (with prefix).
	 * @throws IndexOutOfBoundsException If the register with the given index doesn't exist.
	 */
	public String getRegisterName(int index) throws IndexOutOfBoundsException {
		if(registerNames != null)
			return REGISTER_PREFIX + registerNames.get(index);
		else
			return REGISTER_PREFIX + "" + index;
	}

	/**
	 * Returns whether a register with the given name exists.
	 * @param name Name of the register (with prefix).
	 * @return <tt>true</tt> if the register exists.
	 */
	public boolean hasRegister(String name) {
		return getRegisterIndex(name) != -1;
	}

	/**
	 * Returns the assembler for this CPU.
	 * @return The assembler for this CPU.
	 */
	public Assembler getAssembler() {
		return assembler;
	}

	/**
	 * Parses and creates the components from the given JSON array.
	 * @param cpu The CPU to add the components to.
	 * @param components JSONObject that contains the components array.
	 * @param parentPath Path to the cpu file's parent directory.
	 * @throws JSONException If the JSON file is malformed.
	 * @throws InvalidCPUException If the CPU is invalid or incomplete.
	 */
	private static void parseJSONComponents(CPU cpu, JSONObject components, String parentPath) throws JSONException, InvalidCPUException {
		JSONObject json;
		String type, id;
		Class cl;
		Component comp;

		// ClassLoader to load the built-in components
		ClassLoader loader = CPU.class.getClassLoader();

		// ClassLoader to load custom components
		File parentDir = new File(parentPath + File.separator);
		ClassLoader customLoader;
		try {
			URL[] urls = new URL[] {parentDir.toURI().toURL()};
			customLoader = new URLClassLoader(urls);
		} catch(Exception ex) {
			customLoader = null;
		}

		// Parse the components
		Iterator<String> i = components.keys();
		while(i.hasNext()) {
			id = i.next();
			json = components.getJSONObject(id);
			type = json.getString("type");

			// Load the class with the name specified by "type"
			try {
				// Search in the built-in classes first
				cl = loader.loadClass("brunonova.drmips.simulator.components." + type);
			} catch(ClassNotFoundException ex) {
				// Search in the custom components second
				if(customLoader != null) {
					try {
						cl = customLoader.loadClass(type);
					} catch(ClassNotFoundException ex2) {
						ex2.initCause(ex);
						throw new InvalidCPUException("Unknown component type " + type + "!", ex2);
					}
				} else {
					throw new InvalidCPUException("Unknown component type " + type + "!", ex);
				}
			}

			// Create the component with the (String, JSONObject) contructor
			// and add it to the CPU
			try {
				comp = (Component)cl.asSubclass(Component.class)
				                    .getConstructor(String.class, JSONObject.class)
				                    .newInstance(id, json);
				cpu.addComponent(comp);
			} catch(ClassCastException ex) {
				throw new InvalidCPUException("The " + type + " class is not a subclass of Component!", ex);
			} catch(NoSuchMethodException ex) {
				throw new InvalidCPUException("The " + type + " class is missing the (String, JSONObject) constructor!", ex);
			} catch(InvocationTargetException ex) {
				Throwable target = ex.getCause();
				if(target instanceof InvalidCPUException) {
					throw (InvalidCPUException)target;
				} else if(target instanceof JSONException) {
					throw (JSONException)target;
				} else {
					throw new InvalidCPUException("Failed to create the component " + id + "!", ex);
				}
			} catch(InstantiationException | IllegalAccessException | IllegalArgumentException ex) {
				throw new InvalidCPUException("Failed to create the component " + id + "!", ex);
			}
		}
	}

	/**
	 * Parses wires from the given JSON array and connects the components.
	 * @param cpu The CPU to add wires to.
	 * @param wires JSONArray that contains the wires array.
	 * @throws JSONException If the JSON file is malformed.
	 * @throws InvalidCPUException If the output or the input are already connected or have different sizes or doesn't exist.
	 */
	private static void parseJSONWires(CPU cpu, JSONArray wires) throws JSONException, InvalidCPUException {
		JSONObject wire, point, start, end;
		JSONArray points;
		Output out;

		for(int i = 0; i < wires.length(); i++) {
			wire = wires.getJSONObject(i);
			out = cpu.connectComponents(wire.getString("from"), wire.getString("out"),
				wire.getString("to"), wire.getString("in"));
			points = wire.optJSONArray("points");
			if(points != null) {
				for(int x = 0; x < points.length(); x++) {
					point = points.getJSONObject(x);
					out.addIntermediatePoint(new Point(point.getInt("x"), point.getInt("y")));
				}
			}
			if((start = wire.optJSONObject("start")) != null)
				out.setPosition(new Point(start.getInt("x"), start.getInt("y")));
			if((end = wire.optJSONObject("end")) != null)
				out.getConnectedInput().setPosition(new Point(end.getInt("x"), end.getInt("y")));
		}
	}

	/**
	 * Parses and sets the identifiers of the registers.
	 * @param cpu The CPU to set the registers informations.
	 * @param regs JSONArray that contains the registers.
	 * @throws JSONException If the JSON file is malformed.
	 * @throws InvalidCPUException If not all registers are specified or a register is invalid.
	 */
	private static void parseJSONRegNames(CPU cpu, JSONArray regs) throws JSONException, InvalidCPUException {
		if(regs.length() != cpu.getRegBank().getNumberOfRegisters())
			throw new InvalidCPUException("Not all registers have been specified in the registers block!");
		cpu.registerNames = new ArrayList<>(cpu.getRegBank().getNumberOfRegisters());
		String id;

		for(int i = 0; i < regs.length(); i++) {
			id = regs.getString(i).trim().toLowerCase();

			if(id.isEmpty())
				throw new InvalidCPUException("Invalid name " + id + "!");
			if(!id.matches(REGNAME_REGEX)) // has only letters and digits and starts with a letter?
				throw new InvalidCPUException("Invalid name " + id + "!");
			if(cpu.hasRegister(REGISTER_PREFIX + id))
				throw new InvalidCPUException("Invalid name " + id + "!");

			cpu.registerNames.add(id);
		}
	}
}
