package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.preferences.KeyBindingPrefs;
import com.hiveworkshop.rms.util.ScreenInfo;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeybindingPrefPanel extends JPanel {

	public KeybindingPrefPanel() {
		super(new MigLayout("fill", "[][][]"));
		KeyBindingPrefs keyBindingPrefs = ProgramGlobals.getPrefs().getKeyBindingPrefsCopy();

		JPanel settingsPanel = new JPanel(new MigLayout("fill, wrap 2", "[left][right]"));
		for (TextKey textKey : KeyBindingPrefs.getActionFunctionMap().keySet()) {
			settingsPanel.add(new JLabel(textKey.toString()));
			String kbString = TextKey.NONE.toString();
			if (keyBindingPrefs.getKeyStroke(textKey) != null) {
				kbString = keyBindingPrefs.getKeyStroke(textKey).toString();
			}
			JButton editButton = new JButton(kbString);
			editButton.addActionListener(e -> editKeyBinding(textKey, editButton, keyBindingPrefs));
			settingsPanel.add(editButton);
		}

		settingsPanel.setOpaque(false);

		JScrollPane scrollPane = new JScrollPane(settingsPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, "spanx, growx, wrap");
		scrollPane.setPreferredSize(ScreenInfo.getSmallWindow());
//		scrollPane.setBackground(settingsPanel.getBackground());
		scrollPane.setOpaque(false);

		JButton saveButton = new JButton("Save keybindings");
		saveButton.addActionListener(e -> saveKeybindings(keyBindingPrefs));
		add(saveButton, "");
		JButton resetButton = new JButton("Reset");
		resetButton.setToolTipText("Reset fields to current keybindings.");
		resetButton.addActionListener(e -> keyBindingPrefs.parseString(ProgramGlobals.getPrefs().getKeyBindings()));
		add(resetButton, "");
		JButton fullResetButton = new JButton("Reset Full");
		fullResetButton.setToolTipText("Reset fields to RMS presets. Remember to save if you want to apply these keybindings!");
		fullResetButton.addActionListener(e -> keyBindingPrefs.parseString(ProgramGlobals.getPrefs().getKeyBindings()));
		add(fullResetButton, "");

	}

	private void editKeyBinding(TextKey textKey, JButton button, KeyBindingPrefs keyBindingPrefs) {
		JPanel panel = new JPanel(new MigLayout());
		JTextField textField = new JTextField(24);
		if (keyBindingPrefs.getKeyStroke(textKey) != null) {
			textField.setText(keyBindingPrefs.getKeyStroke(textKey).toString());
		}
		textField.setEditable(false);
		final KeyEvent[] event = {null};
		textField.addKeyListener(new KeyAdapter() {
			KeyEvent lastPressedEvent;

			@Override
			public void keyPressed(KeyEvent e) {
				lastPressedEvent = e;
				if(event[0] == null){
					textField.setText(KeyStroke.getKeyStrokeForEvent(e).toString());
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println("keyReleased ugg");
				if(event[0] == null){
					event[0] = lastPressedEvent;
				}
			}
		});

		panel.add(textField);
		JButton resetButton = new JButton("Edit");
		resetButton.addActionListener(e -> {event[0] = null; textField.setText(""); textField.requestFocus();});
		panel.add(resetButton);

		int change = JOptionPane.showConfirmDialog(this, panel, "Edit KeyBinding for " + textKey.toString(), JOptionPane.OK_CANCEL_OPTION);

		if(change == JOptionPane.OK_OPTION){
			if(event[0] != null){
				keyBindingPrefs.setKeyStroke(textKey, KeyStroke.getKeyStrokeForEvent(event[0]));
				button.setText(KeyStroke.getKeyStrokeForEvent(event[0]).toString());
			} else {
				keyBindingPrefs.setKeyStroke(textKey, KeyStroke.getKeyStroke("null"));
				button.setText("None");
			}
		}
	}

	private void saveKeybindings(KeyBindingPrefs keyBindingPrefs) {
		ProgramGlobals.getKeyBindingPrefs().parseString(keyBindingPrefs.toString());
		ProgramGlobals.getPrefs().setKeyBindings(ProgramGlobals.getKeyBindingPrefs());
		ProgramGlobals.linkActions(ProgramGlobals.getMainPanel());
		ProgramGlobals.getUndoHandler().refreshUndo();
	}
}
