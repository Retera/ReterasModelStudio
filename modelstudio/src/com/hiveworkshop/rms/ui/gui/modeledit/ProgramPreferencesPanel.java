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

		createAndAddGeneralPrefsPanel(programPreferences);

		createAndAddModelEditorPanel(programPreferences);

		createAndAddHotkeysPanel(programPreferences);

		dataSourceChooserPanel = new DataSourceChooserPanel(dataSources);
		addTab("Warcraft Data", dataSourceChooserPanel);
	}

	private void createAndAddGeneralPrefsPanel(ProgramPreferences pref) {
		final JPanel generalPrefsPanel = new JPanel(new MigLayout());
		generalPrefsPanel.add(new JLabel("3D View Mode"), "cell 0 0");

		final JRadioButton wireframeViewMode = new JRadioButton("Wireframe");
		wireframeViewMode.addActionListener(e -> pref.setViewMode(0));
		wireframeViewMode.setSelected(pref.viewMode() == 0);
		generalPrefsPanel.add(wireframeViewMode, "cell 0 1");

		final JRadioButton solidViewMode = new JRadioButton("Solid");
		solidViewMode.addActionListener(e -> pref.setViewMode(1));
		solidViewMode.setSelected(pref.viewMode() == 1);
		generalPrefsPanel.add(solidViewMode, "cell 0 2");

		final ButtonGroup viewModes = new ButtonGroup();
		viewModes.add(wireframeViewMode);
		viewModes.add(solidViewMode);

		final JCheckBox grid2d = new JCheckBox();
		grid2d.addActionListener(e -> pref.setShow2dGrid(grid2d.isSelected()));
		grid2d.setSelected(pref.show2dGrid());
		generalPrefsPanel.add(new JLabel("Show 2D Viewport Gridlines:"), "cell 0 3");
		generalPrefsPanel.add(grid2d, "cell 1 3");

		final JCheckBox useBoxesForNodes = new JCheckBox();
		useBoxesForNodes.addActionListener(e -> pref.setUseBoxesForPivotPoints(useBoxesForNodes.isSelected()));
		useBoxesForNodes.setSelected(pref.getUseBoxesForPivotPoints());
		generalPrefsPanel.add(new JLabel("Use Boxes for Nodes:"), "cell 0 4");
		generalPrefsPanel.add(useBoxesForNodes, "cell 1 4");

		final JCheckBox quickBrowse = new JCheckBox();
		quickBrowse.addActionListener(e -> pref.setQuickBrowse(quickBrowse.isSelected()));
		quickBrowse.setSelected(pref.getQuickBrowse());
		generalPrefsPanel.add(new JLabel("Quick Browse:"), "cell 0 5");
		quickBrowse.setToolTipText("When opening a new model, close old ones if they have not been modified.");
		generalPrefsPanel.add(quickBrowse, "cell 1 5");

		final JCheckBox allowLoadingNonBlpTextures = new JCheckBox();
		allowLoadingNonBlpTextures.addActionListener(e -> pref.setAllowLoadingNonBlpTextures(allowLoadingNonBlpTextures.isSelected()));
		allowLoadingNonBlpTextures.setSelected(pref.getAllowLoadingNonBlpTextures());
		generalPrefsPanel.add(new JLabel("Allow Loading Non BLP Textures:"), "cell 0 6");
		allowLoadingNonBlpTextures.setToolTipText("Needed for opening PNGs with standard File Open");
		generalPrefsPanel.add(allowLoadingNonBlpTextures, "cell 1 6");

//		generalPrefsPanel.add(new JLabel("Render Particle Emitters:"), "cell 0 7");
		// final BoxLayout boxLayout = new BoxLayout(generalPrefsPanel,
		// BoxLayout.PAGE_AXIS);

		addTab("General", generalPrefsPanel);
	}


	private void createAndAddModelEditorPanel(ProgramPreferences pref) {
		final JPanel modelEditorPanel = new JPanel();
		modelEditorPanel.setLayout(new MigLayout("gap 0"));

		int row = 0;
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getBackgroundColor(), pref::setBackgroundColor), "Background Color:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getPerspectiveBackgroundColor(), pref::setPerspectiveBackgroundColor), "Perspective Background Color:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getVertexColor(), pref::setVertexColor), "Vertex Color:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getHighlighVertexColor(), pref::setHighlighVertexColor), "Vertex Highlight Color:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getTriangleColor(), pref::setTriangleColor), "Triangle Color:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getHighlighTriangleColor(), pref::setHighlighTriangleColor), "Triangle Highlight Color:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getSelectColor(), pref::setSelectColor), "Select Color:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getVisibleUneditableColor(), pref::setVisibleUneditableColor), "Visible Uneditable Mesh Color:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getAnimatedBoneUnselectedColor(), pref::setAnimatedBoneUnselectedColor), "Animation Editor Bone Color:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getAnimatedBoneSelectedColor(), pref::setAnimatedBoneSelectedColor), "Animation Editor Selected Bone Color:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getAnimatedBoneSelectedUpstreamColor(), pref::setAnimatedBoneSelectedUpstreamColor), "Animation Editor Selected Upstream Color:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getPivotPointsColor(), pref::setPivotPointsColor), "Pivot Point Color:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getPivotPointsSelectedColor(), pref::setPivotPointsSelectedColor), "Pivot Point Selected Color:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getActiveBColor1(), pref::setActiveBColor1), "Button B Color 1:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getActiveBColor2(), pref::setActiveBColor2), "Button B Color 2:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getActiveColor1(), pref::setActiveColor1), "Button Color 1:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getActiveColor2(), pref::setActiveColor2), "Button Color 2:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getActiveRColor1(), pref::setActiveRColor1), "Button R Color 1:");
		addAtRow(modelEditorPanel, row++, new ColorChooserIcon(pref.getActiveRColor2(), pref::setActiveRColor2), "Button R Color 2:");

		modelEditorPanel.add(new JLabel("Window Borders (Theme):"), "cell 0 " + row);

		final JComboBox<GUITheme> themeCheckBox = new JComboBox<>(GUITheme.values());
		themeCheckBox.setSelectedItem(pref.getTheme());
		themeCheckBox.addActionListener(getSettingsChanged(pref, themeCheckBox));
		modelEditorPanel.add(themeCheckBox, "cell 1 " + row++);

		addTab("Colors/Theme", new JScrollPane(modelEditorPanel));
	}

	public void addAtRow(JPanel modelEditorPanel, int row, ColorChooserIcon backgroundColorIcon, String s) {
		modelEditorPanel.add(new JLabel(s), "cell 0 " + row);
		modelEditorPanel.add(backgroundColorIcon, "cell 1 " + row);
	}

	private void createAndAddHotkeysPanel(ProgramPreferences pref) {
		int row = 0;

		final JPanel hotkeysPanel = new JPanel();
		hotkeysPanel.setLayout(new MigLayout());

		hotkeysPanel.add(new JLabel("3D Camera Spin"), "cell 0 " + row);
		final JComboBox<MouseButtonPreference> cameraSpinBox = new JComboBox<>(MouseButtonPreference.values());
		cameraSpinBox.setSelectedItem(pref.getThreeDCameraSpinButton());
		cameraSpinBox.addActionListener(e -> pref.setThreeDCameraSpinButton((MouseButtonPreference) cameraSpinBox.getSelectedItem()));
		hotkeysPanel.add(cameraSpinBox, "cell 1 " + row);

		row++;

		hotkeysPanel.add(new JLabel("3D Camera Pan"), "cell 0 " + row);
		final JComboBox<MouseButtonPreference> cameraPanBox = new JComboBox<>(MouseButtonPreference.values());
		cameraPanBox.setSelectedItem(pref.getThreeDCameraPanButton());
		cameraPanBox.addActionListener(e -> pref.setThreeDCameraPanButton((MouseButtonPreference) cameraPanBox.getSelectedItem()));
		hotkeysPanel.add(cameraPanBox, "cell 1 " + row);

		row++;
		addTab("Hotkeys", hotkeysPanel);
	}

	private ActionListener getSettingsChanged(ProgramPreferences pref, JComboBox<GUITheme> themeCheckBox) {
		return new ActionListener() {
			boolean hasWarned = false;

			@Override
			public void actionPerformed(final ActionEvent e) {
				pref.setTheme((GUITheme) themeCheckBox.getSelectedItem());
				if (!hasWarned) {
					hasWarned = true;
					JOptionPane.showMessageDialog(ProgramPreferencesPanel.this,
							"Some settings may not take effect until you restart the application.", "Warning",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		};
	}

	public List<DataSourceDescriptor> getDataSources() {
		return dataSourceChooserPanel.getDataSourceDescriptors();
	}
}
