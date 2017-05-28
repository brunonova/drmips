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
import android.widget.Toast;

import brunonova.drmips.android.R;
import brunonova.drmips.android.DrMIPSActivity;

import java.io.File;

/**
 * File deletion confirmation dialog fragment.
 *
 * Use the method {@link #newInstance} to create the dialog.
 *
 * @author Bruno Nova
 */
public class DlgConfirmDelete extends DialogFragment implements DialogInterface.OnClickListener {
	/**
	 * Creates a new dialog.
	 * @param path Path of the file to delete.
	 * @return The dialog.
	 */
	public static DlgConfirmDelete newInstance(String path) {
		DlgConfirmDelete dialog = new DlgConfirmDelete();
		Bundle args = new Bundle();
		args.putString("path", path);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		Bundle args = getArguments();
		String path = args.getString("path");
		String name = path != null ? new File(path).getName() : "?";

		return new AlertDialog.Builder(getActivity())
			.setMessage(getString(R.string.confirm_delete).replace("#1", name))
			.setPositiveButton(android.R.string.ok, this)
			.setNegativeButton(android.R.string.cancel, this)
			.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch(which) {
			case AlertDialog.BUTTON_POSITIVE: // OK
				Bundle args = getArguments();
				DrMIPSActivity activity = (DrMIPSActivity)getActivity();
				String path = args.getString("path");
				File file;

				if(path != null && (file = new File(path)).exists()) {
					if(file.delete()) {
						Toast.makeText(getActivity(), R.string.file_deleted, Toast.LENGTH_SHORT).show();
						activity.newFile();
					} else
						Toast.makeText(getActivity(), R.string.error_deleting_file, Toast.LENGTH_SHORT).show();
				}
				break;

			case AlertDialog.BUTTON_NEGATIVE: // Cancel
				dismiss();
				break;
		}
	}
}
