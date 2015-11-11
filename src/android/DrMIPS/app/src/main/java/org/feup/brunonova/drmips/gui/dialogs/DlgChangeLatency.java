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

package org.feup.brunonova.drmips.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import org.feup.brunonova.drmips.R;
import org.feup.brunonova.drmips.gui.DrMIPSActivity;
import org.feup.brunonova.drmips.simulator.mips.Component;

public class DlgChangeLatency extends DialogFragment implements DialogInterface.OnClickListener {
	private EditText txtLatency;

	public static DlgChangeLatency newInstance(String componentId) {
		DlgChangeLatency dialog = new DlgChangeLatency();
		Bundle args = new Bundle();
		args.putString("id", componentId);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		Bundle args = getArguments();
		DrMIPSActivity activity = (DrMIPSActivity)getActivity();
		Component component = activity.getCPU().getComponent(args.getString("id", ""));
		txtLatency = new EditText(getActivity());
		txtLatency.setHint(R.string.latency);
		txtLatency.setInputType(InputType.TYPE_CLASS_NUMBER);
		if(savedInstanceState != null && savedInstanceState.containsKey("latency")) {
			txtLatency.setText(savedInstanceState.getString("latency", "0"));
		}
		else {
			if(component != null) txtLatency.setText("" + component.getLatency());
		}

		return new AlertDialog.Builder(getActivity())
			.setTitle(getResources().getString(R.string.latency_of_x).replace("#1", args.getString("id", "")))
			.setView(txtLatency)
			.setPositiveButton(android.R.string.ok, this)
			.setNegativeButton(android.R.string.cancel, this)
			.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("latency", txtLatency.getText().toString());
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch(which) {
			case AlertDialog.BUTTON_POSITIVE: // OK
				try {
					Bundle args = getArguments();
					DrMIPSActivity activity = (DrMIPSActivity)getActivity();
					Component component = activity.getCPU().getComponent(args.getString("id", ""));

					int lat = Integer.parseInt(txtLatency.getText().toString());
					if(lat >= 0 && component != null) {
						component.setLatency(lat);
						activity.getCPU().calculatePerformance();
						activity.getDatapath().refresh();
						activity.getDatapath().invalidate();
					} else {
						Toast.makeText(getActivity(), R.string.invalid_value, Toast.LENGTH_SHORT).show();
					}
				} catch(NumberFormatException ex) {
					Toast.makeText(getActivity(), R.string.invalid_value, Toast.LENGTH_SHORT).show();
				}
				break;

			case AlertDialog.BUTTON_NEGATIVE: // Cancel
				dismiss();
				break;
		}
	}
}
