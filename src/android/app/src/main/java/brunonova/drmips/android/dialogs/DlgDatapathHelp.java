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
import android.view.View;

import brunonova.drmips.android.R;

/**
 * Datapath Help dialog fragment.
 *
 * Use the method {@link #newInstance} to create the dialog.
 *
 * @author Bruno Nova
 */
public class DlgDatapathHelp extends DialogFragment implements DialogInterface.OnClickListener {
	/**
	 * Creates a new dialog.
	 * @param performanceMode Whether the datapath is in performance mode.
	 * @return The dialog
	 */
	public static DlgDatapathHelp newInstance(boolean performanceMode) {
		DlgDatapathHelp dialog = new DlgDatapathHelp();
		Bundle args = new Bundle();
		args.putBoolean("performanceMode", performanceMode);
		dialog.setArguments(args);
		return dialog;
	}

	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		View layout = getActivity().getLayoutInflater().inflate(R.layout.datapath_help, null);
		AlertDialog dialog = new AlertDialog.Builder(getActivity())
			.setTitle("Datapath help")
			.setView(layout)
			.setPositiveButton(android.R.string.ok, this)
			.create();

		Bundle args = getArguments();
		if(args.getBoolean("performanceMode", false)) {
			dialog.setTitle(R.string.performance_mode);
			layout.findViewById(R.id.rowControlPathWire).setVisibility(View.VISIBLE);
			layout.findViewById(R.id.rowIrrelevantWire).setVisibility(View.VISIBLE);
			layout.findViewById(R.id.rowWireInCriticalPath).setVisibility(View.VISIBLE);
			layout.findViewById(R.id.lblAdvisedDisplayControlPath).setVisibility(View.VISIBLE);
		} else {
			dialog.setTitle(R.string.data_mode);
			layout.findViewById(R.id.rowControlPathWire).setVisibility(View.VISIBLE);
			layout.findViewById(R.id.rowIrrelevantWire).setVisibility(View.VISIBLE);
			layout.findViewById(R.id.rowWireInCriticalPath).setVisibility(View.GONE);
			layout.findViewById(R.id.lblAdvisedDisplayControlPath).setVisibility(View.GONE);
		}

		return dialog;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dismiss();
	}
}
