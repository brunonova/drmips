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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.feup.brunonova.drmips.R;
import org.feup.brunonova.drmips.gui.DrMIPSActivity;
import org.feup.brunonova.drmips.simulator.mips.CPU;
import org.feup.brunonova.drmips.simulator.mips.Instruction;
import org.feup.brunonova.drmips.simulator.mips.PseudoInstruction;

public class DlgCodeHelp extends DialogFragment implements DialogInterface.OnClickListener {
	public static DlgCodeHelp newInstance() {
		return new DlgCodeHelp();
	}

	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		View layout = getActivity().getLayoutInflater().inflate(R.layout.code_help, null);
		setContents(layout);

		return new AlertDialog.Builder(getActivity())
			.setTitle(R.string.supported_instructions)
			.setView(layout)
			.setPositiveButton(android.R.string.ok, this)
			.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dismiss();
	}

	private void setContents(View rootView) {
		DrMIPSActivity activity = (DrMIPSActivity)getActivity();
		TableLayout tblInstructions = (TableLayout)rootView.findViewById(R.id.tblInstructions);
		TableLayout tblPseudos = (TableLayout)rootView.findViewById(R.id.tblPseudos);
		TableRow row;
		TextView inst, desc;
		tblInstructions.removeAllViews();
		tblPseudos.removeAllViews();
		CPU cpu = activity.getCPU();

		for(Instruction i: cpu.getInstructionSet().getInstructions()) {
			row = new TableRow(activity);
			inst = new TextView(activity);
			inst.setText(i.getUsage() + " ");
			row.addView(inst);
			desc = new TextView(activity);
			desc.setText("# " + (i.hasDescription() ? i.getDescription() : "-"));
			row.addView(desc);
			tblInstructions.addView(row);
		}

		for(PseudoInstruction i: cpu.getInstructionSet().getPseudoInstructions()) {
			row = new TableRow(activity);
			inst = new TextView(activity);
			inst.setText(i.getUsage() + " ");
			row.addView(inst);
			desc = new TextView(activity);
			desc.setText("# " + (i.hasDescription() ? i.getDescription() : "-"));
			row.addView(desc);
			tblPseudos.addView(row);
		}
	}
}
