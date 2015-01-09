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
 * This class creates a generic base menu item. ActionListener, mnemonic,
 * keyboard shortcut, and title are set via the constructor.
 *
 * @author <a href="mailto:tessier@gabinternet.com">Tom Tessier</a>
 * @version 1.0  29-Jan-2001
 */
public class RootMenuItem extends JMenuItem {
    /**
     * creates the RootMenuItem
     *
     * @param listener the action listener to assign
     * @param itemTitle the title of the item
     * @param mnemonic the mnemonic used to access the menu
     * @param shortcut the keyboard shortcut used to access the menu.
     *      -1 indicates no shortcut.
     */
    public RootMenuItem(ActionListener listener, String itemTitle,
        int mnemonic, int shortcut) {
        super(itemTitle, mnemonic);

        // set the alt-Shortcut accelerator
        if (shortcut != -1) {
            setAccelerator(KeyStroke.getKeyStroke(shortcut, ActionEvent.ALT_MASK));
        }

        setActionCommand(itemTitle);
        addActionListener(listener);
    }
}
