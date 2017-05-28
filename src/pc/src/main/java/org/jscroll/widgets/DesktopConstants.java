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

import java.awt.Color;


/**
 * This interface provides a set of reusable constants for use by
 * other classes in the system.
 *
 * @author <a href="mailto:tessier@gabinternet.com">Tom Tessier</a>
 * @version 1.0  29-Jul-2001
 */
public interface DesktopConstants {
    // all variables declared here are automatically public static final

    /** maximum number of internal frames allowed */
    int MAX_FRAMES = 20;

    /** default x offset of first frame in cascade mode,
     *     relative to desktop */
    int X_OFFSET = 30;

    /** default y offset of first frame in cascade mode,
     *    relative to desktop */
    int Y_OFFSET = 30;

    /** minimum width of frame toolbar buttons */
    int MINIMUM_BUTTON_WIDTH = 30;

    /** maximum width of frame toolbar buttons */
    int MAXIMUM_BUTTON_WIDTH = 80;

    /** the foreground color of inactive buttons whose associated frame
       contents have changed */
    Color CONTENTS_CHANGED_COLOR = Color.red;
}
