package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.preferences.*;
import com.hiveworkshop.rms.ui.util.ColorChooserIcon;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public final class ProgramPreferencesPanel extends JTabbedPane {
	private final ProgramPreferences programPreferences;
	private final DataSourceChooserPanel dataSourceChooserPanel;
	private EditorColorPrefs colorPrefs;

	public ProgramPreferencesPanel(final ProgramPreferences programPreferences,
	                               final List<DataSourceDescriptor> dataSources) {
		this.programPreferences = programPreferences;
		setPreferredSize(ScreenInfo.getSmallWindow());

		addTab("General", getGeneralPrefsPanel(programPreferences));
//		addTab("Colors/Theme", new JScrollPane(getModelEditorPanel(programPreferences)));
		addTab("Colors/Theme", getModelEditorPanel(programPreferences));
		addTab("Hotkeys", getHotkeysPanel2(programPreferences));

		dataSourceChooserPanel = new DataSourceChooserPanel(dataSources);
		addTab("Warcraft Data", dataSourceChooserPanel);
	}

	private JPanel getGeneralPrefsPanel(ProgramPreferences pref) {
		JPanel generalPrefsPanel = new JPanel(new MigLayout());
		generalPrefsPanel.add(new JLabel("3D View Mode"), "wrap");

		SmartButtonGroup viewModeGroup = new SmartButtonGroup();
		viewModeGroup.addJRadioButton("Wireframe", e -> pref.setViewMode(0));
		viewModeGroup.addJRadioButton("Solid", e -> pref.setViewMode(1));
		viewModeGroup.setSelectedIndex(pref.viewMode());
		generalPrefsPanel.add(viewModeGroup.getButtonPanel(), "wrap");


		JCheckBox grid2d = getCheckBox(pref::setShow2dGrid, pref.show2dGrid());
		generalPrefsPanel.add(new JLabel("Show 2D Viewport Gridlines:"));
		generalPrefsPanel.add(grid2d, "wrap");

		JCheckBox useBoxesForNodes = getCheckBox(pref::setUseBoxesForPivotPoints, pref.getUseBoxesForPivotPoints());
		generalPrefsPanel.add(new JLabel("Use Boxes for Nodes:"));
		generalPrefsPanel.add(useBoxesForNodes, "wrap");

		JCheckBox quickBrowse = getCheckBox(pref::setQuickBrowse, pref.getQuickBrowse());
		generalPrefsPanel.add(new JLabel("Quick Browse:"));
		quickBrowse.setToolTipText("When opening a new model, close old ones if they have not been modified.");
		generalPrefsPanel.add(quickBrowse, "wrap");

		JCheckBox allowLoadingNonBlpTextures = getCheckBox(pref::setAllowLoadingNonBlpTextures, pref.getAllowLoadingNonBlpTextures());
		generalPrefsPanel.add(new JLabel("Allow Loading Non BLP Textures:"));
		allowLoadingNonBlpTextures.setToolTipText("Needed for opening PNGs with standard File Open");
		generalPrefsPanel.add(allowLoadingNonBlpTextures, "wrap");

//		generalPrefsPanel.add(new JLabel("Render Particle Emitters:"), "cell 0 7");
		// final BoxLayout boxLayout = new BoxLayout(generalPrefsPanel,
		// BoxLayout.PAGE_AXIS);

		return generalPrefsPanel;
	}

	private JCheckBox getCheckBox(Consumer<Boolean> checkboxConsumer, Boolean initialValue) {
		JCheckBox checkBox = new JCheckBox();
		checkBox.addActionListener(e -> checkboxConsumer.accept(checkBox.isSelected()));
		checkBox.setSelected(initialValue);
		return checkBox;
	}

	public EditorColorPrefs getColorPrefs() {
		return colorPrefs;
	}

	private JPanel getModelEditorPanel(ProgramPreferences pref) {
		final JPanel modelEditorPanel = new JPanel(new MigLayout("gap 0"));

		EditorColorsPrefPanel colorsPrefPanel = new EditorColorsPrefPanel();
		this.colorPrefs = colorsPrefPanel.getColorPrefs();
		modelEditorPanel.add(colorsPrefPanel, "wrap");

		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getBackgroundColor(), pref::setBackgroundColor), "Background Color:");
