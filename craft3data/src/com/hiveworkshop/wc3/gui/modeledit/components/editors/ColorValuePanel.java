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

import com.etheller.warsmash.parsers.mdlx.InterpolationType;
import com.hiveworkshop.wc3.gui.modeledit.components.material.FloatTrackTableModel;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Vertex;
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
		this.staticButton = new JRadioButton("Static");
		this.dynamicButton = new JRadioButton("Dynamic");
		staticDynamicGroup.add(this.staticButton);
		staticDynamicGroup.add(this.dynamicButton);
		add(this.staticButton);
		this.staticColorButton = new JButton("Choose Color");
		add(this.staticColorButton, "wrap");
		add(this.dynamicButton);
		this.interpTypeBox = new JComboBox<InterpolationType>(InterpolationType.values());
		add(this.interpTypeBox, "wrap");

		this.floatTrackTableModel = new FloatTrackTableModel(null);
		final JTable keyframeTable = new JTable(this.floatTrackTableModel);
		add(keyframeTable, "span 2, wrap, grow");
		final ChangeListener l = new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				ColorValuePanel.this.staticColorButton.setEnabled(ColorValuePanel.this.staticButton.isSelected());
				ColorValuePanel.this.interpTypeBox.setEnabled(ColorValuePanel.this.dynamicButton.isSelected());
				keyframeTable.setVisible(ColorValuePanel.this.dynamicButton.isSelected());
			}
		};
		this.staticButton.addChangeListener(l);
		this.dynamicButton.addChangeListener(l);
	}

	public void reloadNewValue(final Vertex color, final AnimFlag colorTrack) {
		if (colorTrack == null) {
			this.staticButton.setSelected(true);
		} else {
			this.dynamicButton.setSelected(true);
		}
		if (color != null) {
			this.staticColorButton.setIcon(new ImageIcon(IconUtils.createColorImage(color, 48, 48)));
		} else {
			this.staticColorButton.setIcon(new ImageIcon(IconUtils.createColorImage(DEFAULT_COLOR, 48, 48)));
		}
		this.floatTrackTableModel.setTrack(colorTrack);
	}

}
