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


/**
 * This interface exposes the accessor and mutator (getter and setter) methods
 * required to get and set the internal frame associated with an implementing class.
 * Used by {@link org.jscroll.widgets.DesktopListener DesktopListener}
 * to abstract access to the menu and toggle buttons that implement this interface.
 *
 * @author <a href="mailto:tessier@gabinternet.com">Tom Tessier</a>
 * @version 1.0  2-Aug-2001
 */
public interface FrameAccessorInterface {
    /**
     *  returns the associated frame
     *
     * @return the JScrollInternalFrame associated with the object
     */
    JScrollInternalFrame getAssociatedFrame();

    /**
     *  sets the associated frame
     *
     * @param associatedFrame the JScrollInternalFrame to associate with
     *    the object
     */
    void setAssociatedFrame(JScrollInternalFrame associatedFrame);
}
