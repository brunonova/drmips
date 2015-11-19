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

package brunonova.drmips.android.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import brunonova.drmips.android.R;
import brunonova.drmips.android.DrMIPS;
import brunonova.drmips.android.DrMIPSActivity;
import brunonova.drmips.android.Util;
import brunonova.drmips.simulator.*;
import brunonova.drmips.simulator.components.ALU;
import brunonova.drmips.simulator.components.ExtendedALU;

/**
 * Dialog fragment to view the description of a component.
 *
 * Use the method {@link #newInstance} to create the dialog.
 *
 * @author Bruno Nova
 */
public class DlgComponentDescription extends DialogFragment implements DialogInterface.OnClickListener {
	/**
	 * Creates a new dialog
	 * @param componentId The ID of the component.
	 * @param performanceMode Whether the datapath is in performance mode.
	 * @param datapathFormat The data format that the datapath is currently in.
	 * @return The dialog.
	 */
	public static DlgComponentDescription newInstance(String componentId, boolean performanceMode, int datapathFormat) {
		DlgComponentDescription dialog = new DlgComponentDescription();
		Bundle args = new Bundle();
		args.putString("id", componentId);
		args.putBoolean("performanceMode", performanceMode);
		args.putInt("datapathFormat", datapathFormat);
		dialog.setArguments(args);
		return dialog;
	}

	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		View layout = getActivity().getLayoutInflater().inflate(R.layout.component_details, null);
		AlertDialog dialog = new AlertDialog.Builder(getActivity())
			.setTitle("Component description")
			.setView(layout)
			.setPositiveButton(android.R.string.ok, this)
			.create();

		setContents(dialog, layout);

		return dialog;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dismiss();
	}

	private void setContents(Dialog dialog, View rootView) {
		String title;
		Bundle args = getArguments();
		if(!args.containsKey("id")) return;
		DrMIPSActivity activity = (DrMIPSActivity)getActivity();
		Component component = activity.getCPU().getComponent(args.getString("id"));
		boolean performanceMode = args.getBoolean("performanceMode", false);
		int datapathFormat = args.getInt("datapathFormat", DrMIPS.DEFAULT_DATAPATH_DATA_FORMAT);

		// Title
		int nameId = activity.getResources().getIdentifier(component.getNameKey(), "string", activity.getPackageName());
		if(nameId != 0)
			title = activity.getString(nameId);
		else
			title = component.getDefaultName();
		title += " (" + component.getId() + ")";
		if(component instanceof Synchronous) title += " - " + activity.getString(R.string.synchronous);
		dialog.setTitle(title);

		// Description
		TextView lblComponentDescription = (TextView)rootView.findViewById(R.id.lblComponentDescription);
		String desc = component.getCustomDescription(getResources().getConfiguration().locale.toString());
		if(desc == null) {
			int descId = activity.getResources().getIdentifier(component.getDescriptionKey(), "string", activity.getPackageName());
			if(descId != 0)
				desc = activity.getString(descId);
			else
				desc = component.getDefaultDescription();
		}

		// ALU operation if ALU
		if(!performanceMode && component instanceof ALU) {
			ALU alu = (ALU)component;
			desc += "\n" + getResources().getString(R.string.operation) + ": "+ alu.getOperationName();

			// HI and LO registers if extended ALU
			if(!performanceMode && component instanceof ExtendedALU) {
				ExtendedALU ext_alu = (ExtendedALU)alu;
				desc += "\nHI: " + Util.formatDataAccordingToFormat(ext_alu.getHI(), datapathFormat);
				desc += "\nLO: " + Util.formatDataAccordingToFormat(ext_alu.getLO(), datapathFormat);
			}
		}
		lblComponentDescription.setText(desc);


		// Latency
		TextView lblLatency = (TextView)rootView.findViewById(R.id.lblComponentLatency);
		if(performanceMode) {
			lblLatency.setVisibility(View.VISIBLE);
			lblLatency.setText(getResources().getString(R.string.latency) + ": " + component.getLatency() + " " + CPU.LATENCY_UNIT + " (" + getResources().getString(R.string.long_press_to_change) + ")");
		}
		else
			lblLatency.setVisibility(View.GONE);

		// Inputs
		TableLayout tblInputs = (TableLayout)rootView.findViewById(R.id.tblComponentInputs);
		tblInputs.removeAllViews();
		TableRow row;
		TextView lblId, lblValue;
		for(Input in: component.getInputs()) {
			if(in.isConnected()) {
				row = new TableRow(activity);
				lblId = new TextView(activity);
				lblValue = new TextView(activity);
				lblId.setText(in.getId() + ":");
				lblValue.setGravity(Gravity.RIGHT);
				if(performanceMode) {
					lblValue.setText(in.getAccumulatedLatency() + " " + CPU.LATENCY_UNIT);
				}
				else
					lblValue.setText(Util.formatDataAccordingToFormat(in.getData(), datapathFormat));
				row.addView(lblId);
				row.addView(lblValue);
				if(performanceMode && in.isInCriticalPath()) {
					lblId.setTextColor(getResources().getColor(R.color.red));
					lblValue.setTextColor(getResources().getColor(R.color.red));
				}
				else if(in.isInControlPath()) {
					lblId.setTextColor(getResources().getColor(R.color.control));
					lblValue.setTextColor(getResources().getColor(R.color.control));
				}
				tblInputs.addView(row);
			}
		}

		// Outputs
		TableLayout tblOutputs = (TableLayout)rootView.findViewById(R.id.tblComponentOutputs);
		tblOutputs.removeAllViews();
		for(Output out: component.getOutputs()) {
			if(out.isConnected()) {
				row = new TableRow(activity);
				lblId = new TextView(activity);
				lblValue = new TextView(activity);
				lblId.setText(out.getId() + ":");
				lblValue.setGravity(Gravity.RIGHT);
				if(performanceMode)
					lblValue.setText(component.getAccumulatedLatency() + " " + CPU.LATENCY_UNIT);
				else
					lblValue.setText(Util.formatDataAccordingToFormat(out.getData(), datapathFormat));
				row.addView(lblId);
				row.addView(lblValue);
				if(performanceMode && out.isInCriticalPath()) {
					lblId.setTextColor(getResources().getColor(R.color.red));
					lblValue.setTextColor(getResources().getColor(R.color.red));
				}
				else if(out.isInControlPath()) {
					lblId.setTextColor(getResources().getColor(R.color.control));
					lblValue.setTextColor(getResources().getColor(R.color.control));
				}
				tblOutputs.addView(row);
			}
		}
	}
}
