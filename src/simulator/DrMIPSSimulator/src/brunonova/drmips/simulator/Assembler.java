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

import brunonova.drmips.simulator.exceptions.SyntaxErrorException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The class that assembles code into MIPS assembled instructions and loads data values from the <tt>.data</tt> section.
 * 
 * @author Bruno Nova
 */
public class Assembler {
	/** The character used to indicate comments in the MIPS code. */
	public static final char COMMENT_CHAR = '#';
	/** The regular expression to validate labels. */
	public static final String LABEL_REGEX = "^[a-zA-z][a-zA-Z0-9_]*$";
	/** The possible segment types. */
	private enum Segment {TEXT, DATA}
	
	/** The CPU this assembler is assembling to. */
	private final CPU cpu;
	/** The text segment labels in the code and their lines. */
	private Map<String, Integer> textLabels;
	/** The data segment labels in the code and their addresses. */
	private Map<String, Integer> dataLabels;
	
	/**
	 * Creates the assembler.
	 * @param cpu The CPU this assembler is assembling to.
	 */
	public Assembler(CPU cpu) {
		this.cpu = cpu;
	}
	
	/**
	 * Assembles the given code and updates the CPU's instruction and data memory-
	 * @param code The code to assemble.
	 * @throws SyntaxErrorException If the code has a syntax error.
	 */
	protected void assembleCode(String code) throws SyntaxErrorException {
		String[] codeLines = code.split("\n");
		List<CodeLine> lines = new ArrayList<>();
		List<AssembledInstruction> instructions = new ArrayList<>();
		int index, lineNumber, currentDataAddress = 0;
		String codeLine, label, mnemonic, type;
		String[] split, args, values;
		List<String> interpretedLines;
		textLabels = new TreeMap<>();
		dataLabels = new TreeMap<>();
		Segment currentSegment = Segment.TEXT;
		List<SyntaxErrorException> errors = new LinkedList<>();
		
		// Parse each line
		for(int i = 0; i < codeLines.length; i++) {
			try {
				index = lines.size();
				lineNumber = i + 1;
				codeLine = codeLines[i].split("" + COMMENT_CHAR, 2)[0].trim(); // remove comment, if any

				if(codeLine.equals(".text")) // change to text segment
					currentSegment = Segment.TEXT;
				else if(codeLine.equals(".data")) { // change to data segment
					if(!cpu.hasDataMemory())
						throw new SyntaxErrorException(SyntaxErrorException.Type.DATA_SEGMENT_WITHOUT_DATA_MEMORY, lineNumber);
					currentSegment = Segment.DATA;
				}
				else if(currentSegment == Segment.DATA) { // line in data segment (load data)
					if(codeLine.contains(":")) { // check if the line has a label
						split = codeLine.split(":", 2);
						label = split[0].trim();
						codeLine = split[1].trim();
						if(!label.matches(LABEL_REGEX))
							throw new SyntaxErrorException(SyntaxErrorException.Type.INVALID_LABEL, lineNumber, label);
						if(textLabels.containsKey(label) || dataLabels.containsKey(label))
							throw new SyntaxErrorException(SyntaxErrorException.Type.DUPLICATED_LABEL, lineNumber, label);
						dataLabels.put(label, currentDataAddress);
					}

					if(!codeLine.isEmpty()) { // load values, if any
						split = codeLine.split("\\s+", 2); // split data type (directive) and values (\\s+ matches whitespace characters)
						type = split[0].trim().toLowerCase();
						values = (split.length == 2) ? split[1].trim().split(",") : new String[0]; // split values, if any
						switch (type) {
							case ".word":
								for(String value: values) {
									currentDataAddress = alignAddressToWord(currentDataAddress);
									cpu.getDataMemory().setData(currentDataAddress, parseIntArg(value.trim(), lineNumber));
									currentDataAddress += 4;
								}	break;
							case ".space":
								if(values.length != 1)
									throw new SyntaxErrorException(SyntaxErrorException.Type.WRONG_NUMBER_OF_ARGUMENTS, lineNumber, "" + 1, "" + values.length);
								else {
									int arg = parseIntArg(values[0].trim(), lineNumber);
									if(arg < 0) throw new SyntaxErrorException(SyntaxErrorException.Type.INVALID_POSITIVE_INT_ARG, lineNumber, values[0].trim());
									currentDataAddress += arg;
								}	break;
							default:
								throw new SyntaxErrorException(SyntaxErrorException.Type.UNKNOWN_DATA_DIRECTIVE, lineNumber, type);
						}
					}
				}
				else { // line in text segment (replace pseudo-instructions and find labels' line numbers)
					if(codeLine.contains(":")) { // check if the line has a label
						split = codeLine.split(":", 2);
						label = split[0].trim();
						codeLine = split[1].trim();
						if(!label.matches(LABEL_REGEX))
							throw new SyntaxErrorException(SyntaxErrorException.Type.INVALID_LABEL, lineNumber, label);
						if(textLabels.containsKey(label) || dataLabels.containsKey(label))
							throw new SyntaxErrorException(SyntaxErrorException.Type.DUPLICATED_LABEL, lineNumber, label);
						textLabels.put(label, index);
					}

					if(!codeLine.isEmpty()) {
						split = codeLine.split("\\s+", 2); // split mnemonic and args
						mnemonic = split[0].trim();
						args = (split.length == 2) ? split[1].trim().split(",") : new String[0]; // split args, if any

						if(cpu.getInstructionSet().hasPseudoInstruction(mnemonic)) { // pseudo-instruction
							interpretedLines = interpretPseudoInstruction(mnemonic, args, lineNumber);
							if(!interpretedLines.isEmpty()) {
								interpretedLines.set(0, interpretedLines.get(0) + "  " + COMMENT_CHAR + " " + codeLine);
								for(String line: interpretedLines)
									lines.add(new CodeLine(line, lineNumber));
							}
						}
						else
							lines.add(new CodeLine(codeLine, lineNumber));
					}
				}
			}
			catch(SyntaxErrorException ex) {
				errors.add(ex);
			}
		}
		
		// Assemble the instructions
		for(int i = 0; i < lines.size(); i++) {
			try {
				instructions.add(assembleInstruction(lines.get(i).line, i, lines.get(i).number));
			}
			catch(SyntaxErrorException ex) {
				errors.add(ex);
			}
		}
		
		// Add the labels to the instructions
		for(Map.Entry<String, Integer> e: textLabels.entrySet()) {
			if(e.getValue() >= 0 && e.getValue() < instructions.size())
				instructions.get(e.getValue()).addLabel(e.getKey());
		}
		
		if(!errors.isEmpty()) {
			SyntaxErrorException first = errors.get(0);
			first.setOtherErrors(errors);
			throw first;
		}
		
		cpu.loadProgram(instructions);
	}
	
