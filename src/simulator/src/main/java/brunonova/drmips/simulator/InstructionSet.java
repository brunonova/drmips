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

import brunonova.drmips.simulator.exceptions.InvalidInstructionSetException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that represents the instruction set of the loaded CPU.
 * 
 * @author Bruno Nova
 */
public class InstructionSet {
	/** The character used to reference arguments in the JSON file. */
	public static final char ARGUMENT_CHAR = '#';
	
	/** The instruction types. */
	private final List<InstructionType> types;
	/** The available instructions. */
	private final Map<String, Instruction> instructions;
	/** The available pseudo-instructions. */
	private final Map<String, PseudoInstruction> pseudoInstructions;
	/** How the control unit should work. */
	private Control control = null;
	/** How the ALU Control and ALU should work. */
	private ControlALU controlALU = null;
	
	/**
	 * Creates an instruction set from a JSON file.
	 * @param path The path of the file to load.
	 * @throws IOException If the file doesn't exist or an I/O error occurs.
	 * @throws JSONException If the JSON file is malformed.
	 * @throws InvalidInstructionSetException If the instruction set is invalid.
	 * @throws ArrayIndexOutOfBoundsException If an array index is invalid somewhere.
	 * @throws NumberFormatException If an opcode is not a number.
	 */
	public InstructionSet(String path) throws IOException, JSONException, InvalidInstructionSetException, ArrayIndexOutOfBoundsException, NumberFormatException {
		types = new ArrayList<>();
		instructions = new TreeMap<>();
		pseudoInstructions = new TreeMap<>();
		control = new Control();
		parseFile(path);
	}

	/**
	 * Adds an instruction type.
	 * <p>All the type's fields should be defined before adding it.</p>.
	 * @param type Instruction type to add.
	 * @throws InvalidInstructionSetException If an instruction type with the same identifier already exists or is invalid.
	 */
	public void addType(InstructionType type) throws InvalidInstructionSetException {
		if(hasType(type.getId())) 
			throw new InvalidInstructionSetException("Duplicated ID " + type.getId() + "!");
		if(!type.isValid()) 
			throw new InvalidInstructionSetException("Invalid instruction type " + type.getId() + "!");
		types.add(type);
		
		// Check if the opcodes are all of the same size
		if(types.size() > 1) {
			int size = types.get(0).getOpCodeField().getSize();
			for(InstructionType t: types) {
				if(t.getOpCodeField().getSize() != size)
					throw new InvalidInstructionSetException("The instruction types have different opcode sizes!");
			}
		}
	}
	
	/**
	 * Returns the instruction type with the specified identifier.
	 * @param id Identifier of the instruction type.
	 * @return The desired instruction type, or <tt>null</tt> if it doesn't exist.
	 */
	public InstructionType getType(String id) {
		for(InstructionType t: types) {
			if(t.getId().equals(id))
				return t;
		}
		return null;
	}
	
	/**
	 * Returns whether the instruction type with the given identifier exists.
	 * @param id Identifier of the instruction type.
	 * @return <tt>True</tt> if the instruction type exists.
	 */
	public boolean hasType(String id) {
		return getType(id) != null;
	}
	
	/**
	 * Returns the size of the opcode field.
	 * @return Opcode's field size.
	 */
	public int getOpCodeSize() {
		return types.get(0).getOpCodeField().getSize();
	}
	
	/**
	 * Creates and returns a new instruction, and adds it to the instruction set.
	 * <p>It will then be necessary to define all the instruction's properties.</p>
	 * @param mnemonic The mnemonic of the new instruction.
	 * @param type The type of the new instruction.
	 * @return The created instruction.
	 * @throws InvalidInstructionSetException If the instruction is invalid.
	 */
	public Instruction addNewInstruction(String mnemonic, String type) throws InvalidInstructionSetException {
		mnemonic = mnemonic.toLowerCase();
		if(!hasType(type)) throw new InvalidInstructionSetException("Unknown instruction type " + type + "!");
		if(hasInstructionOrPseudoInstruction(mnemonic)) throw new InvalidInstructionSetException("Duplicated mnemonic " + mnemonic + "!");
		Instruction i = new Instruction(mnemonic, getType(type));
		instructions.put(mnemonic, i);
		return i;
	}
	
	/**
	 * Returns the instruction with the specified mnemonic.
	 * @param mnemonic Mnemonic of the instruction.
	 * @return The desired instruction, or <tt>null</tt> if it doesn't exist.
	 */
	public Instruction getInstruction(String mnemonic) {
		return instructions.get(mnemonic.toLowerCase());
	}
	
	/**
	 * Returns whether the instruction set contains the specified instruction.
	 * @param mnemonic Mnemonic of the instruction.
	 * @return <tt>True</tt> if the instruction exists.
	 */
	public boolean hasInstruction(String mnemonic) {
		return instructions.containsKey(mnemonic.toLowerCase());
	}
	