//		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getPerspectiveBackgroundColor(), pref::setPerspectiveBackgroundColor), "Perspective Background Color:");
//		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getVertexColor(), pref::setVertexColor), "Vertex Color:");
//		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getHighlighVertexColor(), pref::setHighlighVertexColor), "Vertex Highlight Color:");
//		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getTriangleColor(), pref::setTriangleColor), "Triangle Color:");
//		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getHighlighTriangleColor(), pref::setHighlighTriangleColor), "Triangle Highlight Color:");
//		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getSelectColor(), pref::setSelectColor), "Select Color:");
//		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getVisibleUneditableColor(), pref::setVisibleUneditableColor), "Visible Uneditable Mesh Color:");
//		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getAnimatedBoneUnselectedColor(), pref::setAnimatedBoneUnselectedColor), "Animation Editor Bone Color:");
//		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getAnimatedBoneSelectedColor(), pref::setAnimatedBoneSelectedColor), "Animation Editor Selected Bone Color:");
//		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getAnimatedBoneSelectedUpstreamColor(), pref::setAnimatedBoneSelectedUpstreamColor), "Animation Editor Selected Upstream Color:");
//		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getPivotPointsColor(), pref::setPivotPointsColor), "Pivot Point Color:");
//		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getPivotPointsSelectedColor(), pref::setPivotPointsSelectedColor), "Pivot Point Selected Color:");
		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getActiveBColor1(), pref::setActiveBColor1), "Button B Color 1:");
		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getActiveBColor2(), pref::setActiveBColor2), "Button B Color 2:");
		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getActiveColor1(), pref::setActiveColor1), "Button Color 1:");
		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getActiveColor2(), pref::setActiveColor2), "Button Color 2:");
		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getActiveRColor1(), pref::setActiveRColor1), "Button R Color 1:");
		addAtRow(modelEditorPanel, new ColorChooserIcon(pref.getActiveRColor2(), pref::setActiveRColor2), "Button R Color 2:");

		modelEditorPanel.add(new JLabel("Window Borders (Theme):"));

		final JComboBox<GUITheme> themeCheckBox = new JComboBox<>(GUITheme.values());
		themeCheckBox.setSelectedItem(pref.getTheme());
		themeCheckBox.addActionListener(getSettingsChanged(pref, themeCheckBox));
		modelEditorPanel.add(themeCheckBox, "wrap");

		JScrollPane scrollPane = new JScrollPane(modelEditorPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setBorder(null);
		final JPanel wrapPanel = new JPanel(new MigLayout("gap 0, ins 0, fill"));
		wrapPanel.add(scrollPane, "growx, growy");
		modelEditorPanel.add(new JLabel("4"), "wrap");
		modelEditorPanel.add(new JLabel("4"), "wrap");

		return wrapPanel;
	}

	public void addAtRow(JPanel modelEditorPanel, ColorChooserIcon colorIcon, String s) {
		modelEditorPanel.add(new JLabel(s));
		modelEditorPanel.add(colorIcon, "wrap");
	}

	private JPanel getHotkeysPanel(ProgramPreferences pref) {
		final JPanel hotkeysPanel = new JPanel();
		hotkeysPanel.setLayout(new MigLayout());

		hotkeysPanel.add(new JLabel("3D Camera Spin"));
		final JComboBox<MouseButtonPreference> cameraSpinBox = new JComboBox<>(MouseButtonPreference.values());
		cameraSpinBox.setSelectedItem(pref.getThreeDCameraSpinButton());
		cameraSpinBox.addActionListener(e -> pref.setThreeDCameraSpinButton((MouseButtonPreference) cameraSpinBox.getSelectedItem()));
		hotkeysPanel.add(cameraSpinBox, "wrap");


		hotkeysPanel.add(new JLabel("3D Camera Pan"));
		final JComboBox<MouseButtonPreference> cameraPanBox = new JComboBox<>(MouseButtonPreference.values());
		cameraPanBox.setSelectedItem(pref.getThreeDCameraPanButton());
		cameraPanBox.addActionListener(e -> pref.setThreeDCameraPanButton((MouseButtonPreference) cameraPanBox.getSelectedItem()));
		hotkeysPanel.add(cameraPanBox, "wrap");

		return hotkeysPanel;
	}

	private JPanel getHotkeysPanel2(ProgramPreferences pref) {
		final JPanel hotkeysPanel = new JPanel();
		hotkeysPanel.setLayout(new MigLayout());

		String spinTextKey = "Camera Spin";
		hotkeysPanel.add(new JLabel(spinTextKey));
		JButton spinButton = new JButton(MouseEvent.getModifiersExText(pref.getThreeDCameraSpinMouseEx()));
		spinButton.addActionListener(e -> pref.setThreeDCameraSpinMouseEx(editMouseButtonBinding(spinTextKey, spinButton, pref.getThreeDCameraSpinMouseEx(), false)));
		hotkeysPanel.add(spinButton, "wrap");

		String panTextKey = "Camera Pan";
		hotkeysPanel.add(new JLabel(panTextKey));
		JButton panButton = new JButton(MouseEvent.getModifiersExText(pref.getThreeDCameraPanMouseEx()));
		panButton.addActionListener(e -> pref.setThreeDCameraPanMouseEx(editMouseButtonBinding(panTextKey, panButton, pref.getThreeDCameraPanMouseEx(), false)));
		hotkeysPanel.add(panButton, "wrap");

		String modifyTextKey = "Manipulate";
		hotkeysPanel.add(new JLabel(modifyTextKey));
		JButton modifyButton = new JButton(MouseEvent.getModifiersExText(pref.getModifyMouseButton()));
		modifyButton.addActionListener(e -> pref.setModifyMouseButton(editMouseButtonBinding(modifyTextKey, modifyButton, pref.getModifyMouseButton(), true)));
		hotkeysPanel.add(modifyButton, "wrap");

		String selectTextKey = "Select";
		hotkeysPanel.add(new JLabel(selectTextKey));
		JButton selectButton = new JButton(MouseEvent.getModifiersExText(pref.getSelectMouseButton()));
		selectButton.addActionListener(e -> pref.setSelectMouseButton(editMouseButtonBinding(selectTextKey, selectButton, pref.getSelectMouseButton(), true)));
		hotkeysPanel.add(selectButton, "wrap");

		return hotkeysPanel;
	}


	private int editMouseButtonBinding(TextKey textKey, JButton button, int mouseModEx) {
		JPanel panel = new JPanel(new MigLayout());
		JLabel bindingLabel = new JLabel(MouseEvent.getModifiersExText(mouseModEx));
		panel.add(bindingLabel);
		final int[] newModEx = {mouseModEx};
		JButton mouseListenButton = new JButton("Click to change binding");
		mouseListenButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				newModEx[0] = e.getModifiersEx();
				bindingLabel.setText(MouseEvent.getModifiersExText(newModEx[0]));
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
			}
		});

		panel.add(mouseListenButton);
