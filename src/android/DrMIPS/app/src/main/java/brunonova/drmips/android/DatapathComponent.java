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
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import brunonova.drmips.simulator.Component;
import brunonova.drmips.simulator.components.*;

/**
 * Graphical component that displays a CPU component.
 * 
 * @author Bruno Nova
 */
@SuppressLint("ViewConstructor")
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
			setTextColor(component.isInControlPath() ? getResources().getColor(R.color.control) : Util.getThemeColor(getContext(), R.attr.wireColor));
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
