package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.ui.preferences.DataSourceChooserPanel;
import com.hiveworkshop.rms.ui.preferences.GUITheme;
import com.hiveworkshop.rms.ui.preferences.MouseButtonPreference;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.ColorChooserIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public final class ProgramPreferencesPanel extends JTabbedPane {
	private final ProgramPreferences programPreferences;
	private final DataSourceChooserPanel dataSourceChooserPanel;

	public ProgramPreferencesPanel(final ProgramPreferences programPreferences,
								   final List<DataSourceDescriptor> dataSources) {
		this.programPreferences = programPreferences;

		final JPanel generalPrefsPanel = new JPanel();
		final JLabel viewModeLabel = new JLabel("3D View Mode");
		final JRadioButton wireframeViewMode = new JRadioButton("Wireframe");
		final JRadioButton solidViewMode = new JRadioButton("Solid");
		final JCheckBox invertedDisplay = new JCheckBox();
		final JCheckBox useBoxesForNodes = new JCheckBox();
		final JCheckBox quickBrowse = new JCheckBox();
		final JCheckBox allowLoadingNonBlpTextures = new JCheckBox();
		final JCheckBox renderParticles = new JCheckBox();
		if ((programPreferences.show2dGrid() != null) && programPreferences.show2dGrid()) {
			invertedDisplay.setSelected(true);
		}
		if ((programPreferences.getUseBoxesForPivotPoints() != null)
				&& programPreferences.getUseBoxesForPivotPoints()) {
			useBoxesForNodes.setSelected(true);
		}
		if ((programPreferences.getQuickBrowse() != null) && programPreferences.getQuickBrowse()) {
			quickBrowse.setSelected(true);
		}
		if ((programPreferences.getAllowLoadingNonBlpTextures() != null)
				&& programPreferences.getAllowLoadingNonBlpTextures()) {
			allowLoadingNonBlpTextures.setSelected(true);
		}
		if ((programPreferences.getRenderParticles() == null) || programPreferences.getRenderParticles()) {
			renderParticles.setSelected(true);
		}
		final ActionListener viewModeUpdater = e -> {
			programPreferences.setViewMode(wireframeViewMode.isSelected() ? 0 : 1);
			programPreferences.setShow2dGrid(invertedDisplay.isSelected());
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
		generalPrefsPanel.add(new JLabel("Quick Browse:"), "cell 0 5");
		quickBrowse.setToolTipText("When opening a new model, close old ones if they have not been modified.");
		generalPrefsPanel.add(quickBrowse, "cell 1 5");
		generalPrefsPanel.add(new JLabel("Allow Loading Non BLP Textures:"), "cell 0 6");
		allowLoadingNonBlpTextures.setToolTipText("Needed for opening PNGs with standard File Open");
		generalPrefsPanel.add(allowLoadingNonBlpTextures, "cell 1 6");
		generalPrefsPanel.add(new JLabel("Render Particle Emitters:"), "cell 0 7");
		generalPrefsPanel.add(renderParticles, "cell 1 7");
		// final BoxLayout boxLayout = new BoxLayout(generalPrefsPanel,
		// BoxLayout.PAGE_AXIS);

		addTab("General", generalPrefsPanel);

		final JPanel modelEditorPanel = new JPanel();
		modelEditorPanel.setLayout(new MigLayout());
		invertedDisplay.addActionListener(viewModeUpdater);
		quickBrowse.addActionListener(e -> programPreferences.setQuickBrowse(quickBrowse.isSelected()));
		allowLoadingNonBlpTextures.addActionListener(e -> programPreferences.setAllowLoadingNonBlpTextures(allowLoadingNonBlpTextures.isSelected()));
		renderParticles.addActionListener(e -> programPreferences.setRenderParticles(renderParticles.isSelected()));
		useBoxesForNodes.addActionListener(e -> programPreferences.setUseBoxesForPivotPoints(useBoxesForNodes.isSelected()));
		final ColorChooserIcon backgroundColorIcon = new ColorChooserIcon(programPreferences.getBackgroundColor(),
				programPreferences::setBackgroundColor);
		final ColorChooserIcon perspectiveBackgroundColorIcon = new ColorChooserIcon(
				programPreferences.getPerspectiveBackgroundColor(), programPreferences::setPerspectiveBackgroundColor);
		final ColorChooserIcon vertexColorIcon = new ColorChooserIcon(programPreferences.getVertexColor(),
				programPreferences::setVertexColor);
		final ColorChooserIcon triangleColorIcon = new ColorChooserIcon(programPreferences.getTriangleColor(),
				programPreferences::setTriangleColor);
		final ColorChooserIcon visibleUneditableColorIcon = new ColorChooserIcon(
				programPreferences.getVisibleUneditableColor(), programPreferences::setVisibleUneditableColor);
		final ColorChooserIcon selectColorIcon = new ColorChooserIcon(programPreferences.getSelectColor(),
				programPreferences::setSelectColor);
		final ColorChooserIcon triangleHighlightColorIcon = new ColorChooserIcon(
				programPreferences.getHighlighTriangleColor(), programPreferences::setHighlighTriangleColor);
		final ColorChooserIcon vertexHighlightColorIcon = new ColorChooserIcon(
				programPreferences.getHighlighVertexColor(), programPreferences::setHighlighVertexColor);
		final ColorChooserIcon animtedBoneSelectedColorIcon = new ColorChooserIcon(
				programPreferences.getAnimatedBoneSelectedColor(), programPreferences::setAnimatedBoneSelectedColor);
		final ColorChooserIcon animtedBoneUnselectedColorIcon = new ColorChooserIcon(
				programPreferences.getAnimatedBoneUnselectedColor(), programPreferences::setAnimatedBoneUnselectedColor);
		final ColorChooserIcon animtedBoneSelectedUpstreamColorIcon = new ColorChooserIcon(
				programPreferences.getAnimatedBoneSelectedUpstreamColor(), programPreferences::setAnimatedBoneSelectedUpstreamColor);
		final ColorChooserIcon pivotPointColorIcon = new ColorChooserIcon(programPreferences.getPivotPointsColor(),
				programPreferences::setPivotPointsColor);
		final ColorChooserIcon pivotPointSelectedColorIcon = new ColorChooserIcon(
				programPreferences.getPivotPointsSelectedColor(), programPreferences::setPivotPointsSelectedColor);
		final ColorChooserIcon buttonColorB1Icon = new ColorChooserIcon(programPreferences.getActiveBColor1(),
				programPreferences::setActiveBColor1);
		final ColorChooserIcon buttonColorB2Icon = new ColorChooserIcon(programPreferences.getActiveBColor2(),
				programPreferences::setActiveBColor2);
		final ColorChooserIcon buttonColor1Icon = new ColorChooserIcon(programPreferences.getActiveColor1(),
				programPreferences::setActiveColor1);
		final ColorChooserIcon buttonColor2Icon = new ColorChooserIcon(programPreferences.getActiveColor2(),
				programPreferences::setActiveColor2);
		final ColorChooserIcon buttonColorR1Icon = new ColorChooserIcon(programPreferences.getActiveRColor1(),
				programPreferences::setActiveRColor1);
		final ColorChooserIcon buttonColorR2Icon = new ColorChooserIcon(programPreferences.getActiveRColor2(),
				programPreferences::setActiveRColor2);

		final JComboBox<GUITheme> themeCheckBox = new JComboBox<>(GUITheme.values());
		themeCheckBox.setSelectedItem(programPreferences.getTheme());
		themeCheckBox.addActionListener(new ActionListener() {
			boolean hasWarned = false;

			@Override
			public void actionPerformed(final ActionEvent e) {
				programPreferences.setTheme((GUITheme) themeCheckBox.getSelectedItem());
				if (!hasWarned) {
					hasWarned = true;
					JOptionPane.showMessageDialog(ProgramPreferencesPanel.this,
							"Some settings may not take effect until you restart the application.", "Warning",
							JOptionPane.WARNING_MESSAGE);
				}
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
		modelEditorPanel.add(new JLabel("Button B Color 1:"), "cell 0 " + row);
		modelEditorPanel.add(buttonColorB1Icon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Button B Color 2:"), "cell 0 " + row);
		modelEditorPanel.add(buttonColorB2Icon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Button Color 1:"), "cell 0 " + row);
		modelEditorPanel.add(buttonColor1Icon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Button Color 2:"), "cell 0 " + row);
		modelEditorPanel.add(buttonColor2Icon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Button R Color 1:"), "cell 0 " + row);
		modelEditorPanel.add(buttonColorR1Icon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Button R Color 2:"), "cell 0 " + row);
		modelEditorPanel.add(buttonColorR2Icon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel("Window Borders (Theme):"), "cell 0 " + row);
		modelEditorPanel.add(themeCheckBox, "cell 1 " + row);

		addTab("Colors/Theme", new JScrollPane(modelEditorPanel));

		final JPanel hotkeysPanel = new JPanel();
		hotkeysPanel.setLayout(new MigLayout());
		row = 0;
		final JComboBox<MouseButtonPreference> cameraSpinBox = new JComboBox<>(MouseButtonPreference.values());
		cameraSpinBox.setSelectedItem(programPreferences.getThreeDCameraSpinButton());
		final JComboBox<MouseButtonPreference> cameraPanBox = new JComboBox<>(MouseButtonPreference.values());
		cameraPanBox.setSelectedItem(programPreferences.getThreeDCameraPanButton());
		cameraSpinBox.addActionListener(e -> programPreferences.setThreeDCameraSpinButton((MouseButtonPreference) cameraSpinBox.getSelectedItem()));
		cameraPanBox.addActionListener(e -> programPreferences.setThreeDCameraPanButton((MouseButtonPreference) cameraPanBox.getSelectedItem()));
		hotkeysPanel.add(new JLabel("3D Camera Spin"), "cell 0 " + row);
		hotkeysPanel.add(cameraSpinBox, "cell 1 " + row);
		row++;
		hotkeysPanel.add(new JLabel("3D Camera Pan"), "cell 0 " + row);
		hotkeysPanel.add(cameraPanBox, "cell 1 " + row);
		row++;
		addTab("Hotkeys", hotkeysPanel);

		dataSourceChooserPanel = new DataSourceChooserPanel(dataSources);
		addTab("Warcraft Data", dataSourceChooserPanel);
	}

	public List<DataSourceDescriptor> getDataSources() {
		return dataSourceChooserPanel.getDataSourceDescriptors();
	}
}
