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

package org.feup.brunonova.drmips.gui;

import org.feup.brunonova.drmips.simulator.mips.CPU;
import org.feup.brunonova.drmips.simulator.mips.Instruction;
import org.feup.brunonova.drmips.simulator.mips.PseudoInstruction;

/**
 * Supported instructions dialog.
 * 
 * @author Bruno Nova
 */
public class DlgSupportedInstructions extends javax.swing.JDialog {
	/** Index of the instructions tab. */
	private static final int INSTRUCTIONS_INDEX = 0;
	/** Index of the pseudo-instructions tab. */
	private static final int PSEUDO_INSTRUCTIONS_INDEX = 1;
	/** Index of the directives tab. */
	private static final int DIRECTIVES_INDEX = 2;
	
	/**
	 * Creates new form DlgSupportedInstructions
	 * @param parent The simulator's main window.
	 */
	public DlgSupportedInstructions(FrmSimulator parent) {
		super(parent, false);
		initComponents();
		translate();
		getRootPane().setDefaultButton(cmdClose);
		Util.centerWindow(this);
		Util.enableCloseWindowWithEscape(this);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        cmdClose = new javax.swing.JButton();
        pnlTabs = new javax.swing.JTabbedPane();
        pnlInstructions = new javax.swing.JScrollPane();
        tblInstructions = new org.feup.brunonova.drmips.gui.SupportedInstructionsTable();
        pnlPseudoInstructions = new javax.swing.JScrollPane();
        tblPseudoInstructions = new org.feup.brunonova.drmips.gui.SupportedInstructionsTable();
        pnlDirectives = new javax.swing.JScrollPane();
        tblDirectives = new org.feup.brunonova.drmips.gui.SupportedInstructionsTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(300, 200));
        setPreferredSize(new java.awt.Dimension(500, 500));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        cmdClose.setText("close");
        cmdClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCloseActionPerformed(evt);
            }
        });
        jPanel1.add(cmdClose);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        tblInstructions.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        pnlInstructions.setViewportView(tblInstructions);

        pnlTabs.addTab("instructions", pnlInstructions);

        tblPseudoInstructions.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        pnlPseudoInstructions.setViewportView(tblPseudoInstructions);

        pnlTabs.addTab("pseudo_instructions", pnlPseudoInstructions);

        tblDirectives.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        pnlDirectives.setViewportView(tblDirectives);

        pnlTabs.addTab("directives", pnlDirectives);

        getContentPane().add(pnlTabs, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
		close();
    }//GEN-LAST:event_formWindowClosing

    private void cmdCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCloseActionPerformed
		close();
    }//GEN-LAST:event_cmdCloseActionPerformed

	/**
	 * Translates the dialog's strings.
	 */
	protected final void translate() {
		setTitle(Lang.t("instructions_supported_by_cpu"));
		Lang.tButton(cmdClose, "close");
		pnlTabs.setTitleAt(INSTRUCTIONS_INDEX, Lang.t("instructions"));
		pnlTabs.setTitleAt(PSEUDO_INSTRUCTIONS_INDEX, Lang.t("pseudo_instructions"));
		pnlTabs.setTitleAt(DIRECTIVES_INDEX, Lang.t("directives"));
		
		tblDirectives.clear();
		tblDirectives.addInstruction(".data", Lang.t("data_directive"));
		tblDirectives.addInstruction(".space", Lang.t("space_directive"));
		tblDirectives.addInstruction(".text", Lang.t("text_directive"));
		tblDirectives.addInstruction(".word", Lang.t("word_directive"));
		tblDirectives.packFirstColumn();
	}
	
	/**
	 * Refreshes the contents of the tables for the specified CPU.
	 * @param cpu The CPU to get the supported instructions from.
	 */
	protected final void setCPU(CPU cpu) {
		// Instructions
		tblInstructions.clear();
		for(Instruction i: cpu.getInstructionSet().getInstructions())
			tblInstructions.addInstruction(i.getUsage(), i.getDescription());
		tblInstructions.packFirstColumn();
		
		// Pseudo-instructions
		tblPseudoInstructions.clear();
		for(PseudoInstruction i: cpu.getInstructionSet().getPseudoInstructions())
			tblPseudoInstructions.addInstruction(i.getUsage(), i.getDescription());
		tblPseudoInstructions.packFirstColumn();
	}
	
	/**
	 * Closes the window.
	 */
	private void close() {
		setVisible(false);
	}
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdClose;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane pnlDirectives;
    private javax.swing.JScrollPane pnlInstructions;
    private javax.swing.JScrollPane pnlPseudoInstructions;
    private javax.swing.JTabbedPane pnlTabs;
    private org.feup.brunonova.drmips.gui.SupportedInstructionsTable tblDirectives;
    private org.feup.brunonova.drmips.gui.SupportedInstructionsTable tblInstructions;
    private org.feup.brunonova.drmips.gui.SupportedInstructionsTable tblPseudoInstructions;
    // End of variables declaration//GEN-END:variables
}
