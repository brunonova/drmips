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

package brunonova.drmips.simulator.exceptions;

import java.util.List;

/**
 * Exception to be thrown when the MIPS code contains a syntax error.
 * 
 * @author Bruno Nova
 */
public class SyntaxErrorException extends Exception {
	/** The types of syntax errors. */
	public enum Type {INVALID_LABEL, DUPLICATED_LABEL, UNKNOWN_LABEL, UNKNOWN_DATA_DIRECTIVE, UNKNOWN_INSTRUCTION, 
		INVALID_INT_ARG, INVALID_REG_ARG, INVALID_DATA_ARG, WRONG_NUMBER_OF_ARGUMENTS, INVALID_POSITIVE_INT_ARG,
		DATA_SEGMENT_WITHOUT_DATA_MEMORY}
	
	/** The type of the syntax error. */
	private Type type;
	/** The code line where the error is. */
	private int line;
	/** Extra information for the error (code that is causing the error?). <tt>null</tt> if there is no extra information. */
	private String extra = null;
	/** More extra information for the error. */
	private String extra2 = null;
	/** The other syntax errors on the code. */
	private List<SyntaxErrorException> otherErrors = null;
	
	/**
	 * Exception constructor.
	 * @param type The type of the syntax error.
	 * @param line The code line where the error is.
	 */
	public SyntaxErrorException(Type type, int line) {
		super("Line: " + line + ": " + type.toString());
		this.type = type;
		this.line = line;
	}
	
	/**
	 * Exception constructor.
	 * @param type The type of the syntax error.
	 * @param line The code line where the error is.
	 * @param extra Extra information for the error (code that is causing the error?). <tt>null</tt> if there is no extra information.
	 */
	public SyntaxErrorException(Type type, int line, String extra) {
		this(type, line);
		this.extra = extra;
	}
	
	/**
	 * Exception constructor.
	 * @param type The type of the syntax error.
	 * @param line The code line where the error is.
	 * @param extra Extra information for the error (code that is causing the error?). <tt>null</tt> if there is no extra information.
	 * @param extra2 More extra information for the error.
	 */
	public SyntaxErrorException(Type type, int line, String extra, String extra2) {
		this(type, line, extra);
		this.extra2 = extra2;
	}
	
	/**
	 * Returns the type of the syntax error.
	 * @return The type of the syntax error.
	 */
	public Type getType() {
		return type;
	}
	
	/**
	 * Returns the code line where the error is.
	 * @return The code line where the error is.
	 */
	public int getLine() {
		return line;
	}
	
	/**
	 * Returns the extra information for the error (code that is causing the error?). 
	 * <p><tt>null</tt> if there is no extra information.</p>
	 * @return Extra information for the error.
	 */
	public String getExtra() {
		return extra;
	}
	
	/**
	 * Returns more extra information for the error.. 
	 * <p><tt>null</tt> if there is no extra information.</p>
	 * @return More extra information for the error..
	 */
	public String getExtra2() {
		return extra2;
	}
	
	/**
	 * Returns whether this error has references to other errors.
	 * @return <tt>True</tt> if this error has references to other errors.
	 */
	public boolean hasOtherErrors() {
		return otherErrors != null && !otherErrors.isEmpty();
	}
	
	/**
	 * Returns the other syntax errors on the code.
	 * @return The other syntax errors on the code.
	 */
	public List<SyntaxErrorException> getOtherErrors() {
		return otherErrors;
	}
	
	/**
	 * Sets the references to other errors on the code.
	 * @param otherErrors List of errors to set.
	 */
	public void setOtherErrors(List<SyntaxErrorException> otherErrors) {
		this.otherErrors = otherErrors;
	}
}
