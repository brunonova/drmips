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

import org.feup.brunonova.drmips.R;
import org.feup.brunonova.drmips.gui.DrMIPSActivity;
import org.feup.brunonova.drmips.simulator.AppInfo;

public class AboutDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String msg = getString(R.string.by) + ": " + AppInfo.MAIN_AUTHOR_NAME_EMAIL
				+ "\n" + getString(R.string.for_dissertation)
				+ "\n" + AppInfo.MAIN_AUTHOR_INSTITUTION;

		return new AlertDialog.Builder(getActivity())
				.setTitle(AppInfo.NAME + " " + AppInfo.VERSION)
				.setMessage(msg)
				.setPositiveButton(android.R.string.ok, this)
				.setNeutralButton(R.string.license, this)
				.setNegativeButton(R.string.credits, this)
				.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		DrMIPSActivity activity = (DrMIPSActivity)getActivity();
		switch(which) {
			case AlertDialog.BUTTON_POSITIVE: // OK
				dismiss();
				break;
			case AlertDialog.BUTTON_NEUTRAL: // License
				activity.showDialog(DrMIPSActivity.LICENSE_DIALOG);
				break;
			case AlertDialog.BUTTON_NEGATIVE: // Credits
				activity.showDialog(DrMIPSActivity.CREDITS_DIALOG);
				break;
		}
	}
}
