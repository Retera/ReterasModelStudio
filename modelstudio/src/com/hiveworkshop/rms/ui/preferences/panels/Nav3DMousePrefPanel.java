package com.hiveworkshop.rms.ui.preferences.panels;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.preferences.Nav3DMouseAction;
import com.hiveworkshop.rms.ui.preferences.Nav3DMousePrefs;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Nav3DMousePrefPanel extends JPanel {
	private final Nav3DMousePrefs nav3DMousePrefs;
	private final ProgramPreferences prefs;
	private final Map<Nav3DMouseAction, JButton> buttonMap;

	public Nav3DMousePrefPanel(Nav3DMousePrefs nav3DMousePrefs, ProgramPreferences prefs) {
		super(new MigLayout("fill", "[][][]"));
		this.nav3DMousePrefs = nav3DMousePrefs;
		this.prefs = prefs;
		buttonMap = new HashMap<>();

		JPanel settingsPanel = new JPanel(new MigLayout("fill, wrap 2", "[left][right]"));
		for (Nav3DMouseAction mouseAction : Nav3DMouseAction.values()) {
			settingsPanel.add(new JLabel(mouseAction.toString()));

			Integer keyStroke = this.nav3DMousePrefs.getKeyStroke(mouseAction);
			String kbString = keyStroke == null ? TextKey.NONE.toString() : MouseEvent.getModifiersExText(keyStroke);

			JButton button = Button.create(b -> setKB(mouseAction, editKeyBinding(mouseAction, this.nav3DMousePrefs::getKeyStroke, b)), kbString);
			buttonMap.put(mouseAction, button);
			settingsPanel.add(button);
		}

		settingsPanel.setOpaque(false);

		JScrollPane scrollPane = new JScrollPane(settingsPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, "spanx, growx, wrap");
		scrollPane.setPreferredSize(ScreenInfo.getSmallWindow());
		scrollPane.setOpaque(false);

		add(Button.create("Save keybindings", e -> saveKeybindings(this.nav3DMousePrefs)), "");

		JButton resetButton = Button.create("Reset", e -> setAllKB(ProgramGlobals.getPrefs().getNav3DMousePrefs()));
		add(Button.setTooltip(resetButton, "Reset fields to current keybindings."), "");

		JButton fullResetButton = Button.create("Reset Full", e -> setAllKB(new Nav3DMousePrefs()));
		add(Button.setTooltip(fullResetButton, ("Reset fields to RMS presets. Remember to save if you want to apply these keybindings!")), "");
	}

	private void setKB(Nav3DMouseAction cameraShortCut, Integer keyStroke) {
		nav3DMousePrefs.setKeyStroke(cameraShortCut, keyStroke);
	}

	private void setAllKB(Nav3DMousePrefs prefs) {
		this.nav3DMousePrefs.setFrom(prefs);
		for (Nav3DMouseAction cameraShortCut : buttonMap.keySet()) {
			Integer keyStroke = this.nav3DMousePrefs.getKeyStroke(cameraShortCut);
			String kbString = keyStroke == null ? TextKey.NONE.toString() : keyStroke.toString();
			buttonMap.get(cameraShortCut).setText(kbString);
		}
	}

	private Integer editKeyBinding(Nav3DMouseAction mouseAction, Function<Nav3DMouseAction, Integer> ksProvider, JButton button) {
		JPanel panel = new JPanel(new MigLayout());

		Integer keyStroke = ksProvider.apply(mouseAction);
		MouseSettingPanel mouseSettingPanel = new MouseSettingPanel(ksProvider.apply(mouseAction));

		panel.add(mouseSettingPanel);

		int change = JOptionPane.showConfirmDialog(this, panel, "Edit KeyBinding for " + mouseAction.toString(), JOptionPane.OK_CANCEL_OPTION);

		if (change == JOptionPane.OK_OPTION) {
			Integer newKeyStroke = mouseSettingPanel.getNewKeyStroke();
			button.setText(newKeyStroke == null ? TextKey.NONE.toString() : MouseEvent.getModifiersExText(newKeyStroke));
			return newKeyStroke;
		}
		return keyStroke;
	}

	private void saveKeybindings(Nav3DMousePrefs cameraControlPrefs) {
		this.prefs.setNav3DMousePrefs(cameraControlPrefs.toString());
		ProgramGlobals.getPrefs().setNav3DMousePrefs(cameraControlPrefs).saveToFile();
	}

}
