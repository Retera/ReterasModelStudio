package com.hiveworkshop.rms.ui.preferences.panels;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.preferences.KeyBindingPrefs;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class KeybindingPrefPanel extends JPanel {
	private final KeyBindingPrefs keyBindingPrefs;
	private final ProgramPreferences prefs;
	private final Map<TextKey, JButton> buttonMap;

	public KeybindingPrefPanel(KeyBindingPrefs keyBindingPrefs, ProgramPreferences prefs) {
		super(new MigLayout("fill", "[][][]"));
		this.keyBindingPrefs = keyBindingPrefs;
		this.prefs = prefs;
		buttonMap = new HashMap<>();

		JPanel settingsPanel = new JPanel(new MigLayout("fill, wrap 2", "[left][right]"));
		for (TextKey textKey : KeyBindingPrefs.getActionFunctionMap().keySet()) {
			settingsPanel.add(new JLabel(textKey.toString()));

			KeyStroke keyStroke = keyBindingPrefs.getKeyStroke(textKey);
			String kbString = keyStroke == null ? TextKey.NONE.toString() : keyStroke.toString();

			JButton button = Button.create(b -> editKeyBinding(textKey, b, keyBindingPrefs), kbString);
			buttonMap.put(textKey, button);
			settingsPanel.add(button);
		}

		settingsPanel.setOpaque(false);

		JScrollPane scrollPane = new JScrollPane(settingsPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, "spanx, growx, wrap");
		scrollPane.setPreferredSize(ScreenInfo.getSmallWindow());
		scrollPane.setOpaque(false);

		add(Button.create("Save keybindings", e -> saveKeybindings(keyBindingPrefs)), "");

		JButton resetButton = Button.create("Reset", e -> setAllKB(ProgramGlobals.getKeyBindingPrefs()));
		add(Button.setTooltip(resetButton, "Reset fields to current keybindings."), "");

		JButton fullResetButton = Button.create("Reset Full", e -> setAllKB(new KeyBindingPrefs()));
		add(Button.setTooltip(fullResetButton, "Reset fields to RMS presets. Remember to save if you want to apply these keybindings!"), "");
	}

	private void setAllKB(KeyBindingPrefs prefs){
		this.keyBindingPrefs.parseString(prefs.toString());
		for (TextKey textKey : buttonMap.keySet()) {
			KeyStroke keyStroke = this.keyBindingPrefs.getKeyStroke(textKey);
			String kbString = keyStroke == null ? TextKey.NONE.toString() : keyStroke.toString();
			buttonMap.get(textKey).setText(kbString);
		}
	}

	private void editKeyBinding(TextKey textKey, JButton button, KeyBindingPrefs keyBindingPrefs) {
		JPanel panel = new JPanel(new MigLayout());

		KeyStroke keyStroke = keyBindingPrefs.getKeyStroke(textKey);
		KeySettingField keySettingField = new KeySettingField(keyStroke);

		panel.add(keySettingField);
		panel.add(Button.create(TextKey.EDIT.toString(), e -> keySettingField.onEdit()));

		int change = JOptionPane.showConfirmDialog(this, panel, "Edit KeyBinding for " + textKey.toString(), JOptionPane.OK_CANCEL_OPTION);

		if(change == JOptionPane.OK_OPTION){
			KeyStroke newKeyStroke = keySettingField.getNewKeyStroke();
			button.setText(newKeyStroke == null ? TextKey.NONE.toString() : newKeyStroke.toString());
			keyBindingPrefs.setKeyStroke(textKey, newKeyStroke);
		}
	}

	private void saveKeybindings(KeyBindingPrefs keyBindingPrefs) {
		this.prefs.setKeyBindings(keyBindingPrefs);
		ProgramGlobals.getPrefs().setKeyBindings(keyBindingPrefs).saveToFile();

		ProgramGlobals.getUndoHandler().refreshUndo();
		ProgramGlobals.linkActions(ProgramGlobals.getMainPanel());
	}
}