	/**
	 * Returns the pseudo-instruction with the specified mnemonic.
	 * @param mnemonic Mnemonic of the pseudo-instruction.
	 * @return The desired pseudo-instruction, or <tt>null</tt> if it doesn't exist.
	 */
	public PseudoInstruction getPseudoInstruction(String mnemonic) {
		return pseudoInstructions.get(mnemonic.toLowerCase());
	}
	
	/**
	 * Returns whether the instruction set contains the specified pseudo-instruction.
	 * @param mnemonic Mnemonic of the pseudo-instruction.
	 * @return <tt>True</tt> if the pseudo-instruction exists.
	 */
	public boolean hasPseudoInstruction(String mnemonic) {
		return pseudoInstructions.containsKey(mnemonic.toLowerCase());
	}
	
	/**
	 * Returns whether the instruction set contains the specified instruction or pseudo-instruction.
	 * @param mnemonic Mnemonic of the instruction or pseudo-instruction.
	 * @return <tt>True</tt> if the instruction or pseudo-instruction exists.
	 */
	public boolean hasInstructionOrPseudoInstruction(String mnemonic) {
		return hasInstruction(mnemonic) || hasPseudoInstruction(mnemonic);
	}
	
	/**
	 * Returns the object that indicates how the control unit should work.
	 * @return The object that indicates how the control unit should work.
	 */
	public Control getControl() {
		return control;
	}
	
	/**
	 * Returns the object that indicates how the ALU Control and ALU should work.
	 * @return The object that indicates how the ALU Control and ALU should work.
	 */
	public ControlALU getControlALU() {
		return controlALU;
	}
	
	/**
	 * Returns the instructions supported by this instruction set.
	 * @return All instructions.
	 */
	public Instruction[] getInstructions() {
		Instruction[] array = new Instruction[instructions.size()];
		return instructions.values().toArray(array);
	}
	
	/**
	 * Returns the pseudo-instructions supported by this instruction set.
	 * @return All pseudo-instructions.
	 */
	public PseudoInstruction[] getPseudoInstructions() {
		PseudoInstruction[] array = new PseudoInstruction[pseudoInstructions.size()];
		return pseudoInstructions.values().toArray(array);
	}
	
