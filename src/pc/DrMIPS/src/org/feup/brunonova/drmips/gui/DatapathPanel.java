/*
    DrMIPS - Educational MIPS simulator
    Copyright (C) 2013-2015 Bruno Nova <brunomb.nova@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.feup.brunonova.drmips.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import org.feup.brunonova.drmips.simulator.mips.CPU;
import org.feup.brunonova.drmips.simulator.mips.Component;
import org.feup.brunonova.drmips.simulator.mips.Output;
import org.feup.brunonova.drmips.simulator.mips.components.Concatenator;
import org.feup.brunonova.drmips.simulator.mips.components.Distributor;
import org.feup.brunonova.drmips.simulator.mips.components.Fork;
import org.feup.brunonova.drmips.simulator.util.Dimension;
import org.feup.brunonova.drmips.simulator.util.Point;

/**
 * Special JPanel that handles the display of the CPU datapath.
 * 
 * @author Bruno Nova
 */
public class DatapathPanel extends JLayeredPane {
	/** The main window where the datapath is. */
	private FrmSimulator parent = null;
	/** The CPU being displayed. */
	private CPU cpu = null;
	/** The graphical components. */
	private Map<String, DatapathComponent> components;
	/** The wires that connect the graphical components. */
	private List<Wire> wires = null;
	/** The format of the data (<tt>Util.BINARYL_FORMAT_INDEX/Util.DECIMAL_FORMAT_INDEX/Util.HEXADECIMAL_FORMAT_INDEX</tt>). */
	private int dataFormat = DrMIPS.DEFAULT_DATAPATH_DATA_FORMAT;
	/** Whether the control path is visible. */
	private boolean controlPathVisible = true;
	/** Whether to show arrows in the wires. */
	private boolean showArrows = true;
	/** The information to show (data when <tt>false</tt> or performance when <tt>true</tt>). */
	private boolean performanceMode = false;
	/** Whether to display in/out tips. */
	private boolean showTips = true;
	
	/**
	 * Creates the panel.
	 */
	public DatapathPanel() {
		super();
	}
	
	/**
	 * Defins the main window where the datapath is.
	 * @param parent The main FrmSimulator window.
	 */
	public void setParent(FrmSimulator parent) {
		this.parent = parent;
	}
	
