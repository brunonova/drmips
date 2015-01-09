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

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.DesktopIconUI;

import java.awt.*;


/**
 * This class provides an empty DesktopIconUI for
 * {@link org.jscroll.widgets.RootDesktopPane RootDesktopPane}.
 *
 * @author <a href="mailto:tessier@gabinternet.com">Tom Tessier</a>
 * @version 1.0  29-Jul-2001
 */
public class EmptyDesktopIconUI extends DesktopIconUI {
    /**
     * stores the instance of this class. Used by
     * {@link org.jscroll.widgets.EmptyDesktopIconUI#createUI(JComponent)
     * createUI}.
     */
    protected static EmptyDesktopIconUI desktopIconUI;

    /**
     * creates the EmptyDesktopIconUI object
     *
     * @param c the reference to the JComponent object required by createUI
     */
    public static ComponentUI createUI(JComponent c) {
        if (desktopIconUI == null) {
            desktopIconUI = new EmptyDesktopIconUI();
        }

        return desktopIconUI;
    }

    /**
     * overrides the paint method with a blank routine so that no
     * component is displayed when an internal frame is iconified
     *
     * @param g the reference to the Graphics object used to paint the desktop
     */
    protected void paint(Graphics g) {
    }
}