	/**
	 * Parses the specified JSON file, loading the instruction set from it.
	 * @param path The path of the file to load.
	 * @throws IOException If the file doesn't exist or an I/O error occurs.
	 * @throws JSONException If the JSON file is malformed.
	 * @throws InvalidInstructionSetException If the instruction set is invalid.
	 * @throws ArrayIndexOutOfBoundsException If an array index is invalid somewhere.
	 */
	private void parseFile(String path) throws IOException, JSONException, InvalidInstructionSetException, ArrayIndexOutOfBoundsException {
		BufferedReader reader = null;
		String file = "", line;

		// Read file to String
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));
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
		
		JSONObject json = new JSONObject(file);
		parseTypes(json.getJSONObject("types"));
		parseInstructions(json.getJSONObject("instructions"));
		if(json.has("pseudo")) parsePseudo(json.getJSONObject("pseudo"));
		parseControl(json.getJSONObject("control"));
		parseControlALU(json.getJSONObject("alu"));
	}
	
	/**
	 * Parses the instruction types from the JSON file.
	 * @param types JSONObject that contains the instruction types.
	 * @throws JSONException If the JSON file is malformed.
	 * @throws InvalidInstructionSetException If the instruction set is invalid.
	 * @throws NumberFormatException If an opcode is not a number.
	 */
	private void parseTypes(JSONObject types) throws JSONException, InvalidInstructionSetException, NumberFormatException {
		if(types.length() == 0) throw new InvalidInstructionSetException("No instruction types specified!");
		String id;
		JSONObject field;
		JSONArray fields;
		InstructionType instructionType;
		Iterator<String> i = types.keys();
		while(i.hasNext()) {
			id = i.next();
			instructionType = new InstructionType(id);
			fields = types.getJSONArray(id);
			for(int x = 0; x < fields.length(); x++) { // add each field
				field = fields.getJSONObject(x);
				instructionType.addField(field.getString("id"), field.getInt("size"));
			}
			addType(instructionType);
		}
	}
	
	/**
	 * Parses the instructions from the JSON file.
	 * @param instructions JSONObject that contains the instructions.
	 * @throws JSONException If the JSON file is malformed.
	 * @throws InvalidInstructionSetException If the instruction set is invalid.
	 * @throws ArrayIndexOutOfBoundsException If an array index is out of bounds.
	 */
	private void parseInstructions(JSONObject instructions) throws JSONException, InvalidInstructionSetException, ArrayIndexOutOfBoundsException {
		if(instructions.length() == 0) throw new InvalidInstructionSetException("No instructions specified!");
		String mnemonic;
		JSONObject inst, fields;
		JSONArray args;
		Instruction instruction;
		String f;
		int v;
		Iterator<String> i = instructions.keys();
		
		while(i.hasNext()) { // parse each instruction
			mnemonic = i.next();
			inst = instructions.getJSONObject(mnemonic);
			instruction = addNewInstruction(mnemonic, inst.getString("type"));
			
			// Description
			if(inst.has("desc"))
				instruction.setDescription(inst.getString("desc"));
			
			// Parse arguments
			args = inst.optJSONArray("args");
			if(args != null) { 
				for(int x = 0; x < args.length(); x++)
					instruction.addArgument(args.getString(x));
			}
			
			// Parse fields
			fields = inst.getJSONObject("fields");
			for(InstructionType.Field field: instruction.getType().getFields()) {
				f = fields.optString(field.getId());
				if(f != null)
					instruction.setField(field.getId(), f);
			}
		}
	}
	
	/**
	 * Parses the pseudo-instructions from the JSON file.
	 * @param pseudos JSONObject that contains the pseudo-instructions.
	 * @throws JSONException If the JSON file is malformed.
	 * @throws InvalidInstructionSetException If the instruction set is invalid.
	 */
	private void parsePseudo(JSONObject pseudos) throws JSONException, InvalidInstructionSetException {
		String mnemonic;
		JSONObject pseudo;
		JSONArray to, args;
		Iterator<String> i = pseudos.keys();
		
		while(i.hasNext()) {
			mnemonic = i.next();
			PseudoInstruction p = new PseudoInstruction(mnemonic);
			if(hasInstructionOrPseudoInstruction(p.getMnemonic()))
				throw new InvalidInstructionSetException("Duplicated mnemonic " + p.getMnemonic() + "!");
			pseudo = pseudos.getJSONObject(mnemonic);
			
			// Description
			if(pseudo.has("desc"))
				p.setDescription(pseudo.getString("desc"));
			
			// Add arguments
			args = pseudo.optJSONArray("args");
			if(args != null) {
				for(int j = 0; j < args.length(); j++)
					p.addArgument(args.getString(j));
			}
			
			// add instructions
			to = pseudo.getJSONArray("to");
			for(int j = 0; j < to.length(); j++)
				p.addInstruction(to.getString(j));
			
			// check validity
			if(!p.isNumberOfArgumentsValid())
				throw new InvalidInstructionSetException("Invalid pseudo-instruction " + p.getMnemonic() + "! Inconsistent number of arguments.");
			
			if(p.getInstructions().isEmpty())
				throw new InvalidInstructionSetException("Pseudo-instruction " + p.getMnemonic() + " has no instructions!");
			
			pseudoInstructions.put(p.getMnemonic(), p);
		}
	}
	
	/**
	 * Parses the control information from the JSON file.
	 * @param ctrl JSONObject that contains the control information.
	 * @throws JSONException JSONException If the JSON file is malformed.
	 * @throws NumberFormatException If an opcode is not a number.
	 * @throws InvalidInstructionSetException If the instruction set is invalid.
	 */
	private void parseControl(JSONObject ctrl) throws JSONException, NumberFormatException, InvalidInstructionSetException {
		Iterator<String> i = ctrl.keys(), j;
		JSONObject c;
		String op, id;
		int opcode;
		
		while(i.hasNext()) { // parse each opcode
			op = i.next();
			c = ctrl.getJSONObject(op);
			opcode = Integer.parseInt(op);
			if(Data.requiredNumberOfBits(opcode) > getOpCodeSize())
				throw new InvalidInstructionSetException("Invalid opcode size!");
			control.addOpcode(opcode);
			
			j = c.keys();
			while(j.hasNext()) { // add each control signal
				id = j.next();
				control.addOutToOpcode(opcode, id, c.getInt(id));
			}
		}
		
		control.finishCreation();
	}
	
	/**
	 * Parses the ALU control information from the JSON file.
	 * @param ctrl JSONObject that contains the ALU control information.
	 * @throws JSONException JSONException If the JSON file is malformed.
	 * @throws NumberFormatException If a value is not a number.
	 * @throws InvalidInstructionSetException If the instruction set is invalid.
	 */
	private void parseControlALU(JSONObject ctrl) throws JSONException, NumberFormatException, InvalidInstructionSetException {
		Iterator<String> i;
		JSONObject obj, out;
		String id;
		int in;
		controlALU = new ControlALU(ctrl.getInt("aluop_size"), ctrl.getInt("func_size"), ctrl.getInt("control_size"));
		
		// Parse operations
		JSONObject operations = ctrl.getJSONObject("operations");
		i = operations.keys();
		while(i.hasNext()) {
			id = i.next();
			in = Integer.parseInt(id);
			controlALU.addOperation(in, operations.getString(id));
		}
		
		// Parse control
		JSONArray c = ctrl.getJSONArray("control");
		int aluOp, func = 0;
		boolean hasFunc;
		for(int x = 0; x < c.length(); x++) {
			obj = c.getJSONObject(x);
			aluOp = obj.getInt("aluop");
			if(obj.has("func")) {
				func = obj.getInt("func");
				hasFunc = true;
			}
			else
				hasFunc = false;
			
			out = obj.getJSONObject("out");
			i = out.keys();
			while(i.hasNext()) {
				id = i.next();
				if(hasFunc)
					controlALU.addFuncControl(aluOp, func, id, out.getInt(id));
				else
					controlALU.addALUOpControl(aluOp, id, out.getInt(id));
			}
		}
	}
}
