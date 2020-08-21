package com.hiveworkshop.wc3.gui.modeledit.components.editors;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;

import com.etheller.warsmash.parsers.mdlx.InterpolationType;
import com.hiveworkshop.wc3.gui.modeledit.components.material.FloatTrackTableModel;
import com.hiveworkshop.wc3.mdl.AnimFlag;

import net.miginfocom.swing.MigLayout;

public class FloatValuePanel extends JPanel {
	private final JRadioButton staticButton;
	private final JRadioButton dynamicButton;
	private final ComponentEditorJSpinner staticSpinner;
	private final JComboBox<InterpolationType> interpTypeBox;
	private final FloatTrackTableModel floatTrackTableModel;

	public FloatValuePanel(final String title) {
		setBorder(BorderFactory.createTitledBorder(title));
		setLayout(new MigLayout());
		final ButtonGroup staticDynamicGroup = new ButtonGroup();
		staticButton = new JRadioButton("Static");
		dynamicButton = new JRadioButton("Dynamic");
		staticDynamicGroup.add(staticButton);
		staticDynamicGroup.add(dynamicButton);
		add(staticButton);
		staticSpinner = new ComponentEditorJSpinner(
				new SpinnerNumberModel(1.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.01));
		// below: fix this stupid nonsense java bug that a spinner with infinite value
		// range takes infinite GUI space on the screen
		final JSpinner standinGuiSpinner = new JSpinner(
				new SpinnerNumberModel(1.0, -Long.MAX_VALUE, Long.MAX_VALUE, 1.0));
		staticSpinner.setPreferredSize(standinGuiSpinner.getPreferredSize());
		staticSpinner.setMaximumSize(standinGuiSpinner.getMaximumSize());
		staticSpinner.setMinimumSize(standinGuiSpinner.getMinimumSize());
		add(staticSpinner, "wrap");
		add(dynamicButton);
		interpTypeBox = new JComboBox<InterpolationType>(InterpolationType.values());
		add(interpTypeBox, "wrap");

		floatTrackTableModel = new FloatTrackTableModel(null);
		final JTable keyframeTable = new JTable(floatTrackTableModel);
		keyframeTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value,
					final boolean isSelected, final boolean hasFocus, final int row, final int column) {
				setBackground(null);
				setForeground(null);
				final Component tableCellRendererComponent = super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);
				if ("Delete".equals(value)) {
					tableCellRendererComponent.setBackground(Color.RED);
					tableCellRendererComponent.setForeground(Color.WHITE);
				}
				return tableCellRendererComponent;
			}
		});
		add(keyframeTable, "span 2, wrap, grow");
		final ChangeListener l = new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				staticSpinner.setEnabled(staticButton.isSelected());
				interpTypeBox.setEnabled(dynamicButton.isSelected());
				keyframeTable.setVisible(dynamicButton.isSelected());
			}
		};
		staticButton.addChangeListener(l);
		dynamicButton.addChangeListener(l);
	}

	public void reloadNewValue(final float value, final AnimFlag valueTrack) {
		if (valueTrack == null) {
			staticButton.setSelected(true);
		} else {
			dynamicButton.setSelected(true);
		}
		staticSpinner.reloadNewValue(Double.valueOf(value));
		floatTrackTableModel.setTrack(valueTrack);
	}
}
