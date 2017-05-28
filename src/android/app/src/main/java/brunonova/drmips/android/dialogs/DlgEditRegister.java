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
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import brunonova.drmips.android.R;
import brunonova.drmips.android.DrMIPSActivity;

/**
 * Dialog fragment to edit the value of a register.
 *
 * Use the method {@link #newInstance} to create the dialog.
 *
 * @author Bruno Nova
 */
public class DlgEditRegister extends DialogFragment implements DialogInterface.OnClickListener {
	private EditText txtRegisterValue;

	/**
	 * Creates a new dialog.
	 * @param index The index of the register.
	 * @param name The name of the register.
	 * @param value The current value at the register.
	 * @return The dialog.
	 */
	public static DlgEditRegister newInstance(int index, String name, int value) {
		DlgEditRegister dialog = new DlgEditRegister();
		Bundle args = new Bundle();
		args.putInt("index", index);
		args.putString("name", name);
		args.putInt("value", value);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		Bundle args = getArguments();
		txtRegisterValue = new EditText(getActivity());
		txtRegisterValue.setHint(R.string.value);
		txtRegisterValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		if(savedInstanceState != null && savedInstanceState.containsKey("val")) {
			txtRegisterValue.setText(savedInstanceState.getString("val"));
		} else {
			txtRegisterValue.setText("" + args.getInt("value"));
		}

		return new AlertDialog.Builder(getActivity())
			.setTitle(getString(R.string.edit_value).replace("#1", args.getString("name", "?")))
			.setView(txtRegisterValue)
			.setPositiveButton(android.R.string.ok, this)
			.setNegativeButton(android.R.string.cancel, this)
			.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("val", txtRegisterValue.getText().toString());
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch(which) {
			case AlertDialog.BUTTON_POSITIVE: //OK
				String value = txtRegisterValue.getText().toString().trim();
				int val;
				if(!value.isEmpty()) {
					try {
						Bundle args = getArguments();
						DrMIPSActivity activity = (DrMIPSActivity)getActivity();
						int index = args.getInt("index");
						if (index >= 0 && index <= activity.getCPU().getRegBank().getNumberOfRegisters()) {
							val = Integer.parseInt(value);
							activity.setRegisterValue(index, val);
							activity.refreshRegistersTableValues();
						}
					} catch(NumberFormatException ex) {
						Toast.makeText(getActivity(), R.string.invalid_value, Toast.LENGTH_SHORT).show();
					}
				}
				break;

			case AlertDialog.BUTTON_NEGATIVE: // Cancel
				dismiss();
				break;
		}
	}
}
