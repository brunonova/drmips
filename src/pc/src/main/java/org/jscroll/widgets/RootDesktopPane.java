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


/**
 * This class provides a custom desktop pane.
 * The drag mode is set to
 * {@link javax.swing.JDesktopPane#OUTLINE_DRAG_MODE outline}
 * by default, the desktop manager is
 * set to {@link org.jscroll.widgets.JScrollDesktopManager
 * JScrollDesktopManager}, and the look and feel DesktopIconUI is
 * replaced by the blank icon generator,
 * {@link org.jscroll.widgets.EmptyDesktopIconUI EmptyDesktopIconUI}.
 *
 * @author <a href="mailto:tessier@gabinternet.com">Tom Tessier</a>
 * @version 1.0  9-Aug-2001
 */
public class RootDesktopPane extends JDesktopPane {
    private DesktopScrollPane desktopScrollpane;

    /**
     *  creates the RootDesktopPane
     *
     * @param desktopScrollpane a reference to DesktopScrollPane
     */
    public RootDesktopPane(DesktopScrollPane desktopScrollpane) {
        this.desktopScrollpane = desktopScrollpane;

        // setup the UIManager to replace the look and feel DesktopIconUI
        // with an empty one (EmptyDesktopIconUI) so that the desktop icon 
        // for the internal frame is not painted
        // (ie: when internal frame iconified...) 
        UIDefaults defaults = UIManager.getDefaults();
        defaults.put("DesktopIconUI",
            getClass().getPackage().getName() + ".EmptyDesktopIconUI");

        // set up some defaults
        setDesktopManager(new JScrollDesktopManager(this));

        // pre-1.3 code (has no effect in JFC implementations before Swing 1.1.1 Beta 1)
        //      putClientProperty("JDesktopPane.dragMode", "outline"); 
        //
        // replace the following line with the above to execute under JDK 1.2
        setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
    }

    /**
     * returns the view rectangle associated with the
     * {@link org.jscroll.widgets.DesktopScrollPane DesktopScrollPane}
     * viewport
     *
     * @return the Rectangle object of the viewport
     */
    public Rectangle getScrollPaneRectangle() {
        return desktopScrollpane.getViewport().getViewRect();
    }

    /**
     * propogates the removeAssociatedComponents() call to
     * {@link org.jscroll.widgets.DesktopScrollPane DesktopScrollPane}
     *
     * @param f the internal frame whose associated components
     *         are to be removed
     */
    public void removeAssociatedComponents(JScrollInternalFrame f) {
        desktopScrollpane.removeAssociatedComponents(f);
    }

    /**
     * propogates the resizeDesktop() call to
     * {@link org.jscroll.widgets.DesktopScrollPane DesktopScrollPane}
     */
    public void resizeDesktop() {
        desktopScrollpane.resizeDesktop();
    }
}
