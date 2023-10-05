package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.ui.preferences.GUITheme;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.dataSourceChooser.DataSourceChooserPanel;
import com.hiveworkshop.rms.ui.util.colorchooser.ColorChooserIconLabel;
import com.hiveworkshop.rms.util.CollapsablePanel;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ProgramPreferencesPanel extends JTabbedPane {
	private final DataSourceChooserPanel dataSourceChooserPanel;
	private EditorColorPrefs colorPrefs;

	public ProgramPreferencesPanel(final ProgramPreferences programPreferences,
	                               final List<DataSourceDescriptor> dataSources) {
		setPreferredSize(ScreenInfo.getSmallWindow());

		addTab("General", getGeneralPrefsPanel(programPreferences));
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

		generalPrefsPanel.add(new JLabel("Show 2D Viewport Gridlines:"));
		generalPrefsPanel.add(getCheckBox(pref::setShow2dGrid, pref.show2dGrid()), "wrap");

		generalPrefsPanel.add(new JLabel("Use Boxes for Nodes:"));
		generalPrefsPanel.add(getCheckBox(pref::setUseBoxesForPivotPoints, pref.getUseBoxesForPivotPoints()), "wrap");

		generalPrefsPanel.add(new JLabel("Bone Box Size:"));
		generalPrefsPanel.add(new IntEditorJSpinner(pref.getNodeBoxSize(), 1, pref::setNodeBoxSize), "wrap");

		generalPrefsPanel.add(new JLabel("Vertex Square Size:"));
		generalPrefsPanel.add(new IntEditorJSpinner(pref.getVertexSize(), 1, pref::setVertexSize), "wrap");


		JCheckBox quickBrowse = getCheckBox(pref::setQuickBrowse, pref.getQuickBrowse());
		generalPrefsPanel.add(new JLabel("Quick Browse:"));
		quickBrowse.setToolTipText("When opening a new model, close old ones if they have not been modified.");
		generalPrefsPanel.add(quickBrowse, "wrap");

		JCheckBox allowLoadingNonBlpTextures = getCheckBox(pref::setAllowLoadingNonBlpTextures, pref.getAllowLoadingNonBlpTextures());
		generalPrefsPanel.add(new JLabel("Allow Loading Non BLP Textures:"));
		allowLoadingNonBlpTextures.setToolTipText("Needed for opening PNGs with standard File Open");
		generalPrefsPanel.add(allowLoadingNonBlpTextures, "wrap");

		generalPrefsPanel.add(new JLabel("Open Browsers On Startup:"));
		generalPrefsPanel.add(getCheckBox(pref::setLoadBrowsersOnStartup, pref.loadBrowsersOnStartup()), "wrap");

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

		addAtRow(modelEditorPanel, new ColorChooserIconLabel(pref.getBackgroundColor(), pref::setBackgroundColor), "Background Color:");
		addAtRow(modelEditorPanel, new ColorChooserIconLabel(pref.getActiveBColor1(), pref::setActiveBColor1), "Button B Color 1:");
		addAtRow(modelEditorPanel, new ColorChooserIconLabel(pref.getActiveBColor2(), pref::setActiveBColor2), "Button B Color 2:");
		addAtRow(modelEditorPanel, new ColorChooserIconLabel(pref.getActiveColor1(), pref::setActiveColor1), "Button Color 1:");
		addAtRow(modelEditorPanel, new ColorChooserIconLabel(pref.getActiveColor2(), pref::setActiveColor2), "Button Color 2:");
		addAtRow(modelEditorPanel, new ColorChooserIconLabel(pref.getActiveRColor1(), pref::setActiveRColor1), "Button R Color 1:");
		addAtRow(modelEditorPanel, new ColorChooserIconLabel(pref.getActiveRColor2(), pref::setActiveRColor2), "Button R Color 2:");

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

		return wrapPanel;
	}

	public void addAtRow(JPanel modelEditorPanel, ColorChooserIconLabel colorIcon, String s) {
		modelEditorPanel.add(new JLabel(s));
		modelEditorPanel.add(colorIcon, "wrap");
	}

	private JPanel getHotkeysPanel2(ProgramPreferences pref) {
		final JPanel hotkeysPanel = new JPanel();
		hotkeysPanel.setLayout(new MigLayout());
		//cameraFrontKB
		//cameraSideKB
		//cameraTopKB
		//cameraLocZoomReset
		//cameraOppositeKB

		//cameraFrontKB
		//cameraFrontKB
		//cameraFrontKB
		//cameraSideKB
		//cameraSideKB
		//cameraSideKB
		//cameraTopKB
		//cameraTopKB
		//cameraTopKB
		//cameraLocZoomReset
		//cameraLocZoomReset
		//cameraLocZoomReset
		//cameraOppositeKB
		//cameraOppositeKB
		//cameraOppositeKB


		CollapsablePanel camera_shortcuts = new CollapsablePanel("Camera Shortcuts", new CameraShortcutPrefPanel());
		hotkeysPanel.add(camera_shortcuts, "wrap");

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
		modifyButton.addActionListener(e -> pref.setModifyMouseButton(editMouseButtonBinding(modifyTextKey, modifyButton, pref.getModifyMouseButton(), false)));
		hotkeysPanel.add(modifyButton, "wrap");

		String selectTextKey = "Select";
		hotkeysPanel.add(new JLabel(selectTextKey));
		JButton selectButton = new JButton(MouseEvent.getModifiersExText(pref.getSelectMouseButton()));
		selectButton.addActionListener(e -> pref.setSelectMouseButton(editMouseButtonBinding(selectTextKey, selectButton, pref.getSelectMouseButton(), false)));
		hotkeysPanel.add(selectButton, "wrap");

		JButton edit_keybindings = new JButton("Edit Keybindings");
		edit_keybindings.addActionListener(e -> viewKBPanel());
		hotkeysPanel.add(edit_keybindings, "wrap");

		return hotkeysPanel;
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

		int change = JOptionPane.showConfirmDialog(this, panel, "Edit mouse-binding for " + textKey, JOptionPane.OK_CANCEL_OPTION);

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


	private void viewKBPanel() {
		KeybindingPrefPanel keybindingPrefPanel = new KeybindingPrefPanel();
//		keybindingPrefPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		FramePopup.show(keybindingPrefPanel, this, "Edit Keybindings");
	}

}