	/**
	 * Returns the intructions that the given pseudo instruction.
	 * <p>Labels are ignored in this method.</p>
	 * @param line The line of code with the pseudo-instruction to interpret.
	 * @return The resulting lines of code, or an empty list if something is wrong.
	 */
	public List<String> interpretPseudoInstruction(String line) {
		String[] split;
		
		line = line.split("" + COMMENT_CHAR, 2)[0].trim(); // remove comment, if any
		if(line.contains(":")) line = line.split(":")[1].trim(); // remove label, if any
		split = line.split("\\s+", 2); // split mnemonic and args
		String mnemonic = split[0].trim();
		String[] args = (split.length == 2) ? split[1].trim().split(",") : new String[0]; // split args, if any
		
		try {
			return interpretPseudoInstruction(mnemonic, args, 1);
		}
		catch(Exception e) {
			return new LinkedList<>();
		}
	}
	
	/**
	 * Returns all the valid labels that are in the code.
	 * @param code The code where to search.
	 * @return All valid labels.
	 */
	public Set<String> getCodeLabels(String code) {
		Set<String> labels = new TreeSet<>();
		String[] lines = code.split("\n");
		String label;
		int i;
		
		for(String line: lines) {
			i = line.indexOf(':');
			if(i != -1 && line.lastIndexOf(COMMENT_CHAR, i - 1) == -1) {
				label = line.substring(0, i).trim();
				if(label.matches(LABEL_REGEX) && !cpu.getInstructionSet().hasInstructionOrPseudoInstruction(label))
					labels.add(label);
			}
		}
		
		return labels;
	}
	
