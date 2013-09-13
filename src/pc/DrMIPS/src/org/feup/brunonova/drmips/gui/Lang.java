/*
    DrMIPS - Educational MIPS simulator
    Copyright (C) 2013 Bruno Nova <ei08109@fe.up.pt>

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

package org.feup.brunonova.drmips.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.UIManager;

/**
 * Class that loads a language file to allow translating individual strings.
 * <p>First <tt>load()</tt> the file then get the translated string with <tt>t()</tt>.<br />
 * The language file's lines should be in the form <tt>key=string</tt>.<br />
 * Invalid lines or lines starting with '#' are ignored silently.<br />
 * A '&' indicates that the next character can be used as a button mnemonic (write
 * "&&" to display a '&').</p>
 * 
 * @author Bruno Nova
 */
public class Lang {
	/** The path to the language files, with the trailing slash. */
	public static final String FILENAME_PATH = "lang" + File.separator;
	/** The file extension of the language files. */
	public static final String FILENAME_EXTENSION = "lng";
	/** The character that prepends the char to be considered the mnemonic char. */
	public static final char MNEMONIC_CHAR = '&';
	/** The character that indicates a comment in the file. */
	public static final char COMMENT_CHAR = '#';
	/** The character that can indicate a string argument, if followed by a number starting from 1. */
	public static final char ARG_CHAR = '#';
	
	/** The loaded language (the name part of the file name). */
	private static String language;
	/** The path of the loaded language file. */
	private static String filename;
	/** The strings in the loaded language. */
	private static Map<String, String> strings;
	
	/**
	 * Loads the strings of the specified language.
	 * @param language Language (just the name part of the file name).
	 * @throws IOException If an error occurred opening or reading the language file.
	 */
	public static void load(String language) throws IOException {
		String line;
		int index;
		
		Lang.language = language;
		filename = DrMIPS.path + File.separator + FILENAME_PATH + language + "." + FILENAME_EXTENSION;
		
		// Load strings
		strings = new HashMap<String, String>(500);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
		while((line = reader.readLine()) != null) {
			line = line.trim();
			index = line.indexOf('=');
			if(index > 0 && !line.startsWith("" + COMMENT_CHAR))
				strings.put(line.substring(0, index).trim(), line.substring(index + 1).trim());
		}
		reader.close(); 
		
		// Translate message box buttons and mnemonics
		UIManager.put("OptionPane.okButtonText", Lang.t("ok"));
		UIManager.put("OptionPane.cancelButtonText", Lang.t("cancel"));
		UIManager.put("OptionPane.yesButtonText", Lang.t("yes"));
		UIManager.put("OptionPane.noButtonText", Lang.t("no"));
		UIManager.put("OptionPane.okButtonMnemonic", "" + (int)Lang.mnemonic("ok")); // argument for the mnemonic must be the ASCII code of the mnemonic character
		UIManager.put("OptionPane.cancelButtonMnemonic", "" + (int)Lang.mnemonic("cancel"));
		UIManager.put("OptionPane.yesButtonMnemonic", "" + (int)Lang.mnemonic("yes"));
		UIManager.put("OptionPane.noButtonMnemonic", "" + (int)Lang.mnemonic("no"));
		
		// Translate file chooser text
		UIManager.put("FileChooser.lookInLabelText", Lang.t("look_in") + ":");
		UIManager.put("FileChooser.saveInLabelText", Lang.t("save_in") + ":");
		UIManager.put("FileChooser.cancelButtonText", Lang.t("cancel"));
		UIManager.put("FileChooser.cancelButtonToolTipText", Lang.t("cancel"));
		UIManager.put("FileChooser.openButtonText", Lang.t("open"));
		UIManager.put("FileChooser.openButtonToolTipText", Lang.t("open_selected_file"));
		UIManager.put("FileChooser.directoryOpenButtonText", Lang.t("open"));
		UIManager.put("FileChooser.directoryOpenButtonToolTipText", Lang.t("open_selected_directory"));
		UIManager.put("FileChooser.saveButtonText", Lang.t("save"));
		UIManager.put("FileChooser.saveButtonToolTipText", Lang.t("save_selected_file"));
		UIManager.put("FileChooser.filesOfTypeLabelText", Lang.t("files_of_type") + ":");
		UIManager.put("FileChooser.fileNameLabelText", Lang.t("file_name") + ":");
		UIManager.put("FileChooser.acceptAllFileFilterText", Lang.t("all_files"));
		UIManager.put("FileChooser.upFolderToolTipText", Lang.t("up_one_level"));
		UIManager.put("FileChooser.upFolderAccessibleName", Lang.t("up_one_level"));
		UIManager.put("FileChooser.homeFolderToolTipText", Lang.t("home"));
		UIManager.put("FileChooser.homeFolderAccessibleName", Lang.t("home"));
		UIManager.put("FileChooser.newFolderToolTipText", Lang.t("create_new_folder"));
		UIManager.put("FileChooser.newFolderAccessibleName", Lang.t("new_folder"));
		UIManager.put("FileChooser.listViewButtonToolTipText", Lang.t("list"));
		UIManager.put("FileChooser.listViewButtonAccessibleName", Lang.t("list"));
		UIManager.put("FileChooser.detailsViewButtonToolTipText", Lang.t("details"));
		UIManager.put("FileChooser.detailsViewButtonAccessibleName", Lang.t("details"));
		UIManager.put("FileChooser.fileNameHeaderText", Lang.t("name"));
		UIManager.put("FileChooser.fileSizeHeaderText", Lang.t("size"));
		UIManager.put("FileChooser.fileDateHeaderText", Lang.t("date"));
		UIManager.put("FileChooser.refreshActionLabelText", Lang.t("refresh"));
		UIManager.put("FileChooser.viewMenuLabelText", Lang.t("view"));
		UIManager.put("FileChooser.listViewActionLabelText", Lang.t("list"));
		UIManager.put("FileChooser.detailsViewActionLabelText", Lang.t("details"));
		UIManager.put("FileChooser.newFolderActionLabelText", Lang.t("new_folder"));
		UIManager.put("FileChooser.upFolderActionLabelText", Lang.t("go_up"));
		UIManager.put("FileChooser.homeFolderActionLabelText", Lang.t("go_home"));
	}
	
