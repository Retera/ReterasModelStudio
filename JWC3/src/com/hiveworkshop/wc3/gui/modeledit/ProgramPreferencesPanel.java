package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.util.ColorChooserIcon;
import com.hiveworkshop.wc3.gui.util.ColorChooserIcon.ColorListener;

import net.miginfocom.swing.MigLayout;

public final class ProgramPreferencesPanel extends JTabbedPane {
	private final ProgramPreferences programPreferences;

	public ProgramPreferencesPanel(final ProgramPreferences programPreferences) {
		this.programPreferences = programPreferences;

		final JPanel generalPrefsPanel = new JPanel();
		final JLabel viewModeLabel = new JLabel("3D View Mode");
		final JRadioButton wireframeViewMode = new JRadioButton("Wireframe");
		final JRadioButton solidViewMode = new JRadioButton("Solid");
		final JCheckBox invertedDisplay = new JCheckBox();
		if (programPreferences.isInvertedDisplay() != null && programPreferences.isInvertedDisplay()) {
			invertedDisplay.setSelected(true);
		}
		final ActionListener viewModeUpdater = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				programPreferences.setViewMode(wireframeViewMode.isSelected() ? 0 : 1);
				programPreferences.setInvertedDisplay(invertedDisplay.isSelected());
			}
		};
		wireframeViewMode.setSelected(programPreferences.viewMode() == 0);
		wireframeViewMode.addActionListener(viewModeUpdater);
		solidViewMode.setSelected(programPreferences.viewMode() == 1);
		solidViewMode.addActionListener(viewModeUpdater);
		final ButtonGroup viewModes = new ButtonGroup();
		viewModes.add(wireframeViewMode);
		viewModes.add(solidViewMode);

		generalPrefsPanel.add(viewModeLabel);
		generalPrefsPanel.add(wireframeViewMode);
		generalPrefsPanel.add(solidViewMode);
		final BoxLayout boxLayout = new BoxLayout(generalPrefsPanel, BoxLayout.PAGE_AXIS);
		generalPrefsPanel.setLayout(boxLayout);

		addTab("General", generalPrefsPanel);

		final JPanel modelEditorPanel = new JPanel();
		modelEditorPanel.setLayout(new MigLayout());
		invertedDisplay.addActionListener(viewModeUpdater);
		final ColorChooserIcon backgroundColorIcon = new ColorChooserIcon(programPreferences.getBackgroundColor(),
				new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setBackgroundColor(color);
					}
				});
		final ColorChooserIcon perspectiveBackgroundColorIcon = new ColorChooserIcon(
				programPreferences.getPerspectiveBackgroundColor(), new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setPerspectiveBackgroundColor(color);
					}
				});
		final ColorChooserIcon vertexColorIcon = new ColorChooserIcon(programPreferences.getVertexColor(),
				new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setVertexColor(color);
					}
				});
		final ColorChooserIcon triangleColorIcon = new ColorChooserIcon(programPreferences.getTriangleColor(),
				new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setTriangleColor(color);
					}
				});
		final ColorChooserIcon visibleUneditableColorIcon = new ColorChooserIcon(
				programPreferences.getVisibleUneditableColor(), new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setVisibleUneditableColor(color);
					}
				});
		final ColorChooserIcon selectColorIcon = new ColorChooserIcon(programPreferences.getSelectColor(),
				new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setSelectColor(color);
					}
				});
		final ColorChooserIcon triangleHighlightColorIcon = new ColorChooserIcon(
				programPreferences.getHighlighTriangleColor(), new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setHighlighTriangleColor(color);
					}
				});
		final ColorChooserIcon vertexHighlightColorIcon = new ColorChooserIcon(
				programPreferences.getHighlighVertexColor(), new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setHighlighVertexColor(color);
					}
				});
		int row = 0;
		modelEditorPanel.add(new JLabel("Show Viewport Gridlines:"), "cell 0 " + row);
		modelEditorPanel.add(invertedDisplay, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Background Color:"), "cell 0 " + row);
		modelEditorPanel.add(backgroundColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Vertex Color:"), "cell 0 " + row);
		modelEditorPanel.add(vertexColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Triangle Color:"), "cell 0 " + row);
		modelEditorPanel.add(triangleColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Select Color:"), "cell 0 " + row);
		modelEditorPanel.add(selectColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Triangle Highlight Color:"), "cell 0 " + row);
		modelEditorPanel.add(triangleHighlightColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Vertex Highlight Color:"), "cell 0 " + row);
		modelEditorPanel.add(vertexHighlightColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Perspective Background Color:"), "cell 0 " + row);
		modelEditorPanel.add(perspectiveBackgroundColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Visible Uneditable Mesh Color:"), "cell 0 " + row);
		modelEditorPanel.add(visibleUneditableColorIcon, "cell 1 " + row);
		row++;
		addTab("Editor", new JScrollPane(modelEditorPanel));
	}
}
