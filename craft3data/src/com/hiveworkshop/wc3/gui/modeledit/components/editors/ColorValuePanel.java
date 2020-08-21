package com.hiveworkshop.wc3.gui.modeledit.components.editors;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.hiveworkshop.wc3.gui.modeledit.components.material.FloatTrackTableModel;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.timelines.InterpolationType;
import com.hiveworkshop.wc3.util.IconUtils;

import net.miginfocom.swing.MigLayout;

public class ColorValuePanel extends JPanel {
	private static final Vertex DEFAULT_COLOR = new Vertex(1, 1, 1);
	private final JRadioButton staticButton;
	private final JRadioButton dynamicButton;
	private final JComboBox<InterpolationType> interpTypeBox;
	private final FloatTrackTableModel floatTrackTableModel;
	private final JButton staticColorButton;

	public ColorValuePanel(final String title) {
		setBorder(BorderFactory.createTitledBorder(title));
		setLayout(new MigLayout());
		final ButtonGroup staticDynamicGroup = new ButtonGroup();
		staticButton = new JRadioButton("Static");
		dynamicButton = new JRadioButton("Dynamic");
		staticDynamicGroup.add(staticButton);
		staticDynamicGroup.add(dynamicButton);
		add(staticButton);
		staticColorButton = new JButton("Choose Color");
		add(staticColorButton, "wrap");
		add(dynamicButton);
		interpTypeBox = new JComboBox<InterpolationType>(InterpolationType.values());
		add(interpTypeBox, "wrap");

		floatTrackTableModel = new FloatTrackTableModel(null);
		final JTable keyframeTable = new JTable(floatTrackTableModel);
		add(keyframeTable, "span 2, wrap, grow");
		final ChangeListener l = new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				staticColorButton.setEnabled(staticButton.isSelected());
				interpTypeBox.setEnabled(dynamicButton.isSelected());
				keyframeTable.setVisible(dynamicButton.isSelected());
			}
		};
		staticButton.addChangeListener(l);
		dynamicButton.addChangeListener(l);
	}

	public void reloadNewValue(final Vertex color, final AnimFlag colorTrack) {
		if (colorTrack == null) {
			staticButton.setSelected(true);
		} else {
			dynamicButton.setSelected(true);
		}
		if (color != null) {
			staticColorButton.setIcon(new ImageIcon(IconUtils.createColorImage(color, 48, 48)));
		} else {
			staticColorButton.setIcon(new ImageIcon(IconUtils.createColorImage(DEFAULT_COLOR, 48, 48)));
		}
		floatTrackTableModel.setTrack(colorTrack);
	}

}
