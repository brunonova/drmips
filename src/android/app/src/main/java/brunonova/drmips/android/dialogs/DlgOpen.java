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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import brunonova.drmips.android.R;
import brunonova.drmips.android.DrMIPS;
import brunonova.drmips.android.DrMIPSActivity;

import java.io.File;

/**
 * File Open dialog fragment.
 *
 * Use the method {@link #newInstance} to create the dialog.
 *
 * @author Bruno Nova
 */
public class DlgOpen extends DialogFragment implements DialogInterface.OnClickListener {
	/**
	 * Creates a new dialog.
	 * @param files The names of the files that exist.
	 * @return The dialog.
	 */
	public static DlgOpen newInstance(String[] files) {
		DlgOpen dialog = new DlgOpen();
		Bundle args = new Bundle();
		args.putStringArray("files", files);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		Bundle args = getArguments();
		String[] files = args.containsKey("files") ? args.getStringArray("files") : new String[] {};

		return new AlertDialog.Builder(getActivity())
			.setTitle(R.string.open)
			.setItems(files, this)
			.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		Bundle args = getArguments();
		DrMIPSActivity activity = (DrMIPSActivity)getActivity();
		String[] files = args.containsKey("files") ? args.getStringArray("files") : new String[] {};

		if(files != null && which >= 0 && which < files.length) {
			String name = files[which];
			File file = new File(DrMIPS.getApplication().getCodeDir() + File.separator + name);
			activity.openFile(file);
		}

		dialog.dismiss();
	}
}
