package com.hiveworkshop.rms.ui.preferences.panels;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.preferences.CameraControlPrefs;
import com.hiveworkshop.rms.ui.preferences.CameraShortCut;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CameraShortcutPrefPanel extends JPanel {
	private final CameraControlPrefs cameraControlPrefs;
	private final ProgramPreferences prefs;
	private final Map<CameraShortCut, JButton> buttonMap;

	public CameraShortcutPrefPanel(CameraControlPrefs cameraControlPrefs, ProgramPreferences prefs) {
		super(new MigLayout("fill", "[][][]"));
		this.cameraControlPrefs = cameraControlPrefs;
		this.prefs = prefs;
		buttonMap = new HashMap<>();

		JPanel settingsPanel = new JPanel(new MigLayout("fill, wrap 2", "[left][right]"));
		for (CameraShortCut cameraShortCut : CameraShortCut.values()) {
			settingsPanel.add(new JLabel(cameraShortCut.toString()));

			KeyStroke keyStroke = this.cameraControlPrefs.getKeyStroke(cameraShortCut);
			String kbString = keyStroke == null ? TextKey.NONE.toString() : keyStroke.toString();

			JButton button = Button.create(b -> setKB(cameraShortCut, editKeyBinding(cameraShortCut, this.cameraControlPrefs::getKeyStroke, b)), kbString);
			buttonMap.put(cameraShortCut, button);
			settingsPanel.add(button);
		}

		settingsPanel.setOpaque(false);

		JScrollPane scrollPane = new JScrollPane(settingsPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, "spanx, growx, wrap");
		scrollPane.setPreferredSize(ScreenInfo.getSmallWindow());
		scrollPane.setOpaque(false);

		add(Button.create("Save keybindings", e -> saveKeybindings(this.cameraControlPrefs)), "");

		JButton resetButton = Button.create("Reset", e -> setAllKB(ProgramGlobals.getPrefs().getCameraControlPrefs()));
		add(Button.setTooltip(resetButton, "Reset fields to current keybindings."), "");

		JButton fullResetButton = Button.create("Reset Full", e -> setAllKB(new CameraControlPrefs()));
		add(Button.setTooltip(fullResetButton, ("Reset fields to RMS presets. Remember to save if you want to apply these keybindings!")), "");
	}

	private void setKB(CameraShortCut cameraShortCut, KeyStroke keyStroke) {
		cameraControlPrefs.setKeyStroke(cameraShortCut, keyStroke);
	}

	private void setAllKB(CameraControlPrefs prefs) {
		this.cameraControlPrefs.setFrom(prefs);
		for (CameraShortCut cameraShortCut : buttonMap.keySet()) {
			KeyStroke keyStroke = this.cameraControlPrefs.getKeyStroke(cameraShortCut);
			String kbString = keyStroke == null ? TextKey.NONE.toString() : keyStroke.toString();
			buttonMap.get(cameraShortCut).setText(kbString);
		}
	}

	private KeyStroke editKeyBinding(CameraShortCut cameraShortCut, Function<CameraShortCut, KeyStroke> ksProvider, JButton button) {
		JPanel panel = new JPanel(new MigLayout());

		KeyStroke keyStroke = ksProvider.apply(cameraShortCut);
		KeySettingField keySettingField = new KeySettingField(keyStroke);

		panel.add(keySettingField);
		panel.add(Button.create(TextKey.EDIT.toString(), e -> keySettingField.onEdit()));

		int change = JOptionPane.showConfirmDialog(this, panel, "Edit KeyBinding for " + cameraShortCut.toString(), JOptionPane.OK_CANCEL_OPTION);

		if (change == JOptionPane.OK_OPTION) {
			KeyStroke newKeyStroke = keySettingField.getNewKeyStroke();
			button.setText(newKeyStroke == null ? TextKey.NONE.toString() : newKeyStroke.toString());
			return newKeyStroke;
		}
		return keyStroke;
	}

	private void saveKeybindings(CameraControlPrefs cameraControlPrefs) {
		this.prefs.setCameraControlPrefs(cameraControlPrefs.toString());
		ProgramGlobals.getPrefs().setCameraControlPrefs(cameraControlPrefs).saveToFile();
	}

}
