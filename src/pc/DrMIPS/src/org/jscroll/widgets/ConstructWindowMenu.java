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

import java.awt.event.*;


/**
 * This class constructs the "Window" menu items for use by
 * {@link org.jscroll.widgets.DesktopMenu DesktopMenu}.
 *
 * @author <a href="mailto:tessier@gabinternet.com">Tom Tessier</a>
 * @version 1.0  11-Aug-2001
 */
public class ConstructWindowMenu implements ActionListener {
    private DesktopMediator desktopMediator;

    /**
     * creates the ConstructWindowMenu object.
     *
     * @param sourceMenu the source menu to apply the menu items
     * @param desktopMediator a reference to the DesktopMediator
     * @param tileMode the current tile mode (tile or cascade)
     */
    public ConstructWindowMenu(JMenu sourceMenu,
        DesktopMediator desktopMediator, boolean tileMode) {
        this.desktopMediator = desktopMediator;
        constructMenuItems(sourceMenu, tileMode);
    }

    /**
     * constructs the actual menu items.
     *
     * @param sourceMenu the source menu to apply the menu items
     * @param tileMode the current tile mode
     */
    private void constructMenuItems(JMenu sourceMenu, boolean tileMode) {
        sourceMenu.add(new RootMenuItem(this, "Tile", KeyEvent.VK_T, -1));
        sourceMenu.add(new RootMenuItem(this, "Cascade", KeyEvent.VK_C, -1));
        sourceMenu.addSeparator();

        JMenu autoMenu = new JMenu("Auto");
        autoMenu.setMnemonic(KeyEvent.VK_U);

        ButtonGroup autoMenuGroup = new ButtonGroup();
        JRadioButtonMenuItem radioItem = new RootRadioButtonMenuItem(this,
                "Tile", KeyEvent.VK_T, -1, tileMode);
        autoMenu.add(radioItem);
        autoMenuGroup.add(radioItem);

        radioItem = new RootRadioButtonMenuItem(this, "Cascade", KeyEvent.VK_C,
                -1, !tileMode);
        autoMenu.add(radioItem);
        autoMenuGroup.add(radioItem);

        sourceMenu.add(autoMenu);
        sourceMenu.addSeparator();

        sourceMenu.add(new RootMenuItem(this, "Close", KeyEvent.VK_S,
                KeyEvent.VK_Z));
        sourceMenu.addSeparator();
    }

    /**
     * propogates actionPerformed menu event to the DesktopMediator reference
     *
     * @param e the ActionEvent to propogate
     */
    public void actionPerformed(ActionEvent e) {
        desktopMediator.actionPerformed(e);
    }
}
