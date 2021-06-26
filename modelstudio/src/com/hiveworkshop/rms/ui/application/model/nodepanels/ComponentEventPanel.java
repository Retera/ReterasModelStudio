package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.sound.Sound;
import com.hiveworkshop.rms.util.sound.SoundPlayer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ComponentEventPanel extends ComponentIdObjectPanel<EventObject> {
	JPanel soundsPanel;

	public ComponentEventPanel(ModelHandler modelHandler, ModelStructureChangeListener changeListener) {
		super(modelHandler, changeListener);
		soundsPanel = new JPanel(new MigLayout());
		topPanel.add(soundsPanel, "wrap");
	}

	@Override
	public void updatePanels() {
		soundsPanel.removeAll();
		Sound sound = ProgramGlobals.getSoundMappings().getSound(idObject.getName());
		if (sound != null) {
			soundsPanel.add(new JLabel("Name: " + sound.getSoundName()), "wrap, gapbottom 10");
			String[] soundPaths = sound.getFilePaths();
			String[] soundNames = sound.getFileNames();
//			System.out.println("got paths");
			for (int i = 0; i < soundPaths.length; i++) {
				makeSoundButton(soundPaths[i], soundNames[i]);
			}
		}
	}

	private void makeSoundButton(String path, String name) {
		soundsPanel.add(new JLabel(name));
		JButton soundButton = new JButton("play");
		soundsPanel.add(soundButton, "wrap");

		soundButton.addActionListener(e -> {
			SoundPlayer.play(path);
		});
		soundsPanel.repaint();
		System.out.println("got sound: " + path);
	}
}
