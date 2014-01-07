/*
    DrMIPS - Educational MIPS simulator
    Copyright (C) 2013 Bruno Nova <ei08109@fe.up.pt>

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

import org.feup.brunonova.drmips.R;
import org.feup.brunonova.drmips.mips.CPU;
import org.feup.brunonova.drmips.mips.Component;
import org.feup.brunonova.drmips.mips.Input;
import org.feup.brunonova.drmips.mips.IsSynchronous;
import org.feup.brunonova.drmips.mips.Output;
import org.feup.brunonova.drmips.mips.components.ALU;
import org.feup.brunonova.drmips.mips.components.Concatenator;
import org.feup.brunonova.drmips.mips.components.Constant;
import org.feup.brunonova.drmips.mips.components.Distributor;
import org.feup.brunonova.drmips.mips.components.ExtendedALU;
import org.feup.brunonova.drmips.mips.components.Fork;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Graphical component that displays a CPU component.
 * 
 * @author Bruno Nova
 */
public class DatapathComponent extends TextView {
	/** The activity that the component belongs to. */
	private DrMIPSActivity activity;
	/** The respective CPU component. */
	private Component component;

	/**
	 * Creates the datapath component.
	 * @param activity The activity that the component belongs to.
	 * @param component The respective CPU component.
	 */
	public DatapathComponent(DrMIPSActivity activity, Component component) {
		super(activity);
		this.activity = activity;
		this.component = component;
		DrMIPS app = DrMIPS.getApplication();
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(app.dipToPx(component.getSize().width), app.dipToPx(component.getSize().height));
		params.topMargin = app.dipToPx(component.getPosition().y);
		params.leftMargin = app.dipToPx(component.getPosition().x);
		setLayoutParams(params);
		
		setGravity(Gravity.CENTER);
		setText(component.getDisplayName());
		setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
		setTextColor(Color.BLACK);
		setTypeface(Typeface.MONOSPACE);
		
		if(component instanceof Fork || component instanceof Concatenator || component instanceof Distributor) {
			if(component.isInControlPath())
				setBackgroundResource(R.drawable.aux_control_comp_back);
			else {
				TypedValue typedValue = new TypedValue();
				getContext().getTheme().resolveAttribute(R.attr.aux_comp_back, typedValue, true);
				setBackgroundResource(typedValue.resourceId);
			}
		}
		else if(component instanceof Constant) {
			setBackgroundResource(R.drawable.const_comp_back);
			TypedValue typedValue = new TypedValue();
			getContext().getTheme().resolveAttribute(R.attr.wireColor, typedValue, true);
			setTextColor(component.isInControlPath() ? getResources().getColor(R.color.control) : getResources().getColor(typedValue.resourceId));
		}
		else {
			if(component.isInControlPath()) {
				setTextColor(getResources().getColor(R.color.control));
				setBackgroundResource(R.drawable.control_comp_back);
			}
			else
				setBackgroundResource(R.drawable.normal_comp_back);
		}
	}
	
