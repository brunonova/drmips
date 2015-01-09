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

import java.awt.*;

import java.beans.PropertyVetoException;


/**
 * This class provides a custom desktop manager for
 * {@link org.jscroll.widgets.RootDesktopPane RootDesktopPane}.
 *
 * @author <a href="mailto:tessier@gabinternet.com">Tom Tessier</a>
 * @version 1.0  9-Aug-2001
 */
public class JScrollDesktopManager extends DefaultDesktopManager {
    private RootDesktopPane desktopPane;

    /**
     *  creates the JScrollDesktopManager
     *
     * @param desktopPane a reference to RootDesktopPane
     */
    public JScrollDesktopManager(RootDesktopPane desktopPane) {
        this.desktopPane = desktopPane;
    }

    /**
     * maximizes the internal frame to the viewport bounds rather
     * than the desktop bounds
     *
     * @param f the internal frame being maximized
     */
    public void maximizeFrame(JInternalFrame f) {
        Rectangle p = desktopPane.getScrollPaneRectangle();
        f.setNormalBounds(f.getBounds());
        setBoundsForFrame(f, p.x, p.y, p.width, p.height);

        try {
            f.setSelected(true);
        } catch (PropertyVetoException pve) {
            System.out.println(pve.getMessage());
        }

        removeIconFor(f);
    }

    /**
     * insures that the associated toolbar and menu buttons of
     * the internal frame are activated as well
     *
     * @param f the internal frame being activated
     */
    public void activateFrame(JInternalFrame f) {
        super.activateFrame(f);
        ((JScrollInternalFrame) f).selectFrameAndAssociatedButtons();
    }

    /**
     * closes the internal frame and removes any associated button
     * and menu components
     *
     * @param f the internal frame being closed
     */
    public void closeFrame(JInternalFrame f) {
        super.closeFrame(f);

        // possible to retrieve the associated buttons right here via 
        // f.getAssociatedButton(), and then with a call to getParent() the item 
        // can be directly removed from its parent container, but I find the 
        // below message propogation to DesktopPane a cleaner implementation...
        desktopPane.removeAssociatedComponents((JScrollInternalFrame) f);
        desktopPane.resizeDesktop();
    }

    /* could override iconifyFrame here as well, but much simpler
       to define an EmptyDesktopIconUI look and feel class in RootDesktopPane */
}
