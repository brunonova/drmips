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

import java.util.Properties;

/**
 * General information about the simulator, used in the graphical interfaces but
 * independent of the platform.
 *
 * @author Bruno Nova
 */
public class AppInfo {
	/** Name of the application. */
	public static final String NAME = "DrMIPS";
	/** Short description of the application. */
	public static final String DESCRIPTION = "Educational MIPS simulator";
	/** Version of the application. */
	public static final String VERSION;
	/** Homepage of the application. */
	public static final String HOMEPAGE = "http://brunonova.github.io/drmips/";
	/** Name of the main author (creator) of the application. */
	public static final String MAIN_AUTHOR_NAME = "Bruno Nova";
	/** E-mail address of the main author (creator) of the application. */
	public static final String MAIN_AUTHOR_EMAIL = "brunomb.nova@gmail.com";
	/** Name and e-mail address of the main author (creator) of the application. */
	public static final String MAIN_AUTHOR_NAME_EMAIL = MAIN_AUTHOR_NAME + " <" + MAIN_AUTHOR_EMAIL + ">";
	/** Institution of the main author (creator) of the application. */
	public static final String MAIN_AUTHOR_INSTITUTION = "Faculdade de Engenharia da Universidade do Porto";
	/** Copyright line(s) of the application. */
	public static final String COPYRIGHT = "Copyright (C) 2013-2015 " + MAIN_AUTHOR_NAME_EMAIL;
	/** License of the application. */
	public static final String LICENSE = NAME + " - " + DESCRIPTION + "\n" +
		COPYRIGHT + "\n" +
		"\n" +
		"This program is free software: you can redistribute it and/or modify\n" +
		"it under the terms of the GNU General Public License as published by\n" +
		"the Free Software Foundation, either version 3 of the License, or\n" +
		"(at your option) any later version.\n" +
		"\n" +
		"This program is distributed in the hope that it will be useful,\n" +
		"but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
		"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
		"GNU General Public License for more details.\n" +
		"\n" +
		"You should have received a copy of the GNU General Public License\n" +
		"along with this program.  If not, see <http://www.gnu.org/licenses/>.";
	/** Name and URL of the license of the application. */
	public static final String LICENSE_SHORT = "GPLv3 <http://www.gnu.org/licenses/gpl-3.0.html>";
	/** Authors and contributors of the application. */
	public static final String[] AUTHORS = {"António Araújo",
	                                        "Bruno Nova",
	                                        "João Canas Ferreira"};

	/**
	 * Returns the names of the authors/contributors of the application as text,
	 * one line per name.
	 * @return Authors as text.
	 */
	public static final String getAuthorsAsText() {
		String text = "";
		for(int i = 0; i < AUTHORS.length; i++) {
			if(i > 0) text += "\n";
			text += AUTHORS[i];
		}
		return text;
	}

    /**
     * Returns the version of the game.
     * <p>The version is obtained from the version.properties file generated
     * by Gradle.</p>
     * @return Game version.
     */
    static {
        // Read version from version.properties file
        String version = "N/A";

        Properties prop = new Properties();
        try {
            prop.load(AppInfo.class.getResourceAsStream("version.properties"));
            version = prop.getProperty("version", "N/A");
        } catch(Exception ex) {
            // ignore
        }

        VERSION = version;
    }
}
