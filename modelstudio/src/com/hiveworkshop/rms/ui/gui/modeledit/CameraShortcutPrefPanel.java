package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.preferences.CameraControlPrefs;
import com.hiveworkshop.rms.ui.preferences.CameraShortCut;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.ScreenInfo;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CameraShortcutPrefPanel extends JPanel {

	public CameraShortcutPrefPanel() {
		super(new MigLayout("fill", "[][][]"));
		CameraControlPrefs cameraControlPrefs = ProgramGlobals.getPrefs().getCameraControlPrefsCopy();

		JPanel settingsPanel = new JPanel(new MigLayout("fill, wrap 2", "[left][right]"));
		for (CameraShortCut cameraShortCut : cameraControlPrefs.getShortCutMap().keySet()) {
			settingsPanel.add(new JLabel(cameraShortCut.toString()));
			String kbString = TextKey.NONE.toString();
			if (cameraControlPrefs.getKeyStroke(cameraShortCut) != null) {
				kbString = cameraControlPrefs.getKeyStroke(cameraShortCut).toString();
			}
			JButton editButton = new JButton(kbString);
			editButton.addActionListener(e -> editKeyBinding(cameraShortCut, editButton, cameraControlPrefs));
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
		saveButton.addActionListener(e -> saveKeybindings(cameraControlPrefs));
		add(saveButton, "");
		JButton resetButton = new JButton("Reset");
		resetButton.setToolTipText("Reset fields to current keybindings.");
		resetButton.addActionListener(e -> cameraControlPrefs.setFrom(ProgramGlobals.getPrefs().getCameraControlPrefs()));
		add(resetButton, "");
		JButton fullResetButton = new JButton("Reset Full");
		fullResetButton.setToolTipText("Reset fields to RMS presets. Remember to save if you want to apply these keybindings!");
		fullResetButton.addActionListener(e -> cameraControlPrefs.setFrom(ProgramGlobals.getPrefs().getCameraControlPrefs()));
		add(fullResetButton, "");

	}

	private void editKeyBinding(CameraShortCut cameraShortCut, JButton button, CameraControlPrefs cameraControlPrefs) {
		JPanel panel = new JPanel(new MigLayout());
		JTextField textField = new JTextField(24);
		if (cameraControlPrefs.getKeyStroke(cameraShortCut) != null) {
			textField.setText(cameraControlPrefs.getKeyStroke(cameraShortCut).toString());
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

		int change = JOptionPane.showConfirmDialog(this, panel, "Edit KeyBinding for " + cameraShortCut.toString(), JOptionPane.OK_CANCEL_OPTION);

		if(change == JOptionPane.OK_OPTION){
			if(event[0] != null){
				cameraControlPrefs.setKeyStroke(cameraShortCut, KeyStroke.getKeyStrokeForEvent(event[0]));
				button.setText(KeyStroke.getKeyStrokeForEvent(event[0]).toString());
			} else {
				cameraControlPrefs.setKeyStroke(cameraShortCut, KeyStroke.getKeyStroke("null"));
				button.setText("None");
			}
		}
	}


	private void saveKeybindings(CameraControlPrefs cameraControlPrefs) {
		ProgramPreferences prefs = ProgramGlobals.getPrefs();
		prefs.setCameraControlPrefs(cameraControlPrefs);
//		ProgramGlobals.linkActions(ProgramGlobals.getMainPanel().getRootPane());
//		ProgramGlobals.getUndoHandler().refreshUndo();
	}

}
