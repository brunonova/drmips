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

package brunonova.drmips.pc;

import brunonova.drmips.simulator.*;
import java.awt.Color;
import java.awt.Font;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * The code editor of the simulator.
 * <p>The class extends the <tt>TextEditorPane</tt> from RSyntaxTextArea component.<br>
 * RSyntaxTextArea: <a href="http://fifesoft.com/rsyntaxtextarea/">http://fifesoft.com/rsyntaxtextarea/</a></p>
 * 
 * @author Bruno Nova
 */
public class CodeEditor extends TextEditorPane {
	/** The icon used to display errors in the line numbers column. */
	public static final Icon ERROR_ICON = new ImageIcon(CodeEditor.class.getResource("/res/icons/x16/error.png"));
		
	/** The editor's scroll pane. */
	private RTextScrollPane scrollPane;
	/** The provider for the auto-complete. */
	private MIPSCompletionProvider completeProvider;
	/** The auto-complete feature associated with this code editor. */
	private AutoCompletion complete;
	/** The cpu with the supported instructions. */
	private CPU cpu = null;
	
	/** Class logger. */
	private static final Logger LOG = Logger.getLogger(CodeEditor.class.getName());
	
	/**
	 * Creates the code editor.
	 */
	public CodeEditor() {
		super();
		scrollPane = new RTextScrollPane(this);
		scrollPane.setIconRowHeaderEnabled(true);
		setRows(20);
		setColumns(80);
		setMarginLinePosition(80);
		setAntiAliasingEnabled(true);
		//setMarkOccurrences(true);
		setLineWrap(true);
		setColors();
		
		completeProvider = new MIPSCompletionProvider();
		complete = new AutoCompletion(completeProvider);
		complete.setShowDescWindow(true);
		complete.setAutoCompleteSingleChoices(false);
		complete.install(this);
	}
	
	/**
	 * Creates the code editor with a custom popup menu.
	 * @param menu The custom popup menu.
	 */
	public CodeEditor(JPopupMenu menu) {
		this();
		setPopupMenu(menu);
	}
	
	/**
	 * Returns the editor's scroll pane.
	 * @return The editor's scroll pane.
	 */
	public RTextScrollPane getScrollPane() {
		return scrollPane;
	}
	
	/**
	 * Removes all error icons from the line numbers column.
	 */
	public void clearErrorIcons() {
		scrollPane.getGutter().removeAllTrackingIcons();
	}
	
	/**
	 * Adds an error icon to the specified line.
	 * @param line The line to add the icon to (starts on 1).
	 */
	public void addErrorIcon(int line) {
		try {
			scrollPane.getGutter().addLineTrackingIcon(line - 1, ERROR_ICON);
		} catch (BadLocationException ex) {
			LOG.log(Level.WARNING, "error adding error icon to line " + line, ex);
		}
	}
	
	/**
	 * Adds an error icon to the specified line with an additional tip message.
	 * @param line The line to add the icon to (starts on 1).
	 * @param tip Tip message to display when hovering the cursor over the icon.
	 */
	public void addErrorIcon(int line, String tip) {
		try {
			scrollPane.getGutter().addLineTrackingIcon(line - 1, ERROR_ICON, tip);
		} catch (BadLocationException ex) {
			LOG.log(Level.WARNING, "error adding error icon to line " + line, ex);
		}
	}
	
	/**
	 * Sets the CPU for the code editor.
	 * @param cpu The cpu with the supported instructions.
	 */
	public void setCPU(CPU cpu) {
		this.cpu = cpu;
		setSyntaxEditingStyleForCPU();
		setAutoComplete();
	}
	
	/**
	 * Translates the code editor (auto-complete strings).
	 */
	public void translate() {
		setCPU(cpu);
	}
	
	/**
	 * Sets the syntax highlighting rules for the given CPU.
	 */
	private void setSyntaxEditingStyleForCPU() {
		String oldStyle = getSyntaxEditingStyle();
		((RSyntaxDocument)getDocument()).setSyntaxStyle(new MIPSTokenMaker(cpu));
		firePropertyChange(SYNTAX_STYLE_PROPERTY, oldStyle, "DrMIPS");
		setActiveLineRange(-1, -1);
	}
	
