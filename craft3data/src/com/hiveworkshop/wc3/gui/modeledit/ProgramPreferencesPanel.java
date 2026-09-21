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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.hiveworkshop.rms.editor.render3d.HDEnvironmentProbe;
import com.hiveworkshop.wc3.gui.GUITheme;
import com.hiveworkshop.wc3.gui.MouseButtonPreference;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.datachooser.DataSourceChooserPanel;
import com.hiveworkshop.wc3.gui.datachooser.DataSourceDescriptor;
import com.hiveworkshop.wc3.gui.util.ColorChooserIcon;
import com.hiveworkshop.wc3.gui.util.ColorChooserIcon.ColorListener;

import net.miginfocom.swing.MigLayout;

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
		generalPrefsPanel.add(new JLabel("Auto Refresh MDL Text (enabled=more lag):"), "cell 0 8");
		generalPrefsPanel.add(autoPopulateMdlTextEditor, "cell 1 8");
		generalPrefsPanel.add(new JLabel("Force Use OpenGL (may fix Windows UI bugs):"), "cell 0 9");
		generalPrefsPanel.add(disableDirectXToPreventArtifacts, "cell 1 9");
		generalPrefsPanel.add(new JLabel("Always use minimal Matrices in HD (the \"4.5\" save format):"), "cell 0 10");
		generalPrefsPanel.add(alwaysUseMinimalMatricesInHD, "cell 1 10");
		final JSpinner outlinerGlyphSize = new JSpinner(new SpinnerNumberModel(programPreferences.getOutlinerGlyphSize(),
				ProgramPreferences.MIN_OUTLINER_GLYPH_SIZE, ProgramPreferences.MAX_OUTLINER_GLYPH_SIZE, 1));
		outlinerGlyphSize.setToolTipText("Pixel size of the visible (eye) and editable (check) toggles in the Outliner.");
		outlinerGlyphSize.addChangeListener(e -> programPreferences
				.setOutlinerGlyphSize(((Number) outlinerGlyphSize.getValue()).intValue()));
		generalPrefsPanel.add(new JLabel("Outliner Toggle Icon Size:"), "cell 0 11");
		generalPrefsPanel.add(outlinerGlyphSize, "cell 1 11");
		final JCheckBox useModelLights = new JCheckBox();
		useModelLights.setSelected(programPreferences.isUseModelLights());
		useModelLights.setToolTipText("Classic (SD) models: light the preview with the model's own Light nodes using "
				+ "Warsmash's light system. Off keeps the fixed lighting; a model without lights looks the same either way.");
		useModelLights.addActionListener(e -> programPreferences.setUseModelLights(useModelLights.isSelected()));
		generalPrefsPanel.add(new JLabel("Model Lights, classic models (Warsmash lighting):"), "cell 0 12");
		generalPrefsPanel.add(useModelLights, "cell 1 12");
		final JCheckBox useModelLightsHD = new JCheckBox();
		useModelLightsHD.setSelected(programPreferences.isUseModelLightsHD());
		useModelLightsHD.setToolTipText("HD and DE models: shade with the Reforged 3.0 shader, the game's environment "
				+ "probe and the model's omni Light nodes. Off keeps the previous fixed HD lighting.");
		useModelLightsHD
				.addActionListener(e -> programPreferences.setUseModelLightsHD(useModelLightsHD.isSelected()));
		generalPrefsPanel.add(new JLabel("Model Lights, HD/DE models (Reforged shader):"), "cell 0 13");
		generalPrefsPanel.add(useModelLightsHD, "cell 1 13");
		final JComboBox<String> hdEnvironmentProbe = new JComboBox<>(HDEnvironmentProbe.PROBE_NAMES);
		hdEnvironmentProbe.setSelectedIndex(Math.max(0,
				Math.min(HDEnvironmentProbe.PROBE_NAMES.length - 1, programPreferences.getHdEnvironmentProbe())));
		hdEnvironmentProbe.setToolTipText("Which of the game's baked environment probes lights HD and DE models "
				+ "when model lights are on (Environment/EnvironmentMap in the game data).");
		hdEnvironmentProbe.addActionListener(
				e -> programPreferences.setHdEnvironmentProbe(hdEnvironmentProbe.getSelectedIndex()));
		generalPrefsPanel.add(new JLabel("HD Environment Probe:"), "cell 0 14");
		generalPrefsPanel.add(hdEnvironmentProbe, "cell 1 14");
		// final BoxLayout boxLayout = new BoxLayout(generalPrefsPanel,
		// BoxLayout.PAGE_AXIS);

		addTab("General", generalPrefsPanel);

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

		dataSourceChooserPanel = new DataSourceChooserPanel(dataSources);
		addTab("Warcraft Data", dataSourceChooserPanel);
	}

	public List<DataSourceDescriptor> getDataSources() {
		return dataSourceChooserPanel.getDataSourceDescriptors();
	}
}