	/**
	 * Refreshes the contents of the specified component description dialog
	 * @param dialog The dialog.
	 */
	protected void refreshDescriptionDialog(Dialog dialog) {
		String title = "";
		
		// Title
		int nameId = getContext().getResources().getIdentifier(component.getNameKey(), "string", getContext().getPackageName());
		if(nameId != 0)
			title = getContext().getString(nameId);
		else
			title = component.getDisplayName();
		title += " (" + component.getId() + ")";
		if(component instanceof IsSynchronous) title += " - " + getContext().getString(R.string.synchronous);
		dialog.setTitle(title);
		
		// Description
		TextView lblComponentDescription = (TextView)dialog.findViewById(R.id.lblComponentDescription);
		String desc = component.getCustomDescription(getResources().getConfiguration().locale.toString());
		if(desc == null) {
			int descId = getContext().getResources().getIdentifier(component.getDescriptionKey(), "string", getContext().getPackageName());
			if(descId != 0)
				desc = getContext().getString(descId);
			else
				desc = "";
		}
		
		// ALU operation if ALU
		if(!activity.getDatapath().isInPerformanceMode() && component instanceof ALU) {
			ALU alu = (ALU)component;
			desc += "\n" + getResources().getString(R.string.operation) + ": "+ alu.getOperationName();
			
			// HI and LO registers if extended ALU
			if(!activity.getDatapath().isInPerformanceMode() && component instanceof ExtendedALU) {
				ExtendedALU ext_alu = (ExtendedALU)alu;
				desc += "\nHI: " + Util.formatDataAccordingToFormat(ext_alu.getHI(), activity.getDatapathFormat());
				desc += "\nLO: " + Util.formatDataAccordingToFormat(ext_alu.getLO(), activity.getDatapathFormat());
			}
		}
		lblComponentDescription.setText(desc);
		
		
		// Latency
		TextView lblLatency = (TextView)dialog.findViewById(R.id.lblComponentLatency);
		if(activity.getDatapath().isInPerformanceMode()) {
			lblLatency.setVisibility(VISIBLE);
			lblLatency.setText(getResources().getString(R.string.latency) + ": " + component.getLatency() + " " + CPU.LATENCY_UNIT + " (" + getResources().getString(R.string.long_press_to_change) + ")");
		}
		else
			lblLatency.setVisibility(GONE);
		
		// Inputs
		TableLayout tblInputs = (TableLayout)dialog.findViewById(R.id.tblComponentInputs);
		tblInputs.removeAllViews();
		TableRow row;
		TextView lblId, lblValue;
		for(Input in: component.getInputs()) {
			if(in.isConnected()) {
				row = new TableRow(getContext());
				lblId = new TextView(getContext());
				lblValue = new TextView(getContext());
				lblId.setText(in.getId() + ":");
				lblValue.setGravity(Gravity.RIGHT);
				if(activity.getDatapath().isInPerformanceMode()) {
					lblValue.setText(in.getAccumulatedLatency() + " " + CPU.LATENCY_UNIT);
				}
				else
					lblValue.setText(Util.formatDataAccordingToFormat(in.getData(), activity.getDatapathFormat()));
				row.addView(lblId);
				row.addView(lblValue);
				if(in.isInControlPath()) {
					lblId.setTextColor(getResources().getColor(R.color.control));
					lblValue.setTextColor(getResources().getColor(R.color.control));
				}
				tblInputs.addView(row);
			}
		}
		
		// Outputs
		TableLayout tblOutputs = (TableLayout)dialog.findViewById(R.id.tblComponentOutputs);
		tblOutputs.removeAllViews();
		for(Output out: component.getOutputs()) {
			if(out.isConnected()) {
				row = new TableRow(getContext());
				lblId = new TextView(getContext());
				lblValue = new TextView(getContext());
				lblId.setText(out.getId() + ":");
				lblValue.setGravity(Gravity.RIGHT);
				if(activity.getDatapath().isInPerformanceMode())
					lblValue.setText(component.getAccumulatedLatency() + " " + CPU.LATENCY_UNIT);
				else
					lblValue.setText(Util.formatDataAccordingToFormat(out.getData(), activity.getDatapathFormat()));
				row.addView(lblId);
				row.addView(lblValue);
				if(out.isInControlPath()) {
					lblId.setTextColor(getResources().getColor(R.color.control));
					lblValue.setTextColor(getResources().getColor(R.color.control));
				}
				tblOutputs.addView(row);
			}
		}
	}
	
	/**
	 * Returns the respective CPU component.
	 * @return The respective CPU component.
	 */
	public Component getComponent() {
		return component;
	}
	
	/**
	 * Refreshes the component's information, and possibly other things.
	 */
	public void refresh() {
		// Set fork gray if irrelevant
		if(getComponent() instanceof Fork) {
			if(!((Fork)component).getInput().isRelevant() && (!activity.getDatapath().isInPerformanceMode() || activity.getCPU().isPerformanceInstructionDependent()))
				setBackgroundResource(R.drawable.aux_comp_back_gray);
			else if(component.isInControlPath())
				setBackgroundResource(R.drawable.aux_control_comp_back);
			else {
				TypedValue typedValue = new TypedValue();
				getContext().getTheme().resolveAttribute(R.attr.aux_comp_back, typedValue, true);
				setBackgroundResource(typedValue.resourceId);
			}
		}
	}
}
