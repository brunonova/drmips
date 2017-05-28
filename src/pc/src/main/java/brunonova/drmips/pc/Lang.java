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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.UIManager;

/**
 * Class that loads a language file to allow translating individual strings.
 * <p>First <tt>load()</tt> the file then get the translated string with <tt>t()</tt>.<br>
 * The language file's lines should be in the form <tt>key=string</tt>.<br>
 * Invalid lines or lines starting with '#' are ignored silently.<br>
 * A '&' indicates that the next character can be used as a button mnemonic (write
 * "&&" to display a '&').</p>
 * 
 * <p>The default (en) language file has the name "lang.properties".<br>
 * Translated language files should have a name of the kind "lang_LANGUAGE.properties",
 * where LANGUAGE should be one of:
 * <ul>
 * <li>lang_language.properties (ex: lang_pt.properties)</li>
 * <li>lang_language_COUNTRY.properties (ex: lang_pt_PT.properties)</li>
 * <li>lang_language_COUNTRY_variation.properties (ex: lang_pt_PT_ao.properties)</li>
 * </ul>
 * </p>
 * 
 * @author Bruno Nova
 */
public class Lang {
	/** The path to the language files, with the trailing slash. */
	public static final String FILENAME_PATH = "lang";
	/** The file extension of the language files. */
	public static final String FILENAME_EXTENSION = "properties";
	/** The base name of the language files. */
	public static final String FILENAME_BASE_NAME = "lang";
	/** The file prefix of the language files. */
	public static final String FILENAME_PREFIX = FILENAME_BASE_NAME + "_";
	/** The default/fallback language. */
	public static final String DEFAULT_LANGUAGE = "en";
	/** The character that prepends the char to be considered the mnemonic char. */
	public static final char MNEMONIC_CHAR = '&';
	/** The character that can indicate a string argument, if followed by a number starting from 1. */
	public static final char ARG_CHAR = '#';
	
	/** The loaded locale. */
	private static Locale locale;
	/** The locale of the sistem. */
	private static Locale systemLocale;
	/** Set of available languages (filled on the first call to <tt>getAvailableLanguages()</tt>). */
	private static Set<String> availableLanguages;
	/** Class loader to load the language files. */
	private static ClassLoader loader;
	/** The resource bundle that contains the translated strings. */
	private static ResourceBundle strings;
	
	/** Class logger. */
	private static final Logger LOG = Logger.getLogger(Lang.class.getName());
	
