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

import org.jscroll.components.*;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * This class provides the resizable toolbar for the scrollable desktop.
 *
 * @author <a href="mailto:tessier@gabinternet.com">Tom Tessier</a>
 * @version 1.0  11-Aug-2001
 */
public class DesktopResizableToolBar extends ResizableToolBar
    implements DesktopConstants, ActionListener {
    private DesktopMediator desktopMediator;

    /**
     * creates the DesktopResizableToolBar object
     *
     * @param desktopMediator a reference to the DesktopMediator object
     */
    public DesktopResizableToolBar(DesktopMediator desktopMediator) {
        super(MINIMUM_BUTTON_WIDTH, MAXIMUM_BUTTON_WIDTH);

        this.desktopMediator = desktopMediator;

        // prepare test button
        RootToggleButton testButton = new RootToggleButton("test");

        // now add a button-sized separator to the toolBar so that
        // the layout manager can properly setup
        addSeparator(new Dimension(0, testButton.getMinimumSize().height));
    }

    /**
     * creates a RootToggleButton and adds it to the toolbar
     *
     * @param title the title of the toggle button
     *
     * @return the toggle button that was created
     */
    public RootToggleButton add(String title) {
        RootToggleButton toolButton = new RootToggleButton(" " + title + " ");
        toolButton.addActionListener(this);

        super.add(toolButton);

        return toolButton;
    }

    /**
     * propogates actionPerformed button event to DesktopMediator
     *
     * @param e the ActionEvent to propogate
     */
    public void actionPerformed(ActionEvent e) {
        desktopMediator.actionPerformed(e);
    }
}
