/*
 * JScroll - the scrollable desktop pane for Java.
 * Copyright (C) 2003 Tom Tessier
 *               2015 Bruno Nova
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

package org.jscroll;

import org.jscroll.widgets.*;

import javax.swing.*;

import java.awt.BorderLayout;


/**
 * The main scrollable desktop class.
 * <BR><BR>
 * JScrollDesktopPane builds upon JDesktopPane and JScrollPane to provide
 * a complete virtual desktop environment that enables easy access to internal
 * frames that may have been positioned offscreen. This access is made
 * possible via real-time creation and manipulation of the desktop preferred size.
 * <BR><BR>
 * A toolbar provides a set of buttons along the top of the screen, with each
 * button matched to a corresponding internal frame. When one of these buttons
 * is clicked, the associated frame is centered upon the virtual desktop and
 * selected. The buttons within the toolbar automatically resize as more buttons
 * are added beyond the width of the container.
 * <BR><BR>
 * A JMenuBar may be registered with the scrollable desktop so that the
 * application can provide access to the internal frames via its own menu bar.
 * When the registration is complete, a new JMenu entitled "Window" is added to
 * the supplied JMenuBar, a menu containing <code>Tile</code>,
 * <code>Cascade</code>, and <code>Close</code> options along with dynamically
 * updated shortcuts to any internal frames currently upon the scrollable
 * desktop. The <code>Tile</code> and <code>Cascade</code> options provided by
 * the "Window" menu affect the positions of the internal frames upon the
 * scrollable desktop. <code>Cascade</code> positions each internal frame one
 * after the other in a diagonal sequence crosswise the screen, while
 * <code>Tile</code> positions and resizes the internal frames to fill up
 * all available screen real estate, with no single frame overlapping any other.
 * <BR><BR>
 * JScrollDesktopPane is simply a JPanel and as such may be added to any
 * suitable JPanel container, such as a JFrame. The addition of new internal
 * frames to the JScrollDesktopPane and the registration of menu bars for
 * use by the scrollable desktop is relatively simple: The <code>add</code>
 * method creates a new internal frame and returns a reference to the
 * JInternalFrame instance that was created, while the
 * <code>registerMenuBar</code> method registers the menubar for use by the
 * scrollable desktop. A JMenuBar object may also be registered by passing it
 * as a constructor parameter to the JScrollDesktopPane.
 * <BR><BR>
 * An example usage follows:
 * <BR><BR>
 * <code><pre>
 *    JFrame f = new JFrame("Scrollable Desktop");
 *    f.setSize(300,300);
 *    // prepare the menuBar
 *    JMenuBar menuBar = new JMenuBar();
 *    f.setJMenuBar(menuBar);
 *
 *    // create the scrollable desktop instance and add it to the JFrame
 *    JScrollDesktopPane scrollableDesktop =
 *          new JScrollDesktopPane(menuBar);
 *    f.getContentPane().add(scrollableDesktop);
 *    f.setVisible(true);
 *
 *    // add a frame to the scrollable desktop
 *    JPanel frameContents = new JPanel();
 *    frameContents.add(
 *          new JLabel("Hello and welcome to JScrollDesktopPane."));
 *
 *    scrollableDesktop.add(frameContents);
 * </pre></code>
 *
 * JScrollDesktopPane has been tested under Java 2 JDK versions
 * 1.3.1-b24 on Linux and jdk1.3.0_02 on Windows and Intel Solaris.
 *
 * As of March 14, 2003 it has also been tested on JDK 1.4.1 for Windows.
 *
 * @author <a href="mailto:tessier@gabinternet.com">Tom Tessier</a>
 * @version 1.0  12-Aug-2001
 */
public class JScrollDesktopPane extends JPanel implements DesktopConstants {
    private static int count; // count used solely to name untitled frames
    private DesktopMediator desktopMediator;
    private ImageIcon defaultFrameIcon;

    /**
     * creates the JScrollDesktopPane object, registers a menubar, and assigns
     *      a default internal frame icon.
     *
     * @param mb the menubar with which to register the scrollable desktop
     * @param defaultFrameIcon the default icon to use within the title bar of
     *      internal frames.
     */
    public JScrollDesktopPane(JMenuBar mb, ImageIcon defaultFrameIcon) {
        this();
        registerMenuBar(mb);
        this.defaultFrameIcon = defaultFrameIcon;
    }

    /**
     * creates the JScrollDesktopPane object and registers a menubar.
     *
     * @param mb the menubar with which to register the scrollable desktop
     */
    public JScrollDesktopPane(JMenuBar mb) {
        this();
        registerMenuBar(mb);
    }

    /**
     * creates the JScrollDesktopPane object.
     */
    public JScrollDesktopPane() {
        setLayout(new BorderLayout());
        desktopMediator = new DesktopMediator(this);
    }

    /**
     * adds an internal frame to the scrollable desktop
     *
     * @param frameContents the contents of the internal frame
     *
     * @return the JScrollInternalFrame that was created
     */
    public JScrollInternalFrame add(JPanel frameContents) {
        return add("Untitled " + count++, defaultFrameIcon, frameContents,
            true, -1, -1);
    }

    /**
     * adds an internal frame to the scrollable desktop
     *
     * @param title the title displayed in the title bar of the internal frame
     * @param frameContents the contents of the internal frame
     *
     * @return the JScrollInternalFrame that was created
     */
    public JScrollInternalFrame add(String title, JPanel frameContents) {
        return add(title, defaultFrameIcon, frameContents, true, -1, -1);
    }

