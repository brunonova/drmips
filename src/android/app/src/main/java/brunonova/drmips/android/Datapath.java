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

package brunonova.drmips.android;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.view.View;
import android.widget.RelativeLayout;

import brunonova.drmips.android.dialogs.DlgChangeLatency;
import brunonova.drmips.android.dialogs.DlgComponentDescription;
import brunonova.drmips.simulator.*;
import brunonova.drmips.simulator.components.*;
import brunonova.drmips.simulator.util.Dimension;
import brunonova.drmips.simulator.util.Point;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Special component that handles the display of the CPU datapath.
 * 
 * @author Bruno Nova
 */
@SuppressLint("ViewConstructor")
public class Datapath extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener {
	/** The graphical components. */
	private Map<String, DatapathComponent> components;
	/** The wires that connect the graphical components. */
	private List<Wire> wires;
	/** Paint object used when drawing the datapath. */
	Paint paint = new Paint();
	/** The activity that the datapath belongs to. */
	private DrMIPSActivity activity;
	/** The color used to draw wires (and other things). */
	private int wireColor;
	/** The color used for the control components/wires. */
	private int controlColor;
	/** Whether the control path is visible. */
	private boolean controlPathVisible = true;
	/** Whether to show arrows in the wires. */
	private boolean showArrows = true;
	/** The information to show (data when <tt>false</tt> or performance when <tt>true</tt>). */
	private boolean performanceMode = false;
	/** Whether to display in/out tips. */
	private boolean showTips = true;
	/** Whether to display the names in the in/out tips. */
	private boolean showTipsNames = false;
	/** Whether to display the tips for (almost) all components. */
	private boolean showTipsForAllComps = false;
	
	/**
	 * Creates the datapath.
	 * @param activity The activity that the datapath belongs to.
	 */
	public Datapath(DrMIPSActivity activity) {
		super(activity);
		this.activity = activity;
		CPU cpu = activity.getCPU();
		components = new TreeMap<>();
		wires = new LinkedList<>();
		setWillNotDraw(false); // enable draw
		
		wireColor = Util.getThemeColor(getContext(), R.attr.wireColor);
		controlColor = getResources().getColor(R.color.control);
		
		// Add each component
		Component[] comps = cpu.getComponents();
		for(Component c: comps) {
			DatapathComponent comp = new DatapathComponent(activity, c);
			components.put(c.getId(), comp);
			addView(comp);
			comp.setOnClickListener(this);
			comp.setOnLongClickListener(this);
		}
		
		// Add wires
		for(Component c: comps) {
			for(Output out: c.getOutputs())
				if(out.isConnected())
					wires.add(new Wire(out));
		}
		
		// Set the size of the view
		Dimension size = cpu.getSize();
		setMinimumWidth(DrMIPS.getApplication().dipToPx(size.width));
		setMinimumHeight(DrMIPS.getApplication().dipToPx(size.height));
	}
	
	/**
	 * Refreshes the values of the datapath.
	 */
	public void refresh() {
		for(DatapathComponent comp: components.values())
			comp.refresh();
		for(Wire w: wires)
			w.refreshTips();
		invalidate();	
	}
	
	/**
	 * Sets the control path elements visible or invisible.
	 * @param visible Whether to set the control path visible or not.
	 */
	public void setControlPathVisible(boolean visible) {
		this.controlPathVisible = visible;
		for(DatapathComponent c: components.values()) {
			if(c.getComponent().isInControlPath())
				c.setVisibility(visible ? VISIBLE : INVISIBLE);
		}
		for(Wire w: wires)
			w.refreshTips();
		invalidate();
	}
	
	/**
	 * Sets whether to show arrows on the wires.
	 * @param show Whether to show arrows on the wires.
	 */
	public void setShowArrows(boolean show) {
		this.showArrows = show;
		invalidate();
	}
	
	/**
	 * Sets whether to show data (<tt>false</tt>) or performace (<tt>true</tt>) information.
	 * @param performanceMode The mode.
	 */
	public void setPerformanceMode(boolean performanceMode) {
		this.performanceMode = performanceMode;
		refresh();
		invalidate();
	}
	
	/**
	 * Returns whether the datapath is in performance mode.
	 * @return The information to show (data when <tt>false</tt> or performance when <tt>true</tt>).
	 */
	public boolean isInPerformanceMode() {
		return performanceMode;
	}
	
	/**
	 * Sets whether to show in/out tips.
	 * @param show Whether to show the tips or not.
	 */
	public void setShowTips(boolean show) {
		showTips = show;
		refresh();
	}

	/**
	 * Sets whether to show the names in the in/out tips.
	 * @param show Whether to show the names or not.
	 */
	public void setShowTipsNames(boolean show) {
		showTipsNames = show;
		refresh();
	}

	/**
	 * Sets whether to show the in/out tips for (almost) all components.
	 * @param show Whether to show for all components.
	 */
	public void setShowTipsForAllComps(boolean show) {
		showTipsForAllComps = show;
		refresh();
	}