	/**
	 * Sets the auto-complete options for the given CPU.
	 */
	private void setAutoComplete() {
		completeProvider.clear();
		String desc;
		
		for(Instruction instruction: cpu.getInstructionSet().getInstructions()) { // add instructions
			completeProvider.addCompletion(new BasicCompletion(completeProvider, instruction.getMnemonic(), instruction.getUsage(), getInstructionSummary(instruction)));
		}
		
		for(PseudoInstruction pseudo: cpu.getInstructionSet().getPseudoInstructions()) { // add pseudo-instructions
			completeProvider.addCompletion(new BasicCompletion(completeProvider, pseudo.getMnemonic(), pseudo.getUsage(), getInstructionSummary(pseudo)));
		}
		
		addDirectives();
	}
	
	/**
	 * Returns the summary of the given instruction or pseudo-instruction.
	 * @param instruction The instruction or pseudo-instruction.
	 * @return Summary of the instruction or pseudo-instruction.
	 */
	private String getInstructionSummary(AbstractInstruction instruction) {
		// Header line (mnemonic and instruction/pseudo)
		String desc = "<b><u><tt>" + instruction.getMnemonic() + "</tt> (";
		boolean pseudo = instruction instanceof PseudoInstruction;
		desc += pseudo ? Lang.t("pseudo_instruction") : Lang.t("instruction");
		desc += ")</u></b><br /><br /><dl>";
		
		// Usage
		String usage = instruction.getUsage();
		desc += "<dt><b>" + Lang.t("usage") + ":</b></dt>";
		desc += "<dd><pre>" + instruction.getUsage() + "</pre></dd>";

		// Arguments
		if(instruction.hasArguments()) {
			desc += "<dt><br /><b>" + Lang.t("arguments") + ":</b></dt>";
			String ex = Lang.t("ex");
			if(instruction.hasArguments()) {
				for(int i = 0; i < instruction.getNumberOfArguments(); i++) {
					desc += "<dd>";
					switch(instruction.getArgument(i)) {
						case INT: 
							desc += Lang.t("integer");
							desc += " (" + ex + ": " + (20 + i + 1) + " , " + -(20 + i + 1) + ")";
							break;
						case REG: 
							desc += Lang.t("register");
							desc += " (" + ex + ": " + CPU.REGISTER_PREFIX + "t" + (i + 1) + ")";
							break;
						case TARGET:
							desc += Lang.t("target");
							desc += " (" + ex + ": start , " + (20 + i + 1) + ")";
							break;
						case LABEL:
							desc += Lang.t("label");
							desc += " (" + ex + ": num , start)";
							break;
						case OFFSET: 
							desc += Lang.t("offset");
							desc += " (" + ex + ": start , " + (20 + i + 1) + " , " + -(20 + i + 1) + ")";
							break;
						case DATA:
							desc += Lang.t("data");
							desc += " (" + ex + ": num , num(" + CPU.REGISTER_PREFIX + "t" + (i + 1) + ") , "
								+ (20 + i * (Data.DATA_SIZE / 8)) + " , "
								+ (20 + i * (Data.DATA_SIZE / 8)) + "(" + CPU.REGISTER_PREFIX + "t" + (i + 1) + "))";
							break;
					}
					desc += "</dd>";
				}
			}
		}
		
		// Operation/description
		if(instruction.hasDescription()) {
			desc += "<dt><br /><b>" + Lang.t("operation") + ":</b></dt>";
			desc += "<dd><tt>" + instruction.getDescription().replace("<", "&lt;").replace(">", "&gt;") + "</tt></dd>";
		}
		
		// Resulting instructions if pseudo-instruction
		if(cpu != null && instruction instanceof PseudoInstruction) {
			String[] split;
			List<String> lines = cpu.getAssembler().interpretPseudoInstruction(usage);
			if(!lines.isEmpty()) {
				desc += "<dt><br /><b>" + Lang.t("results_in") + ":</b></dt>";
				for(String line: lines) {
					// add href to mnemonic
					split = line.trim().split("\\s+", 2);
					if(split.length > 0) {
						line = "<a href='" + split[0] + "'>" + split[0] + "</a> ";
						if(split.length == 2)
							line += split[1];
					}
					
					
					desc += "<dd><tt>" + line + "</tt></dd>";
				}
			}
		}
		
		desc += "</dl>";
		return desc;
	}
	