	/**
	 * Interprets a pseudo-instruction into instructions.
	 * @param mnemonic The mnemonic of the pseudo-instruction.
	 * @param args The arguments of the pseudo-instruction.
	 * @param lineNumber The number of the line of code.
	 * @return The replacing instructions.
	 */
	private List<String> interpretPseudoInstruction(String mnemonic, String[] args, int lineNumber) throws SyntaxErrorException {
		List<String> instructions = new ArrayList<>();
		PseudoInstruction pseudo = cpu.getInstructionSet().getPseudoInstruction(mnemonic);
		String[] split;
		String m;
		
		if(pseudo.getNumberOfArguments() != args.length) 
			throw new SyntaxErrorException(SyntaxErrorException.Type.WRONG_NUMBER_OF_ARGUMENTS, lineNumber, "" + pseudo.getNumberOfArguments(), "" + args.length);
		
		for(String instruction: pseudo.getInstructions()) { // assemble pseudo-instruction instructions
			for(int i = 0; i < args.length; i++)
				instruction = instruction.replace(InstructionSet.ARGUMENT_CHAR + "" + (i + 1), args[i].trim()).trim();
			
			// Check if the interpreted instruction is another pseudo-instruction
			split = instruction.split("\\s+", 2); // split mnemonic and args
			m = split[0].trim();
			if(cpu.getInstructionSet().hasPseudoInstruction(m)) {
				String pargs[] = (split.length == 2) ? split[1].trim().split(",") : new String[0]; // split args, if any
				instructions.addAll(interpretPseudoInstruction(m, pargs, lineNumber));
			}
			else
				instructions.add(instruction);
		}
		
		return instructions;
	}
	
	/**
	 * Assembles an instruction into an assembled instruction.
	 * @param line The line with the instruction.
	 * @param index The index of the instruction.
	 * @param lineNumber The number of the line of code.
	 * @return The assembled instruction.
	 * @throws SyntaxErrorException If the code has a syntax error.
	 */
	private AssembledInstruction assembleInstruction(String line, int index, int lineNumber) throws SyntaxErrorException {
		String inst = line.split("" + COMMENT_CHAR, 2)[0].trim(); // remove comment, if any
		String[] split = inst.split("\\s+", 2); // split mnemonic and args
		String mnemonic = split[0].trim();
		String[] args = (split.length == 2) ? split[1].trim().split(",") : new String[0]; // split args, if any
		
		if(!cpu.getInstructionSet().hasInstruction(mnemonic))
			throw new SyntaxErrorException(SyntaxErrorException.Type.UNKNOWN_INSTRUCTION, lineNumber, mnemonic);
		
		Instruction instruction = cpu.getInstructionSet().getInstruction(mnemonic);
		Data data = new Data();
		Instruction.FieldValue f;
		int value = 0;
		if(instruction.getNumberOfArguments() != args.length)
			throw new SyntaxErrorException(SyntaxErrorException.Type.WRONG_NUMBER_OF_ARGUMENTS, lineNumber, "" + instruction.getNumberOfArguments(), "" + args.length);
		
		for(InstructionType.Field field: instruction.getType().getFields()) {
			f = instruction.getField(field);
			if(f instanceof Instruction.FieldConstant) {
				value = ((Instruction.FieldConstant)f).getValue();
			}
			else if(f instanceof Instruction.FieldFromArgument) {
				Instruction.FieldFromArgument fa = (Instruction.FieldFromArgument)f;
				switch(fa.getArgumentType()) {
					case INT: case LABEL: value = parseIntArg(args[fa.getArgIndex()].trim(), lineNumber); break;
					case REG: value = parseRegArg(args[fa.getArgIndex()].trim(), lineNumber); break;
					case TARGET: value = parseTargetArg(args[fa.getArgIndex()].trim(), lineNumber); break;
					case OFFSET: value = parseOffsetArg(args[fa.getArgIndex()].trim(), lineNumber, index); break;
				}
			}
			else if(f instanceof Instruction.FieldDataFromArgument) {
				Instruction.FieldDataFromArgument fd = (Instruction.FieldDataFromArgument)f;
				switch(fd.getType()) {
					case BASE: value = parseBaseDataArg(args[fd.getArgIndex()].trim(), lineNumber); break;
					case OFFSET: value = parseOffsetDataArg(args[fd.getArgIndex()].trim(), lineNumber); break;
				}
			}
			data.setValue(data.getValue() | field.getValueInField(value));
		}
		
		return new AssembledInstruction(instruction, data, line, lineNumber);
	}
	
	/**
	 * Returns the specified address aligned to word boundary (ex: returns 16 if address=15)-
	 * @param address The address to align.
	 * @return The aligned address.
	 */
	private int alignAddressToWord(int address) {
		return (address + 3) >> 2 << 2;
	}
	
	/**
	 * Parses an integer from an instruction argument.
	 * @param arg The instruction argument.
	 * @param lineNumber The number of the line of code.
	 * @return The parsed integer.
	 * @throws SyntaxErrorException If the argument is invalid.
	 */
	private int parseIntArg(String arg, int lineNumber) throws SyntaxErrorException {
		try { // integer?
			return Long.decode(arg).intValue();
		}
		catch(NumberFormatException e) { // a label? (for la)
			Integer label;
			if((label = textLabels.get(arg)) != null)
				return label * (Data.DATA_SIZE / 8);
			else if((label = dataLabels.get(arg)) != null)
				return label;
			else
				throw new SyntaxErrorException(SyntaxErrorException.Type.INVALID_INT_ARG, lineNumber, arg);
		}
	}
	
