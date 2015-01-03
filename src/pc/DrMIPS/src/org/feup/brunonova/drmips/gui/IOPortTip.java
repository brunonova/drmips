/*
    DrMIPS - Educational MIPS simulator
    Copyright (C) 2013-2015 Bruno Nova <ei08109@fe.up.pt>

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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
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
	private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 8);
	/** The color of the tip's text. */
	private static final Color TEXT_COLOR = Color.BLACK;
	/** The color of the tip's background. */
	private static final Color BACKGROUND_COLOR = new Color(255, 255, 128, 150);
	/** The color of the tip's border. */
	private static final Color BORDER_COLOR = new Color(128, 128, 0, 150);
	
	/**
	 * Constructor.
	 * @param text The initial text of the tip (value in the input/output?).
	 * @param tooltip The tooltip of the tip (identifier of the input/output?).
	 * @param x The x coordinate of the tip's position in the <tt>attachedComponent</tt>.
	 * @param y The y coordinate of the tip's position in the <tt>attachedComponent</tt>.
	 */
	public IOPortTip(String text, String tooltip, int x, int y) {
		super(text);
		setFont(FONT);
		setForeground(TEXT_COLOR);
		setBackground(BACKGROUND_COLOR);
		setOpaque(true);
		setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
		setToolTipText(tooltip);
		setLocation(x - 3, y + 1);
		setVerticalAlignment(SwingConstants.CENTER);
		resize();
	}

	@Override
	public void setText(String text) {
		super.setText(text);
		resize();
	}
	
	private void resize() {
		FontMetrics fm = getFontMetrics(FONT);
		int w = fm.stringWidth(getText()) + 4;
		int h = fm.getHeight() + 4;
		setSize(w, h);
	}
}
