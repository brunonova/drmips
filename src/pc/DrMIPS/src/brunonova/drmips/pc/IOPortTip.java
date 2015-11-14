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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * The tip that is shown in the datapath for inputs/outputs.
 * 
 * @author Bruno Nova
 */
public class IOPortTip extends JLabel {
	/** The font used for the text. */
	private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 9);
	/** The color of the tip's text. */
	private static final Color TEXT_COLOR = Color.BLACK;
	/** The color of the tip's background. */
	private static final Color BACKGROUND_COLOR = new Color(255, 255, 128, 150);
	/** The color of the tip's border. */
	private static final Color BORDER_COLOR = new Color(128, 128, 0, 150);

	/** Identifier of the input/output. */
	private final String id;
	
	/**
	 * Constructor.
	 * @param id Identifier of the input/output.
	 * @param value Value in the input/output.
	 * @param x The x coordinate of the tip's position in the <tt>attachedComponent</tt>.
	 * @param y The y coordinate of the tip's position in the <tt>attachedComponent</tt>.
	 */
	public IOPortTip(String id, String value, int x, int y) {
		this(value, id);
		setLocation(x, y);
	}

	/**
	 * Constructor.
	 * @param id Identifier of the input/output.
	 * @param value Value in the input/output.
	 */
	public IOPortTip(String id, String value) {
		super();
		this.id = id;
		setFont(FONT);
		setForeground(TEXT_COLOR);
		setBackground(BACKGROUND_COLOR);
		setOpaque(true);
		setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
		setVerticalAlignment(SwingConstants.CENTER);
		setValue(value);
	}

	@Override
	public final void setLocation(int x, int y) {
		// Shift the tip 3 pixels left and 1 pixel down.
		super.setLocation(x - 3, y + 1);
	}

	@Override
	public final void setLocation(Point p) {
		setLocation(p.x, p.y);
	}

	/**
	 * Updates the value of the tip.
	 * @param value New value (as a String formatted in bin/dec/hex).
	 * @param showName Whether to display the name of the input/output as well.
	 */
	public final void setValue(String value, boolean showName) {
		if(showName)
			setText(id + ": " + value);
		else
			setText(value);
		setToolTipText(id + ": " + value);
		resize();
	}

	/**
	 * Updates the value of the tip.
	 * @param value New value (as a String formatted in bin/dec/hex).
	 */
	public final void setValue(String value) {
		setValue(value, false);
	}
	
	private void resize() {
		FontMetrics fm = getFontMetrics(FONT);
		int w = fm.stringWidth(getText()) + 4;
		int h = fm.getHeight() + 4;
		setSize(w, h);
	}
}