	/**
	 * Parses a register identifier from an instruction argument.
	 * @param arg The instruction argument.
	 * @param lineNumber The number of the line of code.
	 * @return The parsed register identifier.
	 * @throws SyntaxErrorException If the argument is invalid.
	 */
	private int parseRegArg(String arg, int lineNumber) throws SyntaxErrorException {
		int index = cpu.getRegisterIndex(arg);
		if(index >= 0)
			return index;
		else
			throw new SyntaxErrorException(SyntaxErrorException.Type.INVALID_REG_ARG, lineNumber, arg);
	}
	
	/**
	 * Parses a target address from an instruction argument.
	 * @param arg The instruction argument.
	 * @param lineNumber The number of the line of code.
	 * @return The parsed target address.
	 * @throws SyntaxErrorException If the argument is invalid.
	 */
	private int parseTargetArg(String arg, int lineNumber) throws SyntaxErrorException {
		try { // direct address?
			return Long.decode(arg).intValue();
		}
		catch(NumberFormatException e) { // label
			Integer target;
			if((target = textLabels.get(arg)) != null)
				return target;
			else
				throw new SyntaxErrorException(SyntaxErrorException.Type.UNKNOWN_LABEL, lineNumber, arg);
		}
	}
	
	/**
	 * Parses an address offset from an instruction argument.
	 * @param arg The instruction argument.
	 * @param lineNumber The number of the line of code.
	 * @param index The index of the instruction.
	 * @return The parsed address offset.
	 * @throws SyntaxErrorException If the argument is invalid.
	 */
	private int parseOffsetArg(String arg, int lineNumber, int index) throws SyntaxErrorException {
		try { // direct offset?
			return Long.decode(arg).intValue();
		}
		catch(NumberFormatException e) { // label
			Integer target;
			if((target = textLabels.get(arg)) != null) {
				return target - index - 1;
			}
			else
				throw new SyntaxErrorException(SyntaxErrorException.Type.UNKNOWN_LABEL, lineNumber, arg);
		}
	}
	
	/**
	 * Parses the data base address from an instruction argument.
	 * @param arg The instruction argument.
	 * @param lineNumber The number of the line of code.
	 * @return The parsed base address.
	 * @throws SyntaxErrorException If the argument is invalid.
	 */
	private int parseBaseDataArg(String arg, int lineNumber) throws SyntaxErrorException {
		int i = arg.indexOf("(");
		if(i >= 0) arg = arg.substring(0, i); // remove "($offset)" part, if it exists

		try { // direct address?
			return Long.decode(arg).intValue();
		}
		catch(NumberFormatException e) { // label
			Integer target;
			if((target = dataLabels.get(arg)) != null)
				return target;
			else
				throw new SyntaxErrorException(SyntaxErrorException.Type.UNKNOWN_LABEL, lineNumber, arg);
		}
	}
	
	/**
	 * Parses the data offset register from an instruction argument.
	 * @param arg The instruction argument.
	 * @param lineNumber The number of the line of code.
	 * @return The parsed offset register.
	 * @throws SyntaxErrorException If the argument is invalid.
	 */
	private int parseOffsetDataArg(String arg, int lineNumber) throws SyntaxErrorException {
		int i = arg.indexOf("(");
		if(i < 0) // only "address", so offset is 0 from the register $0
			return 0;
		else { // base($offset)
			int j = arg.indexOf(")", i);
			if(j < 0 || j != arg.length() - 1) throw new SyntaxErrorException(SyntaxErrorException.Type.INVALID_DATA_ARG, lineNumber, arg);

			String reg = arg.substring(i + 1, j);
			int index = cpu.getRegisterIndex(reg);
			if(index >= 0)
				return index;
			else
				throw new SyntaxErrorException(SyntaxErrorException.Type.INVALID_DATA_ARG, lineNumber, arg);
		}
	}
	
	/**
	 * Saves a line of code (pseudo-instructions already interpreted) and it's original line number.
	 */
	private class CodeLine {
		/** The line of code. */
		public String line;
		/** The original number of the line. */
		public int number;

		/**
		 * Constructor
		 * @param line The line of code.
		 * @param lineNumber The original number of the line.
		 */
		public CodeLine(String line, int lineNumber) {
			this.line = line;
			this.number = lineNumber;
		}
	}
}
