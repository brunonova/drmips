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

package brunonova.drmips.pc;

import brunonova.drmips.simulator.*;
import brunonova.drmips.simulator.components.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;

/**
 * Graphical component that displays a CPU component.
 *
 * @author Bruno Nova
 */
public final class DatapathComponent extends JLabel implements MouseListener {
	/** The font used for the text. */
	private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 8);
	/** Width of the tooltips with the details of the components. */
	private static final int TOOLTIP_WIDTH = 400;

	/** The graphical datapath this component is in. */
	private final DatapathPanel datapath;
	/** The respective CPU component. */
	private final Component component;

	/**
	 * Creates a graphical CPU component.
	 * @param datapath The datapath this component is being added to.
	 * @param component The respective CPU component.
	 */
	public DatapathComponent(DatapathPanel datapath, Component component) {
		super();
		this.datapath = datapath;
		this.component = component;
		boolean dark = DrMIPS.prefs.getBoolean(DrMIPS.DARK_THEME_PREF, DrMIPS.DEFAULT_DARK_THEME);

		setBorder(new LineBorder(Color.BLACK));
		setOpaque(true);
		setBackground(Color.WHITE);
		setLayout(new BorderLayout());
		setLocationAndSizeScaled();

		setText("<html><pre>" + component.getDisplayName() + "</pre></html>");
		setHorizontalAlignment(JLabel.CENTER);
		setFont(FONT);
		setForeground(Color.BLACK);

		if(component instanceof Fork || component instanceof Concatenator || component instanceof Distributor) {
			Color color = component.isInControlPath() ? Util.controlPathColor : Util.wireColor;
			setBackground(color);
			setBorder(BorderFactory.createLineBorder(color));
		}
		else if(component instanceof Constant) {
			setOpaque(false);
			setBorder(null);
			setForeground(component.isInControlPath() ? Util.controlPathColor : Util.wireColor);
		}
		else {
			if(component.isInControlPath()) {
				setBorder(BorderFactory.createLineBorder(Util.controlPathColor));
				setForeground(Util.controlPathColor);
			}
		}

		refresh();
		addMouseListener(this);
	}

	/**
	 * Returns the respective CPU component.
	 * @return The CPU component.
	 */
	public Component getComponent() {
		return component;
	}

	/**
	 * Sets the location and size of the component, scaled to the current zoom level.
	 * Also scales the font.
	 */
	protected final void setLocationAndSizeScaled() {
		setLocation((int)(component.getPosition().x * datapath.getScale()),
		            (int)(component.getPosition().y * datapath.getScale()));
		setSize((int)(component.getSize().width * datapath.getScale()),
		        (int)(component.getSize().height * datapath.getScale()));
		setFont(getFont().deriveFont(FONT.getSize2D() * (float)datapath.getScale()));
	}

	/**
	 * Refreshes the component tooltip with the current information, and possibly other things.
	 */
	public void refresh() {
		boolean dark = DrMIPS.prefs.getBoolean(DrMIPS.DARK_THEME_PREF, DrMIPS.DEFAULT_DARK_THEME);

		// Set fork gray if irrelevant
		if(getComponent() instanceof Fork) {
			Color color;
			if(!((Fork)component).getInput().isRelevant() && (!datapath.isInPerformanceMode() || datapath.getCPU().isPerformanceInstructionDependent()))
				color = Util.irrelevantColor;
			else if(component.isInControlPath())
				color = Util.controlPathColor;
			else
				color = Util.wireColor;
			setBackground(color);
			setBorder(BorderFactory.createLineBorder(color));
		}


		// Refresh the tooltip
		String tip = "<html><table width='" + TOOLTIP_WIDTH + "' cellspacing=0 cellpadding=0>";
		String controlStyle = "style='color: " + Util.colorToRGBString(Util.controlPathColor) + "'";
		String criticalStyle = "style='color: " + Util.colorToRGBString(Util.criticalPathColor) + "'";

		// Name
		String name = component.hasNameKey() ? Lang.t(component.getNameKey())
		                                     : component.getDefaultName();
		tip += "<tr><th><u>" + name + "</u></th></tr>";

		// Identifier and synchronous?
		tip += "<tr><td align='center'><i><tt>" + component.getId() + "</tt></i>";
		if(component instanceof Synchronous)
			tip += " (" + Lang.t("synchronous") + ")";
		tip += "</td></tr>";

		// Description
		String desc = component.getCustomDescription(Lang.getLanguage());
		if(desc == null) {
			desc = component.hasDescriptionKey() ? Lang.t(component.getDescriptionKey())
			                                     : component.getDefaultDescription();
		}
		desc = desc.replace("\n", "<br />");
		tip += "<tr><td align='center'><br />" + desc + "</td></tr>";

		// ALU operation if ALU
		if(!datapath.isInPerformanceMode() && component instanceof ALU) {
			ALU alu = (ALU)component;
			tip += "<tr><td align='center'><table>";
			tip += "<tr><td><tt>" + Lang.t("operation") + ":</tt></td><td align='right'><tt>"+ alu.getOperationName() + "</tt></td></tr>";

			// HI and LO registers if extended ALU
			if(component instanceof ExtendedALU) {
				ExtendedALU ext_alu = (ExtendedALU)alu;
				tip += "<tr><td><tt>HI:</tt></td><td align='right'><tt>" + Util.formatDataAccordingToFormat(ext_alu.getHI(), datapath.getDataFormat()) + "</tt></td></tr>";
				tip += "<tr><td><tt>LO:</tt></td><td align='right'><tt>" + Util.formatDataAccordingToFormat(ext_alu.getLO(), datapath.getDataFormat()) + "</tt></td></tr>";
			}

			tip += "</table></td></tr>";
		}


		// Latency
		if(datapath.isInPerformanceMode()) {
			tip += "<tr><td align='center'>" + Lang.t("latency") + ": " + component.getLatency() + " " + CPU.LATENCY_UNIT
				+ " <i>(" + Lang.t("double_click_to_change") + ")</i></td></tr>";
		}

		// Inputs
		tip += "<tr><td align='center'><table cellspacing=0>";
		tip += "<tr><th colspan=2><br /><u>" + Lang.t("inputs") + "</u></th></tr>";
		for(Input in: component.getInputs()) {
			if(in.isConnected()) {
				if(datapath.isInPerformanceMode() && in.isInCriticalPath())
					tip += "<tr " + criticalStyle + ">";
				else
					tip += in.isInControlPath() ? ("<tr " + controlStyle + ">") : "<tr>";
				tip += "<td><tt><b>" + in.getId() + ":</b></tt></td><td align='right'><tt><b>";
				if(datapath.isInPerformanceMode())
					tip += in.getAccumulatedLatency() + " " + CPU.LATENCY_UNIT + "</b></tt></td></tr>";
				else
					tip += Util.formatDataAccordingToFormat(in.getData(), datapath.getDataFormat())  + "</b></tt></td></tr>";
			}
		}
		// Outputs
		tip += "<tr><th colspan=2><br /><u>" + Lang.t("outputs") + "</u></th></tr>";
		for(Output out: component.getOutputs()) {
			if(out.isConnected()) {
				if(datapath.isInPerformanceMode() && out.isInCriticalPath())
					tip += "<tr " + criticalStyle + ">";
				else
					tip += out.isInControlPath() ? ("<tr " + controlStyle + ">") : "<tr>";
				tip += "<td><tt><b>" + out.getId() + ":</b></tt></td><td align='right'><tt><b>";
				if(datapath.isInPerformanceMode())
					tip += component.getAccumulatedLatency() + " " + CPU.LATENCY_UNIT + "</b></tt></td></tr>";
				else
					tip += Util.formatDataAccordingToFormat(out.getData(), datapath.getDataFormat()) + "</b></tt></td></tr>";
			}
		}
		tip += "</table></td></tr>";

		setToolTipText(tip + "</table></html>");
	}

	@Override
	public void mouseClicked(MouseEvent e) { }

	@Override
	public void mousePressed(MouseEvent e) {
		if(datapath.isInPerformanceMode() && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			String res = (String)JOptionPane.showInputDialog(datapath, Lang.t("latency_of_x", component.getId()), AppInfo.NAME, JOptionPane.QUESTION_MESSAGE, null, null, component.getLatency());
			if(res != null) {
				try {
					int lat = Integer.parseInt(res);
					if(lat >= 0) {
						component.setLatency(lat);
						datapath.getCPU().calculatePerformance();
						datapath.refresh();
						datapath.repaint();
					}
					else
						JOptionPane.showMessageDialog(this.getParent(), Lang.t("invalid_value"), AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
				}
				catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(this.getParent(), Lang.t("invalid_value"), AppInfo.NAME, JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) { }

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }
}
