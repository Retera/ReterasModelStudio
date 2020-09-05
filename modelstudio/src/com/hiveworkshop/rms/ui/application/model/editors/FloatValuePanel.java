package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.model.material.FloatTrackTableModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

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
		interpTypeBox = new JComboBox<>(InterpolationType.values());
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
		final ChangeListener l = e -> {
            staticSpinner.setEnabled(staticButton.isSelected());
            interpTypeBox.setEnabled(dynamicButton.isSelected());
            keyframeTable.setVisible(dynamicButton.isSelected());
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
		staticSpinner.reloadNewValue(value);
		floatTrackTableModel.setTrack(valueTrack);
	}
}