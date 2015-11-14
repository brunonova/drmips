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
import android.widget.EditText;

import brunonova.drmips.android.R;
import brunonova.drmips.android.DrMIPS;
import brunonova.drmips.android.DrMIPSActivity;

import java.io.File;

/**
 * File Save dialog fragment.
 *
 * Use the method {@link #newInstance} to create the dialog.
 *
 * @author Bruno Nova
 */
public class DlgSave extends DialogFragment implements DialogInterface.OnClickListener {
	private EditText txtFilename;

	/**
	 * Creates a new dialog.
	 * @param name Current name of the file (an empty String if this isn't an existing file).
	 * @return The dialog.
	 */
	public static DlgSave newInstance(String name) {
		DlgSave dialog = new DlgSave();
		Bundle args = new Bundle();
		args.putString("name", name);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		Bundle args = getArguments();
		txtFilename = new EditText(getActivity());
		txtFilename.setHint(R.string.filename);
		if(savedInstanceState != null && savedInstanceState.containsKey("val")) {
			txtFilename.setText(savedInstanceState.getString("val"));
		} else {
			txtFilename.setText(args.getString("name", ""));
		}
		return new AlertDialog.Builder(getActivity())
			.setTitle(R.string.save_as)
			.setView(txtFilename)
			.setPositiveButton(android.R.string.ok, this)
			.setNegativeButton(android.R.string.cancel, this)
			.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("val", txtFilename.getText().toString());
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch(which) {
			case AlertDialog.BUTTON_POSITIVE: // OK
				String path = txtFilename.getText().toString().trim();
				if(!path.isEmpty()) { // save the file
					if(!path.contains(".")) path += ".asm"; // append extension if missing
					File file = new File(DrMIPS.getApplication().getCodeDir().getAbsolutePath() + File.separator + path);
					DrMIPSActivity activity = (DrMIPSActivity)getActivity();

					if(!file.exists()) { // new file
						activity.saveFile(file);
					} else { // file exists
						DlgConfirmReplace.newInstance(file.getPath()).show(getFragmentManager(), "confirm-replace-dialog");
					}
				}
				break;

			case AlertDialog.BUTTON_NEGATIVE: // Cancel
				dismiss();
				break;
		}
	}
}