    /**
     * adds an internal frame to the scrollable desktop
     *
     * @param title the title displayed in the title bar of the internal frame
     * @param frameContents the contents of the internal frame
     * @param isClosable <code>boolean</code> indicating whether internal frame
     *          is closable
     *
     * @return the JScrollInternalFrame that was created
     */
    public JScrollInternalFrame add(String title, JPanel frameContents,
        boolean isClosable) {
        return add(title, defaultFrameIcon, frameContents, isClosable, -1, -1);
    }

    /**
     * adds an internal frame to the scrollable desktop
     *
     * @param title the title displayed in the title bar of the internal frame
     * @param icon the icon displayed in the title bar of the internal frame
     * @param frameContents the contents of the internal frame
     * @param isClosable <code>boolean</code> indicating whether internal frame
     *          is closable
     *
     * @return the JScrollInternalFrame that was created
     */
    public JScrollInternalFrame add(String title, ImageIcon icon,
        JPanel frameContents, boolean isClosable) {
        return add(title, icon, frameContents, isClosable, -1, -1);
    }

    /**
     * adds an internal frame to the scrollable desktop.
     * <BR><BR>
     * Propogates the call to DesktopMediator.
     *
     * @param title the title displayed in the title bar of the internal frame
     * @param icon the icon displayed in the title bar of the internal frame
     * @param frameContents the contents of the internal frame
     * @param isClosable <code>boolean</code> indicating whether internal frame
     *          is closable
     * @param x x coordinates of internal frame within the scrollable desktop.
     * @param y y coordinates of internal frame within the scrollable desktop
     *
     * @return the JScrollInternalFrame that was created
     */
    public JScrollInternalFrame add(String title, ImageIcon icon,
        JPanel frameContents, boolean isClosable, int x, int y) {
        return desktopMediator.add(title, icon, frameContents, isClosable, x, y);
    }

    /**
     * adds a JInternalFrame to the scrollable desktop.
     *
     * @param f the internal frame of class JScrollInternalFrame to add
     */
    public void add(JInternalFrame f) {
        add(getWrappedFrame(f), -1, -1);
    }

    /**
     * adds a JInternalFrame to the scrollable desktop.
     *
     * @param f the internal frame of class JScrollInternalFrame to add
     * @param x x coordinates of internal frame within the scrollable desktop.
     * @param y y coordinates of internal frame within the scrollable desktop
     */
    public void add(JInternalFrame f, int x, int y) {
        desktopMediator.add(getWrappedFrame(f), x, y);
    }

    /**
     *  wraps a given internal frame in a JScrollInternalFrame for use
     *  by JScrollDesktopPane
     *
     * @param f the JInternalFrame reference
     *
     * @return
     */
    private JScrollInternalFrame getWrappedFrame(JInternalFrame f) {
        // wrap it in a JScrollInternalFrame
        JScrollInternalFrame b = new JScrollInternalFrame();
        b.setContentPane(f.getContentPane());
        b.setTitle(f.getTitle());
        b.setResizable(f.isResizable());
        b.setClosable(f.isClosable());
        b.setMaximizable(f.isMaximizable());
        b.setIconifiable(f.isIconifiable());
        b.setFrameIcon(f.getFrameIcon());
        b.pack();
        b.saveSize();

        b.setVisible(f.isVisible());

        return b;
    }

    /**
     * removes the specified internal frame from the scrollable desktop
     *
     * @param f the internal frame to remove
     */
    public void remove(JInternalFrame f) {
        f.doDefaultCloseAction();
    }

    /**
     * registers a menubar to which the "Window" menu may be applied.
     * <BR><BR>
     * Propogates the call to DesktopMediator.
     *
     * @param mb the menubar to register
     */
    public void registerMenuBar(JMenuBar mb) {
        desktopMediator.registerMenuBar(mb);
    }

    /**
     * registers a default icon for display in the title bars of
     *    internal frames
     *
     * @param defaultFrameIcon the default icon
     */
    public void registerDefaultFrameIcon(ImageIcon defaultFrameIcon) {
        this.defaultFrameIcon = defaultFrameIcon;
    }

    /**
     * returns the internal frame currently selected upon the
     * virtual desktop.
     * <BR><BR>
     * Propogates the call to DesktopMediator.
     *
     * @return a reference to the active JInternalFrame
     */
    public JInternalFrame getSelectedFrame() {
        return desktopMediator.getSelectedFrame();
    }

    /**
     * selects the specified internal frame upon the virtual desktop.
     * <BR><BR>
     * Propogates the call to DesktopMediator.
     *
     * @param f the internal frame to select
     */
    public void setSelectedFrame(JInternalFrame f) {
        desktopMediator.setSelectedFrame(f);
    }

    /**
     *  flags the specified internal frame as "contents changed." Used to
     * notify the user when the contents of an inactive internal frame
     * have changed.
     * <BR><BR>
     * Propogates the call to DesktopMediator.
     *
     * @param f the internal frame to flag as "contents changed"
     */
    public void flagContentsChanged(JInternalFrame f) {
        desktopMediator.flagContentsChanged(f);
    }

	/**
	 * Tiles all of the internal frames.
	 */
	public void tileInternalFrames() {
		desktopMediator.tileInternalFrames();
	}

	/**
	 * Cascades all of the internal frames.
	 */
	public void cascadeInternalFrames() {
		desktopMediator.cascadeInternalFrames();
	}
}
