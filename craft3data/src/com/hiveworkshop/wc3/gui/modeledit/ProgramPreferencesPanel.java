package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.hiveworkshop.wc3.gui.GUITheme;
import com.hiveworkshop.wc3.gui.MouseButtonPreference;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.datachooser.DataSourceChooserPanel;
import com.hiveworkshop.wc3.gui.datachooser.DataSourceDescriptor;
import com.hiveworkshop.wc3.gui.util.ColorChooserIcon;
import com.hiveworkshop.wc3.gui.util.ColorChooserIcon.ColorListener;
import com.localizationmanager.localization.LocalizationManager;

import net.miginfocom.swing.MigLayout;

public final class ProgramPreferencesPanel extends JTabbedPane {
	private final ProgramPreferences programPreferences;
	private final DataSourceChooserPanel dataSourceChooserPanel;

	public ProgramPreferencesPanel(final ProgramPreferences programPreferences,
			final List<DataSourceDescriptor> dataSources) {
		this.programPreferences = programPreferences;

		final JPanel generalPrefsPanel = new JPanel();
		final JLabel viewModeLabel = new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_viewmodelabel"));
		final JRadioButton wireframeViewMode = new JRadioButton(LocalizationManager.getInstance().get("radiobutton.programpreferencespanel_programpreferencespanel_wireframeviewmode"));
		final JRadioButton solidViewMode = new JRadioButton(LocalizationManager.getInstance().get("radiobutton.programpreferencespanel_programpreferencespanel_solidviewmode"));
		final JCheckBox invertedDisplay = new JCheckBox();
		final JCheckBox useBoxesForNodes = new JCheckBox();
		final JCheckBox quickBrowse = new JCheckBox();
		final JCheckBox allowLoadingNonBlpTextures = new JCheckBox();
		final JCheckBox renderParticles = new JCheckBox();
		final JCheckBox autoPopulateMdlTextEditor = new JCheckBox();
		final JCheckBox disableDirectXToPreventArtifacts = new JCheckBox();
		final JCheckBox alwaysUseMinimalMatricesInHD = new JCheckBox();
		if (programPreferences.isInvertedDisplay() == null || programPreferences.isInvertedDisplay()) {
			invertedDisplay.setSelected(true);
		}
		if (programPreferences.getUseBoxesForPivotPoints() == null || programPreferences.getUseBoxesForPivotPoints()) {
			useBoxesForNodes.setSelected(true);
		}
		if (programPreferences.getQuickBrowse() == null || programPreferences.getQuickBrowse()) {
			quickBrowse.setSelected(true);
		}
		if (programPreferences.getAllowLoadingNonBlpTextures() == null
				|| programPreferences.getAllowLoadingNonBlpTextures()) {
			allowLoadingNonBlpTextures.setSelected(true);
		}
		if (programPreferences.getRenderParticles() == null || programPreferences.getRenderParticles()) {
			renderParticles.setSelected(true);
		}
		if (programPreferences.getAutoPopulateMdlTextEditor() != null
				&& programPreferences.getAutoPopulateMdlTextEditor()) {
			autoPopulateMdlTextEditor.setSelected(true);
		}
		if (programPreferences.getDisableDirectXToSolveVisualArtifacts() != null
				&& programPreferences.getDisableDirectXToSolveVisualArtifacts()) {
			disableDirectXToPreventArtifacts.setSelected(true);
		}
		if (programPreferences.isAlwaysUseMinimalMatricesInHD()) {
			alwaysUseMinimalMatricesInHD.setSelected(true);
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
		generalPrefsPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_gridlines")), "cell 0 3");
		generalPrefsPanel.add(invertedDisplay, "cell 1 3");
		generalPrefsPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_boxes")), "cell 0 4");
		generalPrefsPanel.add(useBoxesForNodes, "cell 1 4");
		generalPrefsPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_browse")), "cell 0 5");
		quickBrowse.setToolTipText(LocalizationManager.getInstance().get("settooltiptext.programpreferencespanel_programpreferencespanel_close_model"));
		generalPrefsPanel.add(quickBrowse, "cell 1 5");
		generalPrefsPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_loading_blp")), "cell 0 6");
		allowLoadingNonBlpTextures.setToolTipText(LocalizationManager.getInstance().get("settooltiptext.programpreferencespanel_programpreferencespanel_opening_png"));
		generalPrefsPanel.add(allowLoadingNonBlpTextures, "cell 1 6");
		generalPrefsPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_render")), "cell 0 7");
		generalPrefsPanel.add(renderParticles, "cell 1 7");
		generalPrefsPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_mdl_text")), "cell 0 8");
		generalPrefsPanel.add(autoPopulateMdlTextEditor, "cell 1 8");
		generalPrefsPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_opengl")), "cell 0 9");
		generalPrefsPanel.add(disableDirectXToPreventArtifacts, "cell 1 9");
		generalPrefsPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_minimal_matrices")), "cell 0 10");
		generalPrefsPanel.add(alwaysUseMinimalMatricesInHD, "cell 1 10");
		// final BoxLayout boxLayout = new BoxLayout(generalPrefsPanel,
		// BoxLayout.PAGE_AXIS);

		addTab(LocalizationManager.getInstance().get("addtab.programpreferencespanel_programpreferencespanel_general"), generalPrefsPanel);

		final JPanel modelEditorPanel = new JPanel();
		modelEditorPanel.setLayout(new MigLayout());
		invertedDisplay.addActionListener(viewModeUpdater);
		quickBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				programPreferences.setQuickBrowse(quickBrowse.isSelected());
			}
		});
		allowLoadingNonBlpTextures.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				programPreferences.setAllowLoadingNonBlpTextures(allowLoadingNonBlpTextures.isSelected());
			}
		});
		renderParticles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				programPreferences.setRenderParticles(renderParticles.isSelected());
			}
		});
		autoPopulateMdlTextEditor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				programPreferences.setAutoPopulateMdlTextEditor(autoPopulateMdlTextEditor.isSelected());
			}
		});
		disableDirectXToPreventArtifacts.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				programPreferences
						.setDisableDirectXToSolveVisualArtifacts(disableDirectXToPreventArtifacts.isSelected());
			}
		});
		alwaysUseMinimalMatricesInHD.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				programPreferences.setAlwaysUseMinimalMatricesInHD(alwaysUseMinimalMatricesInHD.isSelected());
			}
		});
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
		final ColorChooserIcon buttonColorB1Icon = new ColorChooserIcon(programPreferences.getActiveBColor1(),
				new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setActiveBColor1(color);
					}
				});
		final ColorChooserIcon buttonColorB2Icon = new ColorChooserIcon(programPreferences.getActiveBColor2(),
				new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setActiveBColor2(color);
					}
				});
		final ColorChooserIcon buttonColor1Icon = new ColorChooserIcon(programPreferences.getActiveColor1(),
				new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setActiveColor1(color);
					}
				});
		final ColorChooserIcon buttonColor2Icon = new ColorChooserIcon(programPreferences.getActiveColor2(),
				new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setActiveColor2(color);
					}
				});
		final ColorChooserIcon buttonColorR1Icon = new ColorChooserIcon(programPreferences.getActiveRColor1(),
				new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setActiveRColor1(color);
					}
				});
		final ColorChooserIcon buttonColorR2Icon = new ColorChooserIcon(programPreferences.getActiveRColor2(),
				new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						programPreferences.setActiveRColor2(color);
					}
				});

		final JComboBox<GUITheme> themeCheckBox = new JComboBox<GUITheme>(GUITheme.values());
		themeCheckBox.setSelectedItem(programPreferences.getTheme());
		themeCheckBox.addActionListener(new ActionListener() {
			boolean hasWarned = false;

			@Override
			public void actionPerformed(final ActionEvent e) {
				programPreferences.setTheme((GUITheme) themeCheckBox.getSelectedItem());
				if (!hasWarned) {
					hasWarned = true;
					JOptionPane.showMessageDialog(ProgramPreferencesPanel.this,
							LocalizationManager.getInstance().get("dialog.programpreferencespanel_programpreferencespanel_restart"),
							LocalizationManager.getInstance().get("dialog.programpreferencespanel_programpreferencespanel_restart_1"),
							JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		int row = 0;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_background_color")), "cell 0 " + row);
		modelEditorPanel.add(backgroundColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_vertex_color")), "cell 0 " + row);
		modelEditorPanel.add(vertexColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_triangle_color")), "cell 0 " + row);
		modelEditorPanel.add(triangleColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_select_color")), "cell 0 " + row);
		modelEditorPanel.add(selectColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_triangle_highlight_color")), "cell 0 " + row);
		modelEditorPanel.add(triangleHighlightColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_vertex_highlight_color")), "cell 0 " + row);
		modelEditorPanel.add(vertexHighlightColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_perspective_background_color")), "cell 0 " + row);
		modelEditorPanel.add(perspectiveBackgroundColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_visible_uneditable_mesh_color")), "cell 0 " + row);
		modelEditorPanel.add(visibleUneditableColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_animation_bone_color")), "cell 0 " + row);
		modelEditorPanel.add(animtedBoneUnselectedColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_animation_selected_bone_color")), "cell 0 " + row);
		modelEditorPanel.add(animtedBoneSelectedColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_animation_selected_upstream_color")), "cell 0 " + row);
		modelEditorPanel.add(animtedBoneSelectedUpstreamColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_pivot_color")), "cell 0 " + row);
		modelEditorPanel.add(pivotPointColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_pivot_selected_color")), "cell 0 " + row);
		modelEditorPanel.add(pivotPointSelectedColorIcon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_button_color_b1")), "cell 0 " + row);
		modelEditorPanel.add(buttonColorB1Icon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_button_color_b2")), "cell 0 " + row);
		modelEditorPanel.add(buttonColorB2Icon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_button_color_1")), "cell 0 " + row);
		modelEditorPanel.add(buttonColor1Icon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_button_color_2")), "cell 0 " + row);
		modelEditorPanel.add(buttonColor2Icon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_button_color_r1")), "cell 0 " + row);
		modelEditorPanel.add(buttonColorR1Icon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_button_color_r2")), "cell 0 " + row);
		modelEditorPanel.add(buttonColorR2Icon, "cell 1 " + row);
		row++;
		modelEditorPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_theme")), "cell 0 " + row);
		modelEditorPanel.add(themeCheckBox, "cell 1 " + row);

		addTab(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_color_theme"), new JScrollPane(modelEditorPanel));

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
		hotkeysPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_camera_spin")), "cell 0 " + row);
		hotkeysPanel.add(cameraSpinBox, "cell 1 " + row);
		row++;
		hotkeysPanel.add(new JLabel(LocalizationManager.getInstance().get("label.programpreferencespanel_programpreferencespanel_camera_pan")), "cell 0 " + row);
		hotkeysPanel.add(cameraPanBox, "cell 1 " + row);
		row++;
		addTab(LocalizationManager.getInstance().get("addtab.programpreferencespanel_programpreferencespanel_hotkeys"), hotkeysPanel);

		dataSourceChooserPanel = new DataSourceChooserPanel(dataSources);
		addTab(LocalizationManager.getInstance().get("addtab.programpreferencespanel_programpreferencespanel_data"), dataSourceChooserPanel);
	}

	public List<DataSourceDescriptor> getDataSources() {
		return dataSourceChooserPanel.getDataSourceDescriptors();
	}
}
