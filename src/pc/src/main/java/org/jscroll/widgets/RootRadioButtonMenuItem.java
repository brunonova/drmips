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
 * This class creates a base radio button menu item. ActionListener, mnemonic,
 * keyboard shortcut, and title are set via the constructor.
 * <BR><BR>
 * A {@link org.jscroll.widgets.JScrollInternalFrame JScrollInternalFrame}
 * object may optionally be associated with an instance of this class.
 *
 * @author <a href="mailto:tessier@gabinternet.com">Tom Tessier</a>
 * @version 1.0  11-Aug-2001
 */
public class RootRadioButtonMenuItem extends JRadioButtonMenuItem
    implements FrameAccessorInterface {
    private JScrollInternalFrame associatedFrame;

    /**
     * creates the RootRadioButtonMenuItem with an associated frame. Used for
     * radio menu items that are associated with an internal frame.
     *
     * @param listener the action listener to assign
     * @param itemTitle the title of the item
     * @param mnemonic the mnemonic used to access the menu
     * @param shortcut the keyboard shortcut used to access the menu.
     *      -1 indicates no shortcut.
     * @param selected <code>boolean</code> that indicates whether
     *      the menu item is selected or not
     * @param associatedFrame the JScrollInternalFrame associated with the menu item
     */
    public RootRadioButtonMenuItem(ActionListener listener, String itemTitle,
        int mnemonic, int shortcut, boolean selected,
        JScrollInternalFrame associatedFrame) {
        this(listener, itemTitle, mnemonic, shortcut, selected);
        this.associatedFrame = associatedFrame;
    }

    /**
     * creates the RootRadioButtonMenuItem without an associated frame. Used
     * for generic radio button menu items.
     *
     * @param listener the action listener to assign
     * @param itemTitle the title of the item
     * @param mnemonic the mnemonic used to access the menu
     * @param shortcut the keyboard shortcut used to access the menu.
     *      -1 indicates no shortcut.
     * @param selected <code>boolean</code> that indicates whether
     *      the menu item is selected or not
     */
    public RootRadioButtonMenuItem(ActionListener listener, String itemTitle,
        int mnemonic, int shortcut, boolean selected) {
        super(itemTitle, selected);
        setMnemonic(mnemonic);

        // set the alt-Shortcut accelerator
        if (shortcut != -1) {
            setAccelerator(KeyStroke.getKeyStroke(shortcut, ActionEvent.ALT_MASK));
        }

        setActionCommand(itemTitle + "Radio");
        addActionListener(listener);
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
}
