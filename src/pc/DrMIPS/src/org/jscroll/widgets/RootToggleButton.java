/*
 * JScroll - the scrollable desktop pane for Java.
 * Copyright (C) 2003 Tom Tessier
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.jscroll.widgets;

import javax.swing.JToggleButton;

import java.awt.*;


/**
 * This class creates a base toggle button. A
 * {@link org.jscroll.widgets.JScrollInternalFrame JScrollInternalFrame}
 * object is associated with every instance of this class.
 *
 * @author <a href="mailto:tessier@gabinternet.com">Tom Tessier</a>
 * @version 1.0  11-Aug-2001
 */
public class RootToggleButton extends JToggleButton implements DesktopConstants,
    FrameAccessorInterface {
    private JScrollInternalFrame associatedFrame;
    private Color defaultColor;

    /**
     * creates the RootToggleButton
     *
     * @param title the title of the button
     */
    public RootToggleButton(String title) {
        super(title);

        setButtonFormat();
        setToolTipText(title);

        defaultColor = getForeground();
    }

    private void setButtonFormat() {
        Font buttonFont = getFont();
        setFont(new Font(buttonFont.getFontName(), buttonFont.getStyle(),
                buttonFont.getSize() - 1));
        setMargin(new Insets(0, 0, 0, 0));
    }

    /**
     *  sets the associated frame
     *
     * @param associatedFrame the JScrollInternalFrame object to associate with
     * the menu item
     */
    public void setAssociatedFrame(JScrollInternalFrame associatedFrame) {
        this.associatedFrame = associatedFrame;
    }

    /**
     *  returns the associated frame
     *
     * @return the JScrollInternalFrame object associated with this menu item
     */
    public JScrollInternalFrame getAssociatedFrame() {
        return associatedFrame;
    }

    /**
     *  flags the contents as "changed" by setting the foreground color to
     * {@link
     * org.jscroll.widgets.DesktopConstants#CONTENTS_CHANGED_COLOR
     * CONTENTS_CHANGED_COLOR}.
     * Used to notify the user when the contents of an inactive internal frame
     * have changed.
     *
     * @param changed <code>boolean</code> indicating whether contents have
     * changed
     */
    public void flagContentsChanged(boolean changed) {
        if (changed) {
            setForeground(CONTENTS_CHANGED_COLOR);
        } else {
            setForeground(defaultColor);
        }
    }
}