	/**
	 * Adds the directives to the auto-complete options.
	 */
	private void addDirectives() {
		String desc = Lang.t("directive"), summ;
		
		summ = "<b><u><tt>.text </tt> (" + Lang.t("directive") + ")</u></b><br /><br /><dl>";
		summ += "<dt><b>" + Lang.t("usage") + ":</b></dt><dd><pre>.text</pre></dd>";
		summ += "<dt><br /><b>" + Lang.t("description") + ":</b></dt><dd>" + Lang.t("text_directive_description") +"</dd></dl>";
		completeProvider.addCompletion(new BasicCompletion(completeProvider, ".text", desc, summ));
		
		if(cpu.hasDataMemory()) {
			summ = "<b><u><tt>.data </tt> (" + Lang.t("directive") + ")</u></b><br /><br /><dl>";
			summ += "<dt><b>" + Lang.t("usage") + ":</b></dt><dd><pre>.data</pre></dd>";
			summ += "<dt><br /><b>" + Lang.t("description") + ":</b></dt><dd>" + Lang.t("data_directive_description") +"</dd></dl>";
			completeProvider.addCompletion(new BasicCompletion(completeProvider, ".data", desc, summ));

			summ = "<b><u><tt>.word </tt> (" + Lang.t("directive") + ")</u></b><br /><br /><dl>";
			summ += "<dt><b>" + Lang.t("usage") + ":</b></dt><dd><pre>.word 20, -13, 56, ...</pre></dd>";
			summ += "<dt><br /><b>" + Lang.t("description") + ":</b></dt><dd>" + Lang.t("word_directive_description") +"</dd></dl>";
			completeProvider.addCompletion(new BasicCompletion(completeProvider, ".word", desc, summ));

			summ = "<b><u><tt>.space </tt> (" + Lang.t("directive") + ")</u></b><br /><br /><dl>";
			summ += "<dt><b>" + Lang.t("usage") + ":</b></dt><dd><pre>.space 16</pre></dd>";
			summ += "<dt><br /><b>" + Lang.t("description") + ":</b></dt><dd>" + Lang.t("space_directive_description") +"</dd></dl>";
			completeProvider.addCompletion(new BasicCompletion(completeProvider, ".space", desc, summ));
		}
	}
	