	/**
	 * Defines the CPU to be displayed, and displays it.
	 * @param cpu The CPU to be displayed.
	 */
	public void setCPU(CPU cpu) {
		removeTips();
		removeAll();
		components = new TreeMap<String, DatapathComponent>();
		wires = new LinkedList<Wire>();
		this.cpu = cpu;
		Dimension size = cpu.getSize();
		setLocation(0, 0);
		setPreferredSize(new java.awt.Dimension(size.width, size.height));
		
		// Add each component
		Component[] comps = cpu.getComponents();
		for(Component c: comps) {
			DatapathComponent comp = new DatapathComponent(this, c);
			components.put(c.getId(), comp);
			add(comp);
		}
		
		// Add wires
		for(Component c: comps) {
			for(Output out: c.getOutputs())
				if(out.isConnected())
					wires.add(new Wire(out));
		}
		
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	/**
	 * Returns the CPU being displayed.
	 * @return The CPU being displayed.
	 */
	public CPU getCPU() {
		return cpu;
	}
	
	/**
	 * "Refreshes" the datapath with the new values.
	 */
	public void refresh() {
		for(DatapathComponent comp: components.values())
			comp.refresh();
		for(Wire w: wires)
			w.refreshTips();
		repaint();
		parent.refreshStatistics(); // refresh the statistics dialog
	}
	
	/**
	 * Sets the control path elements visible or invisible.
	 * @param visible Whether to set the control path visible or not.
	 */
	public void setControlPathVisible(boolean visible) {
		this.controlPathVisible = visible;
		for(DatapathComponent c: components.values()) {
			if(c.getComponent().isInControlPath())
				c.setVisible(visible);
		}
		for(Wire w: wires)
			w.refreshTips();
		repaint();
	}
	
	/**
	 * Sets whether to show arrows on the wires.
	 * @param show Whether to show arrows on the wires.
	 */
	public void setShowArrows(boolean show) {
		this.showArrows = show;
		repaint();
	}
	
	/**
	 * Sets whether to show in/out tips..
	 * @param show Whether to show the tips or not.
	 */
	public void setShowTips(boolean show) {
		showTips = show;
		refresh();
	}
	
	/**
	 * Sets whether to show data (<tt>false</tt>) or performace (<tt>true</tt>) information.
	 * @param performanceMode The mode.
	 */
	public void setPerformanceMode(boolean performanceMode) {
		this.performanceMode = performanceMode;
		refresh();
		repaint();
	}
	
	/**
	 * Returns whether the datapath is in performance mode.
	 * @return The information to show (data when <tt>false</tt> or performance when <tt>true</tt>).
	 */
	public boolean isInPerformanceMode() {
		return performanceMode;
	}
	
	/**
	 * Translates the datapath's strings.
	 * <p><tt>dataFormat</tt> should be one of <tt>FrmSimulator.BINARY_FORMAT_INDEX</tt>,
	 * <tt>FrmSimulator.DECIMAL_FORMAT_INDEX</tt> or <tt>FrmSimulator.HEXADECIMAL_FORMAT_INDEX</tt>.</p>
	 * @param dataFormat The format of the data.
	 */
	public void translate(int dataFormat) {
		this.dataFormat = dataFormat;
		for(DatapathComponent comp: components.values())
			comp.refresh();
		for(Wire w: wires)
			w.refreshTips();
	}
	
	/**
	 * Returns the datapath's current displayed data format.
	 * @return Data format (<tt>FrmSimulator.BINARYL_FORMAT_INDEX/FrmSimulator.DECIMAL_FORMAT_INDEX/FrmSimulator.HEXADECIMAL_FORMAT_INDEX</tt>).
	 */
	public int getDataFormat() {
		return dataFormat;
	}
	
	/**
	 * Removes all balloon tips.
	 */
	private void removeTips() {
		if(wires != null) {
			for(Wire w: wires)
				w.removeTips();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		boolean dark = DrMIPS.prefs.getBoolean(DrMIPS.DARK_THEME_PREF, DrMIPS.DEFAULT_DARK_THEME);
		
		// Draw the wires
		if(wires != null) {
			for(Wire w: wires)
				w.paint(g, dark);
		}
	}

	/**
	 * Represents a graphical wire on the datapath.
	 */
	public class Wire {
		/** The respective output of a component. */
		private final Output out;
		/** The starting point of the wire. */
		private final Point start;
		/** The ending point of the wire. */
		private final Point end;
		/** The intermediate points of the wire. */
		private final List<Point> points;
		/** The tip for the output of the wire (if any). */
		private IOPortTip outTip = null;
		/** The tip for the input of the wire (if any). */
		private IOPortTip inTip = null;
		
		/**
		 * Creates a wire from an ouput.
		 * @param out Output of a component (that is connected to an input).
		 */
		public Wire(Output out) {
			this.out = out;
			start = out.getComponent().getOutputPosition(out);
			end = out.getConnectedInput().getComponent().getInputPosition(out.getConnectedInput());
			points = out.getIntermediatePoints();
			if(out.shouldShowTip())
				add(outTip = new IOPortTip("0", out.getId(), start.x, start.y), JLayeredPane.PALETTE_LAYER);
			if(out.isConnected() && out.getConnectedInput().shouldShowTip())
				add(inTip = new IOPortTip("0", out.getConnectedInput().getId(), end.x, end.y), JLayeredPane.PALETTE_LAYER);
		}
		
		/**
		 * Refreshes the values on the in/out tips (if any).
		 */
		public void refreshTips() {
			if(outTip != null) { 
				outTip.setText(Util.formatDataAccordingToFormat(out.getData(), dataFormat));
				outTip.setVisible(showTips && !performanceMode && (controlPathVisible || !out.isInControlPath()));
			}
			if(inTip != null && out.isConnected()) {
				inTip.setText(Util.formatDataAccordingToFormat(out.getConnectedInput().getData(), dataFormat));
				inTip.setVisible(showTips && !performanceMode && (controlPathVisible || !out.getConnectedInput().isInControlPath()));
			}
		}
		
		/**
		 * Removes the in/out tips (if any).
		 */
		public void removeTips() {
			if(outTip != null) {
				remove(outTip);
				outTip = null;
			}
			if(inTip != null) {
				remove(inTip);
				inTip = null;
			}
		}
		
		/**
		 * Draws the wire on the datapath.
		 * @param g The graphics context of the datapath panel.
		 * @param dark Whether the UI is using a dark theme.
		 */
		public void paint(Graphics g, boolean dark) {
			if(!out.isInControlPath() || controlPathVisible) {
				if(performanceMode && out.isInCriticalPath())
					g.setColor(Util.criticalPathColor);
				else if(!out.isRelevant() && (!performanceMode || cpu.isPerformanceInstructionDependent()))
					g.setColor(Util.irrelevantColor);
				else if(out.isInControlPath())
					g.setColor(Util.controlPathColor);
				else
					g.setColor(Util.wireColor);
				
				Point s = start;
				for(Point e: points) {
					g.drawLine(s.x, s.y, e.x, e.y);
					s = e;
				}
				g.drawLine(s.x, s.y, end.x, end.y);
				if(showArrows)
					drawArrowTip(g, s.x, s.y, end.x, end.y, 6);
			}
		}
		
		/**
		 * Draws the arrow tip for the wire.
		 * @param g The graphics context of the datapath panel.
		 * @param startx The x coordinate of the start point of the last segment of the wire.
		 * @param starty The y coordinate of the start point of the last segment of the wire.
		 * @param endx The x coordinate of the end point of the wire.
		 * @param endy The y coordinate of the end point of the wire.
		 * @param arrowSize The size of the arrow.
		 */
		private void drawArrowTip(Graphics g, int startx, int starty, int endx, int endy, int arrowSize) {
			Component c = out.getConnectedInput().getComponent();
			if(!(c instanceof Fork || c instanceof Concatenator || c instanceof Distributor)) {
				double angle = Math.atan2(endy - starty, endx - startx) + Math.PI;
				Polygon p = new Polygon();
				p.addPoint(endx, endy);
				p.addPoint(endx + (int)(Math.cos(angle + 0.7) * arrowSize), endy + (int)(Math.sin(angle + 0.7) * arrowSize));
				p.addPoint(endx + (int)(Math.cos(angle - 0.7) * arrowSize), endy + (int)(Math.sin(angle - 0.7) * arrowSize));
				g.fillPolygon(p);
			}
		}
	}
}
