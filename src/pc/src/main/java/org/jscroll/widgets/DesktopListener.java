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

import java.awt.event.*;


/**
 * This class provides common Component and Action Listeners for
 * other objects in the system.
 *
 * @author <a href="mailto:tessier@gabinternet.com">Tom Tessier</a>
 * @version 1.0  11-Aug-2001
 */
public class DesktopListener implements ComponentListener, ActionListener {
    private DesktopMediator desktopMediator;

    /**
     * creates the DesktopListener object.
     *
     * @param desktopMediator a reference to the DesktopMediator object
     */
    public DesktopListener(DesktopMediator desktopMediator) {
        this.desktopMediator = desktopMediator;
    }

    ///
    // respond to component events...
    ///

    /**
     * updates the preferred size of the desktop when either an internal frame
     * or the scrollable desktop pane itself is resized
     *
     * @param e the ComponentEvent
     */
    public void componentResized(ComponentEvent e) {
        desktopMediator.resizeDesktop();
    }

    /**
     * revalidates the desktop to ensure the viewport has the proper
     * height/width settings when a new component is shown upon the desktop
     *
     * @param e the ComponentEvent
     */
    public void componentShown(ComponentEvent e) {
        desktopMediator.revalidateViewport();
    }

    /**
     * updates the preferred size of the desktop when a component is moved
     *
     * @param e the ComponentEvent
     */
    public void componentMoved(ComponentEvent e) {
        desktopMediator.resizeDesktop();
    }

    /**
     * interface placeholder
     *
     * @param e the ComponentEvent
     */
    public void componentHidden(ComponentEvent e) {
    }

    ///
    // respond to action events...
    ///

    /**
     * common actionPerformed method that responds to both button
     * and menu events.
     * If no action command provided in the ActionEvent, selects
     * the frame associated with the current button / menu item (if any).
     *
     * @param e the ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        String actionCmd = e.getActionCommand();

        if (actionCmd.equals("Tile")) {
            desktopMediator.tileInternalFrames();
        } else if (actionCmd.equals("Cascade")) {
            desktopMediator.cascadeInternalFrames();
        } else if (actionCmd.equals("Close")) {
            desktopMediator.closeSelectedFrame();
        }
        else if (actionCmd.equals("TileRadio")) {
            desktopMediator.setAutoTile(true);
        } else if (actionCmd.equals("CascadeRadio")) {
            desktopMediator.setAutoTile(false);
        }
        else { // no action command? 
               // then select the associated frame (if any)

            JScrollInternalFrame associatedFrame = ((FrameAccessorInterface) e.getSource()).getAssociatedFrame();

            if (associatedFrame != null) {
                associatedFrame.selectFrameAndAssociatedButtons();
                desktopMediator.centerView(associatedFrame);
            }
        }
    }
}