	/**
	 * Loads the strings of the specified language.
	 * @param language Language (just the name part of the file name).
	 * @throws Exception If an error occurred opening or reading the language file.
	 */
	public static void load(String language) throws Exception {
		if(loader == null) {
			File file = new File(DrMIPS.path + File.separator + FILENAME_PATH);
			URL[] urls = new URL[] {file.toURI().toURL()};
			loader = new URLClassLoader(urls);
			setDefaultLanguage(DEFAULT_LANGUAGE); // set default language
			LOG.log(Level.INFO, "language files are in: {0}", file);
		}
		
		// Load the strings
		locale = getLocaleForLanguage(language);
		strings = ResourceBundle.getBundle(FILENAME_BASE_NAME, locale, loader);
		
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
	 * Tries to load the user preferred language (defined in the preferences).
	 * If it fails, tries to load the default language.
	 * @return Whether the language was loaded successfully.
	 */
	public static boolean loadPreferredLanguage() {
		try {
			String prefLang = DrMIPS.prefs.get(DrMIPS.LANG_PREF, "");
			Locale loc = getSystemLocale();
			String sysLangLCV = loc.getLanguage() + "_" + loc.getCountry() + "_" + loc.getVariant();
			String sysLangLC = loc.getLanguage() + "_" + loc.getCountry();
			String sysLangL = loc.getLanguage();
			
			// try language selected in the preferences
			if(isLanguageAvailable(prefLang)) {
				load(prefLang);
				return true;
			}
			// try system language with language, country and variant
			else if(isLanguageAvailable(sysLangLCV)) {
				load(sysLangLCV);
				DrMIPS.prefs.put(DrMIPS.LANG_PREF, sysLangLCV);
				return true;
			}
			// try system language with language and country
			else if(isLanguageAvailable(sysLangLC)) {
				load(sysLangLC);
				DrMIPS.prefs.put(DrMIPS.LANG_PREF, sysLangLC);
				return true;
			}
			// try system language with language only
			else if(isLanguageAvailable(sysLangL)) {
				load(sysLangL);
				DrMIPS.prefs.put(DrMIPS.LANG_PREF, sysLangL);
				return true;
			}
			// try default language
			else if(isLanguageAvailable(DEFAULT_LANGUAGE)) {
				load(DEFAULT_LANGUAGE);
				DrMIPS.prefs.put(DrMIPS.LANG_PREF, DEFAULT_LANGUAGE);
				return true;
			}
			else
				return false;
		}
		catch(Exception ex) {
			LOG.log(Level.SEVERE, "error loading preferred language", ex);
			return false;
		}
	}
	
	/**
	 * Returns the translated string from the resource bundle.
	 * If the string is not found, it returns the key converted to upper case
	 * and prints a warning to stderr.
	 * @param key Key of the string.
	 * @return The translated string.
	 */
	private static String getString(String key) {
		try {
			String str = strings.getString(key);
			return new String(str.getBytes("ISO-8859-1"), "UTF-8"); // convert to UTF-8 (strings are read in ISO-8859-1)
		}
		catch(Exception ex) { // translation not found
			LOG.log(Level.WARNING, "no translation for key \"" + key + "\"", ex);
			return key.toUpperCase();
		}
	}
	
	/**
	 * Returns the translated string for the specified key.
	 * <p>Any mnemonic characters are removed (unless the char is doubled).</p>
	 * @param key Key of the string to translate.
	 * @param args Optional string arguments values.
	 * @return String translated to the loaded language, or the key in upper case if not found.
	 */
	public static String t(String key, Object... args) {
		String s = getString(key);
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
		String s = getString(key);
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
	 * Returns the loaded locale.
	 * @return The loaded locale.
	 */
	public static Locale getLocale() {
		return locale;
	}
	
	/**
	 * Returns the loaded language name.
	 * @return The loaded language.
	 */
	public static String getLanguage() {
		return locale.toString();
	}
	
	/**
	 * Returns the display name for the specified locale.
	 * @param locale The desired locale.
	 * @return Display name for the specified locale in the current locale.
	 */
	public static String getDisplayName(Locale locale) {
		Locale currentLocale = (getLocale() != null) ? getLocale() : getSystemLocale();
		String name = locale.getDisplayName(currentLocale);
		return name.isEmpty() ? locale.toString() : name;
	}
	
	/**
	 * Returns the display name for the specified language.
	 * @param language The desired language.
	 * @return Display name for the specified language in the current locale.
	 */
	public static String getDisplayName(String language) {
		return getDisplayName(getLocaleForLanguage(language));
	}
	
	/**
	 * Returns the system locale.
	 * @return System locale.
	 */
	public static Locale getSystemLocale() {
		if(systemLocale == null)
			systemLocale = Locale.getDefault();
		return systemLocale;
	}
	
	/**
	 * Updates the default language.
	 * @param language New default language.
	 */
	private static void setDefaultLanguage(String language) {
		getSystemLocale(); // save system locale
		Locale.setDefault(getLocaleForLanguage(language));
	}
	
	/**
	 * Returns the correct Locale for the specified language name.
	 * @param language Desired language.
	 * @return Correct Locale for the language.
	 */
	public static Locale getLocaleForLanguage(String language) {
		String[] fields = language.split("_", 3);
		switch(fields.length) {
			case 0: return getSystemLocale(); // should not happen
			case 1: return new Locale(fields[0]);
			case 2: return new Locale(fields[0], fields[1]);
			default: return new Locale(fields[0], fields[1], fields[2]);
		}
	}
	
	/**
	 * Returns the set of available languages.
	 * @return Set of available languages.
	 */
	public static Set<String> getAvailableLanguages() {
		if(availableLanguages == null)
			fillAvailableLanguages();
		return availableLanguages;
	}
	
	/**
	 * Returns whether the specified language is available.
	 * @param language Language to query.
	 * @return <tt>true</tt> if the language is available.
	 */
	public static boolean isLanguageAvailable(String language) {
		return getAvailableLanguages().contains(language);
	}
	
	/**
	 * Fills <tt>availableLanguages</tt> with the list of available languages.
	 * This method is called automatically by <tt>getAvailableLanguages()</tt>.
	 */
	private static void fillAvailableLanguages() {
		availableLanguages = new TreeSet<>();
		availableLanguages.add(DEFAULT_LANGUAGE); // add default language
		
		// Find all the language files
		File langDir = new File(DrMIPS.path + File.separator + FILENAME_PATH);
		if(langDir.isDirectory()) {
			File[] files = langDir.listFiles();
			String name, lang;
			for(File f: files) {
				name = f.getName();
				if(name.startsWith(FILENAME_PREFIX) && name.endsWith("." + FILENAME_EXTENSION)) {
					// Add language
					lang = name.substring(FILENAME_PREFIX.length(), name.lastIndexOf("." + FILENAME_EXTENSION));
					availableLanguages.add(lang);
				}
			}
		}
	}
}