//		JButton resetButton = new JButton("Edit");
//		resetButton.addActionListener(e -> {event[0] = null; textField.setText(""); textField.requestFocus();});
//		panel.add(resetButton);

		int change = JOptionPane.showConfirmDialog(this, panel, "Edit mouse-binding for " + textKey.toString(), JOptionPane.OK_CANCEL_OPTION);

		if (change == JOptionPane.OK_OPTION) {
			button.setText(MouseEvent.getModifiersExText(newModEx[0]));
			return newModEx[0];
		}
		return mouseModEx;
	}

	private int editMouseButtonBinding(String textKey, JButton button, int mouseModEx, boolean ignoreModifiers) {
		JPanel panel = new JPanel(new MigLayout());
		JLabel bindingLabel = new JLabel(MouseEvent.getModifiersExText(mouseModEx));
		panel.add(bindingLabel);
		final int[] newModEx = {mouseModEx};
		JButton mouseListenButton = new JButton("Click to change binding");
		mouseListenButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (ignoreModifiers) {
					newModEx[0] = MouseEvent.getMaskForButton(e.getButton());
				} else {
					newModEx[0] = e.getModifiersEx();
				}
				bindingLabel.setText(MouseEvent.getModifiersExText(newModEx[0]));
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
			}
		});

		panel.add(mouseListenButton);
//		JButton resetButton = new JButton("Edit");
//		resetButton.addActionListener(e -> {event[0] = null; textField.setText(""); textField.requestFocus();});
//		panel.add(resetButton);

		int change = JOptionPane.showConfirmDialog(this, panel, "Edit mouse-binding for " + textKey.toString(), JOptionPane.OK_CANCEL_OPTION);

		if (change == JOptionPane.OK_OPTION) {
			button.setText(MouseEvent.getModifiersExText(newModEx[0]));
			return newModEx[0];
		}
		return mouseModEx;
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
