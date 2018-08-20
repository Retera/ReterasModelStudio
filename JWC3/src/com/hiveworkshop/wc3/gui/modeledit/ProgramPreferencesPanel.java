package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.hiveworkshop.wc3.gui.MouseButtonPreference;
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
		final JCheckBox useBoxesForNodes = new JCheckBox();
		if (programPreferences.isInvertedDisplay() != null && programPreferences.isInvertedDisplay()) {
			invertedDisplay.setSelected(true);
		}
		if (programPreferences.getUseBoxesForPivotPoints() != null && programPreferences.getUseBoxesForPivotPoints()) {
			useBoxesForNodes.setSelected(true);
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

		generalPrefsPanel.setLayout(new MigLayout());
		generalPrefsPanel.add(viewModeLabel, "cell 0 0");
		generalPrefsPanel.add(wireframeViewMode, "cell 0 1");
		generalPrefsPanel.add(solidViewMode, "cell 0 2");
		generalPrefsPanel.add(new JLabel("Show Viewport Gridlines:"), "cell 0 3");
		generalPrefsPanel.add(invertedDisplay, "cell 1 3");
		generalPrefsPanel.add(new JLabel("Use Boxes for Nodes:"), "cell 0 4");
		generalPrefsPanel.add(useBoxesForNodes, "cell 1 4");
		// final BoxLayout boxLayout = new BoxLayout(generalPrefsPanel, BoxLayout.PAGE_AXIS);

		addTab("General", generalPrefsPanel);

		final JPanel modelEditorPanel = new JPanel();
		modelEditorPanel.setLayout(new MigLayout());
		invertedDisplay.addActionListener(viewModeUpdater);
		useBoxesForNodes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				programPreferences.setUseBoxesForPivotPoints(useBoxesForNodes.isSelected());
			}
		});
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
		final ColorChooserIcon animtedBoneSelectedColorIcon = new ColorChooserIcon(
				programPreferences.getAnimatedBoneSelectedColor(), new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setAnimatedBoneSelectedColor(color);
					}
				});
		final ColorChooserIcon animtedBoneUnselectedColorIcon = new ColorChooserIcon(
				programPreferences.getAnimatedBoneUnselectedColor(), new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setAnimatedBoneUnselectedColor(color);
					}
				});
		final ColorChooserIcon animtedBoneSelectedUpstreamColorIcon = new ColorChooserIcon(
				programPreferences.getAnimatedBoneSelectedUpstreamColor(), new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setAnimatedBoneSelectedUpstreamColor(color);
					}
				});
		final ColorChooserIcon pivotPointColorIcon = new ColorChooserIcon(programPreferences.getPivotPointsColor(),
				new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setPivotPointsColor(color);
					}
				});
		final ColorChooserIcon pivotPointSelectedColorIcon = new ColorChooserIcon(
				programPreferences.getPivotPointsSelectedColor(), new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setPivotPointsSelectedColor(color);
					}
				});
		int row = 0;
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
		modelEditorPanel.add(new JLabel("Animation Editor Bone Color:"), "cell 0 " + row);
		modelEditorPanel.add(animtedBoneUnselectedColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Animation Editor Selected Bone Color:"), "cell 0 " + row);
		modelEditorPanel.add(animtedBoneSelectedColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Animation Editor Selected Upstream Color:"), "cell 0 " + row);
		modelEditorPanel.add(animtedBoneSelectedUpstreamColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Pivot Point Color:"), "cell 0 " + row);
		modelEditorPanel.add(pivotPointColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Pivot Point Selected Color:"), "cell 0 " + row);
		modelEditorPanel.add(pivotPointSelectedColorIcon, "cell 1 " + row);
		row++;
		addTab("Colors/Theme", new JScrollPane(modelEditorPanel));

		final JPanel hotkeysPanel = new JPanel();
		hotkeysPanel.setLayout(new MigLayout());
		row = 0;
		final JComboBox<MouseButtonPreference> cameraSpinBox = new JComboBox<>(MouseButtonPreference.values());
		cameraSpinBox.setSelectedItem(programPreferences.getThreeDCameraSpinButton());
		final JComboBox<MouseButtonPreference> cameraPanBox = new JComboBox<>(MouseButtonPreference.values());
		cameraPanBox.setSelectedItem(programPreferences.getThreeDCameraPanButton());
		cameraSpinBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				programPreferences.setThreeDCameraSpinButton((MouseButtonPreference) cameraSpinBox.getSelectedItem());
			}
		});
		cameraPanBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				programPreferences.setThreeDCameraPanButton((MouseButtonPreference) cameraPanBox.getSelectedItem());
			}
		});
		hotkeysPanel.add(new JLabel("3D Camera Spin"), "cell 0 " + row);
		hotkeysPanel.add(cameraSpinBox, "cell 1 " + row);
		row++;
		hotkeysPanel.add(new JLabel("3D Camera Pan"), "cell 0 " + row);
		hotkeysPanel.add(cameraPanBox, "cell 1 " + row);
		row++;
		addTab("Hotkeys", hotkeysPanel);
	}
}
