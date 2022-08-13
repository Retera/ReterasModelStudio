package com.hiveworkshop.wc3.gui.modeledit.components.editors;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.timeline.SetFloat3StaticValueAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.components.material.FloatTrackTableModel;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.timelines.InterpolationType;
import com.hiveworkshop.wc3.util.Callback;
import com.hiveworkshop.wc3.util.IconUtils;

import net.miginfocom.swing.MigLayout;

public class ColorValuePanel extends JPanel {
	private static final Vertex DEFAULT_COLOR = new Vertex(1, 1, 1);
	private final JRadioButton staticButton;
	private final JRadioButton dynamicButton;
	private final JComboBox<InterpolationType> interpTypeBox;
	private final FloatTrackTableModel floatTrackTableModel;
	private final JButton staticColorButton;
	private UndoActionListener undoActionListener;
	private ModelStructureChangeListener modelStructureChangeListener;
	private Vertex lastLoadedStaticColor;
	private Callback<Vertex> setter;

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
		staticColorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Color newColor = JColorChooser.showDialog(ColorValuePanel.this.getRootPane(), "Choose " + title,
						new Color((float) lastLoadedStaticColor.z, (float) lastLoadedStaticColor.y,
								(float) lastLoadedStaticColor.x));

				if (newColor != null) {
					final SetFloat3StaticValueAction setFloat3StaticValueAction = new SetFloat3StaticValueAction(title,
							lastLoadedStaticColor,
							new Vertex(newColor.getBlue() / 255f, newColor.getGreen() / 255f, newColor.getRed() / 255f),
							setter);
					setFloat3StaticValueAction.redo();
					undoActionListener.pushAction(setFloat3StaticValueAction);
				}
			}
		});
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

	public void reloadNewValue(final Vertex color, final Callback<Vertex> setter, final AnimFlag colorTrack,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.setter = setter;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		if (colorTrack == null) {
			staticButton.setSelected(true);
		} else {
			dynamicButton.setSelected(true);
		}
		if (color != null) {
			this.lastLoadedStaticColor = color;
			staticColorButton.setIcon(new ImageIcon(IconUtils.createColorImage(color, 48, 48)));
		} else {
			this.lastLoadedStaticColor = DEFAULT_COLOR;
			staticColorButton.setIcon(new ImageIcon(IconUtils.createColorImage(DEFAULT_COLOR, 48, 48)));
		}
		floatTrackTableModel.setTrack(colorTrack);
	}

}