	/**
	 * Returns the translated string for the specified key.
	 * <p>Any mnemonic characters are removed (unless the char is doubled).</p>
	 * @param key Key of the string to translate.
	 * @param args Optional string arguments values.
	 * @return String translated to the loaded language, or the key in upper case if not found.
	 */
	public static String t(String key, Object... args) {
		String s = strings.get(key);
		int i = 0;
		if(s != null) {
			while((i = s.indexOf(MNEMONIC_CHAR, i)) != -1) { // remove mnemonic chars, if any
				if(i == s.length() - 1) { // last char is &
					s = s.substring(0, s.length() - 1);
					break;
				}
				else { // remove & and ignore next char
					s = s.substring(0, i) + s.substring(i + 1);
					i++;
				}
			}
			for(i = 0; i < args.length; i++) // add arguments
				s = s.replace(ARG_CHAR + "" + (i + 1) , args[i].toString());
			return s.replace("\\n", "\n");
		}
		else
			return key.toUpperCase();
	}

	/**
	 * Returns the mnemonic character from the translated string of the specified key.
	 * @param key Key of the string to translate.
	 * @return Mnemonic character of the translated string, or '\0' if not found.
	 */
	public static char mnemonic(String key) {
		String s = strings.get(key);
		if(s != null) {
			int i = 0;
			while(true) { // find the first '&' char that isn't followed by another '&'
				i = s.indexOf(MNEMONIC_CHAR, i);
				if(i >= 0 && i < s.length() - 1) {
					if(s.charAt(i + 1) != MNEMONIC_CHAR)
						return s.charAt(i + 1);
					else
						i += 2;
				}
				else
					return '\0';
			}
		}
		else
			return '\0';
	}
	
	/**
	 * Shortcut function to translate a button type component.
	 * @param btn Button type component.
	 * @param key Key of the button text string.
	 */
	public static void tButton(AbstractButton btn, String key) {
		btn.setText(t(key));
		btn.setMnemonic(mnemonic(key));
	}
	
	/**
	 * Shortcut function to translate a button type component.
	 * @param btn Button type component.
	 * @param key Key of the button text string.
	 * @param args Text string arguments values.
	 */
	public static void tButton(AbstractButton btn, String key, Object[] args) {
		btn.setText(t(key, args));
		btn.setMnemonic(mnemonic(key));
	}
	
	/**
	 * Shortcut function to translate a button type component.
	 * @param btn Button type component.
	 * @param key Key of the button text string.
	 * @param tooltipKey Key of the button tooltip string.
	 */
	public static void tButton(AbstractButton btn, String key, String tooltipKey) {
		tButton(btn, key);
		btn.setToolTipText(t(tooltipKey));
	}
	
	/**
	 * Shortcut function to translate a button type component.
	 * @param btn Button type component.
	 * @param key Key of the button text string.
	 * @param tooltipKey Key of the button tooltip string.
	 * @param textArgs Text string arguments values.
	 * @param tooltipArgs Tooltip string arguments values.
	 */
	public static void tButton(AbstractButton btn, String key, String tooltipKey, Object[] textArgs, Object[] tooltipArgs) {
		tButton(btn, key, textArgs);
		btn.setToolTipText(t(tooltipKey, tooltipArgs));
	}
	
	/**
	 * Returns the loaded language name.
	 * @return The loaded language (the name part of the file name).
	 */
	public static String getLanguage() {
		return language;
	}
	
	/**
	 * Returns the path to the loaded language.
	 * @return The path of the loaded language file.
	 */
	public static String getFilename() {
		return filename;
	}
}
