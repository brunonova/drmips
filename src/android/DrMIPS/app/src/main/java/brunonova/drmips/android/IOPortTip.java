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
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The tip that is shown in the datapath for inputs/outputs.
 * 
 * @author Bruno Nova
 */
@SuppressLint("ViewConstructor")
public class IOPortTip extends TextView implements View.OnClickListener {
	/** Identifier of the input/output. */
	private final String id;
	/** The "tooltip" to display when pressed. */
	private String tooltip;
	
	/**
	 * Constructor.
	 * @param context The context of the tip.
	 * @param id Identifier of the input/output.
	 * @param value Value in the input/output.
	 * @param x The x coordinate of the tip's position in the <tt>attachedComponent</tt>.
	 * @param y The y coordinate of the tip's position in the <tt>attachedComponent</tt>.
	 */
	public IOPortTip(Context context, String id, String value, int x, int y) {
		super(context);
		this.id = id;
		setTypeface(Typeface.MONOSPACE);
		setTextColor(Color.BLACK);
		setBackgroundResource(R.drawable.tip_back);
		setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
		setPadding(2, 1, 2, 1);
		
		DrMIPS app = DrMIPS.getApplication();
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.topMargin = app.dipToPx(y + 1);
		params.leftMargin = app.dipToPx(x - 3);
		setLayoutParams(params);
		
		setOnClickListener(this);
		setValue(value);
	}

	/**
	 * Updates the value of the tip.
	 * @param value New value (as a String formatted in bin/dec/hex).
	 * @param showName Whether to display the name of the input/output as well.
	 */
	public final void setValue(String value, boolean showName) {
		if(showName)
			setText(id + ": " + value);
		else
			setText(value);
		tooltip = id + ": " + value;
	}

	/**
	 * Updates the value of the tip.
	 * @param value New value (as a String formatted in bin/dec/hex).
	 */
	public final void setValue(String value) {
		setValue(value, false);
	}

	@Override
	public void onClick(View v) {
		Toast.makeText(getContext(), tooltip, Toast.LENGTH_SHORT).show();
	}
}