	/**
	 * Sets custom colors for the editor.
	 */
	public final void setColors() {
		SyntaxScheme scheme = getSyntaxScheme();
		boolean dark = DrMIPS.prefs.getBoolean(DrMIPS.DARK_THEME_PREF, DrMIPS.DEFAULT_DARK_THEME);
		
		setBackground(dark ? Color.BLACK : Color.WHITE);
		getScrollPane().getGutter().setBackground(dark ? Color.BLACK : Color.WHITE);
		setCaretColor(dark ? Color.WHITE : Color.BLACK);
		setCurrentLineHighlightColor(dark ? (new Color(32, 32, 32)) : (new Color(255, 255, 170)));
		setSelectionColor(dark ? (new Color(64, 64, 64)) : (new Color(200, 200, 255)));
		setMarginLineColor(dark ? Color.DARK_GRAY : Color.LIGHT_GRAY);

		Font keywordFont = new Font(getDefaultFont().getFamily(), Font.BOLD, getDefaultFont().getSize());
		Font commentFont = new Font(getDefaultFont().getFamily(), Font.ITALIC, getDefaultFont().getSize());
		Style identifierStyle = new Style(dark ? Color.WHITE : Color.BLACK);
		Style directiveStyle = new Style(dark ? new Color(0, 160, 160) : new Color(0, 128, 128), null, keywordFont);
		Style errorStyle = new Style(Color.RED);

		scheme.setStyle(Token.IDENTIFIER /* references to labels */,
		                identifierStyle);
		scheme.setStyle(Token.RESERVED_WORD /* instructions */,
		                new Style(dark ? new Color(128, 128, 255) : new Color(0, 0, 255), null, keywordFont));
		scheme.setStyle(Token.RESERVED_WORD_2 /* pseudo-instructions */,
		                new Style(dark ? new Color(255, 128, 255) : new Color(128, 0, 255), null, keywordFont));
		scheme.setStyle(Token.LITERAL_NUMBER_DECIMAL_INT /* integer numeric values */,
		                identifierStyle);
		scheme.setStyle(Token.OPERATOR /* argument separators (commas and parentheses) */,
		                identifierStyle);
		scheme.setStyle(Token.PREPROCESSOR /* .text and .data directives */,
		                directiveStyle);
		scheme.setStyle(Token.DATA_TYPE /* .word and .space directives */,
		                directiveStyle);
		scheme.setStyle(Token.ERROR_CHAR /* invalid characters (like special characters) */,
		                errorStyle);
		scheme.setStyle(Token.ERROR_IDENTIFIER /* invalid directives */,
		                errorStyle);
		scheme.setStyle(Token.ERROR_NUMBER_FORMAT /* invalid integer numeric values */,
		                errorStyle);
		scheme.setStyle(Token.FUNCTION /* labels */,
		                new Style(dark ? new Color(160, 160, 160) : new Color(96, 96, 96), null, commentFont));
		scheme.setStyle(Token.COMMENT_EOL /* comments */,
		                new Style(dark ? new Color(0, 160, 0) : new Color(0, 128, 0), null, commentFont));
		scheme.setStyle(Token.VARIABLE /* registers */,
		                new Style(dark ? new Color(255, 128, 0) : new Color(192, 96, 0), null, keywordFont));
	}
	
	
	/**
	 * Completion provider for the MIPS instructions.
	 * <p>This checks the text entered up to the caret position to provide the
	 * auto-complete possibilities.</p>
	 */
	private class MIPSCompletionProvider extends DefaultCompletionProvider {
		private final List<BasicCompletion> labelCompletions = new LinkedList<>();
			
		/**
		 * Creates the provider.
		 */
		public MIPSCompletionProvider() {
			super();
		}

		@Override
		protected boolean isValidChar(char ch) {
			return RSyntaxUtilities.isLetterOrDigit(ch) || ch == '_' || ch == '.';
		}

		@Override
		public List getCompletions(JTextComponent comp) {
			// Add labels to auto-complete
			if(cpu != null) {
				// Remove previous completions
				for(BasicCompletion c: labelCompletions)
					removeCompletion(c);
				labelCompletions.clear();
				
				// Add current labels
				Set<String> labels = cpu.getAssembler().getCodeLabels(getText());
				BasicCompletion c;
				String desc = Lang.t("label");
				String summ = Lang.t("label_in_code");
				for(String label: labels) {
					c = new BasicCompletion(this, label, desc, "<b><u><tt>" + label + "</tt> (" + desc + ")</u></b><br /><br />" + summ);
					labelCompletions.add(c);
					addCompletion(c);
				}
			}
			
			return super.getCompletions(comp); // do the default code
		}
	}
	
	/**
	* The "token maker" for the MIPS instructions supported by the loaded CPU.
	* <p>This interprets the code and splits it in token that are then used for
	* syntax highlighting</p>
	*/
   public class MIPSTokenMaker extends AbstractTokenMaker {
	   /** The CPU with the supported instructions and registers. */
	   private final CPU cpu;

	   /**
		* Creates the token maker.
		* @param cpu The CPU with the supported instructions and registers.
		*/
	   public MIPSTokenMaker(CPU cpu) {
		   this.cpu = cpu;
	   }

	   @Override
	   public TokenMap getWordsToHighlight() {
		   return null; // not used
	   }

	   @Override
	   public String[] getLineCommentStartAndEnd() {
		   return new String[] {"#", null};
	   }

	   @Override
	   public boolean getMarkOccurrencesOfTokenType(int type) {
		   return type == Token.VARIABLE;
	   }

	   @Override
	   public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
		   resetTokenList();

		   char[] array = text.array;
		   int offset = text.offset;
		   int count = text.count;
		   int end = offset + count;
		   int start;
		   String str;