	@Override
	public void onClick(View v) {
		if(v instanceof DatapathComponent) {
			DatapathComponent comp = (DatapathComponent)v;
			DlgComponentDescription.newInstance(comp.getComponent().getId(), isInPerformanceMode(),
			                                    activity.getDatapathFormat()).show(activity.getFragmentManager(),
			                                                                       "component-description-dialog");
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		if(v instanceof DatapathComponent && isInPerformanceMode()) {
			DatapathComponent c = (DatapathComponent)v;
			DlgChangeLatency.newInstance(c.getComponent().getId()).show(activity.getFragmentManager(), "change-latency-dialog");
			return true;
		}
		else
			return false;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Draw the wires
		if(wires != null) {
			paint.setStrokeWidth(DrMIPS.getApplication().dipToPx(1));
			for(Wire w: wires)
				w.paint(canvas);
		}
		
		super.onDraw(canvas);
	}
	
	/**
	 * Represents a graphical wire on the datapath.
	 */
	public class Wire {
		/** The respective output of a component. */
		private Output out;
		/** The starting point of the wire. */
		private Point start;
		/** The ending point of the wire. */
		private Point end;
		/** The intermediate points of the wire. */
		private List<Point> points;
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
			createTips();
		}

		/**
		 * Creates the in/out data tips.
		 */
		private void createTips() {
			Component outComp = out.getComponent();
			if(!(outComp instanceof Fork || outComp instanceof Concatenator ||
				outComp instanceof Distributor || outComp instanceof Constant)) {
				addView(outTip = new IOPortTip(getContext(), out.getId(), "0", start.x, start.y));
			}
			if(out.isConnected()) {
				Component inComp = out.getConnectedInput().getComponent();
				if(!(inComp instanceof Fork || inComp instanceof Concatenator ||
					inComp instanceof Distributor || inComp instanceof Constant)) {
					addView(inTip = new IOPortTip(getContext(), out.getConnectedInput().getId(), "0", end.x, end.y));
				}
			}
		}

		/**
		 * Refreshes the values on the in/out tips (if any).
		 */
		public void refreshTips() {
			if(outTip != null) {
				String v = performanceMode ? "" + out.getComponent().getAccumulatedLatency() :
				                             Util.formatDataAccordingToFormat(out.getData(), activity.getDatapathFormat());
				outTip.setValue(v, showTipsNames);
				outTip.setVisibility((showTips && (out.shouldShowTip() || showTipsForAllComps) && (controlPathVisible || !out.isInControlPath())) ? VISIBLE : GONE);
			}
			if(inTip != null && out.isConnected()) {
				Input in = out.getConnectedInput();
				String v = performanceMode ? "" + in.getAccumulatedLatency() :
				                             Util.formatDataAccordingToFormat(in.getData(), activity.getDatapathFormat());
				inTip.setValue(v, showTipsNames);
				inTip.setVisibility((showTips && (in.shouldShowTip() || showTipsForAllComps) && (controlPathVisible || !in.isInControlPath())) ? VISIBLE : GONE);
			}
		}
		
		/**
		 * Draws the wire on the datapath.
		 * @param canvas The canvas of the datapath view.
		 */
		public void paint(Canvas canvas) {
			if(!out.isInControlPath() || controlPathVisible) {
				if(performanceMode && out.isInCriticalPath())
					paint.setColor(Color.RED);
				else if(!out.isRelevant() && (!isInPerformanceMode() || activity.getCPU().isPerformanceInstructionDependent()))
					paint.setColor(Color.GRAY);
				else if(out.isInControlPath())
					paint.setColor(controlColor);
				else
					paint.setColor(wireColor);
				Point s = start;
				DrMIPS app = DrMIPS.getApplication();
				for(Point e: points) {
					canvas.drawLine(app.dipToPx(s.x), app.dipToPx(s.y), app.dipToPx(e.x), app.dipToPx(e.y), paint);
					s = e;
				}
				canvas.drawLine(app.dipToPx(s.x), app.dipToPx(s.y), app.dipToPx(end.x), app.dipToPx(end.y), paint);
				if(showArrows)
					drawArrowTip(canvas, app.dipToPx(s.x), app.dipToPx(s.y), app.dipToPx(end.x), app.dipToPx(end.y), app.dipToPx(6));
			}
		}
		
		/**
		 * Draws the arrow tip for the wire.
		 * @param canvas The canvas of the datapath view.
		 * @param startx The x coordinate of the start point of the last segment of the wire.
		 * @param starty The y coordinate of the start point of the last segment of the wire.
		 * @param endx The x coordinate of the end point of the wire.
		 * @param endy The y coordinate of the end point of the wire.
		 * @param arrowSize The size of the arrow.
		 */
		private void drawArrowTip(Canvas canvas, int startx, int starty, int endx, int endy, int arrowSize) {
			Component c = out.getConnectedInput().getComponent();
			if(!(c instanceof Fork || c instanceof Concatenator || c instanceof Distributor)) {
				double angle = Math.atan2(endy - starty, endx - startx) + Math.PI;
				Path p = new Path();
				paint.setStyle(Style.FILL);
				p.moveTo(endx, endy);
				p.lineTo(endx + (int)(Math.cos(angle + 0.7) * arrowSize), endy + (int)(Math.sin(angle + 0.7) * arrowSize));
				p.lineTo(endx + (int)(Math.cos(angle - 0.7) * arrowSize), endy + (int)(Math.sin(angle - 0.7) * arrowSize));
				p.lineTo(endx, endy);
				canvas.drawPath(p, paint);
				paint.setStyle(Style.STROKE);
			}
		}
	}
}
