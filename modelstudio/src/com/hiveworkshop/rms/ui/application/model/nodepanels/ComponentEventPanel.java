package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.sound.Sound;
import com.hiveworkshop.rms.util.sound.SoundPlayer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ComponentEventPanel extends ComponentIdObjectPanel<EventObject> {
	JPanel soundsPanel;
	JLabel eventName;

//	private final IntegerValuePanel trackPanel;
	JPanel tracksPanel;

	public ComponentEventPanel(ModelHandler modelHandler) {
		super(modelHandler);
		soundsPanel = new JPanel(new MigLayout());
		eventName = new JLabel("");
		topPanel.add(eventName, "wrap");
		topPanel.add(soundsPanel, "wrap");

		tracksPanel = new JPanel(new MigLayout());
		topPanel.add(tracksPanel, "spanx, wrap");
//		trackPanel = new IntegerValuePanel(modelHandler, "EventTrack");
//		topPanel.add(trackPanel, "spanx, growx, wrap");
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
		eventName.setText(EventObject.getEventName(idObject.getName()));
		updateTracksPanel();
//		trackPanel.reloadNewValue(0, idObject.);
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

	ComponentEditorJSpinner staticSpinner;
	private void updateTracksPanel(){
		tracksPanel.removeAll();
		for(int track : idObject.getEventTrack()){
			staticSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(track, -Integer.MAX_VALUE, Integer.MAX_VALUE, 1));
			staticSpinner.addEditingStoppedListener(() -> editingStoppedListener(track));
			tracksPanel.add(staticSpinner, "wrap");
		}
	}

	private void editingStoppedListener(int track){
		editTrack(staticSpinner.getIntValue(), track);
	}

	private void editTrack(int track, int trackOrg){
		idObject.removeTrack(trackOrg);
		idObject.addTrack(track);
	}
}