		   // See, when we find a token, its starting position is always of the form:
		   // 'startOffset + (currentTokenStart-offset)'; but since startOffset and
		   // offset are constant, tokens' starting positions become:
		   // 'newStartOffset+currentTokenStart' for one less subraction operation.
		   int newStartOffset = startOffset - offset;

		   for (int i = offset; i < end; i++) {
			   char c = array[i];
			   switch(c) {
				   case ' ': // whitespaces
				   case '\t':
					   addToken(text, i, i, Token.WHITESPACE, newStartOffset + i);
					   break;
				   case ',': // comma
					   addToken(text, i, i, Token.OPERATOR, newStartOffset + i);
					   break;
				   case '(': // parenthesis
				   case ')':
					   addToken(text, i, i, Token.OPERATOR, newStartOffset + i);
					   break;
				   case Assembler.COMMENT_CHAR: // comment
					   addToken(text, i, end - 1, Token.COMMENT_EOL, newStartOffset + i);
					   i = end;
					   break;
				   case CPU.REGISTER_PREFIX: // register
					   str = "";
					   for(start = i++; i < end && RSyntaxUtilities.isLetterOrDigit(array[i]); i++) {str += array[i];}
					   i--;
					   addToken(text, start, i, cpu.hasRegister(CPU.REGISTER_PREFIX + str) ? Token.VARIABLE : Token.ERROR_IDENTIFIER, newStartOffset + start);
					   break;
				   case '.': // directive
					   str = "";
					   for(start = i++; i < end && RSyntaxUtilities.isLetterOrDigit(array[i]); i++) {str += array[i];}
					   i--;
					   str = str.toLowerCase();
					   if(str.equals("text") || (str.equals("data") && cpu.hasDataMemory()))
						   addToken(text, start, i, Token.PREPROCESSOR, newStartOffset + start);
					   else if(cpu.hasDataMemory() && (str.equals("word") || str.equals("space")))
						   addToken(text, start, i, Token.DATA_TYPE, newStartOffset + start);
					   else
						   addToken(text, start, i, Token.ERROR_IDENTIFIER, newStartOffset + start);
					   break;
				   case '-': // negative number?
						str = "";
						for(start = i++; i < end && (RSyntaxUtilities.isLetterOrDigit(array[i]) || array[i] == '.' || array[i] == '_'); i++) {str += array[i];}
						i--;
						try {
							Long.decode(str);
							addToken(text, start, i, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + start);
						}
						catch(NumberFormatException e) {
							addToken(text, start, i, Token.ERROR_NUMBER_FORMAT, newStartOffset + start);
						}
						break;

				   default:
					   if(RSyntaxUtilities.isDigit(c)) { // integer
							str = "";
							for(start = i; i < end && (RSyntaxUtilities.isLetterOrDigit(array[i]) || array[i] == '.' || array[i] == '_'); i++) {str += array[i];}
							i--;
							try {
								Long.decode(str);
								addToken(text, start, i, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + start);
							}
							catch(NumberFormatException e) {
								addToken(text, start, i, Token.ERROR_NUMBER_FORMAT, newStartOffset + start);
							}
					   }
					   else if(RSyntaxUtilities.isLetter(c)) { // an identifier
						   str = "";
						   for(start = i; i < end && (RSyntaxUtilities.isLetterOrDigit(array[i]) || array[i] == '.' || array[i] == '_'); i++) {str += array[i];}
						   if(i < end && array[i] == ':') // label
							   addToken(text, start, i, Token.FUNCTION, newStartOffset + start);
						   else { // another
							   i--;
							   if(cpu.getInstructionSet().hasInstruction(str)) // instruction
								   addToken(text, start, i, Token.RESERVED_WORD, newStartOffset + start);
							   else if(cpu.getInstructionSet().hasPseudoInstruction(str)) // pseudo-instruction
								   addToken(text, start, i, Token.RESERVED_WORD_2, newStartOffset + start);
							   else // another identifier
								   addToken(text, start, i, Token.IDENTIFIER, newStartOffset + start);
						   }
					   }
					   else // something else
						   addToken(text, i, i, Token.ERROR_CHAR, newStartOffset + i);
					   break;
			   }
		   }

		   addNullToken();
		   return firstToken;
	   }
   }
}
